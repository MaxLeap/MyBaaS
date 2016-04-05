package com.maxleap.pandora.data.support.filter.support;

import com.maxleap.pandora.core.exception.LASDataException;
import com.maxleap.pandora.data.support.mongo.MgoRequest;
import com.maxleap.pandora.data.support.mongo.Response;

/**
 * 	/**
 * A filter is an object that performs filtering tasks on either the request to a resource (a servlet or static content), or on the response from a resource, or both.
 * <br><br>
 * Filters perform filtering in the <code>doFilter</code> method. Every Filter has access to
 * a FilterConfig object from which it can obtain its initialization parameters, a
 * reference to the ServletContext which it can use, for example, to load resources
 * needed for filtering tasks.
 * <p>
 * Filters are configured in the deployment descriptor of a web application
 * <p>
 * Examples that have been identified for this design are<br>
 * 1) Authentication Filters <br>
 * 2) Logging and Auditing Filters <br>
 * 3) Image conversion Filters <br>
 * 4) Data compression Filters <br>
 ** 5) Encryption Filters <br>
 ** 7) Filters that trigger resource access events <br>
 *
 * @author sneaky
 * @since 2.0.0
 */
public interface Filter {
    /**
     * The <code>doFilter</code> method of the Filter is called by the container
     * each time a request is passed through the chain due
     * to a client request for a resource at the end of the chain. The FilterChain passed in to this
     * method allows the Filter to pass on the request and response to the next entity in the
     * chain.<p>
     * A typical implementation of this method would follow the following pattern:- <br>
     * 1. Examine the request<br>
     * 2. Optionally wrap the request object with a custom implementation to
     * filter content or headers for input filtering <br>
     * 3. Optionally wrap the response object with a custom implementation to
     * filter content or headers for output filtering <br>
     * 4. a) <strong>Either</strong> invoke the next entity in the chain using the FilterChain object (<code>chain.doFilter()</code>), <br>
     ** 4. b) <strong>or</strong> not pass on the request/response pair to the next entity in the filter chain to block the request processing<br>
     ** 5. Directly set headers on the response after invocation of the next entity in the filter chain.
     **/
    void doFilter(MgoRequest request, FilterChain chain) throws LASDataException;

    /**
     * The <code>doFilter</code> method of the Filter is called by the container
     * each time a response is passed through the chain due
     * to a client request for a resource at the end of the chain. The FilterChain passed in to this
     * method allows the Filter to pass on the request and response to the next entity in the
     * chain.<p>
     * A typical implementation of this method would follow the following pattern:- <br>
     * 1. Examine the request<br>
     * 2. Optionally wrap the request object with a custom implementation to
     * filter content or headers for input filtering <br>
     * 3. Optionally wrap the response object with a custom implementation to
     * filter content or headers for output filtering <br>
     * 4. a) <strong>Either</strong> invoke the next entity in the chain using the FilterChain object (<code>chain.doFilter()</code>), <br>
     ** 4. b) <strong>or</strong> not pass on the request/response pair to the next entity in the filter chain to block the request processing<br>
     ** 5. Directly set headers on the response after invocation of the next entity in the filter chain.
     **/
    void doFilter(Response response, FilterChain chain) throws LASDataException;

    /**
     * The Filter name
     */
    String name();
}
