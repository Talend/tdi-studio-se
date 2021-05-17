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
package org.talend.designer.mapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.EList;
import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.IExternalData;
import org.talend.core.model.process.IExternalNode;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.process.node.IExternalMapTable;
import org.talend.core.model.process.node.MapperExternalNode;
import org.talend.core.service.IDesignerMapperService;
import org.talend.designer.core.model.utils.emf.talendfile.AbstractExternalData;
import org.talend.designer.mapper.external.data.ExternalMapperData;
import org.talend.designer.mapper.external.data.ExternalMapperTable;
import org.talend.designer.mapper.external.data.ExternalMapperTableEntry;
import org.talend.designer.mapper.model.emf.mapper.InputTable;
import org.talend.designer.mapper.model.emf.mapper.MapperData;
import org.talend.designer.mapper.model.emf.mapper.MapperFactory;
import org.talend.designer.mapper.model.emf.mapper.MapperTableEntry;
import org.talend.designer.mapper.model.emf.mapper.OutputTable;
import org.talend.designer.mapper.utils.MapperHelper;

/**
 * DOC YeXiaowei class global comment. Detailled comment <br/>
 *
 */
public class DesignerMapperService implements IDesignerMapperService {

    /* bug 21080 */
    private Map<String, String> oldMappingMap = new HashMap<String, String>();

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.mapper.IDesignerMapperSerivce#isVirtualComponent(org.talend.core.model.process.INode)
     */
    public boolean isVirtualComponent(INode node) {
        return MapperHelper.isGeneratedAsVirtualComponent(node);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.mapper.IDesignerMapperService#renameJoinTable(java.lang.Process,
     * org.talend.core.model.process.IExternalData, java.util.List)
     */
    public void renameJoinTable(IProcess process, IExternalData data, List<String> createdNames) {
        if (data instanceof ExternalMapperData) {
            ExternalMapperData extenalData = (ExternalMapperData) data;
            final List<ExternalMapperTable> outputTables = extenalData.getOutputTables();
            if (outputTables != null) {
                for (ExternalMapperTable table : outputTables) {
                    if (table.getIsJoinTableOf() != null && !"".equals(table.getIsJoinTableOf())) {
                        final String newName = createNewConnectionName(process, table.getName(), createdNames);
                        table.setName(newName);
                    }
                }
            }
        }

    }

    private String createNewConnectionName(IProcess process, String oldName, List<String> createdNames) {
        String newName = null;

        if (process.checkValidConnectionName(oldName, true)) {
            newName = oldName;
        } else {
            newName = checkExistingNames(process, "copyOf" + oldName); //$NON-NLS-1$
        }
        newName = checkNewNames(process, newName, createdNames);

        createdNames.add(newName);
        return newName;
    }

    private String checkExistingNames(IProcess process, final String oldName) {
        String tmpName = oldName + "_"; //$NON-NLS-1$
        String newName = oldName;

        int index = 0;
        while (!process.checkValidConnectionName(newName, true)) {
            newName = tmpName + (index++);
        }
        return newName;
    }

