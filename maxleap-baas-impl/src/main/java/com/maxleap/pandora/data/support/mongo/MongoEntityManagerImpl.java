package com.maxleap.pandora.data.support.mongo;

import com.maxleap.domain.mongo.BaseEntity;
import com.maxleap.pandora.config.DataSourceStatus;
import com.maxleap.pandora.config.DatabaseVisitor;
import com.maxleap.pandora.config.mgo.MgoDatabase;
import com.maxleap.pandora.core.mongo.MongoQuery;
import com.maxleap.pandora.core.mongo.MongoUpdate;
import com.maxleap.pandora.data.support.MongoEntityManager;
import com.maxleap.pandora.data.support.filter.support.FilterChain;
import com.maxleap.pandora.data.support.filter.support.MgoFilterChainManager;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Iterator;
import java.util.List;

/**
 * @author sneaky
 * @since 3.0.0
 */
@Singleton
public class MongoEntityManagerImpl implements MongoEntityManager {
  private static final Logger LOGGER = LoggerFactory.getLogger(MongoEntityManagerImpl.class);

  private MongoDao mongoDao;
  private DatabaseVisitor<MgoDatabase> mgoDatabaseVisitor;
  private MgoFilterChainManager filterChainManager;

  @Inject
  public MongoEntityManagerImpl(DatabaseVisitor mgoDatabaseVisitor,
                                MgoFilterChainManager filterChainManager,
                                MongoDao mongoDao) {
    this.mongoDao = mongoDao;
    this.mgoDatabaseVisitor = mgoDatabaseVisitor;
    this.filterChainManager = filterChainManager;
  }

  @Override
  public <T extends BaseEntity<ID>, ID> T create(String db, String table, T entity) {
    long start = System.currentTimeMillis();

    CreateRequest request = new CreateRequest(getMgoDatabase(db), db, table, entity);
    FilterChain filterChain = filterChainManager.getFilterChain(db, table);
    filterChain.doFilter(request);
    entity = mongoDao.create(db, table, entity);

    long createTime = System.currentTimeMillis();

    if (createTime - start > 100) {
      LOGGER.warn("[lasdata-create] db: {}, table: {}, time: {}", db, table, createTime - start);
    }

    return entity;
  }

  @Override
  public <T extends BaseEntity<ID>, ID> void create(String db, String table, List<T> entities) {
    long start = System.currentTimeMillis();

    CreateManyRequest request = new CreateManyRequest(getMgoDatabase(db), db, table, entities);
    FilterChain filterChain = filterChainManager.getFilterChain(db, table);
    filterChain.doFilter(request);
    mongoDao.create(db, table, entities);

    long create = System.currentTimeMillis();

    if (create - start > 100) {
      LOGGER.warn("[lasdata-createMany] db: {}, table: {}, time: {}", db, table, create - start);
    }


  }

  @Override
  public <ID> int delete(String db, String table, ID id) {
    long start = System.currentTimeMillis();

    DeleteRequest request = new DeleteRequest(getMgoDatabase(db), db, table, id);
    FilterChain filterChain = filterChainManager.getFilterChain(db, table);
    filterChain.doFilter(request);
    int delete = mongoDao.delete(db, table, id);

    long deleteById = System.currentTimeMillis();

    if (deleteById - start > 100) {
      LOGGER.warn("[lasdata-deleteById] db: {}, table: {}, create: {}", db, table, deleteById - start);
    }

    return delete;
  }

  @Override
  public int delete(String db, String table, MongoQuery query) {
    long start = System.currentTimeMillis();

    DeleteRequest request = new DeleteRequest(getMgoDatabase(db), db, table, query);
    FilterChain filterChain = filterChainManager.getFilterChain(db, table);
    filterChain.doFilter(request);
    int d = mongoDao.delete(db, table, query);

    long delete = System.currentTimeMillis();

    if (delete - start > 100) {
      LOGGER.warn("[lasdata-delete] db: {}, table: {}, query: {}, time: {}", db, table, query, delete - start);
    }

    return d;
  }

  @Override
  public <T extends BaseEntity<ID>, ID> T get(String db, String table, ID id, Class<T> clazz) {
    long start = System.currentTimeMillis();

    FindRequest request = new FindRequest(getMgoDatabase(db), db, table, id);
    FilterChain filterChain = filterChainManager.getFilterChain(db, table);
    filterChain.doFilter(request);
    T t = mongoDao.get(db, table, id, clazz);
    FindOneMessage findOneMessage = new FindOneMessage(request, t);
    filterChain.doFilter(findOneMessage);

    long getById = System.currentTimeMillis();

    if (getById - start > 100) {
      LOGGER.warn("[lasdata-getById] db: {}, table: {}, id: {}, time: {}", db, table, id, getById - start);
    }

    return t;
  }

  @Override
  public <T extends BaseEntity<ID>, ID> List<T> getAll(String db, String table, Class<T> clazz) {
    long start = System.currentTimeMillis();

    FindRequest request = new FindRequest(getMgoDatabase(db), db, table, null);
    FilterChain filterChain = filterChainManager.getFilterChain(db, table);
    filterChain.doFilter(request);
    List<T> all = mongoDao.getAll(db, table, clazz);
    FindManyMessage response = new FindManyMessage(request, all);
    filterChain.doFilter(response);

    long getAll = System.currentTimeMillis();

    if (getAll - start > 100) {
      LOGGER.warn("[lasdata-getAll] db: {}, table: {}, time: {}", db, table, getAll - start);
    }

    return all;
  }

