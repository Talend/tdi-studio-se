package org.talend.repository.ui.processor;

import org.junit.Assert;
import org.junit.Test;
import org.talend.commons.ui.runtime.image.ECoreImage;
import org.talend.core.model.metadata.builder.connection.ConnectionFactory;
import org.talend.core.model.metadata.builder.connection.MetadataTable;
import org.talend.core.model.properties.DatabaseConnectionItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.FakePropertyImpl;
import org.talend.core.model.repository.RepositoryViewObject;
import org.talend.core.repository.model.repositoryObject.MetadataTableRepositoryObject;
import org.talend.core.runtime.CoreRuntimePlugin;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.IRepositoryNode.ENodeType;
import org.talend.repository.model.IRepositoryNode.EProperties;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.model.StableRepositoryNode;

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

/**
 * created by hcyi on Jul 12, 2019
 * Detailled comment
 *
 */
public class QueryTypeProcessorTest {

    @Test
    public void testSelectRepositoryNode4SimpleFolder() {
        QueryTypeProcessor query = new QueryTypeProcessor(null);
        //
        RepositoryNode parent = new RepositoryNode(null, null, ENodeType.STABLE_SYSTEM_FOLDER);
        parent.setProperties(EProperties.LABEL, ERepositoryObjectType.METADATA);
        parent.setProperties(EProperties.CONTENT_TYPE, ERepositoryObjectType.METADATA);
        parent.setType(ENodeType.STABLE_SYSTEM_FOLDER);

        // simple folder
        Property property1 = new FakePropertyImpl();
        Item item1 = PropertiesFactory.eINSTANCE.createFolderItem();
        property1.setItem(item1);
        RepositoryViewObject object1 = new RepositoryViewObject(property1, true);
        RepositoryNode node1 = new RepositoryNode(object1, parent, ENodeType.SIMPLE_FOLDER);
        Assert.assertEquals(node1.getObjectType().getType(), ERepositoryObjectType.FOLDER.getType());
        Assert.assertTrue(query.selectRepositoryNode(null, parent, node1));
    }

    @Test
    public void testSelectRepositoryNode4MetadataConnections() {
        QueryTypeProcessor query = new QueryTypeProcessor(null);
        //
        RepositoryNode parent = new RepositoryNode(null, null, ENodeType.STABLE_SYSTEM_FOLDER);
        parent.setProperties(EProperties.LABEL, ERepositoryObjectType.METADATA);
        parent.setProperties(EProperties.CONTENT_TYPE, ERepositoryObjectType.METADATA);
        parent.setType(ENodeType.STABLE_SYSTEM_FOLDER);

        // metadata connections
        Property property2 = new FakePropertyImpl();
        Item item2 = PropertiesFactory.eINSTANCE.createFolderItem();
        property2.setItem(item2);
        RepositoryNode node2 = new RepositoryNode(null, parent, ENodeType.STABLE_SYSTEM_FOLDER);
        node2.setProperties(EProperties.LABEL, ERepositoryObjectType.METADATA_CONNECTIONS);
        node2.setProperties(EProperties.CONTENT_TYPE, ERepositoryObjectType.METADATA_CONNECTIONS);
        Assert.assertFalse(query.selectRepositoryNode(null, parent, node2));
    }

    @Test
    public void testSelectRepositoryNode4CDC() {
        QueryTypeProcessor query = new QueryTypeProcessor(null);
        IProxyRepositoryFactory factory = CoreRuntimePlugin.getInstance().getProxyRepositoryFactory();
        // db connection
        Property property = new FakePropertyImpl();
        DatabaseConnectionItem item = PropertiesFactory.eINSTANCE.createDatabaseConnectionItem();
        property.setItem(item);
        RepositoryViewObject object = new RepositoryViewObject(property, true);
        RepositoryNode cdcNode = new RepositoryNode(object, null, ENodeType.STABLE_SYSTEM_FOLDER);
        cdcNode.setProperties(EProperties.LABEL, ERepositoryObjectType.METADATA_CONNECTIONS);
        cdcNode.setProperties(EProperties.CONTENT_TYPE, ERepositoryObjectType.METADATA_CONNECTIONS);
        //
        StableRepositoryNode connTypeNode = new StableRepositoryNode(cdcNode, "cdc1", ECoreImage.METADATA_CDC_CONN_ICON);
        cdcNode.getChildren().add(connTypeNode);

        MetadataTable inputTable = ConnectionFactory.eINSTANCE.createMetadataTable();
        inputTable.setId(factory.getNextId());
        inputTable.setLabel("Input1");//$NON-NLS-1$

        MetadataTableRepositoryObject modelObj = new MetadataTableRepositoryObject(cdcNode.getObject(), inputTable);
        modelObj.setLabel("testObject1");
        RepositoryNode tableNode = new RepositoryNode(modelObj, connTypeNode, ENodeType.REPOSITORY_ELEMENT);
        tableNode.setProperties(EProperties.LABEL, "testObject1");
        tableNode.setProperties(EProperties.CONTENT_TYPE, ERepositoryObjectType.METADATA_CON_CDC);
        connTypeNode.getChildren().add(tableNode);

        Assert.assertFalse(query.selectRepositoryNode(null, null, tableNode));

        Assert.assertTrue(query.selectRepositoryNode(null, null, connTypeNode));
    }
}
