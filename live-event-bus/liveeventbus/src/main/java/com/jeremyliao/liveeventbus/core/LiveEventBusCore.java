package com.jeremyliao.liveeventbus.core;

import android.arch.lifecycle.ExternalLiveData;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.jeremyliao.liveeventbus.ipc.IpcConst;
import com.jeremyliao.liveeventbus.ipc.encode.IEncoder;
import com.jeremyliao.liveeventbus.ipc.encode.ValueEncoder;
import com.jeremyliao.liveeventbus.ipc.json.GsonConverter;
import com.jeremyliao.liveeventbus.ipc.json.JsonConverter;
import com.jeremyliao.liveeventbus.ipc.receiver.LebIpcReceiver;
import com.jeremyliao.liveeventbus.logger.DefaultLogger;
import com.jeremyliao.liveeventbus.logger.Logger;
import com.jeremyliao.liveeventbus.utils.ThreadUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * LiveEventBusCore
 */

public final class LiveEventBusCore {

    /**
     * 单例模式实现
     */
    private static class SingletonHolder {
        private static final LiveEventBusCore DEFAULT_BUS = new LiveEventBusCore();
    }

    public static LiveEventBusCore get() {
        return SingletonHolder.DEFAULT_BUS;
    }

    /**
     * 存放LiveEvent
     */
    private final Map<String, LiveEvent<Object>> bus;

    /**
     * 可配置的项
     */
    private final Config config = new Config();
    private boolean lifecycleObserverAlwaysActive;
    private boolean autoClear;
    private Context appContext;
    private Logger logger;

    /**
     * 跨进程通信
     */
    private IEncoder encoder;
    private LebIpcReceiver receiver;

    private LiveEventBusCore() {
        bus = new HashMap<>();
        lifecycleObserverAlwaysActive = true;
        autoClear = false;
        logger = new DefaultLogger();
        JsonConverter converter = new GsonConverter();
        encoder = new ValueEncoder(converter);
        receiver = new LebIpcReceiver(converter);
    }

    public synchronized <T> Observable<T> with(String key, Class<T> type) {
        if (!bus.containsKey(key)) {
            bus.put(key, new LiveEvent<>(key));
        }
        return (Observable<T>) bus.get(key);
    }

    /**
     * use the class Config to set params
     * first of all, call config to get the Config instance
     * then, call the method of Config to config LiveEventBus
     * call this method in Application.onCreate
     */
    public Config config() {
        return config;
    }

    void setLogger(@NonNull Logger logger) {
        this.logger = logger;
    }

    void registerReceiver(Context context) {
        if (context != null) {
            appContext = context.getApplicationContext();
        }
        if (appContext != null) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(IpcConst.ACTION);
            appContext.registerReceiver(receiver, intentFilter);
        }
    }

    void setJsonConverter(JsonConverter jsonConverter) {
        if (jsonConverter == null) {
            return;
        }
        this.encoder = new ValueEncoder(jsonConverter);
        this.receiver.setJsonConverter(jsonConverter);
    }

    void setLifecycleObserverAlwaysActive(boolean lifecycleObserverAlwaysActive) {
        this.lifecycleObserverAlwaysActive = lifecycleObserverAlwaysActive;
    }

    void setAutoClear(boolean autoClear) {
        this.autoClear = autoClear;
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
        public void broadcast(T value) {
            broadcast(value, false);
        }

        @Override
        public void postDelay(T value, long delay) {
            mainHandler.postDelayed(new PostValueTask(value), delay);
        }

        @Override
        public void postDelay(LifecycleOwner owner, final T value, long delay) {
            mainHandler.postDelayed(new PostLifeValueTask(value, owner), delay);
        }

        @Override
        public void postOrderly(T value) {
            mainHandler.post(new PostValueTask(value));
        }

        @Override
        public void broadcast(final T value, final boolean foreground) {
            if (appContext != null) {
                if (ThreadUtils.isMainThread()) {
                    broadcastInternal(value, foreground);
                } else {
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            broadcastInternal(value, foreground);
                        }
                    });
                }
            } else {
                post(value);
            }
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
            logger.log(Level.INFO, "post: " + value + " with key: " + key);
            liveData.setValue(value);
        }

        @MainThread
        private void broadcastInternal(T value, boolean foreground) {
            logger.log(Level.INFO, "broadcast: " + value + " foreground: " + foreground +
                    " with key: " + key);
            Intent intent = new Intent(IpcConst.ACTION);
            if (foreground && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
            }
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
            logger.log(Level.INFO, "observe observer: " + observerWrapper + "(" + observer + ")"
                    + " on owner: " + owner + " with key: " + key);
        }

        @MainThread
        private void observeStickyInternal(@NonNull LifecycleOwner owner, @NonNull Observer<T> observer) {
            ObserverWrapper<T> observerWrapper = new ObserverWrapper<>(observer);
            liveData.observe(owner, observerWrapper);
            logger.log(Level.INFO, "observe sticky observer: " + observerWrapper + "(" + observer + ")"
                    + " on owner: " + owner + " with key: " + key);
        }

        @MainThread
        private void observeForeverInternal(@NonNull Observer<T> observer) {
            ObserverWrapper<T> observerWrapper = new ObserverWrapper<>(observer);
            observerWrapper.preventNextEvent = liveData.getVersion() > ExternalLiveData.START_VERSION;
            observerMap.put(observer, observerWrapper);
            liveData.observeForever(observerWrapper);
            logger.log(Level.INFO, "observe forever observer: " + observerWrapper + "(" + observer + ")"
                    + " with key: " + key);
        }

        @MainThread
        private void observeStickyForeverInternal(@NonNull Observer<T> observer) {
            ObserverWrapper<T> observerWrapper = new ObserverWrapper<>(observer);
            observerMap.put(observer, observerWrapper);
            liveData.observeForever(observerWrapper);
            logger.log(Level.INFO, "observe sticky forever observer: " + observerWrapper + "(" + observer + ")"
                    + " with key: " + key);
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
                if (autoClear && !liveData.hasObservers()) {
                    LiveEventBusCore.get().bus.remove(key);
                }
                logger.log(Level.INFO, "observer removed: " + observer);
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

        private class PostLifeValueTask implements Runnable {
            private Object newValue;
            private LifecycleOwner owner;

            public PostLifeValueTask(@NonNull Object newValue, @Nullable LifecycleOwner owner) {
                this.newValue = newValue;
                this.owner = owner;
            }

            @Override
            public void run() {
                if (owner != null) {
                    if (owner.getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
                        postInternal((T) newValue);
                    }
                }
            }
        }
    }

    private class ObserverWrapper<T> implements Observer<T> {

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
            logger.log(Level.INFO, "message received: " + t);
            try {
                observer.onChanged(t);
            } catch (ClassCastException e) {
                logger.log(Level.WARNING, "error on message received: " + t, e);
            }
        }
    }
}
