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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.emf.common.util.EList;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.actions.SelectionProviderAction;
import org.talend.core.model.metadata.builder.connection.DatabaseConnection;
import org.talend.core.model.metadata.builder.connection.MetadataColumn;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.sqlbuilder.util.ConnectionParameters;
import org.talend.core.sqlbuilder.util.TextUtil;
import org.talend.repository.model.IRepositoryNode;
import org.talend.repository.model.IRepositoryNode.EProperties;
import org.talend.repository.model.RepositoryNode;
import org.talend.sqlbuilder.Messages;
import org.talend.sqlbuilder.SqlBuilderPlugin;
import org.talend.sqlbuilder.dbstructure.DBTreeProvider.MetadataTableRepositoryObject;
import org.talend.sqlbuilder.dbstructure.RepositoryNodeType;
import org.talend.sqlbuilder.dbstructure.SqlBuilderRepositoryObject;
import org.talend.sqlbuilder.repository.utility.SQLBuilderRepositoryNodeManager;
import org.talend.sqlbuilder.ui.ISQLBuilderDialog;
import org.talend.sqlbuilder.util.ImageUtil;

/**
 * Detailled comment for this class. <br/>
 * $Id: GenerateSelectSQLAction.java,v 1.13 2006/11/09 07:24:13 tangfn Exp $
 *
 * @author phou
 *
 */
public class GenerateSelectSQLAction extends SelectionProviderAction {

    private static final ImageDescriptor SQL_EDITOR_IMAGE = ImageUtil.getDescriptor("Images.SqlEditorIcon"); //$NON-NLS-1$

    private SQLBuilderRepositoryNodeManager repositoryNodeManager = new SQLBuilderRepositoryNodeManager();

    private List<IRepositoryNode> selectedNodes;

    private ISelectionProvider provider;

    private ISQLBuilderDialog dialog;

    private boolean isDefaultEditor;

    @SuppressWarnings("unchecked")//$NON-NLS-1$
    public GenerateSelectSQLAction(ISelectionProvider provider, ISQLBuilderDialog dialog, boolean isDefaultEditor) {
        super(provider, Messages.getString("GenerateSelectSQLAction.textCenerateSelectStatement")); //$NON-NLS-1$
        this.provider = provider;
        this.dialog = dialog;
        this.isDefaultEditor = isDefaultEditor;
        selectedNodes = new ArrayList<IRepositoryNode>();
        init();
    }

    @Override
    public void selectionChanged(IStructuredSelection selection) {
        init();
    }

    @SuppressWarnings("unchecked")//$NON-NLS-1$
    public void init() {
        selectedNodes.clear();
        IStructuredSelection structuredSelection = (IStructuredSelection) provider.getSelection();
        for (int i = 0; i < structuredSelection.toList().size(); i++) {
            RepositoryNode repositoryNode = (RepositoryNode) structuredSelection.toList().get(i);
            if (SQLBuilderRepositoryNodeManager.getRepositoryType(repositoryNode) == RepositoryNodeType.FOLDER) {
                this.setEnabled(false);
                return;
            }
            RepositoryNode rootNode = SQLBuilderRepositoryNodeManager.getRoot(repositoryNode);
            if (!selectedNodes.isEmpty() && selectedNodes.get(0) != null
                    && !rootNode.equals(SQLBuilderRepositoryNodeManager.getRoot((RepositoryNode) selectedNodes.get(0)))) {
                setEnabled(false);
                return;
            }
            selectedNodes.add(repositoryNode);
        }
        if (selectedNodes.isEmpty()) {
            this.setEnabled(false);
            return;
        }
        this.setEnabled(true);
        for (IRepositoryNode node : selectedNodes) {
            Object type = node.getProperties(EProperties.CONTENT_TYPE);

            if (type != RepositoryNodeType.COLUMN && type != RepositoryNodeType.TABLE && type != RepositoryNodeType.DATABASE) {
                setEnabled(false);
                break;
            }
        }
    }

