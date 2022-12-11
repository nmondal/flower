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

    String SCALAR_DIRECTIVE = "_" ;

    Map.Entry<String,Object> entry();

    @Override
    default String identifier() {
        return entry().getKey();
    }

    interface ProcessingType{

        String XPATH_ONLY = "#" ;

        String XPATH_ELEM = "@" ;

        boolean isXPath();

        boolean isXElem();

        default boolean isOther(){
            return !isXPath() && !isXElem();
        }

        String pathString();

        static ProcessingType processDirective(String s){

            final boolean isXPath = s.startsWith(XPATH_ONLY);
            final boolean isXElem = s.startsWith(XPATH_ELEM);

            final String path;
            if ( isXPath || isXElem ){
                path = s.substring(1);
            } else {
                path = s;
            }
            return new ProcessingType() {
                @Override
                public boolean isXPath() {
                    return isXPath;
                }

                @Override
                public boolean isXElem() {
                    return isXElem;
                }

                @Override
                public String pathString() {
                    return path;
                }
            };
        }

        default Object process(Object o, boolean multi){
            if ( isXPath() ){
                return ZMethodInterceptor.Default.jxPath(o, pathString(), multi);
            } else if ( isXElem() ){
                return ZMethodInterceptor.Default.jxElement(o, pathString(), multi );
            }
            else {
                Function<Object,Object> f = func(pathString());
                return f.apply(o);
            }
        }
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
        final String directive = entry().getValue().toString().trim();
        ProcessingType pt = ProcessingType.processDirective(directive);
        return pt.process(o,false);
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
            if ( map.containsKey(SCALAR_DIRECTIVE ) ){
                Map.Entry<String,Object> entry = Map.entry(id + ".sc", map.get(SCALAR_DIRECTIVE));
                child = fromEntry(entry);
            } else {
                child = mapTransform(id + ".map", map);
            }
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
                String directive = map.getOrDefault( ARRAY_DIRECTIVE, "#.").toString().trim();
                ProcessingType pt = ProcessingType.processDirective(directive);
                Object resp = pt.process(o,true);
                if ( resp instanceof Collection<?> ){
                    return ((Collection<Object>) resp).stream();
                }
                System.err.printf("Returning Empty Stream : %s %n", identifier());
                return Stream.empty();
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

        String s = map.get(GROUP_DIRECTIVE).toString();
        final ProcessingType pt = ProcessingType.processDirective(s);
        return new GroupTransformation() {

            @Override
            public String groupKey(Object o) {
                return pt.process(o,false).toString();
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
