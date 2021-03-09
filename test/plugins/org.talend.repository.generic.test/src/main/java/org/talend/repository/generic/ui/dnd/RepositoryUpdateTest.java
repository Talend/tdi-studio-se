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
package org.talend.repository.generic.ui.dnd;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.apache.avro.Schema;
import org.eclipse.core.runtime.Path;
import org.junit.Assert;
import org.junit.Test;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.runtime.model.components.IComponentConstants;
import org.talend.commons.utils.VersionUtils;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.salesforce.SalesforceConnectionProperties;
import org.talend.components.salesforce.SalesforceModuleProperties;
import org.talend.core.context.Context;
import org.talend.core.context.RepositoryContext;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.metadata.MetadataToolAvroHelper;
import org.talend.core.model.metadata.builder.ConvertionHelper;
import org.talend.core.model.metadata.builder.connection.ConnectionFactory;
import org.talend.core.model.metadata.builder.connection.MetadataColumn;
import org.talend.core.model.metadata.builder.connection.MetadataTable;
import org.talend.core.model.metadata.types.JavaTypesManager;
import org.talend.core.model.process.EComponentCategory;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.FakePropertyImpl;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.update.EUpdateItemType;
import org.talend.core.model.update.EUpdateResult;
import org.talend.core.model.update.UpdateResult;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.runtime.CoreRuntimePlugin;
import org.talend.core.ui.component.ComponentsFactoryProvider;
import org.talend.daikon.properties.presentation.Form;
import org.talend.designer.core.generic.utils.ComponentsUtils;
import org.talend.designer.core.generic.utils.SchemaUtils;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.model.components.ElementParameter;
import org.talend.designer.core.model.components.EmfComponent;
import org.talend.designer.core.ui.editor.cmd.ChangeValuesFromRepository;
import org.talend.designer.core.ui.editor.cmd.RepositoryChangeMetadataCommand;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.process.Process;
import org.talend.designer.core.ui.editor.update.cmd.UpdateNodeParameterCommand;
import org.talend.repository.generic.model.genericMetadata.GenericConnection;
import org.talend.repository.generic.model.genericMetadata.GenericConnectionItem;
import org.talend.repository.generic.model.genericMetadata.GenericMetadataFactory;
import org.talend.repository.generic.model.genericMetadata.SubContainer;
import org.talend.repository.generic.persistence.GenericRepository;

import orgomg.cwm.objectmodel.core.CoreFactory;
import orgomg.cwm.objectmodel.core.TaggedValue;

/**
 * created by nrousseau on Jun 23, 2016 Detailled comment
 *
 */
public class RepositoryUpdateTest {

    @Test
    public void testRepositoryChange() throws PersistenceException {
        String id = "testId"; //$NON-NLS-1$
        try {
            IComponent component = ComponentsFactoryProvider.getInstance().get("tSalesforceInput", "DI"); //$NON-NLS-1$ //$NON-NLS-2$
            Node node = new Node(component, new Process(new FakePropertyImpl()));

            GenericConnection connection = (GenericConnection) createBasicConnection(id).getConnection();
            setupPropertiesWithoutProxy(id);
            updateNode(id, node, connection);

            testRepositoryValue(node, "connection.userPassword.userId", "\"myUser\""); //$NON-NLS-1$  //$NON-NLS-2$
            testRepositoryValue(node, "connection.userPassword.password", "\"myPassword\""); //$NON-NLS-1$  //$NON-NLS-2$
            testRepositoryValue(node, "connection.proxy.useProxy", Boolean.FALSE); //$NON-NLS-1$
            testRepositoryValue(node, "connection.proxy.host", "\"\""); //$NON-NLS-1$  //$NON-NLS-2$
            testRepositoryValue(node, "connection.proxy.port", null); //$NON-NLS-1$
            testRepositoryValue(node, "connection.proxy.userPassword.userId", "\"\""); //$NON-NLS-1$  //$NON-NLS-2$
            testRepositoryValue(node, "connection.proxy.userPassword.password", "\"\""); //$NON-NLS-1$  //$NON-NLS-2$

            setupPropertiesWithProxy(id);
            updateNode(id, node, connection);

            testRepositoryValue(node, "connection.userPassword.userId", "\"myUser\""); //$NON-NLS-1$  //$NON-NLS-2$
            testRepositoryValue(node, "connection.userPassword.password", "\"myPassword\""); //$NON-NLS-1$  //$NON-NLS-2$
            testRepositoryValue(node, "connection.proxy.useProxy", Boolean.TRUE); //$NON-NLS-1$
            testRepositoryValue(node, "connection.proxy.host", "\"host\""); //$NON-NLS-1$  //$NON-NLS-2$
            testRepositoryValue(node, "connection.proxy.port", 1234); //$NON-NLS-1$
            testRepositoryValue(node, "connection.proxy.userPassword.userId", "\"proxyUser\""); //$NON-NLS-1$  //$NON-NLS-2$
            testRepositoryValue(node, "connection.proxy.userPassword.password", "\"proxyPassword\""); //$NON-NLS-1$  //$NON-NLS-2$

        } finally {
            IRepositoryViewObject object = ProxyRepositoryFactory.getInstance().getLastVersion(id);
            if (object != null) {
                ProxyRepositoryFactory.getInstance().deleteObjectPhysical(object);
            }
        }
    }

