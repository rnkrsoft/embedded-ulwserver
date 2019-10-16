package com.rnkrsoft.embedded.ulwserver.server.connector.nio;

import com.rnkrsoft.config.ConfigProvider;
import com.rnkrsoft.config.properties.PropertiesConfigProvider;
import com.rnkrsoft.embedded.ulwserver.HttpHandler;
import com.rnkrsoft.embedded.ulwserver.LifeStatus;
import com.rnkrsoft.embedded.ulwserver.UlwServer;
import com.rnkrsoft.embedded.ulwserver.server.connection.ConnectionRegistry;
import com.rnkrsoft.embedded.ulwserver.server.event.Event;
import com.rnkrsoft.embedded.ulwserver.server.handler.StaticResourceHandler;
import org.junit.Test;

import javax.servlet.Servlet;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.*;

/**
 * Created by rnkrsoft.com on 2019/10/16.
 */
public class NioHttpConnectorTest implements UlwServer{

    @Test
    public void testStart() throws Exception {
        NioHttpConnector connector = new NioHttpConnector(this, new ConnectionRegistry(this), 80, 1024);
        connector.start();
        Thread.sleep(20 * 1000);
        connector.stop();
    }

    @Override
    public UlwServer publishEvent(Event event) {
        return null;
    }

    @Override
    public UlwServer bind(InetSocketAddress socketAddress, int backlog) throws IOException {
        return null;
    }

    @Override
    public HttpHandler[] getHandlers() {
        return new HttpHandler[]{new StaticResourceHandler(this)};
    }

    @Override
    public UlwServer start(int keepAliveMs) {
        return null;
    }

    @Override
    public void stop(int delayMs) {

    }

    @Override
    public void await() {

    }

    @Override
    public void handleEvent() {

    }

    @Override
    public ConfigProvider getConfig() {
        return new PropertiesConfigProvider("test");
    }

    @Override
    public List<String> getWelcomes() {
        return Arrays.asList("index.html");
    }

    @Override
    public UlwServer parameter(String name, String value) {
        return null;
    }

    @Override
    public UlwServer register(String servletName, Class<? extends Servlet> servletClass, Properties initParams, int loadOnStartup, String... urlPatterns) {
        return null;
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public boolean isRunning() {
        return false;
    }

    @Override
    public boolean isStarted() {
        return false;
    }

    @Override
    public boolean isStarting() {
        return false;
    }

    @Override
    public boolean isStopping() {
        return false;
    }

    @Override
    public boolean isStopped() {
        return false;
    }

    @Override
    public boolean isFailed() {
        return false;
    }

    @Override
    public void setStatus(LifeStatus status) {

    }
}