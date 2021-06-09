package flower;

import flower.workflow.DependencyWorkFlow;
import flower.workflow.impl.MapDependencyWorkFlow;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class MapWorkFlowTest {

    static void testFile(final String path, final String node){
        DependencyWorkFlow workFlow = MapDependencyWorkFlow.MANAGER.load(path);
        assertNotNull(workFlow);
        assertFalse(workFlow.nodes().isEmpty());
        Map<String,Object> result = MapDependencyWorkFlow.MANAGER.run(workFlow, node, new HashMap<>());
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    public void testLoadAndRunJSONWorkFlow() {
        testFile( "samples/1.json", "c");
    }
    @Test
    public void testLoadAndRunYAMLWorkFlow() {
        testFile( "samples/1.yaml", "c");
    }
}
