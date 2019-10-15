package com.rnkrsoft.embedded.httpserver.server;

import com.rnkrsoft.config.ConfigProvider;
import com.rnkrsoft.config.properties.PropertiesConfigProvider;
import com.rnkrsoft.embedded.httpserver.*;
import com.rnkrsoft.embedded.httpserver.server.event.Event;
import com.rnkrsoft.embedded.httpserver.server.event.HandleFinishEvent;
import com.rnkrsoft.embedded.httpserver.server.handler.ServletHandler;
import com.rnkrsoft.embedded.httpserver.server.handler.StaticResourceHandler;
import com.rnkrsoft.embedded.httpserver.server.servlet.EmbeddedServletConfig;
import com.rnkrsoft.embedded.httpserver.server.servlet.EmbeddedServletContext;
import com.rnkrsoft.embedded.httpserver.server.servlet.ServletRegistry;
import com.rnkrsoft.utils.StringUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.Filter;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by rnkrsoft.com on 2019/10/10.
 */
@Slf4j
public class EmbeddedServer extends AbstractLifeCycle implements HttpServer {
    InetSocketAddress address;
    ServerSocketChannel serverSocketChannel;
    Selector selector;
    SelectionKey listenerKey;
    //绑定监控端口
    volatile boolean bound = false;
    /**
     * 线程池
     */
    Executor executor;
    /**
     * 分发线程体
     */
    Thread dispatcherThread;
    /**
     * 分发任务
     */
    Dispatcher dispatcher;

    List<Event> events = new LinkedList<Event>();

    Lock lock = new ReentrantLock();
    @Getter
    HttpHandler[] handlers = null;
    @Getter
    ConfigProvider config;


    final List<String> welcomes = new ArrayList<String>(Arrays.asList("index.html"));

    public EmbeddedServer(ConfigProvider config) throws IOException {
        this.serverSocketChannel = ServerSocketChannel.open();
        this.selector = Selector.open();
        this.serverSocketChannel.configureBlocking(false);
        this.listenerKey = serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        this.dispatcher = new Dispatcher();
        this.config = config;
        this.handlers = new HttpHandler[]{new StaticResourceHandler(this), new ServletHandler(this)};

        String hostName = config.getString("server.http.hostName", "localhost");
        String protocol = config.getString("server.http.protocol", "HTTP/1.1");
        String contextPath = config.getString("server.http.contextPath", "");
        String runtimeDir = config.getString("server.http.runtimeDir", "./work");
        String temp = runtimeDir + "/temp";
        String contextDocBase = runtimeDir + "/httpserver-docBase";
        boolean useBodyEncodingForURI = config.getBoolean("server.http.useBodyEncodingForURI", true);
        String uriEncoding = config.getString("server.http.uriEncoding", "UTF-8");
        int asyncTimeout = config.getInteger("server.http.asyncTimeout", 30000);
        int connectionTimeout = config.getInteger("server.http.connectionTimeout", 30000);
        int maxConnections = config.getInteger("server.http.maxConnections", 30000);
        int maxThreads = config.getInteger("server.http.maxThreads", 100);
        String file_encoding = config.getString("file.encoding", "UTF-8");
        this.executor = new ThreadPoolExecutor(0, maxThreads, connectionTimeout, TimeUnit.MILLISECONDS, new SynchronousQueue<Runnable>());
    }

    static ConfigProvider defaultConfig() {
        ConfigProvider config = new PropertiesConfigProvider("httpserver");
        config.init(".", 60);
        return config;
    }

    public EmbeddedServer() throws IOException {
        this(defaultConfig());
    }

    @Override
    public HttpServer publishEvent(Event event) {
        assert events != null;
        events.add(event);
        return this;
    }

