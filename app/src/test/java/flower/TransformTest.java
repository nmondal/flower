package flower;

import flower.transform.Transformation;
import flower.transform.impl.MapBasedTransform;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import zoomba.lang.core.interpreter.ZMethodInterceptor;
import zoomba.lang.core.types.ZTypes;

import java.util.List;
import java.util.Map;

public class TransformTest {

    public static Object load(String jsonFile) {
        Object o = ZTypes.json(jsonFile, true);
        Assert.assertNotNull(o);
        return o;
    }

    @BeforeClass
    public static void beforeClass() {
        Map<?, ?> map = MapBasedTransform.MANAGER.load("samples/mappers/jolt_all.yaml");
        Assert.assertNotNull(map);
        Assert.assertFalse(map.isEmpty());
        map = MapBasedTransform.MANAGER.load("samples/mappers/jslt_mapper.yaml");
        Assert.assertNotNull(map);
        Assert.assertFalse(map.isEmpty());

    }

    public static Transformation<?> transformation(String transformName) {
        Transformation<?> tr = MapBasedTransform.MANAGER.transformation(transformName);
        Assert.assertNotNull(tr);
        Assert.assertNotEquals(Transformation.NULL, tr);
        return tr;
    }

    public static String toFormattedJson(Object o) {
        return ZTypes.jsonPretty(o, true);
    }

