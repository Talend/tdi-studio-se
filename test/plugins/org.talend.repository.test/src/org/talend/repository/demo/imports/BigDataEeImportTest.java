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
 * created by ldong on 2014-5-4 Detailled comment
 *
 */
public class BigDataEeImportTest extends DemosImportTest {

    public static final String BIG_DATA_EE_DEMO_PLUGIN_ID = "org.talend.demo.bigdata.ee"; //$NON-NLS-1$

    private ResourcesManager eeResManager;

    @Before
    public void importEeDemo() throws Exception {
        initDemo(BIG_DATA_EE_DEMO_PLUGIN_ID);
        Assert.assertNotNull(currentDemo);
        initTestPaths();
        initTempPro();
        eeResManager = DemoImportTestUtil.getResourceManager(currentDemo);
        Assert.assertNotNull(eeResManager);
        ImportExportHandlersManager importManager = new ImportExportHandlersManager();
        List<ImportItem> projectRecords = importManager.populateImportingItems(eeResManager, true, new NullProgressMonitor());
        Assert.assertTrue(projectRecords.size() > 0);
        importManager.importItemRecords(new NullProgressMonitor(), eeResManager, projectRecords, true,
                projectRecords.toArray(new ImportItem[0]), null);
    }

    @Test
    public void testBdEeDemoComplete() throws Exception {
        // test the job items under BigDataDemo_ee.zip
        Assert.assertTrue(eeResManager instanceof FileResourcesUnityManager);
        Iterator path = eeResManager.getPaths().iterator();
        String firstFilePath = ((Path) path.next()).toPortableString();
        String eeDemoName = demoName.substring(0, demoName.indexOf("_"));
        String tempFolderPath = firstFilePath.substring(0,
                firstFilePath.indexOf(TEMP_FOLDER_SUFFIEX) + TEMP_FOLDER_SUFFIEX.length());
        Assert.assertTrue(new File(tempFolderPath).exists());
        String rootPath = tempFolderPath + File.separator + eeDemoName;
        Assert.assertTrue(new File(rootPath).exists());

        File tempJobsFolder = new File(rootPath + File.separator + processItemPath);
        List<File> demoJobItemFiles = DemoImportTestUtil.collectProjectFilesFromDirectory(tempJobsFolder,
                FileConstants.ITEM_EXTENSION, true);
        Assert.assertTrue(demoJobItemFiles.size() > 0);
        int currentJobItemsSize = ProxyRepositoryFactory.getInstance().getAll(ERepositoryObjectType.PROCESS).size();
        Assert.assertTrue(currentJobItemsSize > 0);
        Assert.assertEquals(demoJobItemFiles.size(), currentJobItemsSize);

        // test the hadoop items under BigDataDemo_ee.zip
        int currentMrItemsSize = ProxyRepositoryFactory.getInstance().getAll(ERepositoryObjectType.PROCESS_MR).size();
        File tempMrItemsFolder = new File(rootPath + File.separator + processMrPath);
        List<File> demoMrItemsFiles = DemoImportTestUtil.collectProjectFilesFromDirectory(tempMrItemsFolder,
                FileConstants.ITEM_EXTENSION, true);
        Assert.assertTrue(demoMrItemsFiles.size() > 0);
        Assert.assertTrue(currentMrItemsSize > 0);
        Assert.assertEquals(demoMrItemsFiles.size(), currentMrItemsSize);

        // test the context items under BigDataDemo_ee.zip
        int currentContextItemsSize = ProxyRepositoryFactory.getInstance().getAll(ERepositoryObjectType.CONTEXT).size();
        File tempContextItemsFolder = new File(rootPath + File.separator + contextItemPath);
        List<File> demoContextItemsFiles = DemoImportTestUtil.collectProjectFilesFromDirectory(tempContextItemsFolder,
                FileConstants.ITEM_EXTENSION, true);
        Assert.assertTrue(demoContextItemsFiles.size() > 0);

        Assert.assertTrue(currentContextItemsSize > 0);
        Assert.assertEquals(demoContextItemsFiles.size(), currentContextItemsSize);

        // test the routine items under BigDataDemo_ee.zip
        doRoutinesItemsTest(rootPath);

        // test the metadata items under BigDataDemo_ee.zip
        int currentConnectionItemsSize = ProxyRepositoryFactory.getInstance().getAll(ERepositoryObjectType.METADATA).size();
        int currentHadoopConnItemsSize = ProxyRepositoryFactory.getInstance()
                .getAll(ERepositoryObjectType.valueOf(ERepositoryObjectType.class, "HADOOPCLUSTER")).size();
        File tempConnItemsFolder = new File(rootPath + File.separator + connectionItemPath);
        List<File> demoConItemsFiles = DemoImportTestUtil.collectProjectFilesFromDirectory(tempConnItemsFolder,
                FileConstants.ITEM_EXTENSION, true);
        Assert.assertTrue(demoConItemsFiles.size() > 0);
        Assert.assertTrue(currentConnectionItemsSize > 0);
        Assert.assertEquals(demoConItemsFiles.size(), currentConnectionItemsSize);

        // test the documention items under BigDataDemo_ee.zip
        int currentDocItemsSize = ProxyRepositoryFactory.getInstance().getAll(ERepositoryObjectType.DOCUMENTATION).size();
        File tempDocItemsFolder = new File(rootPath + File.separator + documentionPath);
        List<File> demoDocItemsFiles = DemoImportTestUtil.collectProjectFilesFromDirectory(tempDocItemsFolder,
                FileConstants.ITEM_EXTENSION, true);
        Assert.assertTrue(demoDocItemsFiles.size() > 0);
        Assert.assertTrue(currentDocItemsSize > 0);
        Assert.assertEquals(demoDocItemsFiles.size(), currentDocItemsSize);
    }
}
