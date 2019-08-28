package com.jeremyliao.lebapp.activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.jeremyliao.lebapp.LiveEventBusDemo;
import com.jeremyliao.lebapp.R;
import com.jeremyliao.lebapp.databinding.ActivityObserverActiveLevelDemoBinding;
import com.jeremyliao.liveeventbus.LiveEventBus;


public class ObserverActiveLevelActivity extends AppCompatActivity {

    private ActivityObserverActiveLevelDemoBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_observer_active_level_demo);
        binding.setLifecycleOwner(this);
        binding.setHandler(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void sendMsgToPrevent() {
        LiveEventBus
                .get(LiveEventBusDemo.KEY_TEST_ACTIVE_LEVEL)
                .post("Send Msg To Observer Stopped");
    }
}
