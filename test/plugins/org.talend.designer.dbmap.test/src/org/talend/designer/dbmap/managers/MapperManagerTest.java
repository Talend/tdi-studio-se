// ============================================================================
//
// Copyright (C) 2006-2020 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.dbmap.managers;


import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.talend.commons.utils.VersionUtils;
import org.talend.core.CorePlugin;
import org.talend.core.context.Context;
import org.talend.core.context.RepositoryContext;
import org.talend.core.model.components.ComponentCategory;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.process.EConnectionType;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.INodeConnector;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.Property;
import org.talend.core.ui.component.ComponentsFactoryProvider;
import org.talend.designer.core.model.components.ElementParameter;
import org.talend.designer.core.model.components.NodeConnector;
import org.talend.designer.core.ui.editor.connections.Connection;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.process.Process;
import org.talend.designer.dbmap.DbMapComponent;
import org.talend.designer.dbmap.external.data.ExternalDbMapEntry;
import org.talend.designer.dbmap.external.data.ExternalDbMapTable;
import org.talend.designer.dbmap.model.tableentry.TableEntryLocation;

/**
 * 
 * created by hcyi on Oct 12, 2020 Detailled comment
 *
 */
public class MapperManagerTest {

    private MapperManager mapperManager;

    private DbMapComponent dbMapComponent;

    private static final String SOURCE_COMPONENT_NAME = "tELTTeradataInput";

    private static final String TARGET_COMPONENT_NAME = "tELTTeradataMap";

    private static IComponent sourceComponent;

    private static IComponent targetComponent;

    private Node sourceNode;

    private Node targetNode;

    private IConnection connection;

    private List<ExternalDbMapTable> inputs;

    private List<ExternalDbMapTable> outputs;

    private ExternalDbMapTable input;

    private ExternalDbMapEntry outputEntry;

    @Before
    public void before() {
        sourceComponent = ComponentsFactoryProvider.getInstance().get(SOURCE_COMPONENT_NAME,
                ComponentCategory.CATEGORY_4_DI.getName());
        targetComponent = ComponentsFactoryProvider.getInstance().get(TARGET_COMPONENT_NAME,
                ComponentCategory.CATEGORY_4_DI.getName());

        Process process = new Process(createProperty());
        sourceNode = new Node(sourceComponent, process);
        INodeConnector connector = new NodeConnector(sourceNode);
        connector.setName("connector");
        connector.setDefaultConnectionType(EConnectionType.FLOW_MAIN);
        ArrayList<INodeConnector> connectors = new ArrayList<INodeConnector>();
        connectors.add(connector);
        sourceNode.setListConnector(connectors);

        targetNode = new Node(targetComponent, process);
        dbMapComponent = new DbMapComponent();
        targetNode.setExternalNode(dbMapComponent);
        connection = new Connection(sourceNode, targetNode, EConnectionType.FLOW_MAIN, "connector", "meta", "oldTable", true);

        inputs = (List<ExternalDbMapTable>) targetNode.getExternalData().getInputTables();
        input = new ExternalDbMapTable();
        input.setName("oldTable");
        input.setTableName("oldTable");
        inputs.add(input);

        outputs = (List<ExternalDbMapTable>) targetNode.getExternalData().getOutputTables();
        ExternalDbMapTable output = new ExternalDbMapTable();
        outputEntry = new ExternalDbMapEntry("oldTable", "oldTable.column");
        output.setMetadataTableEntries(new ArrayList<ExternalDbMapEntry>());
        output.getMetadataTableEntries().add(outputEntry);
        outputs.add(output);

        ElementParameter comName = new ElementParameter(dbMapComponent);
        comName.setName("COMPONENT_NAME");
        comName.setValue("tELTTeradataMap");
        List<ElementParameter> list = new ArrayList<>();
        list.add(comName);
        dbMapComponent.setElementParameters(list);

        mapperManager = new MapperManager(dbMapComponent);
        mapperManager.getUiManager();
    }

