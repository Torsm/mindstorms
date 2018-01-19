package de.thkoeln.mindstorms.concurrency;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * ObservableRequest
 */
public class ObservableRequest<T> implements Future<T> {
    private final CountDownLatch latch = new CountDownLatch(1);
    private final List<CompletionListener<T>> listeners = new ArrayList<>();
    private Method method;
    private T value;

    public ObservableRequest(Method method) {
        this.method = method;
    }

    public ObservableRequest(T value) {
        complete(value);
    }

    public ObservableRequest() {
        complete(null);
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return latch.getCount() == 0;
    }

    @Override
    public T get() throws InterruptedException {
        latch.await();
        return value;
    }

    @Override
    public T get(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
        if (latch.await(timeout, unit))
            return value;
        throw new TimeoutException();
    }

    public T await() {
        try {
            return get();
        } catch (InterruptedException e) {
            return null;
        }
    }

    public Method getMethod() {
        return method;
    }

    public void complete(Object value) {
        if (value != null)
            this.value = (T) value;
        latch.countDown();
        synchronized (listeners) {
            for (CompletionListener<T> listener : listeners) {
                listener.onCompleted(this.value);
            }
        }
    }

    public ObservableRequest<T> onComplete(CompletionListener<T> listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
        if (isDone()) {
            listener.onCompleted(value);
        }
        return this;
    }

    public interface CompletionListener<T> {
        void onCompleted(T result);
    }
}