  @Override
  public <ID, Result> Result update(String db, String table, ID id, MongoUpdate update) {
    long start = System.currentTimeMillis();

    UpdateRequest request = new UpdateRequest(getMgoDatabase(db), db, table, id, update);
    FilterChain filterChain = filterChainManager.getFilterChain(db, table);
    filterChain.doFilter(request);
    Result u = mongoDao.update(db, table, id, update);

    long updateById = System.currentTimeMillis();

    if (updateById - start > 100) {
      LOGGER.warn("[lasdata-updateById] db: {}, table: {}, id: {}, time: {}", db, table, id, updateById - start);
    }

    return u;
  }

  @Override
  public <Result> Result update(String db, String table, MongoQuery query, MongoUpdate update) {
    long start = System.currentTimeMillis();

    UpdateRequest request = new UpdateRequest(getMgoDatabase(db), db, table, query, update);
    FilterChain filterChain = filterChainManager.getFilterChain(db, table);
    filterChain.doFilter(request);
    Result u = mongoDao.update(db, table, query, update);

    long updateTime = System.currentTimeMillis();

    if (updateTime - start > 100) {
      LOGGER.warn("[lasdata-update] db: {}, table: {}, query: {}, time: {}", db, table, query, updateTime - start);
    }

    return u;
  }

  @Override
  public <T extends BaseEntity<ID>, ID> T findUniqueOne(String db, String table, MongoQuery query, Class<T> clazz) {
    long start = System.currentTimeMillis();

    FindRequest request = new FindRequest(getMgoDatabase(db), db, table, query);
    FilterChain filterChain = filterChainManager.getFilterChain(db, table);
    filterChain.doFilter(request);
    T uniqueOne = mongoDao.findUniqueOne(db, table, query, clazz);
    FindOneMessage response = new FindOneMessage(request, uniqueOne);
    filterChain.doFilter(response);
    T result = (T) response.getResult();

    long findByUniqueOne = System.currentTimeMillis();

    if (findByUniqueOne - start > 100) {
      LOGGER.warn("[lasdata-findByUniqueOne] db: {}, table: {}, query: {}, time: {}", db, table, query, findByUniqueOne - start);
    }

    return result;
  }

  @Override
  public <T extends BaseEntity<ID>, ID> List<T> find(String db, String table, MongoQuery query, Class<T> clazz) {
    long start = System.currentTimeMillis();

    FindRequest request = new FindRequest(getMgoDatabase(db), db, table, null);
    FilterChain filterChain = filterChainManager.getFilterChain(db, table);
    filterChain.doFilter(request);
    List<T> many = mongoDao.find(db, table, query, clazz);
    FindManyMessage response = new FindManyMessage(request, many);
    filterChain.doFilter(response);

    long find = System.currentTimeMillis();

    if (find - start > 100) {
      LOGGER.warn("[lasdata-find] db: {}, table: {}, query: {}, time: {}", db, table, query, find - start);
    }

    return many;
  }

  @Override
  public <Entity extends BaseEntity<ID>, ID> Iterator<Entity> findIterator(String db, String table, MongoQuery mongoQuery, Class<Entity> clazz) {
    FindRequest request = new FindRequest(getMgoDatabase(db), db, table, mongoQuery);
    FilterChain filterChain = filterChainManager.getFilterChain(db, table);
    filterChain.doFilter(request);
    return mongoDao.findIterator(db, table, mongoQuery, clazz);
  }

  @Override
  public long count(String db, String table) {
    long start = System.currentTimeMillis();

    CountRequest request = new CountRequest(getMgoDatabase(db), db, table, new MongoQuery());
    filterChainManager.getFilterChain(db, table).doFilter(request);
    long count = mongoDao.count(db, table);

    long countTime = System.currentTimeMillis();

    if (countTime - start > 100) {
      LOGGER.warn("[lasdata-countAll] db: {}, table: {}, time: {}", db, table, countTime - start);
    }

    return count;
  }

  @Override
  public long count(String db, String table, MongoQuery query) {
    long start = System.currentTimeMillis();

    CountRequest request = new CountRequest(getMgoDatabase(db), db, table, query);
    filterChainManager.getFilterChain(db, table).doFilter(request);
    long count = mongoDao.count(db, table, query);

    long countTime = System.currentTimeMillis();

    if (countTime - start > 100) {
      LOGGER.warn("[lasdata-count] db: {}, table: {}, query: {}, time: {}", db, table, query, countTime - start);
    }

    return count;
  }

  @Override
  public MongoDatabase getMongoDatabase(String db) {
    return mongoDao.getMongoDatabase(db);
  }

  @Override
  public MongoCollection getMongoCollection(String db, String table) {
    return mongoDao.getMongoCollection(db, table);
  }

  MgoDatabase getMgoDatabase(String db) {
    MgoDatabase mgoDatabase = mgoDatabaseVisitor.get(db);
    if (mgoDatabase.getStatus() != DataSourceStatus.ENABLE && mgoDatabase.getMgoCluster().getStatus() != DataSourceStatus.ENABLE) {
      throw new IllegalArgumentException("DataSource is not available, status: " + mgoDatabase.getStatus() + ", db: " + db);
    }
    return mgoDatabase;
  }
}