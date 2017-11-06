package org.neo4j.talend;

import org.junit.Assert;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import java.util.List;
import java.util.Map;

public class Neo4jBatchInserterNodeTest extends Neo4jBatchUnitTest {

    @Test
    public void compute_label_from_incoming_object_should_succeed() throws Exception {
        Neo4jBatchInserterNode nodeInserter = getNeo4jBatchInserterNode(false);
        DummyTalendPojo pojo = DummyTalendPojo.getDummyTalendPojo();

        Label[] labels = nodeInserter.computeLabels(pojo);

        Assert.assertEquals(LABEL_NAME, labels[0].name());
    }

    @Test
    public void insert_node_should_succeed_with_populated_index() throws Exception {
        Neo4jBatchInserterNode nodeInserter = getNeo4jBatchInserterNode(false);

        // populate the db
        List<String> columns = DummyTalendPojo.getColumnList();
        for (int i = 0; i < 100; i++) {
            DummyTalendPojo pojo = DummyTalendPojo.getDummyTalendPojo();
            nodeInserter.create(pojo, columns);
        }
        nodeInserter.finish();

        // check if index size
        Assert.assertEquals(100, batchDb.batchInserterIndexes.get(INDEX_NAME).query("*:*").size());

        // check the database data
        batchDb.shutdown();
        // Testing it with real graphdb
        GraphDatabaseService graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(dbPath);
        try (Transaction tx = graphDb.beginTx()) {
            Long result = (Long) graphDb.execute("MATCH (n:" + LABEL_NAME + ") RETURN count(n) AS count").next().get("count");
            Assert.assertEquals(Long.valueOf(100), result);
        }
        graphDb.shutdown();
    }

    @Test
    public void update_node_should_succeed() throws Exception {
        // populate the db
        Neo4jBatchInserterNode nodeInserter = getNeo4jBatchInserterNode(false);
        List<String> columns = DummyTalendPojo.getColumnList();
        DummyTalendPojo pojo = DummyTalendPojo.getDummyTalendPojo();
        nodeInserter.create(pojo, columns);
        nodeInserter.finish();

        // By indexing the import id, I update the last node
        nodeInserter = getNeo4jBatchInserterNode(true);
        nodeInserter.create(pojo, columns);
        nodeInserter.finish();

        // check the result into the database
        batchDb.shutdown();
        GraphDatabaseService graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(dbPath);
        try (Transaction tx = graphDb.beginTx()) {
            Long result = (Long) graphDb.execute("MATCH (n:" + LABEL_NAME + ") WHERE exists(n.id) RETURN count(*) AS count").next().get("count");
            Assert.assertEquals(Long.valueOf(1), result);
        }
        graphDb.shutdown();
    }

    @Test
    public void type_convertion_for_neo4j_should_work() throws Exception {
        Neo4jBatchInserterNode nodeInserter = getNeo4jBatchInserterNode(false);
        DummyTalendPojo pojo = DummyTalendPojo.getDummyTalendPojo();

        Map<String, Object> attrs = nodeInserter.constructMapFromObject(pojo, DummyTalendPojo.getColumnList());

        Assert.assertEquals(pojo.id, attrs.get("id"));
        Assert.assertEquals(pojo.propInteger, attrs.get("propInteger"));
        Assert.assertEquals(pojo.propFloat, attrs.get("propFloat"));
        Assert.assertEquals(pojo.propBoolean, attrs.get("propBoolean"));
        Assert.assertEquals(pojo.propLong, attrs.get("propLong"));
        Assert.assertEquals(pojo.propString, attrs.get("propString"));
        Assert.assertEquals(pojo.propDate.getTime(), attrs.get("propDate"));
    }

}
