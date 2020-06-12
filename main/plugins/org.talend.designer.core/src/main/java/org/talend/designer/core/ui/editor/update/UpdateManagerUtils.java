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
package org.talend.designer.core.ui.editor.update;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.gef.commands.Command;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.PlatformUI;
import org.talend.commons.CommonsPlugin;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.LoginException;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.runtime.model.repository.ERepositoryStatus;
import org.talend.core.CorePlugin;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.context.JobContext;
import org.talend.core.model.context.JobContextManager;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.process.IContext;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.process.IProcess2;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.JobletProcessItem;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.update.EUpdateItemType;
import org.talend.core.model.update.IUpdateItemType;
import org.talend.core.model.update.RepositoryUpdateManager;
import org.talend.core.model.update.UpdateManagerHelper;
import org.talend.core.model.update.UpdateResult;
import org.talend.core.model.update.UpdatesConstants;
import org.talend.core.model.update.extension.UpdateManagerProviderDetector;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.ui.CoreUIPlugin;
import org.talend.core.ui.ITestContainerProviderService;
import org.talend.core.ui.component.ComponentPaletteUtilities;
import org.talend.core.ui.context.view.AbstractContextView;
import org.talend.core.ui.context.view.Contexts;
import org.talend.core.ui.process.IGEFProcess;
import org.talend.core.ui.services.IDesignerCoreUIService;
import org.talend.designer.core.DesignerPlugin;
import org.talend.designer.core.IDesignerCoreService;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;
import org.talend.designer.core.ui.AbstractMultiPageTalendEditor;
import org.talend.designer.core.ui.editor.AbstractTalendEditor;
import org.talend.designer.core.ui.editor.process.Process;
import org.talend.designer.core.ui.editor.update.cmd.UpdateContextParameterCommand;
import org.talend.designer.core.ui.editor.update.cmd.UpdateJobletNodeCommand;
import org.talend.designer.core.ui.editor.update.cmd.UpdateJunitNodeCommand;
import org.talend.designer.core.ui.editor.update.cmd.UpdateMainParameterCommand;
import org.talend.designer.core.ui.editor.update.cmd.UpdateNodeParameterCommand;
import org.talend.designer.core.ui.editor.update.cmd.UpdateNodePathCommand;
import org.talend.designer.core.ui.views.jobsettings.JobSettings;
import org.talend.designer.core.ui.views.jobsettings.JobSettingsView;
import org.talend.designer.core.ui.views.properties.ComponentSettings;
import org.talend.designer.core.ui.views.properties.ComponentSettingsView;
import org.talend.designer.joblet.model.JobletProcess;
import org.talend.repository.RepositoryPlugin;
import org.talend.repository.RepositoryWorkUnit;

/**
 * ggu class global comment. Detailled comment
 */
public final class UpdateManagerUtils {

    /**
     *
     * used for get repository id and child name, such as "xxxxxxxxxxx - metadata".
     */
    public static String[] getSourceIdAndChildName(final String idAndName) {
        if (idAndName == null) {
            return null;
        }
        String[] result = idAndName.split(UpdatesConstants.SEGMENT_LINE);
        if (result.length == 2) {
            return result;
        }
        return null;

    }

    public static IComponent getComponent(Process process, final String name) {
        if (name != null) {
            AbstractMultiPageTalendEditor editor = (AbstractMultiPageTalendEditor) process.getEditor();
            if (editor != null) {
                AbstractTalendEditor talendEditor = editor.getTalendEditor();
                if (talendEditor != null) {
                    return talendEditor.getComponent(name);
                }
            }
        }
        return null;
    }

    /**
     * qzhang Comment method "getNewOutputTableForConnection".
     *
     * @param newOutputTableList
     * @param attachedConnector
     * @return
     */
    public static IMetadataTable getNewOutputTableForConnection(List<IMetadataTable> newOutputTableList, String tableName) {
        for (IMetadataTable metadataTable : newOutputTableList) {
            if (tableName != null && tableName.equals(metadataTable.getTableName())
                    || tableName.equals(metadataTable.getTableName())) {
                return metadataTable;
            }
        }
        return null;
    }

