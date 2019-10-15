package com.rnkrsoft.embedded.httpserver.server.io;

/**
 * Created by rnkrsoft.com on 2019/10/12.
 * 到达流截止位置时回调
 */
public interface EndStreamCallback {
    /**
     * 处理操作
     */
    void handle();
}
