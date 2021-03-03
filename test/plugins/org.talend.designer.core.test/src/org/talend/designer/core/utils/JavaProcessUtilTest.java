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
package org.talend.designer.core.utils;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.talend.core.model.components.ComponentCategory;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.general.ModuleNeeded;
import org.talend.core.model.process.EComponentCategory;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.IProcess2;
import org.talend.core.runtime.maven.MavenUrlHelper;
import org.talend.core.runtime.process.TalendProcessOptionConstants;
import org.talend.designer.core.model.components.DummyComponent;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.model.components.ElementParameter;
import org.talend.designer.core.model.components.EmfComponent;
import org.talend.designer.core.model.process.DataNode;
import org.talend.designer.core.test.util.NodeTestCreator;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.process.Process;

/**
 * created by nrousseau on Jun 8, 2016 Detailled comment
 *
 */
public class JavaProcessUtilTest {

    @Test
    public void testFindMoreLibraries() {
        // ensure the CAMEL is not added as jar dependency
        DummyComponent comp = mock(DummyComponent.class);
        when(comp.getType()).thenReturn(ComponentCategory.CATEGORY_4_CAMEL.getName());
        INode node = new DataNode(comp, ""); //$NON-NLS-1$
        IElementParameter param = new ElementParameter(node);
        param.setName("HOTLIBS");
        List<Map<String, Object>> table = new ArrayList<Map<String, Object>>();
        Map<String, Object> line = new HashMap<String, Object>();
        line.put("LIBPATH", "CAMEL");
        table.add(line);
        param.setValue(table);
        List<ModuleNeeded> modulesNeeded = new ArrayList<ModuleNeeded>();
        JavaProcessUtil.findMoreLibraries(null, modulesNeeded, param);
        assertTrue(modulesNeeded.isEmpty());

        table = new ArrayList<Map<String, Object>>();
        line = new HashMap<String, Object>();
        line.put("LIBPATH", "\"camel.jar\"");
        table.add(line);
        param.setValue(table);
        modulesNeeded = new ArrayList<ModuleNeeded>();
        JavaProcessUtil.findMoreLibraries(null, modulesNeeded, param);
        assertEquals(1, modulesNeeded.size());
        assertEquals("camel.jar", modulesNeeded.get(0).getModuleName());
    }

    @Test
    public void testGetChildrenModules_noChildren() {
        INode node = mock(INode.class);
        IComponent component = mock(IComponent.class);
        when(node.getComponent()).thenReturn(component);
        when(component.getName()).thenReturn("tLogRow");

        List<ModuleNeeded> childrenModules = JavaProcessUtil.getChildrenModules(node, Collections.emptySet(), TalendProcessOptionConstants.MODULES_DEFAULT);
        assertTrue(childrenModules.isEmpty());
    }

    // @Test
    public void testGetChildrenModules_withChildren() {
        fail("Not impl yet!");
    }

    // @Test
    public void testGetChildrenModules_independent() {
        fail("Not impl yet!");
    }

    // @Test
    public void testGetChildrenModules_dynamicJob() {
        fail("Not impl yet!");
    }

    @Test
    public void testGetHadoopClusterItemId() {
        Process process = new Process(TestUtils.createDefaultProperty());
        Node simpleInputNode = NodeTestCreator.createSimpleInputNode(process);

        // If use existing connection, then return null.
        IElementParameter uecParam = createUECParameter(simpleInputNode);
        uecParam.setValue(true);
        simpleInputNode.addElementParameter(uecParam);
        assertNull(JavaProcessUtil.getHadoopClusterItemId(simpleInputNode));

        // Built in mode
        assertNull(JavaProcessUtil.getHadoopClusterItemId(simpleInputNode));

        // Repository mode but repository value is null
        IElementParameter propertyElementParameter = simpleInputNode
                .getElementParameterFromField((EParameterFieldType.PROPERTY_TYPE));
        propertyElementParameter.getChildParameters().get(EParameterName.PROPERTY_TYPE.getName())
                .setValue(EmfComponent.REPOSITORY);
        propertyElementParameter.getChildParameters().get(EParameterName.REPOSITORY_PROPERTY_TYPE.getName()).setValue(null);
        assertNull(JavaProcessUtil.getHadoopClusterItemId(simpleInputNode));
    }

    @Test
    public void testIsUseExistingConnection() {
        Process process = new Process(TestUtils.createDefaultProperty());
        Node simpleInputNode = NodeTestCreator.createSimpleInputNode(process);

        assertFalse(JavaProcessUtil.isUseExistingConnection(simpleInputNode));

        IElementParameter uecParam = createUECParameter(simpleInputNode);
        simpleInputNode.addElementParameter(uecParam);
        assertFalse(JavaProcessUtil.isUseExistingConnection(simpleInputNode));

        simpleInputNode.setPropertyValue(EParameterName.USE_EXISTING_CONNECTION.getName(), Boolean.TRUE);
        assertTrue(JavaProcessUtil.isUseExistingConnection(simpleInputNode));
    }

    @Test
    public void testEvaluateOsgiDependency() {
    	ModuleNeeded module = moduleWithEvalOsgiDependency("tester-1.1.1.jar", null);
    	assertEquals("true", module.getExtraAttributes().get("IS_OSGI_EXCLUDED"));
    	module = moduleWithEvalOsgiDependency("tester-1.1.1.jar", "tester-2.2.2.jar,tester-1.1.1.jar");
    	assertNull(module.getExtraAttributes().get("IS_OSGI_EXCLUDED"));
    	module = moduleWithEvalOsgiDependency("tester-1.1.1.jar", "tester-3.3.3.jar,tester-2.2.2.jar");
    	assertEquals("true", module.getExtraAttributes().get("IS_OSGI_EXCLUDED"));
    }

