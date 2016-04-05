package com.maxleap.code.impl;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

abstract class WebUtils {

  public static final String DEFAULT_CHARSET = "UTF-8";
  private static final String METHOD_POST = "POST";
  private static final String METHOD_GET = "GET";
  private static final String METHOD_PUT = "PUT";
  private static final String METHOD_DELETE = "DELETE";
  private static final String CONTENT_TYPE_FORM = "application/x-www-form-urlencoded";
  private static final String CONTENT_TYPE_JSON = "application/json";

  private WebUtils() {
  }

  /**
   * 执行HTTP POST请求。
   *
   * @param url    请求地址
   * @param params 请求参数
   * @return 响应字符串
   * @throws IOException
   */
  public static String doPost(String url, Map<String, String> params, int connectTimeout, int readTimeout) throws IOException {
    return doPost(url, params, DEFAULT_CHARSET, connectTimeout, readTimeout);
  }

  public static String doPut(String url, Map<String, String> header, Map<String, String> params, int connectTimeout, int readTimeout) throws IOException {
    return doPut(url, header, params, DEFAULT_CHARSET, connectTimeout, readTimeout);
  }

  public static String doPut(String url, Map<String, String> header, String queryParams, int connectTimeout, int readTimeout) throws IOException {
    String ctype = CONTENT_TYPE_JSON + ";charset=" + DEFAULT_CHARSET;
    byte[] content = {};
    if (queryParams != null) {
      content = queryParams.getBytes(DEFAULT_CHARSET);
    }
    return doRequestWithBody(url, METHOD_PUT, header, ctype, content, connectTimeout, readTimeout);
  }

  public static String doPut(String url, Map<String, String> header, Map<String, String> params, String charset, int connectTimeout, int readTimeout) throws IOException {
    String ctype = CONTENT_TYPE_JSON + ";charset=" + charset;
    String query = buildQuery(params, charset);
    byte[] content = {};
    if (query != null) {
      content = query.getBytes(charset);
    }
    return doRequestWithBody(url, METHOD_PUT, header, ctype, content, connectTimeout, readTimeout);
  }

  /**
   * 执行HTTP POST请求。
   *
   * @param url     请求地址
   * @param params  请求参数
   * @param charset 字符集，如UTF-8, GBK, GB2312
   * @return 响应字符串
   * @throws IOException
   */
  public static String doPost(String url, Map<String, String> header, Map<String, String> params, String charset, int connectTimeout, int readTimeout)
      throws IOException {
    String ctype = CONTENT_TYPE_JSON + ";charset=" + charset;
    String query = buildQuery(params, charset);
    byte[] content = {};
    if (query != null) {
      content = query.getBytes(charset);
    }
    return doRequestWithBody(url, METHOD_POST, header, ctype, content, connectTimeout, readTimeout);
  }

  public static String doPost(String url, Map<String, String> header, String queryParams, int connectTimeout, int readTimeout) throws IOException {
    String ctype = CONTENT_TYPE_JSON + ";charset=" + DEFAULT_CHARSET;
    byte[] content = {};
    if (queryParams != null) {
      content = queryParams.getBytes(DEFAULT_CHARSET);
    }
    return doRequestWithBody(url, METHOD_POST, header, ctype, content, connectTimeout, readTimeout);
  }

  /**
   * 执行HTTP POST请求。
   *
   * @param url     请求地址
   * @param ctype   请求类型
   * @param content 请求字节数组
   * @return 响应字符串
   * @throws IOException
   */
  private static String doRequestWithBody(String url, String method, Map<String, String> header, String ctype, byte[] content, int connectTimeout, int readTimeout) throws IOException {
    HttpURLConnection conn = null;
    OutputStream out = null;
    String rsp = null;
    try {
      try {
        conn = getConnection(new URL(url), method, header, ctype);
        conn.setConnectTimeout(connectTimeout);
        conn.setReadTimeout(readTimeout);
      } catch (IOException e) {
        throw e;
      }
      try {
        out = conn.getOutputStream();
        out.write(content);
        rsp = getResponseAsString(conn);
      } catch (IOException e) {
        throw e;
      }

    } finally {
      if (out != null) {
        out.close();
      }
      if (conn != null) {
        conn.disconnect();
      }
    }
    return rsp;
  }


