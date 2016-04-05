package com.maxleap.pandora.data.support.mongo;

/**
 * User: qinpeng
 * Date: 14-4-28
 * Time: 11:52
 */
public class DeleteMessage extends Response {

    private int number;

    public DeleteMessage(){}

    public DeleteMessage(int number) {
        this.number = number;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    @Override
    public String toString() {
        return "{" +
            "number=" + number +
            ", request=" + request +
            '}';
    }
}
