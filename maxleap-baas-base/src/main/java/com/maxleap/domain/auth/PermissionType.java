package com.maxleap.domain.auth;

import com.maxleap.domain.LASUserType;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Created by shunlv on 15-9-18.
 */
public enum PermissionType {
    SYS_ADMIN(null, LASUserType.SysAdmin),
        SYS_USER(SYS_ADMIN, LASUserType.SysUser),
            ORG_ADMIN(SYS_USER, LASUserType.OrgAdmin),
                SETTINGS(ORG_ADMIN, LASUserType.OrgUser),
                BILLING(ORG_ADMIN, LASUserType.OrgUser),
                MARKETING(ORG_ADMIN, LASUserType.OrgUser),
                GAME_MASTER(ORG_ADMIN, LASUserType.OrgUser),
                DEV_CENTER(ORG_ADMIN, LASUserType.OrgUser),
                ANALYTICS(ORG_ADMIN, LASUserType.OrgUser),
                SUPPORT(ORG_ADMIN, LASUserType.OrgUser),
                ORG_PROFILE(ORG_ADMIN, LASUserType.OrgUser),
                PAY(ORG_ADMIN, LASUserType.OrgUser),
                ORG_USER(ORG_ADMIN, LASUserType.OrgUser),
                    MASTER_KEY(ORG_USER, null),
                        APP_USER(MASTER_KEY, LASUserType.AppUser),
                            API_KEY(APP_USER, null),
                            CLIENT_KEY(APP_USER, null)
    ;

    private PermissionType parent;
    private LASUserType userType;

    private PermissionType(PermissionType parent,
                           LASUserType userType) {
        this.parent = parent;
        this.userType = userType;
    }

    public PermissionType getParent() {
        return parent;
    }

    public LASUserType getUserType() {
        return userType;
    }

    @JsonCreator
    public static PermissionType fromString(String name) {
        if (name == null || name.isEmpty()) return null;

        for (PermissionType permissionType : PermissionType.values()) {
            if (permissionType.name().equals(name)) {
                return permissionType;
            }
        }

        return null;
    }

    @JsonValue
    public String toString() {
        return name();
    }
}
