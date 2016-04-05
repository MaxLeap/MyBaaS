package com.maxleap.pandora.data.support.lasdata;

import com.maxleap.domain.base.ObjectId;
import com.maxleap.domain.mongo.BaseEntity;
import com.maxleap.pandora.data.support.MongoJsons;

/**
 * User: qinpeng
 * Date: 14-5-6
 * Time: 15:03
 */
public class LASSystemSchema extends BaseEntity<ObjectId> {
    protected String className;
    protected String dbName;

    /**
     * must be unique in system. because cache use it.
     */
    protected String collectionName;

    protected boolean fullTextSearch;

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        LASSystemSchema obj = (LASSystemSchema) o;

        return (obj.collectionName == null && this.collectionName == null || obj.collectionName.equals(this.collectionName))
            && obj.fullTextSearch == this.fullTextSearch
            && (obj.className == null && this.className == null || obj.className.equals(this.className))
            && (obj.dbName == null && this.dbName == null || obj.dbName.equals(this.dbName));

    }

    @Override
    public int hashCode() {
        int result = 0;

        if (this.getClassName() != null) {
            result = 17 * result + this.getClassName().hashCode();
        }

        if (this.getCollectionName() != null) {
            result = 17 * result + this.getCollectionName().hashCode();
        }

        if (this.getDbName() != null) {
            result = 17 * result + this.getDbName().hashCode();
        }

        result = 17 * result + (fullTextSearch ? 1 : 0);

        return result;
    }

    @Override
    public String toString() {
        return MongoJsons.serialize(this);
    }

}
