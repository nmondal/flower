package flower.workflow.impl;

import zoomba.lang.core.types.ZTypes;

import javax.script.*;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

public class JSR223 implements DynamicExecution{

    private static final Map<String,DynamicExecution> cache = new HashMap<>();

    public static synchronized DynamicExecution using(String engineName){
        if ( cache.containsKey( engineName) ) return cache.get(engineName);
        DynamicExecution de = new JSR223(engineName);
        cache.put(engineName,de);
        return de;
    }

    private String engineName = "nashorn" ;

    private ScriptEngine engine;

    private JSR223( String name) {
        ScriptEngineManager manager = new ScriptEngineManager();
        try {
            engine = manager.getEngineByName(name);
            engineName = name;
        }catch (Throwable t){
            engine = manager.getEngineByName(engineName);
        }
    }

    private CompiledScript compiledScript( FileOrString fs){
        try {
            final String script = fs.content();
            return  ((Compilable) engine).compile(script);
        }catch ( Exception e){
            throw new RuntimeException(e);
        }
    }

    private Object runScript(CompiledScript cs, Map<String, Object> input){
        Bindings bindings = new SimpleBindings(input);
        try {
            return cs.eval(bindings);
        }catch ( Exception e){
          throw new RuntimeException(e);
        }
    }

    @Override
    public Function<Map<String, Object>, Object> function(FileOrString fs) {

        return new Function<Map<String, Object>, Object>() {
            final CompiledScript script = compiledScript(fs);
            @Override
            public Object apply(Map<String, Object> input) {
                return runScript(script,input);
            }
        };
    }

    @Override
    public Predicate<Map<String, Object>> predicate(FileOrString fs) {
        return new Predicate<Map<String, Object>>() {
            final CompiledScript script = compiledScript(fs);
            @Override
            public boolean test(Map<String, Object> input) {
                Object o = runScript(script,input);
                return ZTypes.bool(o,false);
            }
        };
    }

    @Override
    public String engine() {
        return engineName;
    }
}
