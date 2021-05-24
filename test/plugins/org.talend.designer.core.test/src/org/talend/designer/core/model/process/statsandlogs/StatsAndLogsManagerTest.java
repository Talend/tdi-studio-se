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
package org.talend.designer.core.model.process.statsandlogs;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.talend.core.model.components.IMultipleComponentItem;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.INode;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.Property;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.model.process.DataNode;
import org.talend.designer.core.ui.editor.process.Process;

/**
 * created by wchen on Jun 13, 2018 Detailled comment
 *
 */
public class StatsAndLogsManagerTest {

    @Test
    public void testGetStatsAndLogsNodes() {
        Property property1 = PropertiesFactory.eINSTANCE.createProperty();
        property1.setId("property1"); //$NON-NLS-1$
        property1.setVersion("0.1"); //$NON-NLS-1$
        property1.setLabel("test1");//$NON-NLS-1$
        Process process = new Process(property1);
        process.getElementParameter(EParameterName.ON_DATABASE_FLAG.getName()).setValue(true);
        process.getElementParameter(EParameterName.ON_FILES_FLAG.getName()).setValue(true);
        process.getElementParameter(EParameterName.ON_CONSOLE_FLAG.getName()).setValue(true);
        process.getElementParameter(EParameterName.ON_STATCATCHER_FLAG.getName()).setValue(true);
        process.getElementParameter(EParameterName.DB_TYPE.getName()).setValue("tMysqlOutput");

        List<DataNode> statsAndLogsNodes = StatsAndLogsManager.getStatsAndLogsNodes(process);
        Assert.assertEquals(statsAndLogsNodes.size(), 4);
        DataNode jobStatsComponent = null;
        for (DataNode node : statsAndLogsNodes) {
            if (node.getComponent().getName().equals("org.talend.designer.core.model.process.statsandlogs.JobStatsComponent")) {
                jobStatsComponent = node;
                break;
            }
        }

        JobStatsComponent component = (JobStatsComponent) jobStatsComponent.getComponent();
        List<IMultipleComponentItem> itemList = component.getMultipleComponentManagers().get(0).getItemList();
        Assert.assertEquals(itemList.size(), 4);
        Assert.assertEquals(itemList.get(0).getComponent(), "tStatCatcher");
        Assert.assertEquals(itemList.get(1).getComponent(), "tFileOutputDelimited");
        Assert.assertEquals(itemList.get(2).getComponent(), "tMysqlOutput");
        Assert.assertEquals(itemList.get(3).getComponent(), "tLogRow");

        process.getElementParameter(EParameterName.ON_FILES_FLAG.getName()).setValue(false);
        statsAndLogsNodes = StatsAndLogsManager.getStatsAndLogsNodes(process);
        Assert.assertEquals(statsAndLogsNodes.size(), 4);
        jobStatsComponent = null;
        for (DataNode node : statsAndLogsNodes) {
            if (node.getComponent().getName().equals("org.talend.designer.core.model.process.statsandlogs.JobStatsComponent")) {
                jobStatsComponent = node;
                break;
            }
        }

        component = (JobStatsComponent) jobStatsComponent.getComponent();
        itemList = component.getMultipleComponentManagers().get(0).getItemList();
        Assert.assertEquals(itemList.size(), 3);
        Assert.assertEquals(itemList.get(0).getComponent(), "tStatCatcher");
        Assert.assertEquals(itemList.get(1).getComponent(), "tMysqlOutput");
        Assert.assertEquals(itemList.get(2).getComponent(), "tLogRow");

    }

    @Test
    public void testGetStatsAndLogsNodesForGeneric() {
        Property property1 = PropertiesFactory.eINSTANCE.createProperty();
        property1.setId("property1"); //$NON-NLS-1$
        property1.setVersion("0.1"); //$NON-NLS-1$
        property1.setLabel("test1");//$NON-NLS-1$
        Process process = new Process(property1);
        process.getElementParameter(EParameterName.ON_DATABASE_FLAG.getName()).setValue(true);
        process.getElementParameter(EParameterName.ON_FILES_FLAG.getName()).setValue(true);
        process.getElementParameter(EParameterName.ON_CONSOLE_FLAG.getName()).setValue(true);
        process.getElementParameter(EParameterName.ON_STATCATCHER_FLAG.getName()).setValue(true);
        process.getElementParameter(EParameterName.DB_TYPE.getName()).setValue("tJDBCOutput");

        List<DataNode> statsAndLogsNodes = StatsAndLogsManager.getStatsAndLogsNodes(process);
        Assert.assertEquals(statsAndLogsNodes.size(), 7);
        DataNode tStatCatcherNode = null;
        for (DataNode node : statsAndLogsNodes) {
            if (node.getComponent().getName().equals("tStatCatcher")) {
                tStatCatcherNode = node;
                break;
            }
        }
        Assert.assertNotNull(tStatCatcherNode);
        List<? extends IConnection> outgoingConnections = tStatCatcherNode.getOutgoingConnections("FLOW");
        Assert.assertEquals(outgoingConnections.size(), 1);
        INode fileNode = outgoingConnections.get(0).getTarget();
        Assert.assertEquals(fileNode.getComponent().getName(), "tFileOutputDelimited");

        outgoingConnections = fileNode.getOutgoingConnections("FLOW");
        Assert.assertEquals(outgoingConnections.size(), 1);
        INode dbNode = outgoingConnections.get(0).getTarget();
        Assert.assertEquals(dbNode.getComponent().getName(), "tJDBCOutput");

        outgoingConnections = dbNode.getOutgoingConnections("MAIN");
        Assert.assertEquals(outgoingConnections.size(), 1);
        INode consoleNode = outgoingConnections.get(0).getTarget();
        Assert.assertEquals(consoleNode.getComponent().getName(), "tLogRow");

        process.getElementParameter(EParameterName.ON_FILES_FLAG.getName()).setValue(false);
        statsAndLogsNodes = StatsAndLogsManager.getStatsAndLogsNodes(process);
        Assert.assertEquals(statsAndLogsNodes.size(), 6);
        tStatCatcherNode = null;
        for (DataNode node : statsAndLogsNodes) {
            if (node.getComponent().getName().equals("tStatCatcher")) {
                tStatCatcherNode = node;
                break;
            }
        }
        Assert.assertNotNull(tStatCatcherNode);
        outgoingConnections = tStatCatcherNode.getOutgoingConnections("FLOW");
        Assert.assertEquals(outgoingConnections.size(), 1);
        dbNode = outgoingConnections.get(0).getTarget();
        Assert.assertEquals(dbNode.getComponent().getName(), "tJDBCOutput");

        outgoingConnections = dbNode.getOutgoingConnections("MAIN");
        Assert.assertEquals(outgoingConnections.size(), 1);
        consoleNode = outgoingConnections.get(0).getTarget();
        Assert.assertEquals(consoleNode.getComponent().getName(), "tLogRow");
    }

}
