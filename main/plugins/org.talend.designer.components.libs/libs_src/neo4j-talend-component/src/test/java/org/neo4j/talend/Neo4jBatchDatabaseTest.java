package org.neo4j.talend;

import org.junit.Assert;
import org.junit.Test;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import java.io.IOException;

public class Neo4jBatchDatabaseTest extends Neo4jBatchUnitTest {

    @Test
    public void index_create_populate_flush_find_should_succeed() throws IOException {
        // create an index
        batchDb.createBatchIndex(INDEX_NAME, 0);

        // populate the index
        for (int i = 0; i < 100; i++) {
            batchDb.indexNodeInBatchIndex(INDEX_NAME, Long.valueOf(i), Long.valueOf(1000 + i));
        }

        // flush it
        batchDb.flushBatchIndex(INDEX_NAME);

        // find node into index & make assert
        for (int i = 0; i < 100; i++) {
            // Make the assert
            Assert.assertEquals(Long.valueOf(i), batchDb.findNodeInBatchIndex(INDEX_NAME, 1000 + i));
        }

    }

    @Test
    public void search_a_none_existing_value_into_index_should_succeed_and_return_null() {
        // create an index
        batchDb.createBatchIndex(INDEX_NAME, 0);

        // populate the index
        for (int i = 0; i < 100; i++) {
            batchDb.indexNodeInBatchIndex(INDEX_NAME, i, 1000 + i);
        }
        // flush it
        batchDb.flushBatchIndex(INDEX_NAME);

        // Make the assert
        Assert.assertNull(batchDb.findNodeInBatchIndex(INDEX_NAME, 666));
    }

    @Test
    public void shutdown_after_a_shutdown_should_not_complained() {
        batchDb.shutdown();
        batchDb.shutdown();
    }

    @Test
    public void create_node_uniqueness_constraint_should_work() {
        // Test const.
        String label = "Test";
        String property = "myProp";

        // crete the schema
        batchDb.createSchema("NODE_PROPERTY_UNIQUE", label, property);
        batchDb.shutdown();

        // Testing it with real graphdb
        GraphDatabaseService graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(dbPath);
        try (Transaction tx = graphDb.beginTx()) {
            Assert.assertTrue(graphDb.schema().getConstraints(DynamicLabel.label(label)).iterator().hasNext());
        }
        graphDb.shutdown();
    }

    @Test
    public void create_node_index_should_work() {
        // Test const.
        String label = "Test";
        String property = "myProp";

        // crete the schema
        batchDb.createSchema("NODE_PROPERTY_INDEX", label, property);
        batchDb.shutdown();

        // Testing it with real graphdb
        GraphDatabaseService graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(dbPath);
        try (Transaction tx = graphDb.beginTx()) {
            Assert.assertTrue(graphDb.schema().getIndexes(DynamicLabel.label(label)).iterator().hasNext());
        }
        graphDb.shutdown();

    }

}
