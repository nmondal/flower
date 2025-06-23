package flower;

import org.junit.Assert;
import org.junit.Test;

import java.util.*;
import static flower.workflow.DependencyWorkFlow.Manager.STATUS;

/**
 * The core of the Retry is moved to ZoomBA now, we just simply use that Decorator
 */
public class RetryTest extends MapWorkFlowTest{

    @Test
    public void retryInWorkFlowTest(){
        final Map<String,Object> params = new HashMap<>();
        params.put("fail_unto", 100); // too many times failure
        final String rNode = "outcome";
        Map<String,Object> result = testFile( "samples/retry/retry.yaml", rNode, params );
        Assert.assertEquals(false , result.get(STATUS));
        params.put("fail_unto", 2); // less, so should pass
        result = testFile( "samples/retry/retry.yaml", rNode, params );
        Assert.assertEquals(true , result.get(STATUS));
    }
}
