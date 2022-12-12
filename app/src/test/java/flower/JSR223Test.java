package flower;

import flower.workflow.DependencyWorkFlow;
import flower.workflow.impl.MapDependencyWorkFlow;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static flower.MapWorkFlowTest.testFile;
import static org.junit.Assert.*;

public class JSR223Test {

    static Map<String,Object> testFileWithEngine(final String path, String engine, final String node,
                                                 final Map<String,Object> params){
        DependencyWorkFlow workFlow = MapDependencyWorkFlow.MANAGER.load(path);
        assertNotNull(workFlow);
        assertFalse(workFlow.nodes().isEmpty());
        ((MapDependencyWorkFlow)workFlow).engine(engine);
        Map<String,Object> result = MapDependencyWorkFlow.MANAGER.run(workFlow, node, params);
        assertNotNull(result);
        return result;
    }

    // TODO in Java 17 there are no JS Engine - https://stackoverflow.com/questions/71481562/use-javascript-scripting-engine-in-java-17
    @Test
    @Ignore
    public void testDefaultShippedEngines(){
        final Map<String,Object> params = new HashMap<>();
        params.put("x", 1);
        params.put("y", 2);
        final String rNode = "+";

        //JS (Nashorn) gets run here
        Map<String,Object> result = testFileWithEngine( "samples/op.yaml", "js" , rNode, params );
        assertEquals(3.0, result.get(rNode));

        //Groovy gets run here
        result = testFileWithEngine( "samples/op.yaml", "groovy" , rNode, params );
        assertEquals(3, result.get(rNode));

        //Python ( Jython ) gets run here
        result = testFileWithEngine( "samples/op.yaml", "python" , rNode, params );
        assertEquals(3, result.get(rNode));

    }

}
