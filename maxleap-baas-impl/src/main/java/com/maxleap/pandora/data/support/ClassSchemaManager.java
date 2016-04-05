package com.maxleap.pandora.data.support;

import com.maxleap.domain.base.ObjectId;
import com.maxleap.pandora.core.lasdata.LASClassSchema;
import com.maxleap.pandora.core.lasdata.LASKeyInfo;
import com.maxleap.pandora.core.mongo.MongoUpdate;

import java.util.List;
import java.util.Map;

/**
 * @author sneaky
 * @since 2.0.0
 */
public interface ClassSchemaManager {
  void validateKeyType(ObjectId appId, String className, Map doc);

  LASClassSchema create(LASClassSchema entity);

  LASClassSchema get(ObjectId id);

  LASClassSchema get(ObjectId appId, String className);

  int update(ObjectId appId, String className, MongoUpdate mongoUpdate);

  int delete(ObjectId id);

  int delete(ObjectId appId, String className);

  List<LASClassSchema> getAll(ObjectId appId);

  void setKeyInfo(ObjectId id, Map<String, LASKeyInfo> keyInfo);

  void unsetKeyInfo(ObjectId id, List<String> classNameList);



}
