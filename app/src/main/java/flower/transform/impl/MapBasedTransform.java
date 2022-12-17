package flower.transform.impl;

import flower.transform.Transformation;
import flower.workflow.impl.DynamicExecution;
import zoomba.lang.core.interpreter.ZMethodInterceptor;
import zoomba.lang.core.types.ZTypes;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface MapBasedTransform extends Transformation<Object> {

    static <K,V> Map<K,V> lru(int limit){
        return new LinkedHashMap<>(){
            @Override
            protected boolean removeEldestEntry(Map.Entry eldest) {
                return size() > limit;
            }
        };
    }
    TransformationManager MANAGER = new TransformationManager() {
        final int pathLimit = 10;

        transient Map<String,Object> context;

        final Map<String, Map<String,Transformation<?>>> lru = lru(pathLimit);
        @Override
        public Map<String, Transformation<?>> load(String path) {
            if ( lru.containsKey(path) ){
                return lru.get(path);
            }
                Map<String,Object> m;
                if ( path.endsWith(".json") ){
                    m = (Map)ZTypes.json(path,true);
                } else if ( path.endsWith(".yaml")){
                    m = (Map)ZTypes.yaml(path,true);
                } else {
                    return Collections.emptyMap();
                }
                Map<String,Transformation<?>> tm = m.entrySet().stream().collect(Collectors.toMap(
                        Map.Entry::getKey,
                        MapBasedTransform::fromEntry
                ) );
                lru.put(path,tm);
                return tm;

        }

        @Override
        public Transformation<?> transformation(String name) {
            for ( Map<String,Transformation<?>> repo : lru.values() ){
                if ( repo.containsKey(name) ) return repo.get(name);
            }
            return NULL;
        }

        @Override
        public Map<String, Object> context() {
            return context;
        }

        @Override
        public void context(Map<String, Object> ctx) {
            context = ctx;
        }
    };

    String EXPLODE_MAPPER_DIRECTIVE = "*" ;

    String INPUT_OBJECT = "$" ;

    String CONTEXT_OBJECT = "_$" ;

    String ARRAY_DIRECTIVE = "_each" ;

    String KEY_DIRECTIVE = "_key" ;
    String VALUE_DIRECTIVE = "_value" ;
    String PRED_DIRECTIVE = "_when" ;

    String GROUP_DIRECTIVE = "_group" ;

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

    static Map<String,Object> createInput(Object o){
        Map<String,Object> input = new HashMap<>();
        input.put(INPUT_OBJECT,o);
        input.put(CONTEXT_OBJECT, MANAGER.context());
        return input;
    }
    static Function<Object,Object>  func( String s){
        DynamicExecution.FileOrString fs = DynamicExecution.FileOrString.string(s);
        Function<Map<String,Object>, Object> f = DynamicExecution.ZMB.function(fs);
        return o -> {
            Map<String,Object> input = createInput(o);
            return f.apply(input);
        };
    }

    static Predicate<Object>  pred( String s){
        DynamicExecution.FileOrString fs = DynamicExecution.FileOrString.string(s);
        Predicate<Map<String,Object>> f = DynamicExecution.ZMB.predicate(fs);
        return o -> {
            Map<String,Object> input = createInput(o);
            return f.test(input);
        };
    }

    static Stream<Object> stream(Object o, Map<String,Object> map, String id ){
        String directive = map.getOrDefault( ARRAY_DIRECTIVE, "#.").toString().trim();
        ProcessingType pt = ProcessingType.processDirective(directive);
        Object resp = pt.process(o,true);
        if ( resp instanceof Collection<?> ){
            return ((Collection<Object>) resp).stream();
        }
        System.err.printf("Returning Empty Stream : %s %n", id);
        return Stream.empty();
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
        if ( map.containsKey(EXPLODE_MAPPER_DIRECTIVE) ){
            Object val = map.get(EXPLODE_MAPPER_DIRECTIVE);
            String dir = val.toString().trim();
            if ( EXPLODE_MAPPER_DIRECTIVE.equals(dir) ){
                child = IDENTITY;
            } else {
                Map.Entry<String,Object> entry = Map.entry(id + ".sc", val);
                child = fromEntry(entry);
            }
        } else {
            child = mapTransform(id + ".map", map);
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
                return stream(o, map, id);
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

    static DictTransformation dictTransform( String id, Map<String,Object> map){

        final Predicate<Object> when;
        if ( map.containsKey(PRED_DIRECTIVE) ){
            when = pred( map.get(PRED_DIRECTIVE).toString());
        } else {
            when = Transformation.TRUE;
        }
        final Function<Object,Object> key;
        if ( map.containsKey(KEY_DIRECTIVE) ){
            key = func( map.get(KEY_DIRECTIVE).toString());
        } else {
            key = IDENTITY;
        }

        final Function<Object,Object> value;
        if ( map.containsKey(VALUE_DIRECTIVE) ){
            value = func( map.get(VALUE_DIRECTIVE).toString());
        } else {
            value = IDENTITY;
        }

        return new DictTransformation() {
            @Override
            public Predicate<Object> when() {
                return when;
            }

            @Override
            public Function<Object, Object> key() {
                return key;
            }

            @Override
            public Function<Object, Object> value() {
                return value;
            }

            @Override
            public Stream<Object> each(Object o) {
                return stream(o, map, id);
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
            if ( EXPLODE_MAPPER_DIRECTIVE.equals(id) && EXPLODE_MAPPER_DIRECTIVE.equals(((String) obj).trim())) return Transformation.IDENTITY;
            return (MapBasedTransform) () -> entry;
        }
        // now here, must be complex objects...
        if ( obj instanceof Map ){
            Map<String,Object> map = (Map<String,Object>)obj;
            if ( map.containsKey(GROUP_DIRECTIVE) ){
                return groupTransform(id, map);
            }
            if ( map.containsKey(KEY_DIRECTIVE) ){
                return dictTransform(id, map);
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
