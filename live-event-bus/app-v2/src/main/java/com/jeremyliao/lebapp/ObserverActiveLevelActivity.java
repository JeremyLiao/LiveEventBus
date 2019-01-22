package com.jeremyliao.lebapp;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.jeremyliao.liveeventbus.LiveEventBus;
import com.jeremyliao.lebapp.databinding.ActivityObserverActiveLevelDemoBinding;


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
        LiveEventBus.get()
                .with(LiveEventBusDemo.KEY_TEST_ACTIVE_LEVEL)
                .postValue("Send Msg To Observer Stopped");
    }
}
