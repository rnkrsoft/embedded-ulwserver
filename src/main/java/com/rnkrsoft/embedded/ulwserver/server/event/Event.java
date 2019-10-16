package com.rnkrsoft.embedded.ulwserver.server.event;

import com.rnkrsoft.embedded.ulwserver.HttpConnection;
import lombok.Getter;

/**
 * Created by rnkrsoft.com on 2019/10/10.
 * 容器事件
 */
public class Event {
    @Getter
    protected final HttpConnection connection;
    protected Event(HttpConnection connection){
        this.connection = connection;
    }
}
