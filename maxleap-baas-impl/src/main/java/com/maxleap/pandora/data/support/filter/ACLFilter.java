package com.maxleap.pandora.data.support.filter;

import com.maxleap.domain.auth.LASPrincipal;
import com.maxleap.domain.auth.PermissionType;
import com.maxleap.domain.base.ObjectId;
import com.maxleap.pandora.core.exception.LASDataException;
import com.maxleap.pandora.core.lasdata.LASClassSchema;
import com.maxleap.pandora.core.lasdata.LASQuery;
import com.maxleap.pandora.core.mongo.MongoQuery;
import com.maxleap.pandora.data.support.ClassSchemaManager;
import com.maxleap.pandora.data.support.LASDataEntityManager;
import com.maxleap.pandora.data.support.filter.support.Filter;
import com.maxleap.pandora.data.support.filter.support.FilterChain;
import com.maxleap.pandora.data.support.lasdata.*;
import com.maxleap.pandora.data.support.mongo.MgoRequest;
import com.maxleap.pandora.data.support.mongo.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * User: qinpeng
 * Time: 19:06
 */
@Singleton
public class ACLFilter extends BasePermissionFilter implements Filter {
  public final static String name = "ACLFilter";
  private static transient final Logger logger = LoggerFactory.getLogger(ACLFilter.class);

  @Inject
  public ACLFilter(ClassSchemaManager classSchemaManager, LASDataEntityManager lasDataEntityManager) {
    this.classSchemaManager = classSchemaManager;
    this.lasDataEntityManager = lasDataEntityManager;
  }

  @Override
  public void doFilter(MgoRequest request, FilterChain chain) throws LASDataException {
    AppRequest appRequest = (AppRequest) request;
    LASPrincipal principal = appRequest.getPrincipal();
    LASClassSchema classSchema = appRequest.getClassSchema();

    if (classSchema.getObjectId() == null) {
      chain.doFilter(request);
      return;
    }

    //TODO
    if (principal == null) {
      chain.doFilter(request);
      return;
    }

    if (principal.getPermissions().contains(PermissionType.MASTER_KEY)) {
      // master key ignore ACL
      chain.doFilter(request);
      return;
    }

    if (principal.getPermissions().contains(PermissionType.ORG_USER) || principal.getPermissions().contains(PermissionType.ORG_ADMIN)) {
      // org session ignore ACL
      chain.doFilter(request);
      return;
    }

    if (principal.getPermissions().contains(PermissionType.SYS_ADMIN)) {
      // sys session ignore ACL
      chain.doFilter(request);
      return;
    }

    LASQuery appendQueryForRead = new LASQuery().equalTo(fullUserKeyForRead("*"), true);
    LASQuery appendQueryForWrite = new LASQuery().equalTo(fullUserKeyForWrite("*"), true);

    if (principal.getPermissions().contains(PermissionType.APP_USER)) {
      String userId = principal.getIdentifier();
      List<Set<String>> act = findActRoleAndUsers(classSchema.getAppId(), userId);

      Set<String> actRoles = addAllAncestors(act.get(0), classSchema.getAppId());
      Set<String> actUsers = act.get(1);

      if (!actRoles.isEmpty()) {
        for (String r : actRoles) {
          appendQueryForRead.or(new MongoQuery().equalTo(fullRoleKeyForRead(r), true));
          appendQueryForWrite.or(new MongoQuery().equalTo(fullRoleKeyForWrite(r), true));
        }
      }
      if (!actUsers.isEmpty()) {
        for (String u : actUsers) {
          appendQueryForRead.or(new MongoQuery().equalTo(fullUserKeyForRead(u), true));
          appendQueryForWrite.or(new MongoQuery().equalTo(fullUserKeyForWrite(u), true));
        }
      }
    }

    request = processRequest(request, appendQueryForRead, appendQueryForWrite);
    chain.doFilter(request);

  }

  @Override
  public void doFilter(Response response, FilterChain chain) throws LASDataException {
    chain.doFilter(response);
  }

  private Set<String> addAllAncestors(Set<String> roles, ObjectId appId) {
    Set<String> result = new HashSet<>();
    result.addAll(roles);

    Map<String, Set<String>> rMap = buildAncestorRoles(appId);
    for (String role : roles) {
      if (rMap.containsKey(role))
        result.addAll(rMap.get(role));
    }
    return result;
  }

  private MgoRequest processRequest(MgoRequest request, LASQuery appendQueryForRead, LASQuery appendQueryForWrite) {
    if (request instanceof AppFindOneRequest) {
      LASQuery query = ((AppFindOneRequest) request).getQuery();
      LASQuery newQuery = query.and(appendQueryForRead);
      ((AppFindOneRequest) request).setQuery(newQuery);
    } else if (request instanceof AppFindRequest) {
      LASQuery query = ((AppFindRequest) request).getQuery();
      LASQuery newQuery = query.and(appendQueryForRead);
      ((AppFindRequest) request).setQuery(newQuery);
    } else if (request instanceof AppCountRequest) {
      MongoQuery query = ((AppCountRequest) request).getQuery();
      MongoQuery newQuery = query.and(appendQueryForRead);
      ((AppCountRequest) request).setQuery(newQuery);
    } else if (request instanceof AppUpdateRequest) {
      LASQuery query = ((AppUpdateRequest) request).getQuery();
      LASQuery newQuery = query.and(appendQueryForWrite);
      ((AppUpdateRequest) request).setQuery(newQuery);
    } else if (request instanceof AppDeleteRequest) {
      MongoQuery query = ((AppDeleteRequest) request).getQuery();
      MongoQuery newQuery = query.and(appendQueryForWrite);
      ((AppDeleteRequest) request).setQuery(newQuery);
    } else {
      // todo
    }
    return request;
  }

  public String name() {
    return name;
  }

  private String fullRoleKeyForRead(String roleName) {
    return PREFIX_ACL + "." + PREFIX_ROLE_ACL + roleName + ".read";
  }

  private String fullRoleKeyForWrite(String roleName) {
    return PREFIX_ACL + "." + PREFIX_ROLE_ACL + roleName + ".write";
  }

  private String fullUserKeyForRead(String user) {
    return PREFIX_ACL + "." + user + ".read";
  }

  private String fullUserKeyForWrite(String user) {
    return PREFIX_ACL + "." + user + ".write";
  }

}

