package com.rnkrsoft.embedded.httpserver.server;

import com.rnkrsoft.embedded.httpserver.server.protocol.http11.Http11Protocol;
import com.rnkrsoft.io.buffer.ByteBuf;
import junit.framework.Assert;
import org.junit.Test;

/**
 * Created by rnkrsoft.com on 2019/10/11.
 */
public class HttpHeaderTest {

    @Test
    public void testGetValues() throws Exception {
        ByteBuf byteBuf = ByteBuf.allocate(1024).autoExpand(true);
        byteBuf.putUTF8("Content-Length: 10\n" +
                "Connection: Upgrade, close\n" +
                "Vary: Accept-Encoding\n" +
                "Transfer-Encoding: chunked\n" +
                "Content-Type: application/json; charset=utf-8");
        Http11Protocol protocol = new Http11Protocol();
        HttpHeader httpHeader = new HttpHeader(protocol.parseHeader(byteBuf.asInputStream()));
        Assert.assertEquals(5, httpHeader.size());
        System.out.println(httpHeader.getContentLength());
        System.out.println(httpHeader.getValues("Content-Type"));
    }
}