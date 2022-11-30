package flower;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static flower.workflow.DependencyWorkFlow.Manager.STATUS;
import static org.junit.Assert.assertFalse;

public class IOWorkflowTest extends MapWorkFlowTest{

    @Test
    public void basicWebCallTest() {
        final Map<String,Object> params = new HashMap<>();
        params.put("LARGE_WORDS", 30);
        final String rNode = "get_all_comments";
        Map<String,Object> result = testFile( "samples/web/web.yaml", rNode, params );
        Assert.assertEquals(true , result.get(STATUS));
    }
}
