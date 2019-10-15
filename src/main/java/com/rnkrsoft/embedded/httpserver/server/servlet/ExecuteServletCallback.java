package com.rnkrsoft.embedded.httpserver.server.servlet;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by rnkrsoft.com on 2019/10/15.
 */
public interface ExecuteServletCallback {
    void execute(ServletRequest request, ServletResponse response);
}
