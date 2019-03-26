package com.jeremyliao.liveeventbus.utils;

import android.annotation.SuppressLint;
import android.app.Application;

/**
 * Created by liaohailiang on 2019/3/26.
 */
public final class AppUtils {

    @SuppressLint("StaticFieldLeak")
    private static volatile Application sApplication;

    public static Application getApplicationContext() {
        if (sApplication == null) {
            synchronized (AppUtils.class) {
                if (sApplication == null) {
                    try {
                        sApplication = (Application) Class.forName("android.app.ActivityThread")
                                .getMethod("currentApplication")
                                .invoke(null, (Object[]) null);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return sApplication;
    }
}
