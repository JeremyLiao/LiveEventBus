package com.jeremyliao.liveeventbus.ipc.decode;

import android.content.Intent;

import com.google.gson.Gson;
import com.jeremyliao.liveeventbus.ipc.DataType;
import com.jeremyliao.liveeventbus.ipc.IpcConst;

/**
 * Created by liaohailiang on 2019/3/25.
 */
public class ValueDecoder implements IDecoder {

    private Gson gson = new Gson();

    @Override
    public Object decode(Intent intent) throws DecodeException {
        int valueTypeIndex = intent.getIntExtra(IpcConst.VALUE_TYPE, -1);
        if (valueTypeIndex < 0) {
            throw new DecodeException("Index Error");
        }
        DataType dataType = DataType.values()[valueTypeIndex];
        switch (dataType) {
            case STRING:
                return intent.getStringExtra(IpcConst.VALUE);
            case INTEGER:
                return intent.getIntExtra(IpcConst.VALUE, -1);
            case BOOLEAN:
                return intent.getBooleanExtra(IpcConst.VALUE, false);
            case LONG:
                return intent.getLongExtra(IpcConst.VALUE, -1);
            case FLOAT:
                return intent.getFloatExtra(IpcConst.VALUE, -1);
            case DOUBLE:
                return intent.getDoubleExtra(IpcConst.VALUE, -1);
            case PARCELABLE:
                return intent.getParcelableExtra(IpcConst.VALUE);
            case SERIALIZABLE:
                return intent.getSerializableExtra(IpcConst.VALUE);
            case BUNDLE:
                return intent.getBundleExtra(IpcConst.VALUE);
            case JSON:
                try {
                    String json = intent.getStringExtra(IpcConst.VALUE);
                    String className = intent.getStringExtra(IpcConst.CLASS_NAME);
                    return gson.fromJson(json, Class.forName(className));
                } catch (Exception e) {
                    throw new DecodeException(e);
                }
            case UNKNOWN:
            default:
                throw new DecodeException();
        }
    }
}
