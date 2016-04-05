package com.maxleap.cloudcode.job.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

/**
 * 任务，包括
 * 立即执行的一次性任务（executeTime为-1，interval为-1）、
 * 延时执行的一次性任务（executeTime不为-1，interval为-1）、
 * 立即执行的周期任务（executeTime为-1，interval不为-1）、
 * 延时执行的周期任务（executeTime不为-1，interval不为-1）
 * User：poplar
 * Date：15-6-9
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown=true)
public class _SYS_Task extends LASObject {
	private String applicationId;//AppId
  private String version;//版本
	private Map params; //消息
	private long executeTime;//待执行时间
	private long interval = -1; //重复周期,间隔时间cd
	private String name;//Job名称
	private String desc;//任务名称
	private Boolean active = Boolean.TRUE;//是否活跃，周期任务永远处于活跃状态，一次性任务完成后将会过期

	public String getApplicationId() {
		return applicationId;
	}

	public void setApplicationId(String applicationId) {
		this.applicationId = applicationId;
	}

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public Map getParams() {
		return params;
	}

	public void setParams(Map params) {
		this.params = params;
	}

	public long getExecuteTime() {
		return executeTime;
	}

	public void setExecuteTime(long executeTime) {
		this.executeTime = executeTime;
	}

	public long getInterval() {
		return interval;
	}

	public void setInterval(long interval) {
		this.interval = interval;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public Boolean isActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

  @Override
  public String toString() {
    return "_SYS_Task{" +
        "applicationId='" + applicationId + '\'' +
        ", version='" + version + '\'' +
        ", params=" + params +
        ", executeTime=" + executeTime +
        ", interval=" + interval +
        ", name='" + name + '\'' +
        ", desc='" + desc + '\'' +
        ", active=" + active +
        "} " + super.toString();
  }
}
