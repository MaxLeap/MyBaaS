package com.maxleap.domain;

import com.maxleap.domain.base.LASObject;
import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Map;

/**
 * Created by shunlv on 16-2-17.
 */
public class LASOrganization extends LASObject {
  public static final String FIELD_NAME = "name";
  public static final String FIELD_WEBSITE = "website";
  public static final String FIELD_ADDRESS = "address";
  public static final String FIELD_QQ = "qq";
  public static final String FIELD_PHONE = "phone";
  public static final String FIELD_CONTRACT = "contract";
  public static final String FIELD_DESCRIPTION = "description";
  public static final String FIELD_ENABLED = "enabled";
  public static final String FIELD_ORG_TYPE = "orgType";
  public static final String FIELD_BILLING_ADDRESS = "billingAddress";
  public static final String FIELD_INVOICE_TITLE = "invoiceTitle";

  public LASOrganization() {
    super();
  }

  @JsonCreator
  public LASOrganization(Map<String, Object> data) {
    super(data);
  }

  public String getName() {
    return (String) this.get(FIELD_NAME);
  }

  public void setName(String name) {
    this.put(FIELD_NAME, name);
  }

  public String getWebsite() {
    return (String) this.get(FIELD_WEBSITE);
  }

  public void setWebsite(String website) {
    this.put(FIELD_WEBSITE, website);
  }

  public String getAddress() {
    return (String) this.get(FIELD_ADDRESS);
  }

  public void setAddress(String address) {
    this.put(FIELD_ADDRESS, address);
  }

  public String getQq() {
    return (String) this.get(FIELD_QQ);
  }

  public void setQq(String qq) {
    this.put(FIELD_QQ, qq);
  }

  public String getPhone() {
    return (String) this.get(FIELD_PHONE);
  }

  public void setPhone(String phone) {
    this.put(FIELD_PHONE, phone);
  }

  public String getContract() {
    return (String) this.get(FIELD_CONTRACT);
  }

  public void setContract(String contract) {
    this.put(FIELD_CONTRACT, contract);
  }

  public String getDescription() {
    return (String) this.get(FIELD_DESCRIPTION);
  }

  public void setDescription(String description) {
    this.put(FIELD_DESCRIPTION, description);
  }

  public Boolean isEnabled() {
    return (Boolean) this.get(FIELD_ENABLED);
  }

  public void setEnabled(Boolean enabled) {
    this.put(FIELD_ENABLED, enabled);
  }

  public String getOrgType() {
    return (String) this.get(FIELD_ORG_TYPE);
  }

  public void setOrgType(String orgType) {
    this.put(FIELD_ORG_TYPE, orgType);
  }

  public String getInvoiceTitle() {
    return (String) this.get(FIELD_INVOICE_TITLE);
  }

  public void setInvoiceTitle(String invoiceTitle) {
    this.put(FIELD_INVOICE_TITLE, invoiceTitle);
  }

  public String getBillingAddress() {
    return (String) this.get(FIELD_BILLING_ADDRESS);
  }

  public void setBillingAddress(String billingAddress) {
    this.put(FIELD_BILLING_ADDRESS, billingAddress);
  }
}
