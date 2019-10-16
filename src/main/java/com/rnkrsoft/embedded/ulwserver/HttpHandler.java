package com.rnkrsoft.embedded.ulwserver;

import java.io.IOException;

/**
 * Created by rnkrsoft.com on 2019/10/12.
 */
public interface HttpHandler extends LifeCycle, Destroyable{
    UlwServer getServer();
    /**
     * 对传入连接的请求进行处理，是否当前处理器处理了连接由返回值进行判断
     * @param connection 连接对象
     * @return 如果不能处理返回假，如果处理返回真
     * @throws IOException IO异常
     */
    boolean handleRequest(HttpConnection connection) throws IOException;
}
