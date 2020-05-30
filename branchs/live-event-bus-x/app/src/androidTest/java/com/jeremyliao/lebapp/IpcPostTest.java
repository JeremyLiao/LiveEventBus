package com.jeremyliao.lebapp;

import android.content.Intent;

import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;

import com.jeremyliao.lebapp.activity.IpcTestActivity;
import com.jeremyliao.lebapp.activity.TestActivity;
import com.jeremyliao.lebapp.bean.TestBean1;
import com.jeremyliao.lebapp.bean.TestBean2;
import com.jeremyliao.lebapp.bean.TestBean3;
import com.jeremyliao.liveeventbus.LiveEventBus;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class IpcPostTest {

    @Rule
    public ActivityTestRule<TestActivity> rule = new ActivityTestRule<>(TestActivity.class);

    @Before
    public void setUp() throws Exception {
        rule.getActivity().strResult = null;
        rule.getActivity().startActivityForResult(
                new Intent(rule.getActivity(), IpcTestActivity.class),
                0);
        Thread.sleep(500);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testPostIpcString() throws Exception {
        LiveEventBus
                .get(IpcTestActivity.KEY_TEST_IPC_OBSERVE)
                .postAcrossApp("a");
        Thread.sleep(500);
        Assert.assertEquals(rule.getActivity().strResult, "a");
    }

    @Test
    public void testPostIpcInt() throws Exception {
        LiveEventBus
                .get(IpcTestActivity.KEY_TEST_IPC_OBSERVE)
                .postAcrossApp(10);
        Thread.sleep(500);
        Assert.assertEquals(rule.getActivity().strResult, "10");
    }

    @Test
    public void testPostIpcLong() throws Exception {
        LiveEventBus
                .get(IpcTestActivity.KEY_TEST_IPC_OBSERVE)
                .postAcrossApp(10l);
        Thread.sleep(500);
        Assert.assertEquals(rule.getActivity().strResult, "10");
    }

    @Test
    public void testPostIpcBool() throws Exception {
        LiveEventBus
                .get(IpcTestActivity.KEY_TEST_IPC_OBSERVE)
                .postAcrossApp(true);
        Thread.sleep(500);
        Assert.assertEquals(rule.getActivity().strResult, "true");
    }

    @Test
    public void testPostIpcFloat() throws Exception {
        LiveEventBus
                .get(IpcTestActivity.KEY_TEST_IPC_OBSERVE)
                .postAcrossApp(10.0f);
        Thread.sleep(500);
        Assert.assertEquals(rule.getActivity().strResult, "10.0");
    }

    @Test
    public void testPostIpcDouble() throws Exception {
        LiveEventBus
                .get(IpcTestActivity.KEY_TEST_IPC_OBSERVE)
                .postAcrossApp(10d);
        Thread.sleep(500);
        Assert.assertEquals(rule.getActivity().strResult, "10.0");
    }

    @Test
    public void testPostIpcSerializable() throws Exception {
        TestBean1 bean = new TestBean1();
        bean.content = "hello world";
        LiveEventBus
                .get(IpcTestActivity.KEY_TEST_IPC_OBSERVE)
                .postAcrossApp(bean);
        Thread.sleep(500);
        Assert.assertEquals(rule.getActivity().strResult, "hello world");
    }

    @Test
    public void testPostIpcParcelable() throws Exception {
        TestBean2 bean = new TestBean2();
        bean.content = "hello world";
        LiveEventBus
                .get(IpcTestActivity.KEY_TEST_IPC_OBSERVE)
                .postAcrossApp(bean);
        Thread.sleep(500);
        Assert.assertEquals(rule.getActivity().strResult, "hello world");
    }

    @Test
    public void testPostIpcBean() throws Exception {
        TestBean3 bean = new TestBean3();
        bean.content = "hello world";
        LiveEventBus
                .get(IpcTestActivity.KEY_TEST_IPC_OBSERVE)
                .postAcrossApp(bean);
        Thread.sleep(500);
        Assert.assertEquals(rule.getActivity().strResult, "hello world");
    }
}
