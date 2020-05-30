package com.jeremyliao.lebapp.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;

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
        LiveEventBus
                .get(KEY_TEST_OBSERVE, String.class)
                .observe(this, new Observer<String>() {
                    @Override
                    public void onChanged(@Nullable String s) {
                        strResult = s;
                    }
                });
        LiveEventBus
                .get(KEY_TEST_OBSERVE_FOREVER, String.class)
                .observeForever(observer);
        LiveEventBus
                .get(KEY_TEST_MULTI_THREAD_POST, String.class)
                .observe(this, new Observer<String>() {
                    @Override
                    public void onChanged(@Nullable String s) {
                        receiveCount++;
                    }
                });
        LiveEventBus
                .get(KEY_TEST_BROADCAST_VALUE, String.class)
                .observe(this, new Observer<String>() {
                    @Override
                    public void onChanged(@Nullable String s) {
                        strResult = s;
                    }
                });
        testMessageSetBeforeOnCreate();
    }

    private void testMessageSetBeforeOnCreate() {
        LiveEventBus.get(KEY_TEST_MSG_SET_BEFORE_ON_CREATE, String.class).post("msg_set_before");
        LiveEventBus
                .get(KEY_TEST_MSG_SET_BEFORE_ON_CREATE, String.class)
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
        LiveEventBus
                .get(KEY_TEST_OBSERVE_FOREVER, String.class)
                .removeObserver(observer);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == IpcTestActivity.RESULT_CODE) {
            strResult = data.getStringExtra(IpcTestActivity.RESULT_EXTRA_KEY);
        }
    }
}
