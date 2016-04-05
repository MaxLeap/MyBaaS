package com.maxleap.pandora.data.support.filter;

import com.maxleap.domain.base.LASObject;
import com.maxleap.domain.base.ObjectId;
import com.maxleap.pandora.core.exception.LASDataException;
import com.maxleap.pandora.core.exception.TypeInvalidException;
import com.maxleap.pandora.core.lasdata.LASKeyType;
import com.maxleap.pandora.core.lasdata.types.*;
import com.maxleap.pandora.core.utils.DateUtils;
import com.maxleap.pandora.data.support.filter.support.Filter;
import com.maxleap.pandora.data.support.filter.support.FilterChain;
import com.maxleap.pandora.data.support.lasdata.*;
import com.maxleap.pandora.data.support.mongo.MgoRequest;
import com.maxleap.pandora.data.support.mongo.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 *
 * @author sneaky
 * @since 2.0
 */
@Singleton
public class AppDataConvertFilter implements Filter {
  private static transient final Logger log = LoggerFactory.getLogger(AppDataConvertFilter.class);

  public final static String name = "AppDataConvertFilter";

  @Override
  public void doFilter(MgoRequest request, FilterChain chain) throws LASDataException {
    // change format
    if (request instanceof AppFindOneRequest) {
      AppFindOneRequest findOneRequest = (AppFindOneRequest) request;
      parse(findOneRequest.getQuery().getQuery());
    }else if (request instanceof AppFindRequest) {
      AppFindRequest findRequest = (AppFindRequest) request;
      parse(findRequest.getQuery().getQuery());
    }else if (request instanceof AppUpdateRequest) {
      AppUpdateRequest appUpdateRequest = (AppUpdateRequest) request;
      parse(appUpdateRequest.getQuery().getQuery());
      parse(appUpdateRequest.getUpdate().getModifierOps());
    } else if (request instanceof AppCreateRequest) {
      AppCreateRequest saveRequest = (AppCreateRequest) request;
      parse((Map) saveRequest.getDocument());
    } else if (request instanceof AppCountRequest) {
      AppCountRequest appCountRequest = (AppCountRequest) request;
      parse(appCountRequest.getQuery().getQuery());
    } else if (request instanceof AppDeleteRequest) {
      AppDeleteRequest appDeleteRequest = (AppDeleteRequest) request;
      parse(appDeleteRequest.getQuery().getQuery());
    } else if (request instanceof AppCreateManyRequest) {
      AppCreateManyRequest createManyRequest = (AppCreateManyRequest) request;
      for (Object doc : createManyRequest.getDocuments()) {
        parse((Map) doc);
      }
    }
    chain.doFilter(request);
  }

  @Override
  public void doFilter(Response response, FilterChain chain) throws LASDataException {
    chain.doFilter(response);
  }

  /**
   * root tree
   * @param map
   */
  public void parse(Map map) {
    for (Object key : map.keySet()) {
      Object obj = map.get(key);
      if (obj instanceof Map) {
        Map objMap = (Map) obj;
        parseSunType(map, key, objMap);
      } else if (obj instanceof List) {
        map.put(key, parseArray((List) obj));
      }
    }
  }

  public Object parseSunType(Object type, Map map) {
    // __type: Pointer
    if (type.equals(LASKeyType.Pointer.name())) {
      LASPointer pointer = parsePointer(map);
      return pointer;
    }

    // __type: Relation
    if (type.equals(LASKeyType.Relation.name())) {
      LASRelation relation = parseRelation(map);
      return relation;
    }

    // __type: Date
    if (type.equals(LASKeyType.Date.name())) {
      LASDate date = parseDate(map);
      return date;
    }

    // __type: Bytes
    if (type.equals(LASKeyType.Bytes.name())) {
      LASBytes bytes = parseBytes(map);
      return bytes;
    }

    // __type: File
    if (type.equals(LASKeyType.File.name())) {
      LASFile file = parseFile(map);
      return file;
    }

    // __type: GeoPoint
    if (type.equals(LASKeyType.GeoPoint.name())) {
      LASGeoPoint geoPoint = parseGeoPoint(map);
      return geoPoint;
    }

    throw new TypeInvalidException(map + String.format(" invalid __type value : %s", type));
  }

