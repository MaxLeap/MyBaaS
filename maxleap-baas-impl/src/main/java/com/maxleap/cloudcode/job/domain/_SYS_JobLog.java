package com.maxleap.cloudcode.job.domain;


import com.maxleap.cloudcode.job.enums.JobStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 任务日志，记录任务的开始时间、结束时间、任务状态等信息
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown=true)
public class _SYS_JobLog extends LASObject {
  private String version;
  //所属的任务ID
  private String taskID;
  //任务名称
  private String taskName;
  //任务调用的JOB名称
  private String jobName;
  //任务开始时间
  private long startTimeStamp;
  //任务结束时间
  private long endTimeStamp;
  //任务状态
  private JobStatus status = JobStatus.HANGING;
  //任务信息，包括调用job的成功、失败信息
  private String message;

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public String getTaskID() {
    return taskID;
  }

  public void setTaskID(String taskID) {
    this.taskID = taskID;
  }

  public String getTaskName() {
    return taskName;
  }

  public void setTaskName(String taskName) {
    this.taskName = taskName;
  }

  public String getJobName() {
    return jobName;
  }

  public void setJobName(String jobName) {
    this.jobName = jobName;
  }

  public long getStartTimeStamp() {
    return startTimeStamp;
  }

  public void setStartTimeStamp(long startTimeStamp) {
    this.startTimeStamp = startTimeStamp;
  }

  public long getEndTimeStamp() {
    return endTimeStamp;
  }

  public void setEndTimeStamp(long endTimeStamp) {
    this.endTimeStamp = endTimeStamp;
  }

  public JobStatus getStatus() {
    return status;
  }

  public void setStatus(JobStatus status) {
    this.status = status;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  @Override
  public String toString() {
    return "_SYS_JobLog{" +
        "version='" + version + '\'' +
        ", taskID='" + taskID + '\'' +
        ", taskName='" + taskName + '\'' +
        ", jobName='" + jobName + '\'' +
        ", startTimeStamp=" + startTimeStamp +
        ", endTimeStamp=" + endTimeStamp +
        ", status=" + status +
        ", message='" + message + '\'' +
        "} " + super.toString();
  }
}
