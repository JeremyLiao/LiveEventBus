package com.jeremyliao.lebapp;

import android.os.Handler;
import android.os.Looper;

import androidx.test.runner.AndroidJUnit4;

import com.jeremyliao.liveeventbus.utils.ThreadUtils;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;


/**
 * Created by liaohailiang on 2019/3/20.
 */
@RunWith(AndroidJUnit4.class)
public class ThreadUtilsTest {

    @Test
    public void testInBackgroundThread() throws Exception {
        Assert.assertFalse(ThreadUtils.isMainThread());
    }

    @Test
    public void testInMainThread() throws Exception {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                Assert.assertTrue(ThreadUtils.isMainThread());
            }
        });
        Thread.sleep(500);
    }
}