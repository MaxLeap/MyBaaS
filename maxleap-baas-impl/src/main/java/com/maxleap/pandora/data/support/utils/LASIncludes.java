package com.maxleap.pandora.data.support.utils;

import com.maxleap.pandora.core.mongo.exception.QueryException;

import java.util.HashSet;
import java.util.Set;

/**
 * @author sneaky
 * @since 2.0.0
 */
public class LASIncludes {
  public static final int MAX_INCLUDES = 10;
  public static final int MAX_CASCADE = 5;
  public static final String SEPARATOR = ".";

  private Set<Include> includes = new HashSet<>();

  public LASIncludes(String includes) {
    String[] includeArray = StringUtils.delimitedListToStringArray(includes, ",");
    if (includeArray.length > MAX_INCLUDES) {
      throw new QueryException(" includes must be less than " + MAX_INCLUDES);
    }

    for (String include : includeArray) {
      String[] split = StringUtils.delimitedListToStringArray(include.trim(), SEPARATOR);
      if (split.length > MAX_CASCADE) {
        throw new QueryException(" Object to access the depth of not more than " + MAX_CASCADE);
      }

      Include child = null;
      for (int i = split.length - 1; i >= 0; i--) {
        if (child == null) {
          Include includeObj = new Include(split[i].trim(), i, null);
          child = includeObj;
        } else {
          Include includeObj = new Include(split[i].trim(), i, child);
          child = includeObj;
        }
      }

      this.includes.add(child);
    }
  }

  public Set<Include> includes() {
    return includes;
  }

  public static void main(String[] args) {
    String includes = "a, a.b, a.b, a.b.c.d.e, b.c.d";
    LASIncludes includeBuilder = new LASIncludes(includes);
    System.out.println(includeBuilder.includes());

    String[] x = StringUtils.delimitedListToStringArray(null, null);
    System.out.println(x);
    String[] zhaojings = StringUtils.delimitedListToStringArray("zhaojing", null);
    System.out.println(zhaojings);
    String[] zhaojings1 = StringUtils.delimitedListToStringArray("zhaojing", ".");
    System.out.println(zhaojings1);
    String[] x1 = StringUtils.delimitedListToStringArray("zhaojing.jing.jing", ".");
    System.out.println(x1);
  }

  public static class Include {
    public String node;
    public int depths;
    public Include child;

    public Include(String node, int depths, Include child) {
      this.node = node;
      this.child = child;
      this.depths = depths;
    }

    @Override
    public boolean equals(Object o) {
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      Include obj = (Include) o;

      if (obj.node.equals(this.node) && obj.depths == this.depths) {
        if (obj.child != null) {
          return obj.child.equals(this.child);
        } else if (this.child != null) {
          return false;
        }
        return true;
      }

      return false;
    }

    @Override
    public int hashCode() {
      int result = 0;

      result = 17 * result + depths;
      result = 17 * result + node.hashCode();
      if (child != null) {
        result = 17 * result + child.hashCode();
      }

      return result;
    }

    @Override
    public String toString() {
      return "Include{" +
          "node='" + node + '\'' +
          ", depths=" + depths +
          ", child=" + child +
          '}';
    }
  }

  @Override
  public String toString() {
    return "IncludeBuilder{" +
        "includes=" + includes +
        '}';
  }
}
