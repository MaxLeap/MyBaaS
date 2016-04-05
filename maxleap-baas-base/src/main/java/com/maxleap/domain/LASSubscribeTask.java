package com.maxleap.domain;

import com.maxleap.domain.base.LASObject;
import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Map;

/**
 * Created by shunlv on 15-11-18.
 */
public class LASSubscribeTask extends LASObject {
  public static final String SUBSCRIPTION_ID = "subscriptionId";

  public static final String TYPE = "type";
  public static final String PARAMS = "params";

  public static final String PERIOD = "period";
  public static final String START_TIME = "startTime";
  public static final String DEADLINE = "deadline";

  public static final String STATUS = "status";

  public LASSubscribeTask() {
    super();
  }

  @JsonCreator
  public LASSubscribeTask(Map<String, Object> data) {
    super(data);
  }

  public LASSubscribeTask(String subscriptionId, int type, Map params, long startTime, long deadline, int status) {
    this.put(SUBSCRIPTION_ID, subscriptionId);
    this.put(TYPE, type);
    this.put(PARAMS, params);
    this.put(START_TIME, startTime);
    this.put(DEADLINE, deadline);
    this.put(STATUS, status);
  }

  public LASSubscribeTask(String subscriptionId, int type, Map params, long startTime, long deadline, String period, int status) {
    this.put(SUBSCRIPTION_ID, subscriptionId);
    this.put(TYPE, type);
    this.put(PARAMS, params);
    this.put(START_TIME, startTime);
    this.put(DEADLINE, deadline);
    this.put(PERIOD, period);
    this.put(STATUS, status);
  }

  public String getSubscriptionId() {
    return (String) this.get(SUBSCRIPTION_ID);
  }

  public void setSubscriptionId(String subscriptionId) {
    this.put(SUBSCRIPTION_ID, subscriptionId);
  }

  public int getType() {
    return this.get(TYPE) == null ? 0 : (int) this.get(TYPE);
  }

  public void setType(int type) {
    this.put(TYPE, type);
  }

  public Map getParams() {
    return (Map) this.get(PARAMS);
  }

  public void setParams(Map params) {
    this.put(PARAMS, params);
  }

  public String getPeriod() {
    return (String) this.get(PERIOD);
  }

  public void setPeriod(String period) {
    this.put(PERIOD, period);
  }

  public long getStartTime() {
    return this.get(START_TIME) == null ? 0 : (long) this.get(START_TIME);
  }

  public void setStartTime(long startTime) {
    this.put(START_TIME, startTime);
  }

  public long getDeadline() {
    return this.get(DEADLINE) == null ? 0 : (long) this.get(DEADLINE);
  }

  public void setDeadline(long deadline) {
    this.put(DEADLINE, deadline);
  }

  public int getStatus() {
    return this.get(STATUS) == null ? 0 : (int) this.get(STATUS);
  }

  public void setStatus(int status) {
    this.put(STATUS, status);
  }
}