    @Test
    public void testRepositoryResetParams() throws PersistenceException {
        String id = "testId"; //$NON-NLS-1$
        try {
            IComponent component = ComponentsFactoryProvider.getInstance().get("tSalesforceInput", "DI"); //$NON-NLS-1$ //$NON-NLS-2$
            Node node = new Node(component, new Process(new FakePropertyImpl()));

            GenericConnection connection = (GenericConnection) createBasicConnection(id).getConnection();
            setupPropertiesWithoutProxy(id);
            updateNode(id, node, connection);

            testRepositoryValue(node, "connection.userPassword.userId", "\"myUser\""); //$NON-NLS-1$  //$NON-NLS-2$

            IElementParameter param = node.getElementParameter("connection.userPassword.userId");
            assertTrue(param.isRepositoryValueUsed());
            assertNotNull(param.getRepositoryValue());
            Form form = node.getComponentProperties().getForm(Form.MAIN);

            List<ElementParameter> parameters = new ArrayList<>();
            parameters = ComponentsUtils.getParametersFromForm(node, false, EComponentCategory.BASIC, node.getComponentProperties(),
                    form);

            node.setElementParameters(parameters);
            param = node.getElementParameter("connection.userPassword.userId");
            assertTrue(param.isRepositoryValueUsed());
            assertNotNull(param.getRepositoryValue());

        } finally {
            IRepositoryViewObject object = ProxyRepositoryFactory.getInstance().getLastVersion(id);
            if (object != null) {
                ProxyRepositoryFactory.getInstance().deleteObjectPhysical(object);
            }
        }
    }

