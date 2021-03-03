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

import org.eclipse.jface.util.TransferDragSourceListener;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.talend.commons.ui.swt.advanced.dataeditor.AbstractDataTableEditorView;

/**
 * DOC Administrator class global comment. Detailled comment
 */
public class DragSourceListenerForWebService implements TransferDragSourceListener {

    private Control draggableControl;

    private AbstractDataTableEditorView draggableControlView;

    public DragSourceListenerForWebService(AbstractDataTableEditorView draggableControl) {
        this.draggableControlView = draggableControlView;
        this.draggableControl = draggableControl.getTable();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.util.TransferDragSourceListener#getTransfer()
     */

    public Transfer getTransfer() {
        return TableEntriesTransfer.getInstance();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.swt.dnd.DragSourceListener#dragFinished(org.eclipse.swt.dnd.DragSourceEvent)
     */

    public void dragFinished(DragSourceEvent event) {

    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.swt.dnd.DragSourceListener#dragSetData(org.eclipse.swt.dnd.DragSourceEvent)
     */

    public void dragSetData(DragSourceEvent event) {
        if (TableEntriesTransfer.getInstance().isSupportedType(event.dataType)) {
            if (draggableControl instanceof Table) {
                Table draggableTable = (Table) draggableControl;
                TableItem selection = draggableTable.getSelection()[0];
                event.data = selection.getData();
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.swt.dnd.DragSourceListener#dragStart(org.eclipse.swt.dnd.DragSourceEvent)
     */

    public void dragStart(DragSourceEvent event) {
        if (draggableControl instanceof Table) {
            Table draggableTable = (Table) draggableControl;
            TableItem[] items = draggableTable.getSelection();
            if (items.length == 0) {
                event.doit = false;
            }
            event.data = items[0].getData();
            // WebServiceTransfer.getInstance().setParameter((Parameter) event.data);
            // TableEntriesTransfer.getInstance().setParameter((Parameter) event.data);
            TableEntriesTransfer.getInstance().setTableItem(items[0]);
        }
    }
}
