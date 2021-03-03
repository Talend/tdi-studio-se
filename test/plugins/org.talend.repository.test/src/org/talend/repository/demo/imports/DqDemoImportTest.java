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
public class DqDemoImportTest extends DemosImportTest {

	private static final String DQ_DEMO_PLUGIN_ID = "org.talend.dqdemo"; //$NON-NLS-1$

	private ResourcesManager dqResManager;

	@Before
	public void importDqDemo() throws Exception {
		initDemo(DQ_DEMO_PLUGIN_ID);
		Assert.assertNotNull(currentDemo);
		initTestPaths();
		initTempPro();
		// For DQ,has special structure to create for temp project before test
		initDQStructure();
		dqResManager = DemoImportTestUtil.getResourceManager(currentDemo);
		Assert.assertNotNull(dqResManager);
		rootPath = getRootPath(dqResManager);
		collectDemoData(rootPath);
		ImportExportHandlersManager importManager = new ImportExportHandlersManager();
		List<ImportItem> projectRecords = importManager.populateImportingItems(dqResManager, true,
				new NullProgressMonitor());
		Assert.assertTrue(projectRecords.size() > 0);
		importManager.importItemRecords(new NullProgressMonitor(), dqResManager, projectRecords, true,
				projectRecords.toArray(new ImportItem[0]), null);
	}

