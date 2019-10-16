package com.rnkrsoft.embedded.ulwserver;

import java.util.EventListener;

/**
 * Created by rnkrsoft.com on 2019/10/14.
 * 生命周期接口
 */
public interface LifeCycle {
    /**
     * 启动
     */
    void start();

    /**
     * 结束
     */
    void stop();

    /**
     * 是否正在运行
     * @return
     */
    boolean isRunning();

    /**
     * 是否已启动
     * @return
     */
    boolean isStarted();

    /**
     * 是否正在启动
     * @return
     */
    boolean isStarting();

    /**
     * 是否正在停止
     * @return
     */
    boolean isStopping();

    /**
     * 是否已经停止
     * @return
     */
    boolean isStopped();

    /**
     * 是否启动失败
     * @return
     */
    boolean isFailed();

    /**
     * 监听器
     */
    interface Listener extends EventListener {

        void lifeCycleStarting(LifeCycle event);

        void lifeCycleStarted(LifeCycle event);

        void lifeCycleFailure(LifeCycle event, Throwable cause);

        void lifeCycleStopping(LifeCycle event);

        void lifeCycleStopped(LifeCycle event);
    }
}
