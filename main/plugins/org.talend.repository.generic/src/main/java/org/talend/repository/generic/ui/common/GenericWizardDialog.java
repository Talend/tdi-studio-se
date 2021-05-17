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
package org.talend.repository.generic.ui.common;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Shell;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.service.ComponentService;
import org.talend.daikon.properties.presentation.Form;

/**
 * created by ycbai on 2015年9月29日 Detailled comment
 *
 */
public class GenericWizardDialog extends WizardDialog {

    private ComponentService compService;

    public GenericWizardDialog(Shell parentShell, IWizard newWizard, ComponentService compService) {
        super(parentShell, newWizard);
        this.compService = compService;
    }

    @Override
    protected void nextPressed() {
        Form form = getForm();
        if (form != null && form.isCallAfterFormNext()) {
            try {
                compService.afterFormNext(form.getName(), (ComponentProperties) form.getProperties());
            } catch (Throwable e) {
                ExceptionHandler.process(e);
            }
        }
        super.nextPressed();
    }

    @Override
    protected void backPressed() {
        Form form = getForm();
        if (form != null && form.isCallAfterFormBack()) {
            try {
                compService.afterFormBack(form.getName(), (ComponentProperties) form.getProperties());
            } catch (Throwable e) {
                ExceptionHandler.process(e);
            }
        }
        super.backPressed();
    }

    @Override
    public void updateButtons() {
        super.updateButtons();
        Form form = getForm();
        Button nextButton = getButton(IDialogConstants.NEXT_ID);
        if (nextButton != null && nextButton.isEnabled()) {
            nextButton.setEnabled(form.isAllowForward());
        }
        Button backButton = getButton(IDialogConstants.BACK_ID);
        if (backButton != null && backButton.isEnabled()) {
            backButton.setEnabled(form.isAllowBack());
        }
        Button finishButton = getButton(IDialogConstants.FINISH_ID);
        if (finishButton != null && finishButton.isEnabled()) {
            finishButton.setEnabled(form.isAllowFinish());
        }
    }

    private Form getForm() {
        Form form = null;
        IWizardPage currentPage = getCurrentPage();
        if (currentPage instanceof GenericWizardPage) {
            GenericWizardPage genericWizardPage = (GenericWizardPage) currentPage;
            form = genericWizardPage.getForm();
        }
        return form;
    }

    public void setCompService(ComponentService compService) {
        this.compService = compService;
    }

}
