package flower.workflow;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public final class DotGraph {

    private static void build(StringBuilder sb, Map<String, DependencyWorkFlow.FNode> map) {
        Set<String> topLevel = new HashSet<>();
        Set<String> tmpLeaf = new HashSet<>(map.keySet());
        for (DependencyWorkFlow.FNode node : map.values()) {
            // for well formed graphs, nodes must be defined
            String nodeName = String.format("%s;%n", node.name());
            sb.append(nodeName);

            if (node.dependencies().isEmpty()) {
                topLevel.add(node.name());
            }
            for (String name : node.dependencies()) {
                String edge = String.format("%s -> %s;%n", name, node.name());
                sb.append(edge);
                tmpLeaf.add(name);
            }
        }
        tmpLeaf.removeAll(topLevel);
        // now we have leaf nodes
        for (String name : topLevel) {
            String edge = String.format("start -> %s;%n", name);
            sb.append(edge);
        }
        for (String name : tmpLeaf) {
            String edge = String.format("%s -> end;%n", name);
            sb.append(edge);
        }
    }

    public static String dot(DependencyWorkFlow workFlow) {
        StringBuilder sb = new StringBuilder();
        sb.append("digraph G {").append("\n");
        sb.append(String.format("label = \"%s\";%n", workFlow.name()));
        sb.append("node [style=filled, shape=rectangle]; ").append("\n");
        build(sb, workFlow.nodes());
        sb.append("start [shape=circle];").append("\n");
        sb.append("end [shape=circle];").append("\n");
        sb.append("}").append("\n");
        return sb.toString();
    }

    public static Path writeFile(String file, String data) {
        Path p = FileSystems.getDefault().getPath(file);
        byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
        try {
            return Files.write(p, dataBytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            return null;
        }
    }

    public static void dot(DependencyWorkFlow workFlow, String file) {
        String data = dot(workFlow);
        writeFile(file,data);
    }
}
