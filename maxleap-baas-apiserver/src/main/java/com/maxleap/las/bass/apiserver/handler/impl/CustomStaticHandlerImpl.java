package com.maxleap.las.bass.apiserver.handler.impl;

import com.maxleap.las.bass.apiserver.handler.CustomStaticHandler;
import io.vertx.core.*;
import io.vertx.core.file.FileProps;
import io.vertx.core.file.FileSystem;
import io.vertx.core.file.FileSystemException;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonArray;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.impl.LRUCache;
import io.vertx.ext.web.impl.Utils;

import java.io.File;
import java.nio.file.NoSuchFileException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.netty.handler.codec.http.HttpResponseStatus.*;

/**
 * @author sneaky
 * @since 1.0.0
 */
public class CustomStaticHandlerImpl implements CustomStaticHandler {
  private static final Logger log = LoggerFactory.getLogger(CustomStaticHandlerImpl.class);
  private final DateFormat dateTimeFormatter = Utils.createRFC1123DateTimeFormatter();
  private Map<String, CacheEntry> propsCache;
  private String webRoot = DEFAULT_WEB_ROOT;
  private long maxAgeSeconds = DEFAULT_MAX_AGE_SECONDS; // One day
  private boolean directoryListing = DEFAULT_DIRECTORY_LISTING;
  private String directoryTemplateResource = DEFAULT_DIRECTORY_TEMPLATE;
  private String directoryTemplate;
  private boolean includeHidden = DEFAULT_INCLUDE_HIDDEN;
  private boolean filesReadOnly = DEFAULT_FILES_READ_ONLY;
  private boolean cachingEnabled = DEFAULT_CACHING_ENABLED;
  private long cacheEntryTimeout = DEFAULT_CACHE_ENTRY_TIMEOUT;
  private String indexPage = DEFAULT_INDEX_PAGE;
  private int maxCacheSize = DEFAULT_MAX_CACHE_SIZE;
  private boolean rangeSupport = DEFAULT_RANGE_SUPPORT;

  // These members are all related to auto tuning of synchronous vs asynchronous file system access
  private static int NUM_SERVES_TUNING_FS_ACCESS = 1000;
  private boolean alwaysAsyncFS = DEFAULT_ALWAYS_ASYNC_FS;
  private long maxAvgServeTimeNanoSeconds = DEFAULT_MAX_AVG_SERVE_TIME_NS;
  private boolean tuning = DEFAULT_ENABLE_FS_TUNING;
  private long totalTime;
  private long numServesBlocking;
  private boolean useAsyncFS;
  private long nextAvgCheck = NUM_SERVES_TUNING_FS_ACCESS;

  private StaticFileContainer staticFileContainer;

  public CustomStaticHandlerImpl() {
    staticFileContainer = new StaticFileContainer(this.webRoot);
    staticFileContainer.load();
  }

  public CustomStaticHandlerImpl(String webRoot) {
    this.webRoot = webRoot;
    staticFileContainer = new StaticFileContainer(this.webRoot);
    staticFileContainer.load();
  }

  private String directoryTemplate(Vertx vertx) {
    if (directoryTemplate == null) {
      directoryTemplate = Utils.readFileToString(vertx, directoryTemplateResource);
    }
    return directoryTemplate;
  }

  private void writeCacheHeaders(HttpServerRequest request, FileProps props) {
    MultiMap headers = request.response().headers();
    if (cachingEnabled) {
      // We use cache-control and last-modified
      // We *do not use* etags and expires (since they do the same thing - redundant)
      headers.set("cache-control", "public, max-age=" + maxAgeSeconds);
      headers.set("last-modified", dateTimeFormatter.format(props.lastModifiedTime()));
    }
  }

  private void resetTuning() {
    // Reset
    nextAvgCheck = NUM_SERVES_TUNING_FS_ACCESS;
    totalTime = 0;
    numServesBlocking = 0;
  }

  private static final Pattern RANGE = Pattern.compile("^bytes=(\\d+)-(\\d*)$");

