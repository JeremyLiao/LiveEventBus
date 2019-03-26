package com.jeremyliao.lebapp.obj;

import java.io.Serializable;

/**
 * Created by liaohailiang on 2019/3/26.
 */
public class SerializableObject implements Serializable {

    private int value = -1;

    public SerializableObject(int value) {
        this.value = value;
    }

    public SerializableObject() {
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof SerializableObject)) {
            return false;
        }
        SerializableObject serializableObject = (SerializableObject) obj;
        return this.value == serializableObject.value;
    }
}
