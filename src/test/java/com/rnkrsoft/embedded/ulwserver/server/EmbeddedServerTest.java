package com.rnkrsoft.embedded.ulwserver.server;

import com.rnkrsoft.embedded.ulwserver.UlwServer;
import com.rnkrsoft.embedded.ulwserver.server.servlet.HelloServlet;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.util.Properties;

/**
 * Created by rnkrsoft.com on 2019/10/10.
 */
public class EmbeddedServerTest {

    @Test
    public void testStart() throws Exception {
        UlwServer server = new EmbeddedUlwServer();
        server.bind(new InetSocketAddress(80), 0);
        server.register("hello", HelloServlet.class, new Properties(), 1, "/hello", "/news/main/list.jsp");
        server.start(1000);
        Thread.sleep(600 * 1000);
    }
}