package com.rnkrsoft.embedded.ulwserver.server.connection;

import com.rnkrsoft.embedded.ulwserver.HttpConnection;
import com.rnkrsoft.embedded.ulwserver.HttpProtocol;
import com.rnkrsoft.embedded.ulwserver.UlwServer;
import com.rnkrsoft.embedded.ulwserver.server.EmbeddedUlwServer;
import com.rnkrsoft.embedded.ulwserver.server.event.HandleFinishEvent;
import com.rnkrsoft.embedded.ulwserver.server.header.HttpHeader;
import com.rnkrsoft.embedded.ulwserver.server.io.IOUtils;
import com.rnkrsoft.embedded.ulwserver.server.io.RawInputStream;
import com.rnkrsoft.embedded.ulwserver.server.io.RawOutputStream;
import lombok.Getter;
import lombok.Setter;

import javax.servlet.Filter;
import javax.servlet.Servlet;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.channels.NetworkChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.List;

/**
 * Created by rnkrsoft.com on 2019/10/10.
 */
public class EmbeddedHttpConnection implements HttpConnection {


    InputStream rawIn;
    OutputStream rawOut;

    @Getter
    @Setter
    NetworkChannel channel;
    @Getter
    @Setter
    SelectionKey selectionKey;
    @Getter
    @Setter
    Servlet servlet;
    @Getter
    @Setter
    URI uri;
    @Getter
    @Setter
    String method;
    @Getter
    @Setter
    String version;
    @Getter
    @Setter
    HttpHeader requestHeader;
    @Getter
    @Setter
    HttpHeader responseHeader;
    @Getter
    @Setter
    HttpProtocol protocol;
    UlwServer server;
    @Getter
    ConnectionRegistry connectionRegistry;
    @Getter
    @Setter
    long lastActiveTime = System.currentTimeMillis();
    @Getter
    @Setter
    volatile State state;

    boolean close = false;

    public EmbeddedHttpConnection(ConnectionRegistry connectionRegistry, UlwServer server) {
        this.server = server;
        this.connectionRegistry = connectionRegistry;
    }

    @Override
    public HttpConnection idleState() {
        this.connectionRegistry.idleState(this);
        return this;
    }

    @Override
    public HttpConnection requestState() {
        this.connectionRegistry.requestState(this);
        return this;
    }

    @Override
    public HttpConnection responseState() {
        this.connectionRegistry.responseState(this);
        return this;
    }

    @Override
    public InputStream getRawIn() throws IOException {
        if (rawIn == null) {
            rawIn = new BufferedInputStream(new RawInputStream(((SocketChannel)this.channel)));
        }
        return rawIn;
    }

    @Override
    public OutputStream getRawOut() throws IOException {
        if (rawOut == null) {
            rawOut = new BufferedOutputStream(new RawOutputStream(((SocketChannel)this.channel)));
        }
        return rawOut;
    }

    @Override
    public void close() {
        if (close) {
            return;
        }
        close = true;
        assert channel != null;
        IOUtils.closeQuietly(this.rawIn);
        IOUtils.closeQuietly(this.rawOut);
        IOUtils.closeQuietly(this.channel);
    }

    @Override
    public void recycle() {
        close = false;
        method = null;
        version = null;
        requestHeader = null;
        responseHeader = null;
        uri = null;
        servlet = null;
        rawIn = null;
        rawOut = null;
    }

    @Override
    public UlwServer getServer() {
        return server;
    }

    @Override
    public void finish() {
        this.server.publishEvent(new HandleFinishEvent(this));
    }

    public String getRemoteAddress() {
        try {
            InetSocketAddress address = (InetSocketAddress) ((SocketChannel)this.channel).getRemoteAddress();
            return address.getAddress().getHostAddress();
        } catch (IOException e) {
            return null;
        }
    }

    public String getRemoteHost() {
        try {
            InetSocketAddress address = (InetSocketAddress)((SocketChannel)this.channel).getRemoteAddress();
            return address.getAddress().getHostName();
        } catch (IOException e) {
            return null;
        }
    }

    public int getRemotePort() {
        try {
            InetSocketAddress address = (InetSocketAddress)((SocketChannel)this.channel).getRemoteAddress();
            return address.getPort();
        } catch (IOException e) {
            return -1;
        }
    }

    public String getLocalAddress() {
        try {
            InetSocketAddress address = (InetSocketAddress) this.channel.getLocalAddress();
            return address.getAddress().getHostAddress();
        } catch (IOException e) {
            return null;
        }
    }

    public int getLocalPort() {
        try {
            InetSocketAddress address = (InetSocketAddress) this.channel.getLocalAddress();
            return address.getPort();
        } catch (IOException e) {
            return -1;
        }
    }

    public String getLocalName() {
        try {
            InetSocketAddress address = (InetSocketAddress) this.channel.getLocalAddress();
            return address.getHostName();
        } catch (IOException e) {
            return null;
        }
    }
}
