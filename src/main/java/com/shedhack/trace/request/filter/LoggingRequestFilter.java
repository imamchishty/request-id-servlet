package com.shedhack.trace.request.filter;

import com.shedhack.trace.request.api.constant.HttpHeaderKeysEnum;
import com.shedhack.trace.request.api.logging.LoggingHandler;
import com.shedhack.trace.request.api.model.RequestModel;
import com.shedhack.trace.request.api.threadlocal.RequestThreadLocalHelper;
import org.slf4j.MDC;

import javax.servlet.*;
import java.io.IOException;
import java.util.Arrays;

/**
 * <pre>
 *  This filter sets the MDC context for logging frameworks (via slf4j-api).
 *  This filter is used with {@link RequestTraceFilter}
 *  which must be called prior to this in the filter chain (that filter sets some of the
 *  properties that are required for the MDC).
 *
 *  You can modify the MDC context by extending this filter and overriding the setup method.
 *  By default the MDC context contains:
 *
 *      application-id
 *      request-id
 *      group-id
 *      caller-id
 *
 *  The cleanup method will clear the MDC.
 *
 *  As part of the construction for LoggingRequestFilter you can optionally pass
 *  an implementation of {@link com.shedhack.trace.request.api.logging.LoggingHandler}.
 *  If you're using the default constructor then it will automatically log at INFO level, using
 *  {@link DefaultLoggingHandler}.
 *  The alternative is that you use create your own implementation which will
 *  allow you to log the {@link com.shedhack.trace.request.api.model.RequestModel} to your chosen destination.
 * </pre>
 *
 * @author imamchishty
 */
public class LoggingRequestFilter implements Filter {

    private final LoggingHandler loggingHandler;

    public LoggingRequestFilter(LoggingHandler loggingHandler) {
        this.loggingHandler = loggingHandler;
    }

    // default constructor
    public LoggingRequestFilter() {
        loggingHandler = new DefaultLoggingHandler();
    }

    public void init(FilterConfig filterConfig) throws ServletException {

    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        try {
            // Set the MDC context
            setup(RequestThreadLocalHelper.get());

            // log
            loggingHandler.log(RequestThreadLocalHelper.get());

            // continue down the chain
            filterChain.doFilter(servletRequest, servletResponse);
        }
        finally {

            // clear the MDC
            cleanup();
        }
    }

    public void destroy() {

    }

    /**
     * Sets the MDC with:
     *
     * request-id
     * group-id
     */
    public void setup(RequestModel model) {
        if(model != null) {
            MDC.put(HttpHeaderKeysEnum.APPLICATION_ID.key(), model.getApplicationId());
            MDC.put(HttpHeaderKeysEnum.GROUP_ID.key(), model.getGroupId());
            MDC.put(HttpHeaderKeysEnum.REQUEST_ID.key(), model.toString());
            MDC.put(HttpHeaderKeysEnum.CALLER_ID.key(), model.getCallerId());
        }
    }

    /**
     * Clears the MDC.
     */
    public void cleanup() {
        MDC.clear();
    }
}
