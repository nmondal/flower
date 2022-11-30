package flower;

import flower.workflow.impl.DynamicExecution;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static flower.workflow.DependencyWorkFlow.Manager.STATUS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class IOWorkflowTest extends MapWorkFlowTest{


    @Test
    public void transformTest(){
        final Map<String,Object> params = new HashMap<>();
        params.put("proto", "https");
        params.put("base", "abc");
        // String
        final String string = "#{proto}://#{base}/comments" ;
        final String rs = DynamicExecution.ZMB.transform(string, params);
        assertEquals("https://abc/comments", rs);
        // list
        final List<String> list = Arrays.asList( "#{proto}", "#{base}" );
        final List<String> rl = DynamicExecution.ZMB.transform(list, params);
        assertEquals( "https", rl.get(0) );
        assertEquals( "abc", rl.get(1) );
        // map
        final Map<String,String> map = new HashMap<>();
        map.put("p", "#{proto}");
        map.put("b", "#{base}");
        final Map<String,String> rm = DynamicExecution.ZMB.transform(map, params);
        assertEquals( "https", rm.get("p") );
        assertEquals( "abc", rm.get("b") );
    }

    @Test
    public void basicWebCallTest() {
        final Map<String,Object> params = new HashMap<>();
        params.put("LARGE_WORDS", 30);
        final String rNode = "get_all_comments";
        Map<String,Object> result = testFile( "samples/web/web.yaml", rNode, params );
        assertEquals(true , result.get(STATUS));
    }
}
