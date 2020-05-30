package com.jeremyliao.lebapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;

import com.jeremyliao.liveeventbus.LiveEventBus;

public class IpcTestActivity extends AppCompatActivity {

    public static final String KEY_TEST_IPC_OBSERVE = "key_test_ipc_observe";

    public static final int RESULT_CODE = 0;
    public static final String RESULT_EXTRA_KEY = "result_extra_key";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LiveEventBus
                .get(KEY_TEST_IPC_OBSERVE, Object.class)
                .observe(this, new Observer<Object>() {
                    @Override
                    public void onChanged(@Nullable Object o) {
                        Toast.makeText(IpcTestActivity.this, o + "", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent();
                        intent.putExtra(RESULT_EXTRA_KEY, o + "");
                        setResult(RESULT_CODE, intent);
                        finish();
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
