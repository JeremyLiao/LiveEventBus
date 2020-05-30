package com.jeremyliao.lebapp.service;

import android.app.Service;
import android.arch.lifecycle.Observer;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.jeremyliao.lebapp.LiveEventBusDemo;
import com.jeremyliao.lebapp.app.DemoApplication;
import com.jeremyliao.liveeventbus.LiveEventBus;

/**
 * Created by liaohailiang on 2019/3/26.
 */
public class IpcService extends Service {

    @Override
    public void onCreate() {
        super.onCreate();
        LiveEventBus
                .get(LiveEventBusDemo.KEY_TEST_BROADCAST, String.class)
                .observeForever(observer);
        LiveEventBus
                .get(LiveEventBusDemo.KEY_TEST_BROADCAST_IN_APP, String.class)
                .observeForever(observer);
        LiveEventBus
                .get(LiveEventBusDemo.KEY_TEST_BROADCAST_GLOBAL, String.class)
                .observeForever(observer);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LiveEventBus
                .get(LiveEventBusDemo.KEY_TEST_BROADCAST, String.class)
                .removeObserver(observer);
        LiveEventBus
                .get(LiveEventBusDemo.KEY_TEST_BROADCAST_IN_APP, String.class)
                .removeObserver(observer);
        LiveEventBus
                .get(LiveEventBusDemo.KEY_TEST_BROADCAST_GLOBAL, String.class)
                .removeObserver(observer);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private Observer<String> observer = new Observer<String>() {
        @Override
        public void onChanged(@Nullable String s) {
            Toast.makeText(IpcService.this, s, Toast.LENGTH_SHORT).show();
        }
    };
}
