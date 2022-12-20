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

    @Override
    default Function<Map<String, Object>, Object> body() {
        final String nodeName = name();
        final String transformName = transformName();
        DynamicExecution.FileOrString fs = location();
        Map<String, Object> data = (Map) ZTypes.yaml(fs.content());
        final Object trBody = data.getOrDefault(transformName, "");
        final String dependsOn = dependencies().iterator().next();
        String closureKey  = nodeName + "::" + UUID.randomUUID().toString();
        Transformation<?> transformation = MapBasedTransformationManager.INSTANCE.fromEntry (Map.entry(transformName, trBody), closureKey);
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
