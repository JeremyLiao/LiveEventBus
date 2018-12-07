package com.jeremyliao.livedatabus;

import android.arch.lifecycle.Observer;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.jeremyliao.livedatabus.databinding.ActivityLiveDataBusDemoBinding;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class TestActivity extends AppCompatActivity {

    public static final String KEY_TEST_OBSERVE = "key_test_observe";
    public static final String KEY_TEST_OBSERVE_FOREVER = "key_test_observe_forever";
    public static final String KEY_TEST_MULTI_THREAD_POST = "key_test_multi_thread_post";
    public static final String KEY_TEST_MSG_SET_BEFORE_ON_CREATE = "key_test_msg_set_before_on_create";

    public boolean receiveMsgSetBeforeOnCreate = false;
    public String strResult = null;
    public int receiveCount = 0;

    private Observer<String> observer = new Observer<String>() {
        @Override
        public void onChanged(@Nullable String s) {
            strResult = s;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LiveDataBus.get()
                .with(KEY_TEST_OBSERVE, String.class)
                .observe(this, new Observer<String>() {
                    @Override
                    public void onChanged(@Nullable String s) {
                        strResult = s;
                    }
                });
        LiveDataBus.get()
                .with(KEY_TEST_OBSERVE_FOREVER, String.class)
                .observeForever(observer);
        LiveDataBus.get()
                .with(KEY_TEST_MULTI_THREAD_POST, String.class)
                .observe(this, new Observer<String>() {
                    @Override
                    public void onChanged(@Nullable String s) {
                        receiveCount++;
                    }
                });
        testMessageSetBeforeOnCreate();
    }

    private void testMessageSetBeforeOnCreate() {
        LiveDataBus.get().with(KEY_TEST_MSG_SET_BEFORE_ON_CREATE, String.class).setValue("msg_set_before");
        LiveDataBus.get()
                .with(KEY_TEST_MSG_SET_BEFORE_ON_CREATE, String.class)
                .observe(this, new Observer<String>() {
                    @Override
                    public void onChanged(@Nullable String s) {
                        receiveMsgSetBeforeOnCreate = true;
                        strResult = s;
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LiveDataBus.get()
                .with(KEY_TEST_OBSERVE_FOREVER, String.class)
                .removeObserver(observer);
    }
}
