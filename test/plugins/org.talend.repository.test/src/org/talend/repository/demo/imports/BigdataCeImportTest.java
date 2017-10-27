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
public class BigdataCeImportTest extends DemosImportTest {

    public static final String BIG_DATA_CE_DEMO_PLUGIN_ID = "org.talend.demo.bigdata.ce"; //$NON-NLS-1$

    private ResourcesManager ceResManager;

    @Before
    public void importCeDemo() throws Exception {
        initDemo(BIG_DATA_CE_DEMO_PLUGIN_ID);
        if (currentDemo == null) { // maybe license problem, the demo plugin is not loaded.
            return;
        }
        initTestPaths();
        initTempPro();
        Assert.assertNotNull(currentDemo);
        ceResManager = DemoImportTestUtil.getResourceManager(currentDemo);
        Assert.assertNotNull(ceResManager);
        ImportExportHandlersManager importManager = new ImportExportHandlersManager();
        List<ImportItem> projectRecords = importManager.populateImportingItems(ceResManager, true, new NullProgressMonitor());
        Assert.assertTrue(projectRecords.size() > 0);
        importManager.importItemRecords(new NullProgressMonitor(), ceResManager, projectRecords, true,
                projectRecords.toArray(new ImportItem[0]), null);
    }

    @Test
    public void testBdCeDemoComplete() throws Exception {
        if (currentDemo == null) { // maybe license problem, the demo plugin is not loaded.
            return;
        }
        Assert.assertTrue(ceResManager instanceof FileResourcesUnityManager);
        Iterator path = ceResManager.getPaths().iterator();
        String firstFilePath = ((Path) path.next()).toPortableString();
        String ceDemoName = demoName.substring(0, demoName.indexOf("_"));
        String tempFolderPath = firstFilePath.substring(0,
                firstFilePath.indexOf(TEMP_FOLDER_SUFFIEX) + TEMP_FOLDER_SUFFIEX.length());
        Assert.assertTrue(new File(tempFolderPath).exists());
        String rootPath = tempFolderPath + File.separator + ceDemoName;
        Assert.assertTrue(new File(rootPath).exists());

        // test the job items under BigDataDemo_ce.zip
        File tempJobsFolder = new File(rootPath + File.separator + processItemPath);
        List<File> demoJobItemFiles = DemoImportTestUtil.collectProjectFilesFromDirectory(tempJobsFolder,
                FileConstants.ITEM_EXTENSION, true);
        Assert.assertTrue(demoJobItemFiles.size() > 0);
        int currentJobItemsSize = ProxyRepositoryFactory.getInstance().getAll(ERepositoryObjectType.PROCESS).size();
        Assert.assertTrue(currentJobItemsSize > 0);
        Assert.assertEquals(demoJobItemFiles.size(), currentJobItemsSize);

        // test the context items under BigDataDemo_ce.zip
        int currentContextItemsSize = ProxyRepositoryFactory.getInstance().getAll(ERepositoryObjectType.CONTEXT).size();
        Assert.assertTrue(currentContextItemsSize > 0);
        File tempContextItemsFolder = new File(rootPath + File.separator + contextItemPath);
        List<File> demoContextItemsFiles = DemoImportTestUtil.collectProjectFilesFromDirectory(tempContextItemsFolder,
                FileConstants.ITEM_EXTENSION, true);
        Assert.assertTrue(demoContextItemsFiles.size() > 0);
        Assert.assertEquals(demoContextItemsFiles.size(), currentContextItemsSize);
    }
}
