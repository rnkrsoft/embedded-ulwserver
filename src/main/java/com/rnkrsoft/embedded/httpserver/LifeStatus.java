package com.rnkrsoft.embedded.httpserver;

/**
 * Created by rnkrsoft.com on 2019/10/14.
 */
public enum LifeStatus {
    /**
     * 启动失败
     */
    FAILED,
    /**
     * 已停止
     */
    STOPPED,
    /**
     * 启动中
     */
    STARTING,
    /**
     * 已启动
     */
    STARTED,
    /**
     * 停止处理中
     */
    STOPPING;
}
