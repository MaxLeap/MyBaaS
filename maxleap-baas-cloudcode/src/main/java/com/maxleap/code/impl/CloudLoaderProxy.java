package com.maxleap.code.impl;

import com.maxleap.code.*;
import com.maxleap.las.sdk.FindMsg;
import com.maxleap.las.sdk.MLQuery;
import com.maxleap.las.sdk.MLUpdate;
import com.maxleap.las.sdk.UpdateMsg;
import io.netty.util.internal.ConcurrentSet;
import io.vertx.core.impl.ConcurrentHashSet;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.*;
import java.lang.reflect.Proxy;
import java.net.*;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Created by stream.
 */
final public class CloudLoaderProxy implements InvocationHandler {
  private static final Logger logger = LoggerFactory.getLogger(CloudLoaderProxy.class);

  private LoaderBase loader;
  private GlobalConfig globalConfig;
  private ClassLoader classLoader;
  private Set<String> parsedJars;
  private Set<Class> hookClasses;
  private MLClassManager<_SYS_EntityHook> classesHookManager;

  public CloudLoaderProxy() {
    this(null);
  }

  public CloudLoaderProxy(ClassLoader classLoader) {
    this.classLoader = classLoader == null ? Thread.currentThread().getContextClassLoader() : classLoader;
    hookClasses = new ConcurrentHashSet<>();
    parsedJars = new ConcurrentSet<>();
    this.globalConfig = CloudCodeContants.GLOBAL_CONFIG;
  }

  public Loader newProxyInstance(LoaderBase targetObject) {
    this.loader = targetObject;
    return (Loader) Proxy.newProxyInstance(targetObject.getClass().getClassLoader(), targetObject.getClass().getInterfaces(), this);
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    Object result = null;
    String methodName = method.getName();
    if ("main".equals(methodName)) {
      result = loader;
      initHandler();
    } else if ("definers".equals(methodName)) {
      result = loader.definers();
    }
    return result;
  }

  private void initHandler() throws InterruptedException {
    classesHookManager = new MLClassManagerImpl<>(null, _SYS_EntityHook.class);
    //instance entity manager with hook package path
    loadClassesManagerWithHook();
    loadClassesManagerWithoutHook();
    //走REST存储Entity Hook
    cacheHookClasses();
    //invoke user main
    loader.main(globalConfig);
  }

  private void cacheHookClasses() {
    MLQuery lasQuery;
    try {
      lasQuery = MLQuery.instance();
      FindMsg<_SYS_EntityHook> findMsg = classesHookManager.find(lasQuery);
      if (findMsg.results() != null && findMsg.results().size() > 0) {
        updateSysHookClasses(findMsg.results().get(0).objectIdString());
      } else {
        createSysHookClasses();
      }
      logger.info("cache hook classes: " + getHookClassesStr());
    } catch (Exception e) {
      e.printStackTrace();
      logger.error("cache hook classes fail: " + e.getMessage());
    }
  }

  private UpdateMsg updateSysHookClasses(String objectId) {
    final String hookClassesStr = getHookClassesStr();
    MLUpdate update = MLUpdate.getUpdate().set("values", hookClassesStr);
    return classesHookManager.update(objectId, update);
  }

  private SaveResult createSysHookClasses() {
    String hookClassesStr = getHookClassesStr();
    _SYS_EntityHook classesHook = new _SYS_EntityHook();
    classesHook.setValues(hookClassesStr);
    return classesHookManager.create(classesHook);
  }

  private String getHookClassesStr() {
    StringBuffer hookClassesBuffers = new StringBuffer();
    Iterator<Class> iterator = hookClasses.iterator();
    if (hookClasses.size() > 0) {
      hookClassesBuffers.append(iterator.next().getSimpleName());
      while (iterator.hasNext()) {
        hookClassesBuffers.append(",").append(iterator.next().getSimpleName());
      }
    }
    return hookClassesBuffers.toString();
  }

