package com.jeremyliao.livedatabus;

import android.arch.lifecycle.Observer;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.jeremyliao.livedatabus.databinding.ActivityLiveDataBusDemoBinding;
import com.jeremyliao.livedatabus.databinding.ActivityStickyDemoBinding;

import java.util.Random;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class StickyActivity extends AppCompatActivity {

    private ActivityStickyDemoBinding binding;
    private Observer<String> observer = new Observer<String>() {
        @Override
        public void onChanged(@Nullable String s) {
            binding.tvSticky2.setText("observeStickyForever注册的观察者收到消息: " + s);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_sticky_demo);
        binding.setLifecycleOwner(this);
        LiveDataBus.get()
                .with(LiveDataBusDemo.KEY_TEST_STICKY, String.class)
                .observeSticky(this, new Observer<String>() {
                    @Override
                    public void onChanged(@Nullable String s) {
                        binding.tvSticky1.setText("observeSticky注册的观察者收到消息: " + s);
                    }
                });
        LiveDataBus.get()
                .with(LiveDataBusDemo.KEY_TEST_STICKY, String.class)
                .observeStickyForever(observer);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LiveDataBus.get()
                .with(LiveDataBusDemo.KEY_TEST_STICKY, String.class)
                .removeObserver(observer);
    }
}
