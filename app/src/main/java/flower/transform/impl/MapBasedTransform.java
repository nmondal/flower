package flower.transform.impl;

import flower.transform.Transformation;
import flower.workflow.impl.DynamicExecution;
import zoomba.lang.core.interpreter.ZMethodInterceptor;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface MapBasedTransform extends Transformation<Object> {

    String IDENTITY_DIRECTIVE = "*" ;

    String CONTEXT_OBJECT = "$" ;

    String ARRAY_DIRECTIVE = "_each" ;

    String PRED_DIRECTIVE = "_when" ;

    String GROUP_DIRECTIVE = "_group" ;

    Map.Entry<String,Object> entry();

    @Override
    default String identifier() {
        return entry().getKey();
    }

    static boolean isXPath(String s){
        // TODO bad idea, but works for now
        return s.startsWith("./") || s.startsWith("/");
    }

    static Function<Object,Object>  func( String s){
        DynamicExecution.FileOrString fs = DynamicExecution.FileOrString.string(s);
        Function<Map<String,Object>, Object> f = DynamicExecution.ZMB.function(fs);
        return o -> {
            Map<String,Object> input = new HashMap<>();
            input.put(CONTEXT_OBJECT,o);
            return f.apply(input);
        };
    }

    static Predicate<Object>  pred( String s){
        DynamicExecution.FileOrString fs = DynamicExecution.FileOrString.string(s);
        Predicate<Map<String,Object>> f = DynamicExecution.ZMB.predicate(fs);
        return o -> {
            Map<String,Object> input = new HashMap<>();
            input.put(CONTEXT_OBJECT,o);
            return f.test(input);
        };
    }

    @Override
    default Object apply(Object o) {
        // this is the default, so Object must be treated as expression hence...
        String directive = entry().getValue().toString().trim();
        if ( isXPath(directive) ){
            boolean array = ARRAY_DIRECTIVE.equalsIgnoreCase(identifier());
            return ZMethodInterceptor.Default.jxPath(o, directive, array);
        } else {
            Function<Object,Object> f = func(directive);
            return f.apply(o);
        }
    }

    static MapTransformation mapTransform( String id, Map<String,Object> map){
        return new MapTransformation() {
            final Map<String,Transformation<?>> child = map.entrySet()
                    .stream().filter( (e) -> !e.getKey().startsWith("_")).map(MapBasedTransform::fromEntry)
                    .collect(Collectors.toMap(Transformation::identifier, (t)-> t ));

            @Override
            public Map<String, Transformation<?>> children() {
                return child;
            }

            @Override
            public String identifier() {
                return id;
            }
        };
    }

    static ListTransformation<?> listTransform( String id, Map<String,Object> map){
        final Transformation<?> child ;
        if ( map.containsKey(IDENTITY_DIRECTIVE) ){
            child = IDENTITY;
        } else {
            child =  mapTransform(id + ".map", map);;
        }
        final Predicate<Object> when;
        if ( map.containsKey(PRED_DIRECTIVE) ){
            when = pred( map.get(PRED_DIRECTIVE).toString());
        } else {
            when = ListTransformation.TRUE;
        }

        return new ListTransformation<>() {
            @Override
            public Stream<Object> each(Object o) {
                String directive = map.getOrDefault( ARRAY_DIRECTIVE, "./").toString().trim();
                Collection<Object> col ;
                if ( isXPath(directive) ){
                   col =  (Collection<Object>) ZMethodInterceptor.Default.jxPath(o, directive, true);
                } else {
                    Function<Object,Object> f = func(directive);
                    col = (Collection<Object>) f.apply(o);
                }
                return col.stream();
            }
            @Override
            public Predicate<Object> when() {
               return when;
            }
            @Override
            public Transformation<?> child() {
                return child;
            }
            @Override
            public String identifier() {
                return id;
            }
        };
    }

    static GroupTransformation groupTransform( String id, Map<String,Object> map){

        final Function<Object,Object> gk ;
        String s = map.get(GROUP_DIRECTIVE).toString();
        if ( isXPath(s)){
            gk = (o) -> ZMethodInterceptor.Default.jxPath(o,false);
        } else {
            gk = func(s);
        }
        return new GroupTransformation() {

            @Override
            public String groupKey(Object o) {
                return gk.apply(o).toString();
            }

            @Override
            public ListTransformation<?> child() {
                return listTransform( id + ".list", map);
            }

            @Override
            public String identifier() {
                return id;
            }
        };
    }

    static boolean isPrimitive(Object o){
        return o instanceof String ||
                o instanceof Number ||
                o instanceof  Boolean ;
    }

    static Transformation<?> fromEntry(Map.Entry<String,Object> entry){
        String id = entry.getKey().trim();
        Object obj = entry.getValue();
        if ( isPrimitive(obj) ){
            // this is important...
            if ( IDENTITY_DIRECTIVE.equals(id) && IDENTITY_DIRECTIVE.equals(((String) obj).trim())) return Transformation.IDENTITY;
            return (MapBasedTransform) () -> entry;
        }
        // now here, must be complex objects...
        if ( obj instanceof Map ){
            Map<String,Object> map = (Map<String,Object>)obj;
            if ( map.containsKey(GROUP_DIRECTIVE) ){
                return groupTransform(id, map);
            }
            if ( map.containsKey(ARRAY_DIRECTIVE) ){
                return listTransform(id, map);
            }
            return mapTransform(id, map);
        }
        System.err.printf("No match for type %s, returning null transformer! %n", obj.getClass());
        return NULL;
    }
}
