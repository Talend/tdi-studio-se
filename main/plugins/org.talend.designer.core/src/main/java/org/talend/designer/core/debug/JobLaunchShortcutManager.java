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

import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchShortcutExtension;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorPart;
import org.talend.core.model.process.Problem;
import org.talend.core.model.process.ProcessUtils;
import org.talend.core.model.process.TalendProblem;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.properties.Project;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.designer.core.ui.views.problems.Problems;
import org.talend.repository.ProjectManager;

/**
 * Added for compatibility with eclipse 3.4.
 */
public class JobLaunchShortcutManager {

    public static void run(ISelection selection) {
        List<LaunchShortcutExtension> launchShortcuts = DebugUIPlugin.getDefault().getLaunchConfigurationManager()
                .getLaunchShortcuts();

        for (LaunchShortcutExtension lse : launchShortcuts) {
            if (lse.getId().equals(TalendDebugUIConstants.TALEND_JOB_LAUNCH_SHORTCUT_ID)) {
                lse.launch(selection, ILaunchManager.RUN_MODE);
                return;
            }
        }
    }

    public static void run(IEditorPart editor) {
        List<LaunchShortcutExtension> launchShortcuts = DebugUIPlugin.getDefault().getLaunchConfigurationManager()
                .getLaunchShortcuts();

        for (LaunchShortcutExtension lse : launchShortcuts) {
            if (lse.getId().equals(TalendDebugUIConstants.TALEND_JOB_LAUNCH_SHORTCUT_ID)) {
                lse.launch(editor, ILaunchManager.RUN_MODE);
                return;
            }
        }
    }

    // debug not used yet, so code is disabled

    // public static void debug(ISelection selection) {
    // List<LaunchShortcutExtension> launchShortcuts = DebugUIPlugin.getDefault().getLaunchConfigurationManager()
    // .getLaunchShortcuts();
    //
    // for (LaunchShortcutExtension lse : launchShortcuts) {
    // if (lse.getId().equals(TalendDebugUIConstants.TALEND_JOB_LAUNCH_SHORTCUT_ID)) {
    // lse.launch(selection, ILaunchManager.DEBUG_MODE);
    // return;
    // }
    // }
    // }
    //
    // public static void debug(IEditorPart editor) {
    // List<LaunchShortcutExtension> launchShortcuts = DebugUIPlugin.getDefault().getLaunchConfigurationManager()
    // .getLaunchShortcuts();
    //
    // for (LaunchShortcutExtension lse : launchShortcuts) {
    // if (lse.getId().equals(TalendDebugUIConstants.TALEND_JOB_LAUNCH_SHORTCUT_ID)) {
    // lse.launch(editor, ILaunchManager.DEBUG_MODE);
    // return;
    // }
    // }
    // }
    /**
     *
     * cli Comment method "removeJobLaunch".
     */
    public static void removeJobLaunch(IRepositoryViewObject objToDelete) {
        if (objToDelete == null) {
            return;
        }
        Property property = objToDelete.getProperty();
        if (property == null || !(property.getItem() instanceof ProcessItem)) {
            return;
        }
        Project project = ProjectManager.getInstance().getProject(property);
        if (project == null) {
            return;
        }
        final String objProjectName = project.getLabel();
        final String objId = property.getId();
        // final String objName = property.getLabel();
        final String objVersion = property.getVersion();

        ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
        if (launchManager == null) {
            return;
        }
        try {
            ILaunchConfiguration configuration = null;
            for (ILaunchConfiguration config : launchManager.getLaunchConfigurations()) {
                String jobId = config.getAttribute(TalendDebugUIConstants.JOB_ID, (String) null);
                // String jobName = configuration.getAttribute(TalendDebugUIConstants.JOB_NAME, (String) null);
                String jobVersion = config.getAttribute(TalendDebugUIConstants.JOB_VERSION, (String) null);
                String projectName = config.getAttribute(TalendDebugUIConstants.CURRENT_PROJECT_NAME, (String) null);
                String jobProjectLabel =  config.getAttribute(TalendDebugUIConstants.JOB_PROJECT_TECH_LABEL, (String) null);
                ILaunchConfigurationType type = config.getType();
                if (type != null && TalendDebugUIConstants.JOB_DEBUG_LAUNCH_CONFIGURATION_TYPE.equals(type.getIdentifier())
                        && objProjectName.equals(projectName) && objId.equals(jobId) && objVersion.equals(jobVersion) && ProcessUtils.isInProject(jobProjectLabel, property)) {
                    configuration = config;
                    break;
                }
            }
            if (configuration == null) {
                return;
            }
            configuration.delete();
        } catch (CoreException e) {
            // nothing to do
        }

    }

