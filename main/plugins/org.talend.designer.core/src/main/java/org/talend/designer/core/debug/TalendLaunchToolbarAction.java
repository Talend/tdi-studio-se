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
package org.talend.designer.core.debug;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationManager;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchHistory;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.actions.AbstractLaunchToolbarAction;
import org.eclipse.debug.ui.actions.LaunchAction;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.gmf.util.DisplayUtils;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.model.process.ProcessUtils;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryObject;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.repository.ui.editor.RepositoryEditorInput;
import org.talend.designer.core.DesignerPlugin;
import org.talend.designer.core.i18n.Messages;
import org.talend.repository.ProjectManager;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.RepositoryNode;

/**
 * DOC bqian class global comment. Detailled comment
 */
public class TalendLaunchToolbarAction extends AbstractLaunchToolbarAction {

    private IWorkbenchWindow window;

    private ISelection selection;

    private IProxyRepositoryFactory factory; // hywang for 10068

    private final static String RUN_LABEL = Messages.getString("MultiPageEditorContributor.runMenu"); //$NON-NLS-1$

    /**
     * DOC bqian TalendLaunchToolbarAction constructor comment.
     *
     * @param launchGroupIdentifier
     */
    public TalendLaunchToolbarAction() {
        super(IDebugUIConstants.ID_RUN_LAUNCH_GROUP);
    }

    /**
     * Fills the drop-down menu with favorites and launch history
     *
     * @param menu the menu to fill
     */
    @Override
    protected void fillMenu(Menu menu) {
        ILaunchConfiguration[] historyList = getHistory();
        ILaunchConfiguration[] favoriteList = getFavorites();

        // Add favorites
        int accelerator = 1;
        accelerator = addToMenu(menu, favoriteList, accelerator);

        // Separator between favorites and history
        if (favoriteList.length > 0 && historyList.length > 0) {
            addSeparator(menu);
        }

        // Add history launches next
        addToMenu(menu, historyList, accelerator);
    }

    private int addToMenu(Menu menu, ILaunchConfiguration[] launchList, int accelerator, IRepositoryObject... allVersion) {

        for (ILaunchConfiguration launch : launchList) {
            try {
                if (launch.getType().getIdentifier().equals(TalendDebugUIConstants.JOB_DEBUG_LAUNCH_CONFIGURATION_TYPE)
                        && isCurrentProject(launch)) {
                    if (checkItemExisted(launch, allVersion)) {
                        LaunchAction action = new LaunchAction(launch, getMode());
                        addToMenu(menu, action, accelerator);
                        accelerator++;
                    }
                }
            } catch (Exception e) {
                ExceptionHandler.process(e);
                continue;
            }
        }
        return accelerator;
    }

