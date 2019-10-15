package com.rnkrsoft.embedded.httpserver.server.servlet;


import javax.servlet.*;
import java.io.IOException;
import java.util.ListIterator;

/**
 * Created by rnkrsoft.com on 2019/10/10.
 * Servlet过滤链
 */
public class EmbeddedFilterChain implements FilterChain {
    ListIterator<Filter> iterator;
    ExecuteServletCallback executeServletCallback;

    public EmbeddedFilterChain(ListIterator<Filter> iterator, ExecuteServletCallback executeServletCallback) {
        this.iterator = iterator;
        this.executeServletCallback = executeServletCallback;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
        while (iterator.hasNext()){
            Filter filter = iterator.next();
            filter.doFilter(request, response, this);
        }
        executeServletCallback.execute(request, response);
    }
}
