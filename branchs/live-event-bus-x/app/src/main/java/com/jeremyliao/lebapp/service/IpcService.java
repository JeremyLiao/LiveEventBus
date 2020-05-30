package com.jeremyliao.lebapp.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;

import com.jeremyliao.lebapp.LiveEventBusDemo;
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
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LiveEventBus
                .get(LiveEventBusDemo.KEY_TEST_BROADCAST, String.class)
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
