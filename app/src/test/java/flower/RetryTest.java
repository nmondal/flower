package flower;

import flower.workflow.Retry;
import org.junit.Test;

import java.util.*;
import java.util.function.Function;
import static org.junit.Assert.*;

public class RetryTest extends MapWorkFlowTest{

    // integer identity
    static Function<Integer,Integer> ii = (i) ->i;

    static Function<Integer,Integer> passInKthIter( int k ){
        return new Function<>() {
            int numTries = 0;

            @Override
            public Integer apply(Integer integer) {
                numTries++;
                if (numTries < k) {
                    throw new RuntimeException("Boom!");
                }
                return 42;
            }
        };
    }

    interface IntervalCollector extends Iterable<Long>, Function<Integer,Integer>{}

    public void retryExec(Retry retry ){
        Function<Integer,Integer> wf = retry.withRetry(passInKthIter(2));
        int x = wf.apply(10);
        assertEquals(42,x);
        wf = retry.withRetry(passInKthIter(4));
        Function<Integer, Integer> finalWf = wf;
        Exception exception = assertThrows(Exception.class, () -> {
            finalWf.apply(10);
        });
        String expectedMessage = "Retry exceeded";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    static List<Long> getSpanGaps(Retry r) {
        IntervalCollector intervalCollector = new IntervalCollector() {
            final List<Long> calls = new ArrayList<>();
            @Override
            public Iterator<Long> iterator() {
                return calls.iterator();
            }
            @Override
            public Integer apply(Integer integer) {
                calls.add( new Date().getTime());
                throw new RuntimeException("Boom!");
            }
        };
        final Function<Integer,Integer> wf = r.withRetry(intervalCollector);
        Exception exception = assertThrows(Exception.class, () -> {
            wf.apply(10);
        });
        String expectedMessage = "Retry exceeded";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
        Iterator<Long> timings = intervalCollector.iterator();
        assertTrue( timings.hasNext());
        long last = timings.next();
        List<Long> gaps = new ArrayList<>();
        while ( timings.hasNext() ){
            long cur = timings.next();
            gaps.add(cur-last);
            last = cur;
        }
        assertFalse(gaps.isEmpty());
        return gaps;
    }

    @Test
    public void wrapTests(){
        Function<Integer,Integer> wf = Retry.NOP.withRetry(ii);
        assertEquals(ii,wf);
        wf = Retry.counter(0,10).withRetry(ii);
        assertNotEquals(ii,wf);
        // should be able to run wf?
        final int x = wf.apply(42);
        assertEquals(42,x);
    }

    @Test
    public void counterStrategyTest(){
        retryExec( Retry.counter(1,10));
        List<Long> gaps = getSpanGaps( Retry.counter(10,10) );
        assertEquals(10, gaps.size());
        // gaps should be close to 10, yes?
        boolean allMatch = gaps.stream().allMatch( x -> Math.abs( x - 10) < 5 );
        assertTrue(allMatch);
    }

    @Test
    public void randomStrategyTest(){
        retryExec( Retry.randomize(1,10));
        List<Long> gaps = getSpanGaps( Retry.randomize(10,50) );
        assertEquals(10, gaps.size());
        // gaps should be close to 25~100 , yes?
        boolean allMatch = gaps.stream().allMatch( x -> x >= 25  && x <= 105 );
        assertTrue(allMatch);
    }

    @Test
    public void expBackOffStrategyTest(){
        retryExec( Retry.exponentialBackOff(1,10));
        List<Long> gaps = getSpanGaps( Retry.exponentialBackOff(3,10) );
        assertEquals(3, gaps.size());
        final long first = gaps.get(0);
        // first must be close to 10
        assertTrue(  Math.abs(10-first) < 3 );
        final long second = gaps.get(1);
        int ratio = (int)(second / first) ;
        assertTrue(  ratio >= 2  );
        final long third = gaps.get(2);
        ratio = (int)(third / second) ;
        assertTrue(  ratio >= 2  );
    }
}
