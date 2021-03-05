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

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.widgets.Composite;
import org.talend.core.model.metadata.builder.connection.MetadataTable;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.cwm.helper.ConnectionHelper;
import org.talend.cwm.helper.TableHelper;
import org.talend.metadata.managment.ui.wizard.AbstractForm;
import org.talend.repository.model.json.JSONFileConnection;

/**
 * wzhang class global comment. Detailled comment
 */
public class JSONFileOutputWizardPage extends JSONFileWizardPage {

    public JSONFileOutputWizardPage(boolean creation, int step, ConnectionItem connectionItem,
            boolean isRepositoryObjectEditable, String[] existingNames) {
        super(creation, step, connectionItem, isRepositoryObjectEditable, existingNames);
    }

    public void createControl(final Composite parent) {
        currentComposite = null;

        if (step == 1) {
            currentComposite = new JSONFileOutputStep1Form(creation, parent, connectionItem, existingNames);
        } else if (step == 2) {
            currentComposite = new JSONFileOutputStep2Form(creation, parent, connectionItem);
        } else if (step == 3) {
            MetadataTable metadataTable = (MetadataTable) ConnectionHelper.getTables(
                    (JSONFileConnection) connectionItem.getConnection()).toArray(new MetadataTable[0])[0];
            currentComposite = new JSONFileOutputStep3Form(parent, connectionItem, metadataTable, TableHelper.getTableNames(
                    ((JSONFileConnection) connectionItem.getConnection()), metadataTable.getLabel()));
        }

        currentComposite.setReadOnly(!isRepositoryObjectEditable);
        currentComposite.setPage(this);

        AbstractForm.ICheckListener listener = new AbstractForm.ICheckListener() {

            public void checkPerformed(final AbstractForm source) {

                if (source.isStatusOnError()) {
                    JSONFileOutputWizardPage.this.setPageComplete(false);
                    setErrorMessage(source.getStatus());
                } else {
                    JSONFileOutputWizardPage.this.setPageComplete(isRepositoryObjectEditable);
                    setErrorMessage(null);
                    setMessage(source.getStatus());
                }
            }
        };

        currentComposite.setListener(listener);
        setControl((Composite) currentComposite);
    }

    @Override
    public IWizardPage getPreviousPage() {
        if (step == 1) {
            return null;
        }
        return super.getPreviousPage();
    }

}
