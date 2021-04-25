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
package org.talend.repository.ui.wizards.exportjob.util;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.talend.core.model.properties.FolderItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.RepositoryObject;
import org.talend.repository.model.IRepositoryNode.ENodeType;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.model.StableRepositoryNode;

/**
 * created by wchen on Jun 28, 2017 Detailled comment
 *
 */
public class ExportJobUtilTest {

    @Test
    public void testGetProcessItem() {
        // Job Design/folder1/folder2
        StableRepositoryNode jobDesign = new StableRepositoryNode(null, "Job Design", null);
        FolderItem folder1 = PropertiesFactory.eINSTANCE.createFolderItem();
        initItem(folder1);
        folder1.getProperty().setLabel("folder1");
        RepositoryNode repFolder1 = new RepositoryNode(new RepositoryObject(folder1.getProperty()), jobDesign,
                ENodeType.SIMPLE_FOLDER);
        jobDesign.getChildren().add(repFolder1);
        FolderItem folder2 = PropertiesFactory.eINSTANCE.createFolderItem();
        initItem(folder2);
        folder2.getProperty().setLabel("folder2");
        RepositoryNode repFolder2 = new RepositoryNode(new RepositoryObject(folder2.getProperty()), repFolder1,
                ENodeType.SIMPLE_FOLDER);
        repFolder1.getChildren().add(repFolder2);

        List<RepositoryNode> nodes = new ArrayList<RepositoryNode>();
        nodes.add(jobDesign);
        ProcessItem processItem = ExportJobUtil.getProcessItem(nodes);
        Assert.assertNull(processItem);

        // Job Design/folder1/folder2/testJob
        ProcessItem testJobItem = PropertiesFactory.eINSTANCE.createProcessItem();
        initItem(testJobItem);
        RepositoryNode repTestJob = new RepositoryNode(new RepositoryObject(testJobItem.getProperty()), repFolder2,
                ENodeType.REPOSITORY_ELEMENT);
        repFolder2.getChildren().add(repTestJob);
        nodes = new ArrayList<RepositoryNode>();
        nodes.add(jobDesign);
        processItem = ExportJobUtil.getProcessItem(nodes);
        Assert.assertNotNull(processItem);

        // Job Design/testJob
        jobDesign = new StableRepositoryNode(null, "Job Design", null);
        testJobItem = PropertiesFactory.eINSTANCE.createProcessItem();
        initItem(testJobItem);
        repTestJob = new RepositoryNode(new RepositoryObject(testJobItem.getProperty()), jobDesign, ENodeType.REPOSITORY_ELEMENT);
        jobDesign.getChildren().add(repTestJob);
        nodes = new ArrayList<RepositoryNode>();
        nodes.add(jobDesign);
        processItem = ExportJobUtil.getProcessItem(nodes);
        Assert.assertNotNull(processItem);
    }

    private void initItem(Item item) {
        Property createProperty = PropertiesFactory.eINSTANCE.createProperty();
        item.setProperty(createProperty);
        item.setState(PropertiesFactory.eINSTANCE.createItemState());
        createProperty.setItem(item);

    }
}
