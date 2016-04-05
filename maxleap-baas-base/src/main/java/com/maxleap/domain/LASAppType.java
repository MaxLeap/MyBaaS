package com.maxleap.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * User: qinpeng
 * Date: 14-7-1
 * Time: 13:40
 */
public enum LASAppType {

    iPhone(0), iPad(1), iOSUniversal(2), android(3), blackBerry(4);

    private final int type;

    LASAppType(int type) {
        this.type = type;
    }

    @JsonValue
    public String toString() {
        return String.valueOf(this.type);
    }

    public int toInt() {
        return this.type;
    }

    public static LASAppType fromInt(int t) {
        if (t == 0)
            return iPhone;
        else if (t == 1)
            return iPad;
        else if (t == 2)
            return iOSUniversal;
        else if (t == 3)
            return android;
        else if (t == 4)
            return blackBerry;
        else
            return null;
    }

    @JsonCreator
    public static LASAppType fromString(String str) {
        int t = Integer.parseInt(str);
        if (t == 0)
            return iPhone;
        else if (t == 1)
            return iPad;
        else if (t == 2)
            return iOSUniversal;
        else if (t == 3)
            return android;
        else if (t == 4)
            return blackBerry;
        else
            return null;
    }

}
