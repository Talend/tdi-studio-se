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
package org.talend.repository.ui.wizards.sqlpattern;

import org.eclipse.core.runtime.IPath;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.ItemState;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.metadata.managment.ui.wizard.PropertiesWizardPage;
import org.talend.repository.i18n.Messages;

/**
 * Page for new project details. <br/>
 *
 * $Id: NewProcessWizardPage.java 1 2006-09-29 17:06:40 +0000 (ven., 29 sept. 2006) nrousseau $
 *
 */
public class NewSqlpatternWizardPage extends PropertiesWizardPage {

    /**
     * Constructs a new NewProjectWizardPage.
     *
     */
    public NewSqlpatternWizardPage(Property property, IPath destinationPath) {
        super("WizardPage", property, destinationPath); //$NON-NLS-1$
        setTitle(Messages.getString("NewSqlpatternWizardPage.title")); //$NON-NLS-1$
        setDescription(Messages.getString("NewSqlpatternWizardPage.description")); //$NON-NLS-1$
    }

    /**
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
     */
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
    public boolean isValid(String itemName) {
        IPath path = getDestinationPath();
        Item item = property.getItem();
        if (item.getState() == null) {
            ItemState itemState = PropertiesFactory.eINSTANCE.createItemState();
            item.setState(itemState);
        }
        item.getState().setPath(path.toString());
        return super.isValid(itemName);
    }

    @Override
    public ERepositoryObjectType getRepositoryObjectType() {
        return ERepositoryObjectType.SQLPATTERNS;
    }
}
