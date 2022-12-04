package flower.workflow;

import flower.Logger;
import zoomba.lang.core.types.ZTypes;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.function.Predicate;

public interface DependencyWorkFlow {

    Predicate<Map<String, Object>> TRUE = stringObjectMap -> true;

    Predicate<Map<String, Object>> FALSE = stringObjectMap -> false;

    Function<Map<String, Object>, Object> IDENTITY = stringObjectMap -> stringObjectMap;

    interface FNode {

        String name();

        Map<String, Object> params();

        default Predicate<Map<String, Object>> when() {
            return TRUE;
        }

        default Function<Map<String, Object>, Object> body() {
            return IDENTITY;
        }

        default long timeOut() {
            return Long.MAX_VALUE;
        }

        DependencyWorkFlow owner();

        Set<String> dependencies();
    }

    String name();

    Map<String,Object> constants();

    default long timeOut() {
        return Long.MAX_VALUE;
    }

    Map<String, FNode> nodes();

    Map<String, Object> params();

    interface Manager {

        String STATUS = "success";

        DependencyWorkFlow load(String path);

        default int executorPoolSize(final @Nonnull DependencyWorkFlow flow) {
            return 10;
        }

        default void executorPoolSize(int size) {
        }

        default Map<String, FNode> subGraph(@Nonnull DependencyWorkFlow workFlow, final @Nonnull String runNodeName) {
            final Map<String, FNode> nodes = workFlow.nodes();
            Stack<String> stack = new Stack<>();
            Map<String, FNode> result = new HashMap<>();
            stack.push(runNodeName);
            while (!stack.isEmpty()) {
                final String name = stack.pop();
                FNode me = nodes.get(name);
                result.put(name, me);
                if (me == null) throw new IllegalArgumentException("Node does not exist!");
                me.dependencies().forEach(stack::push);
            }
            return result;
        }

        default boolean validateParams(@Nonnull DependencyWorkFlow workFlow,
                                       final @Nonnull Map<String, Object> input) {
            //TODO verify types
            boolean allExists = input.keySet().containsAll(workFlow.params().keySet());
            return allExists;
        }

        default Map<String, Object> run(@Nonnull DependencyWorkFlow workFlow,
                                        final @Nonnull String runNodeName,
                                        final @Nonnull Map<String, Object> input) {
            final boolean validateParams = validateParams(workFlow, input);
            if (!validateParams) throw new IllegalArgumentException("Parameter MisMatch!");
            final Map<String, FNode> nodes = subGraph(workFlow, runNodeName);
            final FNode endNode = nodes.get(runNodeName);
            if (endNode == null) throw new IllegalArgumentException("Node does not exist!");
            // just generate the level order traversal
            Set<String> submitted = new HashSet<>();

            Set<String> visited = Collections.synchronizedSet(new HashSet<>());
            // create from constants
            Map<String, Object> contextMemory = new ConcurrentHashMap<>(workFlow.constants());
            contextMemory.put(STATUS, true);
            // constants may be overwritten by input
            contextMemory.putAll(input);
            final ExecutorService executorService = Executors.newFixedThreadPool(executorPoolSize(workFlow));
            boolean runNodeNotSubmitted = true;
            Logger.info("^ < %s > -> [%s]", workFlow.name(), runNodeName);
            final long startTime = System.currentTimeMillis();
            while (runNodeNotSubmitted) {
                for (Map.Entry<String, FNode> entry : nodes.entrySet()) {
                    if (visited.contains(entry.getKey())) continue;
                    if (submitted.contains(entry.getKey())) continue;
                    // now we process further
                    FNode curNode = entry.getValue();
                    final String nodeName = curNode.name();
                    Set<String> parents = curNode.dependencies();
                    if (!visited.containsAll(parents)) continue;
                    Logger.info("> [%s]", nodeName);

                    try {
                        // this is how things should run
                        executorService.submit(new FCallable<>(
                                curNode.timeOut(),
                                (me) -> {
                                    Logger.info("+ [%s]", nodeName);
                                    try {
                                        // run the when condition...
                                        boolean shouldRun = curNode.when().test(contextMemory);
                                        // TODO do we need better?
                                        if (!shouldRun) throw new RuntimeException(":when: returned false");
                                        Object res = curNode.body().apply(contextMemory);
                                        contextMemory.put(nodeName, res);
                                        Logger.info("- [%s]", nodeName);
                                    } catch (Throwable t) {
                                        t = me.wasTimeOut() ? new TimeoutException() : t;
                                        contextMemory.put(nodeName, t);
                                        contextMemory.put(STATUS, false);
                                        Logger.error(t, "! [%s]", nodeName);
                                    }
                                    visited.add(nodeName);
                                    return nodeName;
                                } // end of function
                        ));
                        submitted.add(entry.getKey());
                    } catch (RejectedExecutionException re) {
                        Logger.error(re, "* [%s]", nodeName);
                    }
                    if ( contextMemory.get(STATUS) == Boolean.FALSE ){ break; }
                }
                runNodeNotSubmitted = !submitted.contains(runNodeName) && contextMemory.get(STATUS) == Boolean.TRUE ;
            }
            executorService.shutdown();
            final long timeLeftToWait = workFlow.timeOut() - System.currentTimeMillis() + startTime;
            try {
                final boolean t = executorService.awaitTermination(Math.max(timeLeftToWait, 1L), TimeUnit.MILLISECONDS);
                if (!t) {
                    executorService.shutdownNow();
                    contextMemory.put(runNodeName, new TimeoutException());
                    contextMemory.put(STATUS, false);
                }

            } catch (InterruptedException ie) {
                Logger.warn("! < %s > ", workFlow.name());
            }
            Logger.info("= %s", ZTypes.jsonString(contextMemory));
            Logger.info("$ < %s >", workFlow.name());
            return contextMemory;
        }
    }
}

