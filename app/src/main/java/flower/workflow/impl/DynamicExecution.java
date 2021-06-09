package flower.workflow.impl;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

public interface DynamicExecution {

    Function<Map<String,Object>,Object> function(String s);

    Predicate<Map<String,Object>> predicate(String s);

    String engine();

    DynamicExecution DEFAULT = new DynamicExecution() {
        //TODO this is for dry running and logging workflow
        @Override
        public Function<Map<String, Object>, Object> function(String s) {
            return stringObjectMap -> s ;
        }

        @Override
        public Predicate<Map<String, Object>> predicate(String s) {
            return stringObjectMap -> true ;
        }

        @Override
        public String engine() {
            return "dummy";
        }
    };

    static DynamicExecution engine(String name){
        //TODO Change later
        return DEFAULT;
    }
}
