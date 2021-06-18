package flower.workflow.impl;

import flower.Logger;
import flower.workflow.DependencyWorkFlow;
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
        default Map<String, Object> params() {
            return (Map) config().getOrDefault(PARAMS, Collections.emptyMap());
        }

        default DynamicExecution.FileOrString getContent(final String propName, String defaultValue){
            String propValue = config().getOrDefault(propName,defaultValue).toString();
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

            MapFNode mapFNode = () -> nodeConfig;
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
