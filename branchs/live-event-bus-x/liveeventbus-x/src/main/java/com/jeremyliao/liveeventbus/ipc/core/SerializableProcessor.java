package com.jeremyliao.liveeventbus.ipc.core;

import android.os.Bundle;

import com.jeremyliao.liveeventbus.ipc.consts.IpcConst;

import java.io.Serializable;

/**
 * Created by liaohailiang on 2019/5/30.
 */
public class SerializableProcessor implements Processor {

    @Override
    public boolean writeToBundle(Bundle bundle, Object value) {
        if (!(value instanceof Serializable)) {
            return false;
        }
        bundle.putSerializable(IpcConst.KEY_VALUE, (Serializable) value);
        return true;
    }

    @Override
    public Object createFromBundle(Bundle bundle) {
        return bundle.getSerializable(IpcConst.KEY_VALUE);
    }
}
