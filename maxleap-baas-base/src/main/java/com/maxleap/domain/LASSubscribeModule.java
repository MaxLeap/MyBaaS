package com.maxleap.domain;

import com.maxleap.domain.base.LASObject;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Map;

/**
 * Created by shunlv on 15-11-13.
 */
public class LASSubscribeModule extends LASObject {
  public static final String MODULE_NAME = "moduleName";
  public static final String CONTENT = "content";
  public static final String PERIOD = "period";
  public static final String TYPE = "type";
  public static final String ROLES = "roles";
  public static final String TASK_TYPE = "taskType";

  public LASSubscribeModule() {
    super();
  }

  @JsonCreator
  public LASSubscribeModule(Map<String, Object> data) {
    super(data);
  }

  public String getModuleName() {
    return (String) this.get(MODULE_NAME);
  }

  public void setModuleName(String moduleName) {
    this.put(MODULE_NAME, moduleName);
  }

  public String getContent() {
    return (String) this.get(CONTENT);
  }

  public void setContent(String content) {
    this.put(CONTENT, content);
  }

  public List<String> getPeriod() {
    return (List<String>) this.get(PERIOD);
  }

  public void setPeriod(List<String> period) {
    this.put(PERIOD, period);
  }

  public String getType() {
    return (String) this.get(TYPE);
  }

  public void setType(String type) {
    this.put(TYPE, type);
  }

  public List<String> getRoles() {
    return (List<String>) this.get(ROLES);
  }

  public void setRoles(List<String> roles) {
    this.put(ROLES, roles);
  }

  public TaskType getTaskType() {
    return (TaskType) this.get(TASK_TYPE);
  }

  public void setTaskType(TaskType taskType) {
    this.put(TASK_TYPE, taskType);
  }

  public static enum Period {
    SECONDS_5("seconds_5") {
      @Override
      public long generateStartTime() {
        return System.currentTimeMillis() + 5000;
      }
    },
    MINUTES_1("minutes_1") {
      @Override
      public long generateStartTime() {
        return System.currentTimeMillis() + 60 * 1000;
      }
    },
    DAILY("Daily") {
      @Override
      public long generateStartTime() {
        LocalDate currentDate = LocalDate.now();
        LocalDateTime nextDateTime = LocalDateTime.of(currentDate.plusDays(1), LocalTime.of(6, 0));

        Clock clock = Clock.systemDefaultZone();
        Instant now = clock.instant();
        ZoneOffset offset = clock.getZone().getRules().getOffset(now);

        return nextDateTime.toEpochSecond(offset) * 1000L;
      }
    },
    WEEKLY("Weekly") {
      @Override
      public long generateStartTime() {
        LocalDate currentDate = LocalDate.now();
        LocalDateTime nextDateTime = LocalDateTime.of(currentDate.with(TemporalAdjusters.next(DayOfWeek.MONDAY)), LocalTime.of(8, 0));

        Clock clock = Clock.systemDefaultZone();
        Instant now = clock.instant();
        ZoneOffset offset = clock.getZone().getRules().getOffset(now);

        return nextDateTime.toEpochSecond(offset) * 1000L;
      }
    },
    MONTHLY("Monthly") {
      @Override
      public long generateStartTime() {
        LocalDate currentDate = LocalDate.now();
        LocalDateTime nextDateTime = LocalDateTime.of(currentDate.plusMonths(1).with(TemporalAdjusters.firstDayOfMonth()), LocalTime.of(12, 0));

        Clock clock = Clock.systemDefaultZone();
        Instant now = clock.instant();
        ZoneOffset offset = clock.getZone().getRules().getOffset(now);

        return nextDateTime.toEpochSecond(offset) * 1000L;
      }
    };

    private String desc;

    private Period(String desc) {
      this.desc = desc;
    }

    public String getDesc() {
      return desc;
    }

    @JsonCreator
    public Period fromString(String desc) {
      return fromDesc(desc);
    }

    public static Period fromDesc(String desc) {
      if (desc == null) {
        return null;
      }
      if (desc.equals(DAILY.desc)) {
        return DAILY;
      } else if (desc.equals(WEEKLY.desc)) {
        return WEEKLY;
      } else if (desc.equals(MONTHLY.desc)) {
        return MONTHLY;
      } else if (desc.equals(SECONDS_5.desc)) {
        return SECONDS_5;
      } else if (desc.equals(MINUTES_1.desc)) {
        return MINUTES_1;
      } else {
        return null;
      }
    }

    @JsonValue
    public String toString() {
      return this.desc;
    }

    public long generateStartTime() {
      throw new AbstractMethodError();
    }
  }

  public static enum TaskType {
    APP_SUM(1, 3600000),
    NEW_ISSUE_EVENT(2, 300000);

    private int id;
    private long timeout;

    private TaskType(int id, long timeout) {
      this.id = id;
      this.timeout = timeout;
    }

    public int getId() {
      return id;
    }

    public void setId(int id) {
      this.id = id;
    }

    public long getTimeout() {
      return timeout;
    }

    public void setTimeout(long timeout) {
      this.timeout = timeout;
    }

    public static TaskType fromInt(int id) {
      if (id == 1) {
        return APP_SUM;
      } else if (id == 2) {
        return NEW_ISSUE_EVENT;
      }

      return null;
    }

    @JsonCreator
    public TaskType fromString(String idStr) {
      if (idStr == null) {
        return null;
      }

      int id = Integer.parseInt(idStr);

      return fromInt(id);
    }

    @JsonValue
    public String toString() {
      return String.valueOf(this.id);
    }
  }
}
