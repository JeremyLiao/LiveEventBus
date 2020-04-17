package com.jeremyliao.liveeventbus.core;

/**
 * Created by liaohailiang on 2020-04-18.
 * 调试信息控制台
 */
public final class Console {

    private Console() {
    }

    /**
     * 获取控制台信息
     *
     * @return 调试信息
     */
    public static String getInfo() {
        return LiveEventBusCore.get().console.getConsoleInfo();
    }
}
