package com.maxleap.las.bass.apiserver.resource;

import com.maxleap.pandora.core.utils.DateUtils;

import java.util.*;

/**
 *
 * @author sneaky
 * @since 1.0.0
 */
public class TestData {
    Map map = new HashMap();
    double[][] geopoints = new double[][] {{121.10010, 31.11341}, {121.10013, 31.11339}, {121.10008, 31.11345}, {121.10005, 31.113395}, {121.10000, 31.11331}};
    
    public TestData string() {
        return string("string", UUID.randomUUID().toString());
    }

    public TestData string(String key, String value) {
        map.put(key, value);
        return this;
    }

    public TestData booleans() {
        return booleans("boolean", random(10000) % 2 == 0 ? true : false);
    }

    public TestData booleans(String key, boolean value) {
        map.put(key, value);
        return this;
    }

    public TestData number() {
        return number("number", random(10000000));
    }

    public TestData number(String key, Number value) {
        map.put(key, value);
        return this;
    }

    public TestData object() {
        Map object = new HashMap();

        object.put("object1", "test1");
        object.put("object2", random(1000000));
        object.put("object3", true);

        return object("object", object);
    }

    public TestData object(String key, Map value) {
        map.put(key, value);
        return this;
    }

    public TestData pointer(String className, String objectId) {
        Map pointer = new HashMap();

        pointer.put("__type", "Pointer");
        pointer.put("className", className);
        pointer.put("objectId", objectId);

        return pointer("pointer", pointer);
    }

    public TestData pointer(String key, String className, String objectId) {
        Map pointer = new HashMap();

        pointer.put("__type", "Pointer");
        pointer.put("className", className);
        pointer.put("objectId", objectId);

        return pointer(key, pointer);
    }

    public TestData pointer(String key, Map value) {
        map.put(key, value);
        return this;
    }

    public TestData relation(String className, String objectId) {
        Map relation = new HashMap();
        relation.put("__op", "AddRelation");

        Map pointer = new HashMap();

        pointer.put("__type", "Pointer");
        pointer.put("className", className);
        pointer.put("objectId", objectId);

        List list = new ArrayList<>();
        list.add(pointer);

        relation.put("objects", list);

        return relation("relation", relation);
    }

    public TestData relation(String key, Map value) {
        map.put(key, value);
        return this;
    }

    public TestData date() {
        Map date = new HashMap();

        date.put("__type", "Date");
        date.put("iso", DateUtils.encodeDate(new Date()));

        return date("date", date);
    }

    public TestData date(String key, Map value) {
        map.put(key, value);
        return this;
    }

    public TestData geoPoint() {
        return geoPoint("geoPoint");
    }

    public TestData geoPoint(String key) {
        Map geoPoint = new HashMap();
        geoPoint.put("__type", "GeoPoint");
        int random = random(4);
        geoPoint.put("latitude", geopoints[random][1]);
        geoPoint.put("longitude", geopoints[random][0]);

        return geoPoint(key, geoPoint);
    }

    public TestData geoPoint(String key, Map value) {
        map.put(key, value);
        return this;
    }

    public TestData bytes() {
        Map bytes = new HashMap();
        bytes.put("__type", "Bytes");
        bytes.put("base64", "VGhpcyBpcyBhbiBlbmNvZGVkIHN0cmluZw==");

        return bytes("bytes", bytes);
    }

    public TestData bytes(String key, Map value) {
        map.put(key, value);
        return this;
    }

    public TestData array() {
        List<Object> array = new ArrayList<>();

        array.add(random(10000));
        array.add(random(10000));
        array.add(random(10000));
        array.add(random(10000));
        array.add(random(10000));
        array.add(random(10000));
        array.add(random(10000));
        array.add(2);
        array.add(5);
        array.add(8);

        return array("array", array);
    }

    public TestData array(String key, List value) {
        map.put(key, value);
        return this;
    }

    public int random(int range) {
        return (int) (Math.random() * range);
    }

    public TestData initBaseData() {
        string().number().number("number2", random(10000)).array().booleans().bytes().date().geoPoint().object();
        return this;
    }

    public Map data() {
        return map;
    }
}
