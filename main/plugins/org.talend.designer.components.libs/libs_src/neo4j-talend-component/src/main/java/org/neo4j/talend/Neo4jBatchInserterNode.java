package org.neo4j.talend;

import org.apache.commons.lang3.StringUtils;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.Label;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Neo4jBatchInserterNode extends Neo4jBatchInserterAbstract {

    /**
     * Field name where to find the list of label
     */
    private String labelsField;

    /**
     * Batch index name
     */
    private String batchIndexName;

    /**
     * Name of the field on wich we create the batch index
     */
    private String batchIndexFieldName;

    /**
     * If true, when we create a node we also save the field with its value.
     * It is usefull to put it at false if this field is just a technical one for the import.
     */
    private Boolean insertIndexFieldName;


    /**
     * Constructor
     *
     * @param batchDb              Batch inserter database
     * @param labelsField          Field name where to find labels
     * @param batchIndexName       Batch index name
     * @param batchIndexFieldName  Batch index field name
     * @param insertIndexFieldName If we have to save the index field
     * @param indexCacheSize       Size of the cache index
     * @throws IOException
     */
    public Neo4jBatchInserterNode(Neo4jBatchDatabase batchDb, String labelsField, String batchIndexName, String batchIndexFieldName, Boolean insertIndexFieldName, Integer indexCacheSize) throws IOException {
        super(batchDb);

        // Check required component properties
        if (StringUtils.isEmpty(batchIndexName)) {
            throw new RuntimeException("batchIndexName must be defined !!!");
        }
        if (StringUtils.isEmpty(batchIndexFieldName)) {
            throw new RuntimeException("batchIndexFieldName field must be defined !!!");
        }
        if (indexCacheSize == null) {
            throw new RuntimeException("Index cache size must be defined !!!");
        }

        // Setting class value
        this.batchIndexName = batchIndexName;
        this.batchIndexFieldName = batchIndexFieldName;
        this.labelsField = labelsField;
        this.insertIndexFieldName = insertIndexFieldName;

        // Create the batch index
        this.batchDb.createBatchIndex(batchIndexName, indexCacheSize);
    }

    /**
     * Insert a node.
     *
     * @param incoming   Talend object
     * @param columnList Attributes list of the Talend object
     */
    public void create(Object incoming, List<String> columnList) {
        try {
            // Construct the property map
            Map<String, Object> properties = constructMapFromObject(incoming, columnList);

            // Import ID
            Object importId = this.getObjectProperty(incoming, this.batchIndexFieldName);

            // Label list
            Label[] labels = this.computeLabels(incoming);

            // Remove none necessary fields (importId, & label)
            if (StringUtils.isNotEmpty(this.labelsField)) {
                properties.remove(this.labelsField);
            }
            if (!this.insertIndexFieldName) {
                properties.remove(this.batchIndexFieldName);
            }

            // If the node exist, we update it, otherwise we create it
            Long id = this.batchDb.findNodeInBatchIndex(batchIndexName, importId);
            if (id != null) {

                // update properties
                Map<String, Object> props = this.batchDb.getInserter().getNodeProperties(id);
                properties.putAll(props);
                this.batchDb.getInserter().setNodeProperties(id, properties);

                // update labels
                List<Label> labelList = new ArrayList<>(Arrays.asList(labels));
                for (Label lab : this.batchDb.getInserter().getNodeLabels(id)) {
                    labelList.add(lab);
                }
                Label[] mergeLabels = labelList.toArray(new Label[labelList.size()]);
                this.batchDb.getInserter().setNodeLabels(id, mergeLabels);

            } else {
                // Create the node
                long nodeId = this.batchDb.getInserter().createNode(properties, labels);

                // Save the node into the index
                this.batchDb.indexNodeInBatchIndex(batchIndexName, nodeId, importId);
            }

        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Finish the process.
     */
    public void finish() {
        this.batchDb.flushBatchIndex(batchIndexName);
    }

    protected Label[] computeLabels(Object incoming) throws IllegalAccessException {
        Label[] labels = new Label[0];

        if (StringUtils.isNotEmpty(this.labelsField)) {

            String labelList = (String) this.getObjectProperty(incoming, this.labelsField);

            if (StringUtils.isNotEmpty(labelList)) {
                String[] labelTab = labelList.split(";");
                labels = new Label[labelTab.length];
                for (int i = 0; i < labelTab.length; i++) {
                    labels[i] = DynamicLabel.label(labelTab[i]);
                }
            }
        }

        return labels;
    }

}
