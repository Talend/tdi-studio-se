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
package org.talend.designer.core.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.talend.core.CorePlugin;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.process.IProcess2;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.ui.component.ComponentPaletteUtilities;
import org.talend.core.ui.context.view.Contexts;
import org.talend.core.ui.services.ISQLBuilderService;
import org.talend.designer.business.diagram.custom.IDiagramModelService;
import org.talend.designer.core.DesignerPlugin;
import org.talend.designer.core.ui.editor.process.Process;
import org.talend.designer.core.ui.views.CodeView;
import org.talend.designer.core.ui.views.jobsettings.JobSettings;
import org.talend.designer.core.ui.views.problems.Problems;
import org.talend.designer.runprocess.IRunProcessService;

/**
 * Track the active Process being edited. <br/>
 *
 * $Id$
 *
 */
public class ActiveProcessTracker implements IPartListener {

    private static ActiveProcessTracker apt = null;

    private static final String DISTARTID = "org.talend.rcp.intro.starting.StartingBrowser";

    private static final String MDMSTARTID = "org.talend.mdm.repository.ui.starting.editor.MDMStartingEditor";

    public static void initialize() {
        if (apt == null) {
            apt = new ActiveProcessTracker();
            PlatformUI.getWorkbench().getActiveWorkbenchWindow().getPartService().addPartListener(apt);
        }
    }

    private static IProcess2 currentProcess;

    private static IProcess2 lastProcessOpened;

    private static boolean changedProcess;

    private static List<IJobTrackerListener> jobTrackerListeners = new ArrayList<IJobTrackerListener>();

    public static void addJobTrackerListener(IJobTrackerListener listener) {
        if (currentProcess != null) {
            listener.focusOnJob(currentProcess);
        }
        jobTrackerListeners.add(listener);
    }

    public static void removeJobTrackerListener(IJobTrackerListener listener) {
        jobTrackerListeners.remove(listener);
        listener.allJobClosed();
    }