  @Override
  public void handle(RoutingContext context) {
    HttpServerRequest request = context.request();
    if (request.method() != HttpMethod.GET && request.method() != HttpMethod.HEAD || !staticFileContainer.isMapping(Utils.pathOffset(request.path(), context))) {
      if (log.isTraceEnabled()) log.trace("Not GET or HEAD so ignoring request");
      context.next();
    } else {
      String path = context.normalisedPath();
      // if the normalized path is null it cannot be resolved
      if (path == null) {
        log.warn("Invalid path: " + context.request().path() + " so returning 404");
        context.fail(NOT_FOUND.code());
        return;
      }

      // only root is known for sure to be a directory. all other directories must be identified as such.
      if (!directoryListing && "/".equals(path)) {
        path = indexPage;
      }
      sendStatic(context, staticFileContainer.resolveFilePath(path));
    }
  }

  private void sendStatic(RoutingContext context, String path) {
    String file = getFile(path, context);
    if (!includeHidden) {
      int idx = file.lastIndexOf('/');
      String name = file.substring(idx + 1);
      if (name.length() > 0 && name.charAt(0) == '.') {
        context.fail(NOT_FOUND.code());
        return;
      }
    }

    // Look in cache
    //Response 304
    CacheEntry entry = null;
    if (cachingEnabled) {
      entry = propsCache().get(path);
      if (entry != null) {
        HttpServerRequest request = context.request();
        if ((filesReadOnly || !entry.isOutOfDate()) && entry.shouldUseCached(request)) {
          context.response().setStatusCode(NOT_MODIFIED.code()).end();
          return;
        }
      }
    }

    FileProps props;
    if (filesReadOnly && entry != null) {
      props = entry.props;
      sendFile(context, file, props);
    } else {
      // Need to read the props from the filesystem
      String sfile = file;
      getFileProps(context, file, res -> {
        if (res.succeeded()) {
          FileProps fprops = res.result();
          if (fprops == null) {
            // File does not exist
            context.fail(NOT_FOUND.code());
          } else if (fprops.isDirectory()) {
            sendDirectory(context, path, sfile);
          } else {
            propsCache().put(path, new CacheEntry(fprops, System.currentTimeMillis()));
            sendFile(context, sfile, fprops);
          }
        } else {
          if (res.cause() instanceof NoSuchFileException || (res.cause().getCause() != null && res.cause().getCause() instanceof NoSuchFileException)) {
            context.fail(NOT_FOUND.code());
          } else {
            context.fail(res.cause());
          }
        }
      });

    }
  }

