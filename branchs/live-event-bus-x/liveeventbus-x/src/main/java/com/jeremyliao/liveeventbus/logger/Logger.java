package com.jeremyliao.liveeventbus.logger;

import java.util.logging.Level;

/**
 * Created by liaohailiang on 2019-09-10.
 */
public interface Logger {

    void log(Level level, String msg);

    void log(Level level, String msg, Throwable th);
}
