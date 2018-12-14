package com.jeremyliao.liveeventbus;

import android.arch.lifecycle.Observer;
import android.support.annotation.Nullable;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

/**
 * Created by liaohailiang on 2018/12/6.
 */
@RunWith(AndroidJUnit4.class)
public class LiveEventBusTest {

    @Rule
    public ActivityTestRule<TestActivity> rule = new ActivityTestRule<>(TestActivity.class);

    @Before
    public void setUp() throws Exception {
        rule.getActivity().strResult = null;
        rule.getActivity().receiveCount = 0;
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testSetValue() throws Exception {
        rule.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LiveEventBus.get()
                        .with(TestActivity.KEY_TEST_OBSERVE, String.class)
                        .setValue("value_test_set_value");
            }
        });
        Thread.sleep(500);
        Assert.assertEquals(rule.getActivity().strResult, "value_test_set_value");
    }

    @Test
    public void testPostValue() throws Exception {
        LiveEventBus.get()
                .with(TestActivity.KEY_TEST_OBSERVE, String.class)
                .postValue("value_test_set_value");
        Thread.sleep(500);
        Assert.assertEquals(rule.getActivity().strResult, "value_test_set_value");
    }

    @Test
    public void testSetValueToObserverForever() throws Exception {
        rule.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LiveEventBus.get()
                        .with(TestActivity.KEY_TEST_OBSERVE_FOREVER, String.class)
                        .setValue("value_test_set_value_forever");
            }
        });
        Thread.sleep(500);
        Assert.assertEquals(rule.getActivity().strResult, "value_test_set_value_forever");
    }

    @Test
    public void testPostValueToObserverForever() throws Exception {
        LiveEventBus.get()
                .with(TestActivity.KEY_TEST_OBSERVE_FOREVER, String.class)
                .postValue("value_test_post_value_forever");
        Thread.sleep(500);
        Assert.assertEquals(rule.getActivity().strResult, "value_test_post_value_forever");
    }

    @Test
    public void testPostValueInMultiThread() throws Exception {
        int sendCount = 1000;
        ExecutorService threadPool = Executors.newFixedThreadPool(2);
        for (int i = 0; i < sendCount; i++) {
            threadPool.execute(new Runnable() {
                @Override
                public void run() {
                    LiveEventBus.get()
                            .with(TestActivity.KEY_TEST_MULTI_THREAD_POST)
                            .postValue("test_data");
                }
            });
        }
        Thread.sleep(1000);
        Assert.assertEquals(rule.getActivity().receiveCount, sendCount);
    }

    @Test
    public void testSendMsgBeforeObserveOnCreate() throws Exception {
        Assert.assertFalse(rule.getActivity().receiveMsgSetBeforeOnCreate);
        Assert.assertNull(rule.getActivity().strResult);
    }

    @Test
    public void testSendMsgBeforeAndAfterObserveOnCreate() throws Exception {
        LiveEventBus.get()
                .with(TestActivity.KEY_TEST_MSG_SET_BEFORE_ON_CREATE, String.class)
                .postValue("msg_set_after");
        Thread.sleep(500);
        Assert.assertTrue(rule.getActivity().receiveMsgSetBeforeOnCreate);
        Assert.assertEquals(rule.getActivity().strResult, "msg_set_after");
    }

    @Test
    public void testSendMsgBeforeObserve() throws Exception {
        final String randomKey = "key_random_" + new Random().nextInt();
        rule.getActivity().strResult = "null";
        rule.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LiveEventBus.get().with(randomKey, String.class).setValue("msg_set_before");
                LiveEventBus.get()
                        .with(randomKey, String.class)
                        .observe(rule.getActivity(), new Observer<String>() {
                            @Override
                            public void onChanged(@Nullable String s) {
                                rule.getActivity().strResult = s;
                            }
                        });
            }
        });
        Thread.sleep(500);
        Assert.assertEquals(rule.getActivity().strResult, "null");
    }

    @Test
    public void testSendMsgBeforeAndAfterObserve() throws Exception {
        final String randomKey = "key_random_" + new Random().nextInt();
        rule.getActivity().strResult = "null";
        rule.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LiveEventBus.get().with(randomKey, String.class).setValue("msg_set_before");
                LiveEventBus.get()
                        .with(randomKey, String.class)
                        .observe(rule.getActivity(), new Observer<String>() {
                            @Override
                            public void onChanged(@Nullable String s) {
                                rule.getActivity().strResult = s;
                            }
                        });
                LiveEventBus.get().with(randomKey, String.class).setValue("msg_set_after");
            }
        });
        Thread.sleep(500);
        Assert.assertEquals(rule.getActivity().strResult, "msg_set_after");
    }

    @Test
    public void testObserveSticky() throws Exception {
        final String randomKey = "key_random_" + new Random().nextInt();
        rule.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LiveEventBus.get().with(randomKey, String.class).setValue("msg_set_before");
                LiveEventBus.get()
                        .with(randomKey, String.class)
                        .observeSticky(rule.getActivity(), new Observer<String>() {
                            @Override
                            public void onChanged(@Nullable String s) {
                                rule.getActivity().strResult = s;
                            }
                        });
            }
        });
        Thread.sleep(500);
        Assert.assertEquals(rule.getActivity().strResult, "msg_set_before");
    }

    @Test
    public void testPostValueDelay() throws Exception {
        LiveEventBus.get()
                .with(TestActivity.KEY_TEST_OBSERVE, String.class)
                .postValueDelay("value_test_set_value", 1000, TimeUnit.MILLISECONDS);
        Thread.sleep(500);
        Assert.assertNull(rule.getActivity().strResult);
        Thread.sleep(1000);
        Assert.assertEquals(rule.getActivity().strResult, "value_test_set_value");
    }

    @Test
    public void testSendMsgBeforeObserveForever() throws Exception {
        final String randomKey = "key_random_" + new Random().nextInt();
        rule.getActivity().strResult = "null";
        final Observer<String> observer = new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                rule.getActivity().strResult = s;
            }
        };
        rule.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LiveEventBus.get().with(randomKey, String.class).setValue("msg_set_before");
                LiveEventBus.get()
                        .with(randomKey, String.class)
                        .observeForever(observer);
            }
        });
        Thread.sleep(500);
        Assert.assertEquals(rule.getActivity().strResult, "null");
        rule.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LiveEventBus.get()
                        .with(randomKey, String.class)
                        .removeObserver(observer);
            }
        });
    }

    @Test
    public void testSendMsgBeforeAndAfterObserveForever() throws Exception {
        final String randomKey = "key_random_" + new Random().nextInt();
        rule.getActivity().strResult = "null";
        final Observer<String> observer = new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                rule.getActivity().strResult = s;
            }
        };
        rule.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LiveEventBus.get().with(randomKey, String.class).setValue("msg_set_before");
                LiveEventBus.get()
                        .with(randomKey, String.class)
                        .observeForever(observer);
                LiveEventBus.get().with(randomKey, String.class).setValue("msg_set_after");
            }
        });
        Thread.sleep(500);
        Assert.assertEquals(rule.getActivity().strResult, "msg_set_after");
        rule.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LiveEventBus.get()
                        .with(randomKey, String.class)
                        .removeObserver(observer);
            }
        });
    }

    @Test
    public void testObserveStickyForever() throws Exception {
        final String randomKey = "key_random_" + new Random().nextInt();
        rule.getActivity().strResult = "null";
        final Observer<String> observer = new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                rule.getActivity().strResult = s;
            }
        };
        rule.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LiveEventBus.get().with(randomKey, String.class).setValue("msg_set_before");
                LiveEventBus.get()
                        .with(randomKey, String.class)
                        .observeStickyForever(observer);
            }
        });
        Thread.sleep(500);
        Assert.assertEquals(rule.getActivity().strResult, "msg_set_before");
        rule.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LiveEventBus.get()
                        .with(randomKey, String.class)
                        .removeObserver(observer);
            }
        });
    }

}