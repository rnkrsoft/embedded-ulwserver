package com.rnkrsoft.embedded.ulwserver.server;

import com.rnkrsoft.config.ConfigProvider;
import com.rnkrsoft.config.properties.PropertiesConfigProvider;
import com.rnkrsoft.embedded.ulwserver.*;
import com.rnkrsoft.embedded.ulwserver.server.connection.ConnectionRegistry;
import com.rnkrsoft.embedded.ulwserver.server.connector.nio.NioHttpConnector;
import com.rnkrsoft.embedded.ulwserver.server.event.Event;
import com.rnkrsoft.embedded.ulwserver.server.handler.ServletHandler;
import com.rnkrsoft.embedded.ulwserver.server.handler.StaticResourceHandler;
import com.rnkrsoft.embedded.ulwserver.server.servlet.ServletRegistry;
import com.rnkrsoft.utils.StringUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.Filter;
import javax.servlet.Servlet;
import java.io.IOException;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by rnkrsoft.com on 2019/10/10.
 */
@Slf4j
public class EmbeddedUlwServer extends AbstractLifeCycle implements UlwServer {
    InetSocketAddress address;
    int port;
    //绑定监控端口
    volatile boolean bound = false;


    List<Event> events = new LinkedList<Event>();

    @Getter
    HttpHandler[] handlers = null;
    @Getter
    HttpConnector[] connectors = null;
    @Getter
    ConfigProvider config;

    ConnectionRegistry connectionRegistry;

    final List<String> welcomes = new ArrayList<String>(Arrays.asList("index.html"));


    final LinkedList<HttpConnection> recyclingConnections = new LinkedList();

    Lock lock = new ReentrantLock();

    public EmbeddedUlwServer(ConfigProvider config) throws IOException {
        this.config = config;
        this.handlers = new HttpHandler[]{new StaticResourceHandler(this), new ServletHandler(this)};
        this.connectionRegistry = new ConnectionRegistry(this);

        String hostName = config.getString("server.http.hostName", "localhost");
        String protocol = config.getString("server.http.protocol", "HTTP/1.1");
        String contextPath = config.getString("server.http.contextPath", "");
        String runtimeDir = config.getString("server.http.runtimeDir", "./work");
        String temp = runtimeDir + "/temp";
        String contextDocBase = runtimeDir + "/ulwserver-docBase";
        boolean useBodyEncodingForURI = config.getBoolean("server.http.useBodyEncodingForURI", true);
        String uriEncoding = config.getString("server.http.uriEncoding", "UTF-8");
        int asyncTimeout = config.getInteger("server.http.asyncTimeout", 30000);
        int connectionTimeout = config.getInteger("server.http.connectionTimeout", 30000);
        int maxConnections = config.getInteger("server.http.maxConnections", 30000);
        int maxThreads = config.getInteger("server.http.maxThreads", 100);
        String file_encoding = config.getString("file.encoding", "UTF-8");
    }

    static ConfigProvider defaultConfig() {
        ConfigProvider config = new PropertiesConfigProvider("ulwserver");
        config.init(".", 60);
        return config;
    }

    public EmbeddedUlwServer() throws IOException {
        this(defaultConfig());
    }

    @Override
    public UlwServer publishEvent(Event event) {
        assert events != null;
        events.add(event);
        return this;
    }

    @Override
    public UlwServer bind(InetSocketAddress socketAddress, int backlog) throws IOException {
        if (bound) {
            throw new BindException("ulwserver already bound");
        }
        if (socketAddress == null) {
            throw new NullPointerException("socketAddress is null");
        }
        this.address = socketAddress;
        bound = true;
        return this;
    }


    @Override
    public void await() {
        while (isRunning()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                //nothing
            }
        }
    }

    @Override
    public void handleEvent() {
        List<Event> events0 = null;
        try {
            lock.lock();
            if (!events.isEmpty()) {
                log.info("UlwServer process event queue...");
                events0 = events;
                events = new LinkedList<Event>();
            }
        } finally {
            lock.unlock();
        }
        //进行事件处理
        if (events0 != null) {
            for (Event event : events0) {
//                handleEvent(event);
            }
        }
        Iterator<HttpConnection> it = recyclingConnections.iterator();
        while (it.hasNext()){
            HttpConnection connection = it.next();
            it.remove();
//            register(connection);
        }
    }



    @Override
    public UlwServer setting(String name, String value) {
        return null;
    }


    @Override
    public UlwServer register(String servletName, Class<? extends Servlet> servletClass, Properties initParams, int loadOnStartup, String... urlPatterns) {
        ServletMetadata servletMetadata = ServletMetadata.builder()
                .servletName(servletName)
                .servletClass(servletClass)
                .loadOnStartup(loadOnStartup)
                .build();
        servletMetadata.getUrlPatterns().addAll(Arrays.asList(urlPatterns));
        for (Map.Entry<Object, Object> entry : initParams.entrySet()) {
            servletMetadata.getInitParams().put(StringUtils.safeToString(entry.getKey()), StringUtils.safeToString(entry.getValue()));
        }
        ServletRegistry.registerServlet(servletMetadata);
        return this;
    }

    /**
     * 请求处理完成，将连接状态转换成应答状态
     */
    public void requestCompleted(HttpConnection conn) {
        conn.responseState();
    }

    /**
     * 应答处理完成，将连接状态转换为空闲状态
     */
    public void responseCompleted(HttpConnection conn) {
        conn.idleState();
    }

    public void requestStarted(HttpConnection conn){
        conn.setLastActiveTime(System.currentTimeMillis());
        conn.requestState();
    }

    //-------------------------------------------------------以下为私有方法---------------------------------------------------
    void closeConnection(HttpConnection conn) {
        conn.close();
    }

    @Override
    public List<String> getWelcomes() {
        return Collections.unmodifiableList(welcomes);
    }

    List<Filter> getSystemFilters() {
        return new ArrayList<Filter>();
    }

    List<Filter> getUserFilters() {
        return new ArrayList<Filter>();
    }

    @Override
    public void start() {
        if (isRunning()) {
            throw new IllegalStateException("server in wrong state");
        }
        this.connectors =  new HttpConnector[]{new NioHttpConnector(this, this.connectionRegistry, this.port, 1024)};
        this.status = LifeStatus.STARTING;
        if (handlers != null) {
            for (int i = 0; i < handlers.length; i++) {
                HttpHandler handler = handlers[i];
                handler.start();
            }
        }
        if (connectors != null){
            for (int i = 0; i < connectors.length; i++) {
                HttpConnector connector = connectors[i];
                connector.start();
            }
        }
        this.connectionRegistry.start();

        log.info("start http dispatcher thread... ");
        this.status = LifeStatus.STARTED;
    }

    @Override
    public void stop() {
        status = LifeStatus.STOPPING;
        if (handlers != null) {
            for (int i = 0; i < handlers.length; i++) {
                HttpHandler handler = handlers[i];
                handler.stop();
            }
        }
        this.connectionRegistry.stop();
        status = LifeStatus.STOPPED;
    }

    @Override
    public UlwServer start(int keepAliveMs) {
        start();
        //TODO 向定时任务注册关闭调用
        return this;
    }


    @Override
    public void stop(int delayMs) {
        //TODO 在延时到达后调用生命周期中的stop
        stop();
    }
    //-------------------------------------------------------以下为内部类---------------------------------------------------

}
