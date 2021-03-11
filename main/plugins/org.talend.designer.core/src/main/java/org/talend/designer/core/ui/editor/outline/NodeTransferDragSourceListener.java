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
package org.talend.designer.core.ui.editor.outline;

import org.eclipse.gef.EditPart;
import org.eclipse.jface.util.TransferDragSourceListener;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.talend.core.model.process.ElementParameterParser;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.INodeReturn;

/**
 *
 *
 */
public class NodeTransferDragSourceListener {

    // The listener instance be planned to rigistered by node
    private TransferDragSourceListener dragDropListener;

    private EditPart currentEditPart;

    // The singleton class instance
    private static NodeTransferDragSourceListener instance;

    private NodeTransferDragSourceListener() {
        this.currentEditPart = null;
    }

    public void setEditPart(EditPart currentEditPart) {
        this.currentEditPart = currentEditPart;
    }

    public static NodeTransferDragSourceListener getInstance() {
        if (instance == null)
            instance = new NodeTransferDragSourceListener();
        return instance;
    }

    public TransferDragSourceListener getNodeTransferDragSourceListener() {
        if (dragDropListener == null)
            dragDropListener = new TransferDragSourceListener() {

                TextTransfer transfer;

                public Transfer getTransfer() {
                    transfer = TextTransfer.getInstance();
                    return transfer;
                }

                public void dragFinished(final DragSourceEvent event) {
                }

                public void dragSetData(final DragSourceEvent event) {
                    if (currentEditPart != null && currentEditPart.getParent() != null) {
                        INode node = (INode) currentEditPart.getParent().getModel();
                        String value = ElementParameterParser
                                .parse(node, ((INodeReturn) currentEditPart.getModel()).getVarName());
                        event.data = value;
                    }

                }

                public void dragStart(final DragSourceEvent event) {
                    if (currentEditPart != null && currentEditPart.getParent() != null) {
                        // Make sure the process node can not be dragged and dropped.
                        if (currentEditPart.getParent().getModel() instanceof org.talend.designer.core.ui.editor.nodes.Node) {
                            event.doit = true;
                            return;
                        }

                    }
                    event.doit = false;
                }

            };
        return dragDropListener;
    }
}
