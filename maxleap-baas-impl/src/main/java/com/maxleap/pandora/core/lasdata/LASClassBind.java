package com.maxleap.pandora.core.lasdata;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 *
 * @author sneaky
 * @since 1.0
 */
public class LASClassBind implements Serializable {
    @JsonProperty("app_id")
    private String bindApp;
    @JsonProperty("class")
    private String className;
    private boolean write;
    private boolean read;
    private boolean delete;

    public String getBindApp() {
        return bindApp;
    }

    public void setBindApp(String bindApp) {
        this.bindApp = bindApp;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public boolean isWrite() {
        return write;
    }

    public void setWrite(boolean write) {
        this.write = write;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public boolean isDelete() {
        return delete;
    }

    public void setDelete(boolean delete) {
        this.delete = delete;
    }

    @Override
    public String toString() {
        return "LASClassBind{" +
            "bindApp='" + bindApp + '\'' +
            ", className='" + className + '\'' +
            ", write=" + write +
            ", read=" + read +
            ", delete=" + delete +
            '}';
    }
}