    @Test
    public void testRepositoryChangeFromConnection() throws PersistenceException {
        String id = "testId"; //$NON-NLS-1$
        try {
            IComponent component = ComponentsFactoryProvider.getInstance().get("tSalesforceInput", "DI"); //$NON-NLS-1$ //$NON-NLS-2$
            Node node = new Node(component, new Process(new FakePropertyImpl()));

            GenericConnection connection = (GenericConnection) createBasicConnection(id).getConnection();
            setupPropertiesWithoutProxy(id);
            updateNodeConnectionOnly(id, node, connection);

            testRepositoryValue(node, "connection.userPassword.userId", "\"myUser\""); //$NON-NLS-1$  //$NON-NLS-2$
            testRepositoryValue(node, "connection.userPassword.password", "\"myPassword\""); //$NON-NLS-1$  //$NON-NLS-2$
            testRepositoryValue(node, "connection.proxy.useProxy", Boolean.FALSE); //$NON-NLS-1$
            testRepositoryValue(node, "connection.proxy.host", "\"\""); //$NON-NLS-1$  //$NON-NLS-2$
            testRepositoryValue(node, "connection.proxy.port", null); //$NON-NLS-1$
            testRepositoryValue(node, "connection.proxy.userPassword.userId", "\"\""); //$NON-NLS-1$  //$NON-NLS-2$
            testRepositoryValue(node, "connection.proxy.userPassword.password", "\"\""); //$NON-NLS-1$  //$NON-NLS-2$

            setupPropertiesWithProxy(id);
            updateNodeConnectionOnly(id, node, connection);

            testRepositoryValue(node, "connection.userPassword.userId", "\"myUser\""); //$NON-NLS-1$  //$NON-NLS-2$
            testRepositoryValue(node, "connection.userPassword.password", "\"myPassword\""); //$NON-NLS-1$  //$NON-NLS-2$
            testRepositoryValue(node, "connection.proxy.useProxy", Boolean.TRUE); //$NON-NLS-1$
            testRepositoryValue(node, "connection.proxy.host", "\"host\""); //$NON-NLS-1$  //$NON-NLS-2$
            testRepositoryValue(node, "connection.proxy.port", 1234); //$NON-NLS-1$
            testRepositoryValue(node, "connection.proxy.userPassword.userId", "\"proxyUser\""); //$NON-NLS-1$  //$NON-NLS-2$
            testRepositoryValue(node, "connection.proxy.userPassword.password", "\"proxyPassword\""); //$NON-NLS-1$  //$NON-NLS-2$

        } finally {
            IRepositoryViewObject object = ProxyRepositoryFactory.getInstance().getLastVersion(id);
            if (object != null) {
                ProxyRepositoryFactory.getInstance().deleteObjectPhysical(object);
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testRepositoryUpdate() throws PersistenceException {
        String id = "testId"; //$NON-NLS-1$
        try {
            IComponent component = ComponentsFactoryProvider.getInstance().get("tSalesforceInput", "DI"); //$NON-NLS-1$ //$NON-NLS-2$
            Process process = new Process(new FakePropertyImpl());
            Node node = new Node(component, process);
            ((List<INode>) process.getGraphicalNodes()).add(node);

            GenericConnectionItem connectionItem = createBasicConnection(id);
            setupPropertiesWithoutProxy(id);
            prepareTableForTest(id);

            // get updatedItem
            IRepositoryViewObject object = ProxyRepositoryFactory.getInstance().getLastVersion(id);
            connectionItem = (GenericConnectionItem) object.getProperty().getItem();
            Assert.assertEquals(1, SchemaUtils.getMetadataTables(connectionItem.getConnection(), SubContainer.class).size());

            updateNode(id, node, (GenericConnection) connectionItem.getConnection());

            setupPropertiesWithProxy(id);
            prepareTableForTest(id);

            launchRepositoryUpdateOnNode(id, process, node);

            testRepositoryValue(node, "connection.userPassword.userId", "\"myUser\""); //$NON-NLS-1$  //$NON-NLS-2$
            testRepositoryValue(node, "connection.userPassword.password", "\"myPassword\""); //$NON-NLS-1$  //$NON-NLS-2$
            testRepositoryValue(node, "connection.proxy.useProxy", Boolean.TRUE); //$NON-NLS-1$
            testRepositoryValue(node, "connection.proxy.host", "\"host\""); //$NON-NLS-1$  //$NON-NLS-2$
            testRepositoryValue(node, "connection.proxy.port", 1234); //$NON-NLS-1$
            testRepositoryValue(node, "connection.proxy.userPassword.userId", "\"proxyUser\""); //$NON-NLS-1$  //$NON-NLS-2$
            testRepositoryValue(node, "connection.proxy.userPassword.password", "\"proxyPassword\""); //$NON-NLS-1$  //$NON-NLS-2$

            setupPropertiesWithoutProxy(id);
            prepareTableForTest(id);
            launchRepositoryUpdateOnNode(id, process, node);

            testRepositoryValue(node, "connection.userPassword.userId", "\"myUser\""); //$NON-NLS-1$  //$NON-NLS-2$
            testRepositoryValue(node, "connection.userPassword.password", "\"myPassword\""); //$NON-NLS-1$  //$NON-NLS-2$
            testRepositoryValue(node, "connection.proxy.useProxy", Boolean.FALSE); //$NON-NLS-1$
            testRepositoryValue(node, "connection.proxy.host", "\"\""); //$NON-NLS-1$  //$NON-NLS-2$
            testRepositoryValue(node, "connection.proxy.port", null); //$NON-NLS-1$
            testRepositoryValue(node, "connection.proxy.userPassword.userId", "\"\""); //$NON-NLS-1$  //$NON-NLS-2$
            testRepositoryValue(node, "connection.proxy.userPassword.password", "\"\""); //$NON-NLS-1$  //$NON-NLS-2$


            setupPropertiesWithProxyEmptyVars(id);
            prepareTableForTest(id);

            launchRepositoryUpdateOnNode(id, process, node);

            testRepositoryValue(node, "connection.userPassword.userId", "\"myUser\""); //$NON-NLS-1$  //$NON-NLS-2$
            testRepositoryValue(node, "connection.userPassword.password", "\"myPassword\""); //$NON-NLS-1$  //$NON-NLS-2$
            testRepositoryValue(node, "connection.proxy.useProxy", Boolean.TRUE); //$NON-NLS-1$
            testRepositoryValue(node, "connection.proxy.host", "\"\""); //$NON-NLS-1$  //$NON-NLS-2$
            testRepositoryValue(node, "connection.proxy.port", null); //$NON-NLS-1$
            testRepositoryValue(node, "connection.proxy.userPassword.userId", "\"\""); //$NON-NLS-1$  //$NON-NLS-2$
            testRepositoryValue(node, "connection.proxy.userPassword.password", "\"\""); //$NON-NLS-1$  //$NON-NLS-2$

        } finally {
            IRepositoryViewObject object = ProxyRepositoryFactory.getInstance().getLastVersion(id);
            if (object != null) {
                ProxyRepositoryFactory.getInstance().deleteObjectPhysical(object);
            }
        }
    }

    /**
     * DOC nrousseau Comment method "updateNode".
     * @param id
     * @param process
     * @param node
     * @throws PersistenceException
     */
    private void launchRepositoryUpdateOnNode(String id, Process process, Node node) throws PersistenceException {
        GenericConnectionItem connectionItem;
        IRepositoryViewObject object;
        object = ProxyRepositoryFactory.getInstance().getLastVersion(id);
        connectionItem = (GenericConnectionItem) object.getProperty().getItem();
        Assert.assertEquals(1, SchemaUtils.getMetadataTables(connectionItem.getConnection(), SubContainer.class).size());

        UpdateResult ur = createUpdateResult(node);
        ur.setResult(EUpdateItemType.NODE_PROPERTY, EUpdateResult.UPDATE, connectionItem);
        ur.setJob(process);
        UpdateNodeParameterCommand cmd = new UpdateNodeParameterCommand(ur);
        cmd.execute();
    }

    private void testRepositoryValue(INode node, String parameter, Object expectedValue) {
        IElementParameter param = node.getElementParameter(parameter);
        assertTrue(param.isRepositoryValueUsed());
        assertEquals(expectedValue, param.getValue());
    }

    private UpdateResult createUpdateResult(Node node) {
        UpdateResult ur = new UpdateResult(node) {

            @Override
            public String getJobInfor() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public String getName() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public String getCategory() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            protected void updateJobInfor() {
                // TODO Auto-generated method stub

            }

        };
        return ur;
    }

    private void updateNode(String id, Node node, GenericConnection connection) {
        MetadataTable table = prepareTableForTest(id);
        updateNodeConnectionOnly(id, node, connection);
        IElementParameter schemaParam = node.getElementParameterFromField(EParameterFieldType.SCHEMA_REFERENCE);

        String value = id + " - " + table.getLabel(); //$NON-NLS-1$
        RepositoryChangeMetadataCommand command2 = new RepositoryChangeMetadataCommand(node, schemaParam.getName() + ":" //$NON-NLS-1$
                + EParameterName.REPOSITORY_SCHEMA_TYPE.getName(), value, ConvertionHelper.convert(table), null, connection);

        command2.execute(true);
    }


    private void updateNodeConnectionOnly(String id, Node node, GenericConnection connection) {
        IElementParameter propertyParam = node.getElementParameterFromField(EParameterFieldType.PROPERTY_TYPE);
        propertyParam.getChildParameters().get(EParameterName.PROPERTY_TYPE.getName()).setValue(EmfComponent.REPOSITORY);
        propertyParam.getChildParameters().get(EParameterName.REPOSITORY_PROPERTY_TYPE.getName()).setValue(id);
        ChangeValuesFromRepository command = new ChangeValuesFromRepository(node, connection, null, propertyParam.getName()
                + ":" + EParameterName.REPOSITORY_PROPERTY_TYPE.getName(), id, true); //$NON-NLS-1$

        command.execute(true);
    }

    /**
     * DOC nrousseau Comment method "createBasicConnection".
     *
     * @param id
     * @return
     * @throws PersistenceException
     */
    private GenericConnectionItem createBasicConnection(String id) throws PersistenceException {
        GenericConnection connection = GenericMetadataFactory.eINSTANCE.createGenericConnection();

        Property connectionProperty = PropertiesFactory.eINSTANCE.createProperty();
        // ses the id to be used for persistence lookup
        connectionProperty.setId(id);
        connectionProperty.setAuthor(((RepositoryContext) CoreRuntimePlugin.getInstance().getContext()
                .getProperty(Context.REPOSITORY_CONTEXT_KEY)).getUser());
        connectionProperty.setVersion(VersionUtils.DEFAULT_VERSION);
        connectionProperty.setStatusCode(""); //$NON-NLS-1$
        connectionProperty.setLabel("test");
        GenericConnectionItem connectionItem = GenericMetadataFactory.eINSTANCE.createGenericConnectionItem();

        connectionItem.setProperty(connectionProperty);
        connectionItem.setConnection(connection);
        connectionItem.setTypeName("salesforce");
        ProxyRepositoryFactory.getInstance().create(connectionItem, new Path(""));
        return connectionItem;
    }

    private void setupPropertiesWithoutProxy(String id) {
        SalesforceConnectionProperties sfp = new SalesforceConnectionProperties("test");
        sfp.init();
        sfp.loginType.setValue(SalesforceConnectionProperties.LoginType.Basic);
        sfp.userPassword.userId.setStoredValue("myUser");
        sfp.userPassword.password.setStoredValue("myPassword");
        GenericRepository gr = new GenericRepository();
        gr.storeProperties(sfp, "test", id, null);
    }

    private void setupPropertiesWithProxy(String id) {
        SalesforceConnectionProperties sfp = new SalesforceConnectionProperties("test");
        sfp.init();
        sfp.loginType.setValue(SalesforceConnectionProperties.LoginType.Basic);
        sfp.userPassword.userId.setStoredValue("myUser");
        sfp.userPassword.password.setStoredValue("myPassword");
        sfp.proxy.useProxy.setStoredValue(Boolean.TRUE);
        sfp.proxy.host.setStoredValue("host");
        sfp.proxy.port.setStoredValue(1234);
        sfp.proxy.userPassword.userId.setStoredValue("proxyUser");
        sfp.proxy.userPassword.password.setStoredValue("proxyPassword");
        GenericRepository gr = new GenericRepository();
        gr.storeProperties(sfp, "test", id, null);
    }

    private void setupPropertiesWithProxyEmptyVars(String id) {
        SalesforceConnectionProperties sfp = new SalesforceConnectionProperties("test");
        sfp.init();
        sfp.loginType.setValue(SalesforceConnectionProperties.LoginType.Basic);
        sfp.userPassword.userId.setStoredValue("myUser");
        sfp.userPassword.password.setStoredValue("myPassword");
        sfp.proxy.useProxy.setStoredValue(Boolean.TRUE);
        sfp.proxy.host.setStoredValue(null);
        sfp.proxy.port.setStoredValue(null);
        sfp.proxy.userPassword.userId.setStoredValue(null);
        sfp.proxy.userPassword.password.setStoredValue(null);
        GenericRepository gr = new GenericRepository();
        gr.storeProperties(sfp, "test", id, null);
    }

    private MetadataTable prepareTableForTest(String id) {
        MetadataTable table = ConnectionFactory.eINSTANCE.createMetadataTable();
        table.setLabel("myTableTest");
        MetadataColumn col1 = ConnectionFactory.eINSTANCE.createMetadataColumn();
        col1.setLabel("myColumn1");
        col1.setTalendType(JavaTypesManager.STRING.getId());
        col1.setLength(25);
        table.getColumns().add(col1);

        Schema avroSchema = MetadataToolAvroHelper.convertToAvro(table);
        TaggedValue tv = CoreFactory.eINSTANCE.createTaggedValue();
        tv.setTag(IComponentConstants.COMPONENT_PROPERTIES_TAG);

        SalesforceModuleProperties smp = new SalesforceModuleProperties("test");
        smp.connection.init();
        smp.connection.userPassword.userId.setValue("old"); // because we should get connection always from the main
                                                            // SalesforceConnectionProperties
        smp.moduleName.setStoredValue("myModule");
        smp.main.schema.setStoredValue(avroSchema);

        GenericRepository gr = new GenericRepository();
        gr.storeProperties(smp, table.getLabel(), id + "#", "main.schema");
        return table;
    }

}
