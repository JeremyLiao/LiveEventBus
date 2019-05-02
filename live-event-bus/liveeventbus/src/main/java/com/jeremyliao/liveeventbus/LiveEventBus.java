package com.jeremyliao.liveeventbus;

import android.arch.lifecycle.ExternalLiveData;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.jeremyliao.liveeventbus.ipc.IpcConst;
import com.jeremyliao.liveeventbus.ipc.encode.IEncoder;
import com.jeremyliao.liveeventbus.ipc.encode.ValueEncoder;
import com.jeremyliao.liveeventbus.ipc.receiver.LebIpcReceiver;
import com.jeremyliao.liveeventbus.utils.ThreadUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by liaohailiang on 2019/1/21.
 */

public final class LiveEventBus {

    private final Map<String, LiveEvent<Object>> bus;

    private LiveEventBus() {
        bus = new HashMap<>();
    }

    private static class SingletonHolder {
        private static final LiveEventBus DEFAULT_BUS = new LiveEventBus();
    }

    public static LiveEventBus get() {
        return SingletonHolder.DEFAULT_BUS;
    }

    private Context appContext;
    private boolean lifecycleObserverAlwaysActive = true;
    private IEncoder encoder = new ValueEncoder();
    private Config config = new Config();
    private LebIpcReceiver receiver = new LebIpcReceiver();

    public synchronized <T> Observable<T> with(String key, Class<T> type) {
        if (!bus.containsKey(key)) {
            bus.put(key, new LiveEvent<>(key));
        }
        return (Observable<T>) bus.get(key);
    }

    public Observable<Object> with(String key) {
        return with(key, Object.class);
    }

    /**
     * use the inner class Config to set params
     * first of all, call config to get the Config instance
     * then, call the method of Config to config LiveEventBus
     * call this method in Application.onCreate
     */

    public Config config() {
        return config;
    }

    public class Config {

        /**
         * lifecycleObserverAlwaysActive
         * set if then observer can always receive message
         * true: observer can always receive message
         * false: observer can only receive message when resumed
         *
         * @param active
         * @return
         */
        public Config lifecycleObserverAlwaysActive(boolean active) {
            lifecycleObserverAlwaysActive = active;
            return this;
        }

        /**
         * config broadcast
         * only if you called this method, you can use broadcastValue() to send broadcast message
         *
         * @param context
         * @return
         */
        public Config supportBroadcast(Context context) {
            if (context != null) {
                appContext = context.getApplicationContext();
            }
            if (appContext != null) {
                IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction(IpcConst.ACTION);
                appContext.registerReceiver(receiver, intentFilter);
            }
            return this;
        }
    }

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

    private class LiveEvent<T> implements Observable<T> {

        @NonNull
        private final String key;
        private final LifecycleLiveData<T> liveData;
        private final Map<Observer, ObserverWrapper<T>> observerMap = new HashMap<>();
        private final Handler mainHandler = new Handler(Looper.getMainLooper());

        LiveEvent(@NonNull String key) {
            this.key = key;
            this.liveData = new LifecycleLiveData<>();
        }

        @Override
        public void post(T value) {
            if (ThreadUtils.isMainThread()) {
                postInternal(value);
            } else {
                mainHandler.post(new PostValueTask(value));
            }
        }

