package flower;

import flower.workflow.Retry;
import org.junit.Test;

import java.util.function.Function;
import static org.junit.Assert.*;

public class RetryTest extends MapWorkFlowTest{

    // integer identity
    static Function<Integer,Integer> ii = (i) ->i;

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
}
