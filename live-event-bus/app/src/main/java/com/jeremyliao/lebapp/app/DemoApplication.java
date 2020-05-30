package com.jeremyliao.lebapp.app;

import android.app.Application;
import android.util.Log;

import com.jeremyliao.liveeventbus.LiveEventBus;

/**
 * Created by liaohailiang on 2019/3/26.
 */
public class DemoApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("LiveEventBus", "DemoApplication.this: " + DemoApplication.this);
        LiveEventBus
                .config()
                .lifecycleObserverAlwaysActive(true);
    }
}
