package com.rnkrsoft.embedded.ulwserver.server.connector.nio;

import com.rnkrsoft.embedded.ulwserver.HttpConnection;
import com.rnkrsoft.embedded.ulwserver.LifeStatus;
import com.rnkrsoft.embedded.ulwserver.UlwServer;
import com.rnkrsoft.embedded.ulwserver.server.connection.ConnectionRegistry;
import com.rnkrsoft.embedded.ulwserver.server.connector.AbstractHttpConnector;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by rnkrsoft.com on 2019/10/16.
 * 基于NIO的HTTP连接器
 */
@Slf4j
public class NioHttpConnector extends AbstractHttpConnector {
    ServerSocketChannel serverSocketChannel;
    Selector selector;
    SelectionKey listenerKey;
    /**
     * 演示关闭时间（秒）
     */
    int delaySec = 10;
    /**
     * 分发线程体
     */
    Thread dispatcher;

    /**
     * 线程池
     */
    Executor executor;

    volatile int connectedClientSize = 0;


    public NioHttpConnector(UlwServer server, ConnectionRegistry connectionRegistry, int port, int backlog) {
        super(server, connectionRegistry, port, backlog);
    }


    @Override
    public void start() {
        try {
            start0();
        } catch (IOException e) {
            setStatus(LifeStatus.FAILED);
            log.error("start nio connector happens error!", e);
        }
    }

    @Override
    public void stop() {
        try {
            stop0();
        } catch (IOException e) {
            setStatus(LifeStatus.STOPPED);
            log.error("stop nio connector happens error!", e);
        }
    }

    void start0() throws IOException {
        if (UlwServer.DEBUG){
            log.debug("begin to start nio connector...");
        }
        //将连接器设置成启动中
        setStatus(LifeStatus.STARTING);
        if (UlwServer.DEBUG){
            log.debug("open listen port {} ...", port);
        }
        this.serverSocketChannel = ServerSocketChannel.open();
        this.selector = Selector.open();
        //监听设置成非阻塞
        this.serverSocketChannel.configureBlocking(false);
        this.listenerKey = serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        ServerSocket serverSocket = serverSocketChannel.socket();
        //启动端口重用
        serverSocket.setReuseAddress(true);
        //绑定端口
        serverSocket.bind(new InetSocketAddress(port), backlog);

        this.executor = new ThreadPoolExecutor(0, 20, 30000, TimeUnit.MILLISECONDS, new SynchronousQueue<Runnable>());
        //启动多路复用器线程
        this.dispatcher = new Thread(null, new Dispatcher(), "http-nio-dispatcher", 0);
        if (UlwServer.DEBUG){
            log.debug("start dispatcher thread ...");
        }
        this.dispatcher.start();
        //将连接器设置为已启动
        setStatus(LifeStatus.STARTED);
        if (UlwServer.DEBUG){
            log.debug("finish start nio connector...");
        }
    }

    void stop0() throws IOException {
        if (UlwServer.DEBUG){
            log.debug("begin to stop nio connector...");
        }
        setStatus(LifeStatus.STOPPING);
        if (UlwServer.DEBUG){
            log.debug("close Server Socket Channel...");
        }
        serverSocketChannel.close();
        if (UlwServer.DEBUG){
            log.debug("close all connections...");
        }
        connectionRegistry.stop();
        selector.wakeup();
        long latest = System.currentTimeMillis() + delaySec * 1000;
        if (UlwServer.DEBUG){
            log.debug("delay {} s to close connector...", delaySec);
        }
        while (System.currentTimeMillis() < latest) {
            Thread.yield();
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
            }
            if (isStopped()) {
                break;
            }
        }
        if (dispatcher != null) {
            try {
                if (UlwServer.DEBUG){
                    log.debug("close dispatcher thread...");
                }
                dispatcher.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Nio Http Connector stop hasppens error ", e);
            }
        }
        setStatus(LifeStatus.STOPPED);
        if (UlwServer.DEBUG){
            log.debug("finish stop nio connector......");
        }
    }


    /**
     * 分发线程
     */
    class Dispatcher implements Runnable {

        @Override
        public void run() {
            while (isStarted()) {
                try {
                    //处理容器事件
                    server.handleEvent();
                    //TODO 对连接进行处理重新注册

                    selector.select(1000);
                    Set<SelectionKey> selectionKeys = selector.selectedKeys();
                    if (selectionKeys.isEmpty()) {
                        continue;
                    }
                    Iterator<SelectionKey> it = selectionKeys.iterator();
                    while (it.hasNext()) {
                        SelectionKey key = it.next();
                        it.remove();
                        if (key.isAcceptable()) {//如果有新连接接入
                            //如果连接器停止中或者已停止
                            if (isStopping() || isStopped()) {
                                continue;
                            }
                            SocketChannel socketChannel = serverSocketChannel.accept();
                            if (socketChannel != null) {
                                //开启Nagle算法，该算法是为了提高较慢的广域网传输效率，减小小分组的报文个数
                                socketChannel.socket().setTcpNoDelay(true);
                                //将接收到的客户端连接设置为非阻塞
                                socketChannel.configureBlocking(false);
                                SelectionKey newKey = socketChannel.register(selector, SelectionKey.OP_READ);
                                //在这里创建连接对象,在套接字连接时创建新连接
                                HttpConnection connection = connectionRegistry.getConnection();
                                connection.setSelectionKey(newKey);
                                connection.setChannel(socketChannel);
                                //将连接对象绑定选择器
                                newKey.attach(connection);
                            }
                        } else {
                            //处理连接
                            try {
                                //IO可读
                                if (key.isReadable()) {
                                    if (UlwServer.DEBUG){
                                        log.debug("receive begin http request...");
                                    }
                                    SocketChannel socketChannel = (SocketChannel) key.channel();
                                    //获取绑定HTTP连接对象
                                    HttpConnection connection = (HttpConnection) key.attachment();
                                    //将当前key设置取消关注事件
                                    key.cancel();
                                    //将连接对象设置为请求处理状态
                                    connection.requestState();
                                    //将连接绑定的套接字IO设置为阻塞
                                    socketChannel.configureBlocking(true);
                                    //处理连接
                                    handle(connection);
                                    if (UlwServer.DEBUG){
                                        log.debug("receive end http request...");
                                    }
                                }
                            } catch (CancelledKeyException e) {
                                ;
                            } catch (IOException e) {
                                ;
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

//        void handleEvent(Event event) {
//            if (event instanceof HandleFinishEvent) {
//                HttpConnection connection = event.getConnection();
//                try {
//                    responseCompleted(connection);
//                    InputStream is = connection.getRawIn();
//                    if (is.available() > 0){
//                        requestStarted(connection);
//                        handle(connection);
//                    }else{
//                        recyclingConnections.add(connection);
//                    }
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }

        /**
         * 处理连接
         *
         * @param connection 连接对象
         */
        void handle(HttpConnection connection) {
            try {
                ConsumerThread t = new ConsumerThread(connection);
                executor.execute(t);
            } catch (Exception e) {
                log.error("ConsumerThread happens error!", e);
                connection.close();
            }
        }
//
//        void register(HttpConnection connection) {
//            try {
//                SocketChannel socketChannel = (SocketChannel) connection.getChannel();
//                socketChannel.configureBlocking(false);
//                SelectionKey key = socketChannel.register(selector, SelectionKey.OP_READ);
//                key.attach(connection);
//                connection.setSelectionKey(key);
//                connection.setLastActiveTime(System.currentTimeMillis());
//            } catch (IOException e) {
//                connection.close();
//            }
//        }
    }
}