    @Override
    public HttpServer bind(InetSocketAddress socketAddress, int backlog) throws IOException {
        if (bound) {
            throw new BindException("EmbeddedServer already bound");
        }
        if (socketAddress == null) {
            throw new NullPointerException("socketAddress is null");
        }
        this.address = socketAddress;
        ServerSocket serverSocket = serverSocketChannel.socket();
        serverSocket.bind(socketAddress, backlog);
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
    public HttpServer setting(WebXml webXml) {
        List<WebXml.ServletTag> servletTags = webXml.getServlets();
        Map<String, WebXml.ServletTag> servletTagMap = new HashMap<String, WebXml.ServletTag>();
        for (WebXml.ServletTag servletTag : servletTags) {
            servletTagMap.put(servletTag.getServletName(), servletTag);
        }
        Map<String, List<WebXml.ServletMappingTag>> servletMappingTagMap = new HashMap<String, List<WebXml.ServletMappingTag>>();
        for (WebXml.ServletMappingTag servletMappingTag : webXml.getServletMappings()) {
            List<WebXml.ServletMappingTag> mappingTags = servletMappingTagMap.get(servletMappingTag.getServletName());
            if (mappingTags == null){
                mappingTags = new ArrayList<WebXml.ServletMappingTag>();
            }
            mappingTags.add(servletMappingTag);
            servletMappingTagMap.put(servletMappingTag.getServletName(), mappingTags);
        }
        for (Map.Entry<String, WebXml.ServletTag> servletTagEntry: servletTagMap.entrySet()) {
            WebXml.ServletTag servletTag = servletTagEntry.getValue();
            List<WebXml.ServletMappingTag> mappingTags = servletMappingTagMap.get(servletTag.getServletName());
            ServletMetadata servletMetadata = ServletMetadata.builder()
                    .servletName(servletTag.getServletName())
                    .servletClass(servletTag.getServletClass())
                    .loadOnStartup(servletTag.getLoadOnStartup())
                    .build();
            for (WebXml.ServletMappingTag servletMappingTag : mappingTags){
                servletMetadata.getUrlPatterns().add(servletMappingTag.getUrlPattern());
            }
            for (WebXml.ParamTag paramTag : servletTag.getInitParams()) {
                servletMetadata.getInitParams().put(paramTag.getParamName(), paramTag.getParamValue());
            }
            ServletRegistry.registerServlet(servletMetadata);
        }
        for (String index : webXml.getWelcomeFileList()) {
            welcomes.clear();
            welcomes.add(index);
        }
        return this;
    }

    @Override
    public HttpServer setting(String name, String value) {
        return null;
    }



    @Override
    public HttpServer register(String servletName, Class<? extends Servlet> servletClass, Properties initParams, int loadOnStartup, String ... urlPatterns) {
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
        if (executor == null) {
            throw new NullPointerException("executor is null");
        }
        status = LifeStatus.STARTING;
        if (handlers != null) {
            for (int i = 0; i < handlers.length; i++) {
                HttpHandler handler = handlers[i];
                handler.start();
            }
        }
        //启动分发线程
        dispatcherThread = new Thread(null, dispatcher, "http-dispatcher", 0);
        log.info("start http dispatcher thread... ");
        dispatcherThread.start();
        status = LifeStatus.STARTED;
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
        status = LifeStatus.STOPPED;
    }

    @Override
    public HttpServer start(int keepAliveMs) {
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

    /**
     * 分发线程
     */
    class Dispatcher implements Runnable {

        final LinkedList<HttpConnection> recyclingConnections = new LinkedList();

        @Override
        public void run() {
            while (isStarted()) {
                try {
                    //处理容器事件
                    handleEvent();

                    selector.select(1000);
                    Set<SelectionKey> selectionKeys = selector.selectedKeys();
                    if (selectionKeys.isEmpty()) {
                        continue;
                    }
                    Iterator<SelectionKey> it = selectionKeys.iterator();
                    while (it.hasNext()) {
                        SelectionKey key = it.next();
                        it.remove();
                        if (key.equals(listenerKey)) {//如果有新连接接入
                            if (isStopping()) {
                                continue;
                            }
                            SocketChannel socketChannel = serverSocketChannel.accept();
                            if (socketChannel != null) {
                                socketChannel.socket().setTcpNoDelay(true);
                                //将接收到的客户端连接设置为非阻塞
                                socketChannel.configureBlocking(false);
                                SelectionKey newKey = socketChannel.register(selector, SelectionKey.OP_READ);
                                //在这里创建连接对象
                                HttpConnection httpConnection = new EmbeddedHttpConnection(EmbeddedServer.this, socketChannel, newKey);
                                //将连接对象绑定选择器
                                newKey.attach(httpConnection);
                                //设置连接对象为请求状态
                                httpConnection.requestState();
                            } else {
                                continue;
                            }
                        } else {
                            //处理连接
                            try {
                                //IO可读
                                if (key.isReadable()) {
                                    SocketChannel socketChannel = (SocketChannel) key.channel();
                                    //获取绑定HTTP连接对象
                                    HttpConnection httpConnection = (HttpConnection) key.attachment();
                                    key.cancel();
                                    //将连接绑定的套接字IO设置为阻塞
                                    socketChannel.configureBlocking(true);
                                    //TODO 将连接从空闲连接移除
                                    //处理连接
                                    handle(httpConnection);
                                }
                            } catch (CancelledKeyException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                    }
                    selector.selectNow();
                } catch (IOException e) {

                }
            }

            //当容器关闭时，关闭NIO
            try {
                selector.close();
            } catch (Exception e) {
            }
        }

        void handleEvent(Event event) {
            if (event instanceof HandleFinishEvent) {
                responseCompleted(event.getConnection());
                HttpConnection connection = event.getConnection();
                connection.close();
            }
        }

        void handleEvent() {
            List<Event> events0 = null;
            try {
                lock.lock();
                if (!events.isEmpty()) {
                    log.info("HttpServer process event queue...");
                    events0 = events;
                    events = new LinkedList<Event>();
                }
            } finally {
                lock.unlock();
            }
            //进行事件处理
            if (events0 != null) {
                for (Event event : events0) {
                    handleEvent(event);
                }
            }

            for (HttpConnection c : recyclingConnections) {
                //重新向NIO注册
                register(c);
            }
            recyclingConnections.clear();
        }

        /**
         * 处理连接
         *
         * @param httpConnection 连接对象
         */
        void handle(HttpConnection httpConnection) {
            try {
                HandleTask t = new HandleTask(httpConnection);
                log.debug("提交业务线程处理");
                executor.execute(t);
            } catch (Exception e) {
                log.error("Pre handleRequest Connection happens error!", e);
                closeConnection(httpConnection);
            }
        }

        void register(HttpConnection connection) {
            try {
                SocketChannel socketChannel = connection.getChannel();
                socketChannel.configureBlocking(false);
                SelectionKey key = socketChannel.register(selector, SelectionKey.OP_READ);
                key.attach(connection);
                connection.setSelectionKey(key);
                connection.setLastActiveTime(System.currentTimeMillis());
            } catch (IOException e) {
                connection.close();
            }
        }
    }
}