    public IProcess2 getJobFromActivatedEditor(IWorkbenchPart part) {
        IWorkbenchPart testedPart = part;
        if (!(part instanceof AbstractMultiPageTalendEditor)) {
            testedPart = part.getSite().getWorkbenchWindow().getActivePage().getActiveEditor();
        }

        if (testedPart instanceof AbstractMultiPageTalendEditor) {
            AbstractMultiPageTalendEditor mpte = (AbstractMultiPageTalendEditor) testedPart;
            mpte.setName();
            IProcess2 process = mpte.getTalendEditor().getProcess();
            return process;
        } else {
            // No editor
        }
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ui.IPartListener#partActivated(org.eclipse.ui.IWorkbenchPart)
     */
    @Override
    public void partActivated(final IWorkbenchPart part) {
        if (changedProcess) {
            if (part instanceof AbstractMultiPageTalendEditor) {
                AbstractMultiPageTalendEditor multiPageTEditor = (AbstractMultiPageTalendEditor) part;
                multiPageTEditor.changePaletteComponentHandler();
                ComponentPaletteUtilities.updateFromRepositoryType(ERepositoryObjectType
                        .getItemType(((AbstractMultiPageTalendEditor) part).getProcess().getProperty().getItem()));
                changedProcess = false;
            }
        }

    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ui.IPartListener#partBroughtToTop(org.eclipse.ui.IWorkbenchPart)
     */
    @Override
    public void partBroughtToTop(IWorkbenchPart part) {
        IProcess2 process = getJobFromActivatedEditor(part);
        if (process != null && currentProcess != process) {
            changedProcess = true;
            currentProcess = process;
            Contexts.setTitle(process.getLabel());
            setContextsView(process, part);
            // setStatsAndLogsView(process);
            JobSettings.switchToCurJobSettingsView();

            refreshProcessView();

            for (IJobTrackerListener listener : jobTrackerListeners) {
                listener.focusOnJob(process);
            }

            // if (process instanceof Process) {
            // Process p = (Process) process;
            // if (!p.isReadOnly() && p.isActivate()) {
            // p.checkDifferenceWithRepository();
            // }
            // }
        }

    }

    private void refreshProcessView() {
        DesignerPlugin.getDefault().getRunProcessService().refreshView();
    }

    /**
     * ftang Comment method "setStatsAndLogsView".
     *
     * @param process
     */
    // private void setStatsAndLogsView(IProcess process) {
    // StatsAndLogs.setTitle("Job " + process.getProperty().getLabel()); //$NON-NLS-1$
    // StatsAndLogs.switchToCurStatsAndLogsView();
    // }
    /**
     * qzhang Comment method "setProblemsView".
     *
     * @param process
     */
    private void addJobInProblemView(IProcess2 process) {
        Problems.addProcess(process);

        IRunProcessService service = DesignerPlugin.getDefault().getRunProcessService();
        service.setActiveProcess(process);
    }

    /**
     * qzhang Comment method "setProblemsView".
     *
     * @param process
     */
    private void setContextsView(IProcess2 process, IWorkbenchPart part) {

        IRunProcessService service = DesignerPlugin.getDefault().getRunProcessService();
        service.setActiveProcess(process, true);

        // Contexts.setTitle("Job " + process.getProperty().getLabel()); //$NON-NLS-1$
        Contexts.switchToCurContextsView(part);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ui.IPartListener#partClosed(org.eclipse.ui.IWorkbenchPart)
     */
    @Override
    public void partClosed(IWorkbenchPart part) {

        if (part instanceof AbstractMultiPageTalendEditor && currentProcess != null) {
            AbstractMultiPageTalendEditor mpte = (AbstractMultiPageTalendEditor) part;
            if (mpte.isKeepPropertyLocked()) {
                currentProcess = null;
                return;
            }

            IProcess process = getJobFromActivatedEditor(part);
            if (process != null) {
                Problems.removeProblemsByProcess(process);
                Problems.removeJob(process);
                IRunProcessService service = DesignerPlugin.getDefault().getRunProcessService();
                service.removeProcess(process);
                CodeView.refreshCodeView(null);

                if (currentProcess == process) {
                    Contexts.setTitle(""); //$NON-NLS-1$
                    Contexts.clearAll();
                    if (lastProcessOpened == currentProcess) {
                        lastProcessOpened = null;
                    }
                    currentProcess = null;
                    for (IJobTrackerListener listener : jobTrackerListeners) {
                        listener.allJobClosed();
                    }
                } else if (lastProcessOpened == process) {
                    lastProcessOpened = currentProcess;
                }
                if (GlobalServiceRegister.getDefault().isServiceRegistered(ISQLBuilderService.class)) {
                    ISQLBuilderService sqlBuilderService = (ISQLBuilderService) GlobalServiceRegister.getDefault().getService(
                            ISQLBuilderService.class);
                    sqlBuilderService.closeSqlBuilderDialogs(process.getName());
                }
            }
        } else if (part instanceof IEditorPart) {
            if (GlobalServiceRegister.getDefault().isServiceRegistered(IDiagramModelService.class)
                    && CorePlugin.getDefault().getDiagramModelService().isBusinessDiagramEditor((IEditorPart) part)) {
                Contexts.setTitle(""); //$NON-NLS-1$
                Contexts.clearAll();

            }

        }
        if (part instanceof AbstractMultiPageTalendEditor) {
            AbstractMultiPageTalendEditor mpte = (AbstractMultiPageTalendEditor) part;
            mpte.beforeDispose();
            JobSettings.switchToCurJobSettingsView();
        }

    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ui.IPartListener#partDeactivated(org.eclipse.ui.IWorkbenchPart)
     */
    @Override
    public void partDeactivated(IWorkbenchPart part) {
        IProcess process = getJobFromActivatedEditor(part);
        if (process != null && (part instanceof AbstractMultiPageTalendEditor)) {
            AbstractMultiPageTalendEditor mpte = (AbstractMultiPageTalendEditor) part;
            mpte.getTalendEditor().savePaletteState();
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ui.IPartListener#partOpened(org.eclipse.ui.IWorkbenchPart)
     */
    @Override
    public void partOpened(IWorkbenchPart part) {
        boolean existedJobOpened = false;
        if (part instanceof AbstractMultiPageTalendEditor) {
            AbstractMultiPageTalendEditor mpte = (AbstractMultiPageTalendEditor) part;
            if (mpte.isJobAlreadyOpened()) {
                mpte.updateChildrens();
                // close the first editor and keep the new one. (so only one will remain)
                IEditorReference[] ref = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
                        .findEditors(mpte.getEditorInput(), mpte.getEditorId(), IWorkbenchPage.MATCH_INPUT);
                IEditorPart editorPart = ref[0].getEditor(false);
                editorPart.doSave(new NullProgressMonitor());
                ((AbstractMultiPageTalendEditor) editorPart).setKeepPropertyLocked(true);
                PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().closeEditor(editorPart, false);
                existedJobOpened = true;
            }
        } else {
            if (GlobalServiceRegister.getDefault().isServiceRegistered(IDiagramModelService.class)) {
                CorePlugin.getDefault().getDiagramModelService().handleNewEditorAction(part);
            }
        }
        IProcess2 process = getJobFromActivatedEditor(part);
        if (part instanceof AbstractMultiPageTalendEditor && process instanceof Process) {
            ((Process) process).setEditor((AbstractMultiPageTalendEditor) part);
        }
        if (process != null && currentProcess != process && lastProcessOpened != process) {
            lastProcessOpened = process;
            addJobInProblemView(process);
        } else if (existedJobOpened) {
            currentProcess = process;
            for (IJobTrackerListener listener : jobTrackerListeners) {
                listener.focusOnJob(process);
            }
        }
    }

    /**
     * Getter for currentProcess.
     *
     * @return the currentProcess
     */
    public static Process getCurrentProcess() {
        return (Process) currentProcess;
    }
}
