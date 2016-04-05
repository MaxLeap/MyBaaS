package com.maxleap.pandora.data.support.filter;

import com.maxleap.pandora.core.exception.LASDataException;
import com.maxleap.pandora.data.support.filter.support.Filter;
import com.maxleap.pandora.data.support.filter.support.FilterChain;
import com.maxleap.pandora.data.support.mongo.FindManyMessage;
import com.maxleap.pandora.data.support.mongo.FindOneMessage;
import com.maxleap.pandora.data.support.mongo.MgoRequest;
import com.maxleap.pandora.data.support.mongo.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.util.List;
import java.util.Map;

/**
 * @author sneaky
 * @since 2.0
 */
@Singleton
public class NativeDataDecoratorFilter implements Filter {
  public final static String name = "NativeDataDecoratorFilter";
  private static transient final Logger log = LoggerFactory.getLogger(NativeDataDecoratorFilter.class);

  @Override
  public void doFilter(MgoRequest request, FilterChain chain) throws LASDataException {
    chain.doFilter(request);
  }

  @Override
  public void doFilter(Response response, FilterChain chain) throws LASDataException {

    if (response instanceof FindManyMessage) {
      List<Map> results = ((FindManyMessage) response).getResults();

      for (Map map : results) {
        responseDataDecorate(map);
      }

    } else if (response instanceof FindOneMessage) {
      responseDataDecorate((Map) ((FindOneMessage) response).getResult());
    }
    chain.doFilter(response);
  }

  private void responseDataDecorate(Map map) {
    if (map == null) {
      return;
    }
//    ObjectIdMapper.mapperToMap(map);

  }

  @Override
  public String name() {
    return name;
  }

}
