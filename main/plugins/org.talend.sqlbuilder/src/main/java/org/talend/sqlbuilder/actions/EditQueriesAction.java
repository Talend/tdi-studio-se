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
package org.talend.sqlbuilder.actions;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.talend.commons.runtime.model.repository.ERepositoryStatus;
import org.talend.commons.ui.gmf.util.DisplayUtils;
import org.talend.commons.ui.runtime.image.ECoreImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.PluginChecker;
import org.talend.core.database.EDatabaseTypeName;
import org.talend.core.model.context.ContextUtils;
import org.talend.core.model.metadata.IMetadataConnection;
import org.talend.core.model.metadata.builder.ConvertionHelper;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.metadata.builder.connection.DatabaseConnection;
import org.talend.core.model.properties.ContextItem;
import org.talend.core.model.properties.DatabaseConnectionItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.repository.RepositoryManager;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.repository.model.repositoryObject.MetadataTableRepositoryObject;
import org.talend.core.repository.model.repositoryObject.QueryEMFRepositoryNode;
import org.talend.core.repository.model.repositoryObject.QueryRepositoryObject;
import org.talend.core.sqlbuilder.util.ConnectionParameters;
import org.talend.core.sqlbuilder.util.TextUtil;
import org.talend.core.ui.ICDCProviderService;
import org.talend.cwm.helper.SubItemHelper;
import org.talend.metadata.managment.ui.wizard.metadata.ContextSetsSelectionDialog;
import org.talend.repository.ProjectManager;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.IRepositoryService;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.ui.actions.AContextualAction;
import org.talend.repository.ui.views.IRepositoryView;
import org.talend.sqlbuilder.Messages;
import org.talend.sqlbuilder.ui.SQLBuilderDialog;
import org.talend.sqlbuilder.util.UIUtils;

/**
 * DOC smallet class global comment. Detailled comment <br/>
 *
 * $Id: talend.epf 1 2006-09-29 17:06:40 +0000 (ven., 29 sept. 2006) nrousseau $
 *
 */
public class EditQueriesAction extends AContextualAction {

    private RepositoryNode repositoryNode;

    public EditQueriesAction() {
        super();
        setImageDescriptor(ImageProvider.getImageDesc(ECoreImage.METADATA_QUERY_ICON));
        setText(Messages.getString("EditQueriesAction.textEditQueries")); //$NON-NLS-1$
    }

    @Override
    protected void doRun() {
        IStructuredSelection selection = (IStructuredSelection) getSelection();
        if (repositoryNode == null && selection != null) {
            repositoryNode = (RepositoryNode) selection.getFirstElement();
        }
        IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();

        IRepositoryViewObject nodeObject = repositoryNode.getObject();

        boolean locked = false;

        if (!factory.getRepositoryContext().isEditableAsReadOnly()) {
            if (nodeObject.getRepositoryStatus() == ERepositoryStatus.LOCK_BY_OTHER) {
                locked = true;
            }
        }
        // Avoid to delete node which is locked.
        if (locked || RepositoryManager.isOpenedItemInEditor(nodeObject)) {

            final String title = "Impossible to edit queries";
            String nodeName = nodeObject.getRepositoryObjectType().getLabel();
            final String message = "item is already locked by another user.";
            Display.getDefault().syncExec(new Runnable() {

                @Override
                public void run() {
                    MessageDialog dialog = new MessageDialog(DisplayUtils.getDefaultShell(false), title, null, message,
                            MessageDialog.ERROR,
                            new String[] { IDialogConstants.OK_LABEL }, 0);
                    dialog.open();
                }
            });
            return;
        }

        DatabaseConnectionItem dbConnectionItem = null;
        boolean readOnly = false;

        ConnectionParameters connParameters = new ConnectionParameters();
        if (repositoryNode.getContentType() == ERepositoryObjectType.METADATA_CONNECTIONS) {
            dbConnectionItem = (DatabaseConnectionItem) repositoryNode.getObject().getProperty().getItem();
            connParameters.setRepositoryName(repositoryNode.getObject().getLabel());
            connParameters.setRepositoryId(repositoryNode.getObject().getId());
            connParameters.setQuery(""); //$NON-NLS-1$
        } else if (repositoryNode.getObjectType() == ERepositoryObjectType.METADATA_CON_QUERY) {
            QueryRepositoryObject queryRepositoryObject = (QueryRepositoryObject) repositoryNode.getObject();
            readOnly = SubItemHelper.isDeleted(queryRepositoryObject.getAbstractMetadataObject());
            dbConnectionItem = (DatabaseConnectionItem) queryRepositoryObject.getProperty().getItem();
            connParameters.setRepositoryName(dbConnectionItem.getProperty().getLabel());
            connParameters.setRepositoryId(dbConnectionItem.getProperty().getId());
            connParameters.setQueryObject(queryRepositoryObject.getQuery());
            connParameters.setQuery(queryRepositoryObject.getQuery().getValue());
            connParameters.setFirstOpenSqlBuilder(true); // first open Sql Builder,set true
        } else if (repositoryNode.getObjectType() == ERepositoryObjectType.METADATA_CON_TABLE) {
            dbConnectionItem = (DatabaseConnectionItem) repositoryNode.getObject().getProperty().getItem();
            connParameters.setRepositoryName(dbConnectionItem.getProperty().getLabel());
            connParameters.setRepositoryId(dbConnectionItem.getProperty().getId());
            connParameters.setMetadataTable((MetadataTableRepositoryObject) repositoryNode.getObject());
            connParameters.setQuery(""); //$NON-NLS-1$
        }

        IRepositoryView viewPart = getViewPart();
        Display display = null;
        if (viewPart != null) {
            display = viewPart.getViewer().getControl().getDisplay();
        } else {
            display = Display.getCurrent();
            if (display == null) {
                display = Display.getDefault();
            }
        }
        Shell parentShell = DisplayUtils.getDefaultShell(false);
        TextUtil.setDialogTitle(TextUtil.SQL_BUILDER_TITLE_REP);

        Connection connection = dbConnectionItem.getConnection();
        String selectedContext = null;
        if (connection.isContextMode()) {
            ContextItem contextItem = ContextUtils.getContextItemById2(connection.getContextId());
            if (contextItem != null && connection.isContextMode()) {

                ContextSetsSelectionDialog setsDialog = new ContextSetsSelectionDialog(null, contextItem, false);
                setsDialog.open();
                selectedContext = setsDialog.getSelectedContext();
            }
        }
        SQLBuilderDialog dial = new SQLBuilderDialog(parentShell, repositoryNode, selectedContext);

        dial.setReadOnly(readOnly);

        if (connection instanceof DatabaseConnection) {
            IMetadataConnection imetadataConnection = ConvertionHelper.convert(connection, true);
            connParameters.setSchema(imetadataConnection.getSchema() == null ? "" : imetadataConnection.getSchema());
            UIUtils.checkConnection(parentShell, imetadataConnection);
        }

        connParameters.setNodeReadOnly(readOnly);
        connParameters.setFromRepository(true);
        dial.setConnParameters(connParameters);
        dial.open();
        IRepositoryView view = getViewPart();
        if (view != null) {
            view.refreshView();
        }
    }

