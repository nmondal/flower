package flower.workflow;

import java.util.concurrent.Callable;
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
        wasTimeOut = false;
        t = new Thread(() -> {
            value = m.apply( this);
            wasTimeOut = false;
            callerThread.interrupt();
        });
    }

    @Override
    @SuppressWarnings("deprecation")
    public T call() throws Exception {
        callerThread = Thread.currentThread();
        t.start();
        try {
            Thread.sleep(timeOut);
            wasTimeOut = true;
            if ( t.isAlive() ){
                // TODO, is there a better way? God save us
                t.stop();
            }
        } catch (InterruptedException ie) {
            wasTimeOut = false ;
        }
        t.join();
        return value;
    }
}
