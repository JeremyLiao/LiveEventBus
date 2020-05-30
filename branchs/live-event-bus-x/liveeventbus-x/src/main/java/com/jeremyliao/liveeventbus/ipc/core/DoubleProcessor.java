package com.jeremyliao.liveeventbus.ipc.core;

import android.os.Bundle;

import com.jeremyliao.liveeventbus.ipc.consts.IpcConst;

/**
 * Created by liaohailiang on 2019/5/30.
 */
public class DoubleProcessor implements Processor {

    @Override
    public boolean writeToBundle(Bundle bundle, Object value) {
        if (!(value instanceof Double)) {
            return false;
        }
        bundle.putDouble(IpcConst.KEY_VALUE, (Double) value);
        return true;
    }

    @Override
    public Object createFromBundle(Bundle bundle) {
        return bundle.getDouble(IpcConst.KEY_VALUE);
    }
}
