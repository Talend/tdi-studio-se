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
public class EsbDemoImportTest extends DemosImportTest {

    private static final String ESB_DEMO_PLUGIN_ID = "org.talend.repository.services"; //$NON-NLS-1$

    private ResourcesManager esbResManager;

    String BEANS = "BEANS"; //$NON-NLS-1$

    String ROUTE_RESOURCES = "ROUTE_RESOURCES"; //$NON-NLS-1$

    String SERVICES = "SERVICES"; //$NON-NLS-1$

    @Before
    public void importEsbDemo() throws Exception {
        initDemo(ESB_DEMO_PLUGIN_ID);
        Assert.assertNotNull(currentDemo);
        initTestPaths();
        initTempPro();
        esbResManager = DemoImportTestUtil.getResourceManager(currentDemo);
        Assert.assertNotNull(esbResManager);
        ImportExportHandlersManager importManager = new ImportExportHandlersManager();
        List<ImportItem> projectRecords = importManager.populateImportingItems(esbResManager, true, new NullProgressMonitor());
        Assert.assertTrue(projectRecords.size() > 0);
        importManager.importItemRecords(new NullProgressMonitor(), esbResManager, projectRecords, true,
                projectRecords.toArray(new ImportItem[0]), null);
    }

    @Test
    public void testEsbDemoComplete() throws Exception {
        // test the job items under ESB_Demo.zip
        Assert.assertTrue(esbResManager instanceof FileResourcesUnityManager);
        Iterator pathes = esbResManager.getPaths().iterator();
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
        String rootPath = talendProjectFile.getParentFile().toString();
        File tempJobsFolder = new File(rootPath + File.separator + processItemPath);

        List<File> demoJobItemFiles = DemoImportTestUtil.collectProjectFilesFromDirectory(tempJobsFolder,
                FileConstants.ITEM_EXTENSION, true);
        Assert.assertTrue(demoJobItemFiles.size() > 0);
        int currentJobItemsSize = ProxyRepositoryFactory.getInstance().getAll(ERepositoryObjectType.PROCESS).size();
        Assert.assertTrue(currentJobItemsSize > 0);
        Assert.assertEquals(demoJobItemFiles.size(), currentJobItemsSize);

        // test the metadata items under ESB_Demo.zip
        int currentConnectionItemsSize = ProxyRepositoryFactory.getInstance().getAll(ERepositoryObjectType.METADATA).size();
        Assert.assertTrue(currentConnectionItemsSize > 0);
        File tempConnItemsFolder = new File(rootPath + File.separator + connectionItemPath);
        List<File> demoConItemsFiles = DemoImportTestUtil.collectProjectFilesFromDirectory(tempConnItemsFolder,
                FileConstants.ITEM_EXTENSION, true);
        Assert.assertTrue(demoConItemsFiles.size() > 0);
        Assert.assertEquals(demoConItemsFiles.size(), currentConnectionItemsSize);

        // test the routes under ESB_Demo.zip
        int currentRoutesSize = ProxyRepositoryFactory.getInstance().getAll(ERepositoryObjectType.PROCESS_ROUTE).size();
        Assert.assertTrue(currentRoutesSize > 0);
        File tempRoutesFolder = new File(rootPath + File.separator + routesItemPath);
        List<File> demoRoutesFiles = DemoImportTestUtil.collectProjectFilesFromDirectory(tempRoutesFolder,
                FileConstants.ITEM_EXTENSION, true);
        Assert.assertTrue(demoRoutesFiles.size() > 0);
        Assert.assertEquals(demoRoutesFiles.size(), currentRoutesSize);

        // test the route_resources items under ESB_Demo.zip
        int currentRouteResourcesSize = ProxyRepositoryFactory.getInstance()
                .getAll(ERepositoryObjectType.valueOf(ERepositoryObjectType.class, ROUTE_RESOURCES)).size();
        Assert.assertTrue(currentRouteResourcesSize > 0);
        File tempRouteResourcesFolder = new File(rootPath + File.separator + routeResourcePath);
        List<File> demoRouteResourceFiles = DemoImportTestUtil.collectProjectFilesFromDirectory(tempRouteResourcesFolder,
                FileConstants.ITEM_EXTENSION, true);
        Assert.assertTrue(demoRouteResourceFiles.size() > 0);
        Assert.assertEquals(demoRouteResourceFiles.size(), currentRouteResourcesSize);

        // test the wsdls under ESB_Demo.zip(ESB_DEMO/services)
        int currentWSDLsSize = ProxyRepositoryFactory.getInstance()
                .getAll(ERepositoryObjectType.valueOf(ERepositoryObjectType.class, SERVICES)).size();
        Assert.assertTrue(currentWSDLsSize > 0);
        File tempWSDLServicesFolder = new File(rootPath + File.separator + servicesItemPath);
        List<File> demoWSDLServiceFiles = DemoImportTestUtil.collectProjectFilesFromDirectory(tempWSDLServicesFolder,
                FileConstants.WSDL_FILE_SUFFIX, true);
        Assert.assertTrue(demoWSDLServiceFiles.size() > 0);
        Assert.assertEquals(demoWSDLServiceFiles.size(), currentWSDLsSize);
    }
}
