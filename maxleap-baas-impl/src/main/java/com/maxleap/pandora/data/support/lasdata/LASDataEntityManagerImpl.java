package com.maxleap.pandora.data.support.lasdata;

import com.maxleap.domain.auth.LASPrincipal;
import com.maxleap.domain.auth.PermissionType;
import com.maxleap.domain.base.LASObject;
import com.maxleap.domain.base.ObjectId;
import com.maxleap.domain.mongo.BaseEntity;
import com.maxleap.pandora.config.DataSourceStatus;
import com.maxleap.pandora.config.DatabaseVisitor;
import com.maxleap.pandora.config.mgo.MgoDatabase;
import com.maxleap.pandora.core.exception.LASDataException;
import com.maxleap.pandora.core.lasdata.LASClassSchema;
import com.maxleap.pandora.core.lasdata.LASQuery;
import com.maxleap.pandora.core.lasdata.LASUpdate;
import com.maxleap.pandora.data.support.ClassSchemaManager;
import com.maxleap.pandora.data.support.LASDataEntityManager;
import com.maxleap.pandora.data.support.filter.support.FilterChain;
import com.maxleap.pandora.data.support.filter.support.LASDataFilterChainManager;
import com.maxleap.pandora.data.support.mongo.FindManyMessage;
import com.maxleap.pandora.data.support.mongo.FindOneMessage;
import com.maxleap.pandora.data.support.mongo.MongoDao;
import com.maxleap.pandora.data.support.utils.DbAndTableMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author sneaky
 * @since 3.0.0
 */
@Singleton
public class LASDataEntityManagerImpl implements LASDataEntityManager {
  private static final Logger LOGGER = LoggerFactory.getLogger(MongoDao.class);

  private ClassSchemaManager classSchemaManager;
  private MongoDao mongoDao;
  private LASDataFilterChainManager filterChainManager;
  private LASOperatorHandler lasOperatorHandler;
  private DatabaseVisitor<MgoDatabase> mgoDatabaseVisitor;

  private Lock lock = new ReentrantLock();

  @Inject
  public LASDataEntityManagerImpl(ClassSchemaManager classSchemaManager,
                                  MongoDao mongoDao,
                                  DatabaseVisitor mgoDatabaseVisitor,
                                  LASDataFilterChainManager filterChainManager,
                                  LASOperatorHandler lasOperatorHandler) {
    this.classSchemaManager = classSchemaManager;
    this.mongoDao = mongoDao;
    this.mgoDatabaseVisitor = mgoDatabaseVisitor;
    this.filterChainManager = filterChainManager;
    this.lasOperatorHandler = lasOperatorHandler;
  }

  @Override
  public  LASObject create(ObjectId appId, String className,  LASPrincipal principal, LASObject entity) {
    long start = System.currentTimeMillis();

    LASClassSchema lasClassSchema = classSchemaManager.get(appId, className);
    if (lasClassSchema == null) {
      lasClassSchema = createIfPermit(principal, appId, className);
    }

    long getSchema = System.currentTimeMillis();

    String db = lasClassSchema.getDbName();
    String table = lasClassSchema.getCollectionName();
    AppCreateRequest request = new AppCreateRequest(getMgoDatabase(db), lasClassSchema, entity, principal);
    FilterChain filterChain = filterChainManager.getFilterChain(appId, className);
    filterChain.doFilter(request);

    long handleFilter = System.currentTimeMillis();

    LASObject lasObject = mongoDao.create(lasClassSchema.getDbName(), lasClassSchema.getCollectionName(), entity);

    long create = System.currentTimeMillis();

    if (create - start > 100) {
      LOGGER.warn("[lasdata-create] appId: {}, className: {}, getSchema: {}, handleFilter: {}, create: {}, total: {}",
          appId, className, getSchema - start, handleFilter - getSchema, create - handleFilter, create - start);
    }

    return lasObject;
  }

  @Override
  public  void create(ObjectId appId, String className,  LASPrincipal principal, List<LASObject> entities) {
    long start = System.currentTimeMillis();

    LASClassSchema lasClassSchema = classSchemaManager.get(appId, className);
    if (lasClassSchema == null) {
      lasClassSchema = createIfPermit(principal, appId, className);
    }

    long getSchema = System.currentTimeMillis();

    String db = lasClassSchema.getDbName();
    String table = lasClassSchema.getCollectionName();
    AppCreateManyRequest request = new AppCreateManyRequest(getMgoDatabase(db), lasClassSchema, entities, principal);
    FilterChain filterChain = filterChainManager.getFilterChain(appId, className);
    filterChain.doFilter(request);

    long handleFilter = System.currentTimeMillis();

    mongoDao.create(lasClassSchema.getDbName(), lasClassSchema.getCollectionName(), entities);

    long create = System.currentTimeMillis();

    if (create - start > 100) {
      LOGGER.warn("[lasdata-createMany] appId: {}, className: {}, getSchema: {}, handleFilter: {}, create: {}, total: {}",
          appId, className, getSchema - start, handleFilter - getSchema, create - handleFilter, create - start);
    }
  }