        @Override
        public void broadcast(final T value) {
            if (appContext != null) {
                if (ThreadUtils.isMainThread()) {
                    broadcastInternal(value);
                } else {
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            broadcastInternal(value);
                        }
                    });
                }
            } else {
                post(value);
            }
        }

        @Override
        public void postDelay(T value, long delay) {
            mainHandler.postDelayed(new PostValueTask(value), delay);
        }

        @Override
        public void observe(@NonNull final LifecycleOwner owner, @NonNull final Observer<T> observer) {
            if (ThreadUtils.isMainThread()) {
                observeInternal(owner, observer);
            } else {
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        observeInternal(owner, observer);
                    }
                });
            }
        }

        @Override
        public void observeSticky(@NonNull final LifecycleOwner owner, @NonNull final Observer<T> observer) {
            if (ThreadUtils.isMainThread()) {
                observeStickyInternal(owner, observer);
            } else {
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        observeStickyInternal(owner, observer);
                    }
                });
            }
        }

        @Override
        public void observeForever(@NonNull final Observer<T> observer) {
            if (ThreadUtils.isMainThread()) {
                observeForeverInternal(observer);
            } else {
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        observeForeverInternal(observer);
                    }
                });
            }
        }

        @Override
        public void observeStickyForever(@NonNull final Observer<T> observer) {
            if (ThreadUtils.isMainThread()) {
                observeStickyForeverInternal(observer);
            } else {
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        observeStickyForeverInternal(observer);
                    }
                });
            }
        }

        @Override
        public void removeObserver(@NonNull final Observer<T> observer) {
            if (ThreadUtils.isMainThread()) {
                removeObserverInternal(observer);
            } else {
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        removeObserverInternal(observer);
                    }
                });
            }
        }

        @MainThread
        private void postInternal(T value) {
            liveData.setValue(value);
        }

        @MainThread
        private void broadcastInternal(T value) {
            Intent intent = new Intent(IpcConst.ACTION);
            intent.putExtra(IpcConst.KEY, key);
            try {
                encoder.encode(intent, value);
                appContext.sendBroadcast(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @MainThread
        private void observeInternal(@NonNull LifecycleOwner owner, @NonNull Observer<T> observer) {
            ObserverWrapper<T> observerWrapper = new ObserverWrapper<>(observer);
            observerWrapper.preventNextEvent = liveData.getVersion() > ExternalLiveData.START_VERSION;
            liveData.observe(owner, observerWrapper);
        }

        @MainThread
        private void observeStickyInternal(@NonNull LifecycleOwner owner, @NonNull Observer<T> observer) {
            ObserverWrapper<T> observerWrapper = new ObserverWrapper<>(observer);
            liveData.observe(owner, observerWrapper);
        }

        @MainThread
        private void observeForeverInternal(@NonNull Observer<T> observer) {
            ObserverWrapper<T> observerWrapper = new ObserverWrapper<>(observer);
            observerWrapper.preventNextEvent = liveData.getVersion() > ExternalLiveData.START_VERSION;
            observerMap.put(observer, observerWrapper);
            liveData.observeForever(observerWrapper);
        }

        @MainThread
        private void observeStickyForeverInternal(@NonNull Observer<T> observer) {
            ObserverWrapper<T> observerWrapper = new ObserverWrapper<>(observer);
            observerMap.put(observer, observerWrapper);
            liveData.observeForever(observerWrapper);
        }

        @MainThread
        private void removeObserverInternal(@NonNull Observer<T> observer) {
            Observer<T> realObserver;
            if (observerMap.containsKey(observer)) {
                realObserver = observerMap.remove(observer);
            } else {
                realObserver = observer;
            }
            liveData.removeObserver(realObserver);
        }

        private class LifecycleLiveData<T> extends ExternalLiveData<T> {
            @Override
            protected Lifecycle.State observerActiveLevel() {
                return lifecycleObserverAlwaysActive ? Lifecycle.State.CREATED : Lifecycle.State.STARTED;
            }

            @Override
            public void removeObserver(@NonNull Observer<T> observer) {
                super.removeObserver(observer);
                if (!liveData.hasObservers()) {
                    LiveEventBus.get().bus.remove(key);
                }
            }
        }

        private class PostValueTask implements Runnable {
            private Object newValue;

            public PostValueTask(@NonNull Object newValue) {
                this.newValue = newValue;
            }

            @Override
            public void run() {
                postInternal((T) newValue);
            }
        }
    }

    private static class ObserverWrapper<T> implements Observer<T> {

        @NonNull
        private final Observer<T> observer;
        private boolean preventNextEvent = false;

        ObserverWrapper(@NonNull Observer<T> observer) {
            this.observer = observer;
        }

        @Override
        public void onChanged(@Nullable T t) {
            if (preventNextEvent) {
                preventNextEvent = false;
                return;
            }
            try {
                observer.onChanged(t);
            } catch (ClassCastException e) {
                e.printStackTrace();
            }
        }
    }
}
