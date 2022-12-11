package flower;

import flower.transform.Transformation;
import flower.transform.impl.MapBasedTransform;
import org.junit.Assert;
import org.junit.Test;
import zoomba.lang.core.interpreter.ZMethodInterceptor;
import zoomba.lang.core.types.ZTypes;

import java.util.Map;

public class TransformTest {

    public static Object load( String jsonFile){
        Object o = ZTypes.json(jsonFile,true);
        Assert.assertNotNull(o);
        return o;
    }

    public static Transformation<?> transformation( String mapFile, String transformName){
        Object tm = ZTypes.yaml(mapFile,true);
        Assert.assertTrue( tm instanceof Map );
        Map<String,Object> transforms = (Map<String, Object>)tm;
        Object val = transforms.get(transformName);
        Assert.assertNotNull(val);
        Map.Entry<String,Object> entry = Map.entry(transformName, val);
        return MapBasedTransform.fromEntry(entry);
    }

    public static String toFormattedJson( Object o){
        return ZTypes.jsonPretty(o,true);
    }

    public static void pathAsserter(Object o, String path, Object expected){
       Object actual = ZMethodInterceptor.Default.jxPath(o, path, false);
       Assert.assertEquals(expected,actual);
    }

    @Test
    public void jolt_1(){
        Object o = load( "samples/mappers/j1.json");
        Transformation<?> tr = transformation( "samples/mappers/jolt_all.yaml", "jolt_1");
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
    public void jolt_4x(){
        Object o = load( "samples/mappers/j2.json");
        Transformation<?> tr = transformation( "samples/mappers/jolt_all.yaml", "jolt_4x");
        Object r = tr.apply(o);
        Assert.assertNotNull(r);
        String js = toFormattedJson(r);
        System.out.println(js);
        pathAsserter(r,"ratingNames[1]", "primary");
        pathAsserter(r,"ratingNames[2]", "quality");

    }

    @Test
    public void jolt_4z(){
        Object o = load( "samples/mappers/j2.json");
        Transformation<?> tr = transformation( "samples/mappers/jolt_all.yaml", "jolt_4z");
        Object r = tr.apply(o);
        Assert.assertNotNull(r);
        String js = toFormattedJson(r);
        System.out.println(js);
        pathAsserter(r,"ratingNames[1]", "primary");
        pathAsserter(r,"ratingNames[2]", "quality");

    }
}
