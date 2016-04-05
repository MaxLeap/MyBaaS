package com.maxleap.pandora.config;

import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author sneaky
 * @since 1.0.0
 */
public class Funcs {
  public static long timeMillis(long times, Supplier<Object> func) {
    long start = System.currentTimeMillis();
    for (int i = 0; i < times; i++) {
      func.get();
    }
    long l = System.currentTimeMillis() - start;
    System.out.println("total time: " + l + " mills");
    return l;
  }

  public static long timeMillis(long times,Consumer<Object> func) {
    long start = System.currentTimeMillis();
    for (int i = 0; i < times; i++) {
      func.accept(null);
    }
    long l = System.currentTimeMillis() - start;
    System.out.println("total time: " + l + " mills");
    return l;
  }

  public static <T> T get(String key, Map<String, T> map, Supplier<T> func) {
    T t = map.get(key);
    if (t != null) {
      return t;
    }

    T o = func.get();
    if (o != null) {
      map.putIfAbsent(key, o);
    }
    return map.get(key);
  }

  public static <T> T get(String key, Lock lock, Map<String, T> map, Supplier<T> func) {
    T t = map.get(key);
    if (t != null) {
      return t;
    }

    lock.lock();
    try {
      T o = func.get();
      if (o != null) {
        map.putIfAbsent(key, o);
      }
      return map.get(key);
    } finally {
      lock.unlock();
    }
  }
}
