package com.rnkrsoft.embedded.ulwserver.server;

import com.rnkrsoft.embedded.ulwserver.HttpConnection;
import com.rnkrsoft.embedded.ulwserver.HttpProtocol;
import com.rnkrsoft.embedded.ulwserver.server.event.HandleFinishEvent;
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
    SocketChannel channel;
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
    @Getter
    EmbeddedUlwServer server;
    @Getter
    @Setter
    long lastActiveTime = System.currentTimeMillis();
    volatile State state;

    boolean close = false;

    public EmbeddedHttpConnection(EmbeddedUlwServer server, SocketChannel channel, SelectionKey selectionKey) {
        this.server = server;
        this.channel = channel;
        this.selectionKey = selectionKey;
    }

    @Override
    public State getState() {
        return state;
    }

    @Override
    public HttpConnection idleState() {
        this.state = State.IDLE;
        return this;
    }

    @Override
    public HttpConnection requestState() {
        this.state = State.REQUEST;
        return this;
    }

    @Override
    public HttpConnection responseState() {
        this.state = State.RESPONSE;
        return this;
    }

    @Override
    public InputStream getRawIn() throws IOException {
        if (rawIn == null) {
            rawIn = new BufferedInputStream(new RawInputStream(channel));
        }
        return rawIn;
    }

    @Override
    public OutputStream getRawOut() throws IOException {
        if (rawOut == null) {
            rawOut = new BufferedOutputStream(new RawOutputStream(channel));
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
    public List<Filter> getSystemFilters() {
        return this.server.getSystemFilters();
    }

    @Override
    public List<Filter> getUserFilters() {
        return this.server.getUserFilters();
    }

    @Override
    public void finish() {
        this.server.publishEvent(new HandleFinishEvent(this));
    }

    public String getRemoteAddress() {
        try {
            InetSocketAddress address = (InetSocketAddress) this.channel.getRemoteAddress();
            return address.getAddress().getHostAddress();
        } catch (IOException e) {
            return null;
        }
    }

    public String getRemoteHost() {
        try {
            InetSocketAddress address = (InetSocketAddress) this.channel.getRemoteAddress();
            return address.getAddress().getHostName();
        } catch (IOException e) {
            return null;
        }
    }

    public int getRemotePort() {
        try {
            InetSocketAddress address = (InetSocketAddress) this.channel.getRemoteAddress();
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
