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
package org.talend.presentation.onboarding.resource.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipFile;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.talend.commons.exception.CommonExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.runtime.model.emf.provider.EmfResourcesFactoryReader;
import org.talend.commons.runtime.model.emf.provider.ResourceOption;
import org.talend.commons.runtime.model.repository.ERepositoryStatus;
import org.talend.core.model.process.IProcess2;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.update.RepositoryUpdateManager;
import org.talend.core.model.utils.RepositoryManagerHelper;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.repository.seeker.RepositorySeekerManager;
import org.talend.core.runtime.CoreRuntimePlugin;
import org.talend.core.ui.editor.JobEditorInput;
import org.talend.designer.core.ui.AbstractMultiPageTalendEditor;
import org.talend.designer.core.ui.MultiPageTalendEditor;
import org.talend.designer.core.ui.editor.ProcessEditorInput;
import org.talend.repository.items.importexport.handlers.ImportExportHandlersManager;
import org.talend.repository.items.importexport.handlers.model.ImportItem;
import org.talend.repository.items.importexport.manager.ResourcesManager;
import org.talend.repository.items.importexport.ui.managers.ResourcesManagerFactory;
import org.talend.repository.items.importexport.wizard.models.ImportNodesBuilder;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.IRepositoryNode;
import org.talend.repository.ui.views.IRepositoryView;

/**
 * created by ycbai on 2015年1月29日 Detailled comment
 *
 */
public class TalendImportUtil {

    private static ImportNodesBuilder nodesBuilder = new ImportNodesBuilder();

    private static List<ImportItem> populateItems(final ImportExportHandlersManager importManager,
            final ResourcesManager resourcesManager, IProgressMonitor monitor, final boolean overwrite) {
        List<ImportItem> selectedItemRecords = new ArrayList<ImportItem>();
        nodesBuilder.clear();
        if (resourcesManager != null) { // if resource is not init successfully.
            try {
                // List<ImportItem> items = importManager.populateImportingItems(resourcesManager, overwrite,
                // new NullProgressMonitor(), true);
                List<ImportItem> items = importManager.populateImportingItems(resourcesManager, overwrite, monitor, true);
                nodesBuilder.addItems(items);
            } catch (Exception e) {
                CommonExceptionHandler.process(e);
            }
        }
        ImportItem[] allImportItemRecords = nodesBuilder.getAllImportItemRecords();
        selectedItemRecords.addAll(Arrays.asList(allImportItemRecords));
        Iterator<ImportItem> itemIterator = selectedItemRecords.iterator();
        while (itemIterator.hasNext()) {
            ImportItem item = itemIterator.next();
            if (!item.isValid()) {
                itemIterator.remove();
            }
        }
        return selectedItemRecords;
    }

    public static boolean importItems(String zipPath, IProgressMonitor monitor, final boolean overwrite, final boolean openThem,
            boolean needMigrationTask) throws IOException {
        ZipFile srcZipFile = new ZipFile(zipPath);
        final ResourcesManager resourcesManager = ResourcesManagerFactory.getInstance().createResourcesManager(srcZipFile);
        final ResourceOption importOption = ResourceOption.DEMO_IMPORTATION;
        try {
            EmfResourcesFactoryReader.INSTANCE.addOption(importOption);

            resourcesManager.collectPath2Object(srcZipFile);
            final ImportExportHandlersManager importManager = new ImportExportHandlersManager();
            final List<ImportItem> items = populateItems(importManager, resourcesManager, monitor, overwrite);
            final List<String> itemIds = new ArrayList<String>();

            for (ImportItem itemRecord : items) {
                Item item = itemRecord.getProperty().getItem();
                if (item instanceof ProcessItem) {
                    // only select jobs
                    itemIds.add(item.getProperty().getId());
                }
                IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
                if (item.getState().isLocked()) {
                    factory.unlock(item);
                }
                ERepositoryStatus status = factory.getStatus(item);
                if (status != null && status == ERepositoryStatus.LOCK_BY_USER) {
                    factory.unlock(item);
                }
                if (!needMigrationTask) {
                    itemRecord.setMigrationTasksToApply(null);
                }
            }
            // importManager.importItemRecords(new NullProgressMonitor(), resourcesManager, items, overwrite,
            // nodesBuilder.getAllImportItemRecords(), null);
            if (items != null && !items.isEmpty()) {
                importManager.importItemRecords(monitor, resourcesManager, items, overwrite,
                        nodesBuilder.getAllImportItemRecords(), null);
            }
        } catch (Exception e) {
            CommonExceptionHandler.process(e);
        } finally {
            // clean
            if (resourcesManager != null) {
                resourcesManager.closeResource();
            }
            nodesBuilder.clear();

            EmfResourcesFactoryReader.INSTANCE.removOption(importOption);
        }
        return true;
    }

