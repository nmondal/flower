package flower.workflow.impl;

import flower.Logger;
import flower.workflow.DependencyWorkFlow;
import flower.workflow.Retry;
import zoomba.lang.core.types.ZNumber;
import zoomba.lang.core.types.ZTypes;

import java.io.File;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

@SuppressWarnings("unchecked")
public interface MapDependencyWorkFlow extends DependencyWorkFlow {

    String ENGINE = "engine" ;

    String NAME = "name";

    String PARAMS = "params";

    String CONSTANTS = "constants" ;

    String NODES = "nodes";

    String TIME_OUT = "timeout";

    String OWNER = "owner" ;

    String LOAD_DIR = "_dir" ;

    String ABS_FILE = "_file" ;

    String FROM_FILE = "@" ;

    String RELATIVE_PATH = "_/" ;

    static String fromFile( String filePath, String baseDir){
        return "" ;
    }

    default String dir() {
        return (String) config().getOrDefault(LOAD_DIR, "");
    }

    interface MapFNode extends DependencyWorkFlow.FNode {

        String WHEN = "when" ;

        String BODY = "body" ;

        String DEPENDS = "depends" ;

        String RETRY = "retry" ;


        Map<String,Object> config();

        @Override
        default String name() {
            return config().getOrDefault(NAME, "").toString();
        }

        default String engine() {
            return (String) config().getOrDefault(ENGINE, "");
        }

        @Override
        default long timeOut() {
            return ZNumber.integer(config().getOrDefault(TIME_OUT, Long.MAX_VALUE).toString()).longValue();
        }

        @Override
        default Retry retry() {
            Map<String,Object> retryConfig = (Map) config().getOrDefault(RETRY, Collections.emptyMap());
            return Retry.fromConfig(retryConfig);
        }

        @Override
        default Map<String, Object> params() {
            return (Map) config().getOrDefault(PARAMS, Collections.emptyMap());
        }

        default DynamicExecution.FileOrString getContent(final String propName, String defaultValue){
            return getContent(config(), propName, defaultValue);
        }

       default DynamicExecution.FileOrString getContent(final Map<String,Object> config, final String propName, String defaultValue){
            String propValue = config.getOrDefault(propName,defaultValue).toString();
            if ( propValue.startsWith( FROM_FILE ) ){
                propValue = propValue.substring( FROM_FILE.length() );
                if ( propValue.startsWith( RELATIVE_PATH ) ){
                    propValue = propValue.substring( RELATIVE_PATH.length() );
                    propValue = ((MapDependencyWorkFlow)owner()).dir() + "/" + propValue ;
                }
                return DynamicExecution.FileOrString.file( propValue);
            }
            return DynamicExecution.FileOrString.string(propValue);
        }

        @Override
        default Predicate<Map<String, Object>> when() {
            DynamicExecution e = DynamicExecution.engine(engine());
            Logger.info("# %s ?[%s]", e.engine(), name());
            final DynamicExecution.FileOrString when = getContent(WHEN,"true");
            return e.predicate(when);
        }

        @Override
        default Function<Map<String, Object>, Object> body() {
            DynamicExecution e = DynamicExecution.engine(engine());
            Logger.info("# %s =[%s]", e.engine(), name());
            final DynamicExecution.FileOrString body = getContent(BODY,"");
            return e.function(body);
        }

        @Override
        default DependencyWorkFlow owner() {
            return (DependencyWorkFlow) config().getOrDefault(OWNER, null);
        }

        @Override
        default Set<String> dependencies() {
            List<String> l = (List)config().getOrDefault(DEPENDS,Collections.emptyList());
            return new HashSet<>(l);
        }
    }

    Map<String,Object> config();

    @Override
    default String name() {
        return (String) config().getOrDefault(NAME, "");
    }

    @Override
    default Map<String,Object> constants() {
        return (Map<String,Object>) config().getOrDefault(CONSTANTS, Collections.emptyMap());
    }

    default String engine() {
        return (String) config().getOrDefault(ENGINE, "");
    }

    default void engine(String engine){
        Map<String,Object> c = config();
        c.put(ENGINE,engine);
        nodes().forEach((key, value) -> {
            MapFNode node = (MapFNode) value;
            node.config().put(ENGINE, engine);
        });
    }

    @Override
    default long timeOut() {
        return ZNumber.integer(config().getOrDefault(TIME_OUT, Long.MAX_VALUE).toString()).longValue();
    }

    /**
     * This is the factory
     * @param nodeConfig configuration map
     * @return an appropriate node type
     */
    static MapFNode createFrom( Map<String, Object> nodeConfig){
        // We can obviously find a better way TODO
        // try for HttpLike
        IONode.HTTPLike httpLike = () -> nodeConfig;
        if ( !httpLike.protocol().isEmpty() ) return httpLike;
        //  try for fork
        ForkNode forkNode = () -> nodeConfig;
        if ( !forkNode.forkConfig().isEmpty() ) return forkNode;
        // try for Transformer
        TransformerNode tNode = () -> nodeConfig;
        if ( !tNode.transformConfig().isEmpty() ) return tNode;
        // try for quantifier
        QuantifierNode qNode = () -> nodeConfig;
        if ( !qNode.options().isEmpty() ) return qNode;
        // if nothing happens
        return () -> nodeConfig;
    }

    @Override
    default Map<String, FNode> nodes() {
        Map nodeData = (Map)config().getOrDefault( NODES, Collections.emptyMap());
        String engineName = engine();

        for ( Object k : nodeData.keySet()){
            Object v = nodeData.get(k);
            if ( v instanceof FNode ) break;
            Map<String,Object> nodeConfig = (Map)v;
            nodeConfig.put(NAME, k);
            nodeConfig.put(OWNER, this);
            nodeConfig.put(ENGINE, engineName);
            // create appropriate node...
            MapFNode mapFNode = createFrom(nodeConfig);
            nodeData.put(k, mapFNode);
        }
        return (Map<String, FNode>)nodeData;
    }

    @Override
    default Map<String, Object> params() {
        return (Map)config().getOrDefault( PARAMS, Collections.emptyMap());
    }

    Manager MANAGER = path -> {
        Object o = null;
        if ( path.endsWith(".yaml")){
            o = ZTypes.yaml(path,true);
        } else if ( path.endsWith(".json")){
            o = ZTypes.json(path,true);
        }
        if ( o instanceof Map ){
            Object finalO = o;
            File f = new File(path);
            final Map m = (Map) finalO;
            m.put( ABS_FILE, f.getAbsolutePath());
            m.put ( LOAD_DIR, f.getAbsoluteFile().getParentFile().getAbsolutePath());
            return (MapDependencyWorkFlow) () -> m;
        }
        return null;
    };
}