  private void sendFile(RoutingContext context, String file, FileProps fileProps) {
    HttpServerRequest request = context.request();
    Long offset = null;
    Long end = null;
    MultiMap headers = null;

    if (rangeSupport) {
      // check if the client is making a range request
      String range = request.getHeader("Range");
      // end byte is length - 1
      end = fileProps.size() - 1;

      if (range != null) {
        Matcher m = RANGE.matcher(range);
        if (m.matches()) {
          try {
            String part = m.group(1);
            // offset cannot be empty
            offset = Long.parseLong(part);
            // offset must fall inside the limits of the file
            if (offset < 0 || offset >= fileProps.size()) {
              throw new IndexOutOfBoundsException();
            }
            // length can be empty
            part = m.group(2);
            if (part != null && part.length() > 0) {
              // ranges are inclusive
              end = Long.parseLong(part);
              // offset must fall inside the limits of the file
              if (end < offset || end >= fileProps.size()) {
                throw new IndexOutOfBoundsException();
              }
            }
          } catch (NumberFormatException | IndexOutOfBoundsException e) {
            context.fail(REQUESTED_RANGE_NOT_SATISFIABLE.code());
            return;
          }
        }
      }

      // notify client we support range requests
      headers = request.response().headers();
      headers.set("Accept-Ranges", "bytes");
      // send the content length even for HEAD requests
      headers.set("Content-Length", Long.toString(end + 1 - (offset == null ? 0 : offset)));
    }

    writeCacheHeaders(request, fileProps);

    if (request.method() == HttpMethod.HEAD) {
      request.response().end();
    } else {
      if (rangeSupport && offset != null) {
        // must return content range
        headers.set("Content-Range", "bytes " + offset + "-" + end + "/" + fileProps.size());
        // return a partial response
        request.response().setStatusCode(PARTIAL_CONTENT.code());

        final Long finalOffset = offset;
        final Long finalEnd = end;
        wrapInTCCLSwitch(() ->
            request.response().sendFile(file, finalOffset, finalEnd + 1, res2 -> {
              if (res2.failed()) {
                context.fail(res2.cause());
              }
            }), null);
      } else {
        // Wrap the sendFile operation into a TCCL switch, so the file resolver would find the file from the set
        // classloader (if any).
        wrapInTCCLSwitch(() ->
            request.response().sendFile(file, res2 -> {
                  if (res2.failed()) {
                    context.fail(res2.cause());
                  }
                }
            ), null);
      }
    }
  }
  private void sendDirectory(RoutingContext context, String path, String file) {
    if (directoryListing) {
      sendDirectoryListing(file, context);
    } else if (indexPage != null) {
      // send index page
      String indexPath;
      if (path.endsWith("/") && indexPage.startsWith("/")) {
        indexPath = path + indexPage.substring(1);
      } else if (!path.endsWith("/") && !indexPage.startsWith("/")) {
        indexPath = path + "/" + indexPage.substring(1);
      } else {
        indexPath = path + indexPage;
      }
      // recursive call
      sendStatic(context, indexPath);

    } else {
      // Directory listing denied
      context.fail(FORBIDDEN.code());
    }
  }

  private <T> T wrapInTCCLSwitch(Callable<T> callable, Handler<AsyncResult<FileProps>> resultHandler) {
    try {
      return callable.call();
    } catch (Exception e) {
      if (resultHandler != null) {
        resultHandler.handle(Future.failedFuture(e.getCause()));
        return null;
      } else {
        throw new RuntimeException(e);
      }
    }
  }

  private synchronized void getFileProps(RoutingContext context, String file, Handler<AsyncResult<FileProps>> resultHandler) {
    FileSystem fs = context.vertx().fileSystem();
    if (alwaysAsyncFS || useAsyncFS) {
      wrapInTCCLSwitch(() -> fs.props(file, resultHandler), resultHandler);
    } else {
      // Use synchronous access - it might well be faster!
      long start = 0;
      if (tuning) {
        start = System.nanoTime();
      }
      try {
        FileProps props = wrapInTCCLSwitch(() -> fs.propsBlocking(file), resultHandler);

        if (tuning) {
          long end = System.nanoTime();
          long dur = end - start;
          totalTime += dur;
          numServesBlocking++;
          if (numServesBlocking == Long.MAX_VALUE) {
            // Unlikely.. but...
            resetTuning();
          } else if (numServesBlocking == nextAvgCheck) {
            double avg = (double) totalTime / numServesBlocking;
            if (avg > maxAvgServeTimeNanoSeconds) {
              useAsyncFS = true;
              log.info("Switching to async file system access in static file server as fs access is slow! (Average access time of " + avg + " ns)");
              tuning = false;
            }
            nextAvgCheck += NUM_SERVES_TUNING_FS_ACCESS;
          }
        }
        resultHandler.handle(Future.succeededFuture(props));
      } catch (FileSystemException e) {
        resultHandler.handle(Future.failedFuture(e.getCause()));
      }
    }
  }

  private Map<String, CacheEntry> propsCache() {
    if (propsCache == null) {
      propsCache = new LRUCache<>(maxCacheSize);
    }
    return propsCache;
  }

  private Date parseDate(String header) {
    try {
      return dateTimeFormatter.parse(header);
    } catch (ParseException e) {
      throw new VertxException(e);
    }
  }

