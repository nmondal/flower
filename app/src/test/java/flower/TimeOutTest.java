package flower;

import flower.workflow.FCallable;
import org.junit.Test;

import java.util.Optional;
import java.util.concurrent.*;

import static org.junit.Assert.*;

public class TimeOutTest {

    /**
     * Showing how easy it is to use this as primitive to create a with timeout
     * @param timeout amount of timeout in ms
     * @param callable something that produce a result
     * @return result of the callable in Optional container
     *    if timeout did not happen, then it is non-empty
     *    if timeout happened then it is empty
     * @param <T> type of the result
     */
    public static <T> Optional<T> withTimeout(long timeout, Callable<T> callable){
        FCallable<T> fc = new FCallable<>( timeout, (me) -> {
            try {
                return callable.call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        ExecutorService es = Executors.newSingleThreadExecutor();
        Future<T> future = es.submit(fc);
        try {
            return Optional.of(future.get());
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        } catch ( NullPointerException e){
            // Optional.of(null) would come here...
            // that is timeout
            return Optional.empty();
        }
    }

    @Test
    public void waitWithLongTimeout(){
        Optional<Integer> val = withTimeout( 100000, () ->{
           Thread.sleep(900);
           return 42;
        });
        assertFalse( val.isEmpty());
        assertEquals(42 , val.get().intValue() );
    }

    @Test
    public void waitWithSmallTimeout(){
        Optional<Integer> val  = withTimeout( 100, () ->{
            Thread.sleep(900);
            return 42;
        });
        assertTrue( val.isEmpty());
    }
}
