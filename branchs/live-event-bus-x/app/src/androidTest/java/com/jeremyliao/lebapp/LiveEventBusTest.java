package com.jeremyliao.lebapp;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;

import com.jeremyliao.lebapp.activity.TestActivity;
import com.jeremyliao.lebapp.helper.LiveEventBusTestHelper;
import com.jeremyliao.lebapp.obj.GoodBean;
import com.jeremyliao.lebapp.obj.SerializableObject;
import com.jeremyliao.lebapp.wrapper.Wrapper;
import com.jeremyliao.liveeventbus.LiveEventBus;
import com.jeremyliao.liveeventbus.core.Observable;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


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
                LiveEventBus
                        .get(TestActivity.KEY_TEST_OBSERVE, String.class)
                        .post("value_test_set_value");
            }
        });
        Thread.sleep(500);
        Assert.assertEquals(rule.getActivity().strResult, "value_test_set_value");
    }

    @Test
    public void testPostOnBackThread() throws Exception {
        LiveEventBus
                .get(TestActivity.KEY_TEST_OBSERVE, String.class)
                .post("value_test_set_value");
        Thread.sleep(500);
        Assert.assertEquals(rule.getActivity().strResult, "value_test_set_value");
    }

    @Test
    public void testSetValueToObserverForever() throws Exception {
        rule.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LiveEventBus
                        .get(TestActivity.KEY_TEST_OBSERVE_FOREVER, String.class)
                        .post("value_test_set_value_forever");
            }
        });
        Thread.sleep(500);
        Assert.assertEquals(rule.getActivity().strResult, "value_test_set_value_forever");
    }

    @Test
    public void testPostValueToObserverForever() throws Exception {
        LiveEventBus
                .get(TestActivity.KEY_TEST_OBSERVE_FOREVER, String.class)
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
                    LiveEventBus
                            .get(TestActivity.KEY_TEST_MULTI_THREAD_POST)
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
        LiveEventBus
                .get(TestActivity.KEY_TEST_MSG_SET_BEFORE_ON_CREATE, String.class)
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
                LiveEventBus.get(randomKey, String.class).post("msg_set_before");
                LiveEventBus
                        .get(randomKey, String.class)
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
                LiveEventBus.get(randomKey, String.class).post("msg_set_before");
                LiveEventBus
                        .get(randomKey, String.class)
                        .observe(rule.getActivity(), new Observer<String>() {
                            @Override
                            public void onChanged(@Nullable String s) {
                                rule.getActivity().strResult = s;
                            }
                        });
                LiveEventBus.get(randomKey, String.class).post("msg_set_after");
            }
        });
        Thread.sleep(500);
        Assert.assertEquals(rule.getActivity().strResult, "msg_set_after");
    }

    @Test
    public void testObserveSticky() throws Exception {
        final String randomKey = "key_random_" + new Random().nextInt();
        LiveEventBus.get(randomKey, String.class).post("msg_set_before");
        Thread.sleep(500);
        LiveEventBus
                .get(randomKey, String.class)
                .observeSticky(rule.getActivity(), new Observer<String>() {
                    @Override
                    public void onChanged(@Nullable String s) {
                        rule.getActivity().strResult = s;
                    }
                });
        Thread.sleep(500);
        Assert.assertEquals(rule.getActivity().strResult, "msg_set_before");
    }

    @Test
    public void testPostValueDelay1000() throws Exception {
        LiveEventBus
                .get(TestActivity.KEY_TEST_OBSERVE, String.class)
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
                LiveEventBus.get(randomKey, String.class).post("msg_set_before");
                LiveEventBus
                        .get(randomKey, String.class)
                        .observeForever(observer);
            }
        });
        Thread.sleep(500);
        Assert.assertEquals(rule.getActivity().strResult, "null");
        rule.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LiveEventBus
                        .get(randomKey, String.class)
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
                LiveEventBus.get(randomKey, String.class).post("msg_set_before");
                LiveEventBus
                        .get(randomKey, String.class)
                        .observeForever(observer);
                LiveEventBus.get(randomKey, String.class).post("msg_set_after");
            }
        });
        Thread.sleep(500);
        Assert.assertEquals(rule.getActivity().strResult, "msg_set_after");
        rule.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LiveEventBus
                        .get(randomKey, String.class)
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
                LiveEventBus.get(randomKey, String.class).post("msg_set_before");
                LiveEventBus
                        .get(randomKey, String.class)
                        .observeStickyForever(observer);
            }
        });
        Thread.sleep(500);
        Assert.assertEquals(rule.getActivity().strResult, "msg_set_before");
        rule.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LiveEventBus
                        .get(randomKey, String.class)
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
                LiveEventBus
                        .get(key, String.class)
                        .observe(rule.getActivity(), new Observer<String>() {
                            @Override
                            public void onChanged(@Nullable String s) {
                                rule.getActivity().strResult = s;
                            }
                        });
                LiveEventBus.get(key, Integer.class).post(10);

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
                LiveEventBus
                        .get(key, String.class)
                        .observeForever(observer);
                LiveEventBus.get(key, Integer.class).post(10);
            }
        });
        Thread.sleep(500);
        rule.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LiveEventBus
                        .get(key, String.class)
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
                LiveEventBus
                        .get(key, String.class)
                        .observe(rule.getActivity(), new Observer<String>() {
                            @Override
                            public void onChanged(@Nullable String s) {
                                wrapper.setTarget(s);
                            }
                        });
            }
        });
        Thread.sleep(500);
        LiveEventBus
                .get(key, String.class)
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
                LiveEventBus
                        .get(key, Integer.class)
                        .observe(rule.getActivity(), new Observer<Integer>() {
                            @Override
                            public void onChanged(@Nullable Integer s) {
                                wrapper.setTarget(s);
                            }
                        });
            }
        });
        Thread.sleep(500);
        LiveEventBus
                .get(key, Integer.class)
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
                LiveEventBus
                        .get(key, Boolean.class)
                        .observe(rule.getActivity(), new Observer<Boolean>() {
                            @Override
                            public void onChanged(@Nullable Boolean s) {
                                wrapper.setTarget(s);
                            }
                        });
            }
        });
        Thread.sleep(500);
        LiveEventBus
                .get(key, Boolean.class)
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
                LiveEventBus
                        .get(key, Long.class)
                        .observe(rule.getActivity(), new Observer<Long>() {
                            @Override
                            public void onChanged(@Nullable Long s) {
                                wrapper.setTarget(s);
                            }
                        });
            }
        });
        Thread.sleep(500);
        LiveEventBus
                .get(key, Long.class)
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
                LiveEventBus
                        .get(key, Float.class)
                        .observe(rule.getActivity(), new Observer<Float>() {
                            @Override
                            public void onChanged(@Nullable Float s) {
                                wrapper.setTarget(s);
                            }
                        });
            }
        });
        Thread.sleep(500);
        LiveEventBus
                .get(key, Float.class)
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
                LiveEventBus
                        .get(key, SerializableObject.class)
                        .observe(rule.getActivity(), new Observer<SerializableObject>() {
                            @Override
                            public void onChanged(@Nullable SerializableObject s) {
                                wrapper.setTarget(s);
                            }
                        });
            }
        });
        Thread.sleep(500);
        LiveEventBus
                .get(key, SerializableObject.class)
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
                LiveEventBus
                        .get(key, Bundle.class)
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
        LiveEventBus
                .get(key, Bundle.class)
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
                LiveEventBus
                        .get(key, GoodBean.class)
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
        LiveEventBus
                .get(key, GoodBean.class)
                .broadcast(bean);
        Thread.sleep(500);
        Assert.assertEquals(wrapper.getTarget().getIntValue(), 100);
        Assert.assertEquals(wrapper.getTarget().getStrValue(), "hello");
    }

    @Test
    public void testBroadcastStringValueForeground() throws Exception {
        final String key = "key_test_broadcast_string_foreground";
        final Wrapper<String> wrapper = new Wrapper<>(null);
        rule.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LiveEventBus
                        .get(key, String.class)
                        .observe(rule.getActivity(), new Observer<String>() {
                            @Override
                            public void onChanged(@Nullable String s) {
                                wrapper.setTarget(s);
                            }
                        });
            }
        });
        Thread.sleep(500);
        LiveEventBus
                .get(key, String.class)
                .broadcast("value_test_broadcast_value", true, false);
        Thread.sleep(500);
        Assert.assertEquals(wrapper.getTarget(), "value_test_broadcast_value");
    }

    @Test
    public void testRemoveObserve() throws Exception {
        Observable<String> observe = LiveEventBus.get("key_test_remove_observe", String.class);
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
        Observable<String> observe = LiveEventBus.get("key_test_auto_remove_observe", String.class);
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
        rule.finishActivity();
        Thread.sleep(1000);
        Assert.assertEquals(map.size(), 0);
        Assert.assertFalse(liveData.hasActiveObservers());
        Assert.assertFalse(liveData.hasObservers());
    }

    @Test
    public void testSendSameMsg() throws Exception {
        final String key = "key_test_send_same_msg";
        final Wrapper<Integer> count = new Wrapper<>(0);
        LiveEventBus

                .get(key, String.class)
                .observe(rule.getActivity(), new Observer<String>() {
                    @Override
                    public void onChanged(@Nullable String s) {
                        count.setTarget(count.getTarget() + 1);
                    }
                });
        Thread.sleep(500);
        LiveEventBus

                .get(key, String.class)
                .post("hello");
        LiveEventBus

                .get(key, String.class)
                .post("hello");
        Thread.sleep(500);
        Assert.assertEquals(count.getTarget().intValue(), 2);
    }

    @Test
    public void testSendEmptyMsg() throws Exception {
        final String key = "key_test_send_empty_msg";
        final Wrapper<String> result = new Wrapper<>("");
        final Wrapper<Boolean> received = new Wrapper<>(false);
        LiveEventBus

                .get(key, String.class)
                .observe(rule.getActivity(), new Observer<String>() {
                    @Override
                    public void onChanged(@Nullable String s) {
                        result.setTarget(s);
                        received.setTarget(true);
                    }
                });
        Thread.sleep(500);
        LiveEventBus

                .get(key, String.class)
                .post(null);
        Thread.sleep(500);
        Assert.assertNull(result.getTarget());
        Assert.assertTrue(received.getTarget());
    }

    @Test
    public void testClearBusOnRemove() throws Exception {
        LiveEventBus.config().autoClear(true);
        final String key = "test_clear_bus_on_remove";
        int count = LiveEventBusTestHelper.getLiveEventBusCount();
        Observer observer = new Observer() {
            @Override
            public void onChanged(@Nullable Object o) {
            }
        };
        LiveEventBus

                .get(key)
                .observeForever(observer);
        Thread.sleep(500);
        Assert.assertEquals(LiveEventBusTestHelper.getLiveEventBusCount(), count + 1);
        LiveEventBus

                .get(key)
                .removeObserver(observer);
        Thread.sleep(500);
        Assert.assertEquals(LiveEventBusTestHelper.getLiveEventBusCount(), count);
    }

    @Test
    public void testClearBusOnRemoveAuto() throws Exception {
        LiveEventBus.config().autoClear(true);
        final String key = "test_clear_bus_on_remove_auto";
        int count = LiveEventBusTestHelper.getLiveEventBusCount();
        LiveEventBus

                .get(key, String.class)
                .observe(rule.getActivity(), new Observer<String>() {
                    @Override
                    public void onChanged(@Nullable String s) {
                    }
                });
        Thread.sleep(500);
        Assert.assertEquals(LiveEventBusTestHelper.getLiveEventBusCount(), count + 1);
        rule.finishActivity();
        Thread.sleep(1000);
        Log.d("LiveEventBus", "bus.size final: " + LiveEventBusTestHelper.getLiveEventBusCount());
        Assert.assertEquals(LiveEventBusTestHelper.getLiveEventBusCount(), 0);
    }

    @Test
    public void testClearBusOnRemoveAutoAndAlwaysActiveFalse() throws Exception {
        LiveEventBus.config().autoClear(true);
        LiveEventBus.config().lifecycleObserverAlwaysActive(false);
        final String key = "test_clear_bus_on_remove_auto_aaf";
        int count = LiveEventBusTestHelper.getLiveEventBusCount();
        LiveEventBus

                .get(key, String.class)
                .observe(rule.getActivity(), new Observer<String>() {
                    @Override
                    public void onChanged(@Nullable String s) {
                    }
                });
        Thread.sleep(500);
        Assert.assertEquals(LiveEventBusTestHelper.getLiveEventBusCount(), count + 1);
        rule.finishActivity();
        Thread.sleep(1000);
        Log.d("LiveEventBus", "bus.size final: " + LiveEventBusTestHelper.getLiveEventBusCount());
        Assert.assertEquals(LiveEventBusTestHelper.getLiveEventBusCount(), 0);
    }

    @Test
    public void testSendSameMessageTimes() throws Exception {
        final String key = "test_send_same_message_times";
        final Wrapper<Integer> counter = new Wrapper<>(0);
        LiveEventBus

                .get(key, Boolean.class)
                .observe(rule.getActivity(), new Observer<Boolean>() {
                    @Override
                    public void onChanged(@Nullable Boolean s) {
                        counter.setTarget(counter.getTarget() + 1);
                    }
                });
        for (int i = 0; i < 10; i++) {
            LiveEventBus

                    .get(key, Boolean.class)
                    .post(true);
        }
        Thread.sleep(500);
        Assert.assertEquals(counter.getTarget().intValue(), 10);
    }

    @Test
    public void testContinuePost() throws Exception {
        final String key = "test_continue_post";
        final Wrapper<Integer> counter = new Wrapper<>(0);
        LiveEventBus

                .get(key, String.class)
                .observe(rule.getActivity(), new Observer<String>() {
                    @Override
                    public void onChanged(@Nullable String s) {
                        counter.setTarget(counter.getTarget() + 1);
                    }
                });
        LiveEventBus

                .get(key, String.class)
                .post(null);
        LiveEventBus

                .get(key, String.class)
                .post("初始化中");
        LiveEventBus

                .get(key, String.class)
                .post("上一条提示信息");
        Thread.sleep(500);
        Assert.assertEquals(counter.getTarget().intValue(), 3);
    }

    @Test
    public void testPostNull() throws Exception {
        final String key = "test_post_null";
        final Wrapper<Integer> counter = new Wrapper<>(0);
        final Wrapper<String> result = new Wrapper<>("aaa");
        LiveEventBus

                .get(key, String.class)
                .post(null);
        Thread.sleep(500);
        LiveEventBus

                .get(key, String.class)
                .observeSticky(rule.getActivity(), new Observer<String>() {
                    @Override
                    public void onChanged(@Nullable String s) {
                        counter.setTarget(counter.getTarget() + 1);
                        result.setTarget(s);
                    }
                });
        Thread.sleep(500);
        Assert.assertEquals(counter.getTarget().intValue(), 1);
        Assert.assertNull(result.getTarget());
    }

    @Test
    public void testPostOrderly() throws Exception {
        final String key = "test_post_orderly";
        final List<Integer> result = new ArrayList<>();
        LiveEventBus

                .get(key, Integer.class)
                .observe(rule.getActivity(), new Observer<Integer>() {
                    @Override
                    public void onChanged(@Nullable Integer integer) {
                        result.add(integer);
                    }
                });
        for (int i = 0; i < 10; i++) {
            LiveEventBus

                    .get(key, Integer.class)
                    .postOrderly(i);
        }
        Thread.sleep(500);
        for (int i = 0; i < 10; i++) {
            Assert.assertEquals(result.get(i).intValue(), i);
        }
    }

    @Test
    public void testExceptionOnReceiveMsg() throws Exception {
        final String key = "key_test_exception_on_receive";
        final Wrapper<Integer> counter = new Wrapper<>(0);
        rule.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LiveEventBus
                        .get(key, Integer.class)
                        .observe(rule.getActivity(), new Observer<Integer>() {
                            @Override
                            public void onChanged(@Nullable Integer s) {
                                counter.setTarget(counter.getTarget() + 1);
                                throw new RuntimeException();
                            }
                        });
                for (int i = 0; i < 10; i++) {
                    try {
                        LiveEventBus
                                .get(key, Integer.class)
                                .post(i);
                    } catch (Exception e) {
                    }
                }
            }
        });
        Thread.sleep(1000);
        Assert.assertEquals(counter.getTarget().intValue(), 10);
    }

    @Test
    public void testBroadcastStringValueInApp() throws Exception {
        final String key = "key_test_broadcast_string";
        final Wrapper<String> wrapper = new Wrapper<>(null);
        rule.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LiveEventBus
                        .get(key, String.class)
                        .observe(rule.getActivity(), new Observer<String>() {
                            @Override
                            public void onChanged(@Nullable String s) {
                                wrapper.setTarget(s);
                            }
                        });
            }
        });
        Thread.sleep(500);
        LiveEventBus
                .get(key, String.class)
                .postAcrossProcess("value_test_broadcast_value");
        Thread.sleep(500);
        Assert.assertEquals(wrapper.getTarget(), "value_test_broadcast_value");
    }

    @Test
    public void testBroadcastStringValueGlobal() throws Exception {
        final String key = "key_test_broadcast_string";
        final Wrapper<String> wrapper = new Wrapper<>(null);
        rule.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LiveEventBus
                        .get(key, String.class)
                        .observe(rule.getActivity(), new Observer<String>() {
                            @Override
                            public void onChanged(@Nullable String s) {
                                wrapper.setTarget(s);
                            }
                        });
            }
        });
        Thread.sleep(500);
        LiveEventBus
                .get(key, String.class)
                .postAcrossApp("value_test_broadcast_value");
        Thread.sleep(500);
        Assert.assertEquals(wrapper.getTarget(), "value_test_broadcast_value");
    }
}