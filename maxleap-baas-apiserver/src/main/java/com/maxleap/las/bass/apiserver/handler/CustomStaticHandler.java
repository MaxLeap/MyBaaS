package com.maxleap.las.bass.apiserver.handler;


import com.maxleap.las.bass.apiserver.handler.impl.CustomStaticHandlerImpl;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

/**
 * A handler for serving static resources from the file system.
 *
 * @author sneaky
 * @since 1.0.0
 */
public interface CustomStaticHandler extends Handler<RoutingContext> {
  String DEFAULT_WEB_ROOT = "webroot";

  boolean DEFAULT_FILES_READ_ONLY = true;

  long DEFAULT_MAX_AGE_SECONDS = 86400; // One day

  boolean DEFAULT_CACHING_ENABLED = true;

  boolean DEFAULT_DIRECTORY_LISTING = false;

  String DEFAULT_DIRECTORY_TEMPLATE = "vertx-web-directory.html";

  boolean DEFAULT_INCLUDE_HIDDEN = true;

  long DEFAULT_CACHE_ENTRY_TIMEOUT = 30000; // 30 seconds

  String DEFAULT_INDEX_PAGE = "/index.html";

  int DEFAULT_MAX_CACHE_SIZE = 10000;

  boolean DEFAULT_ALWAYS_ASYNC_FS = false;

  boolean DEFAULT_ENABLE_FS_TUNING = true;

  long DEFAULT_MAX_AVG_SERVE_TIME_NS = 1000000; // 1ms

  boolean DEFAULT_RANGE_SUPPORT = true;

  static CustomStaticHandler create() {
    return new CustomStaticHandlerImpl();
  }

  static CustomStaticHandler create(String root) {
    return new CustomStaticHandlerImpl(root);
  }

  CustomStaticHandler setFilesReadOnly(boolean readOnly);

  CustomStaticHandler setMaxAgeSeconds(long maxAgeSeconds);

  CustomStaticHandler setCachingEnabled(boolean enabled);

  CustomStaticHandler setDirectoryListing(boolean directoryListing);

  CustomStaticHandler setIncludeHidden(boolean includeHidden);

  CustomStaticHandler setCacheEntryTimeout(long timeout);

  CustomStaticHandler setIndexPage(String indexPage);

  CustomStaticHandler setMaxCacheSize(int maxCacheSize);

  CustomStaticHandler setAlwaysAsyncFS(boolean alwaysAsyncFS);

  CustomStaticHandler setEnableFSTuning(boolean enableFSTuning);

  CustomStaticHandler setMaxAvgServeTimeNs(long maxAvgServeTimeNanoSeconds);

  CustomStaticHandler setDirectoryTemplate(String directoryTemplate);

  CustomStaticHandler setEnableRangeSupport(boolean enableRangeSupport);
}

