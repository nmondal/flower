package flower.workflow;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

public class FCallable<T> implements Callable<T> {

    public final Function<FCallable<T>,T> m;

    public final Thread t;

    protected T value;

    protected Thread callerThread;

    public final long timeOut;

    private boolean wasTimeOut;

    public boolean wasTimeOut(){ return wasTimeOut; }

    public FCallable(long timeOut, Function<FCallable<T>,T> body ) {
        m = body;
        this.timeOut = timeOut;
        t = new Thread(() -> {
            wasTimeOut = true;
            value = m.apply( this);
            wasTimeOut = false;
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
        if ( wasTimeOut) {
            throw new TimeoutException();
        }
        return value;
    }
}
