package com.jeremyliao.liveeventbus.ipc.core;

import android.os.Bundle;
import android.os.Parcelable;

import com.jeremyliao.liveeventbus.ipc.consts.IpcConst;

/**
 * Created by liaohailiang on 2019/5/30.
 */
public class ParcelableProcessor implements Processor {

    @Override
    public boolean writeToBundle(Bundle bundle, Object value) {
        if (!(value instanceof Parcelable)) {
            return false;
        }
        bundle.putParcelable(IpcConst.KEY_VALUE, (Parcelable) value);
        return true;
    }

    @Override
    public Object createFromBundle(Bundle bundle) {
        return bundle.getParcelable(IpcConst.KEY_VALUE);
    }
}
