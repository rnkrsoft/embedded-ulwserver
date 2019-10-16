package com.rnkrsoft.embedded.ulwserver.server.handler;

import com.rnkrsoft.embedded.ulwserver.AbstractLifeCycle;
import com.rnkrsoft.embedded.ulwserver.HttpHandler;
import com.rnkrsoft.embedded.ulwserver.UlwServer;
import lombok.Getter;

/**
 * Created by rnkrsoft.com on 2019/10/14.
 */
public abstract class AbstractHandler extends AbstractLifeCycle implements HttpHandler{
    @Getter
    UlwServer server;

    public AbstractHandler(UlwServer server) {
        this.server = server;
    }

}
