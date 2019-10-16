package com.rnkrsoft.embedded.ulwserver.server;

import com.rnkrsoft.embedded.ulwserver.LifeCycle;
import com.rnkrsoft.embedded.ulwserver.LifeStatus;

/**
 * Created by rnkrsoft.com on 2019/10/14.
 * 抽象的生命周期类
 */
public abstract class AbstractLifeCycle implements LifeCycle {
    protected LifeStatus status = LifeStatus.STOPPED;
    @Override
    public boolean isRunning() {
        return status == LifeStatus.STARTING || status == LifeStatus.STARTED;
    }

    @Override
    public boolean isStarted() {
        return status == LifeStatus.STARTED;
    }

    @Override
    public boolean isStarting() {
        return status == LifeStatus.STARTING;
    }

    @Override
    public boolean isStopping() {
        return status == LifeStatus.STOPPING;
    }

    @Override
    public boolean isStopped() {
        return status == LifeStatus.STOPPED;
    }

    @Override
    public boolean isFailed() {
        return status == LifeStatus.FAILED;
    }
}