    @After
    public void tearDown() throws Exception {
        sourceNode = null;
        targetNode = null;
        connection = null;
        inputs = null;
        outputs = null;
        input = null;
        outputEntry = null;

        mapperManager = null;
        dbMapComponent = null;
        sourceComponent = null;
        targetComponent = null;
    }

    @Test
    public void testRenameInputAliasTable4NULL() throws InterruptedException {
        String oldName = input.getName();
        String newName = null;
        outputEntry.setExpression(oldName + ".column");
        TableEntryLocation oldLocation = new TableEntryLocation(oldName, null);
        TableEntryLocation newLocation = new TableEntryLocation(newName, null);
        // mapperManager.replacePreviousLocationInAllExpressions(oldLocation, newLocation, true);
        dbMapComponent.replaceLocationsInAllExpressions(oldLocation, newLocation, true);

        validateResult(newName);
    }

    @Test
    public void testRenameInputAliasTable4Empty() throws InterruptedException {
        String oldName = input.getName();
        String newName = "";
        outputEntry.setExpression(oldName + ".column");
        TableEntryLocation oldLocation = new TableEntryLocation(oldName, null);
        TableEntryLocation newLocation = new TableEntryLocation(newName, null);
        // mapperManager.replacePreviousLocationInAllExpressions(oldLocation, newLocation, true);
        dbMapComponent.replaceLocationsInAllExpressions(oldLocation, newLocation, true);

        validateResult(newName);
    }

    @Test
    public void testRenameInputAliasTable4addAlias() throws InterruptedException {
        String oldName = input.getName();
        String newName = "newAlias";
        outputEntry.setExpression(oldName + ".column");
        TableEntryLocation oldLocation = new TableEntryLocation(oldName, null);
        TableEntryLocation newLocation = new TableEntryLocation(newName, null);
        // mapperManager.replacePreviousLocationInAllExpressions(oldLocation, newLocation, true);
        dbMapComponent.replaceLocationsInAllExpressions(oldLocation, newLocation, true);

        validateResult(newName);
    }

    @Test
    public void testRenameInputAliasTable4RenameAlias() throws InterruptedException {
        input.setAlias("oldAlias");
        String oldName = input.getName();
        String newName = "newAlias";
        outputEntry.setExpression(oldName + ".column");
        TableEntryLocation oldLocation = new TableEntryLocation(oldName, null);
        TableEntryLocation newLocation = new TableEntryLocation(newName, null);
        // mapperManager.replacePreviousLocationInAllExpressions(oldLocation, newLocation, true);
        dbMapComponent.replaceLocationsInAllExpressions(oldLocation, newLocation, true);

        validateResult(newName);
    }

    @Test
    public void testRenameInputAliasTable4DelAlias() throws InterruptedException {
        input.setAlias("oldAlias");
        String oldName = input.getName();
        input.setAlias(null);
        String newName = input.getName();
        outputEntry.setExpression(oldName + ".column");
        TableEntryLocation oldLocation = new TableEntryLocation(oldName, null);
        TableEntryLocation newLocation = new TableEntryLocation(newName, null);
        // mapperManager.replacePreviousLocationInAllExpressions(oldLocation, newLocation, true);
        dbMapComponent.replaceLocationsInAllExpressions(oldLocation, newLocation, true);

        validateResult(newName);
    }

    private void validateResult(String expressionValue) {
        assertEquals(expressionValue + ".column", outputs.get(0).getMetadataTableEntries().get(0).getExpression());
    }

    private static Property createProperty() {
        Property property = PropertiesFactory.eINSTANCE.createProperty();
        property.setAuthor(((RepositoryContext) CorePlugin.getContext().getProperty(Context.REPOSITORY_CONTEXT_KEY)).getUser());
        property.setVersion(VersionUtils.DEFAULT_VERSION);
        return property;
    }
}
