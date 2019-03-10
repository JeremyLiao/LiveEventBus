package com.jeremyliao.liveeventbus;

import android.arch.lifecycle.ExternalLiveData;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.Observer;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by liaohailiang on 2019/1/21.
 */

public final class LiveEventBus {

    private final Map<String, BusLiveData<Object>> bus;

    private LiveEventBus() {
        bus = new HashMap<>();
    }

    private static class SingletonHolder {
        private static final LiveEventBus DEFAULT_BUS = new LiveEventBus();
    }

    public static LiveEventBus get() {
        return SingletonHolder.DEFAULT_BUS;
    }

    private boolean lifecycleObserverAlwaysActive = true;

    public synchronized <T> Observable<T> with(String key, Class<T> type) {
        if (!bus.containsKey(key)) {
            bus.put(key, new BusLiveData<>(key));
        }
        return (Observable<T>) bus.get(key);
    }

    public Observable<Object> with(String key) {
        return with(key, Object.class);
    }

    public void lifecycleObserverAlwaysActive(boolean active) {
        lifecycleObserverAlwaysActive = active;
    }

    public interface Observable<T> {

        void setValue(T value);

        void postValue(T value);

        void postValueDelay(T value, long delay);

        void postValueDelay(T value, long delay, TimeUnit unit);

        void observe(@NonNull LifecycleOwner owner, @NonNull Observer<T> observer);

        void observeSticky(@NonNull LifecycleOwner owner, @NonNull Observer<T> observer);

        void observeForever(@NonNull Observer<T> observer);

        void observeStickyForever(@NonNull Observer<T> observer);

        void removeObserver(@NonNull Observer<T> observer);
    }

    private class BusLiveData<T> extends ExternalLiveData<T> implements Observable<T> {

        private class PostValueTask implements Runnable {
            private Object newValue;

            public PostValueTask(@NonNull Object newValue) {
                this.newValue = newValue;
            }

            @Override
            public void run() {
                setValue((T) newValue);
            }
        }

        @NonNull
        private final String key;
        private Map<Observer, Observer> observerMap = new HashMap<>();
        private Handler mainHandler = new Handler(Looper.getMainLooper());


        private BusLiveData(String key) {
            this.key = key;
        }

        @Override
        protected Lifecycle.State observerActiveLevel() {
            return lifecycleObserverAlwaysActive ? Lifecycle.State.CREATED : Lifecycle.State.STARTED;
        }

        @Override
        public void postValue(T value) {
            mainHandler.post(new PostValueTask(value));
        }

        @Override
        public void postValueDelay(T value, long delay) {
            mainHandler.postDelayed(new PostValueTask(value), delay);
        }

        @Override
        public void postValueDelay(T value, long delay, TimeUnit unit) {
            postValueDelay(value, TimeUnit.MILLISECONDS.convert(delay, unit));
        }

        @Override
        public void observe(@NonNull LifecycleOwner owner, @NonNull Observer<T> observer) {
            super.observe(owner, createStateObserver(observer));
        }

        @Override
        public void observeSticky(@NonNull LifecycleOwner owner, @NonNull Observer<T> observer) {
            super.observeSticky(owner, new SafeCastObserver<>(observer));
        }

        @Override
        public void observeForever(@NonNull Observer<T> observer) {
            if (!observerMap.containsKey(observer)) {
                observerMap.put(observer, createForeverObserver(observer));
            }
            super.observeForever(observerMap.get(observer));
        }

        public void observeStickyForever(@NonNull Observer<T> observer) {
            super.observeForever(observer);
        }

        @Override
        public void removeObserver(@NonNull Observer<T> observer) {
            Observer realObserver = null;
            if (observerMap.containsKey(observer)) {
                realObserver = observerMap.remove(observer);
            } else {
                realObserver = observer;
            }
            super.removeObserver(realObserver);
            if (!hasObservers()) {
                LiveEventBus.get().bus.remove(key);
            }
        }
    }

    private static class ObserverWrapper<T> implements Observer<T> {

        @NonNull
        private final Observer<T> observer;

        @NonNull
        private final String filterClass;

        @NonNull
        private final String filterMethod;

        ObserverWrapper(@NonNull Observer<T> observer, @NonNull String filterClass, @NonNull String filterMethod) {
            this.observer = observer;
            this.filterClass = filterClass;
            this.filterMethod = filterMethod;
        }

        @Override
        public void onChanged(@Nullable T t) {
            if (isFilterChanged()) {
                return;
            }
            try {
                observer.onChanged(t);
            } catch (ClassCastException e) {
                e.printStackTrace();
            }
        }

        private boolean isFilterChanged() {
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            if (stackTrace != null && stackTrace.length > 0) {
                for (StackTraceElement element : stackTrace) {
                    if (filterClass.equals(element.getClassName()) &&
                            filterMethod.equals(element.getMethodName())) {
                        return true;
                    }
                }
            }
            return false;
        }

    }

    private static <T> ObserverWrapper createForeverObserver(Observer<T> observer) {
        return new ObserverWrapper<>(observer, "android.arch.lifecycle.LiveData",
                "observeForever");
    }

    private static <T> ObserverWrapper createStateObserver(Observer<T> observer) {
        return new ObserverWrapper<>(observer, "android.arch.lifecycle.LiveData$LifecycleBoundObserver",
                "onStateChanged");
    }

    private static class SafeCastObserver<T> implements Observer<T> {

        @NonNull
        private final Observer<T> observer;

        SafeCastObserver(@NonNull Observer<T> observer) {
            this.observer = observer;
        }

        @Override
        public void onChanged(@Nullable T t) {
            try {
                observer.onChanged(t);
            } catch (ClassCastException e) {
                e.printStackTrace();
            }
        }
    }
}
