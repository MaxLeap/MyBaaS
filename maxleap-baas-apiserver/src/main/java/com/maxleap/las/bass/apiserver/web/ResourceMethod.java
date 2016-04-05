package com.maxleap.las.bass.apiserver.web;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import java.lang.reflect.Method;
import java.util.Set;

/**
 * Model of a method available on a resource. Covers resource method, sub-resource
 * method and sub-resource locator.
 *
 * @author sneaky
 * @since 1.0.0
 */
public class ResourceMethod {
  private Method method;
  private Resource parent;
  private Data data;

  private ResourceMethod(Resource parent, Method method, Data data) {
    this.parent = parent;
    this.data = data;
    this.method = method;
  }

  private static class Data {
    private final String path;
    // HttpMethod
    private final String httpMethod;
    // Consuming & Producing
    private final Set<String> consumedTypes;
    private final Set<String> producedTypes;

    public Data(String path, String httpMethod, Set<String> consumedTypes, Set<String> producedTypes) {
      this.path = path;
      this.httpMethod = httpMethod;
      this.consumedTypes = consumedTypes;
      this.producedTypes = producedTypes;
    }

    @Override
    public String toString() {
      return "{" +
          "path:'" + path + '\'' +
          ", httpMethod:'" + httpMethod + '\'' +
          ", consumedTypes:" + consumedTypes +
          ", producedTypes:" + producedTypes +
          '}';
    }
  }

  public static class Builder {
    private Resource parent;
    private Method method;
    private String path;
    // HttpMethod
    private String httpMethod;
    // Consuming & Producing
    private Set<String> consumedTypes;
    private Set<String> producedTypes;

    private Builder(Resource parent, Method method) {
      this.parent = parent;
      this.method = method;
    }

    public Builder parent(Resource parent) {
      this.parent = parent;
      return this;
    }

    public Builder path(String path) {
      this.path = path;
      return this;
    }

    public Builder httpMethod(String httpMethod) {
      this.httpMethod = httpMethod;
      return this;
    }

    public Builder consumedTypes(Set<String> consumedTypes) {
      this.consumedTypes = consumedTypes;
      return this;
    }

    public Builder producedTypes(Set<String> producedTypes) {
      this.producedTypes = producedTypes;
      return this;
    }

    public ResourceMethod build() {
      ResourceMethod resourceMethod = new ResourceMethod(parent, method, new Data(path, httpMethod, consumedTypes, producedTypes));
      return resourceMethod;
    }
  }

  public static Builder builder(Method method, Resource parent) {
    Builder builder = new Builder(parent, method);
    ResourceIntrospection.resolveHttpMethodFromMethod(method).ifPresent(m -> builder.httpMethod(m.value()));
    ResourceIntrospection.resolvePathFromMethod(method).ifPresent(p -> builder.path(p.value()));
    builder.consumedTypes(ResourceIntrospection.extractMediaTypes(method.getAnnotation(Consumes.class)))
        .producedTypes(ResourceIntrospection.extractMediaTypes(method.getAnnotation(Produces.class)))
        .parent(parent);

    return builder;
  }

  public String path() {
    return data.path;
  }

  public Method getMethod() {
    return method;
  }

  public Resource getParent() {
    return getParent();
  }

  public String httpMethod() {
    return data.httpMethod;
  }

  public Set<String> consumedTypes() {
    return data.consumedTypes;
  }

  public Set<String> producedTypes() {
    return data.producedTypes;
  }
}
