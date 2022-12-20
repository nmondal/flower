package flower.workflow.impl;

import flower.transform.Transformation;
import flower.transform.impl.MapBasedTransformationManager;
import zoomba.lang.core.types.ZTypes;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

public interface TransformerNode extends MapDependencyWorkFlow.MapFNode {

    String TRANSFORM = "transform";

    String TRANSFORM_NAME = "apply";

    String LOCATION = "from";

    String CONTEXT_OBJECT = "_$" ;


    default Map<String, Object> transformConfig() {
        return (Map) config().getOrDefault(TRANSFORM, Collections.emptyMap());
    }

    default String transformName() {
        return transformConfig().getOrDefault(TRANSFORM_NAME, "").toString();
    }

    default DynamicExecution.FileOrString location() {
        return getContent( transformConfig(), LOCATION, "");
    }

    interface WithClosure extends MapBasedTransformationManager {
        Map<String,Map<String,Object>> closure();

        default Function<Map<String,Object>, Object> applyTransform( String path, String closureKey, String transformName, String inputKey){
            Transformation<?> transformation =  transformation( path + "#" + closureKey + "#" + transformName );
            return params -> {
                try {
                    closure().put(closureKey, params);
                    final Object input = params.get(inputKey);
                    final Object resp = transformation.apply(input);
                    return resp;
                } finally {
                    closure().remove (closureKey);
                }
            };
        }
    }

    WithClosure MANAGER = new WithClosure() {
        final Map<String,Map<String,Object>> cache = MapBasedTransformationManager.lru(42);
        final Map<String,Map<String,Object>> closure = new HashMap<>();

        @Override
        public Map<String, Map<String, Object>> closure() {
            return closure;
        }

        @Override
        public Map<String, Transformation<?>> load(String path) {
            if ( !cache.containsKey(path)){
                Map<String, Object> data = (Map<String,Object>) ZTypes.yaml(path, true);
                cache.put(path,data);
            }
            return Collections.emptyMap();
        }

        @Override
        public Transformation<?> transformation(String name) {
            String[] arr = name.split("#");
            final String path = arr[0];
            if ( !cache.containsKey(path)) return Transformation.NULL;
            final String closureKey = arr[1];
            final String transformName = arr[2];
            final Object trBody = cache.get(path).getOrDefault(transformName, "");
            return fromEntry (Map.entry(transformName, trBody), closureKey);
        }

        @Override
        public Map<String,Object> createInput(Object o, String transformPath){
            Map<String,Object> input = WithClosure.super.createInput(o, transformPath);
            String[] arr = transformPath.split("/");
            String closureKey = arr[0];
            if ( closure.containsKey( closureKey ) ){
                Map<String,Object> ctx = closure.get(closureKey);
                input.put(CONTEXT_OBJECT, ctx);
            }
            return input;
        }

    };

    @Override
    default Function<Map<String, Object>, Object> body() {
        final String nodeName = name();
        final String transformName = transformName();
        final String path = location().path();;
        MANAGER.load(path);
        final String dependsOn = dependencies().iterator().next();
        String closureKey  = nodeName + "::" + UUID.randomUUID().toString();
        return MANAGER.applyTransform( path, closureKey, transformName, dependsOn);
    }
}
