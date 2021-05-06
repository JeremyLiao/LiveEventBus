package com.jeremyliao.liveeventbus.processor.gson;

import android.os.Bundle;

import com.jeremyliao.liveeventbus.ipc.consts.IpcConst;
import com.jeremyliao.liveeventbus.ipc.core.Processor;
import com.jeremyliao.liveeventbus.processor.gson.converter.GsonConverter;
import com.jeremyliao.liveeventbus.processor.gson.converter.JsonConverter;

/**
 * Created by liaohailiang on 2019/5/30.
 */
public class GsonProcessor implements Processor {

    private final JsonConverter jsonConverter = new GsonConverter();

    @Override
    public boolean writeToBundle(Bundle bundle, Object value) {
        String json = jsonConverter.toJson(value);
        bundle.putString(IpcConst.KEY_VALUE, json);
        bundle.putString(IpcConst.KEY_CLASS_NAME, value.getClass().getCanonicalName());
        return true;
    }

    @Override
    public Object createFromBundle(Bundle bundle) throws ClassNotFoundException {
        String json = bundle.getString(IpcConst.KEY_VALUE);
        String className = bundle.getString(IpcConst.KEY_CLASS_NAME);
        Class<?> classType = null;
        try {
            classType = Class.forName(className);
        } catch (ClassNotFoundException e) {
            int last = className.lastIndexOf('.');
            if (last != -1) {
                String pn = className.substring(0, last);
                String cn = className.substring(last + 1);
                classType = Class.forName(pn + "$" + cn);
            }
        }
        return jsonConverter.fromJson(json, classType);
    }
}
