// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.repository.model.migration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URL;
import java.util.List;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.talend.commons.exception.PersistenceException;
import org.talend.core.model.context.ContextUtils;
import org.talend.core.model.context.link.ContextLink;
import org.talend.core.model.context.link.ContextLinkService;
import org.talend.core.model.context.link.ContextParamLink;
import org.talend.core.model.context.link.ItemContextLink;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.ContextItem;
import org.talend.core.model.properties.JobletProcessItem;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.cwm.helper.ResourceHelper;
import org.talend.designer.core.model.utils.emf.talendfile.ContextParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.ContextType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;
import org.talend.model.bridge.ReponsitoryContextBridge;
import org.talend.repository.ProjectManager;
import org.talend.repository.items.importexport.handlers.ImportExportHandlersManager;
import org.talend.repository.items.importexport.handlers.model.ImportItem;
import org.talend.repository.items.importexport.manager.ResourcesManager;
import org.talend.repository.items.importexport.ui.managers.FileResourcesUnityManager;
import org.talend.repository.items.importexport.ui.managers.ResourcesManagerFactory;
import org.talend.repository.model.IProxyRepositoryFactory;

public class CreateContextLinkMigrationTaskTest {

    private static List<ImportItem> importedItems = null;

    @BeforeClass
    public static void setUp() throws Exception {

        ReponsitoryContextBridge.setProject(ProjectManager.getInstance().getCurrentProject().getEmfProject());

        ImportExportHandlersManager importManager = new ImportExportHandlersManager();
        URL testJobURL = FileLocator.find(Platform.getBundle("org.talend.repository.test"),
                new Path("/resources/CreateContextLinkMigrationTaskTest.zip"), null);
        if (testJobURL != null) {
            testJobURL = FileLocator.toFileURL(testJobURL);
        }
        File srcFile = new File(testJobURL.getFile());
        FileResourcesUnityManager fileUnityManager = ResourcesManagerFactory.getInstance().createFileUnityManager(srcFile);
        ResourcesManager resManager = fileUnityManager.doUnify();
        importedItems = importManager.populateImportingItems(resManager, true, new NullProgressMonitor());
        assertNotNull(importedItems);
        assertTrue(importedItems.size() > 0);
        importManager.importItemRecords(new NullProgressMonitor(), resManager, importedItems, true,
                importedItems.toArray(new ImportItem[0]), null);

    }

    @Test
    public void checkJob() throws Exception {
        List<IRepositoryViewObject> objects = getAll(ERepositoryObjectType.PROCESS);
        assertNotNull(objects);
        for (IRepositoryViewObject object : objects) {
            if (object.getLabel().startsWith("CreateContextLink")) {
                ProcessItem processItem = (ProcessItem) object.getProperty().getItem();
                ProcessType processType = processItem.getProcess();
                if (processType.getContext().size() > 0) {
                    ItemContextLink contextLink = ContextLinkService.getInstance().loadContextLinkFromJson(processItem);
                    assertNotNull(contextLink);
                    for (Object obj : processType.getContext()) {
                        if (obj instanceof ContextType) {
                            ContextType contextType = (ContextType) obj;
                            compareContextLink(contextLink, contextType.getName());
                        }
                    }
                }
            }
        }
    }

    @Test
    public void checkJoblet() throws Exception {
        List<IRepositoryViewObject> objects = getAll(ERepositoryObjectType.JOBLET);
        assertNotNull(objects);
        for (IRepositoryViewObject object : objects) {
            if (object.getLabel().startsWith("CreateContextLink")) {
                JobletProcessItem processItem = (JobletProcessItem) object.getProperty().getItem();
                ProcessType processType = processItem.getJobletProcess();
                if (processType.getContext().size() > 0) {
                    ItemContextLink contextLink = ContextLinkService.getInstance().loadContextLinkFromJson(processItem);
                    assertNotNull(contextLink);
                    for (Object obj : processType.getContext()) {
                        if (obj instanceof ContextType) {
                            ContextType contextType = (ContextType) obj;
                            compareContextLink(contextLink, contextType.getName());
                        }
                    }
                }
            }
        }

    }

