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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.core.model.process.ProcessUtils;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.repository.ui.editor.RepositoryEditorInput;
import org.talend.repository.ProjectManager;
import org.talend.repository.model.RepositoryNode;

/**
 * A launch shortcut is capable of launching a selection or active editor in the workbench. A new launch configuration
 * with default values is created, if an existing launch configuration can't be re-used.
 * <p>
 * A launch shortcut is defined as an extension of type <code>org.eclipse.debug.ui.launchShortcuts</code>. A shortcut
 * specifies the perspectives in which is should be available from the "Run/Debug" cascade menus.
 * </p>
 * <p>
 * A launch shortcut extension is defined in <code>plugin.xml</code>. Following is an example definition of a launch
 * shortcut extension:
 *
 * <pre>
 *   &lt;extension
 *          point=&quot;org.eclipse.debug.ui.launchShortcuts&quot;&gt;
 *       &lt;shortcut
 *             category=&quot;org.eclipse.jdt.ui.JavaPerspective&quot;
 *             class=&quot;org.epic.debug.LaunchShortcut&quot;
 *             icon=&quot;icons/epic.gif&quot;
 *             id=&quot;org.epic.debug.LaunchShortcut&quot;
 *             label=&quot;Perl Local&quot;
 *             modes=&quot;run, debug&quot;&gt;
 *
 *             &lt;contextualLaunch&gt;
 *              &lt;enablement&gt;
 *                &lt;with variable=&quot;selection&quot;&gt;
 *                  &lt;count value=&quot;1&quot;/&gt;
 *                  &lt;iterate&gt;
 *                    &lt;or&gt;
 *                      &lt;test property=&quot;org.eclipse.debug.ui.matchesPattern&quot; value=&quot;*.pl&quot;/&gt;
 *                      &lt;test property=&quot;org.eclipse.debug.ui.matchesPattern&quot; value=&quot;*.pm&quot;/&gt;
 *                    &lt;/or&gt;
 *                  &lt;/iterate&gt;
 *                &lt;/with&gt;
 *              &lt;/enablement&gt;
 *   		   &lt;/contextualLaunch&gt;
 *
 *  	    &lt;/shortcut&gt;
 *  	&lt;/extension&gt;
 *
 * </pre>
 *
 * @author bqian
 *
 */
public class JobLaunchShortcut implements ILaunchShortcut {

    public class IInternalDebugUIConstants {

        /**
         * String preference controlling whether editors are saved before launching. Valid values are either "always",
         * "never", or "prompt". If "always" or "never", launching will save editors (or not) automatically. If
         * "prompt", the user will be prompted each time.
         *
         * @since 3.0
         */
        public static final String PREF_SAVE_DIRTY_EDITORS_BEFORE_LAUNCH = IDebugUIConstants.PLUGIN_ID
                + ".save_dirty_editors_before_launch"; //$NON-NLS-1$

    }

    public JobLaunchShortcut() {
        // DebugUIPlugin.getDefault().getPreferenceStore().putValue(IInternalDebugUIConstants.PREF_SAVE_DIRTY_EDITORS_BEFORE_LAUNCH,
        // MessageDialogWithToggle.NEVER);
        DebugUITools.getPreferenceStore().putValue(IInternalDebugUIConstants.PREF_SAVE_DIRTY_EDITORS_BEFORE_LAUNCH,
                MessageDialogWithToggle.NEVER);
    }

    /**
     * Locates a launchable entity in the given selection and launches an application in the specified mode.
     *
     * @see org.eclipse.debug.ui.ILaunchShortcut#launch(org.eclipse.jface.viewers.ISelection, java.lang.String)
     *
     * @param selection workbench selection
     * @param mode one of the launch modes defined by the launch manager
     * @see org.eclipse.debug.core.ILaunchManager
     */
    @Override
    public void launch(ISelection selection, String mode) {
        if (selection instanceof IStructuredSelection) {
            Object object = ((IStructuredSelection) selection).getFirstElement();

            if (object instanceof RepositoryNode) {
                RepositoryNode node = (RepositoryNode) object;
                launch(node.getObject().getProperty().getItem(), mode);
            }
        }
    }

    /**
     * Locates a launchable entity in the given active editor, and launches an application in the specified mode.
     *
     * @see org.eclipse.debug.ui.ILaunchShortcut#launch(org.eclipse.ui.IEditorPart, java.lang.String)
     *
     * @param editor the active editor in the workbench
     * @param mode one of the launch modes defined by the launch manager
     * @see org.eclipse.debug.core.ILaunchManager
     */
    @Override
    public void launch(IEditorPart editor, String mode) {
        IEditorInput input = editor.getEditorInput();
        if (input instanceof RepositoryEditorInput) {
            RepositoryEditorInput rInput = (RepositoryEditorInput) input;
            launch(rInput.getItem(), mode);
        }
    }

