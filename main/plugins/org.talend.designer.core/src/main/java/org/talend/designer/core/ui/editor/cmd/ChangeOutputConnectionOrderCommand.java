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
package org.talend.designer.core.ui.editor.cmd;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.gef.commands.Command;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.IConnectionCategory;
import org.talend.core.model.process.INode;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.ui.editor.process.Process;

/**
 * yzhang class global comment. Detailled comment
 */
public class ChangeOutputConnectionOrderCommand extends Command {

    private INode multipleOutputNode;

    private List<IConnection> connectionInNewOrder;

    private List<IConnection> connectionInOldOrder;

    private List<IConnection> connectionInOldOrderClone = new ArrayList();

    /**
     * yzhang ChangeOutputConnectionOrderCommand constructor comment.
     */
    public ChangeOutputConnectionOrderCommand(INode multipleOutputNode, List<IConnection> outputConnections) {

        this.multipleOutputNode = multipleOutputNode;
        this.connectionInNewOrder = outputConnections;
        // TDI-8394:should not directly use node's outputs here,need to use its clone to deal with "undo"
        connectionInOldOrderClone.addAll(multipleOutputNode.getOutgoingConnections());
        this.connectionInOldOrder = connectionInOldOrderClone;

        if (connectionInNewOrder.size() != connectionInOldOrder.size()) {
            throw new IllegalArgumentException("new connection list should have the same size as the old one"); //$NON-NLS-1$
        }
        this.setLabel(Messages.getString("ChangeMergeOrderCommand.label")); //$NON-NLS-1$

    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.commands.Command#execute()
     */
    @Override
    public void execute() {
        List<IMetadataTable> connectionMetadatas = new ArrayList<IMetadataTable>();
        for (IConnection connection : connectionInNewOrder) {
            if (connection.getLineStyle().hasConnectionCategory(IConnectionCategory.DATA)) {
                if (!connectionMetadatas.contains(connection.getMetadataTable())) {
                    connectionMetadatas.add(connection.getMetadataTable());
                }
            }
        }
        List<IMetadataTable> metadataList = multipleOutputNode.getMetadataList();
        Collections.sort(metadataList, new Comparator<IMetadataTable>() {

            @Override
            public int compare(IMetadataTable o1, IMetadataTable o2) {
                int index1 = connectionMetadatas.indexOf(o1);
                if (index1 == -1) {
                    index1 = 0;
                }
                int index2 = connectionMetadatas.indexOf(o2);
                if (index2 == -1) {
                    index2 = 0;
                }
                return index1 - index2;
            }
        });

        multipleOutputNode.setOutgoingConnections(connectionInNewOrder);
        connectionInNewOrder.get(0).updateAllId();
        ((Process) multipleOutputNode.getProcess()).checkStartNodes();

    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.commands.Command#undo()
     */
    @Override
    public void undo() {
        List<IMetadataTable> connectionMetadatas = new ArrayList<IMetadataTable>();
        for (IConnection connection : connectionInOldOrder) {
            if (connection.getLineStyle().hasConnectionCategory(IConnectionCategory.DATA)) {
                if (!connectionMetadatas.contains(connection.getMetadataTable())) {
                    connectionMetadatas.add(connection.getMetadataTable());
                }
            }
        }

        List<IMetadataTable> metadataList = multipleOutputNode.getMetadataList();
        Collections.sort(metadataList, new Comparator<IMetadataTable>() {

            @Override
            public int compare(IMetadataTable o1, IMetadataTable o2) {
                int index1 = connectionMetadatas.indexOf(o1);
                if (index1 == -1) {
                    index1 = 0;
                }
                int index2 = connectionMetadatas.indexOf(o2);
                if (index2 == -1) {
                    index2 = 0;
                }
                return index1 - index2;
            }
        });

        multipleOutputNode.setOutgoingConnections(connectionInOldOrder);
        connectionInOldOrder.get(0).updateAllId();
        ((Process) multipleOutputNode.getProcess()).checkStartNodes();
    }
}
