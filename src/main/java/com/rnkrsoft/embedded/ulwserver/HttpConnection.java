package com.rnkrsoft.embedded.ulwserver;

import com.rnkrsoft.embedded.ulwserver.server.EmbeddedUlwServer;
import com.rnkrsoft.embedded.ulwserver.server.header.HttpHeader;

import javax.servlet.Filter;
import javax.servlet.Servlet;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.channels.NetworkChannel;
import java.nio.channels.SelectionKey;
import java.util.List;

/**
 * Created by rnkrsoft.com on 2019/10/10.
 * 对HTTP连接的抽象封装,连接对象横跨整个请求处理的全过程
 * 1.空闲状态
 * 2.请求处理状态
 * 3.应答处理状态
 * IDLE->REQUEST->RESPONSE-IDLE如此循环
 */
public interface HttpConnection {
    enum State {
        IDLE,
        REQUEST,
        RESPONSE
    }

    /**
     * 获取状态
     *
     * @return
     */
    State getState();

    /**
     * 空闲状态
     *
     * @return
     */
    HttpConnection idleState();

    /**
     * 请求处理状态
     *
     * @return
     */
    HttpConnection requestState();

    /**
     * 应答处理状态
     *
     * @return
     */
    HttpConnection responseState();

    /**
     * 设置当前HTTP连接绑定的套接字对象
     *
     * @param channel 套接字对象
     */
    void setChannel(NetworkChannel channel);

    /**
     * 获取绑定的套接字对象
     *
     * @return
     */
    NetworkChannel getChannel();

    /**
     * 设置NIO选择键
     *
     * @param selectionKey
     */
    void setSelectionKey(SelectionKey selectionKey);

    SelectionKey getSelectionKey();

    void setMethod(String method);

    String getMethod();

    void setVersion(String version);

    String getVersion();

    void setRequestHeader(HttpHeader requestHeader);

    HttpHeader getRequestHeader();

    void setResponseHeader(HttpHeader responseHeader);

    HttpHeader getResponseHeader();

    void setUri(URI uri);

    URI getUri();

    /**
     * 设置当前HTTP连接对象绑定的Servlet
     *
     * @param servlet servlet对象
     */
    void setServlet(Servlet servlet);

    Servlet getServlet();

    /**
     * 获取原始的输入流
     *
     * @return
     */
    InputStream getRawIn() throws IOException;

    /**
     * 获取原始的输出流
     *
     * @return
     */
    OutputStream getRawOut() throws IOException;

    /**
     * 关闭和客户端的IO连接并进行连接回收标记
     */
    void close();

    /**
     * 回收连接，包括重置成员变量
     */
    void recycle();

    HttpProtocol getProtocol();

    void setProtocol(HttpProtocol protocol);

    UlwServer getServer();

    void setLastActiveTime(long lastActiveTime);

    void finish();

    String getRemoteAddress();

    String getRemoteHost();

    int getRemotePort();

    String getLocalAddress();

    int getLocalPort();

    String getLocalName();
}
