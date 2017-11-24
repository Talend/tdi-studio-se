package org.neo4j.talend;

import org.junit.After;
import org.junit.Before;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class Neo4jBatchUnitTest {

    public final static String LABEL_FIELD = "label";
    public final static String LABEL_NAME = "MyLabel";
    public final static String IMPORT_ID = "id";
    public final static String REL_TYPE = "MY_REL_TYPE";
    public final static String REL_TYPE_FIELD = "relType";
    public final static String INDEX_NAME = "myIndex";

    protected Neo4jBatchDatabase batchDb;
    protected File dbPath;

    @Before
    public void before() throws IOException {
        // create the database
        Path path = Files.createTempDirectory("neo4j_");
        dbPath = path.toFile();

        Map<String, String> conf = new HashMap<>();
        batchDb = new Neo4jBatchDatabase(path.toAbsolutePath().toString(), conf);
    }

    @After
    public void after() {
        batchDb.shutdown();
    }

    protected Neo4jBatchInserterNode getNeo4jBatchInserterNode(Boolean insertIndexFieldName) throws IOException {
        return new Neo4jBatchInserterNode(batchDb, LABEL_FIELD, INDEX_NAME, IMPORT_ID, insertIndexFieldName, 100);
    }

    protected Neo4jBatchInserterRelationship getNeo4jBatchInserterRelationship(Boolean skipOnError) throws IOException {
        return new Neo4jBatchInserterRelationship(batchDb, REL_TYPE_FIELD, "OUTGOING", INDEX_NAME, IMPORT_ID, INDEX_NAME, IMPORT_ID, skipOnError);
    }
}
