package com.maxleap.pandora.data.support.mongo;

import com.maxleap.domain.base.LASObject;
import com.maxleap.domain.base.ObjectId;
import com.maxleap.domain.mongo.BaseEntity;
import com.maxleap.pandora.config.DataSourceStatus;
import com.maxleap.pandora.config.DatabaseVisitor;
import com.maxleap.pandora.config.mgo.MgoDatabase;
import com.maxleap.pandora.core.mongo.MongoQuery;
import com.maxleap.pandora.core.mongo.MongoQueryOptions;
import com.maxleap.pandora.core.mongo.MongoUpdate;
import com.maxleap.pandora.core.mongo.exception.MongoDataAccessException;
import com.maxleap.pandora.core.utils.Assertions;
import com.maxleap.pandora.core.utils.DateUtils;
import com.maxleap.pandora.data.support.LasSunObjectIdMapper;
import com.maxleap.pandora.data.support.MongoEntityManager;
import com.maxleap.pandora.data.support.MongoJsons;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.CountOptions;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * @author sneaky
 * @since 3.0.0
 */
@Singleton
public class MongoDao {
  private static final Logger LOGGER = LoggerFactory.getLogger(MongoDao.class);
  /**
   * If query has not limit parameter.
   */
  private static final int DEFAULT_LIST_SIZE = 100;
  private static final int MAX_LIST_SIZE = 2000;

  private DatabaseVisitor<MgoDatabase> mgoDatabaseVisitor;
  private MongoClientFactory mongoClientFactory;

  @Inject
  public MongoDao(DatabaseVisitor mgoDatabaseVisitor,
                  MongoClientFactory mongoClientFactory) {
    this.mgoDatabaseVisitor = mgoDatabaseVisitor;
    this.mongoClientFactory = mongoClientFactory;
  }