  private void loadClassesManagerWithHook() {
    String hookPackage = globalConfig.getPackageHook();
    if (isBlank(hookPackage)) return;

    String hookPackagePath = hookPackage.replace(".", "/");
    URL[] urls = ((URLClassLoader) classLoader).getURLs();
    for (URL url : urls) {
      File file = new File(url.getFile());
      parseFile(file, hookPackagePath, hookPackage);
    }
  }

  private void loadClassesManagerWithoutHook() {
    String classesPackage = globalConfig.getPackageClasses();
    if (isBlank(classesPackage)) return;
    //处理不存在hook的entity
    try {
      List<Class<?>> allClasses = getClassesForPackage(classLoader, classesPackage);
      allClasses.removeAll(hookClasses);
      //建立空的entityManager
      for (Class<?> classesClazz : allClasses) {
        MLClassManager classesManager = new MLClassManagerImpl(null, classesClazz);
        MLClassManagerFactory.putManager(classesClazz, classesManager);
        logger.info("cache classes to factory:" + classesClazz.getSimpleName());
      }
    } catch (ClassNotFoundException e) {
      logger.error(e.getMessage(), e);
    }
  }

  private void parseFile(File file, String hookPackagePath, String hookPackage) {
    if (file.isFile()) {
      int splitIndex = file.getName().lastIndexOf(".");
      if (splitIndex < 0 || !file.getName().substring(splitIndex + 1).equals("jar")) return;
      if (parsedJars.contains(file.getName())) return;
      try {
        ZipFile zip = new ZipFile(file);
        for (Enumeration<? extends ZipEntry> entries = zip.entries(); entries.hasMoreElements(); ) {
          ZipEntry entry = entries.nextElement();
          String name = entry.getName();
          if (!name.startsWith(hookPackagePath)) continue;
          name = name.substring(hookPackagePath.length() + 1);
          if (name.indexOf('/') < 0 && name.endsWith(".class")) {
            parseClass(hookPackage, name.substring(0, name.length() - 6));
          }
        }
        parsedJars.add(file.getName());
      } catch (IOException e) {
        logger.error(e.getMessage(), e);
      }
    } else if (file.isDirectory()) {
      File[] subFiles = file.listFiles();
      if (subFiles != null) {
        for (File subFile : subFiles) {
          parseFile(subFile, hookPackagePath, hookPackage);
        }
      }
    }
  }

  private void parseClass(String hookPackage, String clazzName) {
    try {
      Class hookClazz = classLoader.loadClass(hookPackage + "." + clazzName);
      ClassManager classManagerAnnotation = (ClassManager) hookClazz.getAnnotation(ClassManager.class);
      if (classManagerAnnotation == null) return;
      String managerName = classManagerAnnotation.value();
      if (managerName.equals("")) {
        String[] managerNames = managerName.split("ManagerHook");
        if (managerNames.length >= 2) {
          managerName = managerNames[0];
        } else {
          managerNames = managerName.split("Hook");
          if (managerNames.length >= 2) {
            managerName = managerNames[0];
          } else {
            throw new IllegalArgumentException(String.format("class %s should be annotate value.", hookClazz.getSimpleName()));
          }
        }
      }
      MLClassManagerHookBase hook = (MLClassManagerHookBase) hookClazz.newInstance();
      Type[] types = ((ParameterizedType) hookClazz.getGenericSuperclass()).getActualTypeArguments();
      Class classesClazz = (Class) types[0];
      MLClassManagerImpl classesManager = new MLClassManagerImpl(hook, classesClazz);
      loader.defineClassesManager(managerName, new MLClassManagerHandler(classesManager, classesClazz));
      MLClassManagerFactory.putManager(classesClazz, classesManager);
      logger.info("cache hook classes to factory:" + classesClazz.getSimpleName());
      hookClasses.add(classesClazz);
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
    }
  }

