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
package org.talend.designer.core.ui.editor.palette;

import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.dnd.AbstractTransferDragSourceListener;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.DragSourceEvent;

/**
 * cli class global comment. Detailled comment
 */
public class TalendPaletteDragSourceListener extends AbstractTransferDragSourceListener {

    private static final long FFFFFFFFL = 0xFFFFFFFFL;

    public TalendPaletteDragSourceListener(EditPartViewer viewer) {
        super(viewer, LocalSelectionTransfer.getTransfer());
    }

    @Override
    public void dragFinished(DragSourceEvent event) {
        LocalSelectionTransfer.getTransfer().setSelection(null);
        LocalSelectionTransfer.getTransfer().setSelectionSetTime(0);
    }

    @Override
    public void dragStart(DragSourceEvent event) {
        ISelection selection = getViewer().getSelection();
        if (selection != null && selection instanceof IStructuredSelection) {
            if (((IStructuredSelection) selection).size() == 1) {
                LocalSelectionTransfer.getTransfer().setSelection(selection);
                LocalSelectionTransfer.getTransfer().setSelectionSetTime(event.time & FFFFFFFFL);
                return;
            }
        }
        event.doit = false;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.swt.dnd.DragSourceListener#dragSetData(org.eclipse.swt.dnd.DragSourceEvent)
     */
    public void dragSetData(DragSourceEvent event) {
        event.data = LocalSelectionTransfer.getTransfer().getSelection();

    }

}
