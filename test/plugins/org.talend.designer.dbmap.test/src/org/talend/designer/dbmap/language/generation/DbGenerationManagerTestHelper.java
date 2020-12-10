// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.dbmap.language.generation;

import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import org.talend.core.model.components.ComponentCategory;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.context.JobContext;
import org.talend.core.model.context.JobContextManager;
import org.talend.core.model.context.JobContextParameter;
import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.MetadataColumn;
import org.talend.core.model.metadata.MetadataTable;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.IContextParameter;
import org.talend.core.model.utils.TalendTextUtils;
import org.talend.core.ui.component.ComponentsFactoryProvider;
import org.talend.designer.core.model.components.ElementParameter;
import org.talend.designer.core.ui.editor.connections.Connection;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.process.Process;
import org.talend.designer.dbmap.DbMapComponent;
import org.talend.designer.dbmap.external.data.ExternalDbMapData;
import org.talend.designer.dbmap.external.data.ExternalDbMapEntry;
import org.talend.designer.dbmap.external.data.ExternalDbMapTable;

/**
 * created by wchen on Nov 23, 2017 Detailled comment
 *
 */
public class DbGenerationManagerTestHelper {

    protected DbMapComponent dbMapComponent;

    protected Process process;

    protected void init(String schema, String main_table, String main_alias, String lookup_table, String lookup_alias) {
        List<IConnection> incomingConnections = new ArrayList<IConnection>();
        String[] mainTableEntities = new String[] { "id", "name", "age" };
        String[] lookupEndtities = new String[] { "id", "score" };
        incomingConnections.add(mockConnection(schema, main_table, mainTableEntities));
        incomingConnections.add(mockConnection(schema, lookup_table, lookupEndtities));
        dbMapComponent.setIncomingConnections(incomingConnections);

        ExternalDbMapData externalData = new ExternalDbMapData();
        List<ExternalDbMapTable> inputs = new ArrayList<ExternalDbMapTable>();
        List<ExternalDbMapTable> outputs = new ArrayList<ExternalDbMapTable>();
        // main table
        ExternalDbMapTable inputTable = new ExternalDbMapTable();
        String mainTableName = "".equals(schema) ? main_table : schema + "." + main_table;
        // quote will be removed in the ui for connections ,so we do the same for test
        String mainTableNameNoQuote = TalendTextUtils.removeQuotes(mainTableName);
        inputTable.setTableName(mainTableNameNoQuote);
        inputTable.setName(mainTableName);
        if (main_alias != null && !"".equals(main_alias)) {
            inputTable.setAlias(main_alias);
        }
        List<ExternalDbMapEntry> entities = getMetadataEntities(mainTableEntities, new String[3]);
        inputTable.setMetadataTableEntries(entities);
        inputs.add(inputTable);
        // lookup table
        inputTable = new ExternalDbMapTable();
        String lookupName = "".equals(schema) ? lookup_table : schema + "." + lookup_table;
        inputTable.setTableName(lookupName);
        inputTable.setName(lookupName);
        if (lookup_alias != null && !"".equals(lookup_alias)) {
            inputTable.setAlias(lookup_alias);
        }
        entities = getMetadataEntities(lookupEndtities, new String[2]);
        inputTable.setMetadataTableEntries(entities);
        inputs.add(inputTable);

        // output
        ExternalDbMapTable outputTable = new ExternalDbMapTable();
        outputTable.setName("grade");
        String[] names = new String[] { "id", "name", "age", "score" };
        String mainTable = mainTableName;
        if (main_alias != null && !"".equals(main_alias)) {
            mainTable = main_alias;
        }
        String lookupTable = lookupName;
        if (lookup_alias != null && !"".equals(lookup_alias)) {
            lookupTable = lookup_alias;
        }
        String[] expressions = new String[] { mainTable + ".id", mainTable + ".name", mainTable + ".age", lookupTable + ".score" };
        outputTable.setMetadataTableEntries(getMetadataEntities(names, expressions));
        outputs.add(outputTable);

        externalData.setInputTables(inputs);
        externalData.setOutputTables(outputs);
        dbMapComponent.setExternalData(externalData);
        List<IMetadataTable> metadataList = new ArrayList<IMetadataTable>();
        MetadataTable metadataTable = getMetadataTable(names);
        metadataTable.setLabel("grade");
        metadataList.add(metadataTable);
        dbMapComponent.setMetadataList(metadataList);
        JobContext newContext = new JobContext("Default");
        List<IContextParameter> newParamList = new ArrayList<IContextParameter>();
        newContext.setContextParameterList(newParamList);
        JobContextParameter param = new JobContextParameter();
        param.setName("schema");
        newParamList.add(param);
        param = new JobContextParameter();
        param.setName("main_table");
        newParamList.add(param);
        param = new JobContextParameter();
        param.setName("lookup");
        newParamList.add(param);
        process = mock(Process.class);
        JobContextManager contextManger = new JobContextManager();
        contextManger.setDefaultContext(newContext);
        when(process.getContextManager()).thenReturn(contextManger);
        dbMapComponent.setProcess(process);

    }

