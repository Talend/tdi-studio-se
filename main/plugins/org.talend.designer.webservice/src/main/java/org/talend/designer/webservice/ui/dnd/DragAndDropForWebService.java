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
package org.talend.designer.webservice.ui.dnd;

import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Control;
import org.talend.commons.ui.swt.advanced.dataeditor.AbstractDataTableEditorView;
import org.talend.commons.ui.swt.linking.TableToTablesLinker;
import org.talend.designer.webservice.WebServiceComponent;
import org.talend.designer.webservice.managers.WebServiceManager;

/**
 * DOC Administrator class global comment. Detailled comment
 */
public class DragAndDropForWebService {

    private DragSource dragSource;

    private DropTarget dropTarget;

    private Control draggableControl;

    private AbstractDataTableEditorView draggableControlView;

    private WebServiceManager webServiceManager;

    private TableToTablesLinker tabTotabLink;

    public DragAndDropForWebService(WebServiceManager webServiceManager, AbstractDataTableEditorView draggableControlView,
            TableToTablesLinker tabTotabLink, WebServiceComponent connector, boolean canBeSourceOfDragging,
            boolean canBeTargetOfDragging) {
        this.draggableControlView = draggableControlView;
        this.draggableControl = draggableControlView.getTable();
        this.webServiceManager = webServiceManager;
        this.tabTotabLink = tabTotabLink;

        if (canBeSourceOfDragging) {
            DragSourceListenerForWebService dragListener = new DragSourceListenerForWebService(draggableControlView);
            createDragSource(dragListener);
        }
        if (canBeTargetOfDragging) {
            DropTargetListenerForWebService dropListener = new DropTargetListenerForWebService(draggableControlView,
                    tabTotabLink, connector);
            createDropTarget(dropListener);
        }
    }

    /**
     *
     * DOC amaumont Comment method "createDragSource".
     *
     * @param sourceListener
     */
    private void createDragSource(DragSourceListener sourceListener) {
        if (dragSource != null) {
            dragSource.dispose();
        }
        dragSource = new DragSource(draggableControl, DND.DROP_DEFAULT | DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_LINK);
        dragSource.setTransfer(new Transfer[] { TableEntriesTransfer.getInstance() });
        dragSource.addDragListener(sourceListener);
    }

    /**
     *
     * create DropTarget.
     */
    private void createDropTarget(DropTargetListener targetListener) {

        if (dropTarget != null) {
            dropTarget.dispose();
        }
        dropTarget = new DropTarget(draggableControl, DND.DROP_DEFAULT | DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_LINK);
        dropTarget.setTransfer(new Transfer[] { TableEntriesTransfer.getInstance() });
        dropTarget.addDropListener(targetListener);
    }

}
