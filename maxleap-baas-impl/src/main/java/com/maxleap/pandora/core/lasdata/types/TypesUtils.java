package com.maxleap.pandora.core.lasdata.types;

import com.maxleap.domain.base.ObjectId;
import com.maxleap.pandora.core.exception.LASDataException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author sneaky
 * @since 2.0.0
 */
public class TypesUtils {
    public static Object toMap(Object obj) {
        if (obj == null) {
            return null;
        }
        if ((obj instanceof String) || (obj instanceof Number) || (obj instanceof Boolean)) {
            return obj;
        } else if (obj instanceof ObjectId) {
            Map $oid = new HashMap();
            $oid.put("$oid", obj.toString());
            return $oid;
        } else if (obj instanceof LASBytes) {
            return ((LASBytes) obj).toMap();
        } else if (obj instanceof LASDate) {
            return ((LASDate) obj).toMap();
        } else if (obj instanceof LASFile) {
            return ((LASFile) obj).toMap();
        } else if (obj instanceof LASGeoPoint) {
            return ((LASGeoPoint) obj).toMap();
        } else if (obj instanceof LASPointer) {
            return ((LASPointer) obj).toMap();
        } else if (obj instanceof LASRelation) {
            return ((LASRelation) obj).toMap();
        } else if (obj instanceof Map) {
            return toMap((Map) obj);
        } else if (obj instanceof List) {
            return toMap((List) obj);
        } else if (obj instanceof Object[]) {
            return toMap((Object[]) obj);
        } else {
            throw new LASDataException(1, "Type mismatch. must be of [String, Number, Boolean, ObjectId, LASBytes, LASDate, LASFile, LASGeoPoint, LASPointer, LASRelation, Map, List]");
        }
    }

    public static Map toMap(Map map) {
        for (Object key : map.keySet()) {
            Object obj = map.get(key);
            Object o = toMap(obj);
            if (o instanceof Map || o instanceof List) {
                map.put(key, o);
            }
        }
        return map;
    }

    public static List toMap(List list) {
        int size = list.size();

        int j = 0;
        for (int i = 0; i < size; i++) {

            Object obj = list.get(j++);
            Object o = toMap(obj);
            if (o instanceof Map | o instanceof List) {
                list.remove(obj);
                list.add(o);
                j--;
            }
        }
        return list;
    }

    public static List toMap(Object[] array) {
        List list = new ArrayList(array.length);
        for (Object o : array) {
            list.add(toMap(o));
        }
        return list;
    }
}
