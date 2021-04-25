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

import org.eclipse.ui.wizards.datatransfer.ExternalProjectImportWizard;

/**
 * DOC zhangchao.wang class global comment. Detailled comment
 */
public class TalendExternalProjectImportWizard extends ExternalProjectImportWizard {

    private TalendWizardProjectsImportPage mainPage;

    public TalendExternalProjectImportWizard() {
        super();
    }

    public void addPages() {
        mainPage = new TalendWizardProjectsImportPage();
        addPage(mainPage);
    }
}
