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
        ImportExportHandlersManager importManager = new ImportExportHandlersManager();
        List<ImportItem> projectRecords = importManager.populateImportingItems(dqResManager, true, new NullProgressMonitor());
        Assert.assertTrue(projectRecords.size() > 0);
        importManager.importItemRecords(new NullProgressMonitor(), dqResManager, projectRecords, true,
                projectRecords.toArray(new ImportItem[0]), null);
    }

    @Test
    public void testDqDemoComplete() throws Exception {
        // test the job items under DQ_Demo.zip
        Assert.assertTrue(dqResManager instanceof FileResourcesUnityManager);
        Iterator path = dqResManager.getPaths().iterator();
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
        Assert.assertEquals(demoJobItemFiles.size(), currentJobItemsSize);

        // test the context items under DQ_Demo.zip
        int currentContextItemsSize = ProxyRepositoryFactory.getInstance().getAll(ERepositoryObjectType.CONTEXT).size();
        Assert.assertTrue(currentContextItemsSize > 0);
        File tempContextItemsFolder = new File(rootPath + File.separator + contextItemPath);
        List<File> demoContextItemsFiles = DemoImportTestUtil.collectProjectFilesFromDirectory(tempContextItemsFolder,
                FileConstants.ITEM_EXTENSION, true);
        Assert.assertTrue(demoContextItemsFiles.size() > 0);
        Assert.assertEquals(demoContextItemsFiles.size(), currentContextItemsSize);

        // test the metadata items under DQ_Demo.zip
        int currentConnectionItemsSize = ProxyRepositoryFactory.getInstance().getAll(ERepositoryObjectType.METADATA).size();
        Assert.assertTrue(currentConnectionItemsSize > 0);
        File tempConnItemsFolder = new File(rootPath + File.separator + connectionItemPath);
        List<File> demoConItemsFiles = DemoImportTestUtil.collectProjectFilesFromDirectory(tempConnItemsFolder,
                FileConstants.ITEM_EXTENSION, true);
        Assert.assertTrue(demoConItemsFiles.size() > 0);
        Assert.assertEquals(demoConItemsFiles.size(), currentConnectionItemsSize);

        // test the routine items under DQ_Demo.zip
        doRoutinesItemsTest(rootPath);

        // test the business process items under DQ_Demo.zip
        int currentBusinessItemsSize = ProxyRepositoryFactory.getInstance().getAll(ERepositoryObjectType.BUSINESS_PROCESS).size();
        Assert.assertTrue(currentBusinessItemsSize > 0);
        File tempBusinessItemsFolder = new File(rootPath + File.separator + bussniessProcessPath);
        List<File> demoBusinessItemsFiles = DemoImportTestUtil.collectProjectFilesFromDirectory(tempBusinessItemsFolder,
                FileConstants.ITEM_EXTENSION, true);
        Assert.assertTrue(demoBusinessItemsFiles.size() > 0);
        Assert.assertEquals(demoBusinessItemsFiles.size(), currentBusinessItemsSize);

        // test the documention items under DQ_Demo.zip
        int currentDocItemsSize = ProxyRepositoryFactory.getInstance().getAll(ERepositoryObjectType.DOCUMENTATION).size();
        Assert.assertTrue(currentDocItemsSize > 0);
        File tempDocItemsFolder = new File(rootPath + File.separator + documentionPath);
        List<File> demoDocItemsFiles = DemoImportTestUtil.collectProjectFilesFromDirectory(tempDocItemsFolder,
                FileConstants.ITEM_EXTENSION, true);
        Assert.assertTrue(demoDocItemsFiles.size() > 0);
        Assert.assertEquals(demoDocItemsFiles.size(), currentDocItemsSize);

        // test the analyes items under DQ_Demo.zip
        int currentAnaItemsSize = ProxyRepositoryFactory.getInstance().getAll(ERepositoryObjectType.TDQ_ANALYSIS_ELEMENT).size();
        Assert.assertTrue(currentAnaItemsSize > 0);
        File tempAnaItemsFolder = new File(rootPath + File.separator + tdqProfilingAnaPath);
        List<File> demoAnaItemsFiles = DemoImportTestUtil.collectProjectFilesFromDirectory(tempAnaItemsFolder,
                FileConstants.ANA_EXTENSION, true);
        Assert.assertTrue(demoAnaItemsFiles.size() > 0);
        Assert.assertEquals(demoAnaItemsFiles.size(), currentAnaItemsSize);

        // test the reports items under DQ_Demo.zip
        int currentRepItemsSize = ProxyRepositoryFactory.getInstance().getAll(ERepositoryObjectType.TDQ_REPORT_ELEMENT).size();
        Assert.assertTrue(currentRepItemsSize > 0);
        File tempRepItemsFolder = new File(rootPath + File.separator + tdqProfilingRepPath);
        List<File> demoRepItemsFiles = DemoImportTestUtil.collectProjectFilesFromDirectory(tempRepItemsFolder,
                FileConstants.REP_EXTENSION, true);
        Assert.assertTrue(demoRepItemsFiles.size() > 0);
        Assert.assertEquals(demoRepItemsFiles.size(), currentRepItemsSize);

        // test the rules sql items under DQ_Demo.zip(DQ_EE_DEMOS\TDQ_Libraries\Rules\SQL)
        int currentRuleSqlItemsSize = ProxyRepositoryFactory.getInstance().getAll(ERepositoryObjectType.TDQ_RULES_SQL).size();
        Assert.assertTrue(currentRuleSqlItemsSize > 0);
        File tempRuleSqlItemsFolder = new File(rootPath + File.separator + tdqLibrariesRuleSqlPath);
        List<File> demoRuleSqlItemsFiles = DemoImportTestUtil.collectProjectFilesFromDirectory(tempRuleSqlItemsFolder,
                FileConstants.RULE_EXTENSION, true);
        Assert.assertTrue(demoRuleSqlItemsFiles.size() > 0);
        Assert.assertEquals(demoRuleSqlItemsFiles.size(), currentRuleSqlItemsSize);

        // test the pattern items under DQ_Demo.zip(DQ_EE_DEMOS\TDQ_Libraries\Patterns)
        int currentPatItemsSize = ProxyRepositoryFactory.getInstance().getAll(ERepositoryObjectType.TDQ_PATTERN_ELEMENT).size();
        Assert.assertTrue(currentPatItemsSize > 0);
        File tempPatItemsFolder = new File(rootPath + File.separator + tdqLibrariesPatternPath);
        List<File> demoPatItemsFiles = DemoImportTestUtil.collectProjectFilesFromDirectory(tempPatItemsFolder,
                FileConstants.PAT_EXTENSION, true);
        Assert.assertTrue(demoPatItemsFiles.size() > 0);
        Assert.assertEquals(demoPatItemsFiles.size(), currentPatItemsSize);
    }
}
