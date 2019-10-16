package com.rnkrsoft.embedded.ulwserver.server.connector.aio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


public class AIOServer {
    public final static int PORT = 9888;
    private AsynchronousServerSocketChannel server;

    public AIOServer() throws IOException {
        server = AsynchronousServerSocketChannel.open().bind(new InetSocketAddress(PORT));
    }

    public void startWithFuture() throws InterruptedException, ExecutionException, TimeoutException {
        while (true) {// 循环接收客户端请求
            Future<AsynchronousSocketChannel> future = server.accept();
            AsynchronousSocketChannel socket = future.get();// get() 是为了确保 accept 到一个连接
            handleWithFuture(socket);
        }
    }

    public void handleWithFuture(AsynchronousSocketChannel channel) throws InterruptedException, ExecutionException, TimeoutException {
        ByteBuffer readBuf = ByteBuffer.allocate(2);
        readBuf.clear();

        while (true) {// 一次可能读不完
            //get 是为了确保 read 完成，超时时间可以有效避免DOS攻击，如果客户端一直不发送数据，则进行超时处理
            Integer integer = channel.read(readBuf).get(10, TimeUnit.SECONDS);
            System.out.println("read: " + integer);
            if (integer == -1) {
                break;
            }
            readBuf.flip();
            System.out.println("received: " + Charset.forName("UTF-8").decode(readBuf));
            readBuf.clear();
        }
    }

    public void startWithCompletionHandler() throws InterruptedException, ExecutionException, TimeoutException {
        server.accept(
                null,
                new CompletionHandler<AsynchronousSocketChannel, Object>() {
                    public void completed(AsynchronousSocketChannel socketChannel, Object attachment) {
                        server.accept(null, this);// 再此接收客户端连接
                        handleWithCompletionHandler(socketChannel);
                    }

                    @Override
                    public void failed(Throwable exc, Object attachment) {
                        exc.printStackTrace();
                    }
                }
        );
    }

    public void handleWithCompletionHandler(final AsynchronousSocketChannel socketChannel) {
        try {
            final ByteBuffer buffer = ByteBuffer.allocate(4);
            final long timeout = 10L;
            socketChannel.read(buffer, timeout, TimeUnit.SECONDS, buffer, new CompletionHandler<Integer, ByteBuffer>() {
                @Override
                public void completed(Integer result, ByteBuffer attachment) {
                    System.out.println("read:" + result);
                    if (result == -1) {
                        try {
                            socketChannel.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return;
                    }
                    attachment.flip();
                    System.out.println("received message:" + Charset.forName("UTF-8").decode(buffer));
                    attachment.clear();
                    socketChannel.read(attachment, timeout, TimeUnit.SECONDS, null, this);
                }

                @Override
                public void failed(Throwable exc, ByteBuffer attachment) {
                    exc.printStackTrace();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}