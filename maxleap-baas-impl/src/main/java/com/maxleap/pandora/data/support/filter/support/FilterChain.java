package com.maxleap.pandora.data.support.filter.support;

import com.maxleap.pandora.core.exception.LASDataException;
import com.maxleap.pandora.data.support.mongo.MgoRequest;
import com.maxleap.pandora.data.support.mongo.Response;

/**
 /**
 * A FilterChain is an object provided by the sun container to the developer
 * giving a view into the invocation chain of a filtered request for a resource. Filters
 * use the FilterChain to invoke the next filter in the chain, or if the calling filter
 * is the last filter in the chain, to invoke the resource at the end of the chain.
 *
 * @author sneaky
 * @see Filter
 * @since 2.0.0
 */
public interface FilterChain {

    /**
     * Causes the next filter in the chain to be invoked, or if the calling filter is the last filter
     * in the chain, causes the resource at the end of the chain to be invoked.
     *
     * @param request the request to pass along the chain.
     *
     */
    void doFilter(MgoRequest request) throws LASDataException;
    /**
     * Causes the next filter in the chain to be invoked, or if the calling filter is the last filter
     * in the chain, causes the resource at the end of the chain to be invoked.
     *
     * @param response the response to pass along the chain.
     *
     */
    void doFilter(Response response) throws LASDataException;


}
