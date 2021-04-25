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
package org.talend.designer.core.ui.action;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.PluginChecker;
import org.talend.core.model.components.ComponentCategory;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.MetadataColumn;
import org.talend.core.model.process.EConnectionType;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.IConnectionCategory;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.INodeConnector;
import org.talend.core.model.process.IProcess2;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.Property;
import org.talend.core.model.utils.TalendTextUtils;
import org.talend.core.ui.IJobletProviderService;
import org.talend.core.ui.component.ComponentsFactoryProvider;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.model.components.NodeConnector;
import org.talend.designer.core.ui.editor.connections.Connection;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.nodes.NodePart;
import org.talend.designer.core.ui.editor.process.Process;
import org.talend.designer.core.utils.ValidationRulesUtil;

/**
 * DOC Administrator class global comment. Detailled comment <br/>
 *
 * $Id: talend.epf 55206 2011-02-15 17:32:14Z mhirt $
 *
 */
public class ConnectionCreateActionTest {

    private Node node = null;

    private INodeConnector curNodeConnector;

    private EConnectionType connecType;

    private List<Object> listArgs;

    /**
     * DOC Administrator Comment method "setUp".
     *
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        Property property = PropertiesFactory.eINSTANCE.createProperty();
        IProcess2 process = new Process(property);
        IComponent sourceCom = ComponentsFactoryProvider.getInstance().get("tMysqlInput",
                ComponentCategory.CATEGORY_4_DI.getName());
        IComponent targetCom = ComponentsFactoryProvider.getInstance().get("tMysqlOutput",
                ComponentCategory.CATEGORY_4_DI.getName());
        node = new Node(sourceCom, process);
        node.setLabel("tMysqlInput_1");
        curNodeConnector = new NodeConnector(node);
        curNodeConnector.setDefaultConnectionType(EConnectionType.FLOW_MAIN);
        connecType = curNodeConnector.getDefaultConnectionType();
        Node target = new Node(targetCom, process);
        IMetadataTable table = createSimpleMetadata();
        table.setLabel("JOBS");
        table.setTableName("JOBS");
        table.setAttachedConnector("FLOW");
        List<IMetadataTable> metadataList = new ArrayList<IMetadataTable>();
        metadataList.add(table);
        node.setMetadataList(metadataList);
        Connection conn = new Connection(node, target, EConnectionType.FLOW_MAIN, "FLOW", "JOBS", "row1", false);
        List<Connection> connList = new ArrayList<Connection>();
        connList.add(conn);
        target.setIncomingConnections(connList);
        node.setOutgoingConnections(connList);
    }

    /**
     * DOC Administrator Comment method "tearDown".
     *
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    /**
     * Test method for {@link org.talend.designer.core.ui.action.ConnectionCreateAction#run()}.
     */
    @Test
    public void testRun() {
        testRun1();
        testRun2();
        testRun3();
        testRun4();
    }

    /**
     * Test method for {@link org.talend.designer.core.ui.action.ConnectionCreateAction#getConnectors()}.
     */
    @Test
    public void testGetConnectors() {

        List<INodeConnector> list = new ArrayList<INodeConnector>();
        List<INodeConnector> nodeConnectorList = new ArrayList<INodeConnector>(
                node.getConnectorsFromType(EConnectionType.FLOW_MAIN));
        int connecSize = nodeConnectorList.size();
        List<INodeConnector> toRemove = new ArrayList<INodeConnector>();
        for (INodeConnector connector : nodeConnectorList) {
            if ((connector.getMaxLinkOutput() != -1) && (connector.getCurLinkNbOutput() >= connector.getMaxLinkOutput())) {
                toRemove.add(connector);
            } else {
                if (PluginChecker.isJobLetPluginLoaded()) {
                    IJobletProviderService service = (IJobletProviderService) GlobalServiceRegister.getDefault().getService(
                            IJobletProviderService.class);
                    if (service != null) {
                        if (service.isJobletComponent(node) && !service.isBuiltTriggerConnector(node, connector)) {
                            toRemove.add(connector);
                        }

                        List<? extends IConnection> outgoingConnections = node.getOutgoingConnections();
                        if (service.isTriggerInputNode(node) && outgoingConnections != null && outgoingConnections.size() >= 1) {
                            toRemove.add(connector);
                        }
                    }
                }
            }
        }
        nodeConnectorList.removeAll(toRemove);
        assertEquals(nodeConnectorList.size(), connecSize - toRemove.size());

    }

