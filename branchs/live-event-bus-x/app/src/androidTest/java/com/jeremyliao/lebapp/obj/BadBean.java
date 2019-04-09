package com.jeremyliao.lebapp.obj;

/**
 * Created by liaohailiang on 2019/3/26.
 */
public class BadBean {

    private int intValue;
    private String strValue;

    public BadBean(int intValue, String strValue) {
        this.intValue = intValue;
        this.strValue = strValue;
    }

    public BadBean() {
    }

    public int getIntValue() {
        return intValue;
    }

    public void setIntValue(int intValue) {
        this.intValue = intValue;
    }

    public String getStrValue() {
        return strValue;
    }
}
