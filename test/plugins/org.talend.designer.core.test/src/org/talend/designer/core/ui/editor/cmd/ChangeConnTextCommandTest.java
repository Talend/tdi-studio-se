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
package org.talend.designer.core.ui.editor.cmd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.talend.core.model.components.ComponentCategory;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.process.EConnectionType;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.Property;
import org.talend.core.ui.component.ComponentsFactoryProvider;
import org.talend.designer.core.ui.editor.connections.Connection;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.process.Process;

/**
 * created by wchen on Mar 13, 2017 Detailled comment
 *
 */
public class ChangeConnTextCommandTest {

    @Test
    public void testChangeConnectionLabel() {
        Property property1 = PropertiesFactory.eINSTANCE.createProperty();
        property1.setId("property1"); //$NON-NLS-1$
        property1.setVersion("0.1"); //$NON-NLS-1$
        property1.setLabel("test1");//$NON-NLS-1$
        Process process = new Process(property1);

        IComponent tMysqlComponent = ComponentsFactoryProvider.getInstance().get("tMysqlInput",
                ComponentCategory.CATEGORY_4_DI.getName());
        IComponent tRunjobComponent = ComponentsFactoryProvider.getInstance().get("tRunJob",
                ComponentCategory.CATEGORY_4_DI.getName());

        Node tMysqlInput_1 = new Node(tMysqlComponent, process);
        Node tRunJob_1 = new Node(tRunjobComponent, process);
        List contextList = new ArrayList();
        Map<String, String> map = new HashMap<String, String>();
        map.put("PARAM_NAME_COLUMN", "new1");
        map.put("PARAM_VALUE_COLUMN", "log4j.newColumn");
        contextList.add(map);
        tRunJob_1.getElementParameter("CONTEXTPARAMS").setValue(contextList);
        Connection connection = new Connection(tMysqlInput_1, tRunJob_1, EConnectionType.FLOW_MAIN,
                EConnectionType.FLOW_MAIN.getName(), "tMysqlInput_1", "log4j", "log4j", false);

        ChangeConnTextCommand command = new ChangeConnTextCommand(connection, "validLabel");
        command.execute();

        Assert.assertEquals(connection.getName(), "validLabel");
        Assert.assertEquals(map.get("PARAM_VALUE_COLUMN"), "validLabel.newColumn");

        command.undo();

        Assert.assertEquals(connection.getName(), "log4j");
        Assert.assertEquals(map.get("PARAM_VALUE_COLUMN"), "log4j.newColumn");
    }

}
