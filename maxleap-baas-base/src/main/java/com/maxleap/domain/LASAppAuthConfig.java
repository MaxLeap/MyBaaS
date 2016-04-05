package com.maxleap.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * User: yuyangning
 * Date: 6/13/14
 * Time: 5:57 PM
 */
public class LASAppAuthConfig {
  public boolean defaultAuth;
  public boolean anonymousUser;
  public boolean baseUserName;

  public FaceBookAuth faceBookAuth;
  public TwitterAuth twitterAuth;
  public WeiboAuth weiboAuth;
  public WeixinAuth weixinAuth;
  public TecentqqAuth qqAuth;

  public FaceBookAuth getFaceBookAuth() {
    return faceBookAuth;
  }

  public void setFaceBookAuth(FaceBookAuth faceBookAuth) {
    this.faceBookAuth = faceBookAuth;
  }

  public TwitterAuth getTwitterAuth() {
    return twitterAuth;
  }

  public void setTwitterAuth(TwitterAuth twitterAuth) {
    this.twitterAuth = twitterAuth;
  }

  public boolean isAnonymousUser() {
    return anonymousUser;
  }

  public void setAnonymousUser(boolean anonymousUser) {
    this.anonymousUser = anonymousUser;
  }

  public boolean isBaseUserName() {
    return baseUserName;
  }

  public void setBaseUserName(boolean baseUserName) {
    this.baseUserName = baseUserName;
  }

  public boolean isDefaultAuth() {
    return defaultAuth;
  }

  public void setDefaultAuth(boolean defaultAuth) {
    this.defaultAuth = defaultAuth;
  }

  public WeiboAuth getWeiboAuth() {
    return weiboAuth;
  }

  public void setWeiboAuth(WeiboAuth weiboAuth) {
    this.weiboAuth = weiboAuth;
  }

  public WeixinAuth getWeixinAuth() {
    return weixinAuth;
  }

  public void setWeixinAuth(WeixinAuth weixinAuth) {
    this.weixinAuth = weixinAuth;
  }

  public TecentqqAuth getQqAuth() {
    return qqAuth;
  }

  public void setQqAuth(TecentqqAuth qqAuth) {
    this.qqAuth = qqAuth;
  }

  public class TwitterAuth {
    private boolean authEnable;
    private List<String> consumerKeys =new ArrayList<>();

    public boolean isAuthEnable() {
      return authEnable;
    }

    public void setAuthEnable(boolean authEnable) {
      this.authEnable = authEnable;
    }

    public List<String> getConsumerKeys() {
      return consumerKeys;
    }

    public void setConsumerKeys(List<String> consumerKeys) {
      this.consumerKeys = consumerKeys;
    }
  }

  public class FaceBookAuth {
    private boolean authEnable;
    private List<OauthAppInfo> appInfos =new ArrayList<>();

    public boolean isAuthEnable() {
      return authEnable;
    }

    public void setAuthEnable(boolean authEnable) {
      this.authEnable = authEnable;
    }

    public List<OauthAppInfo> getAppInfos() {
      return appInfos;
    }

    public void setAppInfos(List<OauthAppInfo> appInfos) {
      this.appInfos = appInfos;
    }
  }

  public class WeiboAuth {
    private boolean authEnable = true;
    private List<OauthAppInfo> appInfos =new ArrayList<>();

    public boolean isAuthEnable() {
      return authEnable;
    }

    public void setAuthEnable(boolean authEnable) {
      this.authEnable = authEnable;
    }

    public List<OauthAppInfo> getAppInfos() {
      return appInfos;
    }

    public void setAppInfos(List<OauthAppInfo> appInfos) {
      this.appInfos = appInfos;
    }

  }

  public class WeixinAuth {
    private boolean authEnable = true;
    private List<OauthAppInfo> appInfos =new ArrayList<>();

    public boolean isAuthEnable() {
      return authEnable;
    }

    public void setAuthEnable(boolean authEnable) {
      this.authEnable = authEnable;
    }

    public List<OauthAppInfo> getAppInfos() {
      return appInfos;
    }

    public void setAppInfos(List<OauthAppInfo> appInfos) {
      this.appInfos = appInfos;
    }
  }

  public class TecentqqAuth {
    private boolean authEnable = true;
    private List<OauthAppInfo> appInfos =new ArrayList<>();

    public boolean isAuthEnable() {
      return authEnable;
    }

    public void setAuthEnable(boolean authEnable) {
      this.authEnable = authEnable;
    }

    public List<OauthAppInfo> getAppInfos() {
      return appInfos;
    }

    public void setAppInfos(List<OauthAppInfo> appInfos) {
      this.appInfos = appInfos;
    }

  }

  public static class OauthAppInfo {
    private String appId;
    private String secret;

    public String getAppId() {
      return appId;
    }

    public void setAppId(String appId) {
      this.appId = appId;
    }

    public String getSecret() {
      return secret;
    }

    public void setSecret(String secret) {
      this.secret = secret;
    }
  }
}