    @Test
    public void checkTestCase() throws Exception {
        List<IRepositoryViewObject> objects = getAll(ERepositoryObjectType.TEST_CONTAINER);
        assertNotNull(objects);
        for (IRepositoryViewObject object : objects) {
            if (object.getLabel().startsWith("CreateContextLink")) {
                ProcessItem processItem = (ProcessItem) object.getProperty().getItem();
                ProcessType processType = processItem.getProcess();
                if (processType.getContext().size() > 0) {
                    ItemContextLink contextLink = ContextLinkService.getInstance().loadContextLinkFromJson(processItem);
                    assertNotNull(contextLink);
                    for (Object obj : processType.getContext()) {
                        if (obj instanceof ContextType) {
                            ContextType contextType = (ContextType) obj;
                            compareContextLink(contextLink, contextType.getName());
                        }
                    }
                }
            }
        }
    }

    @Test
    public void checkRoute() throws Exception {
        List<IRepositoryViewObject> objects = getAll(ERepositoryObjectType.PROCESS_ROUTE);
        assertNotNull(objects);
        for (IRepositoryViewObject object : objects) {
            if (object.getLabel().startsWith("CreateContextLink")) {
                ProcessItem processItem = (ProcessItem) object.getProperty().getItem();
                ProcessType processType = processItem.getProcess();
                if (processType.getContext().size() > 0) {
                    ItemContextLink contextLink = ContextLinkService.getInstance().loadContextLinkFromJson(processItem);
                    assertNotNull(contextLink);
                    for (Object obj : processType.getContext()) {
                        if (obj instanceof ContextType) {
                            ContextType contextType = (ContextType) obj;
                            compareContextLink(contextLink, contextType.getName());
                        }
                    }
                }
            }
        }
    }

    @Test
    public void checkRoutelet() throws Exception {
        List<IRepositoryViewObject> objects = getAll(ERepositoryObjectType.PROCESS_ROUTELET);
        assertNotNull(objects);
        for (IRepositoryViewObject object : objects) {
            if (object.getLabel().startsWith("CreateContextLink")) {
                ProcessItem processItem = (ProcessItem) object.getProperty().getItem();
                ProcessType processType = processItem.getProcess();
                if (processType.getContext().size() > 0) {
                    ItemContextLink contextLink = ContextLinkService.getInstance().loadContextLinkFromJson(processItem);
                    assertNotNull(contextLink);
                    for (Object obj : processType.getContext()) {
                        if (obj instanceof ContextType) {
                            ContextType contextType = (ContextType) obj;
                            compareContextLink(contextLink, contextType.getName());
                        }
                    }
                }
            }
        }
    }

    @Test
    public void checkMetadata() throws Exception {
        List<IRepositoryViewObject> objects = getAll(ERepositoryObjectType.METADATA_CONNECTIONS);
        assertNotNull(objects);
        for (IRepositoryViewObject object : objects) {
            if (object.getLabel().startsWith("CreateContextLink")) {
                ConnectionItem connectionItem = (ConnectionItem) object.getProperty().getItem();
                Connection connection = connectionItem.getConnection();
                if (connection != null && connection.isContextMode()) {
                    ItemContextLink contextLink = ContextLinkService.getInstance().loadContextLinkFromJson(connectionItem);
                    assertNotNull(contextLink);
                    compareContextLink(contextLink, connectionItem.getConnection().getContextName());
                }
            }
        }
    }

    private void compareContextLink(ItemContextLink processContextLink, String contextName) {
        ContextLink contextLink = processContextLink.findContextLink(null, contextName);
        assertNotNull(contextLink);
        ContextItem contextItem = ContextUtils.getContextItemById2(contextLink.getRepoId());
        ContextType repoContextType = ContextUtils.getContextTypeByName(contextItem.getContext(), contextLink.getContextName());
        for (Object object : repoContextType.getContextParameter()) {
            if (object instanceof ContextParameterType) {
                ContextParameterType parameterType = (ContextParameterType) object;
                ContextParamLink paramLink = contextLink.getParamLinkByName(parameterType.getName());
                assertNotNull(paramLink);
                assertEquals(paramLink.getId(), ResourceHelper.getUUID(parameterType));
            }
        }
    }

    @AfterClass
    public static void tearDown() throws Exception {
        if (importedItems != null && !importedItems.isEmpty()) {
            for (ImportItem item : importedItems) {
                IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
                IRepositoryViewObject repObj = factory.getLastVersion(item.getItemId());
                if (repObj != null) {
                    factory.deleteObjectPhysical(repObj);
                    ContextLinkService.getInstance().deleteContextLinkJsonFile(repObj.getProperty().getItem());
                }
            }
            importedItems = null;
        }
    }

    private List<IRepositoryViewObject> getAll(ERepositoryObjectType type) throws PersistenceException {
        return ProxyRepositoryFactory.getInstance().getAll(type);
    }

}