    @Override
    public void init(TreeViewer viewer, IStructuredSelection selection) {
        boolean canWork = !selection.isEmpty() && selection.size() == 1;
        IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
        if (factory.isUserReadOnlyOnCurrentProject()) {
            canWork = false;
        }
        if (canWork) {

            Object o = selection.getFirstElement();
            repositoryNode = (RepositoryNode) o;
            switch (repositoryNode.getType()) {
            case REPOSITORY_ELEMENT:
                if (repositoryNode.getObject().getRepositoryStatus() == ERepositoryStatus.DELETED
                        || repositoryNode.getObject().getRepositoryStatus() == ERepositoryStatus.LOCK_BY_OTHER) {
                    canWork = false;
                }
                // Studio does not support this action for hive, TDI-25365.
                // Studio does not support this action for impala, TBD-3827.
                if (!isUnderDBConnection(repositoryNode)) {
                    canWork = false;
                }
                if (repositoryNode.getContentType() != ERepositoryObjectType.METADATA_CONNECTIONS
                        && repositoryNode.getObjectType() != ERepositoryObjectType.METADATA_CON_QUERY
                        && repositoryNode.getObjectType() != ERepositoryObjectType.METADATA_CON_TABLE) {
                    canWork = false;
                } else {
                    // for cdc
                    if (PluginChecker.isCDCPluginLoaded()) {
                        ICDCProviderService cdcService = (ICDCProviderService) GlobalServiceRegister.getDefault().getService(
                                ICDCProviderService.class);
                        if (cdcService != null && cdcService.isSubscriberTableNode(repositoryNode)) {
                            canWork = false;
                            break;
                        }
                    }
                }
                if (canWork) {
                    Item item = repositoryNode.getObject().getProperty().getItem();
                    if (item instanceof DatabaseConnectionItem) {
                        DatabaseConnectionItem dbItem = (DatabaseConnectionItem) item;
                        DatabaseConnection dbConn = (DatabaseConnection) dbItem.getConnection();
                        String dbType = dbConn.getDatabaseType();
                        if (EDatabaseTypeName.HIVE.getXmlName().equalsIgnoreCase(dbType)
                                || EDatabaseTypeName.HBASE.getXmlName().equalsIgnoreCase(dbType)
                                || EDatabaseTypeName.MAPRDB.getXmlName().equalsIgnoreCase(dbType)
                                || EDatabaseTypeName.IMPALA.getXmlName().equalsIgnoreCase(dbType)) {
                            canWork = false;
                            break;
                        }
                    }
                }
                break;
            default:
                canWork = false;
            }
            if (canWork
                    && (!ProjectManager.getInstance().isInCurrentMainProject(repositoryNode) || !isLastVersion(repositoryNode))) {
                canWork = false;
            }
        }
        setEnabled(canWork);
    }

    @Override
    public Class getClassForDoubleClick() {
        return QueryEMFRepositoryNode.class;
    }
}
