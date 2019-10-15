package com.rnkrsoft.embedded.httpserver.server.handler;

import com.rnkrsoft.embedded.httpserver.AbstractLifeCycle;
import com.rnkrsoft.embedded.httpserver.HttpHandler;
import com.rnkrsoft.embedded.httpserver.HttpServer;
import lombok.Getter;

/**
 * Created by rnkrsoft.com on 2019/10/14.
 */
public abstract class AbstractHandler extends AbstractLifeCycle implements HttpHandler{
    @Getter
    HttpServer server;

    public AbstractHandler(HttpServer server) {
        this.server = server;
    }

    @Override
    public void destroy() {

    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }


}