	@Test
	public void testDqDemoComplete() throws Exception {
		// test the job items under DQ_Demo.zip
		Assert.assertTrue(dqResManager instanceof FileResourcesUnityManager);

		List<File> demoJobItemFiles = getDemoItemFileList(rootPath + File.separator + processItemPath);
		Assert.assertTrue(demoJobItemFiles.size() > 0);
		int currentJobItemsSize = ProxyRepositoryFactory.getInstance().getAll(ERepositoryObjectType.PROCESS).size();
		Assert.assertTrue(currentJobItemsSize > 0);
		Assert.assertEquals(demoJobItemFiles.size(), currentJobItemsSize);

		// test the context items under DQ_Demo.zip
		int currentContextItemsSize = ProxyRepositoryFactory.getInstance().getAll(ERepositoryObjectType.CONTEXT).size();
		Assert.assertTrue(currentContextItemsSize > 0);

		List<File> demoContextItemsFiles = getDemoItemFileList(rootPath + File.separator + contextItemPath);
		Assert.assertTrue(demoContextItemsFiles.size() > 0);
		Assert.assertEquals(demoContextItemsFiles.size(), currentContextItemsSize);

		// test the metadata items under DQ_Demo.zip
		int currentConnectionItemsSize = ProxyRepositoryFactory.getInstance().getAll(ERepositoryObjectType.METADATA)
				.size();
		Assert.assertTrue(currentConnectionItemsSize > 0);
		List<File> demoConItemsFiles = getDemoItemFileList(rootPath + File.separator + connectionItemPath);
		Assert.assertTrue(demoConItemsFiles.size() > 0);
		Assert.assertEquals(demoConItemsFiles.size(), currentConnectionItemsSize);

		// test the routine items under DQ_Demo.zip
		doRoutinesItemsTest(rootPath);

		// test the business process items under DQ_Demo.zip
		int currentBusinessItemsSize = ProxyRepositoryFactory.getInstance()
				.getAll(ERepositoryObjectType.BUSINESS_PROCESS).size();
		Assert.assertTrue(currentBusinessItemsSize > 0);
		List<File> demoBusinessItemsFiles = getDemoItemFileList(rootPath + File.separator + bussniessProcessPath);
		Assert.assertTrue(demoBusinessItemsFiles.size() > 0);
		Assert.assertEquals(demoBusinessItemsFiles.size(), currentBusinessItemsSize);

		// test the documention items under DQ_Demo.zip
		int currentDocItemsSize = ProxyRepositoryFactory.getInstance().getAll(ERepositoryObjectType.DOCUMENTATION)
				.size();
		Assert.assertTrue(currentDocItemsSize > 0);
		List<File> demoDocItemsFiles = getDemoItemFileList(rootPath + File.separator + documentionPath);
		Assert.assertTrue(demoDocItemsFiles.size() > 0);
		Assert.assertEquals(demoDocItemsFiles.size(), currentDocItemsSize);

		// test the analyes items under DQ_Demo.zip
		int currentAnaItemsSize = ProxyRepositoryFactory.getInstance()
				.getAll(ERepositoryObjectType.TDQ_ANALYSIS_ELEMENT).size();
		Assert.assertTrue(currentAnaItemsSize > 0);
		List<File> demoAnaItemsFiles = getDemoItemFileList(rootPath + File.separator + tdqProfilingAnaPath);
		Assert.assertTrue(demoAnaItemsFiles.size() > 0);
		Assert.assertEquals(demoAnaItemsFiles.size(), currentAnaItemsSize);

		// test the reports items under DQ_Demo.zip
		int currentRepItemsSize = ProxyRepositoryFactory.getInstance().getAll(ERepositoryObjectType.TDQ_REPORT_ELEMENT)
				.size();
		Assert.assertTrue(currentRepItemsSize > 0);
		List<File> demoRepItemsFiles = getDemoItemFileList(rootPath + File.separator + tdqProfilingRepPath);
		Assert.assertTrue(demoRepItemsFiles.size() > 0);
		Assert.assertEquals(demoRepItemsFiles.size(), currentRepItemsSize);

		// test the rules sql items under
		// DQ_Demo.zip(DQ_EE_DEMOS\TDQ_Libraries\Rules\SQL)
		int currentRuleSqlItemsSize = ProxyRepositoryFactory.getInstance().getAll(ERepositoryObjectType.TDQ_RULES_SQL)
				.size();
		Assert.assertTrue(currentRuleSqlItemsSize > 0);
		List<File> demoRuleSqlItemsFiles = getDemoItemFileList(rootPath + File.separator + tdqLibrariesRuleSqlPath);
		Assert.assertTrue(demoRuleSqlItemsFiles.size() > 0);
		Assert.assertEquals(demoRuleSqlItemsFiles.size(), currentRuleSqlItemsSize);

		// test the pattern items under DQ_Demo.zip(DQ_EE_DEMOS\TDQ_Libraries\Patterns)
		int currentPatItemsSize = ProxyRepositoryFactory.getInstance().getAll(ERepositoryObjectType.TDQ_PATTERN_ELEMENT)
				.size();
		Assert.assertTrue(currentPatItemsSize > 0);
		List<File> demoPatItemsFiles = getDemoItemFileList(rootPath + File.separator + tdqLibrariesPatternPath);
		Assert.assertTrue(demoPatItemsFiles.size() > 0);
		Assert.assertEquals(demoPatItemsFiles.size(), currentPatItemsSize);
	}

	protected Map<String, String> getCollectFolderMap(String rootPath) {
		Map<String, String> map = super.getCollectFolderMap(rootPath);
		map.put(rootPath + File.separator + processItemPath, FileConstants.ITEM_EXTENSION);
		map.put(rootPath + File.separator + contextItemPath, FileConstants.ITEM_EXTENSION);
		map.put(rootPath + File.separator + connectionItemPath, FileConstants.ITEM_EXTENSION);
		map.put(rootPath + File.separator + bussniessProcessPath, FileConstants.ITEM_EXTENSION);
		map.put(rootPath + File.separator + documentionPath, FileConstants.ITEM_EXTENSION);
		map.put(rootPath + File.separator + tdqProfilingAnaPath, FileConstants.ANA_EXTENSION);
		map.put(rootPath + File.separator + tdqProfilingRepPath, FileConstants.REP_EXTENSION);
		map.put(rootPath + File.separator + tdqLibrariesRuleSqlPath, FileConstants.RULE_EXTENSION);
		map.put(rootPath + File.separator + tdqLibrariesPatternPath, FileConstants.PAT_EXTENSION);
		return map;
	}

    @Override
    protected String getRootPath(ResourcesManager resManager) {
        Iterator pathes = resManager.getPaths().iterator();
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
