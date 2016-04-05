package com.maxleap.las.bass.apiserver.web;

import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Model of a single resource component.
 * <p>
 * Resource component model represents a collection of {@link ResourceMethod methods}
 * grouped under the same parent request path template.
 * @author sneaky
 * @since 1.0.0
 */
public class Resource {
  private Data data;
  private Class clazz;

  private List<ResourceMethod> methods = new ArrayList<>();

  private Resource(Data data, Class clazz) {
    this.data = data;
    this.clazz = clazz;
  }

  private static class Data {
    private final String path;
    // Consuming & Producing
    private final Set<String> consumedTypes;
    private final Set<String> producedTypes;

    public Data(String path, Set<String> consumedTypes, Set<String> producedTypes) {
      this.path = path;
      this.consumedTypes = consumedTypes;
      this.producedTypes = producedTypes;
    }

    @Override
    public String toString() {
      return "{" +
          "path:'" + path + '\'' +
          ", consumedTypes:" + consumedTypes +
          ", producedTypes:" + producedTypes +
          '}';
    }
  }

  /**
   * Resource builder
   */
  public static final class Builder {
    private String path;
    // Consuming & Producing
    private Set<String> consumedTypes;
    private Set<String> producedTypes;
    private Class  clazz;

    public Builder(Class<?> clazz) {
      this.clazz = clazz;
    }

    public Builder path(String path) {
      this.path = path;
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

    private void processMethodBuilders(Resource resource, Class<?> clazz) {
      ResourceIntrospection.getAllDeclaredMethodWithAnnotation(clazz).forEach(m -> {
        ResourceMethod resourceMethod = ResourceMethod.builder(m, resource).build();
        resource.getMethods().add(resourceMethod);
      });
    }

    public Resource build() {
      Resource resource = new Resource(new Data(path, consumedTypes, producedTypes), clazz);
      processMethodBuilders(resource, clazz);
      return resource;
    }

  }

  public static Builder builder(Class<?> clazz) {
    Builder builder = new Builder(clazz);
    builder.path(clazz.getAnnotation(Path.class).value())
        .consumedTypes(ResourceIntrospection.extractMediaTypes(clazz.getAnnotation(Consumes.class)))
        .producedTypes(ResourceIntrospection.extractMediaTypes(clazz.getAnnotation(Produces.class)));
    return builder;
  }

  public String path() {
    return data.path;
  }

  public List<ResourceMethod> getMethods() {
    return methods;
  }

  public Class getResourceClazz() {
    return clazz;
  }

  public Set<String> consumedTypes() {
    return data.consumedTypes;
  }

  public Set<String> producedTypes() {
    return data.producedTypes;
  }

}