  /**
   * Attempts to list all the classes in the specified package as determined
   * by the context class loader
   *
   * @param packageName the package name to search
   * @return a list of classes that exist within that package
   * @throws ClassNotFoundException if something went wrong
   */
  private List<Class<?>> getClassesForPackage(ClassLoader cld, String packageName) throws ClassNotFoundException {
    final ArrayList<Class<?>> classes = new ArrayList<>();
    try {
      if (cld == null) throw new ClassNotFoundException("Can't get class loader.");
      final Enumeration<URL> resources = cld.getResources(packageName.replace('.', '/'));
      URLConnection connection;
      for (URL url; resources.hasMoreElements() && ((url = resources.nextElement()) != null); ) {
        try {
          connection = url.openConnection();
          if (connection instanceof JarURLConnection) {
            checkJarFile((JarURLConnection) connection, packageName, classes);
          } else if (connection != null) {
            try {
              checkDirectory(new File(URLDecoder.decode(url.getPath(), "UTF-8")), packageName, classes);
            } catch (final UnsupportedEncodingException ex) {
              throw new ClassNotFoundException(packageName + " does not appear to be a valid package (Unsupported encoding)", ex);
            }
          } else
            throw new ClassNotFoundException(packageName + " (" + url.getPath() + ") does not appear to be a valid package");
        } catch (final IOException e) {
          throw new ClassNotFoundException("IOException was thrown when trying to get all resources for " + packageName, e);
        }
      }
    } catch (final NullPointerException ex) {
      throw new ClassNotFoundException(packageName + " does not appear to be a valid package (Null pointer exception)", ex);
    } catch (final IOException ioEx) {
      throw new ClassNotFoundException("IOException was thrown when trying to get all resources for " + packageName, ioEx);
    }
    return classes;
  }


  /**
   * Private helper method
   *
   * @param directory   The directory to start with
   * @param packageName The package name to search for. Will be needed for getting the
   *                    Class object.
   * @param classes     if a file isn't loaded but still is in the directory
   * @throws ClassNotFoundException
   */
  private void checkDirectory(File directory, String packageName, ArrayList<Class<?>> classes) throws ClassNotFoundException {
    File tmpDirectory;
    if (directory.exists() && directory.isDirectory()) {
      final String[] files = directory.list();

      for (final String file : files) {
        if (file.endsWith(".class")) {
          try {
            classes.add(Class.forName(packageName + '.' + file.substring(0, file.length() - 6)));
          } catch (final NoClassDefFoundError e) {
            // do nothing. this class hasn't been found by the
            // loader, and we don't care.
          }
        } else if ((tmpDirectory = new File(directory, file)).isDirectory()) {
          checkDirectory(tmpDirectory, packageName + "." + file, classes);
        }
      }
    }
  }

  /**
   * Private helper method.
   *
   * @param connection  the connection to the jar
   * @param packageName the package name to search for
   * @param classes     the current ArrayList of all classes. This method will simply
   *                    add new classes.
   * @throws ClassNotFoundException if a file isn't loaded but still is in the jar file
   * @throws IOException            if it can't correctly read from the jar file.
   */
  private void checkJarFile(JarURLConnection connection, String packageName, ArrayList<Class<?>> classes) throws ClassNotFoundException, IOException {
    final JarFile jarFile = connection.getJarFile();
    final Enumeration<JarEntry> entries = jarFile.entries();
    String name;
    for (JarEntry jarEntry; entries.hasMoreElements() && ((jarEntry = entries.nextElement()) != null); ) {
      name = jarEntry.getName();
      if (name.contains(".class")) {
        name = name.substring(0, name.length() - 6).replace('/', '.');
        if (name.contains(packageName)) {
          classes.add(classLoader.loadClass(name));
        }
      }
    }
  }

  private boolean isBlank(String str) {
    return (str == null || str.trim().equals(""));
  }

}