  private LASPointer parsePointer(Map jsonObject) {
    Object typeJsonElement = jsonObject.get(LASKeyType.KEY_OBJECT_TYPE);
    if (typeJsonElement == null) {
      return null;
    }

    String type = typeJsonElement.toString();
    if (!type.equals(LASKeyType.Pointer.name())) {
      return null;
    }

    Object classNameJsonElement = jsonObject.get("className");
    if (classNameJsonElement == null) {
      throw new TypeInvalidException(jsonObject + " is not valid Pointer");
    }
    String className = classNameJsonElement.toString();
    if ("".equals(className)) {
      throw new TypeInvalidException(jsonObject + " is not valid Pointer");
    }

    String objectId = null;
    Object objectIdJsonElement = jsonObject.get("objectId");

    if (objectIdJsonElement == null) {
      throw new TypeInvalidException(jsonObject + " is not valid Pointer");
    }

    objectId = objectIdJsonElement.toString();

    ObjectId.validate(null, objectId);

    LASPointer zCloudPointer = new LASPointer();
    zCloudPointer.setObjectId(objectId);
    zCloudPointer.setClassName(className);

    return zCloudPointer;
  }

  private LASRelation parseRelation(Map jsonObject) {
    Object jsonArray = jsonObject.get("objects");
    if (!(jsonArray instanceof List)) {
      throw new TypeInvalidException(jsonObject + " is not valid Relation");
    }

    List array = (List) jsonArray;

    if (array.size() == 0) {
      throw new TypeInvalidException(jsonObject + " is not valid Relation");
    }

    LASRelation zCloudRelation = new LASRelation();

    for (int i = 0; i < array.size(); i++) {
      if (array.get(i) instanceof Map) {
        LASPointer pointer = parsePointer((Map) array.get(i));
        if (pointer == null) {
          throw new TypeInvalidException(jsonObject + " is not valid Relation");
        }
        if (i == 0) {
          zCloudRelation.setClassName(pointer.getClassName());
        }
        zCloudRelation.getObjects().add(pointer);
      } else {
        throw new TypeInvalidException(jsonObject + " is not valid Relation");
      }
    }

    return zCloudRelation;
  }

  private LASArray parseArray(List list) {
    int size = list.size();
    LASArray array = new LASArray();

    for (int i = 0; i < size; i++) {
      Object o = list.get(i);
      if (o instanceof List) {
        array.add(parseArray((List) o));
      } else if (o instanceof Map) {
        Map objMap = (Map) o;
        Object type = objMap.get(LASKeyType.KEY_OBJECT_TYPE);
        if (type != null) {
          array.add(parseSunType(type, objMap));
          continue;
        }

        Object op = objMap.get("__op");
        if (op != null) {
          //__type Relation
          if (op.equals("AddRelation")) {
            LASRelation relation = parseRelation(objMap);
            array.add(relation);
          }

          if (!(op.equals("Increment")
              || op.equals("Add")
              || op.equals("AddUnique")
              || op.equals("Remove")
              || op.equals("Delete")
              || op.equals("AddRelation")
              || op.equals("RemoveRelation"))) {
            throw new LASDataException(LASDataException.INVALID_JSON, String.format("invalid __op value : %s", op));
          }
          continue;
        }

        array.add(parseObject((Map) o));
      } else {
        array.add(o);
      }
    }
    return array;
  }

  private Object parseObject(Map obj) {
    LASObject object = new LASObject();
    for (Object key :obj.keySet()) {
      Object o = obj.get(key);
      if (o instanceof Map) {
        parseSunType(object, key, (Map) o);
      } else if (o instanceof List) {
        object.put(key, parseArray((List) o));
      } else {
        object.put(key, o);
      }
    }
    return object;
  }

