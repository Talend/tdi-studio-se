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
package org.talend.designer.mapper.utils.problems;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.talend.core.model.components.ComponentCategory;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.MetadataColumn;
import org.talend.core.model.process.EConnectionType;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.IExternalData;
import org.talend.core.model.process.Problem;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.Property;
import org.talend.core.ui.component.ComponentsFactoryProvider;
import org.talend.designer.core.ui.editor.connections.Connection;
import org.talend.designer.core.ui.editor.nodecontainer.NodeContainer;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.process.Process;
import org.talend.designer.mapper.MapperComponent;
import org.talend.designer.mapper.MapperMain;
import org.talend.designer.mapper.external.connection.IOConnection;
import org.talend.designer.mapper.external.converter.ExternalDataConverter;
import org.talend.designer.mapper.external.data.ExternalMapperData;
import org.talend.designer.mapper.external.data.ExternalMapperTable;
import org.talend.designer.mapper.external.data.ExternalMapperTableEntry;
import org.talend.designer.mapper.managers.MapperManager;
import org.talend.designer.mapper.model.table.InputTable;
import org.talend.designer.mapper.model.table.TMAP_MATCHING_MODE;

/**
 * created by wchen on Mar 10, 2017 Detailled comment
 *
 */
public class ProblemsAnalyserTest {

    @Test
    public void testCheckLookupTablesKeyProblems() {
        MapperComponent mapperComponent = createMapperComponent();
        ProblemsAnalyser analyser = new ProblemsAnalyser(mapperComponent.getMapperMain().getMapperManager());
        IExternalData externalData = mapperComponent.getExternalData();
        ExternalMapperTable lookupTable = (ExternalMapperTable) externalData.getInputTables().get(1);

        // match mode:Unique Match +no expression
        lookupTable.setMatchingMode(TMAP_MATCHING_MODE.UNIQUE_MATCH.name());
        analyser.checkLookupTablesKeyProblems((ExternalMapperData) externalData);
        List<Problem> problems = analyser.getProblems();
        Assert.assertEquals(problems.size(), 1);
        Assert.assertEquals(problems.get(0).getDescription(),
                "The lookup table 'row2' should have at least one expression key filled.");

        // match mode:All Row +expression
        lookupTable.setMatchingMode(TMAP_MATCHING_MODE.ALL_ROWS.name());
        lookupTable.getMetadataTableEntries().get(0).setExpression("row1.newColumn1");
        analyser = new ProblemsAnalyser(mapperComponent.getMapperMain().getMapperManager());
        analyser.checkLookupTablesKeyProblems((ExternalMapperData) externalData);
        problems = analyser.getProblems();
        Assert.assertEquals(problems.size(), 1);
        Assert.assertEquals(problems.get(0).getDescription(),
                "The expression key can't be used in lookup table 'row2' with match mode 'All Rows'.");

    }

    @Test
    public void testGetLookupTableProblem() {
        MapperComponent mapperComponent = createMapperComponent();
        ExternalMapperData externalData = (ExternalMapperData) mapperComponent.getExternalData();
        MapperManager mapperManager = mapperComponent.getMapperMain().getMapperManager();
        ExternalDataConverter converter = new ExternalDataConverter(mapperManager);
        MapperMain mapperMain = ((MapperComponent) mapperManager.getAbstractMapComponent()).getMapperMain();
        List<? extends IConnection> incomingConnections = new ArrayList<IConnection>(mapperManager.getAbstractMapComponent()
                .getIncomingConnections());
        ArrayList<IOConnection> inputsIOConnections = mapperMain.createIOConnections(incomingConnections);
        ArrayList<InputTable> inputTables = converter.prepareInputTables(inputsIOConnections, externalData);
        InputTable lookupTable = inputTables.get(1);
        ProblemsAnalyser analyser = new ProblemsAnalyser(mapperManager);

        // match mode:Unique Match +no expression
        lookupTable.setMatchingMode(TMAP_MATCHING_MODE.UNIQUE_MATCH);
        analyser.getLookupTableProblem(lookupTable, mapperManager.isTableHasAtLeastOneHashKey(lookupTable));
        List<Problem> problems = analyser.getProblems();
        Assert.assertEquals(problems.size(), 1);
        Assert.assertEquals(problems.get(0).getDescription(),
                "The lookup table 'row2' should have at least one expression key filled.");

        // match mode:All Row +expression
        lookupTable.setMatchingMode(TMAP_MATCHING_MODE.ALL_ROWS);
        lookupTable.getColumnEntries().get(0).setExpression("row1.newColumn1");
        analyser = new ProblemsAnalyser(mapperComponent.getMapperMain().getMapperManager());
        analyser.getLookupTableProblem(lookupTable, mapperManager.isTableHasAtLeastOneHashKey(lookupTable));
        problems = analyser.getProblems();
        Assert.assertEquals(problems.size(), 1);
        Assert.assertEquals(problems.get(0).getDescription(),
                "The expression key can't be used in lookup table 'row2' with match mode 'All Rows'.");

    }

