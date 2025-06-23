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
            final long s = System.currentTimeMillis();
            DependencyWorkFlow workFlow = MapDependencyWorkFlow.MANAGER.load(path);
            Map<String, Object> result = MapDependencyWorkFlow.MANAGER.run(workFlow, node, params);
            assertNotNull(result);
            final long e = System.currentTimeMillis();
            elapsedTime.add((e - s)*1.0);
        }
        elapsedTime.sort((x, y) -> (int) Math.signum(x - y));
        int inx = (int) (times * 0.9);
        double _9pc = elapsedTime.get(inx);
        inx = (int) (times * 0.5);
        double _5pc = elapsedTime.get(inx);

        System.out.printf("Time Taken < %s > -> [%s] with %s (ns) (50%% : %.4f) (90%% : %.4f) %n",
                path, node, ZTypes.jsonString(params), _5pc, _9pc);
        Assert.assertTrue("90% is slower than expected", cutOfMS > _9pc);
    }

    @Test
    public void dummyWorkFlowTest() {
        testFile("samples/1.json", "c", Collections.emptyMap(), 100, 5.0);
    }

    @Test
    public void scriptingWorkFlowTest() {
        // tiny work
        Map<String, Object> params = new HashMap<>();
        // it is absolutely bad idea NOT to have an inline map. in Java.
        params.put("x", 1);
        params.put("y", 2);

        testFile("samples/op.yaml", "+", params, 500, 5.0);
        // more work
        params.put("x", new BigDecimal("12313132131321.1321312321313"));
        params.put("y", new BigDecimal("12313132131321.1321312321313"));

        testFile("samples/op.yaml", "+", params, 500, 5.0);
    }

    @Test
    public void largeFlowTest() throws Exception {
        final String genWFile = DotTest.genWorkFlow(10, 10);
        testFile(genWFile, "L_9_0", Collections.emptyMap(), 100, 100.0);
    }
}
