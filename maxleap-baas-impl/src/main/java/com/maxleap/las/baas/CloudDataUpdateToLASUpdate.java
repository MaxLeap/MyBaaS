package com.maxleap.las.baas;

import com.maxleap.pandora.core.exception.TypeInvalidException;
import com.maxleap.pandora.core.lasdata.LASUpdate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author sneaky
 * @since 1.0.0
 */
public class CloudDataUpdateToLASUpdate {
  public static final String OP_KEY = "__op";
  public static final String Increment = "Increment";
  public static final String AddUnique = "AddUnique";
  public static final String Add = "Add";
  public static final String Remove = "Remove";
  public static final String AddRelation = "AddRelation";
  public static final String RemoveRelation = "RemoveRelation";
  public static final String Delete = "Delete";

  public static LASUpdate from(Map<String, Object> updateMap) {
    LASUpdate update = LASUpdate.getLASUpdate();

    updateMap.forEach((key, value) -> {
      if (value instanceof HashMap) {
        Map<String, Object> valueMap = (Map<String, Object>) value;
        String op = (String) valueMap.get(OP_KEY);
        if (op != null) {
          switch (op) {
            case Increment:
              if (!valueMap.containsKey("amount")) {
                throw new TypeInvalidException("Read Increment update error, can not find amount key.");
              }
              Object amount = valueMap.get("amount");
              if (amount instanceof Float || amount instanceof Integer || amount instanceof Double || amount instanceof Short || amount instanceof Byte) {
                update.inc(key, (Number) amount);
              } else {
                throw new TypeInvalidException("amount must be number");
              }
              break;
            /* array */
            case AddUnique:
              if (!valueMap.containsKey("objects")) {
                throw new TypeInvalidException("Read AddUnique array update error, can not find objects key.");
              }
              Object objects = valueMap.get("objects");
              if (objects instanceof List) {
                update.addToSet(key, ((List) objects));
              } else {
                throw new TypeInvalidException("Read AddUnique array update error, objects key must be array");
              }
              break;
            case Add:
              if (!valueMap.containsKey("objects")) {
                throw new TypeInvalidException("Read Add array update error, can not find objects key.");
              }
              Object add = valueMap.get("objects");
              if (add instanceof List) {
                update.push(key, ((List) add));
              } else {
                throw new TypeInvalidException("Read AddUnique array update error, objects key must be array");
              }
              break;
            case Remove:
              if (!valueMap.containsKey("objects")) {
                throw new TypeInvalidException("Read Remove array update error, can not find objects key.");
              }
              Object del = valueMap.get("objects");
              if (del instanceof List) {
                update.pullAll(key, ((List) del));
              } else {
                throw new TypeInvalidException("Read Remove array update error, objects key must be array");
              }
              break;
                        /* relation */
            case AddRelation:
              if (!valueMap.containsKey("objects")) {
                throw new TypeInvalidException("Read AddRelation update error, can not find objects key.");
              }
              Object addRelation = valueMap.get("objects");
              if (addRelation instanceof List) {
                update.addToSet(key, ((List) addRelation));
              } else {
                throw new TypeInvalidException("Read AddRelation update error, objects key must be array");
              }

              break;

            case RemoveRelation:
              if (!valueMap.containsKey("objects")) {
                throw new TypeInvalidException("Read Add Relation update error, can not find objects key.");
              }
              Object delRelation = valueMap.get("objects");
              if (delRelation instanceof List) {
                update.pullAll(key, ((List) delRelation));
              } else {
                throw new TypeInvalidException("Read RemoveRelation update error, objects key must be array");
              }
              break;
                        /* delete */
            case Delete:
              update.unset(key);
              break;
            default:
              throw new TypeInvalidException("Not supported __op " + op);
          }
        } else {
          update.set(key, value);
        }
      } else {
        update.set(key, value);
      }
    });
    return update;
  }
}
