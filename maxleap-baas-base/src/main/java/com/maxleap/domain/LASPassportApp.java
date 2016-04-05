package com.maxleap.domain;

import com.maxleap.domain.base.ObjectId;
import com.maxleap.domain.mongo.BaseEntity;

/**
 * User: qinpeng
 * Date: 14-7-20
 * Time: 23:38
 */
public class LASPassportApp extends BaseEntity<ObjectId> {

    public static final String FIELD_PASSPORT_ID = "passportId";
    public static final String FIELD_APP_ID = "appId";


    private String passportId;
    private String appId;

    public String getPassportId() {
        return passportId;
    }

    public void setPassportId(String passportId) {
        this.passportId = passportId;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }
}
