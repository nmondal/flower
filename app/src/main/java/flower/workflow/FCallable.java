package flower.workflow;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

public class FCallable<T> implements Callable<T> {

    public final Supplier<T> m;

    public final Runnable e;

    public final Thread t;

    protected T value;

    protected Thread callerThread;

    public final long timeOut;

    private boolean wasDone = false;

    public FCallable(long timeOut, Supplier<T> body , Runnable timeOutHook) {
        m = body;
        e = timeOutHook;
        this.timeOut = timeOut;
        t = new Thread(() -> {
            value = m.get();
            wasDone = true;
            callerThread.interrupt();
        });
    }

    @Override
    public T call() throws Exception {
        callerThread = Thread.currentThread();
        t.start();
        try {
            Thread.sleep(timeOut);
            if ( t.isAlive() ){
                t.stop();
            }
        } catch (InterruptedException ie) {
        }
        if ( !wasDone ) {
            e.run();
            throw new TimeoutException();
        }
        return value;
    }
}
