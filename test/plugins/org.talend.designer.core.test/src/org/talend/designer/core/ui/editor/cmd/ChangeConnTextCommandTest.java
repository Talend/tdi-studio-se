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
import org.talend.core.model.utils.TalendTextUtils;
import org.talend.core.ui.component.ComponentsFactoryProvider;
import org.talend.designer.core.ui.editor.connections.Connection;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.process.Process;

/**
 * created by wchen on Mar 13, 2017 Detailled comment
 *
 */
public class ChangeConnTextCommandTest {

    private Node nodeInput = null;

    private Node nodeMap = null;

    private Connection connection = null;

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

    private void init4ConnectionRenameLinkName() {
        Property property1 = PropertiesFactory.eINSTANCE.createProperty();
        property1.setId("property1"); //$NON-NLS-1$
        property1.setVersion("0.1"); //$NON-NLS-1$
        property1.setLabel("test1");//$NON-NLS-1$
        Process process = new Process(property1);

        IComponent tELTInput = ComponentsFactoryProvider.getInstance().get("tELTOracleInput",
                ComponentCategory.CATEGORY_4_DI.getName());
        IComponent tELTMap = ComponentsFactoryProvider.getInstance().get("tELTOracleMap",
                ComponentCategory.CATEGORY_4_DI.getName());

        nodeInput = new Node(tELTInput, process);
        nodeMap = new Node(tELTMap, process);
        nodeInput.getElementParameter("ELT_SCHEMA_NAME").setValue(TalendTextUtils.addQuotes("a"));
        nodeInput.getElementParameter("ELT_TABLE_NAME").setValue(TalendTextUtils.addQuotes("b"));

        connection = new Connection(nodeInput, nodeMap, EConnectionType.TABLE, EConnectionType.TABLE.getName(),
                "tELTInput_1", "a.b", "tInput_1", false);
    }

    @Test
    public void testConnectionRenameLinkNameBothContext() {
        init4ConnectionRenameLinkName();

        ChangeConnTextCommand command = new ChangeConnTextCommand(connection, "context.a.context.b");
        command.execute();

        Assert.assertEquals(connection.getName(), "context.a.context.b");
        Assert.assertEquals(nodeInput.getElementParameter("ELT_SCHEMA_NAME").getValue(), TalendTextUtils.addQuotes("context.a"));
        Assert.assertEquals(nodeInput.getElementParameter("ELT_TABLE_NAME").getValue(), TalendTextUtils.addQuotes("context.b"));
    }

    @Test
    public void testConnectionRenameLinkNameStartContext() {
        init4ConnectionRenameLinkName();

        ChangeConnTextCommand command = new ChangeConnTextCommand(connection, "context.a.b");
        command.execute();

        Assert.assertEquals(connection.getName(), "context.a.b");
        Assert.assertEquals(nodeInput.getElementParameter("ELT_SCHEMA_NAME").getValue(), TalendTextUtils.addQuotes("context.a"));
        Assert.assertEquals(nodeInput.getElementParameter("ELT_TABLE_NAME").getValue(), TalendTextUtils.addQuotes("b"));
    }

    @Test
    public void testConnectionRenameLinkNameEndContext() {
        init4ConnectionRenameLinkName();

        ChangeConnTextCommand command = new ChangeConnTextCommand(connection, "a.context.b");
        command.execute();

        Assert.assertEquals(connection.getName(), "a.context.b");
        Assert.assertEquals(nodeInput.getElementParameter("ELT_SCHEMA_NAME").getValue(), TalendTextUtils.addQuotes("a"));
        Assert.assertEquals(nodeInput.getElementParameter("ELT_TABLE_NAME").getValue(), TalendTextUtils.addQuotes("context.b"));
    }

    @Test
    public void testConnectionRenameLinkNameNoContext() {
        init4ConnectionRenameLinkName();

        ChangeConnTextCommand command = new ChangeConnTextCommand(connection, "a.c");
        command.execute();

        Assert.assertEquals(connection.getName(), "a.c");
        Assert.assertEquals(nodeInput.getElementParameter("ELT_SCHEMA_NAME").getValue(), TalendTextUtils.addQuotes("a"));
        Assert.assertEquals(nodeInput.getElementParameter("ELT_TABLE_NAME").getValue(), TalendTextUtils.addQuotes("c"));
    }

