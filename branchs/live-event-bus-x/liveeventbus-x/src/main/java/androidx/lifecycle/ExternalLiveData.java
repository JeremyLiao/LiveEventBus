package androidx.lifecycle;


import java.lang.reflect.Field;
import java.lang.reflect.Method;

import androidx.annotation.NonNull;

import static androidx.lifecycle.Lifecycle.State.CREATED;
import static androidx.lifecycle.Lifecycle.State.DESTROYED;

/**
 * Created by liaohailiang on 2019/3/7.
 */
public class ExternalLiveData<T> extends MutableLiveData<T> {

    @Override
    public void observe(@NonNull LifecycleOwner owner, @NonNull Observer<? super T> observer) {
        if (owner.getLifecycle().getCurrentState() == DESTROYED) {
            // ignore
            return;
        }
        try {
            //use ExternalLifecycleBoundObserver instead of LifecycleBoundObserver
            LifecycleBoundObserver wrapper = new ExternalLifecycleBoundObserver(owner, observer);
            wrapper.mLastVersion = getVersion();
            //Replace "ObserverWrapper existing = mObservers.putIfAbsent(observer, wrapper);"
            Object existing = callMethodPutIfAbsent(observer, wrapper);
            //Replace "if (existing != null && !existing.isAttachedTo(owner)) {"
            if (existing != null && !callMethodIsAttachedTo(existing, owner)) {
                throw new IllegalArgumentException("Cannot add the same observer"
                        + " with different lifecycles");
            }
            if (existing != null) {
                return;
            }
            owner.getLifecycle().addObserver(wrapper);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void observeSticky(@NonNull LifecycleOwner owner, @NonNull Observer<T> observer) {
        super.observe(owner, observer);
    }

    /**
     * determine when the observer is active, means the observer can receive message
     * the default value is CREATED, means if the observer's state is above create,
     * for example, the onCreate() of activity is called
     * you can change this value to CREATED/STARTED/RESUMED
     * determine on witch state, you can receive message
     *
     * @return
     */
    protected Lifecycle.State observerActiveLevel() {
        return CREATED;
    }

    class ExternalLifecycleBoundObserver extends LifecycleBoundObserver {

        public ExternalLifecycleBoundObserver(@NonNull LifecycleOwner owner, Observer<? super T> observer) {
            super(owner, observer);
        }

        @Override
        boolean shouldBeActive() {
            return mOwner.getLifecycle().getCurrentState().isAtLeast(observerActiveLevel());
        }
    }

    private Object getFieldObservers() throws Exception {
        Field fieldObservers = LiveData.class.getDeclaredField("mObservers");
        fieldObservers.setAccessible(true);
        Object objectObservers = fieldObservers.get(this);
        return objectObservers;
    }

    private Object callMethodPutIfAbsent(Object observer, Object wrapper) throws Exception {
        Object mObservers = getFieldObservers();
        Class<?> classOfSafeIterableMap = mObservers.getClass();
        Method putIfAbsent = classOfSafeIterableMap.getDeclaredMethod("putIfAbsent",
                Object.class, Object.class);
        putIfAbsent.setAccessible(true);
        return putIfAbsent.invoke(mObservers, observer, wrapper);
    }

    private boolean callMethodIsAttachedTo(Object caller, Object owner) throws Exception {
        Class<?> type = caller.getClass().getSuperclass();
        Method isAttachedTo = type.getDeclaredMethod("isAttachedTo", LifecycleOwner.class);
        isAttachedTo.setAccessible(true);
        return (boolean) isAttachedTo.invoke(caller, owner);
    }
}
