package com.rnkrsoft.embedded.ulwserver.server.connector;

import com.rnkrsoft.embedded.ulwserver.server.AbstractLifeCycle;
import com.rnkrsoft.embedded.ulwserver.HttpConnector;
import com.rnkrsoft.embedded.ulwserver.UlwServer;
import com.rnkrsoft.embedded.ulwserver.server.connection.ConnectionRegistry;


/**
 * Created by rnkrsoft.com on 2019/10/16.
 */
public abstract class AbstractHttpConnector extends AbstractLifeCycle implements HttpConnector{
    protected ConnectionRegistry connectionRegistry;
    protected int port;
    protected int backlog;
    protected UlwServer server;

    public AbstractHttpConnector(UlwServer server, ConnectionRegistry connectionRegistry, int port, int backlog) {
        this.server = server;
        this.connectionRegistry = connectionRegistry;
        this.port = port;
        this.backlog = backlog;
    }
}