    private IElementParameter createUECParameter(INode node) {
        ElementParameter param = new ElementParameter(node);
        param.setName(EParameterName.USE_EXISTING_CONNECTION.getName());
        param.setDisplayName(EParameterName.USE_EXISTING_CONNECTION.getDisplayName());
        param.setFieldType(EParameterFieldType.CHECK);
        param.setCategory(EComponentCategory.BASIC);
        return param;
    }

    private ModuleNeeded moduleWithEvalOsgiDependency(String libName, String bundleClassPath) {
    	DataNode node = new DataNode();
    	List<ModuleNeeded> modulesNeeded = new ArrayList<ModuleNeeded>();
    	IProcess2 process = mock(IProcess2.class);
    	when(process.getComponentsType()).thenReturn("CAMEL");
    	if (bundleClassPath != null && bundleClassPath.length() > 0) {
	    	Map<Object, Object> additionalProperties = new HashMap<Object, Object>();
	    	additionalProperties.put("Bundle-ClassPath", bundleClassPath);
	    	when(process.getAdditionalProperties()).thenReturn(additionalProperties);
    	}
    	node.setProcess(process);
    	node.setActivate(true);
    	List<IElementParameter> elementParameters = new ArrayList<IElementParameter>();
    	IElementParameter param = mock(IElementParameter.class);
    	elementParameters.add(param);
    	when(param.getFieldType()).thenReturn(EParameterFieldType.MODULE_LIST);
    	when(param.isShow(elementParameters)).thenReturn(Boolean.TRUE);
    	when(param.getValue()).thenReturn(libName);
    	when(param.getName()).thenReturn("LIBRARY");
    	node.setElementParameters(elementParameters);
    	node.setUniqueName("cMessagingEndpoint_1");
    	JavaProcessUtil.addNodeRelatedModules(process, modulesNeeded, node, 0);
    	assertEquals(1, modulesNeeded.size());
    	return modulesNeeded.get(0);
    }

    @Test
    public void testDescendingOrderModuleList() {
        String[] moduleNames = new String[] { "slf4j-api-1.5.11.jar", "slf4j-api-1.7.5.jar", "slf4j-api-1.8.0-beta1.jar",
                "slf4j-api-1.7.25.jar" };
        Set<ModuleNeeded> moduleNeededSet = prepareModuleNeededSet(moduleNames);
        List<ModuleNeeded> descendingOrderModuleList = JavaProcessUtil.descendingOrderModuleList(moduleNeededSet);
        assertEquals(4, descendingOrderModuleList.size());
        assertEquals(moduleNames[2], descendingOrderModuleList.get(0).getModuleName());
        assertEquals(moduleNames[3], descendingOrderModuleList.get(1).getModuleName());
        assertEquals(moduleNames[1], descendingOrderModuleList.get(2).getModuleName());
        assertEquals(moduleNames[0], descendingOrderModuleList.get(3).getModuleName());

        moduleNames = new String[] { "auto-common-0.8.jar", "auto-common-0.3.jar", "auto-common-0.10.jar" };
        moduleNeededSet = prepareModuleNeededSet(moduleNames);
        descendingOrderModuleList = JavaProcessUtil.descendingOrderModuleList(moduleNeededSet);
        assertEquals(3, descendingOrderModuleList.size());
        assertEquals(moduleNames[2], descendingOrderModuleList.get(0).getModuleName());
        assertEquals(moduleNames[0], descendingOrderModuleList.get(1).getModuleName());
        assertEquals(moduleNames[1], descendingOrderModuleList.get(2).getModuleName());
    }

    private Set<ModuleNeeded> prepareModuleNeededSet(String[] moduleNames) {
        String fakeNodeName = "tJDBCConnection";
        Set<ModuleNeeded> moduleNeededSet = new HashSet<ModuleNeeded>();
        for (String moduleName : moduleNames) {
            ModuleNeeded module = new ModuleNeeded(fakeNodeName, moduleName, null, false);
            moduleNeededSet.add(module);
        }
        return moduleNeededSet;
    }

    @Test
    public void testGetCoordinate() {
        String context = "tHiveConnection";
        String informationMsg = "Required for using this component.";
        boolean required = false;
        String[] mvnUris = { "mvn:org.talend.libraries/httpcore-4.4.6/6.3.0/jar", "mvn:org.apache.httpcomponents/httpcore/4.4.6",
                "mvn:https://studio-dl-client:enc:system.encryption.key.v1:xgJc9IaLeJSt6UpaabOusZUBcK4bLUJipmAhuD6dHI8fBqoB7pm4UDWlWLk=@talend-update.talend.com/nexus/content/groups/dynamicdistribution/!org.apache.httpcomponents/httpcore/4.4.6/jar" };
        String[] expectedCoordinates = { "org.talend.libraries:httpcore-4.4.6:jar:6.3.0",
                "org.apache.httpcomponents:httpcore:jar:4.4.6", "org.apache.httpcomponents:httpcore:jar:4.4.6" };
        for (int i = 0; i < mvnUris.length; i++) {
            String mvnUri = mvnUris[i];
            ModuleNeeded module = new ModuleNeeded(context,informationMsg,required,mvnUri);
            String coordinate = MavenUrlHelper.getCoordinate(module.getMavenUri());
            assertEquals(expectedCoordinates[i], coordinate);
        }
        
    }
}
