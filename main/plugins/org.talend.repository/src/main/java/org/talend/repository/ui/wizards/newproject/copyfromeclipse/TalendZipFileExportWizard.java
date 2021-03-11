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
package org.talend.repository.ui.wizards.newproject.copyfromeclipse;

import java.util.List;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.wizards.datatransfer.ZipFileExportWizard;
import org.talend.core.prefs.IDEWorkbenchPlugin;
import org.talend.repository.i18n.Messages;

/**
 * Copy of class org.eclipse.ui.wizards.datatransfer.ZipFileExportWizard. This copy has been done to filter some
 * projects from export wizard.
 *
 * $Id$
 *
 */
public class TalendZipFileExportWizard extends ZipFileExportWizard {

    private IStructuredSelection selection;

    private TalendWizardArchiveFileResourceExportPage1 mainPage;

    /**
     * Creates a wizard for exporting workspace resources to a zip file.
     */
    public TalendZipFileExportWizard() {
        super();
    }

    public void init(IWorkbench workbench, IStructuredSelection currentSelection) {
        this.selection = currentSelection;
        List selectedResources = IDE.computeSelectedResources(currentSelection);
        if (!selectedResources.isEmpty()) {
            this.selection = new StructuredSelection(selectedResources);
        }

        // setWindowTitle(DataTransferMessages.DataTransfer_export);
        setWindowTitle(Messages.getString("DataTransferMessages.DataTransfer_export")); //$NON-NLS-1$
        //        setDefaultPageImageDescriptor(IDEWorkbenchPlugin.getIDEImageDescriptor("wizban/exportzip_wiz.png"));//$NON-NLS-1$
        setDefaultPageImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(IDEWorkbenchPlugin.IDE_WORKBENCH,
                "$nl$/icons/full/wizban/exportzip_wiz.png")); //$NON-NLS-1$
        setNeedsProgressMonitor(true);
    }

    /*
     * (non-Javadoc) Method declared on IWizard.
     */
    public void addPages() {
        mainPage = new TalendWizardArchiveFileResourceExportPage1(selection);
        addPage(mainPage);
    }

    public boolean performFinish() {
        return mainPage.finish();
    }
}
