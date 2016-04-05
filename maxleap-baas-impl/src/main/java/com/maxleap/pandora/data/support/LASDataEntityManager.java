package com.maxleap.pandora.data.support;

import com.maxleap.domain.auth.LASPrincipal;
import com.maxleap.domain.base.LASObject;
import com.maxleap.domain.base.ObjectId;
import com.maxleap.pandora.core.lasdata.LASQuery;
import com.maxleap.pandora.core.lasdata.LASUpdate;
import com.maxleap.pandora.data.support.lasdata.UpdateResult;

import java.util.List;

/**
 * @author sneaky
 * @since 3.0.0
 */
public interface LASDataEntityManager {
  LASObject create(ObjectId appId, String className, LASPrincipal principal, LASObject entity);

  void create(ObjectId appId, String className, LASPrincipal principal, List<LASObject> entities);

  int delete(ObjectId appId, String className, LASPrincipal principal, ObjectId id);

  int delete(ObjectId appId, String className, LASPrincipal principal, LASQuery query);

  LASObject get(ObjectId appId, String className, LASPrincipal principal, ObjectId id);

  List<LASObject> getAll(ObjectId appId, String className, LASPrincipal principal);

  <Result> Result update(ObjectId appId, String className, LASPrincipal principal, ObjectId id, LASUpdate update);

  <Result> Result update(ObjectId appId, String className, LASPrincipal principal, LASQuery query, LASUpdate update);

  UpdateResult updateWithIncResult(ObjectId appId, String className, LASPrincipal principal, ObjectId id, LASUpdate update);

  UpdateResult updateWithIncResult(ObjectId appId, String className, LASPrincipal principal, LASQuery query, LASUpdate update);

  LASObject findUniqueOne(ObjectId appId, String className, LASPrincipal principal, LASQuery query);

  List<LASObject> find(ObjectId appId, String className, LASPrincipal principal, LASQuery query);

  long count(ObjectId appId, String className, LASPrincipal principal);

  long count(ObjectId appId, String className, LASPrincipal principal, LASQuery query);

}