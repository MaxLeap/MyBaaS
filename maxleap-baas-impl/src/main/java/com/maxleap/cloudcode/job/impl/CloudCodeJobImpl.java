package com.maxleap.cloudcode.job.impl;

import com.maxleap.cloudcode.job.CloudCodeJob;
import com.maxleap.cloudcode.job.domain._SYS_Task;
import com.maxleap.cloudcode.utils.ZJsonParser;
import com.maxleap.domain.base.LASObject;
import com.maxleap.domain.base.ObjectId;
import com.maxleap.exception.LASException;
import com.maxleap.pandora.core.lasdata.LASQuery;
import com.maxleap.pandora.core.lasdata.LASUpdate;
import com.maxleap.pandora.core.utils.LASObjectJsons;
import com.maxleap.pandora.data.support.LASDataEntityManager;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.vertx.core.Vertx;
import io.vertx.core.shareddata.LocalMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * User：poplar
 * Date：15-6-9
 */
@Singleton
public class CloudCodeJobImpl implements CloudCodeJob {

  private static final Logger logger = LoggerFactory.getLogger(CloudCodeJobImpl.class);

  private Vertx vertx;

  private LASDataEntityManager lasDataEntityManager;

  private TaskDeliver taskDeliver;

  private LocalMap<String, Long> taskTimer;

  @Inject
  public CloudCodeJobImpl(Vertx vertx, LASDataEntityManager lasDataEntityManager, TaskDeliver taskDeliver) {
    this.vertx = vertx;
    this.lasDataEntityManager = lasDataEntityManager;
    this.taskDeliver = taskDeliver;
    taskTimer = taskDeliver.getTaskTimer();
  }

  @Override
  public LASObject addTask(_SYS_Task sysTask) {
    AtomicBoolean runNow = new AtomicBoolean(false);
    if (sysTask.getExecuteTime() == -1) {
      runNow.set(true);
      sysTask.setExecuteTime(System.currentTimeMillis());
    }
    LASObject lasObject = ZJsonParser.asObject(ZJsonParser.asJson(sysTask), LASObject.class);
    LASObject saveMsg = lasDataEntityManager.create(new ObjectId(sysTask.getApplicationId()), _SYS_Task.class.getSimpleName(), null, lasObject);
    sysTask.setObjectId(saveMsg.getObjectId());
    Long timerId = new Long(-1);
    if (runNow.get()) {//立即执行
      timerId = taskDeliver.runTask(sysTask);
    } else {
      timerId = taskDeliver.startTaskTimer(sysTask);
    }
    cacheTaskTimer(timerId, sysTask.objectIdString());
    return saveMsg;
  }

  @Override
  public Object updateTask(_SYS_Task sysTask) {
    AtomicBoolean runNow = new AtomicBoolean(false);
    if (sysTask.getExecuteTime() == -1) {
      runNow.set(true);
      sysTask.setExecuteTime(System.currentTimeMillis());
    }
    Map<String,Object> map = ZJsonParser.objectToMap(sysTask);
    if (map.containsKey("objectId")) map.remove("objectId");
    if (map.containsKey("createdAt")) map.remove("createdAt");
    if (map.containsKey("updatedAt")) map.remove("updatedAt");
    if (map.containsKey("ACL")) map.remove("ACL");
    LASUpdate update = LASUpdate.getLASUpdate().setAll(map);
    Object updateMsg = lasDataEntityManager.update(new ObjectId(sysTask.getApplicationId()), _SYS_Task.class.getSimpleName(), null, sysTask.objectId(), update);
    Long timerId = new Long(-1);
    //先取消之前的定时器
    cancelTaskTimer(sysTask.objectIdString());
    if (runNow.get()) {//立刻执行
      timerId = taskDeliver.runTask(sysTask);
    } else {
      timerId = taskDeliver.startTaskTimer(sysTask);
    }
    cacheTaskTimer(timerId, sysTask.objectIdString());
    return updateMsg;
  }

  @Override
  public Integer deleteTask(String appId, String taskId) {
    int deleteMsg = lasDataEntityManager.delete(new ObjectId(appId), _SYS_Task.class.getSimpleName(), null, new ObjectId(taskId));
    cancelTaskTimer(taskId);
    return deleteMsg;
  }

  @Override
  public Boolean runTask(String appId, String taskId) {
    _SYS_Task task = getTask(appId, taskId);
    taskDeliver.deliver(task);//立即执行，不管是否是活跃任务，对已经存在的timer不影响，也不会产生新的timer
    return Boolean.TRUE;
  }

  private _SYS_Task getTask(String appId,String taskId){
    LASQuery sunQuery = new LASQuery();
    sunQuery.equalTo("objectId", taskId);
    LASObject zCloudObject = lasDataEntityManager.findUniqueOne(new ObjectId(appId), _SYS_Task.class.getSimpleName(), null, sunQuery);
    if (zCloudObject != null) {
      return LASObjectJsons.deserialize(LASObjectJsons.serialize(zCloudObject), _SYS_Task.class);
    } else {
      throw new LASException(100401, "invalid taskId.");
    }
  }

