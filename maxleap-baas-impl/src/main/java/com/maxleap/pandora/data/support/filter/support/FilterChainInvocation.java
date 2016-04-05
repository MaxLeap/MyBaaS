package com.maxleap.pandora.data.support.filter.support;

import com.maxleap.pandora.core.exception.LASDataException;
import com.maxleap.pandora.data.support.mongo.MgoRequest;
import com.maxleap.pandora.data.support.mongo.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * A Filter chain impl which basically passes itself to the "current" filter and iterates the chain
 * on {@code doFilter()}. Modeled on something similar in Apache Tomcat.
 *
 * @author sneaky
 * @see Filter
 * @since 2.0.0
 */
public class FilterChainInvocation implements FilterChain {
    private static final Logger log = LoggerFactory.getLogger(FilterChainInvocation.class);

    private List<Filter> filters;
    private int requestIndex = 0;
    private int responseIndex = 0;

    public FilterChainInvocation(List<Filter> filters) {
        this.filters = filters;
    }

    @Override
    public void doFilter(MgoRequest request) throws LASDataException {
        if (log.isTraceEnabled()) {
            log.trace("Invoking wrapped filter[request] at index [" + this.requestIndex + "]");
        }

        if (requestIndex == filters.size()) {
            return;
        }

        this.filters.get(this.requestIndex++).doFilter(request, this);
    }

    @Override
    public void doFilter(Response response) throws LASDataException {
        if (log.isTraceEnabled()) {
            log.trace("Invoking wrapped filter[response] at index [" + this.responseIndex + "]");
        }

        if (responseIndex == filters.size()) {
            return;
        }

        this.filters.get(this.responseIndex++).doFilter(response, this);
    }
}