    /**
     * qzhang Comment method "getNewInputTableForConnection".
     *
     * @param newInputTableList
     * @param connector
     *
     * @return
     */
    public static IMetadataTable getNewInputTableForConnection(List<IMetadataTable> newInputTableList, String connector) {
        for (IMetadataTable metadataTable : newInputTableList) {
            if (connector != null
                    && (connector.equals(metadataTable.getAttachedConnector()) || connector.equals(metadataTable.getTableName()))) {
                return metadataTable;
            }
        }
        return null;
    }

    /**
     * qzhang Comment method "getElemParam".
     *
     * @param elemParams
     * @param string
     *
     * @return
     */
    public static IElementParameter getElemParam(List<IElementParameter> elemParams, String string) {
        for (IElementParameter elementParameter : elemParams) {
            if (string != null && string.equals(elementParameter.getContext())) {
                return elementParameter;
            }
        }
        return null;
    }

    public static String addBrackets(String value) {
        return UpdateManagerHelper.addBrackets(value);
    }

    public static List<IProcess2> getOpenedProcess() {
        if (CommonsPlugin.isHeadless() || !ProxyRepositoryFactory.getInstance().isFullLogonFinished()) {
            return Collections.EMPTY_LIST;
        }
        IEditorReference[] reference = null;
        if (PlatformUI.getWorkbench() != null && PlatformUI.getWorkbench().getActiveWorkbenchWindow() != null
                && PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage() != null) {
            reference = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getEditorReferences();
        } else if (PlatformUI.getWorkbench() != null && PlatformUI.getWorkbench().getWorkbenchWindows().length > 0) {
            if (PlatformUI.getWorkbench().getWorkbenchWindows()[0].getActivePage() != null) {
                reference = PlatformUI.getWorkbench().getWorkbenchWindows()[0].getActivePage().getEditorReferences();
            }
        }

        if (reference != null) {
            List<IProcess2> processes = RepositoryPlugin.getDefault().getDesignerCoreService().getOpenedProcess(reference);
            if (processes != null) {
                return processes;
            }
        }
        return Collections.emptyList();
    }

    public static boolean executeUpdates(final List<UpdateResult> results, final boolean updateAllJobs) {
        return executeUpdates(results, false, updateAllJobs);
    }

    public static boolean executeUpdates(final List<UpdateResult> results, final boolean onlySimpleShow,
            final boolean updateAllJobs) {
        return executeUpdates(results, onlySimpleShow, updateAllJobs, false);
    }

    @SuppressWarnings("unchecked")
    public static boolean executeUpdates(final List<UpdateResult> results, final IProcess2 currentProcess) {
        RepositoryWorkUnit<Boolean> repositoryWorkUnit = new RepositoryWorkUnit<Boolean>(
                Messages.getString("UpdateManagerUtils.updateMOfification")) { //$NON-NLS-1$

            @Override
            protected void run() throws LoginException, PersistenceException {
                result = doExecuteUpdates(results, true);
            }

        };
        repositoryWorkUnit.setAvoidUnloadResources(true);
        ProxyRepositoryFactory.getInstance().executeRepositoryWorkUnit(repositoryWorkUnit);
        return repositoryWorkUnit.getResult();
    }

    @SuppressWarnings("unchecked")
    public static boolean executeUpdates(final List<UpdateResult> results, final boolean onlySimpleShow,
            final boolean updateAllJobs, final boolean noRepositoryWorkUnit) {
        return executeUpdates(results, onlySimpleShow, updateAllJobs, noRepositoryWorkUnit, false);
    }

    @SuppressWarnings("unchecked")
    public static boolean executeUpdates(final List<UpdateResult> results, final boolean onlySimpleShow,
            final boolean updateAllJobs, final boolean noRepositoryWorkUnit, final boolean executeAllWithoutChoose) {
        if (noRepositoryWorkUnit) {
            // RWU means also SVN Access, maybe in some cases to optimize,
            // we want to avoid SVN Access to optimize since we don't really save any file
            return doExecuteUpdates(results, onlySimpleShow, updateAllJobs, executeAllWithoutChoose);
        } else {
            RepositoryWorkUnit<Boolean> repositoryWorkUnit = new RepositoryWorkUnit<Boolean>(
                    Messages.getString("UpdateManagerUtils.updateMOfification")) { //$NON-NLS-1$

                @Override
                protected void run() throws LoginException, PersistenceException {
                    result = doExecuteUpdates(results, onlySimpleShow, updateAllJobs, executeAllWithoutChoose);
                }

            };
            repositoryWorkUnit.setAvoidUnloadResources(true);
            ProxyRepositoryFactory.getInstance().executeRepositoryWorkUnit(repositoryWorkUnit);
            return repositoryWorkUnit.getResult();
        }
    }