    protected void init4ColumnAlias(String schema, String main_table, String main_alias, String lookup_table,
            String lookup_alias) {
        List<IConnection> incomingConnections = new ArrayList<IConnection>();
        String[] mainTableEntities = new String[] { "id", "name", "age" };
        String[] lookupEndtities = new String[] { "id", "score" };
        incomingConnections.add(mockConnection(schema, main_table, mainTableEntities));
        incomingConnections.add(mockConnection(schema, lookup_table, lookupEndtities));
        dbMapComponent.setIncomingConnections(incomingConnections);

        ExternalDbMapData externalData = new ExternalDbMapData();
        List<ExternalDbMapTable> inputs = new ArrayList<ExternalDbMapTable>();
        List<ExternalDbMapTable> outputs = new ArrayList<ExternalDbMapTable>();
        // main table
        ExternalDbMapTable inputTable = new ExternalDbMapTable();
        String mainTableName = "".equals(schema) ? main_table : schema + "." + main_table;
        // quote will be removed in the ui for connections ,so we do the same for test
        String mainTableNameNoQuote = TalendTextUtils.removeQuotes(mainTableName);
        inputTable.setTableName(mainTableNameNoQuote);
        inputTable.setName(mainTableName);
        if (main_alias != null && !"".equals(main_alias)) {
            inputTable.setAlias(main_alias);
        }
        List<ExternalDbMapEntry> entities = getMetadataEntities(mainTableEntities, new String[3]);
        inputTable.setMetadataTableEntries(entities);
        inputs.add(inputTable);
        // lookup table
        inputTable = new ExternalDbMapTable();
        String lookupName = "".equals(schema) ? lookup_table : schema + "." + lookup_table;
        inputTable.setTableName(lookupName);
        inputTable.setName(lookupName);
        if (lookup_alias != null && !"".equals(lookup_alias)) {
            inputTable.setAlias(lookup_alias);
        }
        entities = getMetadataEntities(lookupEndtities, new String[2]);
        inputTable.setMetadataTableEntries(entities);
        inputs.add(inputTable);

        // output
        ExternalDbMapTable outputTable = new ExternalDbMapTable();
        outputTable.setName("grade");
        String[] names = new String[] { "id", "name", "age", "score" };
        String mainTable = mainTableName;
        if (main_alias != null && !"".equals(main_alias)) {
            mainTable = main_alias;
        }
        String lookupTable = lookupName;
        if (lookup_alias != null && !"".equals(lookup_alias)) {
            lookupTable = lookup_alias;
        }
        String[] expressions = new String[] { mainTable + ".id", mainTable + ".name_alias", mainTable + ".age_alias",
                lookupTable + ".score" };
        outputTable.setMetadataTableEntries(getMetadataEntities(names, expressions));
        outputs.add(outputTable);

        externalData.setInputTables(inputs);
        externalData.setOutputTables(outputs);
        dbMapComponent.setExternalData(externalData);
        List<IMetadataTable> metadataList = new ArrayList<IMetadataTable>();
        MetadataTable metadataTable = getMetadataTable(names);
        metadataTable.setLabel("grade");
        metadataList.add(metadataTable);
        dbMapComponent.setMetadataList(metadataList);
        JobContext newContext = new JobContext("Default");
        List<IContextParameter> newParamList = new ArrayList<IContextParameter>();
        newContext.setContextParameterList(newParamList);
        JobContextParameter param = new JobContextParameter();
        param.setName("schema");
        newParamList.add(param);
        param = new JobContextParameter();
        param.setName("main_table");
        newParamList.add(param);
        param = new JobContextParameter();
        param.setName("lookup");
        newParamList.add(param);
        process = mock(Process.class);
        JobContextManager contextManger = new JobContextManager();
        contextManger.setDefaultContext(newContext);
        when(process.getContextManager()).thenReturn(contextManger);
        dbMapComponent.setProcess(process);
    }

