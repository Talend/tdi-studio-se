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
import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.actions.SelectionProviderAction;
import org.talend.commons.ui.runtime.image.EImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.core.model.metadata.builder.connection.MetadataColumn;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.model.IRepositoryNode.EProperties;
import org.talend.sqlbuilder.Messages;
import org.talend.sqlbuilder.dbstructure.DBTreeProvider;
import org.talend.sqlbuilder.dbstructure.RepositoryNodeType;
import org.talend.sqlbuilder.repository.utility.SQLBuilderRepositoryNodeManager;
import org.talend.sqlbuilder.ui.ISQLBuilderDialog;

/**
 * DOC dev class global comment. Detailled comment <br/>
 *
 * $Id: MetadataRefreshAction.java,v 1.22 2006/11/09 01:21:53 qiang.zhang Exp $
 *
 */
public class MetadataRefreshAction extends SelectionProviderAction {

    private ImageDescriptor img = ImageProvider.getImageDesc(EImage.REFRESH_ICON);

    private ISelectionProvider selectionProvider;

    private List<MetadataColumn> columnNodes;

    private List<RepositoryNode> repositorynodes;

    private ISQLBuilderDialog dialog;

    /**
     * DOC dev MetadataRefreshAction constructor comment.
     *
     * @param selectionProvider
     */
    public MetadataRefreshAction(ISelectionProvider selectionProvider, ISQLBuilderDialog d) {
        super(selectionProvider, ""); //$NON-NLS-1$
        this.selectionProvider = selectionProvider;
        columnNodes = new ArrayList<MetadataColumn>();
        repositorynodes = new ArrayList<RepositoryNode>();
        this.dialog = d;
        init();
        setImageDescriptor(img);
        setHoverImageDescriptor(img);
        setText(Messages.getString("MetadataRefreshAction.textSynchronize")); //$NON-NLS-1$
        setToolTipText(Messages.getString("MetadataRefreshAction.textSynchronize")); //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ui.actions.SelectionProviderAction#selectionChanged(org.eclipse.jface.viewers.IStructuredSelection)
     */
    @Override
    public void selectionChanged(IStructuredSelection selection) {
        init();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.action.Action#run()
     *
     */
    @Override
    public void run() {
        SQLBuilderRepositoryNodeManager.saveMetadataColumn(repositorynodes, columnNodes);
        for (RepositoryNode node : repositorynodes) {
            dialog.refreshNode(node);
        }
        dialog.refreshNode(SQLBuilderRepositoryNodeManager.getRoot(repositorynodes.get(0)));
    }

    /**
     * DOC dev Comment method "init".
     */
    public void init() {

        IStructuredSelection selection = (IStructuredSelection) selectionProvider.getSelection();

        repositorynodes.clear();
        columnNodes.clear();
        boolean flag = true;
        for (Object object : selection.toList()) {
            if (!((RepositoryNode) object).getProperties(EProperties.CONTENT_TYPE).equals(RepositoryNodeType.COLUMN)) {
                this.setEnabled(false);
                return;
            }

            MetadataColumn col = ((DBTreeProvider.MetadataColumnRepositoryObject) ((RepositoryNode) object).getObject())
                    .getColumn();
            if (col.getLabel().equals("")) { //$NON-NLS-1$
                this.setEnabled(false);
                return;
            }
            if (col.isSynchronised()) {

                if (!repositorynodes.isEmpty()
                        && repositorynodes.get(0) != null
                        && !SQLBuilderRepositoryNodeManager.getRoot(repositorynodes.get(0)).equals(
                                SQLBuilderRepositoryNodeManager.getRoot(((RepositoryNode) object)))) {
                    this.setEnabled(false);
                    return;
                }

                repositorynodes.add((RepositoryNode) object);

                this.setEnabled(true);
                flag = false;
                columnNodes.add(col);
            }
        }
        if (flag) {
            this.setEnabled(false);
            return;
        }
    }
}
