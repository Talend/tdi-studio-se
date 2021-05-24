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
package org.talend.designer.documentation.generation.wizards;

import java.util.List;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.talend.core.prefs.IDEWorkbenchPlugin;
import org.talend.repository.i18n.Messages;

/**
 * This is a wizard for generating job information as HTML file.
 *
 * $Id: GenerateDocAsHTMLWizard.java 2007-3-7,下午02:26:29 ftang $
 *
 */
public class GenerateDocAsHTMLWizard extends Wizard implements IExportWizard {

    private IStructuredSelection selection;

    private GenerateDocAsHTMLWizardPage mainPage;

    /**
     * Creates a wizard for exporting workspace resources to a zip file.
     */
    public GenerateDocAsHTMLWizard() {
        AbstractUIPlugin plugin = WorkbenchPlugin.getDefault();
        IDialogSettings workbenchSettings = plugin.getDialogSettings();
        IDialogSettings section = workbenchSettings.getSection("GenerateDocAsHTMLWizard"); //$NON-NLS-1$
        if (section == null) {
            section = workbenchSettings.addNewSection("GenerateDocAsHTMLWizard"); //$NON-NLS-1$
        }
        setDialogSettings(section);
    }

    /*
     * (non-Javadoc) Method declared on IWizard.
     */
    public void addPages() {
        super.addPages();
        mainPage = new GenerateDocAsHTMLWizardPage(selection);
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

        setWindowTitle(Messages.getString("GenerateDocAsHTMLWizard.GenerateDocumentation")); //$NON-NLS-1$
        //        setDefaultPageImageDescriptor(IDEWorkbenchPlugin.getIDEImageDescriptor("wizban/exportzip_wiz.png"));//$NON-NLS-1$
        setDefaultPageImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(IDEWorkbenchPlugin.IDE_WORKBENCH,
                "$nl$/icons/full/wizban/exportzip_wiz.png"));//$NON-NLS-1$
        setNeedsProgressMonitor(true);
    }

    /*
     * (non-Javadoc) Method declared on IWizard.
     */
    public boolean performFinish() {
        return mainPage.finish();
    }
}