    private MapperComponent createMapperComponent() {
        IComponent tMysqlComponent = ComponentsFactoryProvider.getInstance().get("tMysqlInput",
                ComponentCategory.CATEGORY_4_DI.getName());
        IComponent tMapComponent = ComponentsFactoryProvider.getInstance().get("tMap", ComponentCategory.CATEGORY_4_DI.getName());
        Property property1 = PropertiesFactory.eINSTANCE.createProperty();
        property1.setId("property1"); //$NON-NLS-1$
        property1.setVersion("0.1"); //$NON-NLS-1$
        property1.setLabel("test1");//$NON-NLS-1$
        Process process = new Process(property1);
        Node tMysqlInput_1 = new Node(tMysqlComponent, process);
        process.addNodeContainer(new NodeContainer(tMysqlInput_1));
        IMetadataTable tMysqlInput_1_table = tMysqlInput_1.getMetadataTable("tDBInput_1");
        createMetadataColumns(tMysqlInput_1_table, 2);
        Node tMysqlInput_2 = new Node(tMysqlComponent, process);
        process.addNodeContainer(new NodeContainer(tMysqlInput_2));
        IMetadataTable tMysqlInput_2_table = tMysqlInput_2.getMetadataTable("tDBInput_2");
        createMetadataColumns(tMysqlInput_2_table, 2);
        Node tMap_1 = new Node(tMapComponent, process);
        process.addNodeContainer(new NodeContainer(tMap_1));
        Connection row1 = new Connection(tMysqlInput_1, tMap_1, EConnectionType.FLOW_MAIN, EConnectionType.FLOW_MAIN.getName(),
                "tDBInput_1", "row1", "row1", false);
        Connection row2 = new Connection(tMysqlInput_2, tMap_1, EConnectionType.FLOW_REF, EConnectionType.FLOW_MAIN.getName(),
                "tDBInput_1", "row2", "row2", false);
        tMap_1.getExternalNode().initialize();
        MapperComponent mapperComponent = (MapperComponent) tMap_1.getExternalNode();
        ExternalMapperData externalData = (ExternalMapperData) mapperComponent.getExternalData();
        ExternalMapperTable externalTable = prepareExternalData(tMysqlInput_1_table);
        externalTable.setName(row1.getName());
        externalData.getInputTables().add(externalTable);
        externalTable = prepareExternalData(tMysqlInput_2_table);
        externalTable.setName(row2.getName());
        externalData.getInputTables().add(externalTable);
        return mapperComponent;
    }

    private ExternalMapperTable prepareExternalData(IMetadataTable table) {
        ExternalMapperTable externalMapperTable = new ExternalMapperTable();
        ArrayList<ExternalMapperTableEntry> perTableEntries = new ArrayList<ExternalMapperTableEntry>();
        for (IMetadataColumn column : table.getListColumns()) {
            ExternalMapperTableEntry externalMapperTableEntry = new ExternalMapperTableEntry();
            externalMapperTableEntry.setName(column.getLabel());
            externalMapperTableEntry.setType(column.getTalendType());
            externalMapperTableEntry.setNullable(column.isNullable());
            perTableEntries.add(externalMapperTableEntry);
        }
        externalMapperTable.setMetadataTableEntries(perTableEntries);
        return externalMapperTable;
    }

    private void createMetadataColumns(IMetadataTable table, int columns) {
        for (int i = 0; i < columns; i++) {
            MetadataColumn newColumn = new MetadataColumn();
            newColumn.setLabel("newColumn" + i);
            newColumn.setTalendType("id_String");
            table.getListColumns().add(newColumn);
        }

    }
}
