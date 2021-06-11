package flower;

import flower.workflow.DependencyWorkFlow;
import flower.workflow.impl.MapDependencyWorkFlow;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import static flower.workflow.DependencyWorkFlow.Manager.STATUS;
import static org.junit.Assert.*;

public class MapWorkFlowTest {

    static Map<String,Object> testFile(final String path, final String node, final Map<String,Object> params){
        DependencyWorkFlow workFlow = MapDependencyWorkFlow.MANAGER.load(path);
        assertNotNull(workFlow);
        assertFalse(workFlow.nodes().isEmpty());
        Map<String,Object> result = MapDependencyWorkFlow.MANAGER.run(workFlow, node, params);
        assertNotNull(result);
        assertTrue( result.containsKey(node));
        return result;
    }

    @Test
    public void testLoadAndRunJSONWorkFlow() {
        testFile( "samples/1.json", "c", new HashMap<>());
    }

    @Test
    public void testLoadAndRunYAMLWorkFlow() { testFile( "samples/1.yaml", "c", new HashMap<>()); }

    @Test
    public void minPathTest() {
        Map<String,Object> result = testFile( "samples/2.yaml", "e", new HashMap<>());
        assertFalse(result.containsKey("c"));
        assertFalse(result.containsKey("d"));
        assertFalse(result.containsKey("b"));
    }

    @Test
    public void opTests() {
        final Map<String,Object> params = new HashMap<>();
        params.put("x", 1);
        params.put("y", 2);
        String rNode = "+";
        Map<String,Object> result = testFile( "samples/op.yaml", rNode, params );
        assertEquals(3, result.get(rNode));
        rNode = "-";
        result = testFile( "samples/op.yaml", rNode, params );
        assertEquals(-1, result.get(rNode));
        rNode = "*";
        result = testFile( "samples/op.yaml", rNode, params );
        assertEquals(2, result.get(rNode));
    }

    @Test
    public void timeOutTests() {
        final Map<String,Object> params = new HashMap<>();
        long n = 10;
        params.put("n", n);
        String rNode = "c";
        Map<String,Object> result = testFile( "samples/to.yaml", rNode, params );
        Assert.assertEquals(true , result.get(rNode));
        Assert.assertEquals(true , result.get(STATUS));
        // this should time out...
        n = 10000;
        params.put("n", n);
        result = testFile( "samples/to.yaml", rNode, params );
        Assert.assertEquals(false , result.get(STATUS));
        Assert.assertTrue(result.get(rNode) instanceof TimeoutException);
    }

    @Test
    public void scriptErrorTest() {
        final Map<String,Object> params = new HashMap<>();
        params.put("x", 1);
        params.put("y", 2);
        String rNode = "+";
        Map<String,Object> result = testFile( "samples/script_error.yaml", rNode, params );
        Assert.assertEquals(false , result.get(STATUS));
        Assert.assertTrue(result.get(rNode) instanceof RuntimeException);
    }
}
