package com.maxleap.pandora.data.support.filter;

import com.maxleap.domain.auth.*;
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

/**
 * User: qinpeng
 * Date: 14-6-11
 * Time: 11:05
 */
@Singleton
public class ClassPermissionFilter extends BasePermissionFilter implements Filter {
  public final static String name = "ClassPermissionFilter";
  private static transient final Logger log = LoggerFactory.getLogger(ClassPermissionFilter.class);

  @Inject
  public ClassPermissionFilter(ClassSchemaManager classSchemaManager, LASDataEntityManager lasDataEntityManager) {
    this.classSchemaManager = classSchemaManager;
    this.lasDataEntityManager = lasDataEntityManager;
  }

  @Override
  public void doFilter(MgoRequest request, FilterChain chain) throws LASDataException {
    if (request instanceof AppRequest) {
      AppRequest appRequest = (AppRequest) request;

      LASClassSchema classSchema = appRequest.getClassSchema();
      if (classSchema == null || classSchema.getClientPermission() == null) {
        chain.doFilter(request);
        return;
      }

      LASPermission permission = classSchema.getClientPermission();
      if (appRequest.getPrincipal() == null) {
        request = filterRequestWithoutSession(request, permission);
        chain.doFilter(request);
        return;
      }
      LASPrincipal principal = appRequest.getPrincipal();

      if (principal.getPermissions().contains(PermissionType.MASTER_KEY)) {
        // master key means admin
        chain.doFilter(request);
        return;
      }

      if (principal.getPermissions().contains(PermissionType.ORG_USER) || principal.getPermissions().contains(PermissionType.ORG_ADMIN)) {
        // org user has all permission
        chain.doFilter(request);
        return;
      }

      if (principal.getPermissions().contains(PermissionType.SYS_ADMIN)) {
        // org user has all permission
        chain.doFilter(request);
        return;
      }

      if (principal.getPermissions().contains(PermissionType.APP_USER)) {
        String userId = principal.getIdentifier();
        LASClassPermissionNode clazz = permission.getClazz();
        LASClassPermissionNode.RestACLPermission restACLPermission = LASClassPermissionNode.RestACLPermission.Full;
        if (clazz != null && clazz.getRestAcl() != null) {
          restACLPermission = clazz.getRestAcl();
        }

        if (restACLPermission.equals(LASClassPermissionNode.RestACLPermission.Shared)) {
          if (request instanceof AppUpdateRequest
              || request instanceof AppDeleteRequest) {
            LASQuery appendQueryForWrite = new LASQuery();
            appendQueryForWrite.equalTo("ACL.creator.id", userId)
                .or(new MongoQuery().equalTo("ACL.creator.type", IdentifierType.API_KEY.getCreatorType()))
                .or(new MongoQuery().notExist("ACL.creator.type"))
                .or(new MongoQuery().equalTo("ACL.users", userId));

            request = processRequest(request, null, appendQueryForWrite);
          }
        } else if (restACLPermission.equals(LASClassPermissionNode.RestACLPermission.Private)) {
          if (!(request instanceof AppCreateRequest
              || request instanceof AppCreateManyRequest)) {
            LASQuery appendQuery = new LASQuery();
            appendQuery.equalTo("ACL.creator.id", userId)
                .or(new MongoQuery().equalTo("ACL.creator.type", IdentifierType.API_KEY.getCreatorType()))
                .or(new MongoQuery().notExist("ACL.creator.type"))
                .or(new MongoQuery().equalTo("ACL.users", userId));

            request = processRequest(request, appendQuery, appendQuery);
          }
        } else if (restACLPermission.equals(LASClassPermissionNode.RestACLPermission.ReadOnly)) {
          if (!(request instanceof AppFindOneRequest
              || request instanceof AppFindRequest
              || request instanceof AppCountRequest)) {
            throw new LASDataException(LASDataException.OPERATION_FORBIDDEN, "User does not has permission to do Write");
          }
        }
      } else {
        request = filterRequestWithoutSession(request, permission);
      }
    }

    chain.doFilter(request);
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

  @Override
  public void doFilter(Response response, FilterChain chain) throws LASDataException {
    chain.doFilter(response);
  }

  private MgoRequest filterRequestWithoutSession(MgoRequest request, LASPermission permission) {
    LASClassPermissionNode clazz = permission.getClazz();
    LASClassPermissionNode.RestACLPermission restACLPermission = LASClassPermissionNode.RestACLPermission.Full;
    if (clazz != null && clazz.getRestAcl() != null) {
      restACLPermission = clazz.getRestAcl();
    }

    if (restACLPermission.equals(LASClassPermissionNode.RestACLPermission.Shared)) {
      if (request instanceof AppUpdateRequest
          || request instanceof AppDeleteRequest) {
        LASQuery appendQueryForWrite = new LASQuery();
        appendQueryForWrite.notExist("ACL.creator.type")
            .or(new MongoQuery().equalTo("ACL.creator.type", IdentifierType.API_KEY.getCreatorType()));

        request = processRequest(request, null, appendQueryForWrite);
      }
    } else if (restACLPermission.equals(LASClassPermissionNode.RestACLPermission.Private)) {
      if (!(request instanceof AppCreateRequest
          || request instanceof AppCreateManyRequest)) {
        LASQuery appendQuery = new LASQuery();
        appendQuery.notExist("ACL.creator.type")
            .or(new MongoQuery().equalTo("ACL.creator.type", IdentifierType.API_KEY.getCreatorType()));

        request = processRequest(request, appendQuery, appendQuery);
      }
    } else if (restACLPermission.equals(LASClassPermissionNode.RestACLPermission.ReadOnly)) {
      if (!(request instanceof AppFindOneRequest
          || request instanceof AppFindRequest
          || request instanceof AppCountRequest)) {
        throw new LASDataException(LASDataException.OPERATION_FORBIDDEN, "User does not has permission to do Write");
      }
    }

    return request;
  }

  @Override
  public String name() {
    return name;
  }
}
