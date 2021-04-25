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
package org.talend.designer.filemultischemas.ui.dnd;

import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TreeDragSourceEffect;
import org.eclipse.swt.dnd.TreeDropTargetEffect;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;
import org.talend.designer.filemultischemas.data.SchemasKeyData;

/**
 * cLi class global comment. Detailled comment
 *
 * @deprecated can't support dnd yet, have changed.
 */
public class SchemasTreeDnD {

    private int operations = DND.DROP_COPY | DND.DROP_MOVE;

    private Transfer[] transferTypes = new Transfer[] { LocalSelectionTransfer.getTransfer() };

    private TreeViewer schemaTreeViewer;

    public SchemasTreeDnD(TreeViewer schemaTreeViewer) {
        super();
        this.schemaTreeViewer = schemaTreeViewer;
    }

    private Tree getTree() {
        return this.schemaTreeViewer.getTree();
    }

    public void addDragAndDrop() {
        // drag
        DragSource source = new DragSource(getTree(), operations);
        source.setTransfer(transferTypes);
        source.addDragListener(new TreeDragSourceEffect(getTree()));
        // drop
        DropTarget target = new DropTarget(getTree(), operations);
        target.setTransfer(transferTypes);

        target.addDropListener(new TreeDropTargetEffect(getTree()) {

            private SchemasKeyData source;

            @Override
            public void dragEnter(DropTargetEvent event) {
                super.dragEnter(event);
                Widget item = event.item;
                if (item != null) {
                    TreeItem treeItem = (TreeItem) event.item;
                    Object data = treeItem.getData();
                    if (data != null && data instanceof SchemasKeyData) {
                        this.source = (SchemasKeyData) data;
                        event.feedback = DND.FEEDBACK_EXPAND;
                        return;
                    }
                }

                event.detail = DND.DROP_NONE;
            }

            @Override
            public void drop(DropTargetEvent event) {
                super.drop(event);
                if (event.item != null && source != null) {
                    TreeItem item = (TreeItem) event.item;
                    Object data = item.getData();
                    if (data != null && data instanceof SchemasKeyData) {
                        SchemasKeyData target = (SchemasKeyData) data;
                        target.addChild(source);
                        schemaTreeViewer.refresh();
                    }
                }
                source = null;
            }

        });

    }
}