    private IMetadataTable createSimpleMetadata() {
        IMetadataTable table = new org.talend.core.model.metadata.MetadataTable();

        IMetadataColumn column1 = new MetadataColumn();
        column1.setLabel("C1"); //$NON-NLS-1$
        column1.setTalendType("id_String"); //$NON-NLS-1$
        table.getListColumns().add(column1);

        IMetadataColumn column2 = new MetadataColumn();
        column2.setLabel("C2"); //$NON-NLS-1$
        column2.setTalendType("id_String"); //$NON-NLS-1$
        table.getListColumns().add(column2);

        IMetadataColumn column3 = new MetadataColumn();
        column3.setLabel("C3"); //$NON-NLS-1$
        column3.setTalendType("id_Integer"); //$NON-NLS-1$
        table.getListColumns().add(column3);

        return table;
    }

    private String getDefaultTableName() {
        StringBuffer removeQuotes = new StringBuffer();
        IElementParameter elementParam = node.getElementParameter("ELT_TABLE_NAME"); //$NON-NLS-1$
        IElementParameter schemaParam = node.getElementParameter("ELT_SCHEMA_NAME");//$NON-NLS-1$
        if (node.isELTComponent() && elementParam != null && elementParam.getFieldType().equals(EParameterFieldType.TEXT)) {
            String name2 = elementParam.getValue().toString();
            if (schemaParam != null) {
                String schema = schemaParam.getValue().toString();
                if (schema != null) {
                    schema = TalendTextUtils.removeQuotes(schema);
                    if (!"".equals(schema)) { //$NON-NLS-1$
                        removeQuotes.append(schema);
                        removeQuotes.append(".");//$NON-NLS-1$
                    }
                }
            }
            if (name2 != null) {
                name2 = TalendTextUtils.removeQuotes(name2);
                List<IMetadataTable> metaList = node.getMetadataList();
                if (metaList != null) {

                    for (IMetadataTable metadataTable : metaList) {
                        if (metadataTable != null) {
                            String tName = metadataTable.getTableName();
                            tName = TalendTextUtils.removeQuotes(tName);
                            if (tName.equals(name2)) {
                                String tableLable = metadataTable.getLabel();
                                if (tableLable != null) {
                                    tableLable = TalendTextUtils.removeQuotes(tableLable);
                                    if (!"".equals(tableLable)) {
                                        name2 = tableLable;
                                    }
                                }
                            }
                        }
                    }
                }
                String temp = name2.replaceAll(" ", "");
                if ("".equals(temp)) {
                    name2 = temp;
                }
                if (!"".equals(name2)) { //$NON-NLS-1$
                    removeQuotes.append(name2);
                    removeQuotes.append(" (");
                    removeQuotes.append(curNodeConnector.getMenuName());
                    removeQuotes.append(")");
                }

            }
        }
        return removeQuotes.toString();
    }