  /**
   * 执行HTTP GET请求。
   *
   * @param url    请求地址
   * @param params 请求参数
   * @return 响应字符串
   * @throws IOException
   */
  public static String doGet(String url, Map<String, String> header, Map<String, String> params) throws IOException {
    return doRequestWithUrl(url, METHOD_GET, header, params, DEFAULT_CHARSET);
  }

  /**
   * 执行HTTP DELETE请求。
   *
   * @param url    请求地址
   * @param params 请求参数
   * @return 响应字符串
   * @throws IOException
   */
  public static String doDelete(String url, Map<String, String> header, Map<String, String> params) throws IOException {
    return doRequestWithUrl(url, METHOD_DELETE, header, params, DEFAULT_CHARSET);
  }

  /**
   * 执行HTTP GET/DELETE请求。
   *
   * @param url     请求地址
   * @param params  请求参数
   * @param charset 字符集，如UTF-8, GBK, GB2312
   * @return 响应字符串
   * @throws IOException
   */
  public static String doRequestWithUrl(String url, String method, Map<String, String> header, Map<String, String> params, String charset) throws IOException {
    HttpURLConnection conn = null;
    String rsp = null;

    try {
      String ctype = "application/json";
      String query = buildQuery(params, charset);
      try {
        conn = getConnection(buildGetUrl(url, query), method, header, ctype);
      } catch (IOException e) {
        throw e;
      }

      try {
        rsp = getResponseAsString(conn);
      } catch (IOException e) {
        throw e;
      }

    } finally {
      if (conn != null) {
        conn.disconnect();
      }
    }

    return rsp;
  }

  private static HttpURLConnection getConnection(URL url, String method, Map<String, String> header, String ctype) throws IOException {
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setUseCaches(false);
    conn.setDoInput(true);
    conn.setDoOutput(true);
    conn.setRequestMethod(method);
//    conn.setRequestProperty("Accept", "text/xml,text/javascript");
//    conn.setRequestProperty("User-Agent", "iLegendSoft-Test-Client");
    conn.setRequestProperty("Content-Type", "application/json");
    if (header != null && header.size() > 0)
      for (Entry<String, String> entry : header.entrySet()) {
        conn.setRequestProperty(entry.getKey(), entry.getValue());
      }
    return conn;
  }

  private static URL buildGetUrl(String strUrl, String query) throws IOException {
    URL url = new URL(strUrl);
    if (isEmpty(query)) {
      return url;
    }

    if (isEmpty(url.getQuery())) {
      if (strUrl.endsWith("?")) {
        strUrl = strUrl + query;
      } else {
        strUrl = strUrl + "?" + query;
      }
    } else {
      if (strUrl.endsWith("&")) {
        strUrl = strUrl + query;
      } else {
        strUrl = strUrl + "&" + query;
      }
    }

    return new URL(strUrl);
  }

  public static String buildQuery(Map<String, String> params, String charset) throws IOException {
    if (params == null || params.isEmpty()) {
      return null;
    }

    StringBuilder query = new StringBuilder();
    Set<Entry<String, String>> entries = params.entrySet();
    boolean hasParam = false;
    // 忽略参数名或参数值为空的参数
    for (Entry<String, String> entry : entries) {
      String name = entry.getKey();
      String value = entry.getValue();
      if (!isBlank(name)) {
        if (hasParam) {
          query.append("&");
        } else {
          hasParam = true;
        }
        if (value == null) {
          value = "";
        }
        query.append(name).append("=").append(URLEncoder.encode(value, charset));
      }
    }

    return query.toString();
  }

