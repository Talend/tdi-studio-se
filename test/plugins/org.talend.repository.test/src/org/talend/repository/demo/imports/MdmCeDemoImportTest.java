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
package org.talend.repository.demo.imports;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * created by ldong on 2014-4-29 Detailled comment
 *
 */
public class MdmCeDemoImportTest extends DemosImportTest {

	private static final String MDM_CE_DEMO_PLUGIN_ID = "org.talend.mdm.demo"; //$NON-NLS-1$

	// private ResourcesManager mdmResCeManager;

	@Before
	public void importMdmDemo() throws Exception {
		initDemo(MDM_CE_DEMO_PLUGIN_ID);

		// initTestPaths();
		// initTempPro();
		// mdmResCeManager = DemoImportTestUtil.getResourceManager(currentDemo);
		// Assert.assertNotNull(mdmResCeManager);
		// ImportExportHandlersManager importManager = new
		// ImportExportHandlersManager();
		// List<ImportItem> projectRecords =
		// importManager.populateImportingItems(mdmResCeManager, true, new
		// NullProgressMonitor());
		// Assert.assertTrue(projectRecords.size() > 0);
		// importManager.importItemRecords(new NullProgressMonitor(), mdmResCeManager,
		// projectRecords, true,
		// projectRecords.toArray(new ImportItem[0]), null);
	}

	@Test
	@Ignore
	public void testMdmCeDemoImportComplete() throws Exception {
		// until now,the MDM_CE demo is overrited by MDM_EE demo,so currentDemo is null
		Assert.assertNull(currentDemo);
		// Assert.assertTrue(mdmResCeManager instanceof FileResourcesUnityManager);
		// Iterator path = mdmResCeManager.getPaths().iterator();
		// String firstFilePath = ((Path) path.next()).toPortableString();
		// String tempFolderPath = firstFilePath.substring(0,
		// firstFilePath.indexOf(TEMP_FOLDER_SUFFIEX) + TEMP_FOLDER_SUFFIEX.length());
		// Assert.assertTrue(new File(tempFolderPath).exists());
		// String rootPath = tempFolderPath + File.separator + demoName;
		// Assert.assertTrue(new File(rootPath).exists());
		//
		// // test the job items under MDM_CE_Demo.zip
		// int currentJobItemsSize =
		// ProxyRepositoryFactory.getInstance().getAll(ERepositoryObjectType.PROCESS).size();
		// File tempJobsFolderForDemo = new File(rootPath + File.separator +
		// processItemPath);
		// List<File> demoJobItemFiles =
		// DemoImportTestUtil.collectProjectFilesFromDirectory(tempJobsFolderForDemo,
		// FileConstants.ITEM_EXTENSION, true);
		// Assert.assertTrue(demoJobItemFiles.size() > 0);
		// Assert.assertTrue(currentJobItemsSize > 0);
		// Assert.assertEquals(demoJobItemFiles.size(), currentJobItemsSize);
		//
		// // test the context items under MDM_CE_Demo.zip
		// int currentContextItemsSize =
		// ProxyRepositoryFactory.getInstance().getAll(ERepositoryObjectType.CONTEXT).size();
		// Assert.assertTrue(currentContextItemsSize > 0);
		// File tempContextItemsFolder = new File(rootPath + File.separator +
		// contextItemPath);
		// List<File> demoContextItemsFiles =
		// DemoImportTestUtil.collectProjectFilesFromDirectory(tempContextItemsFolder,
		// FileConstants.ITEM_EXTENSION, true);
		// Assert.assertTrue(demoContextItemsFiles.size() > 0);
		// Assert.assertEquals(demoContextItemsFiles.size(), currentContextItemsSize);
		//
		// // test the metadata items under MDM_CE_Demo.zip
		// int currentConnectionItemsSize =
		// ProxyRepositoryFactory.getInstance().getAll(ERepositoryObjectType.METADATA).size();
		// Assert.assertTrue(currentConnectionItemsSize > 0);
		// File tempConnItemsFolder = new File(rootPath + File.separator +
		// connectionItemPath);
		// List<File> demoConItemsFiles =
		// DemoImportTestUtil.collectProjectFilesFromDirectory(tempConnItemsFolder,
		// FileConstants.ITEM_EXTENSION, true);
		// Assert.assertTrue(demoConItemsFiles.size() > 0);
		// Assert.assertEquals(demoConItemsFiles.size(), currentConnectionItemsSize);
		//
		// // test the documention items under MDM_CE_Demo.zip
		// int currentDocItemsSize =
		// ProxyRepositoryFactory.getInstance().getAll(ERepositoryObjectType.DOCUMENTATION).size();
		// Assert.assertTrue(currentDocItemsSize > 0);
		// File tempDocItemsFolder = new File(rootPath + File.separator +
		// documentionPath);
		// List<File> demoDocItemsFiles =
		// DemoImportTestUtil.collectProjectFilesFromDirectory(tempDocItemsFolder,
		// FileConstants.ITEM_EXTENSION, true);
		// Assert.assertTrue(demoDocItemsFiles.size() > 0);
		// Assert.assertEquals(demoDocItemsFiles.size(), currentDocItemsSize);
		//
		// // TODO: test the item under MDM folder
	}
}
