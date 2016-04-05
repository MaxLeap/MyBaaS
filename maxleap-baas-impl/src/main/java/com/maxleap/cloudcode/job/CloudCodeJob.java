package com.maxleap.cloudcode.job;

import com.maxleap.cloudcode.job.domain._SYS_Task;
import com.maxleap.domain.base.LASObject;

/**
 * User：poplar
 * Date：15-5-21
 */
public interface CloudCodeJob {

  /**
   * 创建任务，包括
   * 立即执行的一次性任务[立即执行cloudcode job(创建_SYS_JobLog，调用job请求，更新jobLog)]、
   * 延时执行的一次性任务[创建一次性定时器等待执行cloudcode job]、
   * 立即执行的周期任务[立即执行cloudcode job，同时创建周期定时器等待重复执行cloudcode job]、
   * 延时执行的周期任务[创建一次性定时器等待首次执行cloudcode job,首次执行后创建周期定时器等待重复执行cloudcode job]
   * @param sysTask
   * @return
   */
	LASObject addTask(_SYS_Task sysTask);

  /**
   * 更新任务
   * 去掉以前该任务的定时器，重新添加定时器(如果需要的话)
   * @param sysTask
   * @return
   */
	Object updateTask(_SYS_Task sysTask);

  /**
   * 删除任务
   * 去掉该任务正在运行的定时器
   * @param appId 应用ID
   * @param taskId 任务ID
   * @return
   */
	Integer deleteTask(String appId, String taskId);

  /**
   * 强制执行任务（包括过期任务、正在运行的任务），不会影响已经存在的定时器
   * @param appId 应用ID
   * @param taskId 任务ID
   * @return
   */
	Boolean runTask(String appId, String taskId);

  /**
   * 启用任务，强制更新任务为活跃状态，如果任务是一次性的过期任务或者本地已经启动该定时任务，则忽略掉，否则启动该任务
   * @param appId 应用ID
   * @param taskId 任务ID
   * @return
   */
  Boolean enableTask(String appId, String taskId);

  /**
   * 禁用任务，强制更新任务状态为非活跃，如果本地已经启动该定时任务，取消之
   * @param appId 应用ID
   * @param taskId 任务ID
   * @return
   */
  Boolean disableTask(String appId, String taskId);

  /**
   * 停止指定应用版本的所有任务（不会删除任务，只停止定时器）,当用户卸载指定应用版本时调用
   * @param appId 应用ID
   * @param version 版本
   * @return
   */
  void stopTasks(String appId, String version);

  /**
   * 停止并删除指定应用版本的所有任务，当用户删除指定应用版本时调用
   * @param appId 应用ID
   * @param version 版本
   * @return
   */
  void deleteTasks(String appId, String version);

  /**
   * 启动指定应用版本的所有任务，当用户首次部署指定应用版本时调用
   * @param appId 应用ID
   * @param version 版本
   * @return
   */
  void startTasks(String appId, String version);

  /**
   * 复制指定版本的所有任务到当前版本
   * @param appId 应用ID
   * @param currentVersion 当前版本
   * @param copyVersion 需要复制的版本
   * @return
   */
  void copyFrom(String appId, String currentVersion, String copyVersion);

}
