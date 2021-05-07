package com.jeremyliao.liveeventbus.core;

import android.app.Application;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ExternalLiveData;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.jeremyliao.liveeventbus.ipc.consts.IpcConst;
import com.jeremyliao.liveeventbus.ipc.core.ProcessorManager;
import com.jeremyliao.liveeventbus.ipc.receiver.LebIpcReceiver;
import com.jeremyliao.liveeventbus.logger.DefaultLogger;
import com.jeremyliao.liveeventbus.logger.Logger;
import com.jeremyliao.liveeventbus.logger.LoggerManager;
import com.jeremyliao.liveeventbus.utils.AppUtils;
import com.jeremyliao.liveeventbus.utils.ThreadUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
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
    private LoggerManager logger;
    private final Map<String, ObservableConfig> observableConfigs;

    /**
     * 跨进程通信
     */
    private LebIpcReceiver receiver;
    private boolean isRegisterReceiver = false;

    /**
     * 调试
     */
    final InnerConsole console = new InnerConsole();

    private LiveEventBusCore() {
        bus = new HashMap<>();
        observableConfigs = new HashMap<>();
        lifecycleObserverAlwaysActive = true;
        autoClear = false;
        logger = new LoggerManager(new DefaultLogger());
        receiver = new LebIpcReceiver();
        registerReceiver();
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
     * @return Config
     */
    public Config config() {
        return config;
    }

    public ObservableConfig config(String key) {
        if (!observableConfigs.containsKey(key)) {
            observableConfigs.put(key, new ObservableConfig());
        }
        return observableConfigs.get(key);
    }

    void setLogger(@NonNull Logger logger) {
        this.logger.setLogger(logger);
    }

    void enableLogger(boolean enable) {
        this.logger.setEnable(enable);
    }

    void registerReceiver() {
        if (isRegisterReceiver) {
            return;
        }
        Application application = AppUtils.getApp();
        if (application != null) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(IpcConst.ACTION);
            application.registerReceiver(receiver, intentFilter);
            isRegisterReceiver = true;
        }
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
            this.liveData = new LifecycleLiveData<>(key);
        }

        /**
         * 进程内发送消息
         *
         * @param value 发送的消息
         */
        @Override
        public void post(T value) {
            if (ThreadUtils.isMainThread()) {
                postInternal(value);
            } else {
                mainHandler.post(new PostValueTask(value));
            }
        }

        /**
         * App内发送消息，跨进程使用
         *
         * @param value 发送的消息
         */
        @Override
        public void postAcrossProcess(T value) {
            broadcast(value, false, true);
        }

        /**
         * App之间发送消息
         *
         * @param value 发送的消息
         */
        @Override
        public void postAcrossApp(T value) {
            broadcast(value, false, false);
        }

        /**
         * 进程内发送消息，延迟发送
         *
         * @param value 发送的消息
         * @param delay 延迟毫秒数
         */
        @Override
        public void postDelay(T value, long delay) {
            mainHandler.postDelayed(new PostValueTask(value), delay);
        }

        /**
         * 进程内发送消息，延迟发送，带生命周期
         * 如果延时发送消息的时候sender处于非激活状态，消息取消发送
         *
         * @param owner 消息发送者
         * @param value 发送的消息
         * @param delay 延迟毫秒数
         */
        @Override
        public void postDelay(LifecycleOwner owner, final T value, long delay) {
            mainHandler.postDelayed(new PostLifeValueTask(value, owner), delay);
        }

        /**
         * 进程内发送消息
         * 强制接收到消息的顺序和发送顺序一致
         *
         * @param value 发送的消息
         */
        @Override
        public void postOrderly(T value) {
            mainHandler.post(new PostValueTask(value));
        }

        /**
         * App之间发送消息
         *
         * @param value 发送的消息
         */
        @Override
        @Deprecated
        public void broadcast(T value) {
            broadcast(value, false, false);
        }

        /**
         * 以广播的形式发送一个消息
         * 需要跨进程、跨APP发送消息的时候调用该方法
         *
         * @param value      发送的消息
         * @param foreground true:前台广播、false:后台广播
         * @param onlyInApp  true:只在APP内有效、false:全局有效
         */
        @Override
        public void broadcast(final T value, final boolean foreground, final boolean onlyInApp) {
            if (AppUtils.getApp() != null) {
                if (ThreadUtils.isMainThread()) {
                    broadcastInternal(value, foreground, onlyInApp);
                } else {
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            broadcastInternal(value, foreground, onlyInApp);
                        }
                    });
                }
            } else {
                post(value);
            }
        }

        /**
         * 注册一个Observer，生命周期感知，自动取消订阅
         *
         * @param owner    LifecycleOwner
         * @param observer 观察者
         */
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

        /**
         * 注册一个Observer，生命周期感知，自动取消订阅
         * 如果之前有消息发送，可以在注册时收到消息（消息同步）
         *
         * @param owner    LifecycleOwner
         * @param observer 观察者
         */
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

        /**
         * 注册一个Observer，需手动解除绑定
         *
         * @param observer 观察者
         */
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

        /**
         * 注册一个Observer，需手动解除绑定
         * 如果之前有消息发送，可以在注册时收到消息（消息同步）
         *
         * @param observer 观察者
         */
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

        /**
         * 通过observeForever或observeStickyForever注册的，需要调用该方法取消订阅
         *
         * @param observer 观察者
         */
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
        private void broadcastInternal(T value, boolean foreground, boolean onlyInApp) {
            logger.log(Level.INFO, "broadcast: " + value + " foreground: " + foreground +
                    " with key: " + key);
            Application application = AppUtils.getApp();
            if (application == null) {
                logger.log(Level.WARNING, "application is null, you can try setContext() when config");
                return;
            }
            Intent intent = new Intent(IpcConst.ACTION);
            if (foreground && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
            }
            if (onlyInApp) {
                intent.setPackage(application.getPackageName());
            }
            intent.putExtra(IpcConst.KEY, key);
            boolean handle = ProcessorManager.getManager().writeTo(intent, value);
            try {
                if (handle) {
                    application.sendBroadcast(intent);
                }
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

            private final String key;

            public LifecycleLiveData(String key) {
                this.key = key;
            }

            @Override
            protected Lifecycle.State observerActiveLevel() {
                return lifecycleObserverAlwaysActive() ? Lifecycle.State.CREATED : Lifecycle.State.STARTED;
            }

            @Override
            public void removeObserver(@NonNull Observer<? super T> observer) {
                super.removeObserver(observer);
                if (autoClear() && !liveData.hasObservers()) {
                    LiveEventBusCore.get().bus.remove(key);
                }
                logger.log(Level.INFO, "observer removed: " + observer);
            }

            private boolean lifecycleObserverAlwaysActive() {
                if (observableConfigs.containsKey(key)) {
                    ObservableConfig config = observableConfigs.get(key);
                    if (config.lifecycleObserverAlwaysActive != null) {
                        return config.lifecycleObserverAlwaysActive;
                    }
                }
                return lifecycleObserverAlwaysActive;
            }

            private boolean autoClear() {
                if (observableConfigs.containsKey(key)) {
                    ObservableConfig config = observableConfigs.get(key);
                    if (config.autoClear != null) {
                        return config.autoClear;
                    }
                }
                return autoClear;
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
                logger.log(Level.WARNING, "class cast error on message received: " + t, e);
            } catch (Exception e) {
                logger.log(Level.WARNING, "error on message received: " + t, e);
            }
        }
    }

    class InnerConsole {

        String getConsoleInfo() {
            StringBuilder sb = new StringBuilder();
            sb.append("*********Base info*********").append("\n");
            sb.append(getBaseInfo());
            sb.append("*********Event info*********").append("\n");
            sb.append(getBusInfo());
            return sb.toString();
        }

        String getBaseInfo() {
            StringBuilder sb = new StringBuilder();
            sb.append("lifecycleObserverAlwaysActive: ").append(lifecycleObserverAlwaysActive).append("\n")
                    .append("autoClear: ").append(autoClear).append("\n")
                    .append("logger enable: ").append(logger.isEnable()).append("\n")
                    .append("logger: ").append(logger.getLogger()).append("\n")
                    .append("Receiver register: ").append(isRegisterReceiver).append("\n")
                    .append("Application: ").append(AppUtils.getApp()).append("\n");
            return sb.toString();
        }

        String getBusInfo() {
            StringBuilder sb = new StringBuilder();
            for (String key : bus.keySet()) {
                sb.append("Event name: " + key).append("\n");
                ExternalLiveData liveData = bus.get(key).liveData;
                sb.append("\tversion: " + liveData.getVersion()).append("\n");
                sb.append("\thasActiveObservers: " + liveData.hasActiveObservers()).append("\n");
                sb.append("\thasObservers: " + liveData.hasObservers()).append("\n");
                sb.append("\tActiveCount: " + getActiveCount(liveData)).append("\n");
                sb.append("\tObserverCount: " + getObserverCount(liveData)).append("\n");
                sb.append("\tObservers: ").append("\n");
                sb.append("\t\t" + getObserverInfo(liveData)).append("\n");
            }
            return sb.toString();
        }

        private int getActiveCount(LiveData liveData) {
            try {
                Field field = LiveData.class.getDeclaredField("mActiveCount");
                field.setAccessible(true);
                return (int) field.get(liveData);
            } catch (Exception e) {
                return -1;
            }
        }

        private int getObserverCount(LiveData liveData) {
            try {
                Field field = LiveData.class.getDeclaredField("mObservers");
                field.setAccessible(true);
                Object mObservers = field.get(liveData);
                Class<?> classOfSafeIterableMap = mObservers.getClass();
                Method size = classOfSafeIterableMap.getDeclaredMethod("size");
                size.setAccessible(true);
                return (int) size.invoke(mObservers);
            } catch (Exception e) {
                return -1;
            }
        }

        private String getObserverInfo(LiveData liveData) {
            try {
                Field field = LiveData.class.getDeclaredField("mObservers");
                field.setAccessible(true);
                Object mObservers = field.get(liveData);
                return mObservers.toString();
            } catch (Exception e) {
                return "";
            }
        }
    }
}
