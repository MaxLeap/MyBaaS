package com.maxleap.pandora.data.support;

import com.maxleap.domain.base.LASObject;
import com.maxleap.domain.mongo.BaseEntity;
import com.maxleap.pandora.core.mongo.MongoQuery;
import com.maxleap.pandora.core.mongo.MongoUpdate;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author sneaky
 * @since 3.0.0
 */
public interface MongoEntityManager {

  <Entity extends BaseEntity<ID>, ID> Entity create(String db, String table, Entity entity);

  <Entity extends BaseEntity<ID>, ID> void create(String db, String table, List<Entity> entities);

  <ID> int delete(String db, String table, ID id);

  int delete(String db, String table, MongoQuery query);

  <Entity extends BaseEntity<ID>, ID> Entity get(String db, String table, ID id, Class<Entity> clazz);

  <Entity extends BaseEntity<ID>, ID> List<Entity> getAll(String db, String table, Class<Entity> clazz);

  <Entity extends BaseEntity<ID>, ID> Iterator<Entity> findIterator(String db, String table, MongoQuery mongoQuery, Class<Entity> clazz);

  <Entity extends BaseEntity<ID>, ID> Entity findUniqueOne(String db, String table, MongoQuery query, Class<Entity> clazz);

  <Entity extends BaseEntity<ID>, ID> List<Entity> find(String db, String table, MongoQuery query, Class<Entity> clazz);

  <ID, Result> Result update(String db, String table, ID id, MongoUpdate update);

  <Result> Result update(String db, String table, MongoQuery query, MongoUpdate update);

  long count(String db, String table);

  long count(String db, String table, MongoQuery query);

  MongoDatabase getMongoDatabase(String db);

  MongoCollection getMongoCollection(String db, String table);

  class LasDataIterator<T> implements Iterator<T> {
    Iterator iterator;
    Class<T> clazz;

    public LasDataIterator(Iterator iterator, Class<T> clazz) {
      this.iterator = iterator;
      this.clazz = clazz;
    }

    @Override
    public boolean hasNext() {
      return iterator.hasNext();
    }

    @Override
    public T next() {
      Object next = iterator.next();
      if (next instanceof Document) {
        Document document = (Document) next;
        if (clazz == LASObject.class || Map.class.isAssignableFrom(clazz)) {
          LasSunObjectIdMapper.toLasSunObjectId(document);
          return (T) new LASObject(document);
        } else {
          LasSunObjectIdMapper.toObjectId(document);
          return MongoJsons.deserialize(MongoJsons.serialize(document), clazz);
        }
      } else {
        throw new IllegalArgumentException("Unsupported!!");
      }
    }
  }
}