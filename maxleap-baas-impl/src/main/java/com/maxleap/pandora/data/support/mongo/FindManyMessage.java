package com.maxleap.pandora.data.support.mongo;


import com.maxleap.domain.mongo.BaseEntity;

import java.io.Serializable;
import java.util.List;

/**
 * User: qinpeng
 * Date: 14-4-28
 * Time: 11:36
 */
public class FindManyMessage<Entity extends BaseEntity<ID>, ID extends Serializable> extends Response {
    private List<Entity> results;
    int count;

    public FindManyMessage() {}

    public FindManyMessage(MgoRequest request, List<Entity> results) {
        this.request = request;
        this.results = results;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public List<Entity> getResults() {
        return results;
    }

    public void setResults(List<Entity> results) {
        this.results = results;
    }

    @Override
    public String toString() {
        return "{" +
            "results=" + results +
            ", count=" + count +
            ", request=" + request +
            '}';
    }
}