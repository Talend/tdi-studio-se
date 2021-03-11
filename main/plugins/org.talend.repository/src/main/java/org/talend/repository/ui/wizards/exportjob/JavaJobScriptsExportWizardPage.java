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
package org.talend.repository.ui.wizards.exportjob;

import java.util.List;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.talend.commons.exception.PersistenceException;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.repository.ui.wizards.exportjob.scriptsmanager.JobScriptsManager;
import org.talend.repository.ui.wizards.exportjob.util.ExportJobUtil;

/**
 * Page of the Job Scripts Export Wizard. <br/>
 *
 * @referto WizardArchiveFileResourceExportPage1 $Id: JobScriptsExportWizardPage.java 1 2006-12-13 下午03:09:07 bqian
 *
 */
public abstract class JavaJobScriptsExportWizardPage extends JobScriptsExportWizardPage {

    // dialog store id constants
    public static final String STORE_SHELL_LAUNCHER_ID = "JavaJobScriptsExportWizardPage.STORE_SHELL_LAUNCHER_ID"; //$NON-NLS-1$

    public static final String STORE_SYSTEM_ROUTINE_ID = "JavaJobScriptsExportWizardPage.STORE_SYSTEM_ROUTINE_ID"; //$NON-NLS-1$

    public static final String STORE_USER_ROUTINE_ID = "JavaJobScriptsExportWizardPage.STORE_USER_ROUTINE_ID"; //$NON-NLS-1$

    public static final String STORE_MODEL_ID = "JavaJobScriptsExportWizardPage.STORE_MODEL_ID"; //$NON-NLS-1$

    public static final String STORE_JOB_ID = "JavaJobScriptsExportWizardPage.STORE_JOB_ID"; //$NON-NLS-1$

    public static final String STORE_CONTEXT_ID = "JavaJobScriptsExportWizardPage.STORE_CONTEXT_ID"; //$NON-NLS-1$

    public static final String APPLY_TO_CHILDREN_ID = "JavaJobScriptsExportWizardPage.APPLY_TO_CHILDREN_ID"; //$NON-NLS-1$

    public static final String APPLY_LOG4J_TO_CHILDREN_ID = "JavaJobScriptsExportWizardPage.APPLY_LOG4J_TO_CHILDREN_ID"; //$NON-NLS-1$

    public static final String LOG4J_LEVEL_ID = "JavaJobScriptsExportWizardPage.LOG4J_LEVEL_ID"; //$NON-NLS-1$

    public static final String STORE_DEPENDENCIES_ID = "JavaJobScriptsExportWizardPage.STORE_DEPENDENCIES_ID"; //$NON-NLS-1$

    // public static final String STORE_GENERATECODE_ID = "JavaJobScriptsExportWizardPage.STORE_GENERATECODE_ID";
    // //$NON-NLS-1$

    public static final String STORE_SOURCE_ID = "JavaJobScriptsExportWizardPage.STORE_SOURCE_ID"; //$NON-NLS-1$

    public static final String STORE_DESTINATION_NAMES_ID = "JavaJobScriptsExportWizardPage.STORE_DESTINATION_NAMES_ID"; //$NON-NLS-1$

    public static final String EXTRACT_ZIP_FILE = "JavaJobScriptsExportWizardPage.EXTRACT_ZIP_FILE"; //$NON-NLS-1$

    public static final String STORE_DOCKER_IS_REMOTE_HOST = "JavaJobScriptsExportWizardPage.STORE_DOCKER_IS_REMOTE_HOST"; //$NON-NLS-1$

    public static final String STORE_DOCKER_REMOTE_HOST = "JavaJobScriptsExportWizardPage.STORE_DOCKER_REMOTE_HOST"; //$NON-NLS-1$

    /**
     * Create an instance of this class.
     *
     * @param selection the selection
     */
    public JavaJobScriptsExportWizardPage(IStructuredSelection selection) {
        super("JavaJobscriptsExportPage1", selection); //$NON-NLS-1$
    }

    /**
     * Hook method for saving widget values for restoration by the next instance of this class.
     */
    @Override
    protected void internalSaveWidgetValues() {
        // update directory names history
        IDialogSettings settings = getDialogSettings();
        if (settings != null) {
            String[] directoryNames = settings.getArray(STORE_DESTINATION_NAMES_ID);
            if (directoryNames == null) {
                directoryNames = new String[0];
            }

            directoryNames = addToHistory(directoryNames, getDestinationValue());
            settings.put(STORE_DESTINATION_NAMES_ID, directoryNames);
            settings.put(STORE_SHELL_LAUNCHER_ID, shellLauncherButton.getSelection());
            settings.put(STORE_JOB_ID, jobItemButton.getSelection());
            settings.put(STORE_SOURCE_ID, jobScriptButton.getSelection());
            settings.put(STORE_CONTEXT_ID, contextButton.getSelection());
            settings.put(APPLY_TO_CHILDREN_ID, applyToChildrenButton.getSelection());
            settings.put(EXTRACT_ZIP_FILE, chkButton.getSelection());
            // settings.put(STORE_GENERATECODE_ID, genCodeButton.getSelection());
        }
    }

    /**
     * Hook method for restoring widget values to the values that they held last time this wizard was used to
     * completion.
     */
    @Override
    protected void restoreWidgetValues() {
        IDialogSettings settings = getDialogSettings();
        if (settings != null) {
            String[] directoryNames = settings.getArray(STORE_DESTINATION_NAMES_ID);
            if (directoryNames != null && directoryNames.length > 0) {
                for (String directoryName : directoryNames) {
                    addDestinationItem(directoryName);
                }
            }
            setDefaultDestination();

            shellLauncherButton.setSelection(settings.getBoolean(STORE_SHELL_LAUNCHER_ID));
            jobItemButton.setSelection(settings.getBoolean(STORE_JOB_ID));
            jobScriptButton.setSelection(settings.getBoolean(STORE_SOURCE_ID));
            contextButton.setSelection(settings.getBoolean(STORE_CONTEXT_ID));
            applyToChildrenButton.setSelection(settings.getBoolean(APPLY_TO_CHILDREN_ID));
            chkButton.setSelection(settings.getBoolean(EXTRACT_ZIP_FILE));
            // genCodeButton.setSelection(settings.getBoolean(STORE_GENERATECODE_ID));
        }

        launcherCombo.setItems(JobScriptsManager.getLauncher());
        if (JobScriptsManager.getLauncher().length > 0) {
            launcherCombo.select(0);
        }
        try {
            setProcessItem((ProcessItem) ProxyRepositoryFactory.getInstance().getUptodateProperty(getProcessItem().getProperty())
                    .getItem());
        } catch (PersistenceException e) {
            e.printStackTrace();
        }
        List<String> contextNames = ExportJobUtil.getJobContexts(getProcessItem());
        contextCombo.setItems(contextNames.toArray(new String[contextNames.size()]));
        if (contextNames.size() > 0) {
            contextCombo.select(0);
        }
    }

    @Override
    protected String getProcessType() {
        return "Job";
    }
}
