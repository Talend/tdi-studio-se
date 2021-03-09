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
package org.talend.repository.json.ui.wizards;

import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Composite;
import org.talend.core.model.metadata.builder.connection.MetadataTable;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.xml.XmlArray;
import org.talend.cwm.helper.ConnectionHelper;
import org.talend.cwm.helper.TableHelper;
import org.talend.metadata.managment.ui.wizard.AbstractForm;
import org.talend.repository.model.json.JSONFileConnection;

/**
 * DOC wanghong class global comment. Detailled comment
 */
public class JSONFileWizardPage extends WizardPage {

    protected ConnectionItem connectionItem;

    protected int step;

    protected boolean creation;

    protected AbstractJSONFileStepForm currentComposite;

    protected final String[] existingNames;

    protected boolean isRepositoryObjectEditable;

    /**
     * DOC ocarbone JSONFileWizardPage constructor comment.
     *
     * @param step
     * @param connection
     * @param isRepositoryObjectEditable
     * @param existingNames
     */
    public JSONFileWizardPage(int step, ConnectionItem connectionItem, boolean isRepositoryObjectEditable, String[] existingNames) {
        super("wizardPage"); //$NON-NLS-1$
        this.step = step;
        this.connectionItem = connectionItem;
        this.existingNames = existingNames;
        this.isRepositoryObjectEditable = isRepositoryObjectEditable;
    }

    public JSONFileWizardPage(boolean creation, ConnectionItem connectionItem, boolean isRepositoryObjectEditable,
            String[] existingNames) {
        super("wizardPage"); //$NON-NLS-1$
        this.creation = creation;
        this.connectionItem = connectionItem;
        this.existingNames = existingNames;
        this.isRepositoryObjectEditable = isRepositoryObjectEditable;
    }

    public JSONFileWizardPage(boolean creation, int step, ConnectionItem connectionItem, boolean isRepositoryObjectEditable,
            String[] existingNames) {
        this(step, connectionItem, isRepositoryObjectEditable, existingNames);
        this.creation = creation;
    }

    /**
     *
     * @see IDialogPage#createControl(Composite)
     */
    public void createControl(final Composite parent) {
        currentComposite = null;

        if (step == 1) {
            currentComposite = new JSONFileStep1Form(creation, parent, connectionItem, existingNames, (JSONWizard) getWizard());
        } else if (step == 2) {
            currentComposite = new JSONFileStep2Form(parent, connectionItem, (JSONWizard) getWizard());
        } else if (step == 3) {
            MetadataTable metadataTable = ConnectionHelper.getTables(connectionItem.getConnection())
                    .toArray(new MetadataTable[0])[0];
            currentComposite = new JSONFileStep3Form(parent, connectionItem, metadataTable, (JSONWizard) getWizard(),
                    TableHelper.getTableNames(((JSONFileConnection) connectionItem.getConnection()), metadataTable.getLabel()));
        }

        currentComposite.setReadOnly(!isRepositoryObjectEditable);
        currentComposite.setPage(this);

        AbstractForm.ICheckListener listener = new AbstractForm.ICheckListener() {

            public void checkPerformed(final AbstractForm source) {

                if (source.isStatusOnError()) {
                    JSONFileWizardPage.this.setPageComplete(false);
                    setErrorMessage(source.getStatus());
                } else {
                    JSONFileWizardPage.this.setPageComplete(isRepositoryObjectEditable);
                    setErrorMessage(null);
                    setMessage(source.getStatus());
                }
            }
        };

        currentComposite.setListener(listener);
        setControl((Composite) currentComposite);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.dialogs.DialogPage#dispose()
     */
    @Override
    public void dispose() {
        XmlArray.setLimitToDefault();
        super.dispose();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.dialogs.DialogPage#setVisible(boolean)
     */
    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) {
            ((JSONWizard) getWizard()).setCurrentPage(this);
        }
    }

    @Override
    public IWizardPage getPreviousPage() {
        if (step == 1) {
            return null;
        }
        return super.getPreviousPage();
    }

}
