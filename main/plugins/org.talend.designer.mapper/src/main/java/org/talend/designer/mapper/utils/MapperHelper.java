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
package org.talend.designer.mapper.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.talend.core.model.components.ComponentCategory;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.INode;
import org.talend.designer.mapper.external.data.ExternalMapperData;
import org.talend.designer.mapper.external.data.ExternalMapperTable;
import org.talend.designer.mapper.external.data.ExternalMapperTableEntry;
import org.talend.designer.mapper.model.emf.mapper.AbstractInOutTable;
import org.talend.designer.mapper.model.emf.mapper.InputTable;
import org.talend.designer.mapper.model.emf.mapper.MapperData;
import org.talend.designer.mapper.model.emf.mapper.MapperFactory;
import org.talend.designer.mapper.model.emf.mapper.MapperTableEntry;
import org.talend.designer.mapper.model.emf.mapper.OutputTable;
import org.talend.designer.mapper.model.emf.mapper.SizeState;
import org.talend.designer.mapper.model.emf.mapper.UiProperties;
import org.talend.designer.mapper.model.emf.mapper.VarTable;

/**
 * DOC YeXiaowei class global comment. Detailled comment <br/>
 *
 */
public class MapperHelper {

    /**
     *
     * DOC YeXiaowei Comment method "isGeneratedAsVirtualComponent".
     *
     * @param mapperNode
     * @return
     */
    public static boolean isGeneratedAsVirtualComponent(final INode mapperNode) {
        if (isMapperOnBigDataProcess(mapperNode.getComponent().getPaletteType())) {
            return false;
        }

        boolean hasPersistentSortedLookup = false;

        List<IConnection> incomingConnections = (List<IConnection>) mapperNode.getIncomingConnections();

        ExternalMapperData internalExternalData = (ExternalMapperData) mapperNode.getExternalNode().getExternalData();

        if (internalExternalData != null && incomingConnections.size() > 0) {
            List<ExternalMapperTable> inputTables = internalExternalData.getInputTables();

            int sizeInputTables = inputTables.size();

            HashMap<String, IConnection> hNameToConnection = new HashMap<String, IConnection>();
            for (IConnection connection : incomingConnections) {
                hNameToConnection.put(connection.getName(), connection);
            }

            for (int iInputTable = 1; iInputTable < sizeInputTables; iInputTable++) { // T_TM_M_241
                ExternalMapperTable inputTable = inputTables.get(iInputTable);
                String tableName = inputTable.getName();
                IConnection connection = hNameToConnection.get(tableName);
                if (connection == null) {
                    continue;
                }

                if (inputTable != null) { // T_TM_M_245
                    List<ExternalMapperTableEntry> metadataTableEntries = inputTable.getMetadataTableEntries();
                    if (metadataTableEntries == null) {
                        continue;
                    }

                    if (inputTable.isPersistent()
                            && !"ALL_ROWS".equals(inputTable.getMatchingMode()) && !"DYNAMIC".equals(inputTable.getLookupMode())) { //$NON-NLS-1$  //$NON-NLS-2$
                        hasPersistentSortedLookup = true;
                    }

                } // T_TM_M_245
            } // T_TM_M_241
        }
        return hasPersistentSortedLookup;
    }

    public static boolean isMapperOnBigDataProcess(String componentType) {
        return ComponentCategory.CATEGORY_4_SPARK.getName().equals(componentType)
                || ComponentCategory.CATEGORY_4_SPARKSTREAMING.getName().equals(componentType)
                || ComponentCategory.CATEGORY_4_MAPREDUCE.getName().equals(componentType);
    }

