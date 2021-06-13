package flower;

import flower.workflow.DependencyWorkFlow;
import flower.workflow.impl.MapDependencyWorkFlow;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import zoomba.lang.core.types.ZTypes;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.Assert.assertNotNull;

public class PerfTest {

    static final double NANO_TO_MS = 0.000001;

    @BeforeClass
    public static void beforeClass() {
        Logger.disable();
    }

    @AfterClass
    public static void afterClass() {
        Logger.enable();
    }

    static void testFile(final String path, final String node, final Map<String, Object> params, int times, double cutOfMS) {
        times = Math.max(times, 10);
        List<Double> elapsedTime = new ArrayList<>();
        for (int i = 0; i < times; i++) {
            final long s = System.nanoTime();
            DependencyWorkFlow workFlow = MapDependencyWorkFlow.MANAGER.load(path);
            Map<String, Object> result = MapDependencyWorkFlow.MANAGER.run(workFlow, node, params);
            assertNotNull(result);
            final long e = System.nanoTime();
            elapsedTime.add((e - s) * NANO_TO_MS);
        }
        elapsedTime.sort((x, y) -> (int) Math.signum(x - y));
        int inx = (int) (times * 0.9);
        double _9pc = elapsedTime.get(inx);
        System.out.printf("Time Taken < %s > -> [%s] with %s (ns) 90%% : %.4f %n",
                path, node, ZTypes.jsonString(params), _9pc);
        Assert.assertTrue("90% is slower than expected", cutOfMS > _9pc);
    }

    @Test
    public void dummyWorkFlowTest() {
        testFile("samples/1.json", "c", Collections.emptyMap(), 100, 1.5);
    }

    @Test
    public void scriptingWorkFlowTest() {
        // tiny work
        Map<String, Object> params = new HashMap<>() {{
            put("x", 1);
            put("y", 2);
        }};
        testFile("samples/op.yaml", "+", params, 500, 5.0);
        // more work
        params = new HashMap<>() {{
            put("x", new BigDecimal("12313132131321.1321312321313"));
            put("y", new BigDecimal("12313132131321.1321312321313"));
        }};
        testFile("samples/op.yaml", "+", params, 500, 5.0);
    }

    @Test
    public void largeFlowTest() throws Exception {
        final String genWFile = DotTest.genWorkFlow( 10, 10 );
        testFile(genWFile, "L_9_0", Collections.emptyMap(), 100, 30.0 );
    }
}
