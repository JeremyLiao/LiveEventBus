package com.jeremyliao.lebapp;

import android.arch.lifecycle.Observer;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.jeremyliao.lebapp.activity.ObserverActiveLevelActivity;
import com.jeremyliao.lebapp.activity.StickyActivity;
import com.jeremyliao.lebapp.databinding.ActivityLiveDataBusDemoBinding;
import com.jeremyliao.lebapp.service.IpcService;
import com.jeremyliao.liveeventbus.LiveEventBus;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class LiveEventBusDemo extends AppCompatActivity {
    public static final String KEY_TEST_OBSERVE = "key_test_observe";
    public static final String KEY_TEST_OBSERVE_FOREVER = "key_test_observe_forever";
    public static final String KEY_TEST_STICKY = "key_test_sticky";
    public static final String KEY_TEST_MULTI_THREAD_POST = "key_test_multi_thread_post";
    public static final String KEY_TEST_MSG_SET_BEFORE_ON_CREATE = "key_test_msg_set_before_on_create";
    public static final String KEY_TEST_CLOSE_ALL_PAGE = "key_test_close_all_page";
    public static final String KEY_TEST_ACTIVE_LEVEL = "key_test_active_level";
    public static final String KEY_TEST_BROADCAST = "key_test_broadcast";


    private int sendCount = 0;
    private int receiveCount = 0;
    private String randomKey = null;

    private ActivityLiveDataBusDemoBinding binding;

    private Observer<String> observer = new Observer<String>() {
        @Override
        public void onChanged(@Nullable String s) {
            Toast.makeText(LiveEventBusDemo.this, s, Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startService(new Intent(this, IpcService.class));
        binding = DataBindingUtil.setContentView(this, R.layout.activity_live_data_bus_demo);
        binding.setHandler(this);
        binding.setLifecycleOwner(this);
        LiveEventBus.get()
                .with(KEY_TEST_OBSERVE, String.class)
                .observe(this, new Observer<String>() {
                    @Override
                    public void onChanged(@Nullable String s) {
                        Toast.makeText(LiveEventBusDemo.this, s, Toast.LENGTH_SHORT).show();
                    }
                });
        LiveEventBus.get()
                .with(KEY_TEST_OBSERVE_FOREVER, String.class)
                .observeForever(observer);
        LiveEventBus.get()
                .with(KEY_TEST_CLOSE_ALL_PAGE, Boolean.class)
                .observe(this, new Observer<Boolean>() {
                    @Override
                    public void onChanged(@Nullable Boolean b) {
                        if (b) {
                            finish();
                        }
                    }
                });
        LiveEventBus.get()
                .with(KEY_TEST_MULTI_THREAD_POST, String.class)
                .observe(this, new Observer<String>() {
                    @Override
                    public void onChanged(@Nullable String s) {
                        receiveCount++;
                    }
                });
        LiveEventBus.get()
                .with(KEY_TEST_ACTIVE_LEVEL, String.class)
                .observe(this, new Observer<String>() {
                    @Override
                    public void onChanged(@Nullable String s) {
                        Toast.makeText(LiveEventBusDemo.this, "Receive message: " + s,
                                Toast.LENGTH_SHORT).show();
                    }
                });
        testMessageSetBeforeOnCreate();
    }

    private void testMessageSetBeforeOnCreate() {
        //先发出一个消息
        LiveEventBus.get().with(KEY_TEST_MSG_SET_BEFORE_ON_CREATE, String.class).post("msg set before");
        //然后订阅这个消息
        LiveEventBus.get()
                .with(KEY_TEST_MSG_SET_BEFORE_ON_CREATE, String.class)
                .observe(this, new Observer<String>() {
                    @Override
                    public void onChanged(@Nullable String s) {
                        Toast.makeText(LiveEventBusDemo.this, s, Toast.LENGTH_SHORT).show();
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
                        LiveEventBus.get().with(KEY_TEST_OBSERVE).post(s);
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
                        LiveEventBus.get().with(KEY_TEST_OBSERVE_FOREVER).post(s);
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
                        LiveEventBus.get().with(KEY_TEST_STICKY).post(s);
                    }
                });
    }

    public void startStickyActivity() {
        startActivity(new Intent(this, StickyActivity.class));
    }

    public void startNewActivity() {
        startActivity(new Intent(this, LiveEventBusDemo.class));
    }

    public void closeAll() {
        LiveEventBus.get().with(KEY_TEST_CLOSE_ALL_PAGE).post(true);
    }

    public void postValueCountTest() {
        sendCount = 1000;
        receiveCount = 0;
        ExecutorService threadPool = Executors.newFixedThreadPool(2);
        for (int i = 0; i < sendCount; i++) {
            threadPool.execute(new Runnable() {
                @Override
                public void run() {
                    LiveEventBus.get().with(KEY_TEST_MULTI_THREAD_POST).post("test_data");
                }
            });
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(LiveEventBusDemo.this, "sendCount: " + sendCount +
                        " | receiveCount: " + receiveCount, Toast.LENGTH_LONG).show();
            }
        }, 1000);
    }

    public void testMessageSetBefore() {
        //先动态生成一个key
        randomKey = "key_random_" + new Random().nextInt();
        //然后发出一个消息
        LiveEventBus.get().with(randomKey, String.class).post("msg set before");
        //然后订阅这个消息
        LiveEventBus.get()
                .with(randomKey, String.class)
                .observe(this, new Observer<String>() {
                    @Override
                    public void onChanged(@Nullable String s) {
                        Toast.makeText(LiveEventBusDemo.this, s, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void sendMessageSetBefore() {
        LiveEventBus.get().with(randomKey, String.class).post("msg set after");
    }

    public void testObserverActiveLevel() {
        startActivity(new Intent(this, ObserverActiveLevelActivity.class));
    }

    public void testBroadcast() {
        LiveEventBus.get()
                .with(KEY_TEST_BROADCAST)
                .broadcast("broadcast msg");
    }
}
