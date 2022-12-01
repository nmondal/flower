package flower.workflow.impl;

import zoomba.lang.core.types.ZTypes;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface ForkNode extends MapDependencyWorkFlow.MapFNode {

    String FORK = "fork" ;

    String RUN_NODE = "node" ;

    String RUN_VAR = "var" ;

    String COLLECT_UNIQUE = "unique" ;

    default Map<String,Object> forkConfig() {
        return (Map)config().getOrDefault(FORK, Collections.emptyMap());
    }

    default String runNodeName(){
        return forkConfig().getOrDefault(RUN_NODE, "").toString();
    }

    default String runVariableName(){
        return forkConfig().getOrDefault(RUN_VAR, "").toString();
    }

    default boolean unique(){
        String value = forkConfig().getOrDefault( COLLECT_UNIQUE , "false").toString().toLowerCase(Locale.ROOT);
        return ZTypes.bool(value, false);
    }

    default String dataSource(){
        // this is the only data source for a fork-node
        return dependencies().iterator().next();
    }

    @Override
    default Function<Map<String, Object>, Object> body() {
        return memoryMap -> {
            Collection<?> col = Collections.emptyList();
            // if the dependent node did not produce.. any collection, return empty collection
            Object resp = memoryMap.getOrDefault( dataSource(), Collections.emptyList() );
            if ( !(resp instanceof Collection) ){
                return col;
            }
            Collection<?> inputCollection = (Collection<?>)resp;
            // here, means we can process further
            Stream<?> forkStream = inputCollection.parallelStream().map( item -> {
                Map<String,Object> cowMem = new HashMap<>(memoryMap);
                cowMem.put(runVariableName(), item);
                // recursion is divine
                Map<String,Object> resultMem = MapDependencyWorkFlow.MANAGER.run( owner(), runNodeName(), cowMem);
                return resultMem.get( runNodeName());
            });
            if ( unique() ){
                col = forkStream.collect(Collectors.toSet());
            } else {
                col = forkStream.collect(Collectors.toList());
            }
            return col;
        };
    }
}
