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

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
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
public class MdmEeDemoImportTest extends DemosImportTest {

	private static final String MDM_EE_DEMO_PLUGIN_ID = "org.talend.mdm.demo.enterprise"; //$NON-NLS-1$

	private ResourcesManager mdmResEeManager;

	@Before
	public void importMdmEeDemo() throws Exception {
		initDemo(MDM_EE_DEMO_PLUGIN_ID);
		Assert.assertNotNull(currentDemo);
		initTestPaths();
		initTempPro();
		mdmResEeManager = DemoImportTestUtil.getResourceManager(currentDemo);
		Assert.assertNotNull(mdmResEeManager);
		rootPath = getRootPath(mdmResEeManager);
		Assert.assertTrue(new File(rootPath).exists());
		collectDemoData(rootPath);
		ImportExportHandlersManager importManager = new ImportExportHandlersManager();
		List<ImportItem> projectRecords = importManager.populateImportingItems(mdmResEeManager, true,
				new NullProgressMonitor());
		Assert.assertTrue(projectRecords.size() > 0);
		importManager.importItemRecords(new NullProgressMonitor(), mdmResEeManager, projectRecords, true,
				projectRecords.toArray(new ImportItem[0]), null);
	}

	@Test
	public void testMdmEeDemoImportComplete() throws Exception {
		Assert.assertTrue(mdmResEeManager instanceof FileResourcesUnityManager);
		// test the job items under MDM_EE_Demo.zip
		int currentJobItemsSize = ProxyRepositoryFactory.getInstance().getAll(ERepositoryObjectType.PROCESS).size();
		List<File> demoJobItemFiles = getDemoItemFileList(rootPath + File.separator + processItemPath);
		Assert.assertTrue(demoJobItemFiles.size() > 0);
		Assert.assertTrue(currentJobItemsSize > 0);
		Assert.assertEquals(demoJobItemFiles.size(), currentJobItemsSize);

		// test the context items under MDM_EE_Demo.zip
		int currentContextItemsSize = ProxyRepositoryFactory.getInstance().getAll(ERepositoryObjectType.CONTEXT).size();
		Assert.assertTrue(currentContextItemsSize > 0);
		List<File> demoContextItemsFiles = getDemoItemFileList(rootPath + File.separator + contextItemPath);
		Assert.assertTrue(demoContextItemsFiles.size() > 0);
		Assert.assertEquals(demoContextItemsFiles.size(), currentContextItemsSize);

		// test the metadata items under MDM_EE_Demo.zip
		int currentConnectionItemsSize = ProxyRepositoryFactory.getInstance().getAll(ERepositoryObjectType.METADATA)
				.size();
        Assert.assertTrue(currentConnectionItemsSize == 1);
		List<File> demoConItemsFiles = getDemoItemFileList(rootPath + File.separator + connectionItemPath);
		Assert.assertTrue(demoConItemsFiles.size() > 0);

		// 1.test the item under MDM/datamodel
		int currentDmItemsSize = ProxyRepositoryFactory.getInstance()
				.getAll(ERepositoryObjectType.valueOf(ERepositoryObjectType.class, "MDM.DataModel")).size();
		Assert.assertTrue(currentDmItemsSize > 0);
		List<File> demoDmItemsFiles = getDemoItemFileList(rootPath + File.separator + mdmDataModelath);
		Assert.assertTrue(demoDmItemsFiles.size() > 0);
		Assert.assertEquals(demoDmItemsFiles.size(), currentDmItemsSize);

		// 2.test the item under MDM/resource
		int currentResItemsSize = ProxyRepositoryFactory.getInstance()
				.getAll(ERepositoryObjectType.valueOf(ERepositoryObjectType.class, "MDM.Resource")).size();
		Assert.assertTrue(currentResItemsSize > 0);
		List<File> demoResItemsFiles = getDemoItemFileList(rootPath + File.separator + mdmResourcePath);
		Assert.assertTrue(demoResItemsFiles.size() > 0);
		Assert.assertEquals(demoResItemsFiles.size(), currentResItemsSize);

		// 3.test the item under MDM/role
		int currentRoleItemsSize = ProxyRepositoryFactory.getInstance()
				.getAll(ERepositoryObjectType.valueOf(ERepositoryObjectType.class, "MDM.Role")).size();
		Assert.assertTrue(currentRoleItemsSize > 0);
		List<File> demoRoleItemsFiles = getDemoItemFileList(rootPath + File.separator + mdmRolePath);
		Assert.assertTrue(demoRoleItemsFiles.size() > 0);
		Assert.assertEquals(demoRoleItemsFiles.size(), currentRoleItemsSize);

		// 3.test the item under MDM/routingrule
		int currentRoutineRoletemsSize = ProxyRepositoryFactory.getInstance()
				.getAll(ERepositoryObjectType.valueOf(ERepositoryObjectType.class, "MDM.RoutingRule")).size();
		Assert.assertTrue(currentRoutineRoletemsSize > 0);
		List<File> demoRoutineRoleItemsFiles = getDemoItemFileList(rootPath + File.separator + mdmRoutingrulePath);
		Assert.assertTrue(demoRoutineRoleItemsFiles.size() > 0);
		Assert.assertEquals(demoRoutineRoleItemsFiles.size(), currentRoutineRoletemsSize);

		// 4.test the item under MDM/transformerV2
		int currentTransItemsSize = ProxyRepositoryFactory.getInstance()
				.getAll(ERepositoryObjectType.valueOf(ERepositoryObjectType.class, "MDM.TransformerV2")).size();
		Assert.assertTrue(currentTransItemsSize > 0);
		List<File> demoTransItemsFiles = getDemoItemFileList(rootPath + File.separator + mdmTransformerV2Path);
		Assert.assertTrue(demoTransItemsFiles.size() > 0);
		Assert.assertEquals(demoTransItemsFiles.size(), currentTransItemsSize);

		// 5.test the item under MDM/view
		int currentViewItemsSize = ProxyRepositoryFactory.getInstance()
				.getAll(ERepositoryObjectType.valueOf(ERepositoryObjectType.class, "MDM.View")).size();
		Assert.assertTrue(currentViewItemsSize > 0);
		List<File> demoViewtemsFiles = getDemoItemFileList(rootPath + File.separator + mdmViewPath);
		Assert.assertTrue(demoViewtemsFiles.size() > 0);
		Assert.assertEquals(demoViewtemsFiles.size(), currentViewItemsSize);

//		boolean isInstalledBonitaBPM = Platform.getBundle("org.bonitasoft.studio.application") != null;
//		if(isInstalledBonitaBPM) {
//			// 6.test the item under MDM/workflow
//			int currentWorkflowItemsSize = ProxyRepositoryFactory.getInstance()
//					.getAll(ERepositoryObjectType.valueOf(ERepositoryObjectType.class, "MDM.Workflow")).size();
//			Assert.assertTrue(currentWorkflowItemsSize > 0);
//			File tempWorkflowItemsFolder = new File(rootPath + File.separator + mdmWorkflowPath);
//			List<File> demoWorkflowtemsFiles = getDemoItemFileList(rootPath + File.separator + mdmWorkflowPath);
//			Assert.assertTrue(demoWorkflowtemsFiles.size() > 0);
//			Assert.assertEquals(demoWorkflowtemsFiles.size(), currentWorkflowItemsSize);
//		}
		
	}

