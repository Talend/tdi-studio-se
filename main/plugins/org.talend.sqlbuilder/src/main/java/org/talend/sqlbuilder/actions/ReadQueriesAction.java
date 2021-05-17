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

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.talend.commons.runtime.model.repository.ERepositoryStatus;
import org.talend.commons.ui.gmf.util.DisplayUtils;
import org.talend.commons.ui.runtime.image.EImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.core.database.EDatabaseTypeName;
import org.talend.core.model.context.ContextUtils;
import org.talend.core.model.metadata.IMetadataConnection;
import org.talend.core.model.metadata.builder.ConvertionHelper;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.metadata.builder.connection.DatabaseConnection;
import org.talend.core.model.properties.ContextItem;
import org.talend.core.model.properties.DatabaseConnectionItem;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.repository.model.repositoryObject.QueryRepositoryObject;
import org.talend.core.sqlbuilder.util.ConnectionParameters;
import org.talend.core.sqlbuilder.util.TextUtil;
import org.talend.metadata.managment.ui.wizard.metadata.ContextSetsSelectionDialog;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.IRepositoryNode.EProperties;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.ui.actions.AContextualAction;
import org.talend.sqlbuilder.Messages;
import org.talend.sqlbuilder.ui.SQLBuilderDialog;
import org.talend.sqlbuilder.util.UIUtils;

/**
 * DOC smallet class global comment. Detailled comment <br/>
 *
 * $Id: talend.epf 1 2006-09-29 17:06:40 +0000 (ven., 29 sept. 2006) nrousseau $
 *
 */
public class ReadQueriesAction extends AContextualAction {

    private RepositoryNode repositoryNode;

    public ReadQueriesAction() {
        super();
        setText(Messages.getString("EditQueriesAction.textOpenQueries")); //$NON-NLS-1$
        setImageDescriptor(ImageProvider.getImageDesc(EImage.READ_ICON));
    }

    @Override
    protected void doRun() {
        IStructuredSelection selection = (IStructuredSelection) getSelection();
        IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
        if (repositoryNode == null && selection != null) {
            repositoryNode = (RepositoryNode) selection.getFirstElement();
        }

        DatabaseConnectionItem dbConnectionItem = null;
        ConnectionParameters connParameters = new ConnectionParameters();
        if (repositoryNode.getObjectType() == ERepositoryObjectType.METADATA_CONNECTIONS ||
                repositoryNode.getProperties(EProperties.CONTENT_TYPE) == ERepositoryObjectType.METADATA_CONNECTIONS) {
            dbConnectionItem = (DatabaseConnectionItem) repositoryNode.getObject().getProperty().getItem();
            connParameters.setRepositoryName(repositoryNode.getObject().getLabel());
            connParameters.setRepositoryId(repositoryNode.getObject().getId());
            connParameters.setQuery(""); //$NON-NLS-1$
        } else if (repositoryNode.getObjectType() == ERepositoryObjectType.METADATA_CON_QUERY) {
            QueryRepositoryObject queryRepositoryObject = (QueryRepositoryObject) repositoryNode.getObject();
            dbConnectionItem = (DatabaseConnectionItem) queryRepositoryObject.getProperty().getItem();
            connParameters.setRepositoryName(dbConnectionItem.getProperty().getLabel());
            connParameters.setRepositoryId(dbConnectionItem.getProperty().getId());
            connParameters.setQueryObject(queryRepositoryObject.getQuery());
            connParameters.setQuery(queryRepositoryObject.getQuery().getValue());
            connParameters.setFirstOpenSqlBuilder(true); // first open Sql Builder,set true
        }

        Display display = Display.getCurrent();
        if (display == null) {
            display = Display.getDefault();
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

        dial.setReadOnly(true);

        if (connection instanceof DatabaseConnection) {
            IMetadataConnection imetadataConnection = ConvertionHelper.convert(connection, true);
            connParameters.setSchema(imetadataConnection.getSchema() == null ? "" : imetadataConnection.getSchema());
            UIUtils.checkConnection(parentShell, imetadataConnection);
        }
        connParameters.setNodeReadOnly(true);
        connParameters.setFromRepository(true);
        dial.setConnParameters(connParameters);
        dial.open();
        refresh(repositoryNode);
    }

    @Override
    public void init(TreeViewer viewer, IStructuredSelection selection) {
        boolean canWork = !selection.isEmpty() && selection.size() == 1;
        if (canWork) {
            Object o = selection.getFirstElement();
            repositoryNode = (RepositoryNode) o;

            ERepositoryObjectType repObjType = (ERepositoryObjectType) repositoryNode.getProperties(EProperties.CONTENT_TYPE);
            switch (repositoryNode.getType()) {
            case REPOSITORY_ELEMENT:
                if (repositoryNode.getObject().getRepositoryStatus() == ERepositoryStatus.DELETED) {
                    canWork = false;
                }
                if (repObjType != ERepositoryObjectType.METADATA_CONNECTIONS
                        && repObjType != ERepositoryObjectType.METADATA_CON_QUERY) {
                    canWork = false;
                }
                // Studio does not support this action for hive, TDI-25365.
                // Studio does not support this action for impala, TBD-3827.
                if (canWork) {
                    if (isUnderDBConnection(repositoryNode)) {
                        DatabaseConnectionItem item = (DatabaseConnectionItem) repositoryNode.getObject().getProperty().getItem();
                        DatabaseConnection dbConn = (DatabaseConnection) item.getConnection();
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
            if (canWork && !isLastVersion(repositoryNode)) {
                canWork = false;
            }
        }
        setEnabled(canWork);
    }
}