    /**
     *
     * cli Comment method "renameJobLaunch".
     */
    public static void renameJobLaunch(IRepositoryViewObject obj, String oldLabel) {
        if (obj == null) {
            return;
        }
        Property property = obj.getProperty();
        if (property == null || !(property.getItem() instanceof ProcessItem)) {
            return;
        }
        String newLabel = property.getLabel();
        if (!newLabel.equals(oldLabel)) {
            Project project = ProjectManager.getInstance().getProject(property);
            if (project == null) {
                return;
            }

            final String objProjectName = project.getLabel();
            final String id = property.getId();
            final String version = property.getVersion();

            ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
            if (launchManager == null) {
                return;
            }
            try {
                for (ILaunchConfiguration configuration : launchManager.getLaunchConfigurations()) {

                    String jobId = configuration.getAttribute(TalendDebugUIConstants.JOB_ID, (String) null);
                    String jobVersion = configuration.getAttribute(TalendDebugUIConstants.JOB_VERSION, (String) null);
                    String projectName = configuration.getAttribute(TalendDebugUIConstants.CURRENT_PROJECT_NAME, (String) null);
                    String jobProjectLabel = configuration.getAttribute(TalendDebugUIConstants.JOB_PROJECT_TECH_LABEL, (String) null);
                    // ILaunchConfigurationType type = launchManager
                    // .getLaunchConfigurationType(TalendDebugUIConstants.JOB_DEBUG_LAUNCH_CONFIGURATION_TYPE);
                    ILaunchConfigurationType type = configuration.getType();
                    if (type != null && TalendDebugUIConstants.JOB_DEBUG_LAUNCH_CONFIGURATION_TYPE.equals(type.getIdentifier())
                            && objProjectName.equals(projectName) && id.equals(jobId) && version.equals(jobVersion)
                            && type != null && ProcessUtils.isInProject(jobProjectLabel, property)) {
                        String displayName = newLabel + " " + jobVersion; //$NON-NLS-1$
                        ILaunchConfigurationWorkingCopy workingCopy = configuration.getWorkingCopy();
                        workingCopy.setAttribute(TalendDebugUIConstants.JOB_NAME, newLabel);
                        // workingCopy.setAttribute(TalendDebugUIConstants.JOB_ID, jobId);
                        // update to new version
                        workingCopy.setAttribute(TalendDebugUIConstants.JOB_VERSION, jobVersion);
                        // workingCopy.setAttribute(TalendDebugUIConstants.CURRENT_PROJECT_NAME, projectName);
                        workingCopy.rename(displayName);
                        workingCopy.doSave();
                        break;
                    }
                }
                clearUnusedLaunchs();
            } catch (CoreException e) {
                // nothing to do
            }
        }
    }

    /**
     *
     * ldong Comment method "resetJobProblemList".
     */
    public static void resetJobProblemList(IRepositoryViewObject obj, String oldLabel) {
        if (obj == null) {
            return;
        }
        Property property = obj.getProperty();
        if (property == null || !(property.getItem() instanceof ProcessItem)) {
            return;
        }
        String newLabel = property.getLabel();
        if (!newLabel.equals(oldLabel)) {
            for (Iterator<Problem> iter = Problems.getProblemList().getProblemList().iterator(); iter.hasNext();) {
                Problem problem = iter.next();
                if (problem instanceof TalendProblem) {
                    TalendProblem routineProblem = (TalendProblem) problem;
                    if (routineProblem.getJavaUnitName() != null && (routineProblem.getJavaUnitName().equals(oldLabel))) {
                        // TDI-24683:if rename the jobItem,need clear the problem view to avoid use the old
                        // problem list
                        iter.remove();
                    }
                }
            }
        }
    }

    private static void clearUnusedLaunchs() {
        ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
        if (launchManager == null) {
            return;
        }
        for (ILaunch launch : launchManager.getLaunches()) {
            if (launch.getLaunchConfiguration() == null) {
                launchManager.removeLaunch(launch);
            }
        }
    }
}
