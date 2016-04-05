package com.maxleap.organization.service;

import com.maxleap.domain.LASOrganization;
import com.maxleap.pandora.data.support.MongoEntityManager;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

/**
 * Created by shunlv on 16-2-17.
 */
@Singleton
public class OrganizationService {
  private final MongoEntityManager mongoEntityManager;

  private static final String DB = "platform_data";
  private static final String TABLE = "zcloud_organization";

  @Inject
  public OrganizationService(MongoEntityManager mongoEntityManager) {
    this.mongoEntityManager = mongoEntityManager;
  }

  public LASOrganization createOrg(LASOrganization lasOrganization) {
    return mongoEntityManager.create(DB, TABLE, lasOrganization);
  }

  public List<LASOrganization> listAll() {
    return mongoEntityManager.getAll(DB, TABLE, LASOrganization.class);
  }
}
