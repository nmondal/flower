package flower.workflow.impl;


import flower.workflow.DependencyWorkFlow;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface QuantifierNode extends MapDependencyWorkFlow.MapFNode {

    String ANY = "any" ;
    String ALL = "all" ;

    default boolean isAny(){
        return config().containsKey(ANY);
    }

    default boolean isAll(){
        return config().containsKey(ALL);
    }

    default List<String> options() {
        if ( isAny() ){
            return (List)config().get(ANY);
        }
        if ( isAll() ){
            return (List)config().get(ALL);
        }
        return Collections.emptyList();
    }

    @Override
    default Function<Map<String, Object>, Object> body() {

        Map<String, DependencyWorkFlow.FNode> nodes = owner().nodes();
        List<DependencyWorkFlow.FNode> options = options().stream().map(nodes::get).toList();

        return memoryMap -> {
           Stream<DependencyWorkFlow.FNode> stream = options.stream().filter(node-> node.when().test( memoryMap) );
           if ( isAny() ){
               Optional<DependencyWorkFlow.FNode> on = stream.findFirst();
               if ( on.isEmpty() ){
                   // we should crash out...
                   throw new UnsupportedOperationException( String.format("No Condition matched on node '%s'!", name()));
               }
               return on.get().body().apply(memoryMap);
           }
           // now this is all
           Map<String,Object> res = stream.parallel().map( node -> Map.entry(node.name() , node.body().apply(memoryMap)))
                   .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
           return res;
        };
    }
}
