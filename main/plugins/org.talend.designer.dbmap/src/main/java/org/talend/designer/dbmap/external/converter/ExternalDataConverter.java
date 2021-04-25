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
package org.talend.designer.dbmap.external.converter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.process.EConnectionType;
import org.talend.designer.abstractmap.model.table.IDataMapTable;
import org.talend.designer.abstractmap.model.tableentry.ITableEntry;
import org.talend.designer.dbmap.external.connection.IOConnection;
import org.talend.designer.dbmap.external.data.ExternalDbMapData;
import org.talend.designer.dbmap.external.data.ExternalDbMapEntry;
import org.talend.designer.dbmap.external.data.ExternalDbMapTable;
import org.talend.designer.dbmap.external.data.ExternalDbMapUiProperties;
import org.talend.designer.dbmap.managers.MapperManager;
import org.talend.designer.dbmap.model.MapperModel;
import org.talend.designer.dbmap.model.table.InputTable;
import org.talend.designer.dbmap.model.table.OutputTable;
import org.talend.designer.dbmap.model.table.VarsTable;
import org.talend.designer.dbmap.model.tableentry.FilterTableEntry;
import org.talend.designer.dbmap.model.tableentry.InputColumnTableEntry;

/**
 * Convert external data to internal data and conversely.
 *
 * $Id: ExternalDataConverter.java 1744 2007-01-31 13:05:30Z amaumont $
 *
 */
public class ExternalDataConverter {

    private ArrayList<ExternalDbMapTable> inputTables;

    private ArrayList<ExternalDbMapTable> outputTables;

    private ArrayList<ExternalDbMapTable> varsTables;

    private MapperManager mapperManager;

    /**
     * DOC amaumont ExternalDataConverter constructor comment.
     *
     * @param main
     */
    public ExternalDataConverter(MapperManager mapperManager) {
        super();
        this.mapperManager = mapperManager;
    }

    /**
     * Prepare internal data from connections and external data.
     *
     * @param inputConnections
     * @param outputConnections
     * @param outputMetadataTables
     * @param externalData
     * @param checkProblems
     */
    public MapperModel prepareModel(List<IOConnection> inputs, List<IOConnection> outputs,
            List<IMetadataTable> outputMetadataTables, ExternalDbMapData externalData, boolean checkProblems) {

        if (checkProblems) {
            mapperManager.getProblemsManager().checkProblems();
        }

        ArrayList<InputTable> inputDataMapTables = prepareInputTables(inputs, externalData);

        ArrayList<OutputTable> outputDataMapTables = prepareOutputTables(outputs, outputMetadataTables, externalData);

        return new MapperModel(inputDataMapTables, outputDataMapTables);

    }

    public ArrayList<OutputTable> prepareOutputTables(List<IOConnection> outputConnections,
            List<IMetadataTable> outputMetadataTables, ExternalDbMapData externalData) {
        Map<String, ExternalDbMapTable> nameToOutpuPersistentTable = new HashMap<String, ExternalDbMapTable>();
        if (externalData != null) {
            for (ExternalDbMapTable persistentTable : externalData.getOutputTables()) {
                nameToOutpuPersistentTable.put(persistentTable.getName(), persistentTable);
            }
        }
        Map<String, IOConnection> nameToOutputConn = new HashMap<String, IOConnection>();

        if (outputConnections != null) {
            for (IOConnection connection : outputConnections) {
                if (connection.getConnectionType().equals(EConnectionType.FLOW_MAIN)
                        || connection.getConnectionType().equals(EConnectionType.FLOW_REF)
                        || connection.getConnectionType().equals(EConnectionType.TABLE)
                        || connection.getConnectionType().equals(EConnectionType.TABLE_REF)) {
                    nameToOutputConn.put(connection.getUniqueName(), connection);
                }
            }
        }
        ArrayList<OutputTable> outputDataMapTables = new ArrayList<OutputTable>();
        for (IMetadataTable table : outputMetadataTables) {
            IOConnection connection = nameToOutputConn.get(table.getTableName());
            OutputTable outputTable = null;
            if (connection != null) {
                ExternalDbMapTable persistentTable = nameToOutpuPersistentTable.get(connection.getUniqueName());
                outputTable = new OutputTable(this.mapperManager, table.clone(), connection.getUniqueName(), connection.getName());
                outputTable.initFromExternalData(persistentTable);
            } else {
                ExternalDbMapTable persistentTable = nameToOutpuPersistentTable.get(table.getTableName());
                if (persistentTable != null) {
                    outputTable = new OutputTable(this.mapperManager, table, persistentTable.getName(),
                            persistentTable.getTableName());
                    outputTable.initFromExternalData(persistentTable);
                } else {
                    outputTable = new OutputTable(this.mapperManager, table, table.getTableName(), table.getLabel());
                }
            }
            if (outputTable != null) {
                outputDataMapTables.add(outputTable);
            }
        }

        return outputDataMapTables;
    }

