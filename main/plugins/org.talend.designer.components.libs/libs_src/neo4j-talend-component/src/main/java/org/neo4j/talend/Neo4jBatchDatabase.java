package org.neo4j.talend;

import org.apache.log4j.Logger;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.helpers.collection.MapUtil;
import org.neo4j.index.lucene.unsafe.batchinsert.LuceneBatchInserterIndexProvider;
import org.neo4j.unsafe.batchinsert.BatchInserter;
import org.neo4j.unsafe.batchinsert.BatchInserterIndex;
import org.neo4j.unsafe.batchinsert.BatchInserterIndexProvider;
import org.neo4j.unsafe.batchinsert.BatchInserters;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Wrapper on a Neo4j batch inserter 'database'.
 */
public class Neo4jBatchDatabase {

    /**
     * The logger
     */
    private static Logger log = Logger.getLogger(Neo4jBatchDatabase.class);

    /**
     * Name of the field index for the importId
     */
    private final static String BACTH_INDEX_ID_NAME = "importId";

    /**
     * Batch inserter
     */
    private BatchInserter inserter;

    /**
     * Batch inserter index provider
     */
    private BatchInserterIndexProvider indexProvider;

    /**
     * Store the list of index
     */
    protected Map<String, BatchInserterIndex> batchInserterIndexes;


    /**
     * Constructor that create an embedded database.
     *
     * @param path          Path of the graph database folder
     * @param configuration Configuration map of database
     */
    public Neo4jBatchDatabase(String path, Map<String, String> configuration) {
        try {
            // Initialize batch inserter
            this.inserter = BatchInserters.inserter(new File(path), configuration);

            // Init
            this.indexProvider = new LuceneBatchInserterIndexProvider(inserter);
            this.batchInserterIndexes = new HashMap<>();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Getter for inserter
     *
     * @return The batch inserter
     */
    public BatchInserter getInserter() {
        return this.inserter;
    }
    
    

    public Map<String, BatchInserterIndex> getBatchInserterIndexes() {
		return batchInserterIndexes;
	}


	public void setBatchInserterIndexes(Object batchInserterIndexes) {
		if(batchInserterIndexes!=null && batchInserterIndexes instanceof Map) {
			this.batchInserterIndexes.putAll((Map<String, BatchInserterIndex>)batchInserterIndexes);
		}
	}


	/**
     * Create and store the index, if it doesn't exist.
     *
     * @param indexName      Name of the index
     * @param indexCacheSize Number of element into the cache size for the index
     */
    public void createBatchIndex(String indexName, Integer indexCacheSize) {
        if (!batchInserterIndexes.containsKey(indexName)) {
            BatchInserterIndex index = indexProvider.nodeIndex(indexName, MapUtil.stringMap("type", "exact"));
            if (indexCacheSize > 0) {
                index.setCacheCapacity(BACTH_INDEX_ID_NAME, indexCacheSize);
            }
            batchInserterIndexes.put(indexName, index);
        } else {
            log.trace("Index [" + indexName + "] already exist");
        }
    }

    /**
     * Flush the specified index on the disk, so it will be available after for search.
     *
     * @param indexName Name of the index to flush
     */
    public void flushBatchIndex(String indexName) {
        if (batchInserterIndexes.containsKey(indexName)) {
            BatchInserterIndex index = batchInserterIndexes.get(indexName);
            index.flush();
        } else {
            log.trace("Flushing a non exist index ... [" + indexName + "]");
        }
    }

    /**
     * Push a node importId into the batch index.
     *
     * @param indexName     The batch index name
     * @param node          Neo4j identifier
     * @param importIdValue Import identifier
     */
    public void indexNodeInBatchIndex(String indexName, long node, Object importIdValue) {
        if (batchInserterIndexes.containsKey(indexName)) {
            BatchInserterIndex index = batchInserterIndexes.get(indexName);

            Map<String, Object> props = new HashMap<>();
            props.put(BACTH_INDEX_ID_NAME, importIdValue);
            index.add(node, props);
        } else {
            log.trace("Can't index node " + node + "/" + importIdValue + " into a none existant index [" + indexName + "]");
        }
    }

    /**
     * Find a node in the index.
     *
     * @param indexName Name of the batch index
     * @param value     Value to search in index
     * @return Neo4j node identifier
     */
    public Long findNodeInBatchIndex(String indexName, Object value) {
        Long nodeId = null;
        if (this.batchInserterIndexes.containsKey(indexName)) {
            IndexHits<Long> result = this.batchInserterIndexes.get(indexName).get(BACTH_INDEX_ID_NAME, value);
            if (result.size() > 0) {
                nodeId = result.getSingle();
            }
        } else {
            log.trace("Can't find object [" + value + "] into index " + indexName);
        }
        return nodeId;
    }

    /**
     * Create schema.
     *
     * @param type     Type of schema to create (ie. constraint or index on node property)
     * @param label    Node label
     * @param property Node property
     */
    public void createSchema(String type, String label, String property) {
        switch (type) {
            case "NODE_PROPERTY_UNIQUE":
                this.inserter.createDeferredConstraint(DynamicLabel.label(label)).assertPropertyIsUnique(property).create();
                break;
            case "NODE_PROPERTY_INDEX":
                this.inserter.createDeferredSchemaIndex(DynamicLabel.label(label)).on(property).create();
                break;
            default:
                // default is unique constraint
                this.inserter.createDeferredConstraint(DynamicLabel.label(label)).assertPropertyIsUnique(property).create();
                break;
        }
    }

    /**
     * Shutdown the graph database
     */
    public void shutdown() {
        if (this.inserter != null) {
            this.inserter.shutdown();
        }
        this.inserter = null;

        if (this.indexProvider != null) {
            this.indexProvider.shutdown();
        }
        this.indexProvider = null;
        this.batchInserterIndexes = null;
    }

}
