package com.jeremyliao.lebapp.wrapper;

/**
 * Created by liaohailiang on 2019/3/26.
 */
public class Wrapper<T> {

    private T target;

    public Wrapper(T target) {
        this.target = target;
    }

    public T getTarget() {
        return target;
    }

    public void setTarget(T target) {
        this.target = target;
    }
}