    public ArrayList<InputTable> prepareInputTables(List<IOConnection> inputConnections, ExternalDbMapData externalData) {
        // Map<String, ExternalMapperTable> nameToInputPersistentTable = new HashMap<String, ExternalMapperTable>();
        // if (externalData != null) {
        // for (ExternalMapperTable persistentTable : externalData.getInputTables()) {
        // nameToInputPersistentTable.put(persistentTable.getName(), persistentTable);
        // }
        // }

        Map<String, IOConnection> nameToConnection = new HashMap<String, IOConnection>();
        for (IOConnection connection : inputConnections) {
            nameToConnection.put(connection.getName(), connection);
        }

        ArrayList<InputTable> inputDataMapTables = new ArrayList<InputTable>();
        if (externalData == null) {
            for (IOConnection connection : inputConnections) {
                InputTable inputTable = new InputTable(this.mapperManager, connection, connection.getName());
                inputTable.initFromExternalData(null);
                inputDataMapTables.add(inputTable);
            }

        } else {
            ArrayList<IOConnection> remainingConnections = new ArrayList<IOConnection>(inputConnections);
            for (ExternalDbMapTable persistentTable : externalData.getInputTables()) {
                String name = persistentTable.getTableName() != null ? persistentTable.getTableName() : persistentTable.getName();
                IOConnection connection = nameToConnection.get(name);
                if (connection != null) {
                    remainingConnections.remove(connection);
                    InputTable inputTable = new InputTable(this.mapperManager, connection, connection.getName());
                    inputTable.initFromExternalData(persistentTable);
                    inputDataMapTables.add(inputTable);
                }
            }
            // if(externalData.getInputTables().size() > 0) {
            // for (IOConnection connection : remainingConnections) {
            // InputTable inputTable = new InputTable(this.mapperManager, connection, connection.getName());
            // inputTable.initFromExternalData(null);
            // inputDataMapTables.add(inputTable);
            // }
            // }

        }

        // sort for put table with main connection at top position of the list
        Collections.sort(inputDataMapTables, new Comparator<InputTable>() {

            @Override
            public int compare(InputTable o1, InputTable o2) {
                if (o1.isMainConnection()) {
                    return -1;
                } else if (o2.isMainConnection()) {
                    return 1;
                }
                return 0;
            }
        });
        return inputDataMapTables;
    }

    /**
     *
     * Prepare ExternalMapperData object from internal data.
     *
     * @param mapperModel
     * @return
     */
    public ExternalDbMapData prepareExternalData(MapperModel mapperModel, ExternalDbMapUiProperties uiProperties) {
        ExternalDbMapData externalData = new ExternalDbMapData();
        inputTables = new ArrayList<ExternalDbMapTable>();
        externalData.setInputTables(inputTables);
        outputTables = new ArrayList<ExternalDbMapTable>();
        externalData.setOutputTables(outputTables);
        varsTables = new ArrayList<ExternalDbMapTable>();
        externalData.setVarsTables(varsTables);
        loadInExternalData(mapperModel.getInputDataMapTables());
        loadInExternalData(mapperModel.getOutputDataMapTables());
        externalData.setUiProperties(uiProperties);
        return externalData;
    }

