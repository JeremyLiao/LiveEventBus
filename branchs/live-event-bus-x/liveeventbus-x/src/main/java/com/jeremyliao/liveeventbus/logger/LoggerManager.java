package com.jeremyliao.liveeventbus.logger;

import java.util.logging.Level;

/**
 * Created by liaohailiang on 2019-11-07.
 */
public class LoggerManager implements Logger {

    private Logger logger;
    private boolean enable = true;

    public LoggerManager(Logger logger) {
        this.logger = logger;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public Logger getLogger() {
        return logger;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void log(Level level, String msg) {
        if (enable) {
            logger.log(level, msg);
        }
    }

    @Override
    public void log(Level level, String msg, Throwable th) {
        if (enable) {
            logger.log(level, msg, th);
        }
    }
}