    @Test
    public void testConnectionRenameLinkName() {
        init4ConnectionRenameLinkName();

        ChangeConnTextCommand command = new ChangeConnTextCommand(connection, "c");
        command.execute();

        Assert.assertEquals(connection.getName(), "c");
        Assert.assertEquals(nodeInput.getElementParameter("ELT_SCHEMA_NAME").getValue(), TalendTextUtils.addQuotes("a"));
        Assert.assertEquals(nodeInput.getElementParameter("ELT_TABLE_NAME").getValue(), TalendTextUtils.addQuotes("b"));
    }

    @Test
    public void testConnectionRenameLinkNameEmpty() {
        init4ConnectionRenameLinkName();

        ChangeConnTextCommand command = new ChangeConnTextCommand(connection, "");
        command.execute();

        Assert.assertEquals(connection.getName(), "");
        Assert.assertEquals(nodeInput.getElementParameter("ELT_SCHEMA_NAME").getValue(), TalendTextUtils.addQuotes("a"));
        Assert.assertEquals(nodeInput.getElementParameter("ELT_TABLE_NAME").getValue(), TalendTextUtils.addQuotes("b"));
    }

    @Test
    public void testConnectionRenameLinkNameTUP_29072() {
        init4ConnectionRenameLinkName();

        nodeInput.getElementParameter("ELT_SCHEMA_NAME").setValue(TalendTextUtils.addQuotes("context.DWH_Schema"));
        nodeInput.getElementParameter("ELT_TABLE_NAME").setValue(TalendTextUtils.addQuotes("ODS_EGY_TBT_REF_INDICATEUR"));

        connection = new Connection(nodeInput, nodeMap, EConnectionType.TABLE, EConnectionType.TABLE.getName(), "tELTInput_1",
                "context.ODS_Schema.ODS_EGY_TBT_REF_INDICATEUR", "tInput_1", false);

        Assert.assertEquals(nodeInput.getElementParameter("ELT_SCHEMA_NAME").getValue(),
                TalendTextUtils.addQuotes("context.DWH_Schema"));
        Assert.assertEquals(nodeInput.getElementParameter("ELT_TABLE_NAME").getValue(),
                TalendTextUtils.addQuotes("ODS_EGY_TBT_REF_INDICATEUR"));

        ChangeConnTextCommand command = new ChangeConnTextCommand(connection, "context.ODS_Schema.ODS_EGY_TBT_REF_INDICATEUR");
        command.execute();

        Assert.assertEquals(connection.getName(), "context.ODS_Schema.ODS_EGY_TBT_REF_INDICATEUR");
        Assert.assertEquals(nodeInput.getElementParameter("ELT_SCHEMA_NAME").getValue(),
                TalendTextUtils.addQuotes("context.ODS_Schema"));
        Assert.assertEquals(nodeInput.getElementParameter("ELT_TABLE_NAME").getValue(),
                TalendTextUtils.addQuotes("ODS_EGY_TBT_REF_INDICATEUR"));
    }

    @Test
    public void testConnectionRenameLinkNameTUP_29072_2() {
        init4ConnectionRenameLinkName();

        nodeInput.getElementParameter("ELT_SCHEMA_NAME").setValue(TalendTextUtils.addQuotes("context.DWH_Schema"));
        nodeInput.getElementParameter("ELT_TABLE_NAME").setValue(TalendTextUtils.addQuotes("ODS_EGY_TBT_REF_INDICATEUR"));

        connection = new Connection(nodeInput, nodeMap, EConnectionType.TABLE, EConnectionType.TABLE.getName(), "tELTInput_1",
                "context.ODS_Schema.ODS_EGY_TBT_REF_INDICATEUR", "tInput_1", false);

        Assert.assertEquals(nodeInput.getElementParameter("ELT_SCHEMA_NAME").getValue(),
                TalendTextUtils.addQuotes("context.DWH_Schema"));
        Assert.assertEquals(nodeInput.getElementParameter("ELT_TABLE_NAME").getValue(),
                TalendTextUtils.addQuotes("ODS_EGY_TBT_REF_INDICATEUR"));

        ChangeConnTextCommand command = new ChangeConnTextCommand(connection, "context.ODS_Schema");
        command.execute();

        Assert.assertEquals(connection.getName(), "context.ODS_Schema");
        Assert.assertEquals(nodeInput.getElementParameter("ELT_SCHEMA_NAME").getValue(),
                TalendTextUtils.addQuotes("context.DWH_Schema"));
        Assert.assertEquals(nodeInput.getElementParameter("ELT_TABLE_NAME").getValue(),
                TalendTextUtils.addQuotes("ODS_EGY_TBT_REF_INDICATEUR"));
    }
}
