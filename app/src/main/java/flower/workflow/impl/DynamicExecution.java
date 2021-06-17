package flower.workflow.impl;

import static zoomba.lang.core.interpreter.ZContext.*;
import static zoomba.lang.core.operations.Function.MonadicContainer;

import zoomba.lang.core.interpreter.ZScript;
import zoomba.lang.core.types.ZTypes;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

public interface DynamicExecution {

    class FileOrString{
        private final File f;
        private final String s;
        private FileOrString(String s){
            f = null;
            this.s = s;
        }
        private FileOrString(File f){
            this.f = f;
            s = null;
        }
        public static FileOrString file(@Nonnull String f){
            return new FileOrString( new File(f));
        }
        public static FileOrString string(@Nonnull String s){
            return new FileOrString(s);
        }
    }

    Function<Map<String,Object>,Object> function(FileOrString fs);

    Predicate<Map<String,Object>> predicate(FileOrString fs);

    String engine();

    DynamicExecution DUMMY = new DynamicExecution() {
        //TODO this is for dry running and logging workflow
        @Override
        public Function<Map<String, Object>, Object> function(FileOrString fs) {
            return stringObjectMap -> fs.s ;
        }

        @Override
        public Predicate<Map<String, Object>> predicate(FileOrString fs) {
            return stringObjectMap -> true ;
        }

        @Override
        public String engine() {
            return "dummy";
        }
    };

    DynamicExecution ZMB = new DynamicExecution() {

        private Object zmb( ZScript script, Map<String, Object> input){
            FunctionContext ctx =
                    new FunctionContext(EMPTY_CONTEXT, ArgContext.EMPTY_ARGS_CONTEXT);
            input.forEach(ctx::set);
            script.runContext(ctx);
            MonadicContainer mc = script.execute();
            if ( mc.value() instanceof RuntimeException ) throw (RuntimeException)mc.value();
            return mc.value();
        }

        @Override
        public Function<Map<String, Object>, Object> function(FileOrString fs) {
            return new Function<Map<String, Object>, Object>() {
                final ZScript script = fs.s != null ? new ZScript( fs.s ) : new ZScript( fs.f.getAbsolutePath(), null, "" ) ;
                @Override
                public Object apply(Map<String, Object> input) {
                    return zmb(script,input);
                }
            };
        }

        @Override
        public Predicate<Map<String, Object>> predicate(FileOrString fs) {
            return new Predicate<Map<String, Object>>() {
                final ZScript script = fs.s != null ? new ZScript( fs.s ) : new ZScript( fs.f.getAbsolutePath(), null, "" ) ;

                @Override
                public boolean test(Map<String, Object> input) {
                    Object o = zmb(script, input);
                    return ZTypes.bool(o,false);
                }
            };
        }

        @Override
        public String engine() {
            return "zmb";
        }
    };
    
    
    static DynamicExecution engine(String name){
        switch ( name ){
            case "zmb" : return ZMB;
            default:
                return DUMMY;
        }
    }
}
