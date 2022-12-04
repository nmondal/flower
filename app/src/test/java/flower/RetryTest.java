package flower;

import flower.workflow.Retry;
import org.junit.Test;

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
    }

    @Test
    public void randomStrategyTest(){
        retryExec( Retry.randomize(1,10));
    }

    @Test
    public void expBackOffStrategyTest(){
        retryExec( Retry.exponentialBackOff(1,10));
    }
}
