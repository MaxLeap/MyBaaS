package com.maxleap.las.bass.apiserver.web;

import javax.ws.rs.Consumes;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author sneaky
 * @since 1.0.0
 */
public class ResourceIntrospection {
  public static List<Method> getAllDeclaredMethodWithAnnotation(Class c) {
    return getAllDeclaredMethods(c).stream().filter(m -> m.getAnnotation(Path.class) != null
        || m.getAnnotation(Consumes.class) != null
        || m.getAnnotation(Produces.class) != null
        || resolveHttpMethodFromMethod(m).isPresent()).collect(Collectors.toList());
  }

  public static List<Method> getAllDeclaredMethods(Class c) {
    return Arrays.asList(AccessController.doPrivileged(getDeclaredMethodsPA(c)));
  }

  public static PrivilegedAction<Method[]> getDeclaredMethodsPA(final Class<?> clazz) {
    return () -> clazz.getMethods();
  }

  public static Optional<Path> resolvePathFromMethod(final Method method) {
    return Optional.ofNullable(method.getAnnotation(Path.class));
  }

  public static Optional<HttpMethod> resolveHttpMethodFromMethod(final Method method) {
    Annotation[] annotations = method.getAnnotations();
    for (Annotation annotation : annotations) {
      HttpMethod httpMethod = annotation.annotationType().getAnnotation(HttpMethod.class);
      if (httpMethod != null) {
        return Optional.of(httpMethod);
      }
    }
    return Optional.empty();
  }

  public static Set<String> extractMediaTypes(final Consumes annotation) {
    return (annotation != null) ? extractMediaTypes(annotation.value()) : new HashSet<>();
  }

  public static Set<String> extractMediaTypes(final Produces annotation) {
    return (annotation != null) ? extractMediaTypes(annotation.value()) : new HashSet<>();
  }

  private static Set<String> extractMediaTypes(final String[] values) {
    if (values.length == 0) {
      return new HashSet<>();
    }
    final Set<String> types = new HashSet<>(values.length);
    for (final String mtEntry : values) {
      for (final String mt : mtEntry.split(",")) {
        types.add(mt.trim());
      }
    }

    return types;
  }
}
