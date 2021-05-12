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
package org.talend.designer.core.ui.editor.jobletcontainer;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Path;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.image.ImageUtils;
import org.talend.commons.utils.VersionUtils;
import org.talend.components.api.properties.ComponentReferenceProperties;
import org.talend.core.CorePlugin;
import org.talend.core.context.Context;
import org.talend.core.context.RepositoryContext;
import org.talend.core.model.components.ComponentCategory;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.components.IComponentsFactory;
import org.talend.core.model.process.EConnectionType;
import org.talend.core.model.properties.ByteArray;
import org.talend.core.model.properties.JobletProcessItem;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.FakePropertyImpl;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.repository.RepositoryObject;
import org.talend.core.model.routines.RoutinesUtil;
import org.talend.core.repository.ui.view.RepositoryLabelProvider;
import org.talend.core.ui.component.ComponentsFactoryProvider;
import org.talend.daikon.properties.Properties;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.model.process.AbstractProcessProvider;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.ParametersType;
import org.talend.designer.core.model.utils.emf.talendfile.RoutinesParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.TalendFileFactory;
import org.talend.designer.core.ui.editor.connections.Connection;
import org.talend.designer.core.ui.editor.nodecontainer.NodeContainer;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.process.Process;
import org.talend.designer.joblet.model.JobletFactory;
import org.talend.designer.joblet.model.JobletProcess;
import org.talend.repository.model.IProxyRepositoryFactory;

/**
 * created by wchen on Jul 27, 2017 Detailled comment
 *
 */
public class JobletUtilTest {

    IProxyRepositoryFactory factory = CorePlugin.getDefault().getRepositoryService().getProxyRepositoryFactory();

    IComponentsFactory components = ComponentsFactoryProvider.getInstance();

    List<IRepositoryViewObject> repositoryObjects;

    @Before
    public void setUp() throws Exception {
        repositoryObjects = new ArrayList<IRepositoryViewObject>();
    }

    @After
    public void tearDown() throws Exception {
        for (IRepositoryViewObject repositoryObject : repositoryObjects) {
            factory.deleteObjectPhysical(repositoryObject);
            IComponent jobletComponent = components.get(repositoryObject.getLabel(), ComponentCategory.CATEGORY_4_DI.getName());
            components.getComponents().remove(jobletComponent);
        }
        AbstractProcessProvider.loadComponentsFromProviders();
    }

    @Test
    public void testReloadJobletInCurrentProcess() throws PersistenceException {
        String label = "testReloadJobletInCurrentProcess";
        String id = factory.getNextId();
        createRepositoryObject(label, id, VersionUtils.DEFAULT_VERSION);
        AbstractProcessProvider.loadComponentsFromProviders();
        IComponent jobleComponent = components.get(label, ComponentCategory.CATEGORY_4_DI.getName());
        IComponent tMsgComponent = components.get("tMsgBox", ComponentCategory.CATEGORY_4_DI.getName());

        Property porperty = new FakePropertyImpl();
        Process currentProcess = new Process(porperty);
        Node jobletNode = new Node(jobleComponent, currentProcess);
        currentProcess.addNodeContainer(new NodeContainer(jobletNode));

        Process jobletProcess = (Process) jobletNode.getComponent().getProcess();
        Node node = new Node(tMsgComponent, jobletProcess);
        jobletProcess.addNodeContainer(new NodeContainer(node));

        Assert.assertEquals(jobletProcess.getGraphicalNodes().size(), 1);

        JobletUtil jUtil = new JobletUtil();
        jUtil.reloadJobletInCurrentProcess(currentProcess);

        Process jobletProcessAfterReload = (Process) jobletNode.getComponent().getProcess();
        Assert.assertFalse(jobletProcessAfterReload == jobletProcess);
        Assert.assertEquals(jobletProcessAfterReload.getGraphicalNodes().size(), 0);

    }

