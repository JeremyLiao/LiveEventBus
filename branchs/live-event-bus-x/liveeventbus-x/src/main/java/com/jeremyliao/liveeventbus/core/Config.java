package com.jeremyliao.liveeventbus.core;

import android.content.Context;

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
     * @param active
     * @return
     */
    public Config lifecycleObserverAlwaysActive(boolean active) {
        LiveEventBusCore.get().lifecycleObserverAlwaysActive = active;
        return this;
    }

    /**
     * @param clear
     * @return true: clear livedata when no observer observe it
     * false: not clear livedata unless app was killed
     */
    public Config autoClear(boolean clear) {
        LiveEventBusCore.get().autoClear = clear;
        return this;
    }

    /**
     * config broadcast
     * only if you called this method, you can use broadcastValue() to send broadcast message
     *
     * @param context
     * @return
     */
    public Config supportBroadcast(Context context) {
        LiveEventBusCore.get().registerReceiver(context);
        return this;
    }
}
