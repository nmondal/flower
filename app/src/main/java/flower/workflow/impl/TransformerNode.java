package flower.workflow.impl;

import flower.transform.Transformation;
import flower.transform.impl.MapBasedTransformationManager;
import zoomba.lang.core.types.ZTypes;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

public interface TransformerNode extends MapDependencyWorkFlow.MapFNode {

    String TRANSFORM = "transform";

    String TRANSFORM_NAME = "apply";

    String LOCATION = "from";

    default Map<String, Object> transformConfig() {
        return (Map) config().getOrDefault(TRANSFORM, Collections.emptyMap());
    }

    default String transformName() {
        return transformConfig().getOrDefault(TRANSFORM_NAME, "").toString();
    }

    default DynamicExecution.FileOrString location() {
        return getContent( transformConfig(), LOCATION, "");
    }

    MapBasedTransformationManager MANAGER = new MapBasedTransformationManager() {
        final Map<String,Map<String,Object>> cache = MapBasedTransformationManager.lru(42);
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
    };

    @Override
    default Function<Map<String, Object>, Object> body() {
        final String nodeName = name();
        final String transformName = transformName();
        final String path = location().path();;
        MANAGER.load(path);
        final String dependsOn = dependencies().iterator().next();
        String closureKey  = nodeName + "::" + UUID.randomUUID().toString();
        Transformation<?> transformation = MANAGER.transformation( path + "#" + closureKey + "#" + transformName );
        return params -> {
            try {
                MapBasedTransformationManager.setupClosure (closureKey, params);
                final Object input = params.get(dependsOn);
                final Object resp = transformation.apply(input);
                return resp;
            } finally {
                MapBasedTransformationManager.removeClosure(closureKey);
            }
        };
    }
}
