package com.jeremyliao.lebapp.helper;

import com.jeremyliao.liveeventbus.LiveEventBus;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * Created by liaohailiang on 2018/12/27.
 */
public class LiveEventBusTestHelper {

    private LiveEventBusTestHelper() {
    }

    public static int getLiveEventBusCount() {
        try {
            Field bus = LiveEventBus.class.getDeclaredField("bus");
            bus.setAccessible(true);
            Map map = (Map) bus.get(LiveEventBus.get());
            return map.size();
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }
}
