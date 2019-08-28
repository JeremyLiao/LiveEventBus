package com.jeremyliao.lebapp.helper;

import android.content.Context;

import com.jeremyliao.liveeventbus.LiveEventBus;
import com.jeremyliao.liveeventbus.core.LiveEventBusCore;

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
            Field bus = LiveEventBusCore.class.getDeclaredField("bus");
            bus.setAccessible(true);
            Map map = (Map) bus.get(LiveEventBusCore.get());
            return map.size();
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static Object getObject(String name) {
        try {
            Field field = LiveEventBusCore.class.getDeclaredField(name);
            field.setAccessible(true);
            return field.get(LiveEventBusCore.get());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Object getLiveEventField(String name, Object target) {
        try {
            Class<?> targetClass = target.getClass();
            Field field = targetClass.getDeclaredField(name);
            field.setAccessible(true);
            return field.get(target);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Context getAppContext() {
        return (Context) getObject("appContext");
    }

    public static boolean getLifecycleObserverAlwaysActive() {
        return (boolean) getObject("lifecycleObserverAlwaysActive");
    }
}
