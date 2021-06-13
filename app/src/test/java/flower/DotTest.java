package flower;

import flower.workflow.DotGraph;
import flower.workflow.impl.MapDependencyWorkFlow;
import org.junit.Assert;
import org.junit.Test;
import zoomba.lang.core.types.ZTypes;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.SecureRandom;
import java.util.*;

public class DotTest {
    public static void buildDot(String workflowFile, String outFile) {
        try {
            DotGraph.dot( MapDependencyWorkFlow.MANAGER.load(workflowFile), outFile);
        }catch ( Exception e){
            Assert.fail(e.getMessage());
        }
    }

    public static String genWorkFlow(int maxLevel, int maxWidth) throws Exception {
        final String flowName = String.valueOf(System.nanoTime()) ;
        List<String> previousLevel = Collections.emptyList();
        Random r = new SecureRandom();
        Map<String, Object> config = new HashMap<>();
        Map<String,Object> nodeMap = new HashMap<>() ;
        config.put( MapDependencyWorkFlow.NODES, nodeMap);
        config.put(MapDependencyWorkFlow.NAME, flowName );
        r.setSeed(System.nanoTime());
        for (int i = 0; i < maxLevel; i++) {
            List<String> curLevel = new ArrayList<>();
            int width = r.nextInt(maxWidth + 1) + 1;
            for (int j = 0; j < width; j++) {
                String nodeName = String.format("L_%d_%d", i, j);
                int previousLevelSize = previousLevel.size();
                int numParents = previousLevelSize > 0 ? r.nextInt( previousLevel.size()) : 0 ;
                Set<String> myParents = new HashSet<>();
                for ( int k = 0; k < numParents; k++ ){
                    int parentInx = r.nextInt( previousLevelSize );
                    String parentName = String.format("L_%d_%d", i-1, parentInx);
                    myParents.add(parentName);
                }
                Map<String,Object> node = new HashMap<>();
                node.put( MapDependencyWorkFlow.MapFNode.BODY, nodeName);
                node.put( MapDependencyWorkFlow.MapFNode.DEPENDS,  new ArrayList<>(myParents) );
                nodeMap.put( nodeName, node);
                curLevel.add( nodeName);
            }
            previousLevel = curLevel;
        }
        final String data = ZTypes.yamlString( config);
        final String path = "samples/gen/" + flowName  + ".yaml";
        Path p =  Path.of(path);
        Files.writeString( p, data, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        return path;
    }

    @Test
    public void dotOnJSONTest() {
        buildDot("samples/1.json", "samples/1.dot");
    }

    @Test
    public void dotOnLargeYamlTest() throws Exception {
        String genFile = genWorkFlow( 4, 10 );
        buildDot(genFile, genFile + ".dot" );
    }
}
