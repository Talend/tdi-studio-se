// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.repository.demo.imports;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.repository.constants.FileConstants;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.repository.items.importexport.handlers.ImportExportHandlersManager;
import org.talend.repository.items.importexport.handlers.model.ImportItem;
import org.talend.repository.items.importexport.manager.ResourcesManager;
import org.talend.repository.items.importexport.ui.managers.FileResourcesUnityManager;

/**
 * created by ldong on 2014-4-29 Detailled comment
 *
 */
public class DiDemoImportTest extends DemosImportTest {

    private static final String DI_DEMO_PLUGIN_ID = "org.talend.resources";

    private ResourcesManager diResManager;

    @Before
    public void importDiDemo() throws Exception {
        initDemo(DI_DEMO_PLUGIN_ID);
        Assert.assertNotNull(currentDemo);
        initTestPaths();
        initTempPro();
        diResManager = DemoImportTestUtil.getResourceManager(currentDemo);
        Assert.assertNotNull(diResManager);
        ImportExportHandlersManager importManager = new ImportExportHandlersManager();
        List<ImportItem> projectRecords = importManager.populateImportingItems(diResManager, true, new NullProgressMonitor());
        Assert.assertTrue(projectRecords.size() > 0);
        importManager.importItemRecords(new NullProgressMonitor(), diResManager, projectRecords, true,
                projectRecords.toArray(new ImportItem[0]), null);
    }

    @Test
    public void testDiDemoJobItemsComplete() throws Exception {
        // test the job items under DI_Demo.zip
        Assert.assertTrue(diResManager instanceof FileResourcesUnityManager);
        Iterator path = diResManager.getPaths().iterator();
        String firstFilePath = ((Path) path.next()).toPortableString();
        String tempFolderPath = firstFilePath.substring(0,
                firstFilePath.indexOf(TEMP_FOLDER_SUFFIEX) + TEMP_FOLDER_SUFFIEX.length());
        Assert.assertTrue(new File(tempFolderPath).exists());
        String rootPath = tempFolderPath + File.separator + demoName;
        Assert.assertTrue(new File(rootPath).exists());
        File tempJobsFolder = new File(rootPath + File.separator + processItemPath);

        List<File> demoJobItemFiles = DemoImportTestUtil.collectProjectFilesFromDirectory(tempJobsFolder,
                FileConstants.ITEM_EXTENSION, true);
        Assert.assertTrue(demoJobItemFiles.size() > 0);
        int currentJobItemsSize = ProxyRepositoryFactory.getInstance().getAll(ERepositoryObjectType.PROCESS).size();
        Assert.assertTrue(currentJobItemsSize > 0);
        // items in the DI_Demo.zip,there only has one item with two versions,the item is tAddCRC,maybe need only
        // keep one version later for this item
        Assert.assertEquals(demoJobItemFiles.size() - 1, currentJobItemsSize);

        // test the context items under DI_Demo.zip
        int currentContextItemsSize = ProxyRepositoryFactory.getInstance().getAll(ERepositoryObjectType.CONTEXT).size();
        File tempContextItemsFolder = new File(rootPath + File.separator + contextItemPath);
        List<File> demoContextItemsFiles = DemoImportTestUtil.collectProjectFilesFromDirectory(tempContextItemsFolder,
                FileConstants.ITEM_EXTENSION, true);
        Assert.assertTrue(demoContextItemsFiles.size() > 0);

        Assert.assertTrue(currentContextItemsSize > 0);
        Assert.assertEquals(demoContextItemsFiles.size(), currentContextItemsSize);

        // test the metadata items under DI_Demo.zip
        int currentConnectionItemsSize = ProxyRepositoryFactory.getInstance().getAll(ERepositoryObjectType.METADATA).size();
        File tempConnItemsFolder = new File(rootPath + File.separator + connectionItemPath);
        List<File> demoConItemsFiles = DemoImportTestUtil.collectProjectFilesFromDirectory(tempConnItemsFolder,
                FileConstants.ITEM_EXTENSION, true);
        Assert.assertTrue(demoConItemsFiles.size() > 0);

        Assert.assertTrue(currentConnectionItemsSize > 0);
        Assert.assertEquals(demoConItemsFiles.size(), currentConnectionItemsSize);

        // test the routine items under DI_Demo.zip
        doRoutinesItemsTest(rootPath);

        // test the business process items under DI_Demo.zip
        int currentBusinessItemsSize = ProxyRepositoryFactory.getInstance().getAll(ERepositoryObjectType.BUSINESS_PROCESS).size();
        File tempBusinessItemsFolder = new File(rootPath + File.separator + bussniessProcessPath);
        List<File> demoBusinessItemsFiles = DemoImportTestUtil.collectProjectFilesFromDirectory(tempBusinessItemsFolder,
                FileConstants.ITEM_EXTENSION, true);
        Assert.assertTrue(demoBusinessItemsFiles.size() > 0);
        Assert.assertTrue(currentBusinessItemsSize > 0);
        Assert.assertEquals(demoBusinessItemsFiles.size(), currentBusinessItemsSize);

        // test the documention items under DI_Demo.zip
        int currentDocItemsSize = ProxyRepositoryFactory.getInstance().getAll(ERepositoryObjectType.DOCUMENTATION).size();
        File tempDocItemsFolder = new File(rootPath + File.separator + documentionPath);
        List<File> demoDocItemsFiles = DemoImportTestUtil.collectProjectFilesFromDirectory(tempDocItemsFolder,
                FileConstants.ITEM_EXTENSION, true);
        Assert.assertTrue(demoDocItemsFiles.size() > 0);
        Assert.assertTrue(currentDocItemsSize > 0);
        Assert.assertEquals(demoDocItemsFiles.size(), currentDocItemsSize);
    }
}
