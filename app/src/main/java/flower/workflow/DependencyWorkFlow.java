package flower.workflow;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
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

        DependencyWorkFlow load(String path);

        default int executorPoolSize(final @Nonnull DependencyWorkFlow flow){ return 10; }

        default void executorPoolSize(int size){}

        default Map<String,Object> run(@Nonnull  DependencyWorkFlow workFlow, final @Nonnull String runNodeName, final @Nonnull Map<String,Object> input){
            final  boolean validateParams = input.keySet().containsAll( workFlow.params().keySet());
            if ( !validateParams ) throw new IllegalArgumentException( "Parameter MisMatch!" );
            final  Map<String,FNode> nodes = workFlow.nodes();
            final FNode endNode = nodes.get(runNodeName);
            if ( endNode == null ) throw new IllegalArgumentException( "Node does not exist!" );
            // TODO generate subgraph moving up from this node
            // just generate the level order traversal
            Set<String> visited = Collections.synchronizedSet( new HashSet<>());
            Map<String,Object> contextMemory = new ConcurrentHashMap<>(input);
            final ExecutorService executorService = Executors.newFixedThreadPool( executorPoolSize(workFlow));
            boolean couldRunOne = true;
            while (couldRunOne){
                couldRunOne = false;
                for ( Map.Entry<String,FNode> entry : nodes.entrySet() ){
                    if ( visited.contains(entry.getKey()) ) continue;
                    FNode curNode = entry.getValue();
                    Set<String> parents = curNode.dependencies();
                    if ( !visited.containsAll(parents) ) continue;
                    try {
                        // this is how things should run
                        executorService.submit(
                                () -> {
                                    Object res = curNode.body().apply( contextMemory);
                                    contextMemory.put( curNode.name(), res);
                                    visited.add( curNode.name());
                                }
                        );
                        couldRunOne = true;
                    }catch (RejectedExecutionException re){ }
                    final boolean exit =  runNodeName.equals(curNode.name()) ;
                    if ( exit ) {
                        executorService.shutdown();
                        return contextMemory;
                    }
                }
            }
            executorService.shutdownNow();
            return Collections.emptyMap();
        }
    }
}

