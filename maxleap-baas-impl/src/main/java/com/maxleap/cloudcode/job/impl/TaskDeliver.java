package com.maxleap.cloudcode.job.impl;

import com.maxleap.cloudcode.CloudCodeRestClient;
import com.maxleap.cloudcode.job.domain._SYS_JobLog;
import com.maxleap.cloudcode.job.domain._SYS_Task;
import com.maxleap.cloudcode.job.enums.JobStatus;
import com.maxleap.cloudcode.utils.ZJsonParser;
import com.maxleap.domain.base.LASObject;
import com.maxleap.domain.base.ObjectId;
import com.maxleap.exception.LASException;
import com.maxleap.pandora.core.lasdata.LASUpdate;
import com.maxleap.pandora.data.support.LASDataEntityManager;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.LocalMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 任务实现者，包括启动任务，更新任务状态，添加/更新任务日志
 * User：poplar
 * Date：15-6-9
 */
@Singleton
public class TaskDeliver {

  private static final Logger LOGGER = LoggerFactory.getLogger(TaskDeliver.class);

  private Vertx vertx;

  private LASDataEntityManager lasDataEntityManager;
  private CloudCodeRestClient cloudCodeRestClient;

  private LocalMap<String, Long> taskTimer;

  @Inject
  public TaskDeliver(Vertx vertx, LASDataEntityManager lasDataEntityManager, CloudCodeRestClient cloudCodeRestClient) {
    this.vertx = vertx;
    this.lasDataEntityManager = lasDataEntityManager;
    this.cloudCodeRestClient = cloudCodeRestClient;
    taskTimer = vertx.sharedData().getLocalMap("taskTimer");
  }

  public LocalMap<String,Long> getTaskTimer(){
    return taskTimer;
  }

  public Long startTaskTimer(_SYS_Task task) {
    if (!task.isActive()) return Long.valueOf(-1);//过滤非活跃任务
    if (task.getExecuteTime() > System.currentTimeMillis()) {//未过期任务直接启动定时器
      return setupTaskTimer(task);
    } else {
      if (task.getInterval() > 0) {//过期的周期任务需要重新计算下一次执行时间
        long newExecuteTime = getNewExecuteTime(task.getExecuteTime(), task.getInterval());
        task.setExecuteTime(newExecuteTime);
        return setupTaskTimer(task);
      } else {//过期的一次性任务属于非活跃任务，更新状态后过滤掉
        updateTaskActive(task.getApplicationId(), task.objectId());
        return Long.valueOf(-1);
      }
    }
  }

  public Long runTask(_SYS_Task task) {
    //立即执行任务
    deliver(task);
    //添加周期定时器
    if (task.getInterval() > 0) {//重复任务
      LOGGER.debug("repeat begin to execute task[" + task.getName() + "] after " + task.getInterval() / 1000 + " seconds");
      return vertx.setPeriodic(task.getInterval(), periodId -> {//首次执行完后启动周期定时器
        task.setExecuteTime(System.currentTimeMillis() + task.getInterval());
        deliver(task);
      });
    }
    return Long.valueOf(-1);
  }

  //启动task定时器
  public Long setupTaskTimer(_SYS_Task task) {
    LOGGER.debug("begin to execute task[" + task.getName() + "] after " + (task.getExecuteTime() - System.currentTimeMillis()) / 1000 + " seconds");
    long intervalTime = task.getExecuteTime() - System.currentTimeMillis();
    if (intervalTime < 10) {//时间间隔过少会无法启动定时器，这里立即执行然后启动定时任务
      deliver(task);
      if (task.getInterval() > 0) {//重复任务
        LOGGER.debug("repeat begin to execute task[" + task.getName() + "] after " + task.getInterval() / 1000 + " seconds");
        long timeId = vertx.setPeriodic(task.getInterval(), periodId -> {//首次执行完后启动周期定时器
          task.setExecuteTime(task.getExecuteTime() + task.getInterval());
          deliver(task);
        });
        taskTimer.put(task.objectIdString(), timeId);
        return timeId;
      } else {
        return Long.valueOf(-1);
      }
    } else {
      return vertx.setTimer(intervalTime, timeId -> {
        if (task.getInterval() > 0) {//重复任务
          LOGGER.debug("repeat begin to execute task[" + task.getName() + "] after " + task.getInterval() / 1000 + " seconds");
          timeId = vertx.setPeriodic(task.getInterval(), periodId -> {//首次执行完后启动周期定时器
            task.setExecuteTime(task.getExecuteTime() + task.getInterval());
            deliver(task);
          });
          taskTimer.put(task.objectIdString(), timeId);
        }
        deliver(task);
      });
    }
  }

  //执行task
  public void deliver(_SYS_Task task) {
    _SYS_JobLog jobLog = new _SYS_JobLog();
    jobLog.setVersion(task.getVersion());
    jobLog.setTaskID(task.objectIdString());
    jobLog.setJobName(task.getName());
    jobLog.setTaskName(task.getDesc());
    jobLog.setStartTimeStamp(System.currentTimeMillis());
    jobLog.setStatus(JobStatus.RUNNING);
    LASObject lasObject = ZJsonParser.asObject(ZJsonParser.asJson(jobLog), LASObject.class);

    try {
      LASObject saveMsg = lasDataEntityManager.create(new ObjectId(task.getApplicationId()), _SYS_JobLog.class.getSimpleName(), null, lasObject);
      jobLog.setObjectId(saveMsg.getObjectId());
      String result = sendJobRequest(task.getName(), task.getParams() == null ? new JsonObject() : new JsonObject(task.getParams()));
      LASUpdate update = LASUpdate.getLASUpdate()
          .set("status", JobStatus.SUCCEEDED.name())
          .set("message", result)
          .set("endTimeStamp", System.currentTimeMillis());
      lasDataEntityManager.update(new ObjectId(task.getApplicationId()), _SYS_JobLog.class.getSimpleName(), null, jobLog.objectId(), update);
      LOGGER.debug("update jobLog status complete:" + JobStatus.SUCCEEDED);
    } catch (LASException e) {
      e.printStackTrace();
      LASUpdate update = LASUpdate.getLASUpdate()
          .set("status", JobStatus.FAILED.name())
          .set("message", e.getMessage())
          .set("endTimeStamp", System.currentTimeMillis());
      lasDataEntityManager.update(new ObjectId(task.getApplicationId()), _SYS_JobLog.class.getSimpleName(), null, jobLog.objectId(), update);
    } catch (Exception e) {
      LOGGER.error("create or update joblog fail.caused by " + e.getMessage());
    }
    if (task.getInterval() <= 0) updateTaskActive(task.getApplicationId(), task.objectId());
  }

  private void updateTaskActive(String appId, ObjectId taskId) {
    LASUpdate update = LASUpdate.getLASUpdate().set("active", false);
    lasDataEntityManager.update(new ObjectId(appId),_SYS_Task.class.getSimpleName(),null,taskId,update);
  }

  private long getNewExecuteTime(long executeTime, long interval) {
    long currentTime = System.currentTimeMillis();
    return ((currentTime - executeTime) / interval + 1) * interval + executeTime;
  }

  private String sendJobRequest(String jobName, JsonObject params) {
    return doPost("/job/" + jobName, params);
  }

  private String doPost(String uri, JsonObject params) {
    return cloudCodeRestClient.doPost(uri,params);
  }
}
