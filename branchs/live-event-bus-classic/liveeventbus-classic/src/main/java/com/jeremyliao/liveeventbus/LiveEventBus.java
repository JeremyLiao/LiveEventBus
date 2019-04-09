package com.jeremyliao.liveeventbus;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.LifecycleRegistry;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by hailiangliao on 2018/7/4.
 */

public final class LiveEventBus {

    private final Map<String, BusMutableLiveData<Object>> bus;

    private LiveEventBus() {
        bus = new HashMap<>();
    }

    private static class SingletonHolder {
        private static final LiveEventBus DEFAULT_BUS = new LiveEventBus();
    }

    public static LiveEventBus get() {
        return SingletonHolder.DEFAULT_BUS;
    }

    public synchronized <T> Observable<T> with(String key, Class<T> type) {
        if (!bus.containsKey(key)) {
            bus.put(key, new BusMutableLiveData<>(key));
        }
        return (Observable<T>) bus.get(key);
    }

    public Observable<Object> with(String key) {
        return with(key, Object.class);
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

    private static class BusMutableLiveData<T> extends MutableLiveData<T> implements Observable<T> {

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


        private BusMutableLiveData(String key) {
            this.key = key;
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
            SafeCastObserver<T> safeCastObserver = new SafeCastObserver<>(observer);
            //保存LifecycleOwner的当前状态
            Lifecycle lifecycle = owner.getLifecycle();
            Lifecycle.State currentState = lifecycle.getCurrentState();
            int observerSize = getLifecycleObserverMapSize(lifecycle);
            boolean needChangeState = currentState.isAtLeast(Lifecycle.State.STARTED);
            if (needChangeState) {
                //把LifecycleOwner的状态改为INITIALIZED
                setLifecycleState(lifecycle, Lifecycle.State.INITIALIZED);
                //set observerSize to -1，否则super.observe(owner, observer)的时候会无限循环
                setLifecycleObserverMapSize(lifecycle, -1);
            }
            super.observe(owner, safeCastObserver);
            if (needChangeState) {
                //重置LifecycleOwner的状态
                setLifecycleState(lifecycle, currentState);
                //重置observer size，因为又添加了一个observer，所以数量+1
                setLifecycleObserverMapSize(lifecycle, observerSize + 1);
                //把Observer置为active
                hookObserverActive(safeCastObserver, true);
            }
            //更改Observer的version
            hookObserverVersion(safeCastObserver);
        }

        public void observeSticky(@NonNull LifecycleOwner owner, @NonNull Observer<T> observer) {
            super.observe(owner, new SafeCastObserver<>(observer));
        }

        @Override
        public void observeForever(@NonNull Observer<T> observer) {
            if (!observerMap.containsKey(observer)) {
                observerMap.put(observer, new ObserverWrapper(observer));
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

        private void setLifecycleObserverMapSize(Lifecycle lifecycle, int size) {
            if (lifecycle == null) {
                return;
            }
            if (!(lifecycle instanceof LifecycleRegistry)) {
                return;
            }
            try {
                Field observerMapField = LifecycleRegistry.class.getDeclaredField("mObserverMap");
                observerMapField.setAccessible(true);
                Object mObserverMap = observerMapField.get(lifecycle);
                Class<?> superclass = mObserverMap.getClass().getSuperclass();
                Field mSizeField = superclass.getDeclaredField("mSize");
                mSizeField.setAccessible(true);
                mSizeField.set(mObserverMap, size);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private int getLifecycleObserverMapSize(Lifecycle lifecycle) {
            if (lifecycle == null) {
                return 0;
            }
            if (!(lifecycle instanceof LifecycleRegistry)) {
                return 0;
            }
            try {
                Field observerMapField = LifecycleRegistry.class.getDeclaredField("mObserverMap");
                observerMapField.setAccessible(true);
                Object mObserverMap = observerMapField.get(lifecycle);
                Class<?> superclass = mObserverMap.getClass().getSuperclass();
                Field mSizeField = superclass.getDeclaredField("mSize");
                mSizeField.setAccessible(true);
                return (int) mSizeField.get(mObserverMap);
            } catch (Exception e) {
                e.printStackTrace();
                return 0;
            }
        }

        private void setLifecycleState(Lifecycle lifecycle, Lifecycle.State state) {
            if (lifecycle == null) {
                return;
            }
            if (!(lifecycle instanceof LifecycleRegistry)) {
                return;
            }
            try {
                Field mState = LifecycleRegistry.class.getDeclaredField("mState");
                mState.setAccessible(true);
                mState.set(lifecycle, state);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private Object getObserverWrapper(@NonNull Observer<T> observer) throws Exception {
            Field fieldObservers = LiveData.class.getDeclaredField("mObservers");
            fieldObservers.setAccessible(true);
            Object objectObservers = fieldObservers.get(this);
            Class<?> classObservers = objectObservers.getClass();
            Method methodGet = classObservers.getDeclaredMethod("get", Object.class);
            methodGet.setAccessible(true);
            Object objectWrapperEntry = methodGet.invoke(objectObservers, observer);
            Object objectWrapper = null;
            if (objectWrapperEntry instanceof Map.Entry) {
                objectWrapper = ((Map.Entry) objectWrapperEntry).getValue();
            }
            return objectWrapper;
        }

        private void hookObserverVersion(@NonNull Observer<T> observer) {
            try {
                //get wrapper's version
                Object objectWrapper = getObserverWrapper(observer);
                if (objectWrapper == null) {
                    return;
                }
                Class<?> classObserverWrapper = objectWrapper.getClass().getSuperclass();
                Field fieldLastVersion = classObserverWrapper.getDeclaredField("mLastVersion");
                fieldLastVersion.setAccessible(true);
                //get livedata's version
                Field fieldVersion = LiveData.class.getDeclaredField("mVersion");
                fieldVersion.setAccessible(true);
                Object objectVersion = fieldVersion.get(this);
                //set wrapper's version
                fieldLastVersion.set(objectWrapper, objectVersion);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void hookObserverActive(@NonNull Observer<T> observer, boolean active) {
            try {
                //get wrapper's version
                Object objectWrapper = getObserverWrapper(observer);
                if (objectWrapper == null) {
                    return;
                }
                Class<?> classObserverWrapper = objectWrapper.getClass().getSuperclass();
                Field mActive = classObserverWrapper.getDeclaredField("mActive");
                mActive.setAccessible(true);
                mActive.set(objectWrapper, active);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static class ObserverWrapper<T> implements Observer<T> {

        @NonNull
        private final Observer<T> observer;

        ObserverWrapper(@NonNull Observer<T> observer) {
            this.observer = observer;
        }

        @Override
        public void onChanged(@Nullable T t) {
            if (isCallOnObserve()) {
                return;
            }
            try {
                observer.onChanged(t);
            } catch (ClassCastException e) {
                e.printStackTrace();
            }
        }

        private boolean isCallOnObserve() {
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            if (stackTrace != null && stackTrace.length > 0) {
                for (StackTraceElement element : stackTrace) {
                    if ("android.arch.lifecycle.LiveData".equals(element.getClassName()) &&
                            "observeForever".equals(element.getMethodName())) {
                        return true;
                    }
                }
            }
            return false;
        }
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
