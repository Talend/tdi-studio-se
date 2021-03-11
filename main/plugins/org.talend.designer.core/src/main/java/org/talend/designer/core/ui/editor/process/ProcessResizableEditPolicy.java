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
package org.talend.designer.core.ui.editor.process;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.LayerConstants;
import org.eclipse.gef.editpolicies.ResizableEditPolicy;
import org.talend.designer.core.ui.editor.nodecontainer.NodeContainer;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.nodes.NodeFigure;

/**
 * This class describes the figures that will be used for drag and drop each objects on the diagram. <br/>
 *
 * $Id$
 *
 */
public class ProcessResizableEditPolicy extends ResizableEditPolicy {

    /**
     * Creates the figure used for feedback.
     *
     * @return the new feedback figure
     */
    protected IFigure createDragSourceFeedbackFigure() {
        GraphicalEditPart editPart = (GraphicalEditPart) getHost();
        IFigure figure = getCustomFeedbackFigure(editPart.getModel());
        figure.setBounds(editPart.getFigure().getBounds().getCopy());
        addFeedback(figure);
        return figure;
    }

    /**
     * This will take the figure of the node and set it as feedback figure.
     *
     * @param modelPart
     * @return
     */
    protected IFigure getCustomFeedbackFigure(Object modelPart) {
        IFigure figure;

        if (modelPart instanceof NodeContainer) {
            return null;
        }
        if (modelPart instanceof Node) {
            Node node = (Node) modelPart;
            figure = new NodeFigure(node);
            if (node.isStart()) {
                figure.setBackgroundColor(NodeFigure.START_COLOR);
            } else {
                figure.setOpaque(false);
            }
        } else {
            figure = new RectangleFigure();
            ((RectangleFigure) figure).setXOR(true);
            ((RectangleFigure) figure).setFill(true);
            figure.setBackgroundColor(ColorConstants.darkGreen);
            figure.setForegroundColor(ColorConstants.white);
        }

        return figure;
    }

    /**
     * Returns the layer used for displaying feedback.
     *
     * @return the feedback layer
     */
    protected IFigure getFeedbackLayer() {
        return getLayer(LayerConstants.SCALED_FEEDBACK_LAYER);
    }
}
