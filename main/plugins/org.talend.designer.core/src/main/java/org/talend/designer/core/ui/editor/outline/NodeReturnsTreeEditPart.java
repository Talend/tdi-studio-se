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

import java.util.Collections;
import java.util.List;

import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.editparts.AbstractTreeEditPart;
import org.talend.core.model.process.INodeReturn;
import org.talend.designer.core.ui.editor.nodes.NodeEditPolicy;

/**
 * This class uses the Node as model and will show a part of its atributes in the Outline tree. <br/>
 *
 * $Id$
 *
 */
public class NodeReturnsTreeEditPart extends AbstractTreeEditPart {

    @Override
    public Object getAdapter(Class key) {
        return null;
        /*
         * if (key == ITabbedPropertySheetPageContributor.class) { return null; } if (key == IResource.class) { return
         * null; } return super.getAdapter(key);
         */
    }

    @Override
    public void setSelected(int value) {
        // IWorkbench workbench = PlatformUI.getWorkbench();
        // IWorkbenchPage page = workbench.getActiveWorkbenchWindow().getActivePage();
        // IEditorPart editorPart = page.getActiveEditor();
        // if (editorPart instanceof AbstractMultiPageTalendEditor) {
        // AbstractMultiPageTalendEditor multiPageTalendEditor = ((AbstractMultiPageTalendEditor) editorPart);
        // EditPart editPart = multiPageTalendEditor.getOldSelection();
        // if (editPart != null) {
        // ISelection selection = multiPageTalendEditor.getTalendEditor().getViewer().getSelection();
        // if (selection instanceof StructuredSelection) {
        // StructuredSelection structSel = (StructuredSelection) selection;
        // if (!structSel.getFirstElement().equals(editPart)) {
        // multiPageTalendEditor.getTalendEditor().getViewer().setSelection(selection);
        // }
        // }
        // }
        // }
        // Set the editPart parameter of the nodeTransferDragSourceListener object
        nodeTransferDragSourceListener.setEditPart(this);
        super.setSelected(value);
    }

    // Define a NodeTransferDragSourceListener instance which is always single.
    private NodeTransferDragSourceListener nodeTransferDragSourceListener = NodeTransferDragSourceListener.getInstance();

    @Override
    public void activate() {
        super.activate();
        // Set the editPart parameter of the nodeTransferDragSourceListener object and register the drag listener
        nodeTransferDragSourceListener.setEditPart(this);
        getViewer().addDragSourceListener(nodeTransferDragSourceListener.getNodeTransferDragSourceListener());
    }

    @Override
    public void deactivate() {
        super.deactivate();
        nodeTransferDragSourceListener.setEditPart(null);
        getViewer().removeDragSourceListener(nodeTransferDragSourceListener.getNodeTransferDragSourceListener());
    }

    public NodeReturnsTreeEditPart(Object model) {
        super(model);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.editparts.AbstractEditPart#getModelChildren()
     */
    @Override
    protected List getModelChildren() {
        return Collections.EMPTY_LIST;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     *//*
        * public void propertyChange(PropertyChangeEvent change) { refreshVisuals(); }
        */

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.editparts.AbstractTreeEditPart#createEditPolicies()
     */
    @Override
    protected void createEditPolicies() {
        installEditPolicy(EditPolicy.COMPONENT_ROLE, new NodeEditPolicy());
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.editparts.AbstractTreeEditPart#refreshVisuals()
     */
    @Override
    protected void refreshVisuals() {
        INodeReturn nr = ((INodeReturn) getModel());
        setWidgetText(nr.getDisplayName() + " - " + nr.getName() + " (" + nr.getAvailability() + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
}