  protected static String getResponseAsString(HttpURLConnection conn) throws IOException {
    String charset = getResponseCharset(conn.getContentType());
    InputStream es = conn.getErrorStream();
    if (es == null) {
      return getStreamAsString(conn.getInputStream(), charset);
    } else {
      String msg = getStreamAsString(es, charset);
      if (isEmpty(msg)) {
        throw new IOException(conn.getResponseCode() + ":" + conn.getResponseMessage());
      } else {
        throw new IOException(msg);
      }
    }
  }

  private static String getStreamAsString(InputStream stream, String charset) throws IOException {
    try {
      BufferedReader reader = new BufferedReader(new InputStreamReader(stream, charset));
      StringWriter writer = new StringWriter();

      char[] chars = new char[256];
      int count = 0;
      while ((count = reader.read(chars)) > 0) {
        writer.write(chars, 0, count);
      }

      return writer.toString();
    } finally {
      if (stream != null) {
        stream.close();
      }
    }
  }

  private static String getResponseCharset(String ctype) {
    String charset = DEFAULT_CHARSET;

    if (!isEmpty(ctype)) {
      String[] params = ctype.split(";");
      for (String param : params) {
        param = param.trim();
        if (param.startsWith("charset")) {
          String[] pair = param.split("=", 2);
          if (pair.length == 2) {
            if (!isEmpty(pair[1])) {
              charset = pair[1].trim();
            }
          }
          break;
        }
      }
    }

    return charset;
  }

  /**
   * 使用默认的UTF-8字符集反编码请求参数值。
   *
   * @param value 参数值
   * @return 反编码后的参数值
   */
  public static String decode(String value) {
    return decode(value, DEFAULT_CHARSET);
  }

  /**
   * 使用默认的UTF-8字符集编码请求参数值。
   *
   * @param value 参数值
   * @return 编码后的参数值
   */
  public static String encode(String value) {
    return encode(value, DEFAULT_CHARSET);
  }

  /**
   * 使用指定的字符集反编码请求参数值。
   *
   * @param value   参数值
   * @param charset 字符集
   * @return 反编码后的参数值
   */
  public static String decode(String value, String charset) {
    String result = null;
    if (!isEmpty(value)) {
      try {
        result = URLDecoder.decode(value, charset);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    return result;
  }

  /**
   * 使用指定的字符集编码请求参数值。
   *
   * @param value   参数值
   * @param charset 字符集
   * @return 编码后的参数值
   */
  public static String encode(String value, String charset) {
    String result = null;
    if (!isEmpty(value)) {
      try {
        result = URLEncoder.encode(value, charset);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    return result;
  }

  // private static Map<String, String> getParamsFromUrl(String url) {
  // Map<String, String> map = null;
  // if (url != null && url.indexOf('?') != -1) {
  // map = splitUrlQuery(url.substring(url.indexOf('?') + 1));
  // }
  // if (map == null) {
  // map = new HashMap<String, String>();
  // }
  // return map;
  // }

  /**
   * 从URL中提取所有的参数。
   *
   * @param query URL地址
   * @return 参数映射
   */
  public static Map<String, String> splitUrlQuery(String query) {
    Map<String, String> result = new HashMap<String, String>();

    String[] pairs = query.split("&");
    if (pairs != null && pairs.length > 0) {
      for (String pair : pairs) {
        String[] param = pair.split("=", 2);
        if (param != null && param.length == 2) {
          result.put(param[0], param[1]);
        }
      }
    }

    return result;
  }

  private static boolean isEmpty(final String str) {
    return str == null || str.length() == 0;
  }

  private static boolean isBlank(final String str) {
    int length;

    if (str == null || (length = str.length()) == 0) {
      return true;
    }

    for (int i = 0; i < length; i++) {
      if (!Character.isWhitespace(str.charAt(i))) {
        return false;
      }
    }

    return true;
  }
}
