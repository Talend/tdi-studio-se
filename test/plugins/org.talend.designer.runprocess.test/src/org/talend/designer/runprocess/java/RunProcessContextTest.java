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
package org.talend.designer.runprocess.java;

import org.junit.Assert;
import org.junit.Test;
import org.talend.core.model.components.ComponentCategory;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.Property;
import org.talend.core.ui.component.ComponentsFactoryProvider;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.ui.editor.nodecontainer.NodeContainer;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.process.Process;
import org.talend.designer.runprocess.RunProcessContext;


/**
 * created by hcyi on Apr 14, 2017 Detailled comment
 *
 */
public class RunProcessContextTest {

    @Test
    public void testLoadLog4jContextFromProcess() {
        Property property1 = PropertiesFactory.eINSTANCE.createProperty();
        property1.setId("property1"); //$NON-NLS-1$
        property1.setVersion("0.1"); //$NON-NLS-1$
        property1.setLabel("job1");//$NON-NLS-1$
        Process process = new Process(property1);
        IComponent tMysqlInputComponent = ComponentsFactoryProvider.getInstance().get("tMysqlInput",//$NON-NLS-1$
                ComponentCategory.CATEGORY_4_DI.getName());
        Node tMysqlInput_1 = new Node(tMysqlInputComponent, process);
        process.addNodeContainer(new NodeContainer(tMysqlInput_1));
        RunProcessContext context = new RunProcessContext(process);
        Assert.assertFalse(context.isUseCustomLevel());
        Assert.assertEquals("", context.getLog4jLevel());//$NON-NLS-1$
        //
        process.getElementParameter(EParameterName.LOG4J_RUN_ACTIVATE.getName()).setValue(true);
        process.getElementParameter(EParameterName.LOG4J_RUN_LEVEL.getName()).setValue("Info");//$NON-NLS-1$
        context.loadLog4jContextFromProcess();
        Assert.assertTrue(context.isUseCustomLevel());
        Assert.assertEquals("Info", context.getLog4jLevel());//$NON-NLS-1$
    }
}
