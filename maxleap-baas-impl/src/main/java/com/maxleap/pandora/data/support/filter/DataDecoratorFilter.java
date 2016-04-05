package com.maxleap.pandora.data.support.filter;

import com.maxleap.domain.base.LASObject;
import com.maxleap.pandora.core.exception.LASDataException;
import com.maxleap.pandora.data.support.filter.support.Filter;
import com.maxleap.pandora.data.support.filter.support.FilterChain;
import com.maxleap.pandora.data.support.lasdata.*;
import com.maxleap.pandora.data.support.mongo.FindManyMessage;
import com.maxleap.pandora.data.support.mongo.FindOneMessage;
import com.maxleap.pandora.data.support.mongo.MgoRequest;
import com.maxleap.pandora.data.support.mongo.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;

/**
 * @author sneaky
 * @since 2.0
 */
public class DataDecoratorFilter implements Filter {
  public final static String name = "DataDecoratorFilter";
  private static transient final Logger log = LoggerFactory.getLogger(DataDecoratorFilter.class);
  private DataDecorator dataDecorator;

  @Inject
  public DataDecoratorFilter(DataDecorator dataDecorator) {
    this.dataDecorator = dataDecorator;
  }

  @Override
  public void doFilter(MgoRequest request, FilterChain chain) throws LASDataException {
    if (request instanceof AppFindOneRequest) {
      AppFindOneRequest findOneRequest = (AppFindOneRequest) request;
      dataDecorator.requestDataDecorate(findOneRequest.getQuery().getQuery(), findOneRequest.getClassSchema(), false);
    } else if (request instanceof AppFindRequest) {
      AppFindRequest findRequest = (AppFindRequest) request;
      dataDecorator.requestDataDecorate(findRequest.getQuery().getQuery(), findRequest.getClassSchema(), false);
    } else if (request instanceof AppCreateRequest) {
      AppCreateRequest appCreateRequest = (AppCreateRequest) request;
      dataDecorator.requestSaveDataDecorate(((LASObject) appCreateRequest.getDocument()).getMap(), appCreateRequest.getClassSchema());
    } else if (request instanceof AppUpdateRequest) {
      AppUpdateRequest updateRequest = (AppUpdateRequest) request;
      dataDecorator.requestDataDecorate(updateRequest.getQuery().getQuery(), updateRequest.getClassSchema(), false);
      dataDecorator.requestUpdateDataDecorate(updateRequest.getUpdate().getModifierOps(), updateRequest.getClassSchema());
    } else if (request instanceof AppDeleteRequest) {
      AppDeleteRequest deleteRequest = (AppDeleteRequest) request;
      dataDecorator.requestDataDecorate(deleteRequest.getQuery().getQuery(), deleteRequest.getClassSchema(), false);
    } else if (request instanceof AppCountRequest) {
      AppCountRequest countRequest = (AppCountRequest) request;
      dataDecorator.requestDataDecorate(countRequest.getQuery().getQuery(), countRequest.getClassSchema(), false);
    } else if (request instanceof AppCreateManyRequest) {
      AppCreateManyRequest createManyRequest = (AppCreateManyRequest) request;
      for (Object doc : createManyRequest.getDocuments()) {
        dataDecorator.requestSaveDataDecorate((Map) doc, ((AppCreateManyRequest) request).getClassSchema());
      }
    }
    chain.doFilter(request);

  }

  @Override
  public void doFilter(Response response, FilterChain chain) throws LASDataException {
    MgoRequest request = response.getRequest();
    if (request instanceof AppFindRequest || request instanceof AppFindOneRequest) {
      if (response instanceof FindOneMessage) {
        AppFindOneRequest appFindOneRequest = (AppFindOneRequest) request;
        FindOneMessage findOneMessage = (FindOneMessage) response;
        Map map = dataDecorator.responseDataDecorate(appFindOneRequest.getClassSchema(), (Map) findOneMessage.getResult(), appFindOneRequest.getQuery().isRelations());
        findOneMessage.setResult((LASObject) map);
      } else if (response instanceof FindManyMessage) {
        AppFindRequest appFindRequest = (AppFindRequest) request;
        List<Map> results = ((FindManyMessage) response).getResults();
        List list = dataDecorator.responseDataDecorate(appFindRequest.getClassSchema(), results, appFindRequest.getQuery().isRelations());
        ((FindManyMessage) response).setResults(list);
      }
    }
    chain.doFilter(response);
  }

  @Override
  public String name() {
    return name;
  }

}
