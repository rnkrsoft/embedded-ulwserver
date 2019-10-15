package com.rnkrsoft.embedded.httpserver.server;

import com.rnkrsoft.embedded.httpserver.HttpServer;
import com.rnkrsoft.embedded.httpserver.server.servlet.HelloServlet;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.util.Properties;

/**
 * Created by rnkrsoft.com on 2019/10/10.
 */
public class EmbeddedServerTest {

    @Test
    public void testStart() throws Exception {
        HttpServer server = new EmbeddedServer();
        server.bind(new InetSocketAddress(80), 0);
        server.register("hello", HelloServlet.class, new Properties(), 1, "/hello", "/news/main/list.jsp");
        server.start(1000);
        Thread.sleep(600 * 1000);
    }
}