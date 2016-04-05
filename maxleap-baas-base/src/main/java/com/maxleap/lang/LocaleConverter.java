package com.maxleap.lang;

import org.apache.commons.lang.LocaleUtils;
import org.apache.commons.lang.StringUtils;

import java.util.*;

/**
 * Created by shunlv on 16-2-18.
 */
public class LocaleConverter {
  private static final Set<Locale> locales = new HashSet<>();

  static {
    locales.remove(new Locale(""));
  }

  public static Locale langToLocale(String lang) {

    ResourceBundle.Control control = ResourceBundle.Control.getControl(ResourceBundle.Control.FORMAT_DEFAULT);
    List<Locale> locs = control.getCandidateLocales("messages",
        Locale.forLanguageTag(lang));
    for (Locale loc : locs) {
      // IOS
      if (locales.contains(loc))
        return loc;
      // Android
      try {
        Locale nativeLoc = LocaleUtils.toLocale(lang);
        for (Object lookup : LocaleUtils.localeLookupList(nativeLoc)) {
          if (lookup instanceof Locale) {
            return (Locale) lookup;
          }
        }
      } catch (Exception e) {
      }
      // like zh_tw, zh_cn
      try {
        String language = StringUtils.substringBefore(lang, "_");
        String country = StringUtils.substringAfterLast(lang, "_");
        Locale nativeLoc = new Locale(language, country);
        for (Object lookup : LocaleUtils.localeLookupList(nativeLoc)) {
          if (lookup instanceof Locale) {
            return (Locale) lookup;
          }
        }
      } catch (Exception e) {
      }

    }
    return null;
  }


  public static String langToLocalString(String lang) {
    Locale locale = langToLocale(lang);
    String fmtString = locale == null ? null : locale.toLanguageTag().replaceAll("-", "_").toLowerCase();
    return fmtString;
  }
}
