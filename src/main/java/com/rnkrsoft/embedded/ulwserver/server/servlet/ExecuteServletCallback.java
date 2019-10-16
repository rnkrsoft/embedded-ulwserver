package com.rnkrsoft.embedded.ulwserver.server.servlet;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

/**
 * Created by rnkrsoft.com on 2019/10/15.
 */
public interface ExecuteServletCallback {
    void execute(ServletRequest request, ServletResponse response) throws ServletException, IOException;
}
