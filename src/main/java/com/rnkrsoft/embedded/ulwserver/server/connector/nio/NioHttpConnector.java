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
 */
@Slf4j
public class NioHttpConnector extends AbstractHttpConnector{
    ServerSocketChannel serverSocketChannel;
    Selector selector;
    SelectionKey listenerKey;
    /**
     * 分发线程体
     */
    Thread dispatcher;

    /**
     * 线程池
     */
    Executor executor;


    public NioHttpConnector(UlwServer server, ConnectionRegistry connectionRegistry, int port, int backlog) {
        super(server, connectionRegistry, port, backlog);
    }




    @Override
    public void start() {
        try {
            start0();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        try {
            stop0();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void start0() throws IOException {
        this.status = LifeStatus.STARTING;
        this.serverSocketChannel = ServerSocketChannel.open();
        this.selector = Selector.open();
        this.serverSocketChannel.configureBlocking(false);
        this.listenerKey = serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        ServerSocket serverSocket = serverSocketChannel.socket();
        serverSocket.bind(new InetSocketAddress(port), backlog);

        this.executor = new ThreadPoolExecutor(0, 20, 30000, TimeUnit.MILLISECONDS, new SynchronousQueue<Runnable>());
        //启动分发线程
        this.dispatcher = new Thread(null, new Dispatcher(), "http-nio-dispatcher", 0);
        this.dispatcher.start();
        this.status = LifeStatus.STARTED;
    }

    void stop0() throws IOException {
        this.status = LifeStatus.STOPPING;
        serverSocketChannel.close();
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
                                HttpConnection httpConnection = connectionRegistry.getConnection();
                                httpConnection.setSelectionKey(newKey);
                                httpConnection.setChannel(socketChannel);
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
                if (UlwServer.DEBUG){
                    log.debug("提交业务线程处理");
                }
                executor.execute(t);
            } catch (Exception e) {
                log.error("Pre handleRequest Connection happens error!", e);
                connection.close();
            }
        }

        void register(HttpConnection connection) {
            try {
                SocketChannel socketChannel = (SocketChannel) connection.getChannel();
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
