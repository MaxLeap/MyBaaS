package com.maxleap.cloudcode.job.impl;

import com.maxleap.cloudcode.job.domain._SYS_Task;
import com.maxleap.domain.base.LASObject;
import com.maxleap.domain.base.ObjectId;
import com.maxleap.pandora.core.lasdata.LASQuery;
import com.maxleap.pandora.core.utils.LASObjectJsons;
import com.maxleap.pandora.data.support.LASDataEntityManager;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * User：poplar
 * Date：15-6-16
 */
@Singleton
public class TaskAcquirer {

  private final static Logger logger = LoggerFactory.getLogger(TaskDeliver.class);

  private LASDataEntityManager lasDataEntityManager;

  private TaskDeliver taskDeliver;

  @Inject
  public TaskAcquirer(LASDataEntityManager lasDataEntityManager, TaskDeliver taskDeliver) {
    this.lasDataEntityManager = lasDataEntityManager;
    this.taskDeliver = taskDeliver;
  }

  public void acquire(String appId) {
    getAppTasks(appId,new AtomicInteger());
  }

  private void getAppTasks(String appId,AtomicInteger skip) {
    LASQuery query = new LASQuery();
//    query.equalTo("active", true);
    query.setLimit(101);
    query.setSkip(skip.get());
    List<LASObject> result = lasDataEntityManager.find(new ObjectId(appId), _SYS_Task.class.getSimpleName(), null, query);
    if (result != null && result.size() > 0) {
      if (result.size() == 101) {
        skip.addAndGet(100);
        result.remove(100);
        getAppTasks(appId, skip);
      }

      for (LASObject object : result) {
        _SYS_Task task = LASObjectJsons.deserialize(LASObjectJsons.serialize(object), _SYS_Task.class);
        taskDeliver.startTaskTimer(task);
      }
    }
  }
}