package com.jeremyliao.lebapp.activity;

import android.arch.lifecycle.Observer;
import android.databinding.DataBindingUtil;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.jeremyliao.lebapp.LiveEventBusDemo;
import com.jeremyliao.lebapp.R;
import com.jeremyliao.lebapp.databinding.ActivityPostDelayBinding;
import com.jeremyliao.liveeventbus.LiveEventBus;

public class PostDelayActivity extends AppCompatActivity {

    ActivityPostDelayBinding binding;
    private int sendCount = 50;
    private int receiveCount = 0;
    public static final String KEY_TEST_DELAY_LIFE_LONG = "key_test_delay_life_long";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_delay);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_post_delay);
        binding.setLifecycleOwner(this);
        binding.setHandler(this);

        LiveEventBus
                .get(KEY_TEST_DELAY_LIFE_LONG, String.class)
                .observe(this, new Observer<String>() {
                    @Override
                    public void onChanged(@Nullable String s) {
                        Toast.makeText(PostDelayActivity.this,
                                " | receiveCount: " + receiveCount, Toast.LENGTH_SHORT).show();
                        receiveCount++;
                    }
                });

    }

    public void  testDelayNoLife(View view){
        LiveEventBus
                .get(LiveEventBusDemo.KEY_TEST_DELAY_LIFE)
                .postDelay("Send Msg To Observer Delay 2s",2000);
        finish();
    }


    public void testDelayWithLife(View view){
        LiveEventBus
                .get(LiveEventBusDemo.KEY_TEST_DELAY_LIFE)
                .postDelay(this,"Send Msg To Observer Delay 2s",2000);
        finish();
    }

    public void testDelayWithLifeLast(View view){
        for(int i= 0;i< sendCount;i++){
            LiveEventBus
                    .get(KEY_TEST_DELAY_LIFE_LONG)
                    .postDelay(this,"Send " + i + " Msg To Observer Delay 2s",2000);
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(PostDelayActivity.this, "sendCount: " + sendCount +
                        " | receiveCount: " + receiveCount, Toast.LENGTH_LONG).show();
            }
        }, 3000);
    }

}
