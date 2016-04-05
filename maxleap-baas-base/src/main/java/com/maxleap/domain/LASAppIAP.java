package com.maxleap.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by shunlv on 15-3-6.
 */
public class LASAppIAP {
  private IOSIAP iOS;
  @JsonProperty("GooglePlay")
  private GooglePlayIAP googlePlayIAP;
  @JsonProperty("Amazon")
  private AmazonIAP amazonIAP;

  public IOSIAP getIOS() {
    return iOS;
  }

  public void setIOS(IOSIAP iOS) {
    this.iOS = iOS;
  }

  public GooglePlayIAP getGooglePlayIAP() {
    return googlePlayIAP;
  }

  public void setGooglePlayIAP(GooglePlayIAP googlePlayIAP) {
    this.googlePlayIAP = googlePlayIAP;
  }

  public AmazonIAP getAmazonIAP() {
    return amazonIAP;
  }

  public void setAmazonIAP(AmazonIAP amazonIAP) {
    this.amazonIAP = amazonIAP;
  }

  public class IOSIAP {
  }

  public class GooglePlayIAP {
    private String clientId;
    private String refreshToken;
    private String clientSecret;
    private String defaultAccessToken;

    public String getClientId() {
      return clientId;
    }

    public void setClientId(String clientId) {
      this.clientId = clientId;
    }

    public String getRefreshToken() {
      return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
      this.refreshToken = refreshToken;
    }

    public String getClientSecret() {
      return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
      this.clientSecret = clientSecret;
    }

    public String getDefaultAccessToken() {
      return defaultAccessToken;
    }

    public void setDefaultAccessToken(String defaultAccessToken) {
      this.defaultAccessToken = defaultAccessToken;
    }

    @Override
    public String toString() {
      return "GooglePlayIAP{" +
          "clientId='" + clientId + '\'' +
          ", refreshToken='" + refreshToken + '\'' +
          ", clientSecret='" + clientSecret + '\'' +
          ", defaultAccessToken='" + defaultAccessToken + '\'' +
          '}';
    }
  }

  public class AmazonIAP {
    private String developerSecret;

    public String getDeveloperSecret() {
      return developerSecret;
    }

    public void setDeveloperSecret(String developerSecret) {
      this.developerSecret = developerSecret;
    }

    @Override
    public String toString() {
      return "AmazonIAP{" +
          "developerSecret='" + developerSecret + '\'' +
          '}';
    }
  }
}
