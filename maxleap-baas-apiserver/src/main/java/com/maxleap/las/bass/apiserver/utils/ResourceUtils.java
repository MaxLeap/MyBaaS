package com.maxleap.las.bass.apiserver.utils;

import com.maxleap.utils.Assert;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author sneaky
 * @since 1.0.0
 */
public abstract class ResourceUtils {
  private final static Logger logger = LoggerFactory.getLogger(ResourceUtils.class);

  public static final String FILE_URL_PREFIX = "file:";

  public static final String URL_PROTOCOL_JAR = "jar";

  public static final String URL_PROTOCOL_ZIP = "zip";

  public static final String URL_PROTOCOL_WSJAR = "wsjar";

  public static final String URL_PROTOCOL_VFSZIP = "vfszip";

  public static final String JAR_URL_SEPARATOR = "!/";

  public static Set<Class> findAllClass(String resourceLocation) {
    try {
      URL url = getURL(resourceLocation);
      if (isJarURL(url)) {
        Set<Class> classes = doFindJarClasses(url);
        return classes;
      } else {
        return doFindFileClasses(getFile(url));
      }
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
    }
    return new HashSet<>();
  }

  public static URL getURL(String resourceLocation) throws FileNotFoundException {
    Assert.notNull(resourceLocation, "Resource location must not be null");
    Assert.notNull(resourceLocation, "Resource location must not be null");
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    URL url = (cl != null ? cl.getResource(resourceLocation) : ClassLoader.getSystemResource(resourceLocation));
    if (url == null) {
      throw new FileNotFoundException(
          resourceLocation + " cannot be resolved to absolute file path " +
              "because it does not reside in the file system");
    }
    return url;
  }

  public static File getFile(String resourceLocation) throws FileNotFoundException {
    return getFile(getURL(resourceLocation));
  }

  public static Set<Class> doFindJarClasses(URL url)
      throws IOException {
    URLConnection con = url.openConnection();
    JarFile jarFile;
    String jarFileUrl;
    String rootEntryPath;
    boolean newJarFile = false;

    if (con instanceof JarURLConnection) {
      // Should usually be the case for traditional JAR files.
      JarURLConnection jarCon = (JarURLConnection) con;
      jarFile = jarCon.getJarFile();
      jarFileUrl = jarCon.getJarFileURL().toExternalForm();
      JarEntry jarEntry = jarCon.getJarEntry();
      rootEntryPath = (jarEntry != null ? jarEntry.getName() : "");
    }
    else {
      String urlFile = url.getFile();
      int separatorIndex = urlFile.indexOf(ResourceUtils.JAR_URL_SEPARATOR);
      if (separatorIndex != -1) {
        jarFileUrl = urlFile.substring(0, separatorIndex);
        rootEntryPath = urlFile.substring(separatorIndex + ResourceUtils.JAR_URL_SEPARATOR.length());
        jarFile = getJarFile(jarFileUrl);
      }
      else {
        jarFile = new JarFile(urlFile);
        jarFileUrl = urlFile;
        rootEntryPath = "";
      }
      newJarFile = true;
    }

    try {
      if (!"".equals(rootEntryPath) && !rootEntryPath.endsWith("/")) {
        // Root entry path must end with slash to allow for proper matching.
        // The Sun JRE does not return a slash here, but BEA JRockit does.
        rootEntryPath = rootEntryPath + "/";
      }
      Set<Class> result = new LinkedHashSet<>(8);
      for (Enumeration<JarEntry> entries = jarFile.entries(); entries.hasMoreElements();) {
        JarEntry entry = entries.nextElement();
        String entryPath = entry.getName();
        if (entryPath.startsWith(rootEntryPath)) {
          Class clazz = loadClass(entryPath.replace("/", "."));
          if (clazz != null) {
            result.add(clazz);
          }
        }
      }
      return result;
    }
    finally {
      // Close jar file, but only if freshly obtained -
      // not from JarURLConnection, which might cache the file reference.
      if (newJarFile) {
        jarFile.close();
      }
    }
  }

  public static Set<Class> doFindFileClasses(File file) {
    Set<Class> classes = new HashSet<>();
    if (file.isDirectory()) {
      File[] files = file.listFiles();
      for (int i = 0; i < files.length; i++) {
        if (files[i].isFile()) {
          from(files[i]).ifPresent(classes::add);
        } else if (files[i].isDirectory()) {
          classes.addAll(doFindFileClasses(files[i]));
        }
      }
    }
    return classes;
  }

  public static Optional<Class> from(File file) {
    if (file.isFile() && file.getPath().endsWith(".class")) {
      return extractFullClassName(file.getPath()).map(c -> {
        try {
          return Class.forName(c);
        } catch (ClassNotFoundException e) {
          logger.error(e.getMessage(), e);
          return null;
        }
      });
    }
    return Optional.empty();
  }

  public static Optional<String> extractFullClassName(String path) {
    int start = path.indexOf("classes");
    int end = path.lastIndexOf(".class");
    if (start > 0 && end > 0) {
      return Optional.of(path.substring(start + 8, end).replace("/", "."));
    } else {
      return Optional.empty();
    }
  }

  private static Class loadClass(String className) {
    try {
      int end = className.lastIndexOf(".class");
      if (end > 0) {
        return Class.forName(className.substring(0, end));
      }
    } catch (ClassNotFoundException e) {
      logger.error(e.getMessage(), e);
    }
    return null;
  }

  /**
   * Resolve the given jar file URL into a JarFile object.
   */
  public static JarFile getJarFile(String jarFileUrl) throws IOException {
    if (jarFileUrl.startsWith(ResourceUtils.FILE_URL_PREFIX)) {
      try {
        return new JarFile(ResourceUtils.toURI(jarFileUrl).getSchemeSpecificPart());
      }
      catch (URISyntaxException ex) {
        // Fallback for URLs that are not valid URIs (should hardly ever happen).
        return new JarFile(jarFileUrl.substring(ResourceUtils.FILE_URL_PREFIX.length()));
      }
    }
    else {
      return new JarFile(jarFileUrl);
    }
  }

  public static File getFile(URL resourceUrl) throws FileNotFoundException {
    Assert.notNull(resourceUrl, "Resource URL must not be null");
    try {
      return new File(toURI(resourceUrl).getSchemeSpecificPart());
    } catch (URISyntaxException ex) {
      // Fallback for URLs that are not valid URIs (should hardly ever happen).
      return new File(resourceUrl.getFile());
    }
  }

  public static URI toURI(URL url) throws URISyntaxException {
    return toURI(url.toString());
  }

  public static URI toURI(String location) throws URISyntaxException {
    return new URI(StringUtils.replace(location, " ", "%20"));
  }

  public static boolean isJarURL(URL url) {
    String protocol = url.getProtocol();
    return (URL_PROTOCOL_JAR.equals(protocol) || URL_PROTOCOL_ZIP.equals(protocol) ||
        URL_PROTOCOL_VFSZIP.equals(protocol) || URL_PROTOCOL_WSJAR.equals(protocol));
  }

}
