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
package org.talend.repository.generic.ui;

import java.util.Map;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.swt.dialogs.ErrorDialogWidthDetailArea;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.MetadataToolHelper;
import org.talend.core.model.metadata.builder.ConvertionHelper;
import org.talend.core.model.metadata.builder.connection.MetadataTable;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.runtime.CoreRuntimePlugin;
import org.talend.designer.core.generic.constants.IGenericConstants;
import org.talend.designer.core.generic.utils.SchemaUtils;
import org.talend.metadata.managment.ui.wizard.CheckLastVersionRepositoryWizard;
import org.talend.repository.generic.i18n.Messages;
import org.talend.repository.generic.update.GenericUpdateManager;
import org.talend.repository.model.IProxyRepositoryFactory;

/**
 *
 * created by ycbai on 2015年10月28日 Detailled comment
 *
 */
public class GenericSchemaWizard extends CheckLastVersionRepositoryWizard implements INewWizard {

    private GenericSchemaWizardPage tableWizardPage;

    private Map<String, String> oldTableMap;

    private IMetadataTable oldMetadataTable;

    public GenericSchemaWizard(IWorkbench workbench, boolean creation, IRepositoryViewObject object,
            ConnectionItem connectionItem, MetadataTable metadataTable, boolean forceReadOnly) {
        super(workbench, creation, forceReadOnly);
        this.connectionItem = connectionItem;
        this.metadataTable = metadataTable;
        if (connectionItem != null) {
            oldTableMap = GenericUpdateManager.getOldTableIdAndNameMap(connectionItem, metadataTable, creation);
            oldMetadataTable = ConvertionHelper.convert(metadataTable);
        }
        setNeedsProgressMonitor(true);
        setRepositoryObject(object);
        initLockStrategy();
    }

    @Override
    public void addPages() {
        setWindowTitle(Messages.getString("GenericSchemaWizard.windowTitle")); //$NON-NLS-1$
        tableWizardPage = new GenericSchemaWizardPage(connectionItem, isRepositoryObjectEditable(), metadataTable);
        if (creation) {
            tableWizardPage.setTitle(Messages.getString(
                    "GenericSchemaWizardPage.titleCreate", connectionItem.getProperty().getLabel())); //$NON-NLS-1$
            tableWizardPage.setDescription(Messages.getString("GenericSchemaWizardPage.descriptionCreate")); //$NON-NLS-1$
            tableWizardPage.setPageComplete(false);
        } else {
            tableWizardPage.setTitle(Messages.getString("GenericSchemaWizardPage.titleUpdate", metadataTable.getLabel())); //$NON-NLS-1$
            tableWizardPage.setDescription(Messages.getString("GenericSchemaWizardPage.descriptionUpdate")); //$NON-NLS-1$
            tableWizardPage.setPageComplete(isRepositoryObjectEditable());
        }
        addPage(tableWizardPage);
    }

    /**
     * This method determine if the 'Finish' button is enable This method is called when 'Finish' button is pressed in
     * the wizard. We will create an operation and run it using wizard as execution context.
     */
    @Override
    public boolean performFinish() {
        if (tableWizardPage.isPageComplete()) {
            IMetadataTable newTable = MetadataToolHelper.convert(metadataTable);
            if (!newTable.sameMetadataAs(oldMetadataTable)) {
                SchemaUtils.updateComponentSchema(metadataTable, connectionItem.getConnection());
            }
            GenericUpdateManager.updateSingleSchema(connectionItem, metadataTable, oldMetadataTable, oldTableMap);
            IProxyRepositoryFactory factory = CoreRuntimePlugin.getInstance().getProxyRepositoryFactory();
            try {
                factory.save(connectionItem);
                closeLockStrategy();
            } catch (PersistenceException e) {
                new ErrorDialogWidthDetailArea(getShell(), IGenericConstants.REPOSITORY_PLUGIN_ID,
                        Messages.getString("NoSQLSchemaWizard.persistenceException"), ExceptionUtils.getFullStackTrace(e)); //$NON-NLS-1$
                ExceptionHandler.process(e);
            }
            return true;
        } else {
            return false;
        }

    }

    @Override
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
    @Override
    public void init(final IWorkbench workbench, final IStructuredSelection sel) {
        this.selection = sel;
    }

    @Override
    public ConnectionItem getConnectionItem() {
        return this.connectionItem;
    }
}