  public <T extends BaseEntity<ID>, ID> T create(String db, String table, T entity) {
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("[create][db: " + db + "][table: " + table + "]");
    }
    Assertions.notBlank("db", db);
    Assertions.notBlank("table", table);
    Assertions.notNull("entity", entity);
    Document document = toDocument(entity);
    getMongoCollection(db, table).insertOne(document);
    Object id = document.get("_id");
    if (id instanceof org.bson.types.ObjectId) {
      ((BaseEntity) entity).setObjectId(new ObjectId(((org.bson.types.ObjectId) id).toHexString()));
    } else {
      ((BaseEntity) entity).setObjectId(id);
    }
    return entity;
  }

  public <T extends BaseEntity<ID>, ID> void create(String db, String table, List<T> entities) {
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("[create, size: " + entities.size() + "][db: " + db + "][table: " + table + "]");
    }
    Assertions.notBlank("db", db);
    Assertions.notBlank("table", table);
    Assertions.notNull("entities", entities);
    List<Document> documents = toDocument(entities);
    getMongoCollection(db, table).insertMany(documents);
  }

  public <ID> int delete(String db, String table, ID id) {
    Assertions.notBlank("db", db);
    Assertions.notBlank("table", table);
    Assertions.notNull("id", id);
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("[deleteById: " + id + "][db: " + db + "][table: " + table + "]");
    }
    if (id instanceof ObjectId) {
      id = (ID) new org.bson.types.ObjectId(((ObjectId) id).toHexString());
    }
    return (int) getMongoCollection(db, table).deleteOne(new Document("_id", id)).getDeletedCount();
  }

  public int delete(String db, String table, MongoQuery query) {
    Assertions.notBlank("db", db);
    Assertions.notBlank("table", table);
    Assertions.notNull("query", query);
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("[delete: " + query + "][db: " + db + "][table: " + table + "]");
    }
    LasSunObjectIdMapper.toMongoObjectIdInMongoQuery(query.getQuery());
    DeleteResult deleteResult = getMongoCollection(db, table).deleteMany(toDocument(query.getQuery()));
    return (int) deleteResult.getDeletedCount();
  }

  public <T extends BaseEntity<ID>, ID> T get(String db, String table, ID id, Class<T> clazz) {
    Assertions.notBlank("db", db);
    Assertions.notBlank("table", table);
    Assertions.notNull("id", id);
    Assertions.notNull("clazz", clazz);
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("[getById: " + id + "][db: " + db + "][table: " + table + "]");
    }

    if (id instanceof ObjectId) {
      id = (ID) new org.bson.types.ObjectId(((ObjectId) id).toHexString());
    }
    Iterator<Document> iterable = getMongoCollection(db, table).find(new Document().append("_id", id)).maxTime(1, TimeUnit.SECONDS).iterator();
    if (iterable.hasNext()) {
      return (T) adjust(iterable.next(), clazz);
    } else {
      return null;
    }
  }

  public <T extends BaseEntity<ID>, ID> List<T> getAll(String db, String table, Class<T> clazz) {
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("[getAll][db: " + db + "][table: " + table + "]");
    }
    Assertions.notBlank("db", db);
    Assertions.notBlank("table", table);
    Assertions.notNull("clazz", clazz);
    FindIterable<Document> iterable = getMongoCollection(db, table).find().limit(200000).maxTime(5, TimeUnit.SECONDS);
    return extract(iterable, clazz);
  }

  public <ID, Result> Result update(String db, String table, ID id, MongoUpdate update) {
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("[updateById: " + id + "][db: " + db + "][table: " + table + "]");
    }
    MongoQuery mongoQuery = new MongoQuery().equalTo("_id", id);
    return update(db, table, mongoQuery, update);
  }

  public <Result> Result update(String db, String table, MongoQuery query, MongoUpdate update) {
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("[update: " + query + "][db: " + db + "][table: " + table + "]");
    }
    Assertions.notBlank("db", db);
    Assertions.notBlank("table", table);
    Assertions.notNull("query", query);
    Assertions.notNull("update", update);
    LasSunObjectIdMapper.toMongoObjectIdInMongoQuery(query.getQuery());
    LasSunObjectIdMapper.toMongoObjectId(update.getModifierOps());
    if (update.isUpsert()) {
      UpdateResult updateResult = getMongoCollection(db, table).updateMany(toDocument(query.getQuery()), toDocument(update.getModifierOps()), new UpdateOptions().upsert(true));
      BsonValue upsertedId = updateResult.getUpsertedId();
      if (upsertedId != null) {
        if (upsertedId instanceof BsonObjectId) {
          org.bson.types.ObjectId value = ((BsonObjectId) upsertedId).getValue();
          return (Result) new ObjectId(value.toHexString());
        } else if (upsertedId instanceof BsonString) {
          return (Result) ((BsonString) upsertedId).getValue();
        } else if (upsertedId instanceof BsonInt64) {
          return (Result) (Long) ((BsonInt64) upsertedId).getValue();
        } else {
          throw new MongoDataAccessException("Id Type not supported! type: " + upsertedId.getBsonType().name());
        }
      } else {
        return (Result) (Integer) (int) updateResult.getModifiedCount();
      }

    } else {
      UpdateResult updateResult = getMongoCollection(db, table).updateMany(toDocument(query.getQuery()), toDocument(update.getModifierOps()));
      return (Result) (Integer) (int) updateResult.getModifiedCount();
    }
  }

  public <T extends BaseEntity<ID>, ID> T findUniqueOne(String db, String table, MongoQuery query, Class<T> clazz) {
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("[findUniqueOne: " + query + "][db: " + db + "][table: " + table + "]");
    }
    Assertions.notBlank("db", db);
    Assertions.notBlank("table", table);
    Assertions.notNull("query", query);
    Assertions.notNull("clazz", clazz);
    MongoCollection collection = getMongoCollection(db, table);
    LasSunObjectIdMapper.toMongoObjectIdInMongoQuery(query.getQuery());
    Map projectKeys = query.getQueryOptions().getProjectKeys();
    FindIterable findIterable = collection.find(toDocument(query.getQuery()));

    if (projectKeys != null) {
      Object objectId = projectKeys.get("objectId");
      if (objectId != null) {
        projectKeys.put("_id", objectId);
        projectKeys.remove("objectId");
      }
      findIterable.projection(toDocument(projectKeys));
    }
    findIterable.maxTime(3, TimeUnit.SECONDS);

    MongoCursor<Document> iterator = findIterable.iterator();
    T entity =  null;
    if (iterator.hasNext()) {
      entity = (T) adjust(iterator.next(), clazz);
      if (iterator.hasNext()) {
        throw new MongoDataAccessException(1, "Expected one result (or null) to be returned, but found more than one");
      }
    }
    return entity;
  }

  public <T extends BaseEntity<ID>, ID> List<T> find(String db, String table, MongoQuery query, Class<T> clazz) {
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("[find: " + query + "][db: " + db + "][table: " + table + "]");
    }
    return doQuery(db, table, query, clazz);
  }

  public <Entity extends BaseEntity<ID>, ID> Iterator<Entity> findIterator(String db, String table, MongoQuery mongoQuery, Class<Entity> clazz) {
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("[findIterator: " + mongoQuery + "][db: " + db + "][table: " + table + "]");
    }
    MongoEntityManager.LasDataIterator lasDataIterator = new MongoEntityManager.LasDataIterator(build(db, table, mongoQuery, true).iterator(), clazz);
    return lasDataIterator;
  }

  public long count(String db, String table) {
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("[count all][db: " + db + "][table: " + table + "]");
    }
    return getMongoCollection(db, table).count();
  }

  List doQuery(String db, String table, MongoQuery query, Class clazz) {
    return extract(build(db, table, query, false), clazz);
  }

  public long count(String db, String table, MongoQuery query) {
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("[count: " + query + "][db: " + db + "][table: " + table + "]");
    }
    CountOptions countOptions = new CountOptions();
    countOptions.maxTime(60, TimeUnit.SECONDS);
    LasSunObjectIdMapper.toMongoObjectIdInMongoQuery(query.getQuery());
    return getMongoCollection(db, table).count(toDocument(query.getQuery()), countOptions);
  }

  public MongoDatabase getMongoDatabase(String db) {
    return mongoClientFactory.get(getMgoDatabase(db)).getDatabase(db);
  }

  public MongoCollection getMongoCollection(String db, String table) {
    return getMongoDatabase(db).getCollection(table);
  }

  MgoDatabase getMgoDatabase(String db) {
    MgoDatabase mgoDatabase = mgoDatabaseVisitor.get(db);
    if (mgoDatabase.getStatus() != DataSourceStatus.ENABLE && mgoDatabase.getMgoCluster().getStatus() != DataSourceStatus.ENABLE) {
      throw new IllegalArgumentException("DataSource is not available, status: " + mgoDatabase.getStatus() + ", db: " + db);
    }
    return mgoDatabase;
  }

  FindIterable build(String db, String table, MongoQuery query, boolean it) {
    Assertions.notBlank("db", db);
    Assertions.notBlank("table", table);
    Assertions.notNull("query", query);
    MongoCollection collection = getMongoCollection(db, table);
    LasSunObjectIdMapper.toMongoObjectIdInMongoQuery(query.getQuery());
    MongoQueryOptions queryOptions = query.getQueryOptions();
    FindIterable findIterable = collection.find(toDocument(query.getQuery()));

    Map projectKeys = queryOptions.getProjectKeys();
    if (projectKeys != null) {
      Object objectId = projectKeys.get("objectId");
      if (objectId != null) {
        projectKeys.put("_id", objectId);
        projectKeys.remove("objectId");
      }
      findIterable.projection(toDocument(projectKeys));
    }
    Map sorts = queryOptions.getSorts();
    if (sorts != null) {
      Object objectId = sorts.get("objectId");
      if (objectId != null) {
        sorts.put("_id", objectId);
        sorts.remove("objectId");
      }
      findIterable.sort(toDocument(sorts));
    }
    if(queryOptions.getSkip() > 0) {
      findIterable.skip(queryOptions.getSkip());
    }
    if (!it) {
      int limit = queryOptions.getLimit();
      if (limit > 0 && limit <= 2000) {
        findIterable.limit(limit);
      } else if (limit > 2000) {
        throw new IllegalArgumentException("Query max limit is: " + 2000 + " db: " + db + " table: " + table);
      } else if (limit <= 0) {
        findIterable.limit(100);
      }
    }
    findIterable.maxTime(3L, TimeUnit.SECONDS);

    return findIterable;
  }

  <T> Document toDocument(T entity) {
    if (entity instanceof Document) {
      LasSunObjectIdMapper.toMongoObjectId((Map) entity);
      return (Document) entity;
    } else if (entity instanceof Map) {
      LasSunObjectIdMapper.toMongoObjectId((Map) entity);
      return new Document((Map) entity);
    } else {
      BaseEntity doc = (BaseEntity) entity;
      long createdAt = doc.getCreatedAt();
      long updatedAt = doc.getUpdatedAt();
      Document parse = Document.parse(MongoJsons.serializeMongo(entity));
      parse.put("createdAt", createdAt);
      parse.put("updatedAt", updatedAt);
      LasSunObjectIdMapper.to_id(parse);
      return parse;
    }
  }

  <T> List<Document> toDocument(List<T> entities) {
    T t = entities.get(0);
    if (t instanceof Document) {
      return (List<Document>) entities;
    } else {
      List<Document> documents = new ArrayList<>(entities.size());
      for (T t1 : entities) {
        documents.add(toDocument(t1));
      }
      return documents;
    }
  }

  <T> List<T> extract(FindIterable<Document> findIterable, Class entityClass) {
    List entities = new ArrayList<>();
    findIterable.forEach(new Consumer<Document>() {
      @Override
      public void accept(Document document) {
        entities.add(adjust(document, entityClass));
      }
    });
    return entities;
  }

  public Object adjust(Document document, Class entityClass) {
    if (entityClass == LASObject.class) {
      LasSunObjectIdMapper.toLasSunObjectId(document);
      return new LASObject(document);
    } else if (Map.class.isAssignableFrom(entityClass)) {
      LasSunObjectIdMapper.toLasSunObjectId(document);
      try {
        return entityClass.getConstructor(Map.class).newInstance(document);
      } catch (Exception e) {
        throw new IllegalArgumentException(e);
      }
    } else {
      LasSunObjectIdMapper.toObjectId(document);
      Object createdAt = document.get("createdAt");
      if (createdAt instanceof Long) {
        document.put("createdAt", DateUtils.encodeDate(new Date((Long) createdAt)));
      }
      Object updatedAt = document.get("updatedAt");
      if (updatedAt instanceof Long) {
        document.put("updatedAt", DateUtils.encodeDate(new Date((Long) updatedAt)));
      }
      return MongoJsons.deserialize(MongoJsons.serialize(document), entityClass);
    }
  }

  Document toDocument(Map doc) {
    return new Document(doc);
  }
}