package com.rnkrsoft.embedded.ulwserver.server.connector.aio;

import com.rnkrsoft.embedded.ulwserver.UlwServer;
import com.rnkrsoft.embedded.ulwserver.server.connection.ConnectionRegistry;
import com.rnkrsoft.embedded.ulwserver.server.connector.AbstractHttpConnector;

import java.net.InetSocketAddress;

/**
 * Created by rnkrsoft.com on 2019/10/16.
 */
public class AioHttpConnector extends AbstractHttpConnector{
    public AioHttpConnector(UlwServer server, ConnectionRegistry connectionRegistry, int port, int backlog) {
        super(server, connectionRegistry, port, backlog);
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }
}
