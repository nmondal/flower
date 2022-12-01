package flower;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static flower.workflow.DependencyWorkFlow.Manager.STATUS;
import static org.junit.Assert.*;

public class ForkTest extends MapWorkFlowTest {

    @Test
    public void testBasicFork(){
        final Map<String,Object> params = new HashMap<>();
        params.put("max_fork", 10);
        final String rNode = "distribute";
        Map<String,Object> result = testFile( "samples/fork/for-each.yaml", rNode, params );
        assertEquals(true , result.get(STATUS));
        assertTrue( result.get(rNode) instanceof Set);
        assertFalse( ((Set<?>) result.get(rNode)).isEmpty() );
    }
}
