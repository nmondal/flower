package flower;

import org.junit.Assert;
import org.junit.Test;

import java.util.*;

import static flower.workflow.DependencyWorkFlow.Manager.STATUS;
import static org.junit.Assert.*;

public class ForkTest extends MapWorkFlowTest {

    public static Collection<?> runFork( String rNode, int maxFork){
        final Map<String,Object> params = new HashMap<>();
        params.put("max_fork", maxFork);
        Map<String,Object> result = testFile( "samples/fork/for-each.yaml", rNode, params );
        assertEquals(true , result.get(STATUS));
        Object o = result.get(rNode);
        assertTrue( o instanceof Collection<?>);
        assertFalse( ((Collection<?>) o).isEmpty());
        return (Collection<?>) o;
    }

    @Test
    public void testFork(){
        Collection<?> result = runFork("distribute", 10 );
        assertTrue( result instanceof List);
    }

    @Test
    public void testUniqueFork(){
        Collection<?> result = runFork("distribute_unique", 10 );
        assertTrue( result instanceof Set);
    }

    @Test
    public void testAnyQuantifierOK(){
        final String rNode = "switch_over_x" ;
        final Map<String,Object> params = new HashMap<>();
        params.put("x", 0);
        Map<String,Object> result = testFile( "samples/fork/matches.yaml", rNode, params);
        assertEquals(true , result.get(STATUS));
        assertEquals( "a", result.get(rNode));
        params.put("x", 15);
        result = testFile( "samples/fork/matches.yaml", rNode, params);
        assertEquals(true , result.get(STATUS));
        assertEquals( "b", result.get(rNode));
        params.put("x", 25);
        result = testFile( "samples/fork/matches.yaml", rNode, params);
        assertEquals(true , result.get(STATUS));
        assertEquals( "c", result.get(rNode));
    }

    @Test
    public void testAnyFailure(){
        final String rNode = "switch_over_x" ;
        final Map<String,Object> params = new HashMap<>();
        params.put("x", 100);
        Map<String,Object> result = testFile( "samples/fork/matches.yaml", rNode, params);
        assertEquals(false , result.get(STATUS));
    }

    @Test
    public void testAllQuantifierOK(){
        final String rNode = "all_over_x" ;
        final Map<String,Object> params = new HashMap<>();
        // gets all of them
        params.put("x", 0);
        Map<String,Object> result = testFile( "samples/fork/matches.yaml", rNode, params);
        assertEquals(true , result.get(STATUS));
        Object r = result.get(rNode);
        assertTrue( r instanceof Map );
        assertEquals( 3, ((Map<?, ?>) r).size());
        // gets 2 of them
        params.put("x", 15);
        result = testFile( "samples/fork/matches.yaml", rNode, params);
        assertEquals(true , result.get(STATUS));
        r = result.get(rNode);
        assertTrue( r instanceof Map );
        assertEquals( 2, ((Map<?, ?>) r).size());

        // gets only 1
        params.put("x", 25);
        result = testFile( "samples/fork/matches.yaml", rNode, params);
        assertEquals(true , result.get(STATUS));
        r = result.get(rNode);
        assertTrue( r instanceof Map );
        assertEquals( 1, ((Map<?, ?>) r).size());

        // gets none
        params.put("x", 100);
        result = testFile( "samples/fork/matches.yaml", rNode, params);
        assertEquals(true , result.get(STATUS));
        r = result.get(rNode);
        assertTrue( r instanceof Map );
        assertEquals( 0, ((Map<?, ?>) r).size());
    }
}
