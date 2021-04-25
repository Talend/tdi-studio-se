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
package org.talend.sdk.component.studio.util;

import static java.util.Collections.emptyMap;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.talend.core.model.process.BigDataNode;
import org.talend.core.model.process.EComponentCategory;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.designer.core.model.components.DummyComponent;
import org.talend.designer.core.model.components.ElementParameter;
import org.talend.designer.core.ui.editor.process.Process;
import org.talend.sdk.component.server.front.model.ComponentId;
import org.talend.sdk.component.server.front.model.ComponentIndex;
import org.talend.sdk.component.server.front.model.SimplePropertyDefinition;
import org.talend.sdk.component.studio.i18n.Messages;

/**
 * created by hcyi on Oct 9, 2019 Detailled comment
 *
 */
public class TaCoKitUtilTest {

    @Test
    public void testGetDisplayName1() throws Exception {
        final ComponentId id = new ComponentId("Y291Y2hiYXNlI0NvdWNoYmFzZSNJbnB1dA", "Y291Y2hiYXNlI0NvdWNoYmFzZQ", "test",
                "org.test.components:test:1.1.0-SNAPSHOT", "Test", "Input");
        final ComponentIndex index = new ComponentIndex(id, "Test Input", null, null, null, 1, Arrays.asList("Local", "File"),
                null, emptyMap());
        String displayName = TaCoKitUtil.getDisplayName(index);
        Assert.assertEquals("TestInput", displayName);//$NON-NLS-1$
    }

    @Test
    public void testGetDisplayName2() throws Exception {
        final ComponentId id = new ComponentId("Y291Y2hiYXNlI0NvdWNoYmFzZSNJbnB1dA", "Y291Y2hiYXNlI0NvdWNoYmFzZQ", "test",
                "org.talend.components:test:1.1.0-SNAPSHOT", "Test", "Input");
        final ComponentIndex index = new ComponentIndex(id, "Test Input", null, null, null, 1, Arrays.asList("Local", "File"),
                null, emptyMap());
        String displayName = TaCoKitUtil.getDisplayName(index);
        Assert.assertEquals("tTestInput", displayName);//$NON-NLS-1$
    }

    @Test
    public void testisTaCoKitComponentMadeByTalendNull() throws Exception {
        Assert.assertFalse(TaCoKitUtil.isTaCoKitComponentMadeByTalend(null));
    }

    @Test
    public void testisTaCoKitComponentMadeByTalendEmpty() throws Exception {
        final ComponentId id = new ComponentId("Y291Y2hiYXNlI0NvdWNoYmFzZSNJbnB1dA", "Y291Y2hiYXNlI0NvdWNoYmFzZQ", "test", "",
                "Test", "Input");
        final ComponentIndex index = new ComponentIndex(id, "Test Input", null, null, null, 1, Arrays.asList("Local", "File"),
                null, emptyMap());
        Assert.assertFalse(TaCoKitUtil.isTaCoKitComponentMadeByTalend(index));
    }

    @Test
    public void testisTaCoKitComponentMadeByTalendFalse() throws Exception {
        final ComponentId id = new ComponentId("Y291Y2hiYXNlI0NvdWNoYmFzZSNJbnB1dA", "Y291Y2hiYXNlI0NvdWNoYmFzZQ", "test",
                "org.test.components:test:1.1.0-SNAPSHOT", "Test", "Input");
        final ComponentIndex index = new ComponentIndex(id, "Test Input", null, null, null, 1, Arrays.asList("Local", "File"),
                null, emptyMap());
        Assert.assertFalse(TaCoKitUtil.isTaCoKitComponentMadeByTalend(index));
    }

    @Test
    public void testisTaCoKitComponentMadeByTalendTrue() throws Exception {
        final ComponentId id = new ComponentId("Y291Y2hiYXNlI0NvdWNoYmFzZSNJbnB1dA", "Y291Y2hiYXNlI0NvdWNoYmFzZQ", "test",
                "org.talend.components:test:1.1.0-SNAPSHOT", "Test", "Input");
        final ComponentIndex index = new ComponentIndex(id, "Test Input", null, null, null, 1, Arrays.asList("Local", "File"),
                null, emptyMap());
        Assert.assertTrue(TaCoKitUtil.isTaCoKitComponentMadeByTalend(index));
    }

    @Test
    public void testIsUseExistConnection() {
        BigDataNode node = new BigDataNode(new DummyComponent("dummyComponent"), "dummyComponent");
        node.setActivate(true);
        IProcess process = new Process(PropertiesFactory.eINSTANCE.createProperty());
        node.setProcess(process);

        final ElementParameter parameter = new ElementParameter(node);
        parameter.setName(TaCoKitConst.PARAMETER_USE_EXISTING_CONNECTION);
        parameter.setValue(false);
        parameter.setDisplayName(Messages.getString("ElementParameterCreator.userExistConnectionLabel"));
        parameter.setFieldType(EParameterFieldType.CHECK);
        parameter.setCategory(EComponentCategory.BASIC);
        parameter.setNumRow(1);
        parameter.setReadOnly(false);
        parameter.setRequired(false);
        parameter.setShow(true);
        parameter.setDefaultValue(false);
        node.addElementParameter(parameter);

        String connectionName = "tNetSuiteConnection_1";
        final ElementParameter connectionParameter = new ElementParameter(node);
        connectionParameter.setName(TaCoKitConst.PARAMETER_CONNECTION);
        connectionParameter.setDisplayName(Messages.getString("ElementParameterCreator.connectionLabel"));
        connectionParameter.setFieldType(EParameterFieldType.COMPONENT_LIST);
        connectionParameter.setCategory(EComponentCategory.BASIC);
        connectionParameter.setReadOnly(false);
        connectionParameter.setRequired(true);
        connectionParameter.setNumRow(1);
        connectionParameter.setShow(true);
        connectionParameter.setValue(connectionName);
        connectionParameter.setDynamicSettings(true);
        connectionParameter.setShowIf("USE_EXISTING_CONNECTION == 'true'");
        node.addElementParameter(connectionParameter);

        assertFalse(TaCoKitUtil.isUseExistConnection(node));

        parameter.setValue(true);
        assertTrue(TaCoKitUtil.isUseExistConnection(node));
        assertEquals(connectionName, TaCoKitUtil.getUseExistConnectionName(node));
    }

    @Test
    public void testGetDatastorePath() {
        SimplePropertyDefinition p = new SimplePropertyDefinition();
        p.setName("dataset");
        p.setPath("configuration.datestore.dataset");

        List<SimplePropertyDefinition> propertyList = new ArrayList<SimplePropertyDefinition>();
        propertyList.add(p);

        assertNull(TaCoKitUtil.getDatastorePath(propertyList));

        p = new SimplePropertyDefinition();
        p.setName("connection");
        p.setPath("configuration.connection");
        propertyList.add(p);
        assertEquals("configuration.connection", TaCoKitUtil.getDatastorePath(propertyList));

        p = new SimplePropertyDefinition();
        p.setName("datastore");
        p.setPath("configuration.datastore");
        propertyList.add(p);
        assertEquals("configuration.datastore", TaCoKitUtil.getDatastorePath(propertyList));
    }

    @Test
    public void testGetConfigurationPath() {
        SimplePropertyDefinition p = new SimplePropertyDefinition();
        p.setName("configuration");
        p.setPath("configuration");

        List<SimplePropertyDefinition> propertyList = new ArrayList<SimplePropertyDefinition>();
        propertyList.add(p);
        assertEquals("configuration", TaCoKitUtil.getConfigurationPath(propertyList));
    }
}
