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
package org.talend.repository.ui.wizards.routinesjar;

import org.eclipse.core.runtime.IPath;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.metadata.managment.ui.wizard.PropertiesWizard;
import org.talend.repository.i18n.Messages;


public class EditRoutinesJarPropertiesWizard extends PropertiesWizard {

    public EditRoutinesJarPropertiesWizard(IRepositoryViewObject repositoryViewObject, IPath path, boolean useLastVersion) {
        super(repositoryViewObject, path, useLastVersion);
        setWindowTitle(Messages.getString("EditCodesJarPropertiesWizard.wizard.title"));//$NON-NLS-1$
    }

    @Override
    public void addPages() {
        mainPage = new EditRoutinesJarPropertiesWizardPage(Messages.getString("EditCodesJarPropertiesWizard.pageName"), //$NON-NLS-1$
                object.getProperty(), path, isReadOnly(), false, lastVersionFound);
        mainPage.setItem(object.getProperty().getItem());
        // If required to add a converter, then add here.
        addPage(mainPage);
    }

}
