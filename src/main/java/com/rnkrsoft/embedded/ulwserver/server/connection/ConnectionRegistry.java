package com.rnkrsoft.embedded.ulwserver.server.connection;

import com.rnkrsoft.embedded.ulwserver.server.AbstractLifeCycle;
import com.rnkrsoft.embedded.ulwserver.HttpConnection;
import com.rnkrsoft.embedded.ulwserver.UlwServer;
import com.rnkrsoft.embedded.ulwserver.server.EmbeddedUlwServer;
import lombok.extern.slf4j.Slf4j;

import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Created by rnkrsoft.com on 2019/10/16.
 */
@Slf4j
public class ConnectionRegistry extends AbstractLifeCycle{
    UlwServer server;
    /**
     * 所有连接
     */
    final Set<HttpConnection> allConnections = new LinkedHashSet<HttpConnection>();
    /**
     * 空闲连接
     */
    final Set<HttpConnection> idleConnections = new LinkedHashSet<HttpConnection>();
    /**
     * 请求状态连接
     */
    final Set<HttpConnection> requestConnections = new LinkedHashSet<HttpConnection>();
    /**
     * 应答状态连接
     */
    final Set<HttpConnection> responseConnections = new LinkedHashSet<HttpConnection>();

    public ConnectionRegistry(UlwServer server) {
        this.server = server;
    }

    public HttpConnection getConnection(){
        if (UlwServer.DEBUG){
            log.info("begin get connection, all:'{}', idle:'{}', request:'{}', response:'{}'", allConnections.size(), idleConnections.size(), requestConnections.size(), responseConnections.size());
        }
        HttpConnection connection = null;
        Iterator<HttpConnection> it = idleConnections.iterator();
        while (it.hasNext()){
            connection = it.next();
            if (connection != null){
                it.remove();
                break;
            }
        }
        if (connection == null) {
            connection = new EmbeddedHttpConnection(this, server);
            allConnections.add(connection);
        }
        connection.requestState();
        return connection;
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {
        try {
            for (HttpConnection connection : allConnections){
                connection.close();
            }
        }catch (Exception e){
            log.error("ConnectionRegistry close connection", e);
        }
    }

    public void idleState(EmbeddedHttpConnection connection){
        HttpConnection.State state = connection.getState();
        if ( state== HttpConnection.State.REQUEST){
            this.requestConnections.remove(connection);
            this.idleConnections.add(connection);
        }else if ( state== HttpConnection.State.RESPONSE){
            this.responseConnections.remove(connection);
            this.idleConnections.add(connection);
        }else{

        }
        connection.setState(HttpConnection.State.IDLE);
    }

    public void requestState(EmbeddedHttpConnection connection){
        HttpConnection.State state = connection.getState();
        if ( state== HttpConnection.State.IDLE){
            this.idleConnections.remove(connection);
            this.requestConnections.add(connection);
        }else if ( state== HttpConnection.State.RESPONSE){
            this.responseConnections.remove(connection);
            this.requestConnections.add(connection);
        }else{

        }
        connection.setState(HttpConnection.State.REQUEST);
    }

    public void responseState(EmbeddedHttpConnection connection){
        HttpConnection.State state = connection.getState();
        if ( state== HttpConnection.State.IDLE){
            this.idleConnections.remove(connection);
            this.responseConnections.add(connection);
        }else if ( state== HttpConnection.State.REQUEST){
            this.requestConnections.remove(connection);
            this.responseConnections.add(connection);
        }else{

        }
        connection.setState(HttpConnection.State.RESPONSE);

    }
}