    /**
     * run.
     */
    @Override
    public void run() {
        if (selectedNodes.isEmpty()) {
            return;
        }
        try {
            // String query = null;
            // ErDiagramDialog erDiagramDialog = new ErDiagramDialog(dialog.getShell(), Messages
            // .getString("GenerateSelectSQLAction.textCenerateSelectStatement")); //$NON-NLS-1$
            // erDiagramDialog.setNodes(selectedNodes);
            // erDiagramDialog.setSqlText(null);
            // if (Window.OK == erDiagramDialog.open()) {
            // query = erDiagramDialog.getSql();
            // }
            // if (query == null) {
            // return;
            // }
            List<String> repositoryNames = repositoryNodeManager.getALLReposotoryNodeNames();
            ConnectionParameters connParam = new ConnectionParameters();
            connParam.setQuery(""); //$NON-NLS-1$
            connParam.setNeedTakePrompt(false);
            connParam.setShowDesignerPage(true);
            connParam.setEditorTitle(TextUtil.getNewQueryLabel()); //$NON-NLS-1$
            dialog.openEditor(SQLBuilderRepositoryNodeManager.getRoot((RepositoryNode) selectedNodes.get(0)), repositoryNames,
                    connParam, isDefaultEditor, selectedNodes);
        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("GenerateSelectSQLAction.logMessageGenerateSql"), e); //$NON-NLS-1$
        }
    }

    /**
     * @return query string for full table select
     */
    protected String createColumnSelect() {

        StringBuffer query = new StringBuffer("select "); //$NON-NLS-1$
        String fix = getPrePostfix(selectedNodes.get(0));
        String tablePrefix = getTablePrefix(selectedNodes.get(0));
        String sep = ""; //$NON-NLS-1$
        String table = ""; //$NON-NLS-1$

        for (int i = 0; i < selectedNodes.size(); i++) {

            RepositoryNode node = (RepositoryNode) selectedNodes.get(i);
            if (node.getParent() != ((RepositoryNode) selectedNodes.get(0)).getParent()) {
                continue;
            }

            if ((RepositoryNodeType) selectedNodes.get(0).getProperties(EProperties.CONTENT_TYPE) == RepositoryNodeType.COLUMN) {

                if (table.length() == 0) {
                    table = ((SqlBuilderRepositoryObject) node.getParent().getObject()).getSourceName();
                }

                String column = ((SqlBuilderRepositoryObject) node.getObject()).getSourceName();
                if (column == null || column.trim().equals("")) { //$NON-NLS-1$
                    continue;
                }
                query.append(sep);
                query.append(fix + column + fix);
                sep = ", "; //$NON-NLS-1$
            }
        }

        query.append(" from "); //$NON-NLS-1$
        if (fix != null && !fix.trim().equals("")) { //$NON-NLS-1$
            if (tablePrefix == null || tablePrefix.equals("")) { //$NON-NLS-1$
                query.append(fix + table + fix);
            } else {
                query.append(fix + tablePrefix + fix + "." + fix + table + fix); //$NON-NLS-1$
            }
        } else {
            if (tablePrefix == null || tablePrefix.equals("")) { //$NON-NLS-1$
                query.append(table);
            } else {
                query.append(tablePrefix + "." + table); //$NON-NLS-1$
            }

        }

        return query.toString();

    }

    /**
     * @return query string for full table select
     */
    protected String createTableSelect() {

        RepositoryNode node = (RepositoryNode) selectedNodes.get(0);
        String fix = getPrePostfix(node);
        String tablePrefix = getTablePrefix(node);

        StringBuffer query = new StringBuffer("select "); //$NON-NLS-1$
        String sep = ""; //$NON-NLS-1$

        EList columns = ((MetadataTableRepositoryObject) node.getObject()).getTable().getColumns();
        Iterator it = columns.iterator();

        if (columns.isEmpty()) {
            query.append("*"); //$NON-NLS-1$
        }
        while (it.hasNext()) {

            String column = ((MetadataColumn) it.next()).getOriginalField();
            if (column == null || column.trim().equals("")) { //$NON-NLS-1$
                continue;
            }
            query.append(sep);
            query.append(fix + column + fix);
            sep = ", "; //$NON-NLS-1$
        }

        query.append(" from "); //$NON-NLS-1$
        if (fix != null && !fix.trim().equals("")) { //$NON-NLS-1$
            if (tablePrefix == null || tablePrefix.equals("")) { //$NON-NLS-1$
                query.append(fix + ((SqlBuilderRepositoryObject) node.getObject()).getSourceName() + fix);
            } else {
                query.append(fix + tablePrefix + fix + "." + fix //$NON-NLS-1$
                        + ((SqlBuilderRepositoryObject) node.getObject()).getSourceName() + fix);
            }
        } else {
            if (tablePrefix == null || tablePrefix.equals("")) { //$NON-NLS-1$
                query.append(((SqlBuilderRepositoryObject) node.getObject()).getSourceName());
            } else {
                query.append(tablePrefix + "." + ((SqlBuilderRepositoryObject) node.getObject()).getSourceName()); //$NON-NLS-1$
            }
        }

        return query.toString();
    }

    private String getTablePrefix(IRepositoryNode node) {
        RepositoryNode root = SQLBuilderRepositoryNodeManager.getRoot((RepositoryNode) node);
        DatabaseConnection connection = (DatabaseConnection) ((ConnectionItem) root.getObject().getProperty().getItem())
                .getConnection();
        if (connection.getUiSchema() != null && !connection.getUiSchema().trim().equals("")) { //$NON-NLS-1$
            return connection.getUiSchema();
        } else {
            return ""; //$NON-NLS-1$
        }
    }

    /**
     * Get Prepostfix.
     *
     * @param node the selected node
     * @return PrePostfix
     */
    private String getPrePostfix(IRepositoryNode node) {
        RepositoryNode root = SQLBuilderRepositoryNodeManager.getRoot((RepositoryNode) node);
        DatabaseConnection connection = (DatabaseConnection) ((ConnectionItem) root.getObject().getProperty().getItem())
                .getConnection();
        if (TextUtil.isDoubleQuotesNeededDbType(connection.getDatabaseType())) { //$NON-NLS-1$
            return "\""; //$NON-NLS-1$
        }
        return ""; //$NON-NLS-1$
    }

    /**
     * Custom image for generate SQL action.
     *
     * @see org.eclipse.jface.action.IAction#getImageDescriptor()
     * @return ImageDescriptor
     */
    public ImageDescriptor getImageDescriptor() {

        return SQL_EDITOR_IMAGE;
    }

    /**
     * yzhang Comment method "setSelectedNode".
     *
     * @param node
     */
    public void setSelectedNode(RepositoryNode node) {
        if (node != null) {
            selectedNodes.clear();
            selectedNodes.add(node);
        }
    }
}
