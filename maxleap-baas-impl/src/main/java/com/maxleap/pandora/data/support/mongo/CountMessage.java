package com.maxleap.pandora.data.support.mongo;

/**
 * User: qinpeng
 * Date: 14-4-28
 * Time: 11:50
 */
public class CountMessage extends Response {

    private int count;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    @Override
    public String toString() {
        return "CountMessage{" +
            ", count=" + count +
            ", request=" + request +
            '}';
    }
}
