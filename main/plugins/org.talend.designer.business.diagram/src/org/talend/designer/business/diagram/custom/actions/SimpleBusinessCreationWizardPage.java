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
package org.talend.designer.business.diagram.custom.actions;

import org.eclipse.core.runtime.IPath;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.designer.business.diagram.i18n.Messages;
import org.talend.metadata.managment.ui.wizard.PropertiesWizardPage;

public class SimpleBusinessCreationWizardPage extends PropertiesWizardPage {

    public SimpleBusinessCreationWizardPage(Property property, IPath destinationPath) {
        super(Messages.getString("SimpleBusinessCreationWizardPage.Super"), property, destinationPath); //$NON-NLS-1$

        setTitle(Messages.getString("SimpleBusinessCreationWizardPage.Title")); //$NON-NLS-1$
    }

    @Override
    public void createControl(Composite parent) {
        Composite container = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(2, false);
        container.setLayout(layout);

        super.createControl(container);

        setControl(container);
        updateContent();
        addListeners();
        setPageComplete(false);
    }

    @Override
    public ERepositoryObjectType getRepositoryObjectType() {
        return ERepositoryObjectType.BUSINESS_PROCESS;
    }
}
