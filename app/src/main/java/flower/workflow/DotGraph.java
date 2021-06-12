package flower.workflow;

import java.nio.file.*;
import java.util.*;

public final class DotGraph {

    private static void build(StringBuilder sb, Map<String, DependencyWorkFlow.FNode> map){
        Set<String> topLevel = new HashSet<>();
        Set<String> tmpLeaf = new HashSet<>(map.keySet());
        for ( DependencyWorkFlow.FNode node : map.values()){
            if ( node.dependencies().isEmpty() ){
                topLevel.add( node.name());
            }
            for ( String name : node.dependencies() ){
                String edge = String.format( "%s -> %s;%n", name, node.name());
                sb.append( edge );
                tmpLeaf.add(name);
            }
        }
        tmpLeaf.removeAll(topLevel);
        // now we have leaf nodes
        for ( String name : topLevel ){
            String edge = String.format( "start -> %s;%n", name);
            sb.append( edge );
        }
        for ( String name : tmpLeaf ){
            String edge = String.format( "%s -> end;%n", name);
            sb.append( edge );
        }
    }
    public static String dot( DependencyWorkFlow workFlow){
       StringBuilder sb = new StringBuilder();
       sb.append( "digraph G {").append("\n");
       sb.append( String.format("label = \"%s\";%n", workFlow.name()));
       sb.append("node [style=filled, shape=rectangle]; ").append("\n");
       build(sb, workFlow.nodes());
       sb.append("start [shape=circle];").append("\n");
       sb.append("end [shape=circle];").append("\n");
       sb.append( "}").append("\n");
       return sb.toString();
   }

    public static void dot(DependencyWorkFlow workFlow, String file) throws Exception{
       Path p =  Path.of(file);
       Files.writeString( p, dot(workFlow), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }
}
