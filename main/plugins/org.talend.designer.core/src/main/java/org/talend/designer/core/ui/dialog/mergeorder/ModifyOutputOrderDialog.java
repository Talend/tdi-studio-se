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
package org.talend.designer.core.ui.dialog.mergeorder;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.widgets.Shell;
import org.talend.core.model.process.EConnectionType;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.INodeConnector;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.ui.editor.connections.Connection;

/**
 * yzhang class global comment. Detailled comment
 */
public class ModifyOutputOrderDialog extends MergeOrderDialog {

    private INode multipleOutputNode;

    private List<IConnection> OtherConnectionList = new ArrayList<IConnection>();

    private ModifyOutputOrderDialog(Shell parentShell, INode multipleOutputNode) {
        super(parentShell);
        this.multipleOutputNode = multipleOutputNode;
        this.connectionList = new ArrayList<IConnection>();
    }

    private EConnectionType connType;

    public ModifyOutputOrderDialog(Shell parentShell, INode multipleOutputNode, EConnectionType connType) {
        this(parentShell, multipleOutputNode);
        Assert.isNotNull(connType);
        this.connType = connType;
        List<IConnection> fullList = (List<IConnection>) multipleOutputNode.getOutgoingConnections();
        for (IConnection connection : fullList) {
            if (connection.getLineStyle() == connType) {
                connectionList.add(connection);
            } else {
                OtherConnectionList.add(connection);
            }
        }
    }

    /**
     * connCategory must be in IConnectionCategory
     */
    private Integer connCategory;

    public ModifyOutputOrderDialog(Shell parentShell, INode multipleOutputNode, Integer connCategory) {
        this(parentShell, multipleOutputNode);
        Assert.isNotNull(connCategory);
        this.connCategory = connCategory;
        List<IConnection> fullList = (List<IConnection>) multipleOutputNode.getOutgoingConnections();
        for (IConnection connection : fullList) {
            if (connection.getLineStyle().hasConnectionCategory(connCategory)) {
                connectionList.add(connection);
            } else {
                OtherConnectionList.add(connection);
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.ui.dialog.mergeorder.MergeOrderDialog#configureShell(org.eclipse.swt.widgets.Shell)
     */
    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.designer.core.ui.dialog.mergeorder.MergeOrderDialog#configureSizeAndTitle(org.eclipse.swt.widgets.
     * Shell)
     */
    @Override
    protected void configureSizeAndTitle(Shell shell) {
        String midStr = "output"; //$NON-NLS-1$
        if (connType != null) {
            midStr = connType.getDefaultLinkName();
        }
        shell.setText(Messages.getString("ModifyOutputOrderDialog.title", multipleOutputNode.getUniqueName(), midStr)); //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.ui.dialog.mergeorder.MergeOrderDialog#getConnectionQty()
     */
    @Override
    protected int getConnectionQty() {
        int nb = 0;
        for (Connection connection : (List<Connection>) multipleOutputNode.getOutgoingConnections()) {
            if (connCategory != null && connection.getLineStyle().hasConnectionCategory(connCategory)) {
                nb++;
            } else if (connection.getLineStyle().equals(connType)) {
                nb++;
            }
        }
        return nb;
    }

    @Override
    protected String getDisplayStr(Connection bean) {
        if (bean != null && connType != null) {
            INodeConnector sourceNodeConnector = bean.getSourceNodeConnector();
            if (sourceNodeConnector != null) {
                String linkName = sourceNodeConnector.getLinkName();
                if (linkName != null) {
                    if (linkName.equals(bean.getName())) {
                        return bean.getName();
                    }
                    return linkName + " (" + bean.getName() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
                }
            }
        }
        return super.getDisplayStr(bean);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.ui.dialog.mergeorder.MergeOrderDialog#getConnectionList()
     */
    @Override
    public List<IConnection> getConnectionList() {
        List<IConnection> fillConnectionList = new ArrayList<IConnection>();
        fillConnectionList.addAll(this.connectionList);
        fillConnectionList.addAll(this.OtherConnectionList);
        return fillConnectionList;
    }

    public INode getMergeNode() {
        return this.multipleOutputNode;
    }
}
