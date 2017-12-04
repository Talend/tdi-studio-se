// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.componentdesigner.ui.wizard.creatcomponent;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Composite;
import org.talend.componentdesigner.model.componentpref.ComponentPref;

/**
 * DOC rli class global comment. Detailled comment
 */
public abstract class AbstractComponentPage extends WizardPage {

    protected final ComponentPref componentPref;

    // protected PropertyChangeBean propertyChangeBean;

    protected AbstractComponentPage(String pageName, ComponentPref componentPref) {
        super(pageName);
        this.componentPref = componentPref;
        // propertyChangeBean = new PropertyChangeBean();
    }

    // PropertyChangeBean getPropertyChangeBean() {
    // return propertyChangeBean;
    // }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    public void createControl(Composite parent) {
        this.createPageContent(parent);
        this.initialize();
    }

    protected abstract void createPageContent(Composite parent);

    protected abstract void initialize();

    protected abstract boolean validatePage();

}
