package org.neo4j.talend;

import org.junit.Assert;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import java.io.IOException;
import java.util.List;

public class Neo4jBatchInserterRelationshipTest extends Neo4jBatchUnitTest {

    @Test
    public void insert_edge_should_succeed() throws Exception {
        Neo4jBatchInserterNode nodeInserter = getNeo4jBatchInserterNode(false);

        // populate the db with nodes
        List<String> columns = DummyTalendPojo.getColumnList();
        for (int i = 0; i < 100; i++) {
            DummyTalendPojo pojo = DummyTalendPojo.getDummyTalendPojo(i);
            nodeInserter.create(pojo, columns);
        }
        nodeInserter.finish();

        // create relationship
        Neo4jBatchInserterRelationship batchInserterRelationship = getNeo4jBatchInserterRelationship(true);
        for (int i = 0; i < 100; i++) {
            batchInserterRelationship.create(DummyTalendPojo.getDummyTalendPojo(i), DummyTalendPojo.getColumnList());
        }
        batchInserterRelationship.finish();

        // check the database data
        batchDb.shutdown();

        // Testing it with real graphdb
        GraphDatabaseService graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(dbPath);
        try (Transaction tx = graphDb.beginTx()) {
            Long result = (Long) graphDb.execute("MATCH ()-[r:" + REL_TYPE + "]->() RETURN count(r) AS count").next().get("count");
            Assert.assertEquals(Long.valueOf(100), result);
        }
        graphDb.shutdown();
    }

    @Test
    public void insert_edge_on_node_not_found_should_succeed() throws IOException {
        DummyTalendPojo pojo = DummyTalendPojo.getDummyTalendPojo(2);
        Neo4jBatchInserterRelationship batchInserterRelationship = getNeo4jBatchInserterRelationship(true);
        batchInserterRelationship.create(pojo, DummyTalendPojo.getColumnList());
        batchInserterRelationship.finish();
    }

    @Test(expected = RuntimeException.class)
    public void insert_edge_on_node_not_found_should_fail() throws IOException {
        DummyTalendPojo pojo = DummyTalendPojo.getDummyTalendPojo(2);
        Neo4jBatchInserterRelationship batchInserterRelationship = getNeo4jBatchInserterRelationship(false);
        batchInserterRelationship.create(pojo, DummyTalendPojo.getColumnList());
        batchInserterRelationship.finish();

        Assert.fail();
    }
}
