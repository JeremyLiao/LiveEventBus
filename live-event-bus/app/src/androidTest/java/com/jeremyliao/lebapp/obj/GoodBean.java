package com.jeremyliao.lebapp.obj;

/**
 * Created by liaohailiang on 2019/3/26.
 */
public class GoodBean {

    private int intValue;
    private String strValue;

    public GoodBean(int intValue, String strValue) {
        this.intValue = intValue;
        this.strValue = strValue;
    }

    public GoodBean() {
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

    public void setStrValue(String strValue) {
        this.strValue = strValue;
    }
}
