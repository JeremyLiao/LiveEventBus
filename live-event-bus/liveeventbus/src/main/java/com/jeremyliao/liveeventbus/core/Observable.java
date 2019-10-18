package com.jeremyliao.liveeventbus.core;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.Observer;
import android.support.annotation.NonNull;

/**
 * Created by liaohailiang on 2019-08-28.
 */
public interface Observable<T> {

    /**
     * 发送一个消息，支持前台线程、后台线程发送
     *
     * @param value
     */
    void post(T value);

    /**
     * 发送一个消息，支持前台线程、后台线程发送
     * 需要跨进程、跨APP发送消息的时候调用该方法
     *
     * @param value
     */
    void broadcast(T value);

    /**
     * 延迟发送一个消息，支持前台线程、后台线程发送
     *
     * @param value
     * @param delay 延迟毫秒数
     */
    void postDelay(T value, long delay);

    /**
     * 延迟发送一个消息，支持前台线程、后台线程发送，带生命周期
     * 如果延时发送消息的时候sender处于非激活状态，消息取消发送
     *
     * @param sender 消息发送者
     * @param value
     * @param delay  延迟毫秒数
     */
    void postDelay(LifecycleOwner sender, T value, long delay);

    /**
     * 发送一个消息，支持前台线程、后台线程发送
     * 接收到消息的顺序和发送顺序一致
     *
     * @param value
     */
    void postOrderly(T value);

    /**
     * 发送一个消息，支持前台线程、后台线程发送
     * 需要跨进程、跨APP发送消息的时候调用该方法
     *
     * @param value
     */
    void broadcast(T value, boolean foreground);

    /**
     * 注册一个Observer，生命周期感知，自动取消订阅
     *
     * @param owner
     * @param observer
     */
    void observe(@NonNull LifecycleOwner owner, @NonNull Observer<T> observer);

    /**
     * 注册一个Observer，生命周期感知，自动取消订阅
     * 如果之前有消息发送，可以在注册时收到消息（消息同步）
     *
     * @param owner
     * @param observer
     */
    void observeSticky(@NonNull LifecycleOwner owner, @NonNull Observer<T> observer);

    /**
     * 注册一个Observer
     *
     * @param observer
     */
    void observeForever(@NonNull Observer<T> observer);

    /**
     * 注册一个Observer
     * 如果之前有消息发送，可以在注册时收到消息（消息同步）
     *
     * @param observer
     */
    void observeStickyForever(@NonNull Observer<T> observer);

    /**
     * 通过observeForever或observeStickyForever注册的，需要调用该方法取消订阅
     *
     * @param observer
     */
    void removeObserver(@NonNull Observer<T> observer);
}
