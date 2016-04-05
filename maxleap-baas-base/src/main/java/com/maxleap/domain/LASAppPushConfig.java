package com.maxleap.domain;


/**
 * User: yuyangning
 * Date: 6/13/14
 * Time: 5:55 PM
 */
public class LASAppPushConfig {
  public Boolean enable;
  public String message;

  public AppleCertificateConfig appleConfig = new AppleCertificateConfig();

  public WindowsCertificateConfig windowsConfig = new WindowsCertificateConfig();

  public GcmConfig gcmConfig = new GcmConfig();

  public Boolean getEnable() {
    return enable;
  }

  public void setEnable(Boolean enable) {
    this.enable = enable;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public AppleCertificateConfig getAppleConfig() {
    return appleConfig;
  }

  public void setAppleConfig(AppleCertificateConfig appleConfig) {
    this.appleConfig = appleConfig;
  }

  public WindowsCertificateConfig getWindowsConfig() {
    return windowsConfig;
  }

  public void setWindowsConfig(WindowsCertificateConfig windowsConfig) {
    this.windowsConfig = windowsConfig;
  }

  public GcmConfig getGcmConfig() {
    return gcmConfig;
  }

  public void setGcmConfig(GcmConfig gcmConfig) {
    this.gcmConfig = gcmConfig;
  }

  public class AppleCertificateConfig {
    public String key;
    private String password;
    private Boolean verify;

    public String getKey() {
      return key;
    }

    public void setKey(String key) {
      this.key = key;
    }

    public String getPassword() {
      return password;
    }

    public void setPassword(String password) {
          this.password = password;
    }
    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      AppleCertificateConfig that = (AppleCertificateConfig) o;

      if (key != null ? !key.equals(that.key) : that.key != null) return false;
      if (password != null ? !password.equals(that.password) : that.password != null) return false;

      return true;
    }

   @Override
   public int hashCode() {
      int result = key != null ? key.hashCode() : 0;
      result = 31 * result + (password != null ? password.hashCode() : 0);
      return result;
   }

    public Boolean getVerify() {
      return verify;
    }

    public void setVerify(Boolean verify) {
      this.verify = verify;
    }
  }

  public class GcmConfig {
    public String apiKey;

    public String getApiKey() {
      return apiKey;
    }

    public void setApiKey(String apiKey) {
      this.apiKey = apiKey;
    }
      @Override
      public boolean equals(Object o) {
          if (this == o) return true;
          if (o == null || getClass() != o.getClass()) return false;

          GcmConfig gcmConfig = (GcmConfig) o;

          if (apiKey != null ? !apiKey.equals(gcmConfig.apiKey) : gcmConfig.apiKey != null) return false;

          return true;
      }

      @Override
      public int hashCode() {
          return apiKey != null ? apiKey.hashCode() : 0;
      }
  }

  public class WindowsCertificateConfig {
    public String packageSid;
    public String clientSecret;

    public String getClientSecret() {
      return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
      this.clientSecret = clientSecret;
    }

    public String getPackageSid() {
      return packageSid;
    }

    public void setPackageSid(String packageSid) {
      this.packageSid = packageSid;
    }

  }


}
