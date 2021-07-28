package com.tmax.WaplMath.Common.util.context;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.LoggerContextListener;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.spi.LifeCycle;

public class LogBackContextListener extends ContextAwareBase implements LoggerContextListener, LifeCycle{
    private boolean started = false;

    @Override
    public void start() {
        if(started) return;

        String hostname = System.getenv("HOSTNAME");

        Context context = getContext();

        //Put contexts
        context.putProperty("HOST_NAME", hostname);

        started = true;
    }

    @Override
    public void stop() {
    }

    @Override
    public boolean isStarted() {
        return this.started;
    }

    @Override
    public boolean isResetResistant() {
        return true;
    }

    @Override
    public void onStart(LoggerContext context) {
    }

    @Override
    public void onStop(LoggerContext context) {
    }

    @Override
    public void onReset(LoggerContext context) {
    }

    @Override
    public void onLevelChange(Logger logger, Level level) {
    }

}
