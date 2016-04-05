package com.maxleap.pandora.config;

import com.maxleap.pandora.config.mgo.MgoClusterVisitor;
import com.maxleap.pandora.config.mgo.MgoDatabaseVisitor;
import com.google.inject.Binder;
import com.google.inject.Module;

/**
 * @author sneaky
 * @since 3.0.0
 */
public class ConfigModule implements Module {
  @Override
  public void configure(Binder binder) {
    binder.bind(ClusterVisitor.class).to(MgoClusterVisitor.class);
    binder.bind(DatabaseVisitor.class).to(MgoDatabaseVisitor.class);
  }
}
