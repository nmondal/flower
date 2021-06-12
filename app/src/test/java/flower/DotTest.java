package flower;

import flower.workflow.DotGraph;
import flower.workflow.impl.MapDependencyWorkFlow;
import org.junit.Assert;
import org.junit.Test;

public class DotTest {
    public static void buildDot(String workflowFile, String outFile) {
        try {
            DotGraph.dot( MapDependencyWorkFlow.MANAGER.load(workflowFile), outFile);
        }catch ( Exception e){
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void dotOnJSONTest() {
        buildDot("samples/1.json", "samples/1.dot");
    }
}
