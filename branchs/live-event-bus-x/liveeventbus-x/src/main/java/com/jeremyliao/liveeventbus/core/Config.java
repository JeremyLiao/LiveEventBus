package com.jeremyliao.liveeventbus.core;

import android.content.Context;

import androidx.annotation.NonNull;

import com.jeremyliao.liveeventbus.logger.Logger;
import com.jeremyliao.liveeventbus.utils.AppUtils;

/**
 * Created by liaohailiang on 2019-08-28.
 */
public class Config {

    /**
     * lifecycleObserverAlwaysActive
     * set if then observer can always receive message
     * true: observer can always receive message
     * false: observer can only receive message when resumed
     *
     * @param active boolean
     * @return Config
     */
    public Config lifecycleObserverAlwaysActive(boolean active) {
        LiveEventBusCore.get().setLifecycleObserverAlwaysActive(active);
        return this;
    }

    /**
     * @param clear boolean
     * @return true: clear livedata when no observer observe it
     * false: not clear livedata unless app was killed
     */
    public Config autoClear(boolean clear) {
        LiveEventBusCore.get().setAutoClear(clear);
        return this;
    }

    /**
     * config broadcast
     * only if you called this method, you can use broadcastValue() to send broadcast message
     *
     * @param context Context
     * @return Config
     */
    public Config setContext(Context context) {
        AppUtils.init(context);
        LiveEventBusCore.get().registerReceiver();
        return this;
    }

    /**
     * setLogger, if not set, use DefaultLogger
     *
     * @param logger Logger
     * @return Config
     */
    public Config setLogger(@NonNull Logger logger) {
        LiveEventBusCore.get().setLogger(logger);
        return this;
    }

    /**
     * set logger enable or disable, default enable
     *
     * @param enable boolean
     * @return Config
     */
    public Config enableLogger(boolean enable) {
        LiveEventBusCore.get().enableLogger(enable);
        return this;
    }
}
