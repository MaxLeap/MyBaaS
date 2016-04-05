package com.maxleap.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * User: qinpeng
 * Date: 14-6-25
 * Time: 10:08
 */
public enum LASAppStatus {

    enabled(0), disabled(1), deleted(2);

    private final int status;

    LASAppStatus(int status) {
        this.status = status;
    }

    @JsonValue
    public String toString() {
        return String.valueOf(this.status);
    }

    public int toInt() {
        return this.status;
    }

    public static LASAppStatus fromInt(int t) {
        if (t == 0)
            return enabled;
        else if (t == 1)
            return disabled;
        else if (t == 2)
            return deleted;
        else
            return null;
    }

    @JsonCreator
    public static LASAppStatus fromString(String str) {
        int t = Integer.parseInt(str);
        if (t == 0)
            return enabled;
        else if (t == 1)
            return disabled;
        else if (t == 2)
            return deleted;
        else
            return null;
    }
}
