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

import java.util.List;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.actions.SelectionProviderAction;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.sqlbuilder.util.ConnectionParameters;
import org.talend.core.sqlbuilder.util.TextUtil;
import org.talend.repository.model.IRepositoryNode.EProperties;
import org.talend.repository.model.RepositoryNode;
import org.talend.sqlbuilder.Messages;
import org.talend.sqlbuilder.dbstructure.DBTreeProvider;
import org.talend.sqlbuilder.dbstructure.RepositoryNodeType;
import org.talend.sqlbuilder.repository.utility.SQLBuilderRepositoryNodeManager;
import org.talend.sqlbuilder.ui.ISQLBuilderDialog;

/**
 * This class is responsible for opening new editor. <br/>
 *
 * @author ftang
 *
 */
public class OpenNewEditorAction extends SelectionProviderAction {

    private ISQLBuilderDialog dialog;

    private TreeViewer selectionProvider;

    private ConnectionParameters connParam;

    private boolean isQuery;

    private SQLBuilderRepositoryNodeManager repositoryNodeManager = new SQLBuilderRepositoryNodeManager();

    /**
     * OpenNewEditorAction constructor.
     *
     * @param selectionProvider
     * @param editorComposite
     * @param connParam
     * @param isQuery
     */
    public OpenNewEditorAction(TreeViewer selectionProvider, ISQLBuilderDialog dialog, ConnectionParameters connParam,
            boolean isQuery) {
        super(selectionProvider, Messages.getString("OpenNewEditorAction.textNewEditor")); //$NON-NLS-1$
        setText(Messages.getString("DBStructureComposite.NewEditor")); //$NON-NLS-1$
        setToolTipText(Messages.getString("DBStructureComposite.NewEditor")); //$NON-NLS-1$
        this.dialog = dialog;
        this.selectionProvider = selectionProvider;
        this.connParam = connParam;
        this.isQuery = isQuery;
        init();
    }

    @SuppressWarnings("unchecked")
    public void init() {
        RepositoryNode[] selectedNodes = (RepositoryNode[]) ((IStructuredSelection) selectionProvider.getSelection()).toList()
                .toArray(new RepositoryNode[] {});
        if (selectedNodes.length == 0) {
            this.setEnabled(false);
            return;
        }
        int i = 0;
        for (RepositoryNode node : selectedNodes) {
            if (node.getProperties(EProperties.CONTENT_TYPE) == RepositoryNodeType.FOLDER
                    || node.getProperties(EProperties.CONTENT_TYPE) == RepositoryNodeType.QUERY
                    || node.getProperties(EProperties.CONTENT_TYPE) == RepositoryNodeType.QUERIESCONNECTION) {
                i++;
            }
        }
        if (i > 0) {
            this.setEnabled(false);
        } else {
            this.setEnabled(true);
        }
    }

    @Override
    public void selectionChanged(IStructuredSelection selection) {
        init();
    }

    @Override
    public void run() {
        IStructuredSelection selection = (IStructuredSelection) selectionProvider.getSelection();
        RepositoryNode firstNode = (RepositoryNode) selection.getFirstElement();
        if(firstNode==null) {
        	return;
        }
        if (firstNode.getProperties(EProperties.CONTENT_TYPE) == RepositoryNodeType.FOLDER) {
            firstNode = repositoryNodeManager.getRepositoryNodebyName(connParam.getRepositoryName());
        }
        List<String> repositoryNames = repositoryNodeManager.getALLReposotoryNodeNames();
        IRepositoryViewObject object = SQLBuilderRepositoryNodeManager.getRoot(firstNode).getObject();
        connParam.setRepositoryName(object.getLabel());
        connParam.setRepositoryId(object.getId());
        connParam.setFirstOpenSqlBuilder(false);
        if (isQuery) {
            DBTreeProvider provider = (DBTreeProvider) selectionProvider.getContentProvider();
            selectReveal(provider.getSelectedExtReposiotryNode());
            isQuery = false;
        } else {
            connParam.setEditorTitle(TextUtil.getNewQueryLabel());
        }
        dialog.openEditor(firstNode, repositoryNames, connParam, false);
    }

    private void selectReveal(final RepositoryNode selectQuery) {
        Control ctrl = selectionProvider.getControl();
        if (ctrl == null || ctrl.isDisposed()) {
            return;
        }
        ISelection javaSelection = new StructuredSelection(selectQuery);
        selectionProvider.setSelection(javaSelection, true);
    }

    /**
     * Getter for connParam.
     *
     * @return the connParam
     */
    public ConnectionParameters getConnParam() {
        return connParam;
    }

}
