package flower;

import flower.transform.Transformation;
import flower.transform.impl.MapBasedTransform;
import org.junit.Assert;
import org.junit.Test;
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

    @Test
    public void jolt_1(){
        Object o = load( "samples/mappers/j1.json");
        Transformation<?> tr = transformation( "samples/mappers/jolt_all.yaml", "jolt_1");
        Object r = tr.apply(o);
        Assert.assertNotNull(r);
        String js = toFormattedJson(r);
        System.out.println(js);
    }
}
