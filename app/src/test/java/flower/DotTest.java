package flower;

import flower.workflow.DotGraph;
import flower.workflow.impl.MapDependencyWorkFlow;
import org.junit.Assert;
import org.junit.Test;
import zoomba.lang.core.types.ZTypes;

import java.io.File;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.*;

import static flower.workflow.DotGraph.writeFile;

public class DotTest {
    public static void buildDot(String workflowFile, String outFile) {
        try {
            DotGraph.dot( MapDependencyWorkFlow.MANAGER.load(workflowFile), outFile);
        }catch ( Exception e){
            Assert.fail(e.getMessage());
        }
    }

    public static String genWorkFlow(int maxLevel, int maxWidth) {
        final String flowName = String.valueOf(System.nanoTime()) ;
        List<String> previousLevel = Collections.emptyList();
        List<String> allAboveLevel = new ArrayList<>();
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
                // now, in here, if previous level is empty, no need to add parents...
                int previousLevelSize = previousLevel.size();
                Set<String> myParents = new HashSet<>();
                if ( previousLevelSize != 0  ){
                    //get at least 1 parent from previous level
                    int parentInx = r.nextInt( previousLevelSize );
                    String parentName = previousLevel.get(parentInx);
                    myParents.add(parentName);
                    // now, get some random parents from all above levels
                    int numParents = r.nextInt( maxWidth );
                    for ( int k = 0; k < numParents; k++ ){
                        parentInx = r.nextInt( allAboveLevel.size() );
                        parentName = allAboveLevel.get( parentInx) ;
                        myParents.add(parentName);
                    }
                }

                Map<String,Object> node = new HashMap<>();
                node.put( MapDependencyWorkFlow.MapFNode.BODY, nodeName);
                node.put( MapDependencyWorkFlow.MapFNode.DEPENDS,  new ArrayList<>(myParents) );
                nodeMap.put( nodeName, node);
                curLevel.add( nodeName);
            }
            previousLevel = curLevel;
            allAboveLevel.addAll(curLevel);
        }
        final String data = ZTypes.yamlString( config);
        final String path = "samples/gen/" + flowName  + ".yaml";
        Path p =  writeFile(path, data);
        Assert.assertNotNull(p);
        return p.toString();
    }

    @Test
    public void dotOnJSONTest() {
        buildDot("samples/1.json", "samples/gen/1.dot");
    }

    @Test
    public void tmpDotTest() {
        File genDir = new File( "samples/gen");
        for ( File f : genDir.listFiles() ){
            if ( f.getName().endsWith(".yaml")){
                 String fileName = f.getPath();
                buildDot(fileName,  fileName + ".dot");
            }
        }
    }

    @Test
    public void dotOnLargeYamlTest() throws Exception {
        String genFile = genWorkFlow( 4, 4 );
        buildDot(genFile, genFile + ".dot" );
    }
}
