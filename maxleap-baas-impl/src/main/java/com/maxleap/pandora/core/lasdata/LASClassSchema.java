package com.maxleap.pandora.core.lasdata;

import com.maxleap.domain.auth.LASPermission;
import com.maxleap.domain.base.ObjectId;
import com.maxleap.domain.mongo.BaseEntity;
import com.maxleap.pandora.core.utils.LASObjectJsons;

import java.util.Map;

/**
 * User: qinpeng
 * Date: 14-5-6
 * Time: 15:03
 */
public class LASClassSchema extends BaseEntity<ObjectId> {
    private Map<String, LASKeyInfo> keys;
    private ObjectId appId;

    /**
     * Bind to class
     */
    private LASClassBind bindTo;
    private LASPermission clientPermission = new LASPermission();

    protected String className;
    protected String dbName;

    /**
     * must be unique in system. because cache use it.
     */
    protected String collectionName;

    protected boolean fullTextSearch;

    public LASClassSchema() {
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public Map<String, LASKeyInfo> getKeys() {
        return keys;
    }

    public void setKeys(Map<String, LASKeyInfo> keys) {
        this.keys = keys;
    }

    public boolean isFullTextSearch() {
        return fullTextSearch;
    }

    public void setFullTextSearch(boolean fullTextSearch) {
        this.fullTextSearch = fullTextSearch;
    }

    public ObjectId getAppId() {
        return appId;
    }

    public void setAppId(ObjectId appId) {
        this.appId = appId;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public String getCollectionName() {
        return collectionName;
    }

    public void setCollectionName(String collectionName) {
        this.collectionName = collectionName;
    }


    public LASPermission getClientPermission() {
        return clientPermission;
    }

    public void setClientPermission(LASPermission clientPermission) {
        this.clientPermission = clientPermission;
    }

    public LASClassBind getBindTo() {
        return bindTo;
    }

    public void setBindTo(LASClassBind bindTo) {
        this.bindTo = bindTo;
    }

    @Override
    public String toString() {
        return LASObjectJsons.serialize(this);
    }

}