    private static boolean doExecuteUpdates(final List<UpdateResult> results, boolean onlySimpleShow, final boolean updateAllJobs,
            final boolean executeAllWithoutChoose) {
        if (results == null || results.isEmpty()) {
            return false;
        }
        try {
            if (CommonsPlugin.isHeadless() || !ProxyRepositoryFactory.getInstance().isFullLogonFinished()) {
                doExecuteUpdates(results, updateAllJobs);
            } else {
                if (executeAllWithoutChoose) {
                    return doExecuteUpdates(results, updateAllJobs, true);
                }
                // changed by hqzhang, Display.getCurrent().getActiveShell() may cause studio freeze
                UpdateDetectionDialog checkDialog = new UpdateDetectionDialog(
                        PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), results, onlySimpleShow);

                if (checkDialog.open() == IDialogConstants.OK_ID) {
                    doExecuteBuiltIn(checkDialog.getBuiltInElements(), updateAllJobs, false);
                    return doExecuteUpdates(checkDialog.getSelectedElements(), updateAllJobs);
                }
            }
        } finally {
            results.clear();
        }
        return false;
    }

    private static boolean doExecuteUpdates(final List<UpdateResult> results, final boolean updateAllJobs) {
        return doExecuteUpdates(results, updateAllJobs, false);
    }

    private static boolean doExecuteUpdates(final List<UpdateResult> results, final boolean updateAllJobs,
            final boolean executeWithoutShow) {
        if (results == null || results.isEmpty()) {
            return false;
        }
        try {
            IWorkspaceRunnable op = new IWorkspaceRunnable() {

                @Override
                public void run(IProgressMonitor monitor) throws CoreException {
                    monitor.setCanceled(false);
                    int size = (results.size() * 2 + 1) * UpdatesConstants.SCALE;
                    monitor.beginTask(Messages.getString("UpdateManagerUtils.Update"), size); //$NON-NLS-1$

                    ProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();

                    // first list by job we need to update

                    Map<String, Set<String>> jobIdToVersion = new HashMap<String, Set<String>>();
                    Map<String, Boolean> jobIdClosed = new HashMap<String, Boolean>();

                    for (UpdateResult result : results) {
                        // if (!result.isChecked()) {
                        // continue;
                        // }
                        String id = result.getObjectId();
                        String version = result.getObjectVersion();
                        if (id == null) {
                            if (result.getJob() != null && result.getJob() instanceof IProcess) {
                                IProcess process = (IProcess) result.getJob();
                                if (process instanceof IProcess2
                                        && ERepositoryStatus.LOCK_BY_OTHER.equals(factory.getStatus(((IProcess2) process)
                                                .getProperty().getItem()))) {
                                    // if item is locked by another user, don't do anything, or it might corrupt the
                                    // file.
                                    continue;

                                }
                                id = process.getId();
                                version = process.getVersion();
                                result.setObjectId(id);
                                result.setObjectVersion(version);
                            } else {
                                continue;
                            }
                        }
                        Set<String> versionList;
                        if (!jobIdToVersion.containsKey(id)) {
                            versionList = new HashSet<String>();
                            jobIdToVersion.put(id, versionList);
                        } else {
                            versionList = jobIdToVersion.get(id);
                        }
                        versionList.add(version);
                        jobIdClosed.put(id + " - " + version, result.isFromItem()); //$NON-NLS-1$
                    }

                    // now will execute updates only for the job selected depends this list.
                    for (String currentId : jobIdToVersion.keySet()) {

                        for (String version : jobIdToVersion.get(currentId)) {
                            IRepositoryViewObject currentObj = null;
                            boolean closedItem = jobIdClosed.get(currentId + " - " + version); //$NON-NLS-1$

                            IProcess process = null;
                            Item item = null;

                            if (closedItem) {
                                // if item is closed, then just load it.
                                boolean checkOnlyLastVersion = Boolean.parseBoolean(DesignerPlugin.getDefault()
                                        .getPreferenceStore().getString("checkOnlyLastVersion")); //$NON-NLS-1$
                                try {
                                    if (checkOnlyLastVersion || version == null) {
                                        currentObj = factory.getLastVersion(currentId);
                                    } else {
                                        List<IRepositoryViewObject> allVersion = factory.getAllVersion(currentId);
                                        for (IRepositoryViewObject obj : allVersion) {
                                            if (obj.getVersion().equals(version)) {
                                                currentObj = obj;
                                            }
                                        }

                                    }
                                } catch (PersistenceException e) {
                                    ExceptionHandler.process(e);
                                }

                                if (currentObj == null) {
                                    // item not found, don't do anything
                                    continue;
                                }
                                item = currentObj.getProperty().getItem();
                                IDesignerCoreService designerCoreService = CorePlugin.getDefault().getDesignerCoreService();
                                if (item instanceof ProcessItem) {
                                    process = designerCoreService.getProcessFromProcessItem((ProcessItem) item);
                                } else if (item instanceof JobletProcessItem) {
                                    process = designerCoreService.getProcessFromJobletProcessItem((JobletProcessItem) item);
                                }
                            }

                            for (UpdateResult result : results) {
                                // if (!result.isChecked()) {
                                // continue;
                                // }
                                if (!StringUtils.equals(currentId, result.getObjectId())) {
                                    continue; // not the current job we need to update
                                }
                                if (closedItem) {
                                    if (result.getJob() == null) {
                                        result.setJob(process);
                                    } else {
                                        process = (IProcess) result.getJob();
                                    }
                                    if (result.getRepositoryUpdateManager() != null) {
                                        Map<IContext, String> renameContextGroup = result.getRepositoryUpdateManager().getRenameContextGroup();
                                        if (renameContextGroup != null && !renameContextGroup.isEmpty()) {
                                            if (process.getContextManager() instanceof JobContextManager) {
                                                ((JobContextManager) process.getContextManager()).setRenameGroupContext(renameContextGroup);
                                            }
                                        }
                                    }
                                    IUpdateItemType jobletContextType = UpdateManagerProviderDetector.INSTANCE
                                            .getUpdateItemType(UpdateManagerHelper.TYPE_JOBLET_CONTEXT);
                                    // JunitConextUpdateManagerProvider#retrieveUpdateResults added JOB_CONTEXT result
                                    // JunitConextUpdateManagerProvider#doUpdate said have update in check
                                    IUpdateItemType testcaseContextType = UpdateManagerProviderDetector.INSTANCE
                                            .getUpdateItemType(UpdateManagerHelper.TYPE_JOB_CONTEXT);
                                    if (process != null
                                            && (jobletContextType != null && jobletContextType.equals(result.getUpdateType())
                                                    || testcaseContextType != null
                                                            && testcaseContextType.equals(result.getUpdateType()))) {
                                        if ((result.getParameter() instanceof List) && process.getContextManager() != null) {
                                            process.getContextManager().setListContext((List<IContext>) result.getParameter());
                                        }
                                    }
                                }
                                // execute
                                executeUpdate(result, monitor, updateAllJobs);

                                if (closedItem) {
                                    result.setJob(null);
                                }
                            }
                            boolean isTestContainer = false;
                            ITestContainerProviderService testContainerService = null;
                            if (GlobalServiceRegister.getDefault().isServiceRegistered(ITestContainerProviderService.class)) {
                                testContainerService = (ITestContainerProviderService) GlobalServiceRegister
                                        .getDefault().getService(ITestContainerProviderService.class);
                                if (testContainerService != null ) {
                                    isTestContainer = testContainerService.isTestContainerItem(item);
                                }
                            }

                            if (closedItem && process instanceof IProcess2) {
                                IProcess2 process2 = (IProcess2) process;
                                ProcessType processType;
                                try {
                                    processType = process2.saveXmlFile(false);
                                    if(isTestContainer){
                                        testContainerService.setTestContainerProcess(processType,item);
                                    }else if (item instanceof JobletProcessItem) {
                                        ((JobletProcessItem) item).setJobletProcess((JobletProcess) processType);
                                    } else {
                                        ((ProcessItem) item).setProcess(processType);
                                    }
                                    factory.save(item, true);
                                } catch (IOException e) {
                                    ExceptionHandler.process(e);
                                } catch (PersistenceException e) {
                                    ExceptionHandler.process(e);
                                }
                            }

                            if (closedItem && !ERepositoryStatus.LOCK_BY_USER.equals(factory.getStatus(item))) {
                                // unload item from memory, but only if this one is not locked by current user.
                                try {
                                    factory.unloadResources(item.getProperty());
                                } catch (PersistenceException e) {
                                    ExceptionHandler.process(e);
                                }
                            }
                        }
                    }

                    UpdateManagerProviderDetector.INSTANCE.postUpdate(results);

                    // update joblet reference
                    upadateJobletReferenceInfor();

                    if (!CommonsPlugin.isHeadless() && ProxyRepositoryFactory.getInstance().isFullLogonFinished()) {
                        final List<UpdateResult> tempResults = new ArrayList<UpdateResult>(results);
                        // refresh
                        Display.getDefault().asyncExec(new Runnable() {

                            @Override
                            public void run() {
                                refreshRelatedViewers(tempResults);

                                // hyWang add method checkandRefreshProcess for bug7248
                                checkandRefreshProcess(tempResults);
                            }
                        });
                    }


                    monitor.worked(1 * UpdatesConstants.SCALE);
                    monitor.done();
                }
            };

            final IRunnableWithProgress iRunnableWithProgress = new IRunnableWithProgress() {

                @Override
                public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                    IWorkspace workspace = ResourcesPlugin.getWorkspace();
                    try {
                        ISchedulingRule refreshRule = workspace.getRuleFactory().refreshRule(workspace.getRoot());
                        workspace.run(op, refreshRule, IWorkspace.AVOID_UPDATE, monitor);
                    } catch (CoreException e) {
                        throw new InvocationTargetException(e);
                    }
                }
            };
            try {
                if (!CommonsPlugin.isHeadless() && ProxyRepositoryFactory.getInstance().isFullLogonFinished()
                        && !executeWithoutShow) {
                    new ProgressMonitorDialog(null).run(false, false, iRunnableWithProgress);
                } else {
                    iRunnableWithProgress.run(new NullProgressMonitor());
                }
            } catch (InvocationTargetException e) {
                ExceptionHandler.process(e);
            } catch (InterruptedException e) {
            }
            return !results.isEmpty();
        } finally {
            results.clear();
        }
    }

    private static void doExecuteBuiltIn(final List<UpdateResult> unChecked, final boolean updateAllJobs,
            final boolean executeWithoutShow) {
        if (unChecked == null || unChecked.isEmpty()) {
            return;
        }
        final IWorkspaceRunnable op = new IWorkspaceRunnable() {

            @Override
            public void run(IProgressMonitor monitor) throws CoreException {
                monitor.setCanceled(false);
                int size = (unChecked.size() * 2 + 1) * UpdatesConstants.SCALE;
                monitor.beginTask(Messages.getString("UpdateManagerUtils.Update"), size); //$NON-NLS-1$

                ProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();

                // first list by job we need to update

                Map<String, Set<String>> jobIdToVersion = new HashMap<String, Set<String>>();
                Map<String, Boolean> jobIdClosed = new HashMap<String, Boolean>();

                for (UpdateResult result : unChecked) {
                    String id = result.getObjectId();
                    String version = result.getObjectVersion();
                    if (id == null) {
                        if (result.getJob() != null && result.getJob() instanceof IProcess) {
                            IProcess process = (IProcess) result.getJob();
                            if (process instanceof IProcess2
                                    && ERepositoryStatus.LOCK_BY_OTHER.equals(factory.getStatus(((IProcess2) process)
                                            .getProperty().getItem()))) {
                                // if item is locked by another user, don't do anything, or it might corrupt the
                                // file.
                                continue;

                            }
                            id = process.getId();
                            version = process.getVersion();
                            result.setObjectId(id);
                            result.setObjectVersion(version);
                        } else {
                            continue;
                        }
                    }
                    Set<String> versionList;
                    if (!jobIdToVersion.containsKey(id)) {
                        versionList = new HashSet<String>();
                        jobIdToVersion.put(id, versionList);
                    } else {
                        versionList = jobIdToVersion.get(id);
                    }
                    versionList.add(version);
                    jobIdClosed.put(id + " - " + version, result.isFromItem()); //$NON-NLS-1$
                }

                // now will execute updates only for the job selected depends this list.
                for (String currentId : jobIdToVersion.keySet()) {

                    for (String version : jobIdToVersion.get(currentId)) {
                        IRepositoryViewObject currentObj = null;
                        boolean closedItem = jobIdClosed.get(currentId + " - " + version); //$NON-NLS-1$

                        IProcess process = null;
                        Item item = null;

                        if (closedItem) {
                            // if item is closed, then just load it.
                            boolean checkOnlyLastVersion = Boolean.parseBoolean(DesignerPlugin.getDefault()
                                    .getPreferenceStore().getString("checkOnlyLastVersion")); //$NON-NLS-1$
                            try {
                                if (checkOnlyLastVersion || version == null) {
                                    currentObj = factory.getLastVersion(currentId);
                                } else {
                                    List<IRepositoryViewObject> allVersion = factory.getAllVersion(currentId);
                                    for (IRepositoryViewObject obj : allVersion) {
                                        if (obj.getVersion().equals(version)) {
                                            currentObj = obj;
                                        }
                                    }

                                }
                            } catch (PersistenceException e) {
                                ExceptionHandler.process(e);
                            }

                            if (currentObj == null) {
                                // item not found, don't do anything
                                continue;
                            }
                            item = currentObj.getProperty().getItem();
                            IDesignerCoreService designerCoreService = CorePlugin.getDefault().getDesignerCoreService();
                            if (item instanceof ProcessItem) {
                                process = designerCoreService.getProcessFromProcessItem((ProcessItem) item);
                            } else if (item instanceof JobletProcessItem) {
                                process = designerCoreService.getProcessFromJobletProcessItem((JobletProcessItem) item);
                            }
                        }

                        for (UpdateResult result : unChecked) {
                            if (!StringUtils.equals(currentId, result.getObjectId())) {
                                continue; // not the current job we need to update
                            }
                            if (closedItem) {
                                if (result.getJob() == null) {
                                    result.setJob(process);
                                } else {
                                    process = (IProcess) result.getJob();
                                }
                            }
                            // execute
                            executeUpdate(result, monitor, updateAllJobs);

                            if (closedItem) {
                                result.setJob(null);
                            }
                        }
                        boolean isTestContainer = false;
                        ITestContainerProviderService testContainerService = null;
                        if (GlobalServiceRegister.getDefault().isServiceRegistered(ITestContainerProviderService.class)) {
                            testContainerService = (ITestContainerProviderService) GlobalServiceRegister
                                    .getDefault().getService(ITestContainerProviderService.class);
                            if (testContainerService != null ) {
                                isTestContainer = testContainerService.isTestContainerItem(item);
                            }
                        }

                        if (closedItem && process instanceof IProcess2) {
                            IProcess2 process2 = (IProcess2) process;
                            ProcessType processType;
                            try {
                                processType = process2.saveXmlFile(false);
                                if(isTestContainer){
                                    testContainerService.setTestContainerProcess(processType,item);
                                }else if (item instanceof JobletProcessItem) {
                                    ((JobletProcessItem) item).setJobletProcess((JobletProcess) processType);
                                } else {
                                    ((ProcessItem) item).setProcess(processType);
                                }
                                factory.save(item);
                            } catch (IOException e) {
                                ExceptionHandler.process(e);
                            } catch (PersistenceException e) {
                                ExceptionHandler.process(e);
                            }
                        }

                        if (closedItem && !ERepositoryStatus.LOCK_BY_USER.equals(factory.getStatus(item))) {
                            // unload item from memory, but only if this one is not locked by current user.
                            try {
                                factory.unloadResources(item.getProperty());
                            } catch (PersistenceException e) {
                                ExceptionHandler.process(e);
                            }
                        }
                    }
                }

                UpdateManagerProviderDetector.INSTANCE.postUpdate(unChecked);

                if (!CommonsPlugin.isHeadless() && ProxyRepositoryFactory.getInstance().isFullLogonFinished()) {
                    final List<UpdateResult> tempResults = new ArrayList<UpdateResult>(unChecked);
                    // refresh
                    Display.getDefault().asyncExec(new Runnable() {

                        @Override
                        public void run() {
                            refreshRelatedViewers(tempResults);

                            // hyWang add method checkandRefreshProcess for bug7248
                            checkandRefreshProcess(tempResults);
                        }
                    });
                }


                monitor.worked(1 * UpdatesConstants.SCALE);
                monitor.done();
            }
        };

        final IRunnableWithProgress iRunnableWithProgress = new IRunnableWithProgress() {

            @Override
            public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                IWorkspace workspace = ResourcesPlugin.getWorkspace();
                try {
                    ISchedulingRule refreshRule = workspace.getRuleFactory().refreshRule(workspace.getRoot());
                    workspace.run(op, refreshRule, IWorkspace.AVOID_UPDATE, monitor);
                } catch (CoreException e) {
                    throw new InvocationTargetException(e);
                }
            }
        };

        try {
            if (!CommonsPlugin.isHeadless() && ProxyRepositoryFactory.getInstance().isFullLogonFinished()
                    && !executeWithoutShow) {
                new ProgressMonitorDialog(null).run(false, false, iRunnableWithProgress);
            } else {
                iRunnableWithProgress.run(new NullProgressMonitor());
            }
        } catch (InvocationTargetException e) {
            ExceptionHandler.process(e);
        } catch (InterruptedException e) {
        }finally {
            unChecked.clear();
        }
    }

    /**
     *
     * ggu Comment method "refreshViewers".
     */
    @SuppressWarnings("unchecked")
    private static void refreshRelatedViewers(List results) {
        boolean context = false;
        boolean jobSetting = false;
        boolean componentSettings = false;
        boolean palette = false;

        for (UpdateResult result : (List<UpdateResult>) results) {
            // if (!result.isChecked()) {
            // continue;
            // }
            IUpdateItemType updateType = result.getUpdateType();

            if (updateType != null) {
                if (EUpdateItemType.CONTEXT.equals(updateType)) {
                    if (result.isJoblet() && !result.isChecked()) {
                        continue;
                    }
                    context = true;
                } else if (EUpdateItemType.JOB_PROPERTY_EXTRA.equals(updateType)
                        || EUpdateItemType.JOB_PROPERTY_STATS_LOGS.equals(updateType)
                        || EUpdateItemType.JOB_PROPERTY_HEADERFOOTER.equals(updateType)) {
                    jobSetting = true;
                } else if (EUpdateItemType.NODE_PROPERTY.equals(updateType) || EUpdateItemType.NODE_QUERY.equals(updateType)
                        || EUpdateItemType.NODE_SCHEMA.equals(updateType)) {
                    componentSettings = true;
                } else if (EUpdateItemType.RELOAD.equals(updateType) || EUpdateItemType.JOBLET_RENAMED.equals(updateType)
                        || EUpdateItemType.JOBLET_SCHEMA.equals(updateType)) {
                    if (result.isJoblet() && !result.isChecked()) {
                        continue;
                    }
                    palette = true;
                }
            }
        }
        // recheck from the providers
        Set<String> viewIds = UpdateManagerProviderDetector.INSTANCE.needRefreshRelatedViews(results);
        // context
        if (!context) {
            context = viewIds.contains(AbstractContextView.CTX_ID_DESIGNER);
        }
        if (context) {
            Contexts.switchToCurContextsView();
        }
        // jobsetting
        if (!jobSetting) {
            jobSetting = viewIds.contains(JobSettingsView.ID);
        }
        if (jobSetting) {
            JobSettings.switchToCurJobSettingsView();
        }
        // component setting
        if (!componentSettings) {
            componentSettings = viewIds.contains(ComponentSettingsView.ID);
        }
        if (componentSettings) {
            ComponentSettings.switchToCurComponentSettingsView();
        }
        // palette
        // if (!palette) {
        // need find the palette id to refresh.
        // palette=viewIds.contains("???");
        // }
        if (palette) {
            ComponentPaletteUtilities.updatePalette();
        }
    }

    private static void executeUpdate(UpdateResult result, IProgressMonitor monitor, boolean updateAllJobs) {
        if (result.isReadOnlyProcess()) {
            return;
        }

        // update
        Command command = null;
        IUpdateItemType updateType = result.getUpdateType();
        if (updateType instanceof EUpdateItemType) {
            switch ((EUpdateItemType) updateType) {
            case NODE_PROPERTY:
            case NODE_SCHEMA:
            case NODE_QUERY:
            case NODE_SAP_IDOC:
            case NODE_SAP_FUNCTION:
            case NODE_VALIDATION_RULE:
                command = new UpdateNodeParameterCommand(result);
                break;
            case JOB_PROPERTY_EXTRA:
            case JOB_PROPERTY_STATS_LOGS:
            case JOB_PROPERTY_HEADERFOOTER:
            case JOB_PROPERTY_STORM:
            case JOB_PROPERTY_MAPREDUCE:
                command = new UpdateMainParameterCommand(result);
                break;
            case CONTEXT:
                command = executeContextUpdates(result);
                break;
            case CONTEXT_GROUP:
                command = executeContextGroupUpdates(result);
                break;
            case JOBLET_RENAMED:
            case JOBLET_SCHEMA:
            case RELOAD:
                command = executeJobletNodesUpdates(result);
                break;
            case JUNIT_RELOAD:
                command = executeJunitNodesUpdates(result);
                break;
            // case JOBLET_CONTEXT:
            // command = new Command() { // have update in checking.
            // };
            // break;
            case MAP_PATH:
                command = new UpdateNodePathCommand(result);
                break;
            default:
                break;
            }
        }
        if (command != null) {
            SubProgressMonitor subMonitor = new SubProgressMonitor(monitor, 1 * UpdatesConstants.SCALE,
                    SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK);
            subMonitor.beginTask(UpdatesConstants.EMPTY, 1);
            subMonitor.subTask(getResultTaskInfor(result));
            //
            Object job = result.getJob();
            boolean executed = false;
            if (job != null && job instanceof IGEFProcess) {
                IDesignerCoreUIService designerCoreUIService = CoreUIPlugin.getDefault().getDesignerCoreUIService();
                if (designerCoreUIService != null) {
                    executed = designerCoreUIService.executeCommand((IGEFProcess) job, command);
                }
            }
            if (!executed) {
                command.execute();
            }

            subMonitor.worked(1);

        }
        // from extension point
        UpdateManagerProviderDetector.INSTANCE.doUpdate(monitor, result);

    }

    private static String getResultTaskInfor(UpdateResult result) {
        return UpdateManagerHelper.getResultTaskInfor(result);
    }

    /*
     * context
     */
    @SuppressWarnings("unchecked")
    private static Command executeContextUpdates(UpdateResult result) {
        if (result == null) {
            return null;
        }
        Object object = result.getUpdateObject();
        if (object instanceof Set) {
            return new UpdateContextParameterCommand(result);
        }
        return null;
    }

    /*
     * contextGroup
     */
    @SuppressWarnings("unchecked")
    private static Command executeContextGroupUpdates(UpdateResult result) {
        if (result == null) {
            return null;
        }
        Object object = result.getUpdateObject();
        if (object instanceof JobContext) {
            return new UpdateContextParameterCommand(result);
        }
        return null;
    }

    /*
     * joblet
     */
    private static Command executeJobletNodesUpdates(UpdateResult result) {
        if (result == null) {
            return null;
        }
        Object parameter = result.getParameter();
        if (parameter != null) {
            return new UpdateJobletNodeCommand(result);
        }
        return null;
    }

    /*
     * junit
     */
    private static Command executeJunitNodesUpdates(UpdateResult result) {
        if (result == null) {
            return null;
        }
        Object parameter = result.getParameter();
        if (parameter != null) {
            return new UpdateJunitNodeCommand(result);
        }
        return null;
    }

    private static void upadateJobletReferenceInfor() {
        List<IProcess2> openedProcessList = CorePlugin.getDefault().getDesignerCoreService()
                .getOpenedProcess(RepositoryUpdateManager.getEditors());

        for (IProcess2 proc : openedProcessList) {
            proc.getUpdateManager().retrieveRefInformation();
        }
    }

    /**
     * DOC hyWang Comment method "checkandRefreshProcess".
     *
     * @param results
     */
    private static void checkandRefreshProcess(final List<UpdateResult> results) {
        for (UpdateResult result : results) {
            if (result.isJoblet() && !result.isChecked()) {
                continue;
            }
            if (result.getJob() instanceof IProcess2) {
                IProcess2 process = (IProcess2) result.getJob();
                process.checkProcess();
            }
        }
    }
}
