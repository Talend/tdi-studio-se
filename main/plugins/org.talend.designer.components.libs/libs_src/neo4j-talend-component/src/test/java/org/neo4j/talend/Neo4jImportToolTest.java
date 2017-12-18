package org.neo4j.talend;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Neo4jImportToolTest  {

    private Neo4jImportTool importTool;
    private File dbPath;

    @Before
    public void before() throws IOException {
        this.dbPath = Files.createTempDirectory("neo4j_").toFile();

        // Neo4j configuration
        Map<String, String> configNeo = new HashMap<>();
        configNeo.put("dbms.pagecache.memory", "2G");

        // Import configuration
        Map<String, String> configImport = new HashMap<>();
        configImport.put("stacktrace", "");

        // Nodes file
        final File actors = new File(ClassLoader.getSystemResource("importTool/actors.csv").getFile());
        List<Map<String, String>> nodes = new ArrayList<>();
        nodes.add(new HashMap<String, String>() {{
            put(Neo4jImportTool.FILE_KEY,actors.getAbsolutePath());
            put(Neo4jImportTool.HEADERS_KEY, "personId:ID,name,:LABEL");
        }});
        final File movies = new File(ClassLoader.getSystemResource("importTool/movies.csv").getFile());
        nodes.add(new HashMap<String, String>() {{
            put(Neo4jImportTool.FILE_KEY,movies.getAbsolutePath());
            put(Neo4jImportTool.HEADERS_KEY, "movieId:ID,title,year:int,:LABEL");
        }});

        // Relationship files
        final File roles = new File(ClassLoader.getSystemResource("importTool/roles.csv").getFile());
        List<Map<String, String>> relationships = new ArrayList<>();
        relationships.add(new HashMap<String, String>() {{
            put(Neo4jImportTool.FILE_KEY, roles.getAbsolutePath());
            put(Neo4jImportTool.HEADERS_KEY, ":START_ID,role,:END_ID,:TYPE");
        }});

        importTool = new Neo4jImportTool(dbPath.getPath(), configNeo, configImport, nodes, relationships);
    }

    @Test
    public void create_neo4j_config_file_should_succeed() throws Exception {
        String path = this.importTool.createNeo4jConfigFile();

        File file = new File(path);
        List<String> lines =  Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);

        Assert.assertTrue(file.exists());
        Assert.assertEquals("dbms.pagecache.memory=2G",lines.get(0));
    }

    @Test
    public void create_headers_file_should_succeed() throws IOException {
        String path = this.importTool.createHeaderFile(":START_ID,role,:END_ID,:TYPE");

        File file = new File(path);
        List<String> lines =  Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);

        Assert.assertTrue(file.exists());
        Assert.assertEquals(":START_ID,role,:END_ID,:TYPE",lines.get(0));
    }

    @Test
    public void execute_import_should_succeed() throws IOException {
        this.importTool.execute();

        // Testing it with real graphdb
        GraphDatabaseService graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(dbPath);
        try (Transaction tx = graphDb.beginTx()) {
            Long result = (Long) graphDb.execute("MATCH ()-[r]->() RETURN count(r) AS count").next().get("count");
            Assert.assertEquals(Long.valueOf(9), result);
        }
        graphDb.shutdown();

    }

}
