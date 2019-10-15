package com.rnkrsoft.embedded.httpserver.server.servlet;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by rnkrsoft.com on 2019/10/15.
 */
public interface ExecuteServletCallback {
    void execute(ServletRequest request, ServletResponse response) throws ServletException, IOException;
}
