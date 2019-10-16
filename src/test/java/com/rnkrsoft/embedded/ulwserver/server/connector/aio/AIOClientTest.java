package com.rnkrsoft.embedded.ulwserver.server.connector.aio;

import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class AIOClientTest {

    @Test
    public void test0() throws IOException, InterruptedException, ExecutionException, TimeoutException {
        //        new AIOServer().startWithFuture();
        new AIOServer().startWithCompletionHandler();
        Thread.sleep(600000);

    }

    @Test
    public void test1() throws ExecutionException, InterruptedException, IOException {
        AsynchronousSocketChannel client = AsynchronousSocketChannel.open();
        client.connect(new InetSocketAddress("localhost", 9888)).get();
        client.write(ByteBuffer.wrap("123456789".getBytes()));
        Thread.sleep(1111111);
    }
}