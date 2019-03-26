package com.jeremyliao.lebapp.app;

import android.app.Application;

import com.jeremyliao.liveeventbus.LiveEventBus;

/**
 * Created by liaohailiang on 2019/3/26.
 */
public class DemoApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        LiveEventBus.get()
                .initConfig()
                .allowBroadcast(this)
                .lifecycleObserverAlwaysActive(true);
    }
}
