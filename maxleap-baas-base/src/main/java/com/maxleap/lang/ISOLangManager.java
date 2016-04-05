package com.maxleap.lang;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by shunlv on 16-2-18.
 */
public class ISOLangManager {
  private static Logger logger = LoggerFactory.getLogger(ISOLangManager.class);
  static ObjectMapper mapper = new ObjectMapper(); // can reuse, share globally
  private static URL url;
  public static Map<String, Lang> langMap = new HashMap<>();

  static {
    url = ISOLangManager.class.getClassLoader().getResource("raw.json");
    init();
  }

  public static void init() {

    try {
      Map<String, Object> langs = mapper.readValue(url, Map.class);
      Iterator<Map.Entry<String, Object>> iterator = langs.entrySet().iterator();
      while (iterator.hasNext()) {
        Map.Entry<String, Object> entry = iterator.next();
        String code = entry.getKey();
        Map content = (Map) entry.getValue();
        String name = (String) content.get("name");
        String nativeName = (String) content.get("nativeName");
        Lang lang = new Lang();
        lang.setCode(code);
        lang.setName(name);
        lang.setNativeName(nativeName);
        langMap.put(code, lang);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static Lang get(String key) {
    return langMap.get(key);
  }

  public static String getLangCode(String langCode) {

    Lang lang = getLang(langCode);

    return null == lang ? null : lang.getCode();


  }

  public static Lang getLang(String langCode) {

    if (StringUtils.isEmpty(langCode)) return null;
    try {
      String langCodeVar = LocaleConverter.langToLocalString(langCode);
      if (StringUtils.isEmpty(langCodeVar)) {
        return null;
      }
      Lang lang = get(langCodeVar);
      if (null == lang) {
        lang = get(langCodeVar.split("_")[0]);
      }
      if (null == lang) {
        return null;
      }
      return lang;
    } catch (Exception e) {
      logger.error(String.format("the langCode : cannot be  recognition", langCode), e);
    }
    return null;
  }

  public static void main(String[] args) {
    Lang lang = ISOLangManager.get("zh");
    System.out.println(lang);
  }
}
