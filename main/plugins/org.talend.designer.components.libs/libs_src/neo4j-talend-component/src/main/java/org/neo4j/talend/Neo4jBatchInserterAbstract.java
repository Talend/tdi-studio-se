package org.neo4j.talend;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Abstract class for batch inserter classes.
 * It has some usefull helper.
 */
abstract class Neo4jBatchInserterAbstract {

    /**
     * Neo4jBatchDatabase.
     */
    protected Neo4jBatchDatabase batchDb;

    /**
     * Default construtor.
     *
     * @param batchDb Batch inserter database
     */
    public Neo4jBatchInserterAbstract(Neo4jBatchDatabase batchDb) {
        this.batchDb = batchDb;
    }

    /**
     * Create an object (ie a node or a relationship) into the batch database.
     *
     * @param incoming   Talend incoming object
     * @param columnList Attribute list of Talend object
     */
    public abstract void create(Object incoming, List<String> columnList) throws IllegalAccessException;

    /**
     * What we do when the job is finished ?
     */
    public abstract void finish();

    /**
     * Retrieve the value of a field on an object
     *
     * @param obj      the object
     * @param property the property to retrieve
     * @return The property value, or null if not found
     */
    protected Object getObjectProperty(Object obj, String property) throws IllegalAccessException {
        Object value = null;

        Class<?> cls = obj.getClass();
        Field[] fields = cls.getFields();

        for (Field field : fields) {
            if (field.getName().equals(property)) {
                value = field.get(obj);
            }
        }

        return value;
    }

    /**
     * Convert a Talend object to a map of attributes.
     *
     * @param obj        The talend object
     * @param columnList List of object properties
     * @return Map of attributes
     * @throws IllegalAccessException
     */
    protected Map<String, Object> constructMapFromObject(Object obj, List<String> columnList) throws IllegalAccessException {
        // Construct the property map
        Map<String, Object> properties = new HashMap<>();
        for (String column : columnList) {
            Object value = this.getObjectProperty(obj, column);

            // Type conversion for Neo4j
            Object transValue = null;
            if (value != null) {

                if (value instanceof java.sql.Date) {
                    transValue = ((java.sql.Date) value).getTime();
                }

                if (value instanceof java.sql.Timestamp) {
                    transValue = ((java.sql.Timestamp) value).getTime();
                }

                if (value instanceof java.util.Date) {
                    transValue = ((java.util.Date) value).getTime();
                }

                if (transValue == null) {
                    transValue = value;
                }

                properties.put(column, transValue);
            }
        }
        return properties;
    }

}
