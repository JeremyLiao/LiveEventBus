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

public class LiveDataBusDemo extends AppCompatActivity {

    private ActivityLiveDataBusDemoBinding binding;
    private int sendCount = 0;
    private int receiveCount = 0;
    private Observer<String> observer = new Observer<String>() {
        @Override
        public void onChanged(@Nullable String s) {
            Toast.makeText(LiveDataBusDemo.this, s, Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_live_data_bus_demo);
        binding.setHandler(this);
        binding.setLifecycleOwner(this);
        LiveDataBus.get()
                .with("key1", String.class)
                .observe(this, new Observer<String>() {
                    @Override
                    public void onChanged(@Nullable String s) {
                        Toast.makeText(LiveDataBusDemo.this, s, Toast.LENGTH_SHORT).show();
                    }
                });
        LiveDataBus.get()
                .with("key2", String.class)
                .observeForever(observer);
        LiveDataBus.get()
                .with("close_all_page", Boolean.class)
                .observe(this, new Observer<Boolean>() {
                    @Override
                    public void onChanged(@Nullable Boolean b) {
                        if (b) {
                            finish();
                        }
                    }
                });
        LiveDataBus.get()
                .with("multi_thread_count", String.class)
                .observe(this, new Observer<String>() {
                    @Override
                    public void onChanged(@Nullable String s) {
                        receiveCount++;
                    }
                });
        LiveDataBus.get()
                .with("key_active_level", String.class)
                .observe(this, new Observer<String>() {
                    @Override
                    public void onChanged(@Nullable String s) {
                        Toast.makeText(LiveDataBusDemo.this, "Receive message: " + s,
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LiveDataBus.get()
                .with("key2", String.class)
                .removeObserver(observer);
    }

    public void sendMsgBySetValue() {
        Observable.just(new Random())
                .map(new Func1<Random, String>() {
                    @Override
                    public String call(Random random) {
                        return "Message By SetValue: " + random.nextInt(100);
                    }
                })
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        LiveDataBus.get().with("key1").setValue(s);
                    }
                });
    }

    public void sendMsgByPostValue() {
        Observable.just(new Random())
                .map(new Func1<Random, String>() {
                    @Override
                    public String call(Random random) {
                        return "Message By PostValue: " + random.nextInt(100);
                    }
                })
                .subscribeOn(Schedulers.io())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        LiveDataBus.get().with("key1").postValue(s);
                    }
                });
    }

    public void sendMsgToForeverObserver() {
        Observable.just(new Random())
                .map(new Func1<Random, String>() {
                    @Override
                    public String call(Random random) {
                        return "Message To ForeverObserver: " + random.nextInt(100);
                    }
                })
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        LiveDataBus.get().with("key2").setValue(s);
                    }
                });
    }

    public void sendMsgToStickyReceiver() {
        Observable.just(new Random())
                .map(new Func1<Random, String>() {
                    @Override
                    public String call(Random random) {
                        return "Message Sticky: " + random.nextInt(100);
                    }
                })
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        LiveDataBus.get().with("sticky_key").setValue(s);
                    }
                });
    }

    public void startStickyActivity() {
        startActivity(new Intent(this, StickyActivity.class));
    }

    public void startNewActivity() {
        startActivity(new Intent(this, LiveDataBusDemo.class));
    }

    public void closeAll() {
        LiveDataBus.get().with("close_all_page").setValue(true);
    }

    public void postValueCountTest() {
        sendCount = 1000;
        receiveCount = 0;
        ExecutorService threadPool = Executors.newFixedThreadPool(2);
//        ExecutorService threadPool = Executors.newSingleThreadExecutor();
        for (int i = 0; i < sendCount; i++) {
            threadPool.execute(new Runnable() {
                @Override
                public void run() {
                    LiveDataBus.get().with("multi_thread_count").postValue("test_data");
                }
            });
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(LiveDataBusDemo.this, "sendCount: " + sendCount +
                        " | receiveCount: " + receiveCount, Toast.LENGTH_LONG).show();
            }
        }, 1000);
    }

    public void testObserverActiveLevel() {
        startActivity(new Intent(this, ObserverActiveLevelActivity.class));
    }
}
