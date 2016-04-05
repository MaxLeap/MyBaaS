package com.maxleap.cloudcode;

/**
 * Created by stream.
 */
public class LockEntity {

  private String appId;
  private String version;
  private String name;

  public LockEntity() {
  }

  public LockEntity(String appId, String version, String name) {
    this.appId = appId;
    this.version = version;
    this.name = name;
  }

  public String getAppId() {
    return appId;
  }

  public void setAppId(String appId) {
    this.appId = appId;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof LockEntity)) return false;

    LockEntity that = (LockEntity) o;

    if (appId != null ? !appId.equals(that.appId) : that.appId != null) return false;
    if (version != null ? !version.equals(that.version) : that.version != null) return false;
    return !(name != null ? !name.equals(that.name) : that.name != null);

  }

  @Override
  public int hashCode() {
    int result = appId != null ? appId.hashCode() : 0;
    result = 31 * result + (version != null ? version.hashCode() : 0);
    result = 31 * result + (name != null ? name.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "LockEntity{" +
        "appId='" + appId + '\'' +
        ", version='" + version + '\'' +
        ", name='" + name + '\'' +
        '}';
  }
}
