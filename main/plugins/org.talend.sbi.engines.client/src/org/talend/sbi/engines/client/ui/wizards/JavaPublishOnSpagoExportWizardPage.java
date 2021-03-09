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
package org.talend.sbi.engines.client.ui.wizards;

import java.util.List;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.custom.BusyIndicator;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.designer.runprocess.IProcessor;
import org.talend.designer.runprocess.ProcessorException;
import org.talend.repository.documentation.ExportFileResource;

/**
 * Publish SpagoBI Export Wizard. <br/>
 *
 * @referto WizardArchiveFileResourceExportPage1 $Id: JavaPublishOnSpagoExportWizardPage.java 1 2007-04-27 12:00:07
 * cantoine
 *
 */
public class JavaPublishOnSpagoExportWizardPage extends PublishOnSpagoExportWizardPage {

    // dialog store id constants
    public static final String STORE_SHELL_LAUNCHER_ID = "JavaJobScriptsExportWizardPage.STORE_SHELL_LAUNCHER_ID"; //$NON-NLS-1$

    public static final String STORE_SYSTEM_ROUTINE_ID = "JavaJobScriptsExportWizardPage.STORE_SYSTEM_ROUTINE_ID"; //$NON-NLS-1$

    public static final String STORE_USER_ROUTINE_ID = "JavaJobScriptsExportWizardPage.STORE_USER_ROUTINE_ID"; //$NON-NLS-1$

    public static final String STORE_MODEL_ID = "JavaJobScriptsExportWizardPage.STORE_MODEL_ID"; //$NON-NLS-1$

    public static final String STORE_JOB_ID = "JavaJobScriptsExportWizardPage.STORE_JOB_ID"; //$NON-NLS-1$

    public static final String STORE_CONTEXT_ID = "JavaJobScriptsExportWizardPage.STORE_CONTEXT_ID"; //$NON-NLS-1$

    // public static final String STORE_GENERATECODE_ID = "JavaJobScriptsExportWizardPage.STORE_GENERATECODE_ID";
    // //$NON-NLS-1$

    public static final String STORE_SOURCE_ID = "JavaJobScriptsExportWizardPage.STORE_SOURCE_ID"; //$NON-NLS-1$

    private static final String STORE_DESTINATION_NAMES_ID = "JavaJobScriptsExportWizardPage.STORE_DESTINATION_NAMES_ID"; //$NON-NLS-1$

    /**
     * Create an instance of this class.
     *
     * @param selection the selection
     */
    public JavaPublishOnSpagoExportWizardPage(IStructuredSelection selection) {
        super("JavaPublishOnSpagoExportPage1", selection); //$NON-NLS-1$
    }

    /**
     * Returns resources to be exported. This returns file - for just the files use getSelectedResources.
     *
     * @return a collection of resources currently selected for export (element type: <code>IResource</code>)
     */
    protected List<ExportFileResource> getExportResources() {
        final List<ExportFileResource>[] resourcesToExportxx = new List[1];

        BusyIndicator.showWhile(this.getShell().getDisplay(), new Runnable() {

            public void run() {
                try {
                    resourcesToExportxx[0] = JavaPublishOnSpagoExportWizardPage.super.getExportResources();
                } catch (ProcessorException e) {
                    ExceptionHandler.process(e);
                }
            }
        });
        return resourcesToExportxx[0];
    }

    /**
     * Hook method for saving widget values for restoration by the next instance of this class.
     */
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
            settings.put(STORE_SHELL_LAUNCHER_ID, true);
            settings.put(STORE_SYSTEM_ROUTINE_ID, true);
            settings.put(STORE_USER_ROUTINE_ID, true);
            settings.put(STORE_MODEL_ID, true);
            settings.put(STORE_JOB_ID, true);
            settings.put(STORE_SOURCE_ID, false);
            settings.put(STORE_CONTEXT_ID, contextButton.getSelection());
            // settings.put(STORE_GENERATECODE_ID, genCodeButton.getSelection());
        }
    }

    /**
     * Hook method for restoring widget values to the values that they held last time this wizard was used to
     * completion.
     */
    protected void restoreWidgetValues() {
        IDialogSettings settings = getDialogSettings();
        if (settings != null) {

            // String[] directoryNames = settings.getArray(STORE_DESTINATION_NAMES_ID);
            // if (directoryNames != null) {
            // // destination
            // setDestinationValue(directoryNames[0]);
            // for (int i = 0; i < directoryNames.length; i++) {
            // addDestinationItem(directoryNames[i]);
            // }
            // }
            // shellLauncherButton.setSelection(settings.getBoolean(STORE_SHELL_LAUNCHER_ID));
            // systemRoutineButton.setSelection(settings.getBoolean(STORE_SYSTEM_ROUTINE_ID));
            // userRoutineButton.setSelection(settings.getBoolean(STORE_USER_ROUTINE_ID));
            // modelButton.setSelection(settings.getBoolean(STORE_MODEL_ID));
            // jobButton.setSelection(settings.getBoolean(STORE_JOB_ID));
            // sourceButton.setSelection(settings.getBoolean(STORE_SOURCE_ID));
            contextButton.setSelection(settings.getBoolean(STORE_CONTEXT_ID));
            // genCodeButton.setSelection(settings.getBoolean(STORE_GENERATECODE_ID));
        }

        // launcherCombo.setItems(manager.getLauncher());
        // if (manager.getLauncher().length > 0) {
        // launcherCombo.select(0);
        // }
        if (process.length > 0) {
            try {
                process[0].setProcess((ProcessItem) ProxyRepositoryFactory.getInstance()
                        .getUptodateProperty(process[0].getItem().getProperty()).getItem());
            } catch (PersistenceException e) {
                e.printStackTrace();
            }
            if (manager == null) {
                manager = new JobJavaScriptsManager(getExportChoiceMap(), contextCombo.getText(),
                        "all", IProcessor.NO_STATISTICS, //$NON-NLS-1$
                        IProcessor.NO_TRACES);
            }
            List<String> contextNames = manager.getJobContextsComboValue((ProcessItem) process[0].getItem());
            contextCombo.setItems(contextNames.toArray(new String[contextNames.size()]));
            if (contextNames.size() > 0) {
                contextCombo.select(0);
            }
        }
    }
}
