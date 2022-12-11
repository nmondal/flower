package flower.transform;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface Transformation<R> extends Function<Object,R> {

    String identifier();

    Transformation<Object> IDENTITY = new Transformation<>() {
        @Override
        public String identifier() {
            return "IDENTITY";
        }

        @Override
        public Object apply(Object o) {
            return o;
        }
    };

    Transformation<Object> NULL = new Transformation<>() {
        @Override
        public String identifier() {
            return "NULL";
        }

        @Override
        public Object apply(Object o) {
            return null;
        }
    };

    interface MapTransformation extends Transformation<Map<String,?>> {
        default Map<String,Transformation<?>> children(){
            return Collections.emptyMap();
        }

        @Override
        default Map<String, ?> apply(Object o) {
            Map<String,Object> map = new HashMap<>();
            final Map<String,Transformation<?>> children = children();
            for ( String fieldName : children.keySet() ){
                Transformation<?> tr = children.get(fieldName);
                Object value = tr.apply(o);
                map.put(fieldName,value);
            }
            return map;
        }
    }

    interface ListTransformation<T> extends Transformation<List<?>> {

        Predicate<Object> TRUE = (x) -> true;

        default Predicate<Object> when(){
            return TRUE;
        }

        default Transformation<?> child(){
            return IDENTITY;
        }

        default Stream<Object> each(Object o){
            return Stream.empty();
        }

        @Override
        default List<?> apply(Object o) {
            final Transformation<?> ch = child();
            Predicate<Object> when = when();
            return each(o).filter(when).map(ch).collect(Collectors.toList());
        }
    }

    interface GroupTransformation extends Transformation<Map<String,List>>{

        ListTransformation<?> child();
        default String groupKey(Object o){
            return o.toString();
        }

        @Override
        default Map<String, List> apply(Object o) {
            final Map<String,List> group = new HashMap<>();
            final ListTransformation<?> child = child();
            final Transformation<?> childTransform = child.child();
            final Predicate<Object> when = child.when();
            child.each(o).filter(when).forEach( (x) -> {
                final String key = groupKey(x);
                final Object v = childTransform.apply(x);
                if ( !group.containsKey(key) ) {
                    group.put(key, new ArrayList<>());
                }
                group.get(key).add(v);
            });
            return group;
        }
    }
}
