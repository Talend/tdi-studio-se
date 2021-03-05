// ============================================================================
//
// Talend Community Edition
//
// Copyright (C) 2006-2021 Talend Inc. - www.talend.com
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
//
// ============================================================================

package org.talend.spagic.engines.client.ui.wizards;

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
import org.talend.spagic.engines.client.i18n.Messages;

/**
 * Job scripts export wizard. <br/>
 *
 * $Id: JobScriptsExportWizard.java 1 2006-12-13 涓嬪�?3:13:18 bqian
 *
 */
public class SpagicDeployWizard extends Wizard implements IExportWizard {

    private IStructuredSelection selection;

    private WizardFileSystemResourceExportPage1 mainPage;

    /**
     * Creates a wizard for exporting workspace resources to a zip file.
     */
    public SpagicDeployWizard() {
        AbstractUIPlugin plugin = WorkbenchPlugin.getDefault();
        IDialogSettings workbenchSettings = plugin.getDialogSettings();
        IDialogSettings section = workbenchSettings.getSection("SapgicDeployWizard"); //$NON-NLS-1$
        if (section == null) {
            section = workbenchSettings.addNewSection("SapgicDeployWizard"); //$NON-NLS-1$
            section.put(JavaSpagicDeployWizardPage.STORE_SHELL_LAUNCHER_ID, true);
            section.put(JavaSpagicDeployWizardPage.STORE_SYSTEM_ROUTINE_ID, true);
            section.put(JavaSpagicDeployWizardPage.STORE_USER_ROUTINE_ID, true);
            section.put(JavaSpagicDeployWizardPage.STORE_MODEL_ID, true);
            section.put(JavaSpagicDeployWizardPage.STORE_JOB_ID, true);
            section.put(JavaSpagicDeployWizardPage.STORE_CONTEXT_ID, true);
            section.put(JavaSpagicDeployWizardPage.APPLY_TO_CHILDREN_ID, true);

        }
        setDialogSettings(section);
    }

    /*
     * (non-Javadoc) Method declared on IWizard.
     */
    public void addPages() {
        super.addPages();

        mainPage = new JavaSpagicDeployWizardPage(selection);
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

        setWindowTitle(Messages.getString("SapgicDeployWizard.exporttospagic")); //$NON-NLS-1$
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