  private void parseSunType(Map obj, Object key, Map objMap) {
    Object type = objMap.get(LASKeyType.KEY_OBJECT_TYPE);
    if (type != null) {
      obj.put(key, parseSunType(type, objMap));
      return ;
    }

    Object op = objMap.get("__op");
    if (op != null) {
      //__type Relation
      if (op.equals("AddRelation")) {
        LASRelation relation = parseRelation(objMap);
        obj.put(key, relation);
      }

      if (!(op.equals("Increment")
          || op.equals("Add")
          || op.equals("AddUnique")
          || op.equals("Remove")
          || op.equals("Delete")
          || op.equals("AddRelation")
          || op.equals("RemoveRelation"))) {
        throw new LASDataException(LASDataException.INVALID_JSON, String.format("invalid __op value : %s", op));
      }
      return ;
    }

    obj.put(key, parseObject(objMap));
  }

  private LASDate parseDate(Map jsonObject) {
    Object typeJsonElement = jsonObject.get(LASKeyType.KEY_OBJECT_TYPE);
    if (typeJsonElement == null) {
      return null;
    }
    String type = typeJsonElement.toString();
    if (!type.equals(LASKeyType.Date.name())) {
      return null;
    }

    Object isoJsonElement = jsonObject.get("iso");
    if (isoJsonElement == null) {
      throw new TypeInvalidException(jsonObject + " is not valid Date");
    }
    String iso = isoJsonElement.toString();
    Date date = DateUtils.parseDate(iso);


    if (date == null) {
      throw new TypeInvalidException(jsonObject + " is not valid Date");
    }

    LASDate zCloudDate = new LASDate();
    zCloudDate.setIso(iso);
    return zCloudDate;
  }

  private LASBytes parseBytes(Map jsonObject) {
    Object typeJsonElement = jsonObject.get(LASKeyType.KEY_OBJECT_TYPE);
    if (typeJsonElement == null) {
      return null;
    }
    String type = typeJsonElement.toString();
    if (!type.equals(LASKeyType.Bytes.name())) {
      return null;
    }

    Object base64JsonElement = jsonObject.get("base64");
    if (base64JsonElement == null) {
      throw new TypeInvalidException(jsonObject + " is not valid Bytes");
    }
    String base64 = base64JsonElement.toString();

    LASBytes zCloudBytes = new LASBytes();
    zCloudBytes.setBase64(base64);
    return zCloudBytes;
  }

  private LASFile parseFile(Map jsonObject) {
    Object typeJsonElement = jsonObject.get(LASKeyType.KEY_OBJECT_TYPE);
    if (typeJsonElement == null) {
      return null;
    }
    String type = typeJsonElement.toString();
    if (!type.equals(LASKeyType.File.name())) {
      return null;
    }

    Object nameJsonElement = jsonObject.get("name");
    if (nameJsonElement == null) {
      throw new TypeInvalidException(jsonObject + " is not valid File");
    }
    String name = nameJsonElement.toString();

    Object urlJsonElement = jsonObject.get("url");
    if (urlJsonElement == null) {
      throw new TypeInvalidException(jsonObject + " is not valid File");
    }
    String url = urlJsonElement.toString();

    LASFile zCloudFile = new LASFile(url, name);
    return zCloudFile;
  }

  private LASGeoPoint parseGeoPoint(Map jsonObject) {
    Object typeJsonElement = jsonObject.get(LASKeyType.KEY_OBJECT_TYPE);
    if (typeJsonElement == null) {
      return null;
    }
    String type = typeJsonElement.toString();
    if (!type.equals(LASKeyType.GeoPoint.name())) {
      return null;
    }

    Object latitudeJsonElement = jsonObject.get("latitude");
    Object longitudeJsonElement = jsonObject.get("longitude");

    if (longitudeJsonElement == null || latitudeJsonElement == null) {
      throw new TypeInvalidException(jsonObject + " is not valid GeoPoint");
    }

    try {
      double latitude = ((Number) latitudeJsonElement).doubleValue();
      double longitude = ((Number) longitudeJsonElement).doubleValue();
      if (latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) {
        throw new TypeInvalidException(jsonObject + " is not valid GeoPoint");
      }
      LASGeoPoint zCloudGeoPoint = new LASGeoPoint(latitude, longitude);
      return zCloudGeoPoint;
    } catch (NumberFormatException e) {
      throw new TypeInvalidException(jsonObject + " is not valid GeoPoint");
    }

  }
  @Override
  public String name() {
    return name;
  }
}
