package com.maxleap.domain;


import com.maxleap.domain.base.LASObject;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;
import java.util.Map;

/**
 * User: qinpeng
 * Date: 14-5-6
 * Time: 15:46
 */
public class LASRole extends LASObject {

    @JsonIgnore
    public static final String FIELD_NAME = "name";
    @JsonIgnore
    public static final String FIELD_USERS = "users";
    @JsonIgnore
    public static final String FIELD_ROLES = "roles";

    public LASRole() {
        super();
    }

    public LASRole(Map<String, Object> map) {
        super(map);
    }

    @JsonIgnore
    public String getName() {
        return this.get(FIELD_NAME) == null ? null : (String) this.get(FIELD_NAME);
    }

    public void setName(String name) {
        this.put(FIELD_NAME, name);
    }

    @JsonIgnore
    public List<LASRole> getRoles() {
        return this.get(FIELD_ROLES) == null ? null : (List<LASRole>) this.get(FIELD_ROLES);
    }

    public void setRoles(List<LASRole> roles) {
        this.put(FIELD_ROLES, roles);
    }

    @JsonIgnore
    public List<LASUser> getUsers() {
        return this.get(FIELD_USERS) == null ? null : (List<LASUser>) this.get(FIELD_USERS);
    }

    public void setUsers(List<LASUser> users) {
        this.put(FIELD_USERS, users);
    }
}