    // private static void doSelection(List<String> itemIds) {
    // List<IRepositoryNode> nodes = new ArrayList<IRepositoryNode>();
    // RepositorySeekerManager repSeekerManager = RepositorySeekerManager.getInstance();
    // for (String itemId : itemIds) {
    // IRepositoryNode repoViewNode = repSeekerManager.searchRepoViewNode(itemId);
    // if (repoViewNode != null) {
    // nodes.add(repoViewNode);
    // }
    // }
    //
    // IRepositoryView repositoryView = RepositoryManagerHelper.findRepositoryView();
    // repositoryView.getViewer().setSelection(new StructuredSelection(nodes));
    // }

    public static void openJob(String jobName) {
        if (jobName == null) {
            return;
        }
        if (isJobAlreadyOpened(jobName)) {
            return;
        }
        try {
            // can't open deleted jobs
            List<IRepositoryViewObject> repViewObjectList = ProxyRepositoryFactory.getInstance().getAll(
                    ERepositoryObjectType.PROCESS, false, false);
            Iterator<IRepositoryViewObject> repoViewObjectIter = repViewObjectList.iterator();
            while (repoViewObjectIter.hasNext()) {
                final IRepositoryViewObject current = repoViewObjectIter.next();
                if (jobName.equals(current.getLabel())) {
                    Display.getDefault().syncExec(new Runnable() {

                        @Override
                        public void run() {
                            final List<IRepositoryNode> jobs = new ArrayList<IRepositoryNode>(1);
                            IRepositoryNode repositoryNode = RepositorySeekerManager.getInstance().searchRepoViewNode(
                                    current.getId());
                            jobs.add(repositoryNode);
                            IRepositoryView repositoryView = RepositoryManagerHelper.findRepositoryView();
                            repositoryView.getViewer().setSelection(new StructuredSelection(jobs));
                            openJobs(jobs);
                        }
                    });
                    break;
                }
            }
        } catch (Throwable e) {
            CommonExceptionHandler.process(e);
        }

    }

    private static void openJobs(List<IRepositoryNode> nodes) {
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        for (IRepositoryNode node : nodes) {
            Property property = node.getObject().getProperty();
            if (property != null) {
                Item item = property.getItem();
                if (!(item instanceof ProcessItem)) {
                    continue;
                }
                try {
                    ProcessItem processItem = (ProcessItem) item;
                    final JobEditorInput fileEditorInput = getEditorInput(processItem);
                    // checkUnLoadedNodeForProcess(fileEditorInput);
                    final IEditorPart editorPart = page.findEditor(fileEditorInput);
                    if (editorPart == null) {
                        fileEditorInput.setRepositoryNode(node);
                        page.openEditor(fileEditorInput, getEditorId(), true);
                    } else {
                        ((AbstractMultiPageTalendEditor) editorPart).setReadOnly(fileEditorInput.setForceReadOnly(false));
                        page.activate(editorPart);
                    }
                } catch (Throwable e) {
                    CommonExceptionHandler.process(e);
                }
            }
        }
    }

    private static boolean isJobAlreadyOpened(String jobName) {
        List<IProcess2> openedProcessList = CoreRuntimePlugin.getInstance().getDesignerCoreService()
                .getOpenedProcess(RepositoryUpdateManager.getEditors());
        if (openedProcessList == null || openedProcessList.isEmpty()) {
            return false;
        }
        for (IProcess2 process : openedProcessList) {
            if (jobName.equals(process.getName())) {
                return true;
            }
        }
        return false;
    }

    private static JobEditorInput getEditorInput(final ProcessItem processItem) throws PersistenceException {
        return new ProcessEditorInput(processItem, true, true);
    }

    private static String getEditorId() {
        return MultiPageTalendEditor.ID;
    }
}