    private String checkNewNames(IProcess process, final String oldName, List<String> createdNames) {
        String tmpName = oldName + "_"; //$NON-NLS-1$

        String newName = oldName;
        int index = 0;
        while (createdNames.contains(newName)) {
            newName = tmpName + index++;
        }
        // check the name again in process.
        while (!process.checkValidConnectionName(newName, true)) {
            newName = tmpName + (index++);
        }
        return newName;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.designer.mapper.IDesignerMapperService#getJoinTableNames(org.talend.core.model.process.IExternalData)
     */
    public List<String> getJoinTableNames(IExternalData data) {
        List<String> namesList = new ArrayList<String>();
        if (data instanceof ExternalMapperData) {
            ExternalMapperData extenalData = (ExternalMapperData) data;
            final List<ExternalMapperTable> outputTables = extenalData.getOutputTables();
            if (outputTables != null) {
                for (ExternalMapperTable table : outputTables) {
                    if (table.getIsJoinTableOf() != null && !"".equals(table.getIsJoinTableOf())) {
                        namesList.add(table.getName());
                    }
                }
            }
        }

        return namesList;
    }

    public void createAutoMappedNode(INode node, IConnection inputConnection, IConnection outputConnection) {
        MapperData data = (MapperData) node.getExternalNode().getExternalEmfData();
        data.setUiProperties(MapperFactory.eINSTANCE.createUiProperties());
        data.getUiProperties().setShellMaximized(true);

        data.getInputTables().clear();
        InputTable inputTable = MapperFactory.eINSTANCE.createInputTable();
        data.getInputTables().add(inputTable);
        inputTable.setName(inputConnection.getName());

        if (inputConnection.getMetadataTable() != null) {
            for (IMetadataColumn column : inputConnection.getMetadataTable().getListColumns()) {
                MapperTableEntry tableEntry = MapperFactory.eINSTANCE.createMapperTableEntry();
                tableEntry.setName(column.getLabel());
                tableEntry.setType(column.getTalendType());
                tableEntry.setNullable(column.isNullable());
                inputTable.getMapperTableEntries().add(tableEntry);
            }
        }

        data.getOutputTables().clear();
        OutputTable outputTable = MapperFactory.eINSTANCE.createOutputTable();
        data.getOutputTables().add(outputTable);
        outputTable.setName(outputConnection.getName());

        if (outputConnection.getMetadataTable() != null) {
            for (IMetadataColumn column : outputConnection.getMetadataTable().getListColumns()) {
                MapperTableEntry tableEntry = MapperFactory.eINSTANCE.createMapperTableEntry();
                tableEntry.setName(column.getLabel());
                tableEntry.setType(column.getTalendType());
                tableEntry.setNullable(column.isNullable());
                tableEntry.setExpression(inputConnection.getName() + "." + column.getLabel());
                outputTable.getMapperTableEntries().add(tableEntry);
            }
        }

        ((MapperComponent) node.getExternalNode()).buildExternalData(data);
    }

    public void updateLink(INode node, IConnection oldConnection, IConnection newConnection) {
        // MapperData data = (MapperData) node.getExternalNode().getExternalEmfData();
        //
        // for (InputTable inputTable : data.getInputTables()) {
        // if (inputTable.getName().equals(oldConnection.getName())) {
        // inputTable.setName(newConnection.getName());
        // }
        // }
        //
        // ((MapperComponent) node.getExternalNode()).buildExternalData(data);
        ((MapperExternalNode) node.getExternalNode()).renameInputConnection(oldConnection.getName(), newConnection.getName());
    }

    public List<String> getRepositorySchemaIds(AbstractExternalData nodeData) {
        List<String> schemaIds = new ArrayList<String>();
        if (nodeData != null && nodeData instanceof MapperData) {
            MapperData mapperData = (MapperData) nodeData;
            EList<InputTable> inputTables = mapperData.getInputTables();
            if (inputTables != null && inputTables.size() > 0) {
                for (InputTable inputTable : inputTables) {
                    String id = inputTable.getId();
                    if (id != null && !schemaIds.contains(id)) {
                        schemaIds.add(id);
                    }
                }
            }
            EList<OutputTable> outputTables = mapperData.getOutputTables();
            if (outputTables != null && outputTables.size() > 0) {
                for (OutputTable outputTable : outputTables) {
                    String id = outputTable.getId();
                    if (id != null && !schemaIds.contains(id)) {
                        schemaIds.add(id);
                    }
                }
            }
        }

        return schemaIds;
    }

    public List<String> getRepositorySchemaIds(IExternalData nodeData) {
        List<String> schemaIds = new ArrayList<String>();
        if (nodeData != null && nodeData instanceof ExternalMapperData) {
            ExternalMapperData mapperData = (ExternalMapperData) nodeData;
            List<ExternalMapperTable> inputTables = mapperData.getInputTables();
            if (inputTables != null && inputTables.size() > 0) {
                for (ExternalMapperTable inputTable : inputTables) {
                    String id = inputTable.getId();
                    if (id != null && !schemaIds.contains(id)) {
                        schemaIds.add(id);
                    }
                }
            }
            List<ExternalMapperTable> outputTables = mapperData.getOutputTables();
            if (outputTables != null && outputTables.size() > 0) {
                for (ExternalMapperTable outputTable : outputTables) {
                    String id = outputTable.getId();
                    if (id != null && !schemaIds.contains(id)) {
                        schemaIds.add(id);
                    }
                }
            }
        }

        return schemaIds;
    }

    public String getRepositorySchemaId(IExternalMapTable table) {
        if (table != null && table instanceof ExternalMapperTable) {
            return ((ExternalMapperTable) table).getId();
        }
        return null;
    }

    public void updateMapperTableEntries(IExternalNode externalNode, String schemaId, IMetadataTable metadataTable) {
        if (externalNode == null || schemaId == null || metadataTable == null) {
            return;
        }
        if (externalNode instanceof MapperComponent) {
            MapperComponent component = (MapperComponent) externalNode;
            IExternalData nodeData = externalNode.getExternalData();
            if (nodeData != null && nodeData instanceof ExternalMapperData) {
                ExternalMapperData mapperData = (ExternalMapperData) nodeData;

                List<ExternalMapperTable> outputTables = mapperData.getOutputTables();
                if (outputTables != null && outputTables.size() > 0) {
                    for (ExternalMapperTable outputTable : outputTables) {
                        updateEntriesByMetaColumns(schemaId, metadataTable, outputTable, component);

                    }
                }
            }
        }
    }

    public void renameMapperTable(IExternalNode externalNode, String schemaId, String newSchemaId, IMetadataTable metadataTable) {
        if (externalNode == null || schemaId == null || metadataTable == null) {
            return;
        }
        if (externalNode instanceof MapperComponent) {
            MapperComponent component = (MapperComponent) externalNode;
            IExternalData nodeData = externalNode.getExternalData();

            if (nodeData != null && nodeData instanceof ExternalMapperData) {
                ExternalMapperData mapperData = (ExternalMapperData) nodeData;
                List<ExternalMapperTable> outputTables = mapperData.getOutputTables();
                if (outputTables != null && outputTables.size() > 0) {
                    for (ExternalMapperTable outputTable : outputTables) {
                        if (!schemaId.equals(outputTable.getId())) {
                            continue;
                        }
                        outputTable.setId(newSchemaId);
                        updateEntriesByMetaColumns(newSchemaId, metadataTable, outputTable, component);
                    }
                }
            }
        }
    }

    public boolean isSameMetadata(IExternalNode externalNode, String schemaId, IMetadataTable metadataTable) {
        boolean isSame = true;
        if (externalNode == null || schemaId == null || metadataTable == null) {
            return false;
        }

        if (externalNode instanceof MapperComponent) {
            MapperComponent component = (MapperComponent) externalNode;
            IExternalData nodeData = externalNode.getExternalData();
            if (nodeData != null && nodeData instanceof ExternalMapperData) {
                ExternalMapperData mapperData = (ExternalMapperData) nodeData;

                List<ExternalMapperTable> outputTables = mapperData.getOutputTables();
                if (outputTables != null && outputTables.size() > 0) {
                    for (ExternalMapperTable outputTable : outputTables) {
                        if (schemaId.equals(outputTable.getId())) {
                            final IMetadataTable mapperTable = getMetadataTable(component, outputTable.getName());
                            if (mapperTable == null || !mapperTable.sameMetadataAs(metadataTable, IMetadataColumn.OPTIONS_NONE)) {
                                return false;
                            }
                        }
                    }
                }
                // for (ExternalMapperTable extTable : extTables) {
                // isSame = isMetadataSame(extTable, metadataTable);
                // if (!isSame)
                // return isSame;
                // }
            }
        }
        return isSame;
    }

    private IMetadataTable getMetadataTable(MapperComponent component, String name) {
        if (component.getMetadataList() != null) {
            for (IMetadataTable table : component.getMetadataList()) {
                if (table.getTableName() != null && table.getTableName().equals(name)) {
                    return table;
                }
            }
        }

        return null;

    }

    private void updateEntriesByMetaColumns(String schemaId, IMetadataTable metadataTable, ExternalMapperTable table,
            MapperComponent component) {
        String id = table.getId();
        if (schemaId.equals(id)) {
            List<ExternalMapperTableEntry> mapperTableEntries = table.getMetadataTableEntries();
            List<ExternalMapperTableEntry> newTableEntries = new ArrayList<ExternalMapperTableEntry>();
            /* bug 21080 */
            for (ExternalMapperTableEntry oldEntry : mapperTableEntries) {
                String expression = oldEntry.getExpression();
                String columnname = oldEntry.getName();
                if (expression != null) {
                    oldMappingMap.put(columnname, expression);
                }
            }
            List<IMetadataColumn> columns = metadataTable.getListColumns();
            if (columns != null) {
                for (IMetadataColumn metadataColumn : columns) {
                    ExternalMapperTableEntry tableEntry = new ExternalMapperTableEntry();
                    tableEntry.setName(metadataColumn.getLabel());
                    tableEntry.setType(metadataColumn.getTalendType());
                    tableEntry.setNullable(metadataColumn.isNullable());

                    ExternalMapperTableEntry sameEntry = null;
                    for (ExternalMapperTableEntry oldEntry : mapperTableEntries) {
                        if (oldEntry.getName().equals(metadataColumn.getLabel())) {
                            sameEntry = oldEntry;
                        }
                    }
                    if (sameEntry != null) {
                        tableEntry.setExpression(sameEntry.getExpression());
                        tableEntry.setOperator(sameEntry.getOperator());
                        String oldExpression = oldMappingMap.get(tableEntry.getName());
                        if (oldExpression != null) {
                            tableEntry.setExpression(oldExpression);
                        }
                    }
                    newTableEntries.add(tableEntry);
                }
            }
            mapperTableEntries.clear();
            table.setMetadataTableEntries(newTableEntries);
            final IMetadataTable mapperTabel = getMetadataTable(component, table.getName());
            if (mapperTabel != null) {
                if (mapperTabel.getListColumns() != null) {
                    mapperTabel.getListColumns().clear();
                    mapperTabel.getListColumns().addAll(metadataTable.getListColumns());
                } else {
                    mapperTabel.setListColumns(metadataTable.getListColumns());
                }
            }
        }
        oldMappingMap.clear();
    }

    public boolean isMetacolumnSame(ExternalMapperTableEntry extColumn, IMetadataColumn column) {
        if (extColumn == null || column == null) {
            return false;
        }
        if (!sameStringValue(extColumn.getName(), column.getLabel())) {
            return false;
        }
        if (!sameStringValue(extColumn.getType(), column.getTalendType())) {
            return false;
        }
        if (extColumn.isNullable() != column.isNullable()) {
            return false;
        }

        return true;
    }

    private boolean sameStringValue(String value1, String value2) {
        if (value1 == null) {
            if (value2 == null) {
                return true;
            } else {
                return value2.equals(""); //$NON-NLS-1$
            }
        } else {
            if (value1.equals("") && value2 == null) { //$NON-NLS-1$
                return true;
            } else {
                return value1.equals(value2);
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.core.service.IDesignerMapperService#getExpressionFilter(org.talend.core.model.process.IExternalNode)
     */
    public List<String> getExpressionFilter(IExternalData nodeData) {
        List<String> experssionFilters = new ArrayList<String>();
        if (nodeData instanceof ExternalMapperData) {
            ExternalMapperData mapperData = (ExternalMapperData) nodeData;
            for (ExternalMapperTable inputMapperTable : mapperData.getInputTables()) {
                experssionFilters.add(inputMapperTable.getExpressionFilter());
            }
            for (ExternalMapperTable outputMapperTable : mapperData.getOutputTables()) {
                experssionFilters.add(outputMapperTable.getExpressionFilter());
            }
        }
        return experssionFilters;
    }

}
