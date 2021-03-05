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
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Composite;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.metadata.builder.connection.MetadataTable;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.cwm.helper.TableHelper;
import org.talend.metadata.managment.ui.wizard.AbstractForm;
import org.talend.repository.model.json.JSONFileConnection;
import org.talend.repository.model.json.util.JsonSwitch;

import orgomg.cwm.objectmodel.core.Package;

/**
 * TableWizard present the TableForm width the MetaDataTable. Use to create a new table (need a connection to a DB).
 * Page allows setting a table.
 */
public class FileJSONTableWizardPage extends WizardPage {

    private MetadataTable metadataTable;

    private ConnectionItem connectionItem;

    private boolean isRepositoryObjectEditable;

    /**
     * DatabaseWizardPage constructor (to instance IMetadataConnection OR MetaDataTableType). If MetaDataTableType
     * exist, it's an update of existing metadata else it's a new metadata.
     *
     * @param existingNames
     *
     * @param ISelection
     */
    public FileJSONTableWizardPage(ConnectionItem connectionItem, MetadataTable metadataTable, boolean isRepositoryObjectEditable) {
        super("wizardPage"); //$NON-NLS-1$
        this.connectionItem = connectionItem;
        this.metadataTable = metadataTable;
        this.isRepositoryObjectEditable = isRepositoryObjectEditable;
    }

    /**
     * Create the first composite, addComponentsAndControls and initialize TableWizardPage.
     *
     * @see IDialogPage#createControl(Composite)
     */
    public void createControl(final Composite parent) {

        final AbstractForm.ICheckListener listener = new AbstractForm.ICheckListener() {

            public void checkPerformed(final AbstractForm source) {
                if (source.isStatusOnError()) {
                    FileJSONTableWizardPage.this.setPageComplete(false);
                    setErrorMessage(source.getStatus());
                } else {
                    FileJSONTableWizardPage.this.setPageComplete(isRepositoryObjectEditable);
                    setErrorMessage(null);
                    setMessage(source.getStatus(), source.getStatusLevel());
                }
            }
        };

        Composite theForm = null;
        Connection connection = null;
        if (metadataTable.getNamespace() != null) {
            if (metadataTable.getNamespace() instanceof Package) {
                Package pkg = (Package) metadataTable.getNamespace();
                if (!pkg.getDataManager().isEmpty()) {
                    connection = (Connection) pkg.getDataManager().get(0);
                }
            }
        }
        theForm = (Composite) new JsonSwitch() {

            public Object caseJSONFileConnection(final JSONFileConnection object) {
                JSONFileConnection jsonFileConnection = (JSONFileConnection) connectionItem.getConnection();
                boolean isInputModel = jsonFileConnection.isInputModel();
                if (isInputModel) {
                    JSONFileStep3Form xmlFileStep3Form = new JSONFileStep3Form(parent, connectionItem, metadataTable, null,
                            TableHelper.getTableNames(object, metadataTable.getLabel()));

                    xmlFileStep3Form.setReadOnly(!isRepositoryObjectEditable);
                    xmlFileStep3Form.setListener(listener);
                    return xmlFileStep3Form;
                } else {
                    JSONFileOutputStep3Form xmlFileOutputStep3Form = new JSONFileOutputStep3Form(parent, connectionItem,
                            metadataTable, TableHelper.getTableNames(object, metadataTable.getLabel()));
                    xmlFileOutputStep3Form.setReadOnly(!isRepositoryObjectEditable);
                    xmlFileOutputStep3Form.setListener(listener);
                    return xmlFileOutputStep3Form;
                }
            }

        }.doSwitch(connection);
        setControl(theForm);
    }
}
