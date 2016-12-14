// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
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

import org.junit.Test;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.repository.FakePropertyImpl;
import org.talend.core.ui.component.ComponentsFactoryProvider;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.process.Process;

/**
 * created by ycbai on 2016年12月14日 Detailled comment
 *
 */
public class DesignerUtilitiesTest {

    @Test
    public void testGetMainSchemaParameterName() {
        Process process = new Process(new FakePropertyImpl());

        // New component
        IComponent component = ComponentsFactoryProvider.getInstance().get("tSalesforceInput", "DI"); //$NON-NLS-1$ //$NON-NLS-2$
        Node node = new Node(component, process);
        String parameterName = DesignerUtilities.getMainSchemaParameterName(node);
        assertEquals("module.main.schema" + ":" + EParameterName.REPOSITORY_SCHEMA_TYPE.getName(), parameterName); //$NON-NLS-1$ //$NON-NLS-2$

        // Old component
        component = ComponentsFactoryProvider.getInstance().get("tJavaRow", "DI"); //$NON-NLS-1$ //$NON-NLS-2$
        node = new Node(component, process);
        parameterName = DesignerUtilities.getMainSchemaParameterName(node);
        assertEquals("SCHEMA:" + EParameterName.REPOSITORY_SCHEMA_TYPE.getName(), parameterName); //$NON-NLS-1$
    }

}