  @Override
  public  int delete(ObjectId appId, String className,  LASPrincipal principal, ObjectId id) {
    LASQuery query = new LASQuery();
    query.equalTo("_id", id);
    return delete(appId, className, principal, query);
  }

  @Override
  public int delete(ObjectId appId, String className,  LASPrincipal principal,  LASQuery query) {
    long start = System.currentTimeMillis();

    LASClassSchema lasClassSchema = classSchemaManager.get(appId, className);
    if (lasClassSchema == null) {
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("Schema isn't exists[appId: {}, className: {}]", appId, className);
      }
      return 0;
    }

    long getSchema = System.currentTimeMillis();

    String db = lasClassSchema.getDbName();
    String table = lasClassSchema.getCollectionName();
    AppDeleteRequest request = new AppDeleteRequest(getMgoDatabase(db), lasClassSchema, query, principal);
    FilterChain filterChain = filterChainManager.getFilterChain(appId, className);
    filterChain.doFilter(request);

    long handleFilter = System.currentTimeMillis();

    int deleteCount = mongoDao.delete(db, table, query);

    long delete = System.currentTimeMillis();

    if (delete - start > 100) {
      LOGGER.warn("[lasdata-delete] appId: {}, className: {}, getSchema: {}, handleFilter: {}, delete: {}, total: {}",
          appId, className, getSchema - start, handleFilter - getSchema, delete - handleFilter, delete - start);
    }

