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
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.wizards.datatransfer.WizardFileSystemResourceExportPage1;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.talend.core.prefs.IDEWorkbenchPlugin;
import org.talend.sbi.engines.client.i18n.Messages;

/**
 * Publish SpagoBI export wizard. <br/>
 *
 * $Id: PublishOnSpagoExportWizard.java 1 2007-04-27 11:30:00 cantoine
 *
 */
public class PublishOnSpagoExportWizard extends Wizard implements IExportWizard {

    private IStructuredSelection selection;

    private WizardFileSystemResourceExportPage1 mainPage;

    /**
     * Creates a wizard for exporting workspace resources to a zip file.
     */
    public PublishOnSpagoExportWizard() {
        AbstractUIPlugin plugin = WorkbenchPlugin.getDefault();
        IDialogSettings workbenchSettings = plugin.getDialogSettings();
        IDialogSettings section = workbenchSettings.getSection("PublishOnSpagoExportWizard"); //$NON-NLS-1$
        if (section == null) {
            section = workbenchSettings.addNewSection("PublishOnSpagoExportWizard"); //$NON-NLS-1$
            section.put(JavaPublishOnSpagoExportWizardPage.STORE_SHELL_LAUNCHER_ID, true);
            section.put(JavaPublishOnSpagoExportWizardPage.STORE_SYSTEM_ROUTINE_ID, true);
            section.put(JavaPublishOnSpagoExportWizardPage.STORE_USER_ROUTINE_ID, true);
            section.put(JavaPublishOnSpagoExportWizardPage.STORE_MODEL_ID, true);
            section.put(JavaPublishOnSpagoExportWizardPage.STORE_JOB_ID, true);
            section.put(JavaPublishOnSpagoExportWizardPage.STORE_CONTEXT_ID, true);

        }
        setDialogSettings(section);
    }

    /*
     * (non-Javadoc) Method declared on IWizard.
     */
    public void addPages() {
        super.addPages();

        mainPage = new JavaPublishOnSpagoExportWizardPage(selection);
        addPage(mainPage);
    }

    /*
     * (non-Javadoc) Method declared on IWorkbenchWizard.
     */
    public void init(IWorkbench workbench, IStructuredSelection currentSelection) {
        this.selection = currentSelection;
        List selectedResources = IDE.computeSelectedResources(currentSelection);
        if (!selectedResources.isEmpty()) {
            this.selection = new StructuredSelection(selectedResources);
        }

        setWindowTitle(Messages.getString("PublishOnSpagoExportWizard.publishJob")); //$NON-NLS-1$
        //        setDefaultPageImageDescriptor(IDEWorkbenchPlugin.getIDEImageDescriptor("wizban/exportzip_wiz.png"));//$NON-NLS-1$
        setDefaultPageImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(IDEWorkbenchPlugin.IDE_WORKBENCH,
                "$nl$/icons/full/wizban/exportzip_wiz.png")); //$NON-NLS-1$
        setNeedsProgressMonitor(true);
    }

    /*
     * (non-Javadoc) Method declared on IWizard.
     */
    public boolean performFinish() {
        return mainPage.finish();
    }
}
