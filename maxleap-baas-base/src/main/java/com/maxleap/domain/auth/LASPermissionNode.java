package com.maxleap.domain.auth;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonCreator;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * User: qinpeng
 * Date: 14-6-10
 * Time: 12:41
 */
public class LASPermissionNode implements Serializable {

    public static final String ALL = "*";
    private Map<String, Boolean> data = new LinkedHashMap();

    public LASPermissionNode(){
        data.put(ALL, true);
    }

    public static LASPermissionNode instance() {
        return new LASPermissionNode();
    }

    @JsonCreator
    public LASPermissionNode(Map<String, Boolean> map) {
        this.data = map;
    }

    @JsonAnyGetter
    public Map<String, Boolean> getMap() {
        return data;
    }

    @JsonAnySetter
    public LASPermissionNode put(String key, boolean node) {
        data.remove(ALL);
        data.put(key, node);
        return this;
    }

    public Boolean get(String key) {
        return data.get(key);
    }

}
