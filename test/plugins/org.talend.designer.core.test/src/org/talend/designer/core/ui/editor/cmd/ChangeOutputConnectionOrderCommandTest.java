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
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.talend.core.model.components.ComponentCategory;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.MetadataColumn;
import org.talend.core.model.metadata.MetadataTable;
import org.talend.core.model.process.EConnectionType;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.Property;
import org.talend.core.ui.component.ComponentsFactoryProvider;
import org.talend.designer.core.ui.editor.connections.Connection;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.process.Process;

/**
 * created by wchen on Feb 16, 2017 Detailled comment
 *
 */
public class ChangeOutputConnectionOrderCommandTest {

    IComponent tSAPBapiComponent;

    IComponent tLogRowComponent;

    @Test
    public void test() {
        tSAPBapiComponent = ComponentsFactoryProvider.getInstance().get("tSAPBapi", ComponentCategory.CATEGORY_4_DI.getName());
        tLogRowComponent = ComponentsFactoryProvider.getInstance().get("tLogRow", ComponentCategory.CATEGORY_4_DI.getName());
        Property property1 = PropertiesFactory.eINSTANCE.createProperty();
        property1.setId("property1"); //$NON-NLS-1$
        property1.setVersion("0.1"); //$NON-NLS-1$
        property1.setLabel("test1");//$NON-NLS-1$
        Process process = new Process(property1);
        Node tSAPBapi_1 = new Node(tSAPBapiComponent, process);
        tSAPBapi_1.getMetadataList().add(createMetadataTable("row_IV_KOKRS_1"));
        tSAPBapi_1.getMetadataList().add(createMetadataTable("row_ES_RETURN_1"));
        tSAPBapi_1.getMetadataList().add(createMetadataTable("row_ET_COSTELEMGRP_HIER_1"));
        tSAPBapi_1.getMetadataList().add(createMetadataTable("row_SINGLE_PARAM_1"));
        Node tLogRow_1 = new Node(tLogRowComponent, process);
        Connection conn1 = new Connection(tSAPBapi_1, tLogRow_1, EConnectionType.FLOW_MAIN, EConnectionType.FLOW_MAIN.getName(),
                "tSAPBapi_1", "row_ES_RETURN_1", "row_ES_RETURN_1", false);
        Connection conn2 = new Connection(tSAPBapi_1, tLogRow_1, EConnectionType.FLOW_MAIN, EConnectionType.FLOW_MAIN.getName(),
                "tSAPBapi_1", "row_ET_COSTELEMGRP_HIER_1", "row_ET_COSTELEMGRP_HIER_1", false);
        Connection conn3 = new Connection(tSAPBapi_1, tLogRow_1, EConnectionType.FLOW_MAIN, EConnectionType.FLOW_MAIN.getName(),
                "tSAPBapi_1", "row_SINGLE_PARAM_1", "row_SINGLE_PARAM_1", false);

        List<IConnection> reoderConnection = new ArrayList<IConnection>();
        reoderConnection.add(conn2);
        reoderConnection.add(conn3);
        reoderConnection.add(conn1);

        ChangeOutputConnectionOrderCommand command = new ChangeOutputConnectionOrderCommand(tSAPBapi_1, reoderConnection);
        command.execute();
        Assert.assertEquals(conn1.getConnectionLabel().getLabelText(), "row_ES_RETURN_1 (Main order:3)");
        Assert.assertEquals(conn2.getConnectionLabel().getLabelText(), "row_ET_COSTELEMGRP_HIER_1 (Main order:1)");
        Assert.assertEquals(conn3.getConnectionLabel().getLabelText(), "row_SINGLE_PARAM_1 (Main order:2)");
        command.undo();
        Assert.assertEquals(conn1.getConnectionLabel().getLabelText(), "row_ES_RETURN_1 (Main order:1)");
        Assert.assertEquals(conn2.getConnectionLabel().getLabelText(), "row_ET_COSTELEMGRP_HIER_1 (Main order:2)");
        Assert.assertEquals(conn3.getConnectionLabel().getLabelText(), "row_SINGLE_PARAM_1 (Main order:3)");
    }

    private IMetadataTable createMetadataTable(String tableName) {
        IMetadataTable table = new MetadataTable();
        table.setTableName(tableName);
        MetadataColumn newColumn = new MetadataColumn();
        newColumn.setLabel("newColumn");
        newColumn.setTalendType("id_String");
        table.getListColumns().add(newColumn);
        return table;
    }

}
