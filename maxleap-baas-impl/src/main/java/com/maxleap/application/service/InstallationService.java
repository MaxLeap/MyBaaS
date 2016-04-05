package com.maxleap.application.service;

import com.maxleap.cloudcode.service.ZCloudCodeService;
import com.maxleap.domain.LASInstallation;
import com.maxleap.domain.auth.LASPrincipal;
import com.maxleap.domain.base.LASObject;
import com.maxleap.domain.base.ObjectId;
import com.maxleap.pandora.core.lasdata.LASQuery;
import com.maxleap.pandora.core.lasdata.LASUpdate;
import com.maxleap.pandora.data.support.LASDataEntityManager;
import com.maxleap.las.sdk.SaveMsg;
import com.maxleap.las.sdk.UpdateMsg;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Map;

/**
 * Created by shunlv on 16-2-22.
 */
@Singleton
public class InstallationService {
  private LASDataEntityManager lasDataEntityManager;
  private ZCloudCodeService zCloudCodeService;
  private static final String CLASS_NAME = "_Installation";

  @Inject
  public InstallationService(LASDataEntityManager lasDataEntityManager,
                             ZCloudCodeService zCloudCodeService) {
    this.lasDataEntityManager = lasDataEntityManager;
    this.zCloudCodeService = zCloudCodeService;
  }

  public List<LASObject> find(ObjectId appId, LASPrincipal principal, LASQuery query) {
    return lasDataEntityManager.find(appId, CLASS_NAME, principal, query);
  }

  public LASObject create(ObjectId appId, LASPrincipal lasPrincipal, LASInstallation lasInstallation, boolean cloudcode) {
    if (cloudcode) {
      SaveMsg saveMsg = zCloudCodeService.invokeCreate(appId.toHexString(), CLASS_NAME, lasInstallation, lasPrincipal);
      lasInstallation.setObjectId(new ObjectId(saveMsg.objectId().toHexString()));
      lasInstallation.setCreatedAt(saveMsg.createdAt());
      lasInstallation.setUpdatedAt(saveMsg.createdAt());
      return lasInstallation;
    } else {
      return lasDataEntityManager.create(appId, CLASS_NAME, lasPrincipal, lasInstallation);
    }
  }

  public int update(ObjectId appId, LASPrincipal principal, LASQuery query, Map<String, Object> params) {
    LASUpdate update = new LASUpdate(params);
    return update(appId, principal, query, update);
  }

  public int update(ObjectId appId, LASPrincipal principal, String installId, Map<String, Object> params, boolean cloudcode) {
    LASUpdate update = new LASUpdate(params);
    return update(appId, principal, installId, update, cloudcode);
  }

  public int update(ObjectId appId, LASPrincipal principal, LASQuery query, LASUpdate update) {
    return lasDataEntityManager.update(appId, CLASS_NAME, principal, query, update);
  }

  public int update(ObjectId appId, LASPrincipal principal, String installId, LASUpdate update, boolean cloudcode) {
    if (cloudcode) {
      LASUpdate update1 = new LASUpdate(update.getModifierOps());
      UpdateMsg updateMsg = zCloudCodeService.invokeUpdate(appId.toHexString(), CLASS_NAME, installId, update1, principal);
      return updateMsg.number();
    }
    return lasDataEntityManager.update(appId, CLASS_NAME, principal, new ObjectId(installId), update);
  }

  public LASObject get(ObjectId appId, LASPrincipal principal, ObjectId installId) {
    return lasDataEntityManager.get(appId, CLASS_NAME, principal, installId);
  }

  public int delete(ObjectId appId, LASPrincipal principal, ObjectId installId) {
    return lasDataEntityManager.delete(appId, CLASS_NAME, principal, installId);
  }

  public long count(ObjectId appId, LASPrincipal principal, LASQuery query) {
    return lasDataEntityManager.count(appId, CLASS_NAME, principal, query);
  }
}
