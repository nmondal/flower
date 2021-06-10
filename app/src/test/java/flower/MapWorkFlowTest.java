package flower;

import flower.workflow.DependencyWorkFlow;
import flower.workflow.impl.MapDependencyWorkFlow;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class MapWorkFlowTest {

    static Map<String,Object> testFile(final String path, final String node){
        DependencyWorkFlow workFlow = MapDependencyWorkFlow.MANAGER.load(path);
        assertNotNull(workFlow);
        assertFalse(workFlow.nodes().isEmpty());
        Map<String,Object> result = MapDependencyWorkFlow.MANAGER.run(workFlow, node, new HashMap<>());
        assertNotNull(result);
        assertTrue( result.containsKey(node));
        return result;
    }

    @Test
    public void testLoadAndRunJSONWorkFlow() {
        testFile( "samples/1.json", "c");
    }

    @Test
    public void testLoadAndRunYAMLWorkFlow() {
        testFile( "samples/1.yaml", "c");
    }

    @Test
    public void minPathTest() {
        Map<String,Object> result = testFile( "samples/2.yaml", "e");
        assertFalse(result.containsKey("c"));
        assertFalse(result.containsKey("d"));
        assertFalse(result.containsKey("b"));
    }
}
