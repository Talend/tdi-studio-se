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
package org.talend.designer.business.diagram.custom.dnd;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.gef.EditPartViewer;
import org.eclipse.gmf.runtime.diagram.ui.parts.DiagramDropTargetListener;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TransferData;
import org.talend.designer.business.diagram.custom.util.GmfPropertiesViewHelper;

/**
 * DOC mhelleboid class global comment. Detailled comment <br/>
 *
 * $Id$
 *
 */
public class BusinessDiagramDropTargetListener extends DiagramDropTargetListener {

    /**
     * DOC mhelleboid BusinessDiagramDropTargetListener constructor comment.
     *
     * @param viewer
     */
    public BusinessDiagramDropTargetListener(EditPartViewer viewer) {
        super(viewer, LocalSelectionTransfer.getTransfer());
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gmf.runtime.diagram.ui.parts.DiagramDropTargetListener#getObjectsBeingDropped()
     */
    @Override
    protected List getObjectsBeingDropped() {
        TransferData[] data = getCurrentEvent().dataTypes;
        List eObjects = new ArrayList();
        ISelection selection = LocalSelectionTransfer.getTransfer().getSelection();
        if (selection instanceof IStructuredSelection) {
            IStructuredSelection structuredSelection = (IStructuredSelection) selection;
            eObjects.addAll(structuredSelection.toList());
        }
        return eObjects;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gmf.runtime.diagram.ui.parts.DiagramDropTargetListener#handleDrop()
     */
    @Override
    protected void handleDrop() {
        super.handleDrop();
        // hack to show Assignment Tab in Properties View
        if (getCurrentEvent().detail != DND.DROP_NONE) {
            new GmfPropertiesViewHelper().selectTargetEditPart(getViewer(), getTargetEditPart());
        }
    }

    // /*
    // * (non-Javadoc)
    // *
    // * @see
    // org.eclipse.gmf.runtime.diagram.ui.parts.DiagramDropTargetListener#isEnabled(org.eclipse.swt.dnd.DropTargetEvent)
    // */
    // @Override
    // public boolean isEnabled(DropTargetEvent event) {
    // super.isEnabled(event);
    // return true;
    // }
    //
    // /*
    // * (non-Javadoc)
    // *
    // * @see org.eclipse.gmf.runtime.diagram.ui.parts.DiagramDropTargetListener#updateTargetEditPart()
    // */
    // @Override
    // protected void updateTargetEditPart() {
    // setTargetEditPart(otherCalculateTargetEditPart());
    // }
    //
    // private EditPart otherCalculateTargetEditPart() {
    // return getViewer().findObjectAt(getDropLocation());
    // }
}
