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
package org.talend.repository.ui.login;

import org.eclipse.jface.wizard.Wizard;
import org.talend.repository.i18n.Messages;

/**
 * DOC Administrator class global comment. Detailled comment
 */
public class NewImportProjectWizard extends Wizard {

    private NewImportProjectWizardPage mainPage;

    private String projectName;

    private String technicalName;

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.wizard.Wizard#performFinish()
     */
    @Override
    public boolean performFinish() {
        projectName = mainPage.getProjectName();
        technicalName = mainPage.getTechnicalName();
        return true;
    }

    @Override
    public void addPages() {
        mainPage = new NewImportProjectWizardPage();
        addPage(mainPage);
        setWindowTitle(Messages.getString("NewImportProjectWizard.windowTitle")); //$NON-NLS-1$
    }

    public String getName() {
        return projectName;
    }

    public String getTechnicalName() {
        return technicalName;
    }

}
