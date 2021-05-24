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
package org.talend.designer.core.ui.editor.nodes;

import java.util.List;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Cursors;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.gef.DragTracker;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.Handle;
import org.eclipse.gef.SharedCursors;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.ResizableEditPolicy;
import org.eclipse.gef.handles.MoveHandle;
import org.eclipse.gef.requests.ChangeBoundsRequest;
import org.eclipse.swt.graphics.Cursor;
import org.talend.designer.core.ui.editor.TalendScalableFreeformRootEditPart;
import org.talend.designer.core.ui.editor.cmd.ResizeNodeCommand;
import org.talend.designer.core.ui.editor.connections.NodeResizableHandle;
import org.talend.designer.core.ui.editor.nodecontainer.NodeContainer;

/**
 */
public class NodeResizableEditPolicy extends ResizableEditPolicy {

    @Override
    protected Command getResizeCommand(ChangeBoundsRequest request) {
        Object parent = getHost().getParent().getModel();
        if (!(parent instanceof NodeContainer)) {
            return null;
        }

        Node node = (Node) getHost().getModel();
        if (node.isReadOnly()) {
            return null;
        }

        TalendScalableFreeformRootEditPart rootEditPart = (TalendScalableFreeformRootEditPart) getHost().getRoot();
        double scale = 1 / rootEditPart.getZoomManager().getZoom();
        return new ResizeNodeCommand(node, new Dimension(node.getSize().width + request.getSizeDelta().getScaled(scale).width,
                node.getSize().height + request.getSizeDelta().getScaled(scale).height));
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.editpolicies.ResizableEditPolicy#createResizeHandle(java.util.List, int)
     */
    @Override
    protected void createResizeHandle(List handles, int direction) {
        if ((getResizeDirections() & direction) == direction) {
            addHandle((GraphicalEditPart) getHost(), handles, direction, getResizeTracker(direction),
                    Cursors.getDirectionalCursor(direction, getHostFigure().isMirrored()));
        } else {
            // display 'resize' handle to allow dragging or indicate selection
            // only
            createDragHandle(handles, direction);
        }
    }

    private void addHandle(GraphicalEditPart part, List handles, int direction, DragTracker tracker, Cursor cursor) {
        handles.add(createHandle(part, direction, tracker, cursor));
    }

    private Handle createHandle(GraphicalEditPart owner, int direction, DragTracker tracker, Cursor cursor) {
        NodeResizableHandle handle = new NodeResizableHandle(owner, direction);
        handle.setDragTracker(tracker);
        handle.setCursor(cursor);
        return handle;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.editpolicies.NonResizableEditPolicy#createMoveHandle(java.util.List)
     */
    @Override
    protected void createMoveHandle(List handles) {
        if (isDragAllowed()) {
            // display 'move' handle to allow dragging
            handles.add(moveHandle((GraphicalEditPart) getHost(), getDragTracker(), Cursors.SIZEALL));
        } else {
            // display 'move' handle only to indicate selection
            handles.add(moveHandle((GraphicalEditPart) getHost(), getSelectTracker(), SharedCursors.ARROW));
        }
    }

    private Handle moveHandle(GraphicalEditPart owner, DragTracker tracker, Cursor cursor) {
        MoveHandle moveHandle = new MoveHandle(owner);
        moveHandle.setForegroundColor(ColorConstants.gray);
        moveHandle.setDragTracker(tracker);
        moveHandle.setCursor(cursor);
        return moveHandle;
    }

}