    public static void pathAsserter(Object o, String path, Object expected) {
        Object actual = ZMethodInterceptor.Default.jxPath(o, path, false);
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testInvalidTransform() {
        Transformation<?> tr = MapBasedTransform.MANAGER.transformation("foo-bar");
        Assert.assertEquals(Transformation.NULL, tr);
    }

    /*
     *
     *  These are the http://jolt-demo.appspot.com tests
     *  http://jolt-demo.appspot.com
     */
    @Test
    public void jolt_1() {
        Object o = load("samples/mappers/j1.json");
        Transformation<?> tr = transformation( "jolt_1");
        Object r = tr.apply(o);
        Assert.assertNotNull(r);
        String js = toFormattedJson(r);
        System.out.println(js);
        pathAsserter(r, "Rating", 3);
        pathAsserter(r, "Range", 5);
        pathAsserter(r, "SecondaryRatings/quality/Value", 3);
        pathAsserter(r, "SecondaryRatings/quality/Id", "quality");
        pathAsserter(r, "SecondaryRatings/quality/Range", 5);
    }

    @Test
    public void jolt_2() {
        Object o = load("samples/mappers/j3.json");
        Transformation<?> tr = transformation( "jolt_2");
        Object r = tr.apply(o);
        Assert.assertNotNull(r);
        String js = toFormattedJson(r);
        System.out.println(js);
        Assert.assertTrue(r instanceof Map);
        Assert.assertEquals(4, ((Map<?, ?>) r).size());

    }

    @Test
    public void jolt_3x() {
        Object o = load("samples/mappers/j32.json");
        Transformation<?> tr = transformation( "jolt_3x");
        Object r = tr.apply(o);
        Assert.assertNotNull(r);
        String js = toFormattedJson(r);
        System.out.println(js);
        Assert.assertTrue(r instanceof Map);
        Assert.assertEquals(2, ((Map<?, ?>) r).size());

    }

    @Test
    public void jolt_4x() {
        Object o = load("samples/mappers/j2.json");
        Transformation<?> tr = transformation( "jolt_4x");
        Object r = tr.apply(o);
        Assert.assertNotNull(r);
        String js = toFormattedJson(r);
        System.out.println(js);
        pathAsserter(r, "ratingNames[1]", "primary");
        pathAsserter(r, "ratingNames[2]", "quality");

    }

    @Test
    public void jolt_4z() {
        Object o = load("samples/mappers/j2.json");
        Transformation<?> tr = transformation( "jolt_4z");
        Object r = tr.apply(o);
        Assert.assertNotNull(r);
        String js = toFormattedJson(r);
        System.out.println(js);
        pathAsserter(r, "ratingNames[1]", "primary");
        pathAsserter(r, "ratingNames[2]", "quality");

    }

    @Test
    public void jolt_5x() {
        Object o = load("samples/mappers/j5.json");
        Transformation<?> tr = transformation( "jolt_5x");
        Object r = tr.apply(o);
        Assert.assertNotNull(r);
        String js = toFormattedJson(r);
        System.out.println(js);
        pathAsserter(r, "Ratings[1]/Name", "design");
        pathAsserter(r, "Ratings[1]/Value", 5);

    }

    @Test
    public void jolt_5z() {
        Object o = load("samples/mappers/j5.json");
        Transformation<?> tr = transformation( "jolt_5z");
        Object r = tr.apply(o);
        Assert.assertNotNull(r);
        String js = toFormattedJson(r);
        System.out.println(js);
        pathAsserter(r, "Ratings[1]/Name", "design");
        pathAsserter(r, "Ratings[1]/Value", 5);

    }


    @Test
    public void jolt_6x() {
        Object o = load("samples/mappers/j6.json");
        Transformation<?> tr = transformation( "jolt_6x");
        Object r = tr.apply(o);
        Assert.assertNotNull(r);
        String js = toFormattedJson(r);
        System.out.println(js);
        Assert.assertTrue(r instanceof Map);
        Assert.assertEquals(6, ((Map<?, ?>) r).size());
    }


    @Test
    public void jolt_7x() {
        Object o = load("samples/mappers/j7.json");
        Transformation<?> tr = transformation( "jolt_7x");
        Object r = tr.apply(o);
        Assert.assertNotNull(r);
        String js = toFormattedJson(r);
        System.out.println(js);
        Object actual = ZMethodInterceptor.Default.jxPath(r, "//clients", false);
        Assert.assertTrue(actual instanceof Map);
        Assert.assertEquals(2, ((Map<?, ?>) actual).size());

    }

    @Test
    public void jolt_8z() {
        Object o = load("samples/mappers/j8.json");
        Transformation<?> tr = transformation( "jolt_8z");
        Object r = tr.apply(o);
        Assert.assertNotNull(r);
        String js = toFormattedJson(r);
        System.out.println(js);
        Object actual = ZMethodInterceptor.Default.jxPath(r, "//bookMap", false);
        Assert.assertTrue(actual instanceof Map);
        Assert.assertEquals(1, ((Map<?, ?>) actual).size());

    }

    @Test
    public void jolt_9x() {
        Object o = load("samples/mappers/j9.json");
        Transformation<?> tr = transformation( "jolt_9x");
        Object r = tr.apply(o);
        Assert.assertNotNull(r);
        String js = toFormattedJson(r);
        System.out.println(js);
        Object actual = ZMethodInterceptor.Default.jxPath(r, "//clientIds", false);
        Assert.assertTrue(actual instanceof List);
        Assert.assertEquals(2, ((List<?>) actual).size());
    }

    @Test
    public void jolt_11x() {
        Object o = load("samples/mappers/j11.json");
        Transformation<?> tr = transformation( "jolt_11x");
        Object r = tr.apply(o);
        Assert.assertNotNull(r);
        String js = toFormattedJson(r);
        System.out.println(js);
        Assert.assertTrue(r instanceof Map);
        Assert.assertEquals(3, ((Map<?, ?>) r).size());
        Object bi = ((Map<?, ?>) r).get("basket_item");
        Assert.assertTrue(bi instanceof List);
        Assert.assertEquals(2, ((List<?>) bi).size());

    }

    @Test
    public void jolt_12x() {
        Object o = load("samples/mappers/j12.json");
        Transformation<?> tr = transformation( "jolt_12x");
        Object r = tr.apply(o);
        Assert.assertNotNull(r);
        String js = toFormattedJson(r);
        System.out.println(js);
        Assert.assertTrue(r instanceof Map);
        Object actual = ZMethodInterceptor.Default.jxPath(r, "alpha", false);
        Assert.assertTrue(actual instanceof List);
        Assert.assertEquals(((List<?>) actual).size(), 2);

        actual = ZMethodInterceptor.Default.jxPath(r, "beta", false);
        Assert.assertTrue(actual instanceof List);
        Assert.assertEquals(((List<?>) actual).size(), 1);

    }

    @Test
    public void jolt_12z() {
        Object o = load("samples/mappers/j12.json");
        Transformation<?> tr = transformation( "jolt_12z");
        Object r = tr.apply(o);
        Assert.assertNotNull(r);
        String js = toFormattedJson(r);
        System.out.println(js);
        Assert.assertTrue(r instanceof Map);
        Object actual = ZMethodInterceptor.Default.jxPath(r, "alpha", false);
        Assert.assertTrue(actual instanceof List);
        Assert.assertEquals(((List<?>) actual).size(), 2);

        actual = ZMethodInterceptor.Default.jxPath(r, "beta", false);
        Assert.assertTrue(actual instanceof List);
        Assert.assertEquals(((List<?>) actual).size(), 1);
    }

    @Test
    public void jolt_13x() {
        Object o = load("samples/mappers/j13.json");
        Transformation<?> tr = transformation( "jolt_13x");
        Object r = tr.apply(o);
        Assert.assertNotNull(r);
        String js = toFormattedJson(r);
        System.out.println(js);

    }

    /**
     * These are the JSLT failures - where too much code was written to get things done...
     */
    @Test
    public void mapperIssueJSLT() {
        // https://github.com/schibsted/jslt/discussions/267
        Object o = load("samples/mappers/input_data_1.json");
        Transformation<?> tr = transformation( "nested_mapper");
        Object r = tr.apply(o);
        Assert.assertNotNull(r);
        String js = toFormattedJson(r);
        System.out.println(js);
        Assert.assertTrue(r instanceof List<?>);
        Assert.assertEquals(8, ((List<?>) r).size());
        ((List<?>) r).forEach((item) -> {
            Assert.assertTrue(item instanceof Map<?, ?>);
            Assert.assertEquals(6, ((Map<?, ?>) item).size());
        });
    }

}
