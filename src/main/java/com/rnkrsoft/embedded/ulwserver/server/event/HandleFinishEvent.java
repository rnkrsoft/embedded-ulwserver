package com.rnkrsoft.embedded.ulwserver.server.event;

import com.rnkrsoft.embedded.ulwserver.HttpConnection;

/**
 * Created by rnkrsoft.com on 2019/10/10.
 * 重置事件
 */
public class HandleFinishEvent extends Event{
    public HandleFinishEvent(HttpConnection connection) {
        super(connection);
    }
}
