package com.rnkrsoft.embedded.httpserver.server;

import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by rnkrsoft.com on 2019/10/15.
 */
public class TestURI {
    @Test
    public void test1() throws URISyntaxException {
        URI uri= new URI("http://localhost:8080/news/main/list.jsp?name=1234&part=true&text=%e6%88%91%e4%bb%ac");
        System.out.println("uri:" + uri);
        System.out.println("getRawPath:" + uri.getRawPath());
        System.out.println("getRawQuery:" + uri.getRawQuery());
        System.out.println("getRawSchemeSpecificPart:" + uri.getRawSchemeSpecificPart());
        System.out.println("getHost:" + uri.getHost());
        System.out.println("getPort:" + uri.getPort());
        System.out.println("getPath:" + uri.getPath());
        System.out.println("getQuery:" + uri.getQuery());
        System.out.println("getScheme:" + uri.getScheme());
        System.out.println("getSchemeSpecificPart:" + uri.getSchemeSpecificPart());
        System.out.println("getUserInfo:" + uri.getUserInfo());
    }
}
