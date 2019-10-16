package com.rnkrsoft.embedded.ulwserver.server.connector.aio;

import com.rnkrsoft.embedded.ulwserver.HttpProtocol;
import com.rnkrsoft.embedded.ulwserver.UlwServer;
import com.rnkrsoft.embedded.ulwserver.server.connection.ConnectionRegistry;
import com.rnkrsoft.embedded.ulwserver.server.connector.AbstractHttpConnector;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.TimeUnit;

/**
 * Created by rnkrsoft.com on 2019/10/16.
 */
public class AioHttpConnector extends AbstractHttpConnector {
    static final ThreadLocal<HttpProtocol> protocols = new ThreadLocal<HttpProtocol>();

    AsynchronousServerSocketChannel serverSocketChannel;

    public AioHttpConnector(UlwServer server, ConnectionRegistry connectionRegistry, int port, int backlog) {
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

    }


    void start0() throws IOException {
        this.serverSocketChannel = AsynchronousServerSocketChannel.open();
        this.serverSocketChannel.bind(new InetSocketAddress(this.port), this.backlog);
        this.serverSocketChannel.accept(null, new CompletionHandler<AsynchronousSocketChannel, Object>() {
            @Override
            public void completed(AsynchronousSocketChannel socketChannel, Object attachment) {
                serverSocketChannel.accept(null, this);
                handleWithCompletionHandler(socketChannel);
            }

            @Override
            public void failed(Throwable exc, Object attachment) {

            }
        });
    }

    void handleWithCompletionHandler(final AsynchronousSocketChannel socketChannel) {
        if (!socketChannel.isOpen()){
            return;
        }

        final ByteBuffer buffer = ByteBuffer.allocate(256);
        socketChannel.read(buffer, 1, TimeUnit.SECONDS, buffer, new CompletionHandler<Integer, ByteBuffer>() {
            @Override
            public void completed(Integer result, ByteBuffer attachment) {

            }

            @Override
            public void failed(Throwable exc, ByteBuffer attachment) {

            }
        });
        HttpProtocol protocol = protocols.get();
    }
}