  private String getFile(String path, RoutingContext context) {
    String file = webRoot + Utils.pathOffset(path, context);
    if (log.isTraceEnabled()) log.trace("File to serve is " + file);
    return file;
  }

  private void setRoot(String webRoot) {
    this.webRoot = webRoot;
  }

  /**
   * Directory list
   */
  private void sendDirectoryListing(String dir, RoutingContext context) {
    FileSystem fileSystem = context.vertx().fileSystem();
    HttpServerRequest request = context.request();

    fileSystem.readDir(dir, asyncResult -> {
      if (asyncResult.failed()) {
        context.fail(asyncResult.cause());
      } else {

        String accept = request.headers().get("accept");
        if (accept == null) {
          accept = "text/plain";
        }

        if (accept.contains("html")) {
          String normalizedDir = context.normalisedPath();
          if (!normalizedDir.endsWith("/")) {
            normalizedDir += "/";
          }

          String file;
          StringBuilder files = new StringBuilder("<ul id=\"files\">");

          List<String> list = asyncResult.result();
          Collections.sort(list);

          for (String s : list) {
            file = s.substring(s.lastIndexOf(File.separatorChar) + 1);
            // skip dot files
            if (!includeHidden && file.charAt(0) == '.') {
              continue;
            }
            files.append("<li><a href=\"");
            files.append(normalizedDir);
            files.append(file);
            files.append("\" title=\"");
            files.append(file);
            files.append("\">");
            files.append(file);
            files.append("</a></li>");
          }

          files.append("</ul>");

          // link to parent dir
          int slashPos = 0;
          for (int i = normalizedDir.length() - 2; i > 0; i--) {
            if (normalizedDir.charAt(i) == '/') {
              slashPos = i;
              break;
            }
          }

          String parent = "<a href=\"" + normalizedDir.substring(0, slashPos + 1) + "\">..</a>";

          request.response().putHeader("content-type", "text/html");
          request.response().end(
              directoryTemplate(context.vertx()).replace("{directory}", normalizedDir)
                  .replace("{parent}", parent)
                  .replace("{files}", files.toString()));
        } else if (accept.contains("json")) {
          String file;
          JsonArray json = new JsonArray();

          for (String s : asyncResult.result()) {
            file = s.substring(s.lastIndexOf(File.separatorChar) + 1);
            // skip dot files
            if (!includeHidden && file.charAt(0) == '.') {
              continue;
            }
            json.add(file);
          }
          request.response().putHeader("content-type", "application/json");
          request.response().end(json.encode());
        } else {
          String file;
          StringBuilder buffer = new StringBuilder();

          for (String s : asyncResult.result()) {
            file = s.substring(s.lastIndexOf(File.separatorChar) + 1);
            // skip dot files
            if (!includeHidden && file.charAt(0) == '.') {
              continue;
            }
            buffer.append(file);
            buffer.append('\n');
          }

          request.response().putHeader("content-type", "text/plain");
          request.response().end(buffer.toString());
        }
      }
    });
  }

  @Override
  public CustomStaticHandler setFilesReadOnly(boolean readOnly) {
    this.filesReadOnly = readOnly;
    return this;
  }

  @Override
  public CustomStaticHandler setMaxAgeSeconds(long maxAgeSeconds) {
    this.maxAgeSeconds = maxAgeSeconds;
    return this;
  }

  @Override
  public CustomStaticHandler setCachingEnabled(boolean enabled) {
    this.cachingEnabled = enabled;
    return this;
  }

  @Override
  public CustomStaticHandler setDirectoryListing(boolean directoryListing) {
    this.directoryListing = directoryListing;
    return this;
  }

  @Override
  public CustomStaticHandler setIncludeHidden(boolean includeHidden) {
    this.includeHidden = includeHidden;
    return this;
  }

  @Override
  public CustomStaticHandler setCacheEntryTimeout(long timeout) {
    this.cacheEntryTimeout = timeout;
    return this;
  }

