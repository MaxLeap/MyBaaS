package com.maxleap.pandora.data.support.mongo;

/**
 * User: qinpeng
 * Date: 14-4-28
 * Time: 11:31
 */
public class CreateMessage<ID> extends Response {

    private ID objectId;
    private long createdAt;

    public ID getObjectId() {
        return objectId;
    }

    public void setObjectId(ID objectId) {
        this.objectId = objectId;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "CreateMessage{" +
            "objectId=" + objectId +
            ", createdAt=" + createdAt +
            ", request=" + request +
            '}';
    }
}
