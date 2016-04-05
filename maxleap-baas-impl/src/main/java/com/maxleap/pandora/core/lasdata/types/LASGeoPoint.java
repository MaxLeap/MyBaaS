package com.maxleap.pandora.core.lasdata.types;

import com.maxleap.pandora.core.lasdata.LASKeyType;
import com.maxleap.pandora.core.mongo.GeoPoint;

import java.util.HashMap;
import java.util.Map;

/**
 * User: qinpeng
 * Date: 14-5-19
 * Time: 9:21
 */
public class LASGeoPoint extends GeoPoint {
  private final String __type = LASKeyType.GeoPoint.name();

  public LASGeoPoint(double latitude, double longitude) {
    super(latitude, longitude);
  }
  public Map toMap() {
    Map map = new HashMap();
    map.put("__type", __type);
    map.put("latitude", latitude);
    map.put("longitude", longitude);
    return map;
  }
}
