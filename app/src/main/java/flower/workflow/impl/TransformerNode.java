package flower.workflow.impl;

import flower.transform.Transformation;
import flower.transform.impl.MapBasedTransform;
import zoomba.lang.core.types.ZTypes;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

public interface TransformerNode extends MapDependencyWorkFlow.MapFNode {

    String TRANSFORM = "transform";

    String NAME = "apply";

    String LOCATION = "from";

    default Map<String, Object> transformConfig() {
        return (Map) config().getOrDefault(TRANSFORM, Collections.emptyMap());
    }

    default String name() {
        return transformConfig().getOrDefault(NAME, "").toString();
    }

    default DynamicExecution.FileOrString location() {
        return getContent( transformConfig(), LOCATION, "");
    }

    @Override
    default Function<Map<String, Object>, Object> body() {
        final String name = name();
        DynamicExecution.FileOrString fs = location();
        Map<String, Object> data = (Map) ZTypes.yaml(fs.content());
        final Object trBody = data.getOrDefault(name, "");
        final String dependsOn = dependencies().iterator().next();
        String closureKey  = name + "::" + UUID.randomUUID().toString();
        Transformation<?> transformation = MapBasedTransform.fromEntry(Map.entry(name, trBody), closureKey);
        return params -> {
            try {
                MapBasedTransform.CLOSURE.put(closureKey, params);
                final Object input = params.get(dependsOn);
                final Object resp = transformation.apply(input);
                return resp;
            } finally {
                MapBasedTransform.CLOSURE.remove(closureKey);
            }
        };
    }
}
