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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.editparts.AbstractTreeEditPart;
import org.eclipse.swt.widgets.Display;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.nodes.NodeEditPolicy;

/**
 * This class uses the Node as model and will show a part of its atributes in the Outline tree. <br/>
 *
 * $Id$
 *
 */
public class NodeTreeEditPart extends AbstractTreeEditPart implements PropertyChangeListener {

    private NodeTransferDragSourceListener nodeTransferDragSourceListener = NodeTransferDragSourceListener.getInstance();

    @Override
    public void setSelected(int value) {
        nodeTransferDragSourceListener.setEditPart(this);
        super.setSelected(value);
    }

    public NodeTreeEditPart(Object model) {
        super(model);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.editparts.AbstractEditPart#activate()
     */
    @Override
    public void activate() {
        super.activate();
        ((Node) getModel()).addPropertyChangeListener(this);
        nodeTransferDragSourceListener.setEditPart(this);
        getViewer().addDragSourceListener(nodeTransferDragSourceListener.getNodeTransferDragSourceListener());
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.editparts.AbstractEditPart#deactivate()
     */
    @Override
    public void deactivate() {
        ((Node) getModel()).removePropertyChangeListener(this);
        nodeTransferDragSourceListener.setEditPart(null);
        getViewer().removeDragSourceListener(nodeTransferDragSourceListener.getNodeTransferDragSourceListener());
        super.deactivate();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.editparts.AbstractEditPart#getModelChildren()
     */
    @Override
    protected List getModelChildren() {
        return ((Node) getModel()).getReturns(); // Collections.EMPTY_LIST;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    @Override
    public void propertyChange(final PropertyChangeEvent change) {
        if (Node.RETURNS_CHANGED.equals(change.getPropertyName())) {
            refreshChildren();
            return;
        }
        Display.getDefault().syncExec(new Runnable() {

            @Override
            public void run() {
                refreshVisuals();
            }
        });

    }

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
        Node node = (Node) getModel();
        if (node.getLabel().equals(node.getUniqueName())) {
            setWidgetText(node.getUniqueName());
        } else {
            setWidgetText(node.getUniqueName() + " (" + node.getLabel() + ")"); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }
}
