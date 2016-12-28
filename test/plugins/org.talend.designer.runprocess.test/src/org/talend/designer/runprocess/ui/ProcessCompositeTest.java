// ============================================================================
//
// Copyright (C) 2006-2014 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.runprocess.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.junit.Assert;
import org.junit.Test;
import org.talend.core.model.components.ComponentCategory;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.Property;
import org.talend.core.ui.component.ComponentsFactoryProvider;
import org.talend.designer.core.ui.editor.nodecontainer.NodeContainer;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.process.Process;
import org.talend.designer.runprocess.ProcessMessage;
import org.talend.designer.runprocess.ProcessMessage.MsgType;
import org.talend.designer.runprocess.RunProcessContext;

/**
 * created by wchen on Dec 27, 2016 Detailled comment
 *
 */
public class ProcessCompositeTest {

    /**
     * Test method for
     * {@link org.talend.designer.runprocess.ui.ProcessComposite#getAllErrorMess(org.talend.designer.runprocess.IProcessMessage)}
     * .
     */
    @Test
    public void testGetAllErrorMess() {
        Property property1 = PropertiesFactory.eINSTANCE.createProperty();
        property1.setId("property1"); //$NON-NLS-1$
        property1.setVersion("0.1"); //$NON-NLS-1$
        property1.setLabel("ParentJob");//$NON-NLS-1$
        Process process = new Process(property1);
        IComponent tOracleSPComponent = ComponentsFactoryProvider.getInstance().get("tOracleSP",
                ComponentCategory.CATEGORY_4_DI.getName());
        Node tOracleSP_1 = new Node(tOracleSPComponent, process);
        process.addNodeContainer(new NodeContainer(tOracleSP_1));

        ProcessComposite composite = new ProcessComposite(new Composite(PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                .getShell(), SWT.NONE), SWT.NONE);
        RunProcessContext context = new RunProcessContext(process);
        composite.setProcessContext(context);
        String errorMessage1 = "Exception in component tOracleSP_1 (ParentJob)\njava.sql.SQLException: ORA-20000: Failed to execute one or more sql statements";
        ProcessMessage message1 = new ProcessMessage(MsgType.STD_ERR, errorMessage1);
        composite.getAllErrorMess(message1);
        Assert.assertEquals(composite.errorMessMap.get(tOracleSP_1.getUniqueName()).getContent(), errorMessage1);

        composite.errorMessMap.clear();
        String errorMessage2 = "Exception in component tOracleSP_1 (ChildJob)\njava.sql.SQLException: ORA-20000: Failed to execute one or more sql statements";
        ProcessMessage message2 = new ProcessMessage(MsgType.STD_ERR, errorMessage2);
        composite.getAllErrorMess(message2);
        Assert.assertNull(composite.errorMessMap.get(tOracleSP_1.getUniqueName()));
    }
}
