package com.maxleap.pandora.data.support.mongo;


import com.maxleap.domain.mongo.BaseEntity;

import java.io.Serializable;

/**
 * User: qinpeng
 * Date: 14-4-28
 * Time: 11:39
 */
public class FindOneMessage<Entity extends BaseEntity<ID>, ID extends Serializable> extends Response {
    private Entity result;

    public FindOneMessage() {
    }

    public FindOneMessage(MgoRequest request, Entity result) {
        this.request = request;
        this.result = result;
    }

    public Entity getResult() {
        return result;
    }

    public void setResult(Entity result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return "{" +
            "result=" + result +
            ", request=" + request +
            '}';
    }
}
