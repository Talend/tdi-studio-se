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
package org.talend.repository.ftp.ui.wizards.pags;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.metadata.managment.ui.wizard.AbstractForm;
import org.talend.repository.ftp.i18n.Messages;
import org.talend.repository.ftp.ui.wizards.forms.FTPForm;

/**
 * DOC hwang class global comment. Detailled comment
 */
public class FTPPage extends WizardPage {

    private FTPForm ftpForm;

    private ConnectionItem connectionItem;

    private String[] existingNames;

    private boolean isRepositoryObjectEditable;

    /**
     * DOC Administrator FTPPage constructor comment.
     *
     * @param pageName
     */
    public FTPPage(ConnectionItem connectionItem, boolean isRepositoryObjectEditable, String[] existingNames) {
        super("wizardPage"); //$NON-NLS-1$
        this.connectionItem = connectionItem;
        this.existingNames = existingNames;
        this.isRepositoryObjectEditable = isRepositoryObjectEditable;
        this.setTitle(Messages.getString("FTPPage_titleCreate_Step")); //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
     */

    @Override
    public void createControl(Composite parent) {
        ScrolledComposite scrolledComposite = new ScrolledComposite(parent, SWT.VERTICAL);
        scrolledComposite.setExpandHorizontal(true);
        scrolledComposite.setExpandVertical(true);
        scrolledComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
        ftpForm = new FTPForm(scrolledComposite, connectionItem, existingNames, this);
        scrolledComposite.setContent(ftpForm);
        scrolledComposite.setMinSize(ftpForm.computeSize(SWT.DEFAULT, 550));
        ftpForm.setReadOnly(!isRepositoryObjectEditable);

        AbstractForm.ICheckListener listener = new AbstractForm.ICheckListener() {

            @Override
            public void checkPerformed(final AbstractForm source) {
                if (source.isStatusOnError()) {
                    FTPPage.this.setPageComplete(false);
                    setErrorMessage(source.getStatus());
                } else {
                    FTPPage.this.setPageComplete(isRepositoryObjectEditable);
                    setErrorMessage(null);
                    setMessage(source.getStatus(), source.getStatusLevel());
                }
            }
        };
        ftpForm.setListener(listener);
        setControl(ftpForm);
        if (connectionItem.getProperty().getLabel() != null && !connectionItem.getProperty().getLabel().equals("")) { //$NON-NLS-1$
            ftpForm.checkFieldsValue();
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.wizard.WizardPage#getNextPage()
     */

    @Override
    public IWizardPage getNextPage() {
        ftpForm.removeHideValue();
        return super.getNextPage();
    }

}