    private String getNewOutputMenuName() {
        return "*New Output*" + " (" + curNodeConnector.getMenuName() + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    private void testRun1() {
        EConnectionType ct = EConnectionType.TABLE;
        String text = "TALEND.JOBS (Table)";
        int end = text.length() - 1;
        int start = text.lastIndexOf("(") + 1; //$NON-NLS-1$
        String tableName = text.substring(start, end);
        assertEquals("Table", tableName);
        IMetadataTable meta = node.getMetadataTable("JOBS");
        String connectionName = meta.getLabel();
        assertEquals(connectionName, "JOBS");
    }

    private void testRun2() {
        EConnectionType ct = EConnectionType.FLOW_MAIN;
        String text = "Main";
        String tableName = text;
        IMetadataTable meta = node.getMetadataTable("JOBS");
        String connectionName = meta.getTableName();
        assertEquals(connectionName, "JOBS");
    }

    private void testRun3() {
        EConnectionType ct = EConnectionType.TABLE;
        String text = "TALEND.JOBS (Table)";
        int end = text.lastIndexOf("(") - 1;//$NON-NLS-1$
        int start = 0;
        String connectionName = text.substring(start, end);
        assertEquals(connectionName, "TALEND.JOBS");// 3
        IMetadataTable meta = node.getMetadataList().get(0);
        meta.setAttachedConnector(curNodeConnector.getName());
    }

    private void testRun4() {
        EConnectionType ct = EConnectionType.TABLE;
        String connectionName = null;
        if (ct.hasConnectionCategory(IConnectionCategory.FLOW)) {
            connectionName = node.getProcess().generateUniqueConnectionName(Process.DEFAULT_ROW_CONNECTION_NAME);
            assertEquals(connectionName, "row1");// 61
        } else {
            connectionName = curNodeConnector.getLinkName();
        }
    }

    private void testRun5() {
        List<Object> listArgs = new ArrayList<Object>();
        IMetadataTable meta = node.getMetadataList().get(0);
        if (connecType.equals(EConnectionType.FLOW_MAIN) || connecType.equals(EConnectionType.FLOW_REF)
                || connecType.equals(EConnectionType.TABLE)) {
            if (meta == null) {
                listArgs.add(null);
            } else {
                listArgs.add(meta.getTableName());
                assertEquals(listArgs.size(), 1);
                assertEquals(listArgs.get(0).toString(), meta.getTableName());
            }
        } else {
            listArgs.add(node.getUniqueName());
        }

        String baseName = node.getConnectionName();
        assertEquals("row", baseName);
        String fromConnectionName = null;
        if (node.getProcess().checkValidConnectionName(baseName)) {
            fromConnectionName = node.getProcess().generateUniqueConnectionName(baseName);
            assertEquals("row1", fromConnectionName);
        }
        if (fromConnectionName != null && connecType.hasConnectionCategory(IConnectionCategory.FLOW)
                && node.getProcess().checkValidConnectionName(fromConnectionName, false) && !curNodeConnector.isMultiSchema()) {

            listArgs.add(fromConnectionName);
            assertEquals(listArgs.size(), 2);
            assertEquals(listArgs.get(1).toString(), "row1");
        }

    }

    @Test
    public void testValidationRuleEnableStatus() {
        IElementParameter validationParam = node.getElementParameter(EParameterName.VALIDATION_RULES.getName());
        validationParam.setValue(true);
        curNodeConnector = ValidationRulesUtil.createRejectConnector(node);
        curNodeConnector.setDefaultConnectionType(EConnectionType.FLOW_MAIN);
        connecType = curNodeConnector.getDefaultConnectionType();

        ConnectionCreateAction action = new ConnectionCreateAction(null, curNodeConnector);
        action.setSelectionProvider(new SelectionProvider(node));
        action.update();
        Assert.assertTrue(action.isEnabled());

        Property property = PropertiesFactory.eINSTANCE.createProperty();
        Process process = new Process(property);
        process.setComponentsType(ComponentCategory.CATEGORY_4_SPARK.getName());
        IComponent sourceCom = ComponentsFactoryProvider.getInstance().get("tMysqlInput",
                ComponentCategory.CATEGORY_4_SPARK.getName());

        node = new Node(sourceCom, process);
        validationParam = node.getElementParameter(EParameterName.VALIDATION_RULES.getName());
        validationParam.setValue(true);
        curNodeConnector = ValidationRulesUtil.createRejectConnector(node);
        curNodeConnector.setDefaultConnectionType(EConnectionType.FLOW_MAIN);
        connecType = curNodeConnector.getDefaultConnectionType();

        action = new ConnectionCreateAction(null, curNodeConnector);
        action.setSelectionProvider(new SelectionProvider(node));
        action.update();
        Assert.assertFalse(action.isEnabled());

    }

    class SelectionProvider implements ISelectionProvider {

        StructuredSelection selection;

        /**
         * DOC wchen ConnectionCreateActionTest.SelectionProvider constructor comment.
         */
        public SelectionProvider(INode node) {
            NodePart part = new NodePart();
            part.setModel(node);
            selection = new StructuredSelection(part);
        }

        /*
         * (non-Javadoc)
         *
         * @see org.eclipse.jface.viewers.ISelectionProvider#addSelectionChangedListener(org.eclipse.jface.viewers.
         * ISelectionChangedListener)
         */
        @Override
        public void addSelectionChangedListener(ISelectionChangedListener listener) {

        }

        /*
         * (non-Javadoc)
         *
         * @see org.eclipse.jface.viewers.ISelectionProvider#getSelection()
         */
        @Override
        public ISelection getSelection() {
            return selection;
        }

        /*
         * (non-Javadoc)
         *
         * @see org.eclipse.jface.viewers.ISelectionProvider#removeSelectionChangedListener(org.eclipse.jface.viewers.
         * ISelectionChangedListener)
         */
        @Override
        public void removeSelectionChangedListener(ISelectionChangedListener listener) {

        }

        /*
         * (non-Javadoc)
         *
         * @see org.eclipse.jface.viewers.ISelectionProvider#setSelection(org.eclipse.jface.viewers.ISelection)
         */
        @Override
        public void setSelection(ISelection selection) {

        }

    }

}