    /**
     * bqian Comment method "launch".
     *
     * @param object
     * @param mode
     */
    private void launch(Item item, String mode) {
        if (item instanceof ProcessItem) {
            ILaunchConfiguration config = findLaunchConfiguration((ProcessItem) item, mode);
            if (config != null) {
                IPreferenceStore debugUiStore = DebugUITools.getPreferenceStore();
                debugUiStore.setValue(IDebugUIConstants.PREF_BUILD_BEFORE_LAUNCH, Boolean.FALSE);
                DebugUITools.launch(config, mode);
            }
        }
    }

    /**
     * If re-usable configuration associated with the File and the project exist, this configuration is returned.
     * Otherwise a new configuration is created.
     *
     * @param bin
     * @param mode
     * @return a re-useable or new config or <code>null</code> if none
     */
    private ILaunchConfiguration findLaunchConfiguration(ProcessItem file, String mode) {
        ILaunchConfiguration configuration = null;
        List candidateConfigs = Collections.EMPTY_LIST;
        try {
            ILaunchConfiguration[] configs = DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurations();
            candidateConfigs = new ArrayList(configs.length);
            for (ILaunchConfiguration config : configs) {
                String projectName = config.getAttribute(TalendDebugUIConstants.CURRENT_PROJECT_NAME, (String) null);
                if (projectName == null) {
                    continue;
                }
                if (!projectName.equals(ProjectManager.getInstance().getCurrentProject().getLabel())) {
                    continue;
                }
                String jobId = config.getAttribute(TalendDebugUIConstants.JOB_ID, (String) null);
                String jobVersion = config.getAttribute(TalendDebugUIConstants.JOB_VERSION, (String) null);
                if (jobId == null) {
                    continue;
                }
                String jobProjectLabel = config.getAttribute(TalendDebugUIConstants.JOB_PROJECT_TECH_LABEL, (String) null);
                if (file.getProperty().getId().equals(jobId) && file.getProperty().getVersion().equals(jobVersion) && ProcessUtils.isInProject(jobProjectLabel, file.getProperty())) {
                    candidateConfigs.add(config);
                }
            }
        } catch (CoreException e) {
            ExceptionHandler.process(e);
        }

        int candidateCount = candidateConfigs.size();
        if (candidateCount < 1) {
            configuration = createConfiguration(file);
        } else {
            configuration = (ILaunchConfiguration) candidateConfigs.get(0);
        }
        return configuration;
    }

    /**
     * Creates a new configuration associated with the given file.
     *
     * @param file
     * @return ILaunchConfiguration
     */
    private ILaunchConfiguration createConfiguration(ProcessItem file) {
        ILaunchConfiguration config = null;
        String jobId = file.getProperty().getId();
        String jobName = file.getProperty().getLabel();
        String jobVersion = file.getProperty().getVersion();
        String jobProjectLabel = ProjectManager.getInstance().getProject(file.getProperty()).getTechnicalLabel();
        ILaunchConfigurationType type = getLaunchManager()
                .getLaunchConfigurationType(TalendDebugUIConstants.JOB_DEBUG_LAUNCH_CONFIGURATION_TYPE);
        String displayName = jobName + " " + jobVersion; //$NON-NLS-1$

        try {
            if (type != null) {
                ILaunchConfigurationWorkingCopy wc = type.newInstance(null,
                        getLaunchManager().generateUniqueLaunchConfigurationNameFrom(displayName));
                wc.setAttribute(TalendDebugUIConstants.JOB_NAME, jobName);
                wc.setAttribute(TalendDebugUIConstants.JOB_ID, jobId);
                wc.setAttribute(TalendDebugUIConstants.JOB_VERSION, jobVersion);
                wc.setAttribute(TalendDebugUIConstants.JOB_PROJECT_TECH_LABEL, jobProjectLabel);
                wc.setAttribute(TalendDebugUIConstants.CURRENT_PROJECT_NAME, ProjectManager.getInstance().getCurrentProject().getLabel());
                config = wc.doSave();
            }
        } catch (CoreException e) {
            ExceptionHandler.process(e);
        }
        return config;
    }

    /**
     * Method to get the LaunchManager
     *
     * @return ILaunchManager
     */
    protected ILaunchManager getLaunchManager() {
        return DebugPlugin.getDefault().getLaunchManager();
    }

}
