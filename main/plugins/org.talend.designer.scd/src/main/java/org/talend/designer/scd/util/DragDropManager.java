package org.talend.designer.scd.util;

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

import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;
import org.talend.designer.scd.ui.IDragDropDelegate;

/**
 * DOC hcw class global comment. Detailled comment
 */
public class DragDropManager {

    static boolean dropped = false;

    static Transfer[] types = new Transfer[] { TextTransfer.getInstance() };

    static String currentSelectionText;

    public void addDragSupport(final Control control, final IDragDropDelegate delegate) {

        DragSource dragSource = new DragSource(control, DND.DROP_MOVE | DND.DROP_COPY);
        dragSource.setTransfer(types);

        dragSource.addDragListener(new DragSourceAdapter() {

            @Override
            public void dragStart(DragSourceEvent event) {
                currentSelectionText = delegate.getDragItemsAsText();
                dropped = false;
            }

            @Override
            public void dragSetData(DragSourceEvent event) {
                // Get the selected items in the drag source
                event.data = currentSelectionText;
            }

            @Override
            public void dragFinished(DragSourceEvent event) {
                if (event.detail == DND.DROP_MOVE && dropped) {
                    // remove selection
                    delegate.removeDragItems(currentSelectionText);
                }
            }
        });
    }

    public void addDropSupport(final Control control, final IDragDropDelegate delegate) {

        // Create the drop target
        DropTarget dropTarget = new DropTarget(control, DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_DEFAULT);
        dropTarget.setTransfer(types);
        dropTarget.addDropListener(new DropTargetAdapter() {

            @Override
            public void dragEnter(DropTargetEvent event) {
                if (event.detail == DND.DROP_COPY && !delegate.isDropAllowed(currentSelectionText)) {
                    event.detail = DND.DROP_NONE;
                    return;
                }

                if (event.detail == DND.DROP_DEFAULT) {
                    event.detail = DND.DROP_MOVE;
                }

                // Allow dropping text only
                for (int i = 0, n = event.dataTypes.length; i < n; i++) {
                    if (TextTransfer.getInstance().isSupportedType(event.dataTypes[i])) {
                        event.currentDataType = event.dataTypes[i];
                    }
                }
            }

            @Override
            public void dragLeave(DropTargetEvent event) {
                event.detail = DND.DROP_NONE;
            }

            @Override
            public void dragOver(DropTargetEvent event) {
                event.feedback = DND.FEEDBACK_SELECT | DND.FEEDBACK_SCROLL;
            }

            @Override
            public void drop(DropTargetEvent event) {
                if (!delegate.isDropAllowed(currentSelectionText)) {
                    event.detail = DND.DROP_NONE;
                    return;
                }
                if (TextTransfer.getInstance().isSupportedType(event.currentDataType)) {
                    // Get the dropped data
                    String data = (String) event.data;
                    Point point = new Point(event.x, event.y);
                    delegate.onDropItems(data, point);
                    dropped = true;
                }
            }
        });
    }

}