  @Override
  public CustomStaticHandler setIndexPage(String indexPage) {
    this.indexPage = indexPage;
    return this;
  }

  @Override
  public CustomStaticHandler setMaxCacheSize(int maxCacheSize) {
    this.maxCacheSize = maxCacheSize;
    return this;
  }

  @Override
  public CustomStaticHandler setAlwaysAsyncFS(boolean alwaysAsyncFS) {
    this.alwaysAsyncFS = alwaysAsyncFS;
    return this;
  }

  @Override
  public CustomStaticHandler setEnableFSTuning(boolean enableFSTuning) {
    this.tuning = enableFSTuning;
    return this;
  }

  @Override
  public CustomStaticHandler setMaxAvgServeTimeNs(long maxAvgServeTimeNanoSeconds) {
    this.maxAvgServeTimeNanoSeconds = maxAvgServeTimeNanoSeconds;
    return this;
  }

  @Override
  public CustomStaticHandler setDirectoryTemplate(String directoryTemplate) {
    this.directoryTemplate = directoryTemplate;
    return this;
  }

  @Override
  public CustomStaticHandler setEnableRangeSupport(boolean enableRangeSupport) {
    this.rangeSupport = enableRangeSupport;
    return this;
  }

  // TODO make this static and use Java8 DateTimeFormatter
  private final class CacheEntry {
    final FileProps props;
    long createDate;

    private CacheEntry(FileProps props, long createDate) {
      this.props = props;
      this.createDate = createDate;
    }

    // return true if there are conditional headers present and they match what is in the entry
    boolean shouldUseCached(HttpServerRequest request) {
      String ifModifiedSince = request.headers().get("if-modified-since");
      if (ifModifiedSince == null) {
        // Not a conditional request
        return false;
      }
      Date ifModifiedSinceDate = parseDate(ifModifiedSince);
      boolean modifiedSince = Utils.secondsFactor(props.lastModifiedTime()) > ifModifiedSinceDate.getTime();
      return !modifiedSince;
    }

    boolean isOutOfDate() {
      boolean outOfDate = System.currentTimeMillis() - createDate > cacheEntryTimeout;
      return outOfDate;
    }
  }

  public class StaticFileContainer {
    private Set<String> staticFiles;
    private String parentDir;

    public StaticFileContainer(String webroot) {
      String cwdOverride = System.getProperty("vertx.cwd");
      if (cwdOverride != null) {
        parentDir = cwdOverride + "/" + webroot;
      } else {
        parentDir = webroot;
      }
    }

    public void load() {
      try {
        staticFiles = findFiles(new File(parentDir));
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    public Set<String> findFiles(File file) {
      Set<String> staticFiles = new HashSet<>();
      File[] currentFiles = file.listFiles();
      for (int i = 0; i < currentFiles.length; i++) {
        if (currentFiles[i].isFile()) {
          staticFiles.add(resolveFilePath(currentFiles[i]));
        } else if (currentFiles[i].isDirectory()) {
          staticFiles.addAll(findFiles(currentFiles[i]));
        }
      }
      return staticFiles;
    }

    public boolean isMapping(String path) {
     if (staticFiles.contains(path) || staticFiles.contains(path + ".html")) {
       return true;
     } else if (path.indexOf("clouddata/apps") >= 0 || path.indexOf("settings/apps") >= 0) {
       return true;
     } else {
       return false;
     }
    }

    public String resolveFilePath(String path) {
      if (staticFiles.contains(path)) {
        return path;
      } else if (staticFiles.contains(path + ".html")) {
        return path + ".html";
      } else if (path.indexOf("clouddata/apps") >= 0) {
        return "/clouddata.html";
      } else if (path.indexOf("settings/apps") >= 0) {
        return "/settings.html";
      } else {
        throw new IllegalArgumentException("path is invalid. path: " + path);
      }
    }

    public String resolveFilePath(File file) {
      String path = file.getPath();
      return path.substring(parentDir.length());
    }

  }

}
