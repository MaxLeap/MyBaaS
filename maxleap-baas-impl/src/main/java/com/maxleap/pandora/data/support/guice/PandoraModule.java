package com.maxleap.pandora.data.support.guice;

import com.maxleap.pandora.config.ConfigModule;
import com.maxleap.pandora.data.support.ClassSchemaManager;
import com.maxleap.pandora.data.support.LASDataEntityManager;
import com.maxleap.pandora.data.support.filter.*;
import com.maxleap.pandora.data.support.filter.support.DefaultLASDataFilterChainManager;
import com.maxleap.pandora.data.support.filter.support.DefaultMgoFilterChainManager;
import com.maxleap.pandora.data.support.filter.support.LASDataFilterChainManager;
import com.maxleap.pandora.data.support.filter.support.MgoFilterChainManager;
import com.maxleap.pandora.data.support.lasdata.*;
import com.maxleap.pandora.data.support.mongo.MongoClientFactory;
import com.maxleap.pandora.data.support.mongo.MongoDao;
import com.maxleap.pandora.data.support.MongoEntityManager;
import com.maxleap.pandora.data.support.mongo.MongoEntityManagerImpl;
import com.google.inject.Binder;
import com.google.inject.Module;

/**
 * @author sneay
 * @since 3.0.0
 */
public class PandoraModule implements Module {
  @Override
  public void configure(Binder binder) {
    binder.install(new ConfigModule());

    binder.bind(DataDecorator.class);
    binder.bind(ClassSchemaManager.class).to(ClassSchemaManagerImpl.class);
    binder.bind(MongoClientFactory.class);

    binder.bind(ACLFilter.class);
    binder.bind(AppDataConvertFilter.class);
    binder.bind(ClassPermissionFilter.class);
    binder.bind(ClassSchemaAdjustFilter.class);
    binder.bind(ClassSchemaAdjuster.class);
    binder.bind(DataDecoratorFilter.class);
    binder.bind(DataValidateFilter.class);
    binder.bind(NativeDataDecoratorFilter.class);
    binder.bind(SchemaBindToFilter.class);
    binder.bind(TimestampFilter.class);
    binder.bind(MgoFilterChainManager.class).to(DefaultMgoFilterChainManager.class);
    binder.bind(MongoDao.class);
    binder.bind(LASDataFilterChainManager.class).to(DefaultLASDataFilterChainManager.class);
    binder.bind(MongoEntityManager.class).to(MongoEntityManagerImpl.class);
    binder.bind(LASDataEntityManager.class).to(LASDataEntityManagerImpl.class);

    binder.bind(LASOperatorHandler.class);
  }
}