  @Override
  public Boolean enableTask(String appId, String taskId) {
    _SYS_Task task = getTask(appId, taskId);
    if (taskTimer.keySet().contains(taskId) || (task.getExecuteTime()< System.currentTimeMillis() && task.getInterval() < 0)) {
      //本地已经启动了这个定时任务或者该任务为一次性的过期任务，则忽略掉
      return true;
    } else {
      return updateActiveAndSetup(appId, task);
    }
  }

  @Override
  public Boolean disableTask(String appId, String taskId) {
    _SYS_Task task = getTask(appId, taskId);
    cancelTaskTimer(taskId);
    if (task.isActive()) {
      //将任务状态设置为非活跃状态
      return updateActive(appId,task.objectId(),false);
    } else {
      return true;
    }
  }

  @Override
  public void startTasks(String appId, String version) {
    List<_SYS_Task> tasks = getVersionTasks(appId, version);

    if (tasks != null && tasks.size() > 0) {
      tasks.forEach(task -> {
        //本地没有该定时任务，并且该任务未过期或是周期任务则启动它
        if (!taskTimer.keySet().contains(task.objectIdString()) && (task.getExecuteTime() > System.currentTimeMillis() || task.getInterval() > 0)) {
          if (!task.isActive()) {
            task.setActive(true);
            updateActiveAndStart(appId, task);
          } else {
            taskDeliver.startTaskTimer(task);
          }
        }
      });
    }
  }

  private Boolean updateActive(String appId,ObjectId taskId,Boolean isActive){
    LASUpdate update = LASUpdate.getLASUpdate().set("active", isActive);
    lasDataEntityManager.update(new ObjectId(appId), _SYS_Task.class.getSimpleName(), null, taskId, update);
    return true;
  }

  private Boolean updateActiveAndSetup(String appId,_SYS_Task task){
    updateActive(appId, task.objectId(), true);
    taskDeliver.setupTaskTimer(task);
    return true;
  }

  private Long updateActiveAndStart(String appId,_SYS_Task task){
    updateActive(appId, task.objectId(), true);
    return taskDeliver.startTaskTimer(task);
  }

  @Override
  public void stopTasks(String appId, String version) {
    stopTasks(appId,getVersionTasks(appId,version));
  }

  @Override
  public void deleteTasks(String appId, String version) {
    List<_SYS_Task> tasks = getVersionTasks(appId, version);
    if (tasks != null && tasks.size() > 0 ) {
      tasks.forEach(task -> deleteTask(appId,task.objectIdString()));
    }
  }

  @Override
  public void copyFrom(String appId,String currentVersion, String copyVersion) {
    createCopyTasks(appId,currentVersion,stopTasks(appId,getVersionTasks(appId,copyVersion)));
  }

  private void createCopyTasks(String appId,String currentVersion,List<_SYS_Task> copyFromTasks){
    List<LASObject> newTasks = new ArrayList<>();
    for (_SYS_Task copyFromTask : copyFromTasks) {
      newTasks.add(copyTask(copyFromTask, currentVersion));
    }
    lasDataEntityManager.create(new ObjectId(appId),_SYS_Task.class.getSimpleName(),null,newTasks);
    this.startTasks(appId,currentVersion);
  }

  private List<_SYS_Task> stopTasks(String appId,List<_SYS_Task> tasks){
    if (tasks != null && tasks.size() > 0) {
      return tasks.stream().map(task -> {
        //删除本地定时器
        cancelTaskTimer(task.objectIdString());
        //将任务状态设置为过期状态
        updateActive(appId,task.objectId(),false);
        return task;
      }).collect(Collectors.toList());
    }
    return tasks;
  }

  /**
   * 获取指定版本应用所有的任务
   * @param appId 应用ID
   * @param version 版本
   * @return 任务列表
   */
  private List<_SYS_Task> getVersionTasks(String appId,String version){
    LASQuery query = new LASQuery();
    query.equalTo("version",version);
    List<LASObject> lasObjects = lasDataEntityManager.find(new ObjectId(appId),_SYS_Task.class.getSimpleName(),null,query);
    if (lasObjects != null && lasObjects.size() > 0) {
      List<_SYS_Task> result = new ArrayList<>();
      lasObjects.forEach(lasObject -> result.add(LASObjectJsons.deserialize(LASObjectJsons.serialize(lasObject), _SYS_Task.class)));
      return result;
    }
    return null;
  }

  private LASObject copyTask(_SYS_Task task,String currentVersion){
    _SYS_Task newTask = new _SYS_Task();
    newTask.setActive(task.isActive());
    newTask.setApplicationId(task.getApplicationId());
    newTask.setDesc(task.getDesc());
    newTask.setExecuteTime(task.getExecuteTime());
    newTask.setInterval(task.getInterval());
    newTask.setName(task.getName());
    newTask.setParams(task.getParams());
    newTask.setVersion(currentVersion);
    return ZJsonParser.asObject(ZJsonParser.asJson(newTask),LASObject.class);
  }

  private void cacheTaskTimer(Long timerId, String taskId) {
    if (timerId != -1) {
      taskTimer.put(taskId, timerId);
    }
  }

  private void cancelTaskTimer(String taskId) {
    if (taskTimer.keySet().contains(taskId)) {
      vertx.cancelTimer(taskTimer.get(taskId));
      taskTimer.remove(taskId);
    }
  }
}
