package com.jeremyliao.lebapp.activity;

import android.arch.lifecycle.Observer;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.jeremyliao.liveeventbus.LiveEventBus;

public class TestActivity extends AppCompatActivity {

    public static final String KEY_TEST_OBSERVE = "key_test_observe";
    public static final String KEY_TEST_OBSERVE_FOREVER = "key_test_observe_forever";
    public static final String KEY_TEST_MULTI_THREAD_POST = "key_test_multi_thread_post";
    public static final String KEY_TEST_MSG_SET_BEFORE_ON_CREATE = "key_test_msg_set_before_on_create";
    public static final String KEY_TEST_BROADCAST_VALUE = "key_test_broadcast_value";

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
        LiveEventBus.get()
                .with(KEY_TEST_OBSERVE, String.class)
                .observe(this, new Observer<String>() {
                    @Override
                    public void onChanged(@Nullable String s) {
                        strResult = s;
                    }
                });
        LiveEventBus.get()
                .with(KEY_TEST_OBSERVE_FOREVER, String.class)
                .observeForever(observer);
        LiveEventBus.get()
                .with(KEY_TEST_MULTI_THREAD_POST, String.class)
                .observe(this, new Observer<String>() {
                    @Override
                    public void onChanged(@Nullable String s) {
                        receiveCount++;
                    }
                });
        LiveEventBus.get()
                .with(KEY_TEST_BROADCAST_VALUE, String.class)
                .observe(this, new Observer<String>() {
                    @Override
                    public void onChanged(@Nullable String s) {
                        strResult = s;
                    }
                });
        testMessageSetBeforeOnCreate();
    }

    private void testMessageSetBeforeOnCreate() {
        LiveEventBus.get().with(KEY_TEST_MSG_SET_BEFORE_ON_CREATE, String.class).post("msg_set_before");
        LiveEventBus.get()
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
        LiveEventBus.get()
                .with(KEY_TEST_OBSERVE_FOREVER, String.class)
                .removeObserver(observer);
    }
}
