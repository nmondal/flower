package flower.workflow;

import zoomba.lang.core.types.ZNumber;
import zoomba.lang.core.types.ZTypes;

import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

public interface Retry {
    /**
     * Can we retry again?
     * This MUST be immutable function
     * @return true if we can , false if we can not
     */
    boolean can();

    /**
     * This is where one must update the state of the Retry
     */
    void numTries(int currentNumberOfFailures);

    /**
     * interval of wait between two successive tries
     * @return interval of time in ms
     */
    long interval();

    /**
     * A retry to ensure no retry
     */
    Retry NOP = new Retry() {
        @Override
        public boolean can() {
            return false;
        }
        @Override
        public void numTries(int currentNumberOfFailures) {
        }
        @Override
        public long interval() {
            return Long.MAX_VALUE;
        }
    } ;

    class MaximumRetryExceededException extends RuntimeException {
        public final List<Throwable> failures;
        public final Retry retry;

        public MaximumRetryExceededException( Retry retry, List<Throwable> causes){
            super();
            failures = Collections.unmodifiableList(causes);
            this.retry = retry;
        }

        @Override
        public String toString() {
            Map<String,Object> map = new HashMap<>();
            map.put("failures", ZTypes.jsonString(failures));
            map.put("retry", retry.toString());
            map.put("numTries", failures.size() + 1);
            return ZTypes.jsonString(map);
        }

        @Override
        public String getMessage() {
            return "Retry exceeded : " +  toString();
        }
    }

    default  <T,R> Function<T,R>  withRetry( Function<T,R> function){
        // optimization trick : if no retry, do not wrap the stuff...
        if ( !can() ) return function;
        // else do wrapping up
        return t -> {
            int numTries = 0;
            // initialize
            List<Throwable> failures = new ArrayList<>();
            numTries(numTries);
            while( can() ){
                try {
                    return function.apply(t);
                }catch (Throwable th){
                    numTries++;
                    numTries(numTries);
                    failures.add(th);
                    try {
                        Thread.sleep(interval());
                    }catch (InterruptedException e){
                        throw new RuntimeException( new TimeoutException("Possible Timeout Not sure..."));
                    }
                }
            }
            throw new MaximumRetryExceededException( this, failures);
        };
    }

    Random random = new SecureRandom();

    class CounterStrategy implements Retry {
        public final int maxRetries;
        public final long interval ;

        public int currentState = -1;

        public CounterStrategy( int maxRetries, long interval ){
            this.maxRetries = maxRetries;
            this.interval = interval;
        }

        @Override
        public boolean can() {
            return currentState <= maxRetries ;
        }

        @Override
        public void numTries(int currentNumberOfFailures) {
            currentState = currentNumberOfFailures;
        }

        @Override
        public long interval() {
            return interval;
        }

        @Override
        public String toString() {
            Map<String,Object> map = new HashMap<>();
            map.put("max", maxRetries);
            map.put("interval", interval);
            map.put("type", getClass().getName());
            return ZTypes.jsonString(map);
        }
    }

    /**
     * A counter based Retry
     * @param maxRetries maximum tries
     * @param interval wait time between the tries
     * @return a Retry algorithm
     */
    static Retry counter( int maxRetries, long interval) {
        return new CounterStrategy(maxRetries,interval);
    }

    /**
     * A Retry that has random interval spacing
     * @param maxRetries maximum tries
     * @param avgInterval avg wait time between the tries
     * @return a Retry algorithm
     */
    static Retry randomize( int maxRetries, long avgInterval) {

        return new CounterStrategy(maxRetries,avgInterval){
            @Override
            public long interval() {
                long half = avgInterval / 2 ;
                return half + random.nextLong( half + avgInterval );
            }
        };
    }


    /**
     * A Retry that has  interval spacing increasing exponentially as time progresses
     * @param maxRetries maximum tries
     * @param startInterval  initial time gap after the first failure
     * @return a Retry algorithm
     */
    static Retry exponentialBackOff( int maxRetries, long startInterval) {

        return new CounterStrategy(maxRetries, startInterval ){
            long curInterval = startInterval;

            @Override
            public void numTries(int currentNumberOfFailures) {
                super.numTries(currentNumberOfFailures);
                curInterval = (long)(startInterval * Math.exp( currentNumberOfFailures - 1));
            }
            @Override
            public long interval() {
                return curInterval;
            }
        };
    }

    static Retry fromConfig(Map<String,Object> config){
        if ( config.isEmpty() ) return NOP;
        final String strategy = config.getOrDefault("strategy", "counter").toString();
        final int maxRetries = ZNumber.integer(config.getOrDefault("max", 0),0).intValue();
        final long interval = ZNumber.integer(config.getOrDefault("interval", Long.MAX_VALUE),0).longValue();
        return switch (strategy.toLowerCase(Locale.ROOT)) {
            case "exp" -> exponentialBackOff(maxRetries, interval);
            case "random" -> randomize(maxRetries, interval);
            case "counter" -> counter(maxRetries,interval);
            default -> { // log it out may be?
                System.err.printf("Could not make a Retry out of : %s --> NOP %n", ZTypes.jsonString(config));
                yield  NOP;
            }
        };
    }
}