    protected IConnection mockConnection(String schemaName, String tableName, String[] columns) {
        return mockConnection(null, schemaName, tableName, columns);
    }

    protected IConnection mockConnection(Node node, String schemaName, String tableName, String[] columns) {
        Connection connection = mock(Connection.class);
        if(node == null){
            node = mock(Node.class);
        }
        ElementParameter param = new ElementParameter(node);
        param.setName("ELT_SCHEMA_NAME");
        param.setValue(schemaName);
        when(node.getElementParameter("ELT_SCHEMA_NAME")).thenReturn(param);
        param = new ElementParameter(node);
        param.setName("ELT_TABLE_NAME");
        param.setValue(tableName);
        when(node.getElementParameter("ELT_TABLE_NAME")).thenReturn(param);
        String tName = "".equals(schemaName) ? tableName : schemaName + "." + tableName;
        // quote will be removed in the ui for connections ,so we do the same for test
        tName = TalendTextUtils.removeQuotes(tName);
        when(connection.getName()).thenReturn(tName);
        when(connection.getSource()).thenReturn(node);
        when(connection.getMetaName()).thenReturn(tName);
        when(connection.getUniqueName()).thenReturn(tName);
        IMetadataTable table = new MetadataTable();
        table.setLabel(tableName);
        table.setTableName(tableName);
        List<IMetadataColumn> listColumns = new ArrayList<IMetadataColumn>();
        for (String columnName : columns) {
            IMetadataColumn column = new MetadataColumn();
            column.setLabel(columnName);
            column.setOriginalDbColumnName(columnName);
            listColumns.add(column);
        }
        table.setListColumns(listColumns);
        when(connection.getMetadataTable()).thenReturn(table);

        return connection;
    }

    protected Node mockNode(DbMapComponent mapCom) {
        Node node = mock(Node.class);
        when(node.isELTComponent()).thenReturn(true);
        IComponent component = ComponentsFactoryProvider.getInstance().get("tELTMap", ComponentCategory.CATEGORY_4_DI.getName());
        when(node.getComponent()).thenReturn(component);
        when(node.getExternalNode()).thenReturn(mapCom);
        return node;
    }

    protected List<ExternalDbMapEntry> getMetadataEntities(String[] entitiesName, String[] expressions) {
        List<ExternalDbMapEntry> entities = new ArrayList<ExternalDbMapEntry>();
        for (int i = 0; i < entitiesName.length; i++) {
            ExternalDbMapEntry entity = new ExternalDbMapEntry();
            entity.setName(entitiesName[i]);
            if (i < expressions.length && !"".equals(expressions[i]) && expressions[i] != null) {
                entity.setExpression(expressions[i]);
            }
            entities.add(entity);
        }
        return entities;
    }

    protected MetadataTable getMetadataTable(String[] entitiesName) {
        return getMetadataTable(entitiesName, null);
    }

    protected MetadataTable getMetadataTable(String[] entitiesName, String[] dbColumns) {
        if (dbColumns == null) {
            dbColumns = entitiesName;
        }
        MetadataTable table = new MetadataTable();
        for (int i = 0; i < entitiesName.length; i++) {
            String element = entitiesName[i];
            MetadataColumn column = new MetadataColumn();
            column.setLabel(element);
            column.setOriginalDbColumnName(dbColumns[i]);
            table.getListColumns().add(column);
        }
        return table;
    }
}
