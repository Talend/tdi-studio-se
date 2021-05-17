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

import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.swt.dialogs.ErrorDialogWidthDetailArea;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.builder.ConvertionHelper;
import org.talend.core.model.metadata.builder.connection.MetadataTable;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.update.RepositoryUpdateManager;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.metadata.managment.ui.wizard.AbstractRepositoryFileTableWizard;
import org.talend.repository.model.IProxyRepositoryFactory;

/**
 * DOC hwang class global comment.
 *
 * $Id: FileJSONTableWizard.java $
 *
 */
public class FileJSONTableWizard extends AbstractRepositoryFileTableWizard implements INewWizard {

    private static Logger log = Logger.getLogger(FileJSONTableWizard.class);

    private FileJSONTableWizardPage tableWizardpage;

    private Map<String, String> oldTableMap;

    private IMetadataTable oldMetadataTable;

    /**
     * Constructor for TableWizard.
     *
     * @param ISelection
     */
    @SuppressWarnings("unchecked")//$NON-NLS-1$
    public FileJSONTableWizard(IWorkbench workbench, boolean creation, ConnectionItem connectionItem,
            MetadataTable metadataTable, boolean forceReadOnly) {
        super(workbench, creation, forceReadOnly);
        this.connectionItem = connectionItem;
        this.metadataTable = metadataTable;
        if (connectionItem != null) {
            oldTableMap = RepositoryUpdateManager.getOldTableIdAndNameMap(connectionItem, metadataTable, creation);
            oldMetadataTable = ConvertionHelper.convert(metadataTable);
            // initConnectionCopy(connectionItem.getConnection());
        }
        setNeedsProgressMonitor(true);

        isRepositoryObjectEditable();
        initLockStrategy();
    }

    /**
     * Adding the page to the wizard.
     */

    public void addPages() {
        setWindowTitle("Schema");

        tableWizardpage = new FileJSONTableWizardPage(connectionItem, metadataTable, isRepositoryObjectEditable());

        if (creation) {
            tableWizardpage.setTitle("New Schema on " + connectionItem.getProperty().getLabel());
            tableWizardpage.setDescription("Add a Schema in repository");
            tableWizardpage.setPageComplete(false);
        } else {
            tableWizardpage.setTitle("Update Schema " + metadataTable.getLabel());
            tableWizardpage.setDescription("Update an existing Schema in repository");
            tableWizardpage.setPageComplete(isRepositoryObjectEditable());
        }
        addPage(tableWizardpage);
    }

    /**
     * This method determine if the 'Finish' button is enable This method is called when 'Finish' button is pressed in
     * the wizard. We will create an operation and run it using wizard as execution context.
     */
    public boolean performFinish() {
        if (tableWizardpage.isPageComplete()) {
            // applyConnectionCopy();
            // update
            RepositoryUpdateManager.updateSingleSchema(connectionItem, metadataTable, oldMetadataTable, oldTableMap);

            IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
            try {
                factory.save(repositoryObject.getProperty().getItem());
                closeLockStrategy();
            } catch (PersistenceException e) {
                String detailError = e.toString();
                new ErrorDialogWidthDetailArea(getShell(), PID, "Access to the data failure ", detailError);
                log.error("Access to the data failure " + "\n" + detailError);
            }
            // connectionCopy = null;
            // metadataTableCopy = null;
            return true;
        } else {
            return false;
        }

    }

    public boolean performCancel() {
        if (metadataTable != null && oldMetadataTable != null && metadataTable.getLabel() != null
                && !metadataTable.getLabel().equals(oldMetadataTable.getLabel())) {
            this.metadataTable.setLabel(oldMetadataTable.getLabel());
        }
        return super.performCancel();
    }

    /**
     * We will accept the selection in the workbench to see if we can initialize from it.
     *
     * @see IWorkbenchWizard#init(IWorkbench, IStructuredSelection)
     */
    public void init(final IWorkbench workbench, final IStructuredSelection selection) {
        this.selection = selection;
    }

    @Override
    public ConnectionItem getConnectionItem() {
        return this.connectionItem;
    }

}
