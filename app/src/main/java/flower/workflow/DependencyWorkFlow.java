package flower.workflow;

import flower.Logger;
import zoomba.lang.core.types.ZTypes;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.function.Predicate;

public interface DependencyWorkFlow {

    Predicate<Map<String,Object>> TRUE = stringObjectMap -> true;

    Predicate<Map<String,Object>> FALSE = stringObjectMap -> false;

    Function<Map<String,Object>,Object> IDENTITY = stringObjectMap -> stringObjectMap;

    interface FNode{

        String name();

        Map<String, Object> params();

        default Predicate<Map<String,Object>> when(){ return TRUE; }

        default Function<Map<String,Object>,Object> body() { return IDENTITY ; }

        DependencyWorkFlow owner();

        Set<String> dependencies();
    }

    String name();

    Map<String,FNode> nodes();

    Map<String,Object> params();

    interface Manager {

        String STATUS = "success" ;

        DependencyWorkFlow load(String path);

        default int executorPoolSize(final @Nonnull DependencyWorkFlow flow){ return 10; }

        default void executorPoolSize(int size){}

        default Map<String,FNode> subGraph( @Nonnull  DependencyWorkFlow workFlow, final @Nonnull String runNodeName){
            return workFlow.nodes();
        }

        default Map<String,Object> run(@Nonnull  DependencyWorkFlow workFlow, final @Nonnull String runNodeName, final @Nonnull Map<String,Object> input){
            final  boolean validateParams = input.keySet().containsAll( workFlow.params().keySet());
            if ( !validateParams ) throw new IllegalArgumentException( "Parameter MisMatch!" );
            final  Map<String,FNode> nodes = subGraph( workFlow, runNodeName );
            final FNode endNode = nodes.get(runNodeName);
            if ( endNode == null ) throw new IllegalArgumentException( "Node does not exist!" );
            // just generate the level order traversal
            Set<String> submitted =  new HashSet<>();

            Set<String> visited = Collections.synchronizedSet( new HashSet<>());
            Map<String,Object> contextMemory = new ConcurrentHashMap<>(input);
            final ExecutorService executorService = Executors.newFixedThreadPool( executorPoolSize(workFlow));
            boolean runNodeNotSubmitted = true;
            Logger.info("^ < %s > -> [%s]", workFlow.name(), runNodeName );
            while (runNodeNotSubmitted){
                for ( Map.Entry<String,FNode> entry : nodes.entrySet() ){
                    if ( visited.contains(entry.getKey()) ) continue;
                    if ( submitted.contains(entry.getKey()) ) continue;
                    // now we process further
                    FNode curNode = entry.getValue();
                    final String nodeName = curNode.name();
                    Set<String> parents = curNode.dependencies();
                    if ( !visited.containsAll(parents) ) continue;
                    Logger.info("> [%s]", nodeName);

                    try {
                        // this is how things should run
                        executorService.submit(
                                () -> {
                                    Logger.info("+ [%s]" , nodeName );
                                    try {
                                        Object res = curNode.body().apply( contextMemory);
                                        contextMemory.put( nodeName, res);
                                        Logger.info("- [%s]", nodeName);
                                    }catch ( Throwable t){
                                       Logger.error(t, "! [%s]", nodeName );
                                    }
                                    visited.add( nodeName );
                                }
                        );
                        submitted.add(entry.getKey());
                    }catch (RejectedExecutionException re){

                    }
                }
                runNodeNotSubmitted = !submitted.contains(runNodeName);
            }
            executorService.shutdown();
            try {
                final boolean t = executorService.awaitTermination( 1000L, TimeUnit.MILLISECONDS);
                contextMemory.put(STATUS, t);
            }catch ( InterruptedException ie){
                Logger.warn("! < %s > ", workFlow.name() );
            }
            Logger.info("= %s", ZTypes.jsonString(contextMemory));
            Logger.info("$ < %s >", workFlow.name() );
            return contextMemory;
        }
    }
}

