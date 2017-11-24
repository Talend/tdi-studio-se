package org.neo4j.talend;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import org.neo4j.graphdb.DynamicRelationshipType;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class Neo4jBatchInserterRelationship extends Neo4jBatchInserterAbstract {

    private static Logger log = Logger.getLogger(Neo4jBatchInserterRelationship.class);

    private String relationshipTypeField;
    private String direction;
    private String startIndexName;
    private String startIndexField;
    private String endIndexName;
    private String endIndexField;

    private Boolean skipOnError;

    /**
     * Constructor.
     */
    public Neo4jBatchInserterRelationship(Neo4jBatchDatabase graphDb, String relationshipTypeField, String direction,
                                          String startIndexName, String startIndexField, String endIndexName, String endIndexField,
                                          Boolean skipOnError) throws IOException {
        super(graphDb);

        if (StringUtils.isEmpty(relationshipTypeField)) {
            throw new RuntimeException("relationshipTypeField must be defined !!!");
        }
        this.relationshipTypeField = relationshipTypeField;

        if (StringUtils.isEmpty(direction)) {
            throw new RuntimeException("direction field must be defined !!!");
        }
        this.direction = direction;

        if (StringUtils.isEmpty(startIndexName)) {
            throw new RuntimeException("startIndexName field must be defined !!!");
        }
        this.startIndexName = startIndexName;

        if (StringUtils.isEmpty(startIndexField)) {
            throw new RuntimeException("startIndexField field must be defined !!!");
        }
        this.startIndexField = startIndexField;

        if (StringUtils.isEmpty(endIndexName)) {
            throw new RuntimeException("endIndexKey field must be defined !!!");
        }
        this.endIndexName = endIndexName;

        if (StringUtils.isEmpty(endIndexField)) {
            throw new RuntimeException("endIndexField field must be defined !!!");
        }
        this.endIndexField = endIndexField;

        this.skipOnError = skipOnError;

        this.batchDb = graphDb;
    }

    /**
     * Create the relationship.
     *
     * @param incoming   Talend incoming object
     * @param columnList Attribute list of Talend object
     */
    public void create(Object incoming, List<String> columnList) {
        try {
            Long startNode = this.batchDb.findNodeInBatchIndex(startIndexName, this.getObjectProperty(incoming, startIndexField));
            Long endNode = this.batchDb.findNodeInBatchIndex(endIndexName, this.getObjectProperty(incoming, endIndexField));
            Map<String, Object> properties = constructMapFromObject(incoming, columnList);

            if (startNode != null && endNode != null) {

                String type = (String) this.getObjectProperty(incoming, relationshipTypeField);
                if (StringUtils.isNotEmpty(type)) {

                    properties.remove(relationshipTypeField);
                    properties.remove(startIndexField);
                    properties.remove(endIndexField);

                    if (direction.endsWith("OUTGOING")) {
                        this.batchDb.getInserter()
                                .createRelationship(startNode, endNode, DynamicRelationshipType.withName(type),
                                        properties);
                    } else {
                        this.batchDb.getInserter()
                                .createRelationship(endNode, startNode, DynamicRelationshipType.withName(type),
                                        properties);
                    }
                } else {
                    log.trace("Ignoring incoming object {" + properties.toString() + "} due to none relation type");
                }
            } else {
                String msg = "Can't find start node " + startNode + " or end node " + endNode;
                if (skipOnError) {
                    log.trace(msg);
                } else {
                    throw new RuntimeException(msg);
                }
            }

        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Finish the process.
     */
    public void finish() {
    }

}