    private void loadInExternalData(Collection<? extends IDataMapTable> tables) {
        for (IDataMapTable table : tables) {
            ExternalDbMapTable externalMapperTable = new ExternalDbMapTable();
            fillExternalTable(table, externalMapperTable);
            ArrayList<ExternalDbMapEntry> perTableEntries = new ArrayList<ExternalDbMapEntry>();
            boolean isInputTable = table instanceof InputTable;
            for (ITableEntry dataMapTableEntry : table.getColumnEntries()) {
                ExternalDbMapEntry externalMapperTableEntry = new ExternalDbMapEntry();
                externalMapperTableEntry.setExpression(dataMapTableEntry.getExpression());
                externalMapperTableEntry.setName(dataMapTableEntry.getName());
                if (isInputTable) {
                    externalMapperTableEntry.setOperator(((InputColumnTableEntry) dataMapTableEntry).getOperator());
                    externalMapperTableEntry.setJoin(((InputColumnTableEntry) dataMapTableEntry).isJoin());
                }
                perTableEntries.add(externalMapperTableEntry);
            }
            externalMapperTable.setMetadataTableEntries(perTableEntries);

        }
    }

    /**
     * DOC amaumont Comment method "processToExternal".
     *
     * @param table
     * @param externalMapperTable
     */
    private void fillExternalTable(IDataMapTable table, ExternalDbMapTable externalMapperTable) {
        if (table instanceof InputTable) {
            fillExternalTable((InputTable) table, externalMapperTable);
        } else if (table instanceof VarsTable) {
            fillExternalTable((VarsTable) table, externalMapperTable);
        } else if (table instanceof OutputTable) {
            fillExternalTable((OutputTable) table, externalMapperTable);
        }
    }

    /**
     *
     * DOC amaumont Comment method "fillExternalTable".
     *
     * @param table
     * @param externalMapperTable
     */
    private void fillExternalTable(InputTable table, ExternalDbMapTable externalMapperTable) {
        externalMapperTable.setJoinType(table.getJoinType().toString());
        externalMapperTable.setAlias(table.getAlias());
        externalMapperTable.setTableName(table.getTableName());
        fillExternalTableWithCommonsData(table, externalMapperTable);
        inputTables.add(externalMapperTable);
    }

    /**
     * DOC amaumont Comment method "fillCommonsDataForExternalTable".
     *
     * @param table
     * @param externalMapperTable
     */
    private void fillExternalTableWithCommonsData(IDataMapTable table, ExternalDbMapTable externalMapperTable) {
        externalMapperTable.setName(table.getName());
        externalMapperTable.setMinimized(table.isMinimized());
    }

    /**
     * DOC amaumont Comment method "process".
     *
     * @param externalMapperTable
     * @param table
     */
    private void fillExternalTable(OutputTable table, ExternalDbMapTable externalMapperTable) {
        externalMapperTable.setMinimized(table.isMinimized());
        externalMapperTable.setName(table.getUniqueName());
        externalMapperTable.setTableName(table.getTableName());

        /**
         * set custom where conditions entries
         */
        ArrayList<ExternalDbMapEntry> constraintTableEntries = new ArrayList<ExternalDbMapEntry>();
        for (FilterTableEntry constraintWhereTableEntry : table.getWhereFilterEntries()) {
            ExternalDbMapEntry externalMapperTableEntry = new ExternalDbMapEntry();
            externalMapperTableEntry.setExpression(constraintWhereTableEntry.getExpression());
            externalMapperTableEntry.setName(constraintWhereTableEntry.getName());
            constraintTableEntries.add(externalMapperTableEntry);
        }
        externalMapperTable.setCustomWhereConditionsEntries(constraintTableEntries);

        /**
         * set custom other conditions entries
         */
        constraintTableEntries = new ArrayList<ExternalDbMapEntry>();
        for (FilterTableEntry constraintOtherTableEntry : table.getOtherFilterEntries()) {
            ExternalDbMapEntry externalMapperTableEntry = new ExternalDbMapEntry();
            externalMapperTableEntry.setExpression(constraintOtherTableEntry.getExpression());
            externalMapperTableEntry.setName(constraintOtherTableEntry.getName());
            constraintTableEntries.add(externalMapperTableEntry);
        }
        externalMapperTable.setCustomOtherConditionsEntries(constraintTableEntries);

        outputTables.add(externalMapperTable);
    }

    /**
     *
     * DOC amaumont Comment method "fillExternalTable".
     *
     * @param table
     * @param externalMapperTable
     */
    private void fillExternalTable(VarsTable table, ExternalDbMapTable externalMapperTable) {
        fillExternalTableWithCommonsData(table, externalMapperTable);
        varsTables.add(externalMapperTable);
    }

}
