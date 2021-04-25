// ============================================================================
//
// Copyright (C) 2006-2021 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.codegen.flowvariables.runtime;

import java.util.Iterator;
import java.util.LinkedList;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.generic.IndexedRecord;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.common.avro.RootSchemaUtils;

/**
 * Processes data retrieved from wrapped component runtime classes as Root record data.
 * It retrieves Main and Flow variables data from Root record, stores flow variables in Runtime container
 */
public class FlowVariablesProcessor extends DataProcessor {

    /**
     * Job runtime container
     */
    private final RuntimeContainer runtimeContainer;

    /**
     * Flow variables schema (aka out of band schema)
     */
    private Schema flowVariablesSchema;

    /**
     * Constructor sets job {@link RuntimeContainer}, which is used to store flow variables data
     *
     * @param runtimeContainer job {@link RuntimeContainer}
     */
    FlowVariablesProcessor(RuntimeContainer runtimeContainer) {
        this.runtimeContainer = runtimeContainer;
    }

    /**
     * Initializes Flow variables data schema
     *
     * @param rootRecord Root record
     */
    void initSchema(IndexedRecord rootRecord) {
        if (flowVariablesSchema == null) {
            Schema rootSchema = rootRecord.getSchema();
            flowVariablesSchema = RootSchemaUtils.getOutOfBandSchema(rootSchema);
        }
    }

    /**
     * Retrieves Main and Flow variables data from Root record.
     * Stores flow variables in Runtime container
     *
     * @param Root record
     * @return Main data retrieved from root record
     */
    private Object processRootRecord(IndexedRecord rootRecord) {
        Object mainData = rootRecord.get(0);
        IndexedRecord flowVariablesRecord = (IndexedRecord) rootRecord.get(1);

        String componentId = runtimeContainer.getCurrentComponentId();
        for (Field field : flowVariablesSchema.getFields()) {
            String flowVariableKey = field.name();
            int fieldPos = field.pos();
            Object flowVariableValue = flowVariablesRecord.get(fieldPos);
            runtimeContainer.setComponentData(componentId, flowVariableKey, flowVariableValue);
        }

        return mainData;
    }

    /**
     * Processes incoming <code>data</code> as Root record:
     * Retrieves Main and Flow variables data from Root record.
     * Stores flow variables in Runtime container
     * It may throw {@link ClassCastException} in case of component development rules violation.
     * E.g. first data was Root record, but some of subsequent data wasn't
     *
     * @param data data retrieved from wrapped component runtime
     * @return Main data retrieved from root record
     */
    @Override
    Object processData(Object data) {
        IndexedRecord rootRecord = (IndexedRecord) data;
        return processRootRecord(rootRecord);
    }

    /**
     * Processes incoming <code>dataIterable</code> as {@link Iterable} with root records:
     * Retrieves and stores Flow variables data only from first element. In most cases {@link Iterable} contains
     * only one element.
     * Retrieves main data from each root record and copies it to another {@link Iterable},
     * which will be returned
     *
     * @param dataIterable data {@link Iterable} retrieved from component runtime class
     * @return new {@link Iterable} with only main data
     */
    @Override
    Iterable<Object> processDataIterable(Iterable<Object> dataIterable) {
        LinkedList<Object> mainDataList = new LinkedList<>();
        Iterator<Object> dataIterator = dataIterable.iterator();

        // process first data
        if (dataIterator.hasNext()) {
            IndexedRecord rootRecord = (IndexedRecord) dataIterator.next();
            Object mainData = processRootRecord(rootRecord);
            mainDataList.add(mainData);
        }

        // process rest data
        while (dataIterator.hasNext()) {
            IndexedRecord rootRecord = (IndexedRecord) dataIterator.next();
            Object mainData = rootRecord.get(0);
            mainDataList.add(mainData);
        }

        return mainDataList;
    }

}
