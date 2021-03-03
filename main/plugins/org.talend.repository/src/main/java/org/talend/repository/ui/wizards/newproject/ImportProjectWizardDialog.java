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
package org.talend.repository.ui.wizards.newproject;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.ui.branding.IBrandingService;
import org.talend.repository.i18n.Messages;

/**
 * DOC mhirt class global comment. Detailled comment <br/>
 *
 * $Id: LicenseWizardDialog.java 1874 2007-02-06 11:36:15Z yzhang $
 *
 */
public final class ImportProjectWizardDialog extends WizardDialog {

    /**
     * DOC mhirt LicenseWizardDialog constructor comment.
     *
     * @param parentShell
     * @param newWizard
     */
    public ImportProjectWizardDialog(Shell parentShell, IWizard newWizard) {
        super(parentShell, newWizard);
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        super.createButtonsForButtonBar(parent);

        IBrandingService brandingService = (IBrandingService) GlobalServiceRegister.getDefault().getService(
                IBrandingService.class);

        Button nextButton = getButton(IDialogConstants.NEXT_ID);
        if (nextButton != null) {
            nextButton.setText(Messages.getString("ImportProjectsAction.actionTitle")); //$NON-NLS-1$
            nextButton.setToolTipText(Messages.getString(
                    "ImportProjectsAction.actionTooltip", brandingService.getShortProductName())); //$NON-NLS-1$
            Point point = nextButton.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
            GridData data = new GridData(point.x, point.y);
            nextButton.setLayoutData(data);
        }

        Button backButton = getButton(IDialogConstants.BACK_ID);
        if (backButton != null) {
            backButton.setText(Messages.getString("ImportProjectAsAction.actionTitle")); //$NON-NLS-1$
            backButton.setToolTipText(Messages.getString(
                    "ImportProjectAsAction.actionTooltip", brandingService.getShortProductName())); //$NON-NLS-1$
            Point point = backButton.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
            GridData data = new GridData(point.x, point.y);
            backButton.setLayoutData(data);
        }

        parent.redraw();
    }
}