    @Test
    public void testGetAndUpdateReferenceComponentInJoblet() {
        Property porperty = new FakePropertyImpl();
        Process currentProcess = new Process(porperty);
        JobletUtil jobletutil = new JobletUtil();
        // tJDBCInput
        IComponent tJDBCInput = components.get("tJDBCInput", ComponentCategory.CATEGORY_4_DI.getName());
        Assert.assertNotNull("component tJDBCInput not load", tJDBCInput);
        Node node = new Node(tJDBCInput, currentProcess);
        Node clonedNode = new Node(node.getComponent(), currentProcess, node.getUniqueName());
        Properties nodeReference = jobletutil.findOutReferencedComponentProperties(node.getComponentProperties());
        Properties cloneNodeReference = jobletutil.findOutReferencedComponentProperties(clonedNode.getComponentProperties());
        Assert.assertNotNull(nodeReference);

        getReferenceCompProperty(nodeReference, "referenceType")
                .setStoredValue(ComponentReferenceProperties.ReferenceType.COMPONENT_INSTANCE);
        jobletutil.updateReferenceComponentInJoblet(node, clonedNode);
        Assert.assertEquals(getReferenceCompProperty(nodeReference, "referenceType").getValue(),
                getReferenceCompProperty(cloneNodeReference, "referenceType").getValue());

        // tSnowflakeInput
        IComponent tSnowflakeInput = components.get("tSnowflakeInput", ComponentCategory.CATEGORY_4_DI.getName());
        node = new Node(tSnowflakeInput, currentProcess);
        clonedNode = new Node(node.getComponent(), currentProcess, node.getUniqueName());
        nodeReference = jobletutil.findOutReferencedComponentProperties(node.getComponentProperties());
        cloneNodeReference = jobletutil.findOutReferencedComponentProperties(clonedNode.getComponentProperties());
        Assert.assertNotNull(nodeReference);
        getReferenceCompProperty(nodeReference, "referenceType")
                .setStoredValue(ComponentReferenceProperties.ReferenceType.COMPONENT_INSTANCE);
        jobletutil.updateReferenceComponentInJoblet(node, clonedNode);
        Assert.assertEquals(getReferenceCompProperty(nodeReference, "referenceType").getValue(),
                getReferenceCompProperty(cloneNodeReference, "referenceType").getValue());

        // normal component tJava should no exception
        IComponent tjava = components.get("tJava", ComponentCategory.CATEGORY_4_DI.getName());
        node = new Node(tSnowflakeInput, currentProcess);
        clonedNode = new Node(node.getComponent(), currentProcess, node.getUniqueName());
        jobletutil.updateReferenceComponentInJoblet(node, clonedNode);

    }

    private org.talend.daikon.properties.property.Property<?> getReferenceCompProperty(Properties properties,
            String propertyName) {
        return (org.talend.daikon.properties.property.Property<?>) properties.getProperty(propertyName);
    }

    private IRepositoryViewObject createRepositoryObject(String label, String id, String version) throws PersistenceException {
        Property property = PropertiesFactory.eINSTANCE.createProperty();
        property.setAuthor(((RepositoryContext) CorePlugin.getContext().getProperty(Context.REPOSITORY_CONTEXT_KEY)).getUser());
        property.setVersion(version);
        property.setStatusCode(""); //$NON-NLS-1$

        JobletProcessItem processItem = PropertiesFactory.eINSTANCE.createJobletProcessItem();
        ByteArray ba = PropertiesFactory.eINSTANCE.createByteArray();
        processItem.setIcon(ba);
        processItem.getIcon().setInnerContent(
                ImageUtils.saveImageToData(RepositoryLabelProvider.getDefaultJobletImage(processItem)));

        processItem.setProperty(property);
        property.setId(id);
        property.setLabel(label);
        property.setDisplayName(property.getLabel());
        ParametersType parameterType = TalendFileFactory.eINSTANCE.createParametersType();
        // add depended routines.
        List<RoutinesParameterType> dependenciesInPreference;
        dependenciesInPreference = RoutinesUtil.createDependenciesInPreference();

        parameterType.getRoutinesParameter().addAll(dependenciesInPreference);
        JobletProcess process = JobletFactory.eINSTANCE.createJobletProcess();
        process.setParameters(parameterType);
        processItem.setJobletProcess(process);
        factory.create(processItem, new Path(""));
        return new RepositoryObject(property);
    }
    
    @Test
    public void testCloneConnection() throws PersistenceException {
        String label = "testCloneConnection";
        String id = factory.getNextId();
        createRepositoryObject(label, id, VersionUtils.DEFAULT_VERSION);
        AbstractProcessProvider.loadComponentsFromProviders();
        IComponent jobleComponent = components.get(label, ComponentCategory.CATEGORY_4_DI.getName());
        IComponent tMsgComponent1 = components.get("tMsgBox", ComponentCategory.CATEGORY_4_DI.getName());
        IComponent tMsgComponent2 = components.get("tMsgBox", ComponentCategory.CATEGORY_4_DI.getName());

        Property porperty = new FakePropertyImpl();
        Process currentProcess = new Process(porperty);
        Node jobletNode = new Node(jobleComponent, currentProcess);
        currentProcess.addNodeContainer(new NodeContainer(jobletNode));

        Process jobletProcess = (Process) jobletNode.getComponent().getProcess();
        Node node1 = new Node(tMsgComponent1, jobletProcess);
        Node node2 = new Node(tMsgComponent2, jobletProcess);
        Connection conn = new Connection(node1, node2, EConnectionType.ON_SUBJOB_OK,
                EConnectionType.ON_SUBJOB_OK.getName(), "OnSubjobOk", "OnSubjobOk", "OnSubjobOk", false);
        conn.setPropertyValue(EParameterName.UNIQUE_NAME.name(), "OnSubjobOk_new");
        jobletProcess.addNodeContainer(new NodeContainer(node1));
        jobletProcess.addNodeContainer(new NodeContainer(node2));
        
        JobletUtil jobletutil = new JobletUtil();
        Connection cloneConn = jobletutil.cloneConnection(conn, node1, node2);
        Assert.assertEquals(cloneConn.getUniqueName(), "OnSubjobOk_new");
    }

}
