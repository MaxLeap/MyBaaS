package com.maxleap.domain;



/**
 * User: yuyangning
 * Date: 6/10/14
 * Time: 2:40 PM
 */
public class LASAppMetadata {

    private String name;
    private String url;
    private String desc;
    private String icon;
    private boolean production = false;

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isProduction() {
        return production;
    }

    public void setProduction(boolean production) {
        this.production = production;
    }

}
