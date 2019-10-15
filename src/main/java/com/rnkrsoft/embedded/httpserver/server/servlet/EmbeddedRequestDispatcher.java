package com.rnkrsoft.embedded.httpserver.server.servlet;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

/**
 * Created by rnkrsoft.com on 2019/10/14.
 */
public class EmbeddedRequestDispatcher implements RequestDispatcher{
    @Override
    public void forward(ServletRequest request, ServletResponse response) throws ServletException, IOException {

    }

    @Override
    public void include(ServletRequest request, ServletResponse response) throws ServletException, IOException {

    }
}
