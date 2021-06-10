package flower;

import flower.workflow.DependencyWorkFlow;
import flower.workflow.impl.MapDependencyWorkFlow;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

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
}
