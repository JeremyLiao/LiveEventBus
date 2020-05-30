package com.jeremyliao.lebapp.bean;

import java.io.Serializable;

public class TestBean1 implements Serializable {
    public String content;

    @Override
    public String toString() {
        return content;
    }
}