	protected Map<String, String> getCollectFolderMap(String rootPath) {
		Map<String, String> map = super.getCollectFolderMap(rootPath);
		map.put(rootPath + File.separator + processItemPath, FileConstants.ITEM_EXTENSION);
		map.put(rootPath + File.separator + contextItemPath, FileConstants.ITEM_EXTENSION);
		map.put(rootPath + File.separator + connectionItemPath, FileConstants.ITEM_EXTENSION);
		map.put(rootPath + File.separator + mdmDataModelath, FileConstants.ITEM_EXTENSION);
		map.put(rootPath + File.separator + mdmResourcePath, FileConstants.ITEM_EXTENSION);
		map.put(rootPath + File.separator + mdmRolePath, FileConstants.ITEM_EXTENSION);
		map.put(rootPath + File.separator + mdmRoutingrulePath, FileConstants.ITEM_EXTENSION);
		map.put(rootPath + File.separator + mdmTransformerV2Path, FileConstants.ITEM_EXTENSION);
		map.put(rootPath + File.separator + mdmViewPath, FileConstants.ITEM_EXTENSION);
		map.put(rootPath + File.separator + mdmWorkflowPath, FileConstants.ITEM_EXTENSION);
		return map;
	}

	protected String getRootPath(ResourcesManager resManager) {
		Iterator pathes = mdmResEeManager.getPaths().iterator();
		IPath projectPath = null;
		while (pathes.hasNext()) {
			IPath p = ((Path) pathes.next());
			if (p.lastSegment().equals(FileConstants.LOCAL_PROJECT_FILENAME)) {
				projectPath = p;
				break;
			}
		}
		Assert.assertNotNull("Can't find the talend.project, so it's invalid demo", projectPath);
		final File talendProjectFile = projectPath.toFile();
		Assert.assertTrue(talendProjectFile.exists());
		return talendProjectFile.getParentFile().toString();
	}
}