    private boolean checkItemExisted(ILaunchConfiguration launch, IRepositoryObject... allVersion) throws CoreException,
            PersistenceException {
        String jobId = launch.getAttribute(TalendDebugUIConstants.JOB_ID, (String) null);
        String jobVersion = launch.getAttribute(TalendDebugUIConstants.JOB_VERSION, (String) null);
        String jobProjectLabel = launch.getAttribute(TalendDebugUIConstants.JOB_PROJECT_TECH_LABEL, (String) null);
        if (jobId != null) {
            if (allVersion != null && allVersion.length > 0) {
                for (IRepositoryViewObject obj : allVersion) {
                    if (ProcessUtils.isInProject(jobProjectLabel, obj.getProperty()) && obj.getVersion().equals(jobVersion)) {
                        return true;
                    }
                }
            } else {
                List<IRepositoryViewObject> all = factory.getAllVersion(jobId);
                for (IRepositoryViewObject obj : all) {
                    if (ProcessUtils.isInProject(jobProjectLabel, obj.getProperty()) && obj.getProperty().getVersion().equals(jobVersion)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.debug.ui.actions.AbstractLaunchHistoryAction#init(org.eclipse.ui.IWorkbenchWindow)
     */
    @Override
    public void init(IWorkbenchWindow window) {
        super.init(window);
        this.window = window;
        this.factory = DesignerPlugin.getDefault().getRepositoryService().getProxyRepositoryFactory();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.debug.ui.actions.AbstractLaunchHistoryAction#selectionChanged(org.eclipse.jface.action.IAction,
     * org.eclipse.jface.viewers.ISelection)
     */
    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        super.selectionChanged(action, selection);
        this.selection = selection;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.debug.ui.actions.AbstractLaunchHistoryAction#getLastLaunch()
     */
    @Override
    protected ILaunchConfiguration getLastLaunch() {
        getLaunchConfigurationManager().removeLaunchHistoryListener(this);
        LaunchHistory history = getLaunchConfigurationManager().getLaunchHistory(getLaunchGroupIdentifier());
        getLaunchConfigurationManager().addLaunchHistoryListener(this);
        if (history != null) {
            try {
                ILaunchConfiguration[] filterConfigs = history.getCompleteLaunchHistory();
                for (ILaunchConfiguration launchConfiguration : filterConfigs) {
                    if (isCurrentProject(launchConfiguration) && checkItemExisted(launchConfiguration)) {
                        return launchConfiguration;
                    }
                }
            } catch (Exception e) {
                ExceptionHandler.process(e);
            }
        }
        return null;
    }

    /**
     * Launch the last launch, or open the launch config dialog if none.
     *
     * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
     */
    @Override
    public void run(IAction action) {
        // launch the job that is selected in the repository.
        if (selection instanceof IStructuredSelection) {
            IStructuredSelection sel = (IStructuredSelection) selection;
            Object o = sel.getFirstElement();
            if ((o instanceof RepositoryNode)) {
                RepositoryNode node = (RepositoryNode) o;
                if (node.getObject() != null && node.getObject().getRepositoryObjectType().equals(ERepositoryObjectType.PROCESS)) {
                    JobLaunchShortcutManager.run(selection);
                    return;
                }
            }
        }
        // launch the job that is open in editor
        if (window != null) {
            IWorkbenchPage page = window.getActivePage();
            if (page != null) {
                if (page.getActivePart() == page.getActiveEditor()) {
                    IEditorPart editor = page.getActiveEditor();
                    IEditorInput input = editor.getEditorInput();
                    if (input instanceof RepositoryEditorInput) {
                        JobLaunchShortcutManager.run(editor);
                        return;
                    }
                }
            }
        }

        ILaunchConfiguration configuration = getLastLaunch();

        if (configuration == null) {
            // MessageDialog
            // .openInformation(
            // DebugUIPlugin.getShell(),
            //                            Messages.getString("TalendLaunchToolbarAction.information"), Messages.getString("TalendLaunchToolbarAction.noAvailableItem")); //$NON-NLS-1$ //$NON-NLS-2$
            MessageDialog
                    .openInformation(
                            getShell(),
                            Messages.getString("TalendLaunchToolbarAction.information"), Messages.getString("TalendLaunchToolbarAction.noAvailableItem")); //$NON-NLS-1$ //$NON-NLS-2$

            // DebugUITools.openLaunchConfigurationDialogOnGroup(DebugUIPlugin.getShell(), new StructuredSelection(),
            // getLaunchGroupIdentifier());
        } else {
            IPreferenceStore debugUiStore = DebugUITools.getPreferenceStore();
            debugUiStore.setValue(IDebugUIConstants.PREF_BUILD_BEFORE_LAUNCH, Boolean.FALSE);
            DebugUITools.launch(configuration, getMode());
        }
    }

    private Shell getShell() {
        if (window == null) {
            return DisplayUtils.getDefaultShell(false);
        } else {
            return window.getShell();
        }
    }

    private boolean isCurrentProject(ILaunchConfiguration configuration) {
        if (configuration == null) {
            return false;
        }
        try {
            String projectName = configuration.getAttribute(TalendDebugUIConstants.CURRENT_PROJECT_NAME, (String) null);
            return projectName.equals(ProjectManager.getInstance().getCurrentProject().getLabel());
        } catch (Exception e) {
            // TODO: handle exception
        }
        return false;
    }

    // }
    /**
     * Returns the launch configuration manager.
     *
     * @return launch configuration manager
     */
    private LaunchConfigurationManager getLaunchConfigurationManager() {
        return DebugUIPlugin.getDefault().getLaunchConfigurationManager();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.debug.ui.actions.AbstractLaunchHistoryAction#launchHistoryChanged()
     */
    @Override
    public void launchHistoryChanged() {
        updateTooltip();
        super.launchHistoryChanged();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.debug.ui.actions.AbstractLaunchHistoryAction#updateTooltip()
     */
    @Override
    protected void updateTooltip() {
        getAction().setToolTipText(getToolTip());
    }

    private String getToolTip() {
        ILaunchConfiguration launch = getLastLaunch();
        if (launch != null) {
            try {
                String jobId = launch.getAttribute(TalendDebugUIConstants.JOB_ID, (String) null);
                String jobVersion = launch.getAttribute(TalendDebugUIConstants.JOB_VERSION, (String) null);
                String jobProjectLabel =  launch.getAttribute(TalendDebugUIConstants.JOB_PROJECT_TECH_LABEL, (String) null);
                if (jobId != null) {
                    List<IRepositoryViewObject> all = factory.getAllVersion(jobId);
                    for (IRepositoryViewObject obj : all) {
                        if (obj.getProperty().getVersion().equals(jobVersion) && ProcessUtils.isInProject(jobProjectLabel, obj.getProperty())) {
                            return RUN_LABEL + " " + obj.getProperty().getLabel();
                        }
                    }
                }
            } catch (CoreException e) {
                ExceptionHandler.process(e);
            } catch (PersistenceException e) {
                ExceptionHandler.process(e);
            }
        }
        return RUN_LABEL;
    }

}
