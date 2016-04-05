package com.maxleap.pandora.data.support.mongo;

import com.maxleap.domain.base.ObjectId;

import java.util.Map;

/**
 * User: qinpeng
 * Date: 14-4-28
 * Time: 11:32
 */
public class UpdateMessage extends Response {

    private int number;
    /**
     * $inc result.
     */
    private Map<String, Number> result;
    private ObjectId objectId;

    private Long updatedAt;

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Map<String, Number> getResult() {
        return result;
    }

    public void setResult(Map<String, Number> result) {
        this.result = result;
    }

    public ObjectId getObjectId() {
        return objectId;
    }

    public void setObjectId(ObjectId objectId) {
        this.objectId = objectId;
    }

    @Override
    public String toString() {
        return "UpdateMessage{" +
            "number=" + number +
            ", result=" + result +
            ", objectId=" + objectId +
            ", updatedAt=" + updatedAt +
            ", request=" + request +
            '}';
    }
}
