package com.maxleap.domain;

import com.maxleap.domain.base.ObjectId;
import com.maxleap.domain.mongo.BaseEntity;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Created by.
 * User: ben
 * Date: 12/9/14
 * Time: 10:36 AM
 * Email:benkris1@gmail.com
 *
 * @ilengend
 */
public class LASSegment extends BaseEntity<ObjectId> implements Serializable {

  public static final String FIELD_APP_ID = "appId";
  public static final String FIELD_NAME = "name";
  public static final String FIELD_CRITERIA = "criteria";
  public static final String FIELD_CREATE_BY = "createBy";
  public static final String FIELD_EXPRESSION = "expression";
  public static final String FIELD_CALC_SEG_ID = "calcSegId";
  public static final String FIELD_ATOM_SEGS = "atomSegs";
  public static final String FIELD_ATOM_IDS = "atomIds";
  public static final String FIELD_ANALYZE = "analyze";
  public static final String FIELD_STATUS = "status";
  public static final String FIELD_VISIBLE = "visible";
  public static final String FIELD_CAMPAIGN_NUM = "campaignNum";
  public static final String FIELD_ClOUD_CONFIG_NUM = "cloudConfigNum";
  public static final String FIELD_DISABLE = "disable";


  private String appId;
  private String name;
  private Map criteria;
  private String createBy;
  private String expression;
  private String calcSegId;
  private Set<String> atomIds;
  private Collection<LASAtomSeg> atomSegs;
  private Boolean analyze;
  private LASSegmentStatus status = LASSegmentStatus.created;
  private Boolean visible = true;
  private Integer campaignNum = 0 ;
  private Integer cloudConfigNum =0 ;
  private Boolean disable = false;

  public Integer getCloudConfigNum() {
    return cloudConfigNum;
  }

  public void setCloudConfigNum(Integer cloudConfigNum) {
    this.cloudConfigNum = cloudConfigNum;
  }



  public String getAppId() {
    return appId;
  }

  public void setAppId(String appId) {
    this.appId = appId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getCreateBy() {
    return createBy;
  }

  public void setCreateBy(String createBy) {
    this.createBy = createBy;
  }



  public String getExpression() {
    return expression;
  }

  public void setExpression(String expression) {
    this.expression = expression;
  }

  @JsonInclude(value = JsonInclude.Include.NON_NULL)
  public Collection<LASAtomSeg> getAtomSegs() {
    return atomSegs;
  }

  public void setAtomSegs(Collection<LASAtomSeg> atomSegs) {
    this.atomSegs = atomSegs;
  }


  public String getCalcSegId() {
    return calcSegId;
  }

  public void setCalcSegId(String calcSegId) {
    this.calcSegId = calcSegId;
  }

  public Map getCriteria() {
    return criteria;
  }

  public void setCriteria(Map criteria) {
    this.criteria = criteria;
  }

  @JsonInclude(value = JsonInclude.Include.NON_NULL)
  public Set<String> getAtomIds() {
    return atomIds;
  }

  public void setAtomIds(Set<String> atomIds) {
    this.atomIds = atomIds;
  }

  public LASSegmentStatus getStatus() {
    return status;
  }

  public void setStatus(LASSegmentStatus status) {
    this.status = status;
  }


  public Boolean getAnalyze() {
    return analyze;
  }

  public void setAnalyze(Boolean analyze) {
    this.analyze = analyze;
  }

  public Boolean getVisible() {
    return visible;
  }

  public void setVisible(Boolean visible) {
    this.visible = visible;
  }

  public Integer getCampaignNum() {
    return campaignNum;
  }

  public void setCampaignNum(Integer campaignNum) {
    this.campaignNum = campaignNum;
  }

  public Boolean getDisable() {
    return disable;
  }

  public void setDisable(Boolean disable) {
    this.disable = disable;
  }
}