    return deleteCount;
  }

  @Override
  public  LASObject get(ObjectId appId, String className,  LASPrincipal principal, ObjectId id) {
    return get(appId, className, principal, id, LASObject.class);
  }

  <T extends BaseEntity, ID extends Serializable> T get(ObjectId appId, String className,  LASPrincipal principal, ID id, Class<T> clazz) {
    LASQuery query = new LASQuery();
    query.equalTo("_id", id);
    return findUniqueOne(appId, className, principal, query, clazz);
  }

  @Override
  public  List<LASObject> getAll(ObjectId appId, String className, LASPrincipal principal) {
    return (List) getAll(appId, className, principal, LASObject.class);
  }

  <T extends BaseEntity, ID extends Serializable> List<T> getAll(ObjectId appId, String className,  LASPrincipal principal, Class<T> clazz) {
    LASQuery query = new LASQuery();
    query.setLimit(2000);
    return find(appId, className, principal, query, clazz);
  }

  @Override
  public <Result> Result update(ObjectId appId, String className,  LASPrincipal principal, ObjectId id, LASUpdate update) {
    LASQuery query = new LASQuery();
    query.equalTo("_id", id);
    return update(appId, className, principal, query, update);
  }

  @Override
  public <Result> Result update(ObjectId appId, String className,  LASPrincipal principal, LASQuery query, LASUpdate update) {
    long start = System.currentTimeMillis();

    LASClassSchema lasClassSchema = classSchemaManager.get(appId, className);
    if (lasClassSchema == null) {
      throw new LASDataException(401, "Class is not exists." + " className: " + className);
    }

    long getSchema = System.currentTimeMillis();

    String db = lasClassSchema.getDbName();
    String table = lasClassSchema.getCollectionName();
    AppUpdateRequest request = new AppUpdateRequest(getMgoDatabase(db), lasClassSchema, query, update, principal);
    FilterChain filterChain = filterChainManager.getFilterChain(appId, className);
    filterChain.doFilter(request);

    long handleFilter = System.currentTimeMillis();

    Result u = mongoDao.update(db, table, query, update);

    long updateTime = System.currentTimeMillis();

    if (updateTime - start > 100) {
      LOGGER.warn("[lasdata-update] appId: {}, className: {}, query: {}, getSchema: {}, handleFilter: {}, updateTime: {}, total: {}",
          appId, className, query, getSchema - start, handleFilter - getSchema, updateTime - handleFilter, updateTime - start);
    }

    return u;
  }

  @Override
  public UpdateResult updateWithIncResult(ObjectId appId, String className, LASPrincipal principal, ObjectId id, LASUpdate update) {
    return updateWithIncResult(appId, className, principal, new LASQuery().equalTo("objectId", id), update);
  }

  @Override
  public UpdateResult updateWithIncResult(ObjectId appId, String className, LASPrincipal principal, LASQuery query, LASUpdate update) {
    long ts = System.currentTimeMillis();
    update.set("updatedAt", ts);
    Object rs = this.update(appId, className, principal, query, update);
    UpdateResult updateResult = new UpdateResult();

    if (rs instanceof Integer) {
      updateResult.setNumber((Integer) rs);
      if (updateResult.getNumber() == 0 || updateResult.getNumber() > 1) {
        return updateResult;
      }
    } else {
      updateResult.setObjectId((ObjectId) rs);
      updateResult.setNumber(1);
    }
    updateResult.setUpdatedAt(ts);

    Object $inc = update.getModifierOps().get("$inc");
    if ($inc instanceof Map) {
      Map<String, ?> $incMap = (Map) $inc;
      LASQuery $incQuery = new LASQuery(query.getQuery());
      for (String incKey : $incMap.keySet()) {
        int $ = incKey.indexOf(".$.");
        if ($ < 0) {
          $incQuery.addProjectKey(incKey);
        }
      }
      $incQuery.excludeProjectKey("_id");
      LASObject doc = this.findUniqueOne(appId, className, principal, $incQuery);
      if (doc != null) {
        updateResult.setResult(new HashMap<>(doc));
      }
    }
    return updateResult;
  }

  @Override
  public LASObject findUniqueOne(ObjectId appId, String className,  LASPrincipal principal, LASQuery query) {
    return findUniqueOne(appId, className, principal, query, LASObject.class);
  }

  <T extends BaseEntity> T findUniqueOne(ObjectId appId, String className,  LASPrincipal principal, LASQuery query, Class<T> clazz) {
    long start = System.currentTimeMillis();

    LASClassSchema lasClassSchema = classSchemaManager.get(appId, className);
    if (lasClassSchema == null) {
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("Schema isn't exists[appId: {}, className: {}]", appId, className);
      }
      return null;
    }

    long getSchema = System.currentTimeMillis();

    String db = lasClassSchema.getDbName();
    String table = lasClassSchema.getCollectionName();
    AppFindOneRequest request = new AppFindOneRequest(getMgoDatabase(db), lasClassSchema, query, principal);
    FilterChain filterChain = filterChainManager.getFilterChain(appId, className);
    filterChain.doFilter(request);

    long handleFilter = System.currentTimeMillis();

    T object = (T) mongoDao.findUniqueOne(db, table, query, clazz);

    if (object == null) {
      return null;
    }

    FindOneMessage response = new FindOneMessage(request, object);
    filterChain.doFilter(response);

    if (request != null && object instanceof Map) {
      lasOperatorHandler.include(request, principal, request.getLasIncludes(), (Map) response.getResult());
    }

    long findUnique = System.currentTimeMillis();

    if (findUnique - start > 100) {
      LOGGER.warn("[lasdata-findUnique] appId: {}, className: {}, query: {}, getSchema: {}, handleFilter: {}, findUnique: {}, total: {}",
          appId, className, query, getSchema - start, handleFilter - getSchema, findUnique - handleFilter, findUnique - start);
    }

    return (T) response.getResult();
  }

  @Override
  public  List<LASObject> find(ObjectId appId, String className,  LASPrincipal principal, LASQuery query) {
    List<LASObject> lasObjects = find(appId, className, principal, query, LASObject.class);
    return (List) lasObjects;
  }

  <T extends BaseEntity, ID extends Serializable> List<T> find(ObjectId appId, String className,  LASPrincipal principal, LASQuery query, Class<T> clazz) {
    long start = System.currentTimeMillis();

    LASClassSchema lasClassSchema = classSchemaManager.get(appId, className);
    if (lasClassSchema == null) {
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("Schema isn't exists[appId: {}, className: {}]", appId, className);
      }
      return new ArrayList<>();
    }

    long getSchema = System.currentTimeMillis();

    String db = lasClassSchema.getDbName();
    String table = lasClassSchema.getCollectionName();
    AppFindRequest request = new AppFindRequest(getMgoDatabase(db), lasClassSchema, query, principal);
    FilterChain filterChain = filterChainManager.getFilterChain(appId, className);
    filterChain.doFilter(request);

    long handleFilter = System.currentTimeMillis();

    lasOperatorHandler.handleLASOperator(lasClassSchema, principal, query.getQuery());

    List lists = mongoDao.find(db, table, query, clazz);
    if (lists.isEmpty()) {
      return lists;
    }

    FindManyMessage response = new FindManyMessage(request, lists);
    filterChain.doFilter(response);

    if (request.getLasIncludes() != null && Map.class.isAssignableFrom(clazz)) {
      lasOperatorHandler.include(request, principal, request.getLasIncludes(), response.getResults());
    }

    long find = System.currentTimeMillis();

    if (find - start > 100) {
      LOGGER.warn("[lasdata-find] appId: {}, className: {}, query: {}, getSchema: {}, handleFilter: {}, find: {}, total: {}",
          appId, className, query, getSchema - start, handleFilter - getSchema, find - handleFilter, find - start);
    }

    return response.getResults();
  }

  @Override
  public long count(ObjectId appId, String className, LASPrincipal principal) {
    long start = System.currentTimeMillis();

    LASClassSchema lasClassSchema = classSchemaManager.get(appId, className);
    if (lasClassSchema == null) {
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("Schema isn't exists[appId: {}, className: {}]", appId, className);
      }
      return 0;
    }

    long getSchema = System.currentTimeMillis();

    String db = lasClassSchema.getDbName();
    String table = lasClassSchema.getCollectionName();
    AppCountRequest request = new AppCountRequest(getMgoDatabase(db), lasClassSchema, new LASQuery(), principal);
    FilterChain filterChain = filterChainManager.getFilterChain(appId, className);
    filterChain.doFilter(request);

    long handleFilter = System.currentTimeMillis();

    long c = mongoDao.count(db, table);

    long count = System.currentTimeMillis();

    if (count - start > 100) {
      LOGGER.warn("[lasdata-count] appId: {}, className: {}, getSchema: {}, handleFilter: {}, count: {}, total: {}",
          appId, className, getSchema - start, handleFilter - getSchema, count - handleFilter, count - start);
    }
    return c;
  }

  @Override
  public long count(ObjectId appId, String className,  LASPrincipal principal, LASQuery query) {
    long start = System.currentTimeMillis();

    LASClassSchema lasClassSchema = classSchemaManager.get(appId, className);
    if (lasClassSchema == null) {
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("Schema isn't exists[appId: {}, className: {}]", appId, className);
      }
       return 0;
    }

    long getSchema = System.currentTimeMillis();

    String db = lasClassSchema.getDbName();
    String table = lasClassSchema.getCollectionName();
    AppCountRequest request = new AppCountRequest(getMgoDatabase(db), lasClassSchema, query, principal);
    FilterChain filterChain = filterChainManager.getFilterChain(appId, className);
    filterChain.doFilter(request);

    long handleFilter = System.currentTimeMillis();

    lasOperatorHandler.handleLASOperator(lasClassSchema, principal, query.getQuery());

    long c = mongoDao.count(db, table, query);

    long count = System.currentTimeMillis();

    if (count - start > 100) {
      LOGGER.warn("[lasdata-count] appId: {}, className: {}, query: {}, getSchema: {}, handleFilter: {}, count: {}, total: {}",
          appId, className, query, getSchema - start, handleFilter - getSchema, count - handleFilter, count - start);
    }

    return c;
  }

  MgoDatabase getMgoDatabase(String db) {
    MgoDatabase mgoDatabase = mgoDatabaseVisitor.get(db);
    if (mgoDatabase.getStatus() != DataSourceStatus.ENABLE && mgoDatabase.getMgoCluster().getStatus() != DataSourceStatus.ENABLE) {
      throw new IllegalArgumentException("DataSource is not available, status: " + mgoDatabase.getStatus() + ", db: " + db);
    }
    return mgoDatabase;
  }

  private LASClassSchema createIfPermit(LASPrincipal principal, ObjectId appId, String className) {
    if (principal!=null && principal.getPermissions().contains(PermissionType.ORG_ADMIN)) {
      return createLASClassSchema(appId, className);
    } else {
      LASObject app = mongoDao.get("platform_data", "zcloud_application", appId, LASObject.class);
      Object autoClassCreate = app.get("autoClassCreate");
      if (app != null && autoClassCreate != null && (Boolean) autoClassCreate || className.toLowerCase().startsWith("_sys_")) {
        return createLASClassSchema(appId, className);
      } else {
        throw new LASDataException(401, "Class: " + className + " not exists.");
      }
    }
  }

  private LASClassSchema createLASClassSchema(ObjectId appId, String className) {
    lock.lock();
    try {
      LASClassSchema exists = classSchemaManager.get(appId, className);
      if (exists != null) {
        return exists;
      }
      LASClassSchema lasClassSchema = new LASClassSchema();
      lasClassSchema.setAppId(appId);
      lasClassSchema.setClassName(className);
      long ts = System.currentTimeMillis();
      lasClassSchema.setUpdatedAt(ts);
      lasClassSchema.setCreatedAt(ts);
      lasClassSchema.setDbName(DbAndTableMapper.getDB(appId));
      lasClassSchema.setCollectionName(DbAndTableMapper.getTable(appId, className));
      return classSchemaManager.create(lasClassSchema);
    } finally {
      lock.unlock();
    }
  }

}