    public static void saveDataToEmf(ExternalMapperData externalData, MapperData emfMapperData) {
        if (externalData == null || externalData == null) {
            return;
        }
        // input
        if (externalData.getInputTables() != null && !externalData.getInputTables().isEmpty()) {
            for (ExternalMapperTable table : externalData.getInputTables()) {
                final InputTable persistentTable = MapperFactory.eINSTANCE.createInputTable();
                setInOutPersistentTable(persistentTable, table);
                persistentTable.setLookupMode(table.getLookupMode());
                persistentTable.setMatchingMode(table.getMatchingMode());
                persistentTable.setInnerJoin(table.isInnerJoin());
                persistentTable.setPersistent(table.isPersistent());
                persistentTable.setId(table.getId());
                if (table.getGlobalMapKeysValues() != null && !table.getGlobalMapKeysValues().isEmpty()) {
                    persistentTable.getGlobalMapKeysValues().addAll(getPersistentTableEntitys(table.getGlobalMapKeysValues()));
                }
                emfMapperData.getInputTables().add(persistentTable);
            }
        }
        // output
        if (externalData.getOutputTables() != null && !externalData.getOutputTables().isEmpty()) {
            for (ExternalMapperTable table : externalData.getOutputTables()) {
                final OutputTable persistentTable = MapperFactory.eINSTANCE.createOutputTable();
                setInOutPersistentTable(persistentTable, table);
                persistentTable.setReject(table.isReject());
                persistentTable.setRejectInnerJoin(table.isRejectInnerJoin());
                persistentTable.setIsErrorRejectTable(table.isErrorRejectTable());
                persistentTable.setIsJoinTableOf(table.getIsJoinTableOf());
                persistentTable.setId(table.getId());
                emfMapperData.getOutputTables().add(persistentTable);
            }
        }
        // var
        if (externalData.getVarsTables() != null && !externalData.getVarsTables().isEmpty()) {
            for (ExternalMapperTable table : externalData.getVarsTables()) {
                final VarTable persistentVarTable = MapperFactory.eINSTANCE.createVarTable();
                persistentVarTable.setMinimized(table.isMinimized());
                persistentVarTable.setName(table.getName());
                persistentVarTable.setSizeState(getSizeState(table.getSizeState()));
                if (table.getMetadataTableEntries() != null && !table.getMetadataTableEntries().isEmpty()) {
                    persistentVarTable.getMapperTableEntries().addAll(getPersistentTableEntitys(table.getMetadataTableEntries()));
                }
                emfMapperData.getVarTables().add(persistentVarTable);
            }
        }
        // ui properties
        if (externalData.getUiProperties() != null) {
            final UiProperties createUiProperties = MapperFactory.eINSTANCE.createUiProperties();
            createUiProperties.setShellMaximized(externalData.getUiProperties().isShellMaximized());
            emfMapperData.setUiProperties(createUiProperties);
        }
    }

    /**
     *
     * set common attributes for in and output table
     *
     * @param persistentTable
     * @param table
     */
    private static void setInOutPersistentTable(AbstractInOutTable persistentTable, ExternalMapperTable table) {
        persistentTable.setActivateCondensedTool(table.isActivateCondensedTool());
        persistentTable.setActivateExpressionFilter(table.isActivateExpressionFilter());
        persistentTable.setActivateColumnNameFilter(table.isActivateColumnNameFilter());
        persistentTable.setExpressionFilter(table.getExpressionFilter());
        persistentTable.setColumnNameFilter(table.getColumnNameFilter());
        persistentTable.setMinimized(table.isMinimized());
        persistentTable.setName(table.getName());
        persistentTable.setSizeState(getSizeState(table.getSizeState()));
        if (table.getMetadataTableEntries() != null && !table.getMetadataTableEntries().isEmpty()) {
            persistentTable.getMapperTableEntries().addAll(getPersistentTableEntitys(table.getMetadataTableEntries()));

        }
    }

    private static List<MapperTableEntry> getPersistentTableEntitys(List<ExternalMapperTableEntry> entities) {
        List<MapperTableEntry> persistentEntities = new ArrayList<MapperTableEntry>();
        for (ExternalMapperTableEntry entity : entities) {
            final MapperTableEntry emfMapperTableEntry = MapperFactory.eINSTANCE.createMapperTableEntry();
            emfMapperTableEntry.setExpression(entity.getExpression());
            emfMapperTableEntry.setName(entity.getName());
            emfMapperTableEntry.setNullable(entity.isNullable());
            emfMapperTableEntry.setType(entity.getType());
            emfMapperTableEntry.setOperator(entity.getOperator());
            persistentEntities.add(emfMapperTableEntry);
        }
        return persistentEntities;
    }

    private static SizeState getSizeState(String sizeState) {
        if (SizeState.MAXIMIZED.getLiteral().equals(sizeState)) {
            return SizeState.MAXIMIZED;
        } else if (SizeState.INTERMEDIATE.getLiteral().equals(sizeState)) {
            return SizeState.INTERMEDIATE;
        } else if (SizeState.MAXIMIZED.getLiteral().equals(sizeState)) {
            return SizeState.MAXIMIZED;
        }
        return null;
    }
}
