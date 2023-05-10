package flower;

import java.util.concurrent.Callable;

public interface AutoTimer{

    static <R> R withTime( Callable<R> inner){
        Throwable th = new Throwable();
        StackTraceElement elem = th.getStackTrace()[1];
        final long cur = System.nanoTime();
        try {
            R result = inner.call();
            final long end = System.nanoTime();
            System.out.printf("#%s => %d %n", elem, end-cur);
            return result;
        }catch (Exception e){
           throw new RuntimeException(e);
        }
    }
}
