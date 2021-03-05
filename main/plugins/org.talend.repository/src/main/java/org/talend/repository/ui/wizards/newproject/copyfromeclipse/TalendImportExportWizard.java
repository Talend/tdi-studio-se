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

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.dialogs.ImportExportPage;
import org.eclipse.ui.internal.dialogs.ImportExportWizard;
import org.talend.repository.i18n.Messages;

/**
 * DOC guanglong.du class global comment. Detailled comment
 */
public class TalendImportExportWizard extends ImportExportWizard {

    private ImportExportPage importExportPage;

    private IWorkbench workbench;

    private IStructuredSelection selection;

    /**
     * DOC guanglong.du TalendImportExportWizard constructor comment.
     *
     * @param pageId
     */
    public TalendImportExportWizard(String pageId) {
        super(pageId);
        // TODO Auto-generated constructor stub
    }

    public void addPages() {
        importExportPage = new TalendExportPage(this.workbench, this.selection);
        if (importExportPage != null) {
            addPage(importExportPage);
        }
    }

    public void init(IWorkbench aWorkbench, IStructuredSelection currentSelection) {
        this.workbench = aWorkbench;
        this.selection = currentSelection;
        // ImageDescriptor wizardBannerImage =
        // WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_WIZBAN_EXPORT_WIZ);
        ImageDescriptor wizardBannerImage = PlatformUI.getWorkbench().getSharedImages()
                .getImageDescriptor("IMG_WIZBAN_EXPORT_WIZ"); //$NON-NLS-1$
        // setWindowTitle(WorkbenchMessages.ExportWizard_title);
        setWindowTitle(Messages.getString("WorkbenchMessages.ExportWizard_title")); //$NON-NLS-1$
        if (wizardBannerImage != null) {
            setDefaultPageImageDescriptor(wizardBannerImage);
        }
        setNeedsProgressMonitor(true);
    }

    public boolean performFinish() {
        importExportPage.saveWidgetValues();
        return true;
    }
}
