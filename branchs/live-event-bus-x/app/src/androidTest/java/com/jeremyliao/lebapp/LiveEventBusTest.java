package com.jeremyliao.lebapp;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;

import com.jeremyliao.lebapp.activity.TestActivity;
import com.jeremyliao.lebapp.helper.LiveEventBusTestHelper;
import com.jeremyliao.lebapp.obj.BadBean;
import com.jeremyliao.lebapp.obj.GoodBean;
import com.jeremyliao.lebapp.obj.SerializableObject;
import com.jeremyliao.lebapp.wrapper.Wrapper;
import com.jeremyliao.liveeventbus.LiveEventBus;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


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
    public void testPostOnMainThread() throws Exception {
        rule.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LiveEventBus.get()
                        .with(TestActivity.KEY_TEST_OBSERVE, String.class)
                        .post("value_test_set_value");
            }
        });
        Thread.sleep(500);
        Assert.assertEquals(rule.getActivity().strResult, "value_test_set_value");
    }

    @Test
    public void testPostOnBackThread() throws Exception {
        LiveEventBus.get()
                .with(TestActivity.KEY_TEST_OBSERVE, String.class)
                .post("value_test_set_value");
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
                        .post("value_test_set_value_forever");
            }
        });
        Thread.sleep(500);
        Assert.assertEquals(rule.getActivity().strResult, "value_test_set_value_forever");
    }

    @Test
    public void testPostValueToObserverForever() throws Exception {
        LiveEventBus.get()
                .with(TestActivity.KEY_TEST_OBSERVE_FOREVER, String.class)
                .post("value_test_post_value_forever");
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
                            .post("test_data");
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
                .post("msg_set_after");
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
                LiveEventBus.get().with(randomKey, String.class).post("msg_set_before");
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
                LiveEventBus.get().with(randomKey, String.class).post("msg_set_before");
                LiveEventBus.get()
                        .with(randomKey, String.class)
                        .observe(rule.getActivity(), new Observer<String>() {
                            @Override
                            public void onChanged(@Nullable String s) {
                                rule.getActivity().strResult = s;
                            }
                        });
                LiveEventBus.get().with(randomKey, String.class).post("msg_set_after");
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
                LiveEventBus.get().with(randomKey, String.class).post("msg_set_before");
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
    public void testPostValueDelay1000() throws Exception {
        LiveEventBus.get()
                .with(TestActivity.KEY_TEST_OBSERVE, String.class)
                .postDelay("value_test_set_value", 1000);
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
                LiveEventBus.get().with(randomKey, String.class).post("msg_set_before");
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
                LiveEventBus.get().with(randomKey, String.class).post("msg_set_before");
                LiveEventBus.get()
                        .with(randomKey, String.class)
                        .observeForever(observer);
                LiveEventBus.get().with(randomKey, String.class).post("msg_set_after");
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
                LiveEventBus.get().with(randomKey, String.class).post("msg_set_before");
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

    @Test
    public void testSendWrongTypeMsg() throws Exception {
        final String key = "key_send_wrong_type";
        rule.getActivity().strResult = "null";
        rule.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LiveEventBus.get()
                        .with(key, String.class)
                        .observe(rule.getActivity(), new Observer<String>() {
                            @Override
                            public void onChanged(@Nullable String s) {
                                rule.getActivity().strResult = s;
                            }
                        });
                LiveEventBus.get().with(key, Integer.class).post(10);

            }
        });
        Thread.sleep(500);
        Assert.assertEquals(rule.getActivity().strResult, "null");
    }

    @Test
    public void testSendWrongTypeMsgToObserverForever() throws Exception {
        final String key = "key_send_wrong_type_forever";
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
                LiveEventBus.get()
                        .with(key, String.class)
                        .observeForever(observer);
                LiveEventBus.get().with(key, Integer.class).post(10);
            }
        });
        Thread.sleep(500);
        rule.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LiveEventBus.get()
                        .with(key, String.class)
                        .removeObserver(observer);
            }
        });
        Assert.assertEquals(rule.getActivity().strResult, "null");
    }

    @Test
    public void testBroadcastStringValue() throws Exception {
        final String key = "key_test_broadcast_string";
        final Wrapper<String> wrapper = new Wrapper<>(null);
        rule.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LiveEventBus.get()
                        .with(key, String.class)
                        .observe(rule.getActivity(), new Observer<String>() {
                            @Override
                            public void onChanged(@Nullable String s) {
                                wrapper.setTarget(s);
                            }
                        });
            }
        });
        Thread.sleep(500);
        LiveEventBus.get()
                .with(key, String.class)
                .broadcast("value_test_broadcast_value");
        Thread.sleep(500);
        Assert.assertEquals(wrapper.getTarget(), "value_test_broadcast_value");
    }

    @Test
    public void testBroadcastIntegerValue() throws Exception {
        final String key = "key_test_broadcast_int";
        final Wrapper<Integer> wrapper = new Wrapper<>(null);
        rule.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LiveEventBus.get()
                        .with(key, Integer.class)
                        .observe(rule.getActivity(), new Observer<Integer>() {
                            @Override
                            public void onChanged(@Nullable Integer s) {
                                wrapper.setTarget(s);
                            }
                        });
            }
        });
        Thread.sleep(500);
        LiveEventBus.get()
                .with(key, Integer.class)
                .broadcast(100);
        Thread.sleep(500);
        Assert.assertEquals(wrapper.getTarget().intValue(), 100);
    }

    @Test
    public void testBroadcastBooleanValue() throws Exception {
        final String key = "key_test_broadcast_boolean";
        final Wrapper<Boolean> wrapper = new Wrapper<>(false);
        rule.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LiveEventBus.get()
                        .with(key, Boolean.class)
                        .observe(rule.getActivity(), new Observer<Boolean>() {
                            @Override
                            public void onChanged(@Nullable Boolean s) {
                                wrapper.setTarget(s);
                            }
                        });
            }
        });
        Thread.sleep(500);
        LiveEventBus.get()
                .with(key, Boolean.class)
                .broadcast(true);
        Thread.sleep(500);
        Assert.assertEquals(wrapper.getTarget(), true);
    }

    @Test
    public void testBroadcastLongValue() throws Exception {
        final String key = "key_test_broadcast_long";
        final Wrapper<Long> wrapper = new Wrapper<>(null);
        rule.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LiveEventBus.get()
                        .with(key, Long.class)
                        .observe(rule.getActivity(), new Observer<Long>() {
                            @Override
                            public void onChanged(@Nullable Long s) {
                                wrapper.setTarget(s);
                            }
                        });
            }
        });
        Thread.sleep(500);
        LiveEventBus.get()
                .with(key, Long.class)
                .broadcast(Long.valueOf(100));
        Thread.sleep(500);
        Assert.assertEquals(wrapper.getTarget(), Long.valueOf(100));
    }

    @Test
    public void testBroadcastFloatValue() throws Exception {
        final String key = "key_test_broadcast_float";
        final Wrapper<Float> wrapper = new Wrapper<>(null);
        rule.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LiveEventBus.get()
                        .with(key, Float.class)
                        .observe(rule.getActivity(), new Observer<Float>() {
                            @Override
                            public void onChanged(@Nullable Float s) {
                                wrapper.setTarget(s);
                            }
                        });
            }
        });
        Thread.sleep(500);
        LiveEventBus.get()
                .with(key, Float.class)
                .broadcast(Float.valueOf(100));
        Thread.sleep(500);
        Assert.assertEquals(wrapper.getTarget(), Float.valueOf(100));
    }

    @Test
    public void testBroadcastSerializableValue() throws Exception {
        final String key = "key_test_broadcast_serializable";
        final Wrapper<SerializableObject> wrapper = new Wrapper<>(null);
        rule.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LiveEventBus.get()
                        .with(key, SerializableObject.class)
                        .observe(rule.getActivity(), new Observer<SerializableObject>() {
                            @Override
                            public void onChanged(@Nullable SerializableObject s) {
                                wrapper.setTarget(s);
                            }
                        });
            }
        });
        Thread.sleep(500);
        LiveEventBus.get()
                .with(key, SerializableObject.class)
                .broadcast(new SerializableObject(100));
        Thread.sleep(500);
        Assert.assertEquals(wrapper.getTarget().getValue(), 100);
    }

    @Test
    public void testBroadcastBundleValue() throws Exception {
        final String key = "key_test_broadcast_bundle";
        final Wrapper<Bundle> wrapper = new Wrapper<>(null);
        rule.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LiveEventBus.get()
                        .with(key, Bundle.class)
                        .observe(rule.getActivity(), new Observer<Bundle>() {
                            @Override
                            public void onChanged(@Nullable Bundle s) {
                                wrapper.setTarget(s);
                            }
                        });
            }
        });
        Thread.sleep(500);
        Bundle bundle = new Bundle();
        bundle.putInt("key_test_int", 100);
        LiveEventBus.get()
                .with(key, Bundle.class)
                .broadcast(bundle);
        Thread.sleep(500);
        Assert.assertEquals(wrapper.getTarget().getInt("key_test_int"), 100);
    }

    @Test
    public void testBroadcastGoodBeanValue() throws Exception {
        final String key = "key_test_broadcast_good_bean";
        final Wrapper<GoodBean> wrapper = new Wrapper<>(null);
        rule.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LiveEventBus.get()
                        .with(key, GoodBean.class)
                        .observe(rule.getActivity(), new Observer<GoodBean>() {
                            @Override
                            public void onChanged(@Nullable GoodBean s) {
                                wrapper.setTarget(s);
                            }
                        });
            }
        });
        Thread.sleep(500);
        GoodBean bean = new GoodBean(100, "hello");
        LiveEventBus.get()
                .with(key, GoodBean.class)
                .broadcast(bean);
        Thread.sleep(500);
        Assert.assertEquals(wrapper.getTarget().getIntValue(), 100);
        Assert.assertEquals(wrapper.getTarget().getStrValue(), "hello");
    }

    @Test
    public void testBroadcastBadBeanValue() throws Exception {
        final String key = "key_test_broadcast_bad_bean";
        final Wrapper<BadBean> wrapper = new Wrapper<>(null);
        rule.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LiveEventBus.get()
                        .with(key, BadBean.class)
                        .observe(rule.getActivity(), new Observer<BadBean>() {
                            @Override
                            public void onChanged(@Nullable BadBean s) {
                                wrapper.setTarget(s);
                            }
                        });
            }
        });
        Thread.sleep(500);
        BadBean bean = new BadBean(100, "hello");
        LiveEventBus.get()
                .with(key, BadBean.class)
                .broadcast(bean);
        Thread.sleep(500);
        Assert.assertEquals(wrapper.getTarget().getIntValue(), 100);
        Assert.assertEquals(wrapper.getTarget().getStrValue(), "hello");
    }

    @Test
    public void testRemoveObserve() throws Exception {
        LiveEventBus.Observable<String> observe = LiveEventBus.get().with("key_test_remove_observe", String.class);
        Map map = (Map) LiveEventBusTestHelper.getLiveEventField("observerMap", observe);
        LiveData liveData = (LiveData) LiveEventBusTestHelper.getLiveEventField("liveData", observe);
        Observer<String> observer = new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
            }
        };
        observe.observeForever(observer);
        Thread.sleep(500);
        Assert.assertEquals(map.size(), 1);
        Assert.assertTrue(liveData.hasActiveObservers());
        Assert.assertTrue(liveData.hasObservers());
        observe.removeObserver(observer);
        Thread.sleep(500);
        Assert.assertEquals(map.size(), 0);
        Assert.assertFalse(liveData.hasActiveObservers());
        Assert.assertFalse(liveData.hasObservers());
    }

    @Test
    public void testAutoRemoveObserve() throws Exception {
        LiveEventBus.Observable<String> observe = LiveEventBus.get().with("key_test_auto_remove_observe", String.class);
        Map map = (Map) LiveEventBusTestHelper.getLiveEventField("observerMap", observe);
        LiveData liveData = (LiveData) LiveEventBusTestHelper.getLiveEventField("liveData", observe);
        observe.observe(rule.getActivity(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
            }
        });
        Thread.sleep(500);
        Assert.assertEquals(map.size(), 0);
        Assert.assertTrue(liveData.hasActiveObservers());
        Assert.assertTrue(liveData.hasObservers());
        rule.getActivity().finish();
        Thread.sleep(500);
        Assert.assertEquals(map.size(), 0);
        Assert.assertFalse(liveData.hasActiveObservers());
        Assert.assertFalse(liveData.hasObservers());
    }
}