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

import org.eclipse.draw2d.FigureListener;
import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.LayerConstants;
import org.eclipse.gef.editpolicies.SelectionEditPolicy;
import org.talend.core.model.process.INodeConnector;
import org.talend.designer.core.ui.editor.connections.NodeConnectorTool;
import org.talend.designer.core.ui.editor.connections.TalendConnectionHandle;
import org.talend.designer.core.ui.editor.connections.TalendConnectionHandleLocator;

/**
 * DOC Talend class global comment. Detailled comment
 */
public class SelectionFeedbackEditPolicy extends SelectionEditPolicy implements LayerConstants {

    public static final Object TALEND_SELECTION_FEEDBACK_ROLE = "TalendSelectionFeedback";

    public static final String TALEND_FEEDBACK_LAYER = "TalendFeedbackLayer";

    public static final String SELECTION_FEEDBACK = "SelectionFeedback";

    private IFigure layer;

    private FigureListener figureListener;

    private IFigure sourceFigure;

    private NodePart nodePart;

    private TalendConnectionHandle handle = null;

    private boolean hideHandle = false;

    public SelectionFeedbackEditPolicy(NodePart nodePart) {
        this.nodePart = nodePart;
        this.figureListener = new FigureListener() {

            @Override
            public void figureMoved(IFigure source) {
                List selectedEditPart = getHost().getViewer().getSelectedEditParts();
                if (selectedEditPart.contains(getHost())
                // || selectedEditPart.contains(getHost().getParent())
                // || selectedEditPart.contains(getHost().getParent().getParent())
                ) {
                    showFeedback(true, null);
                }

            }
        };
    }

    @Override
    public void activate() {
        super.activate();
    }

    @Override
    public void showSelection() {
        if (this.sourceFigure == null) {
            this.sourceFigure = getHostFigure();
            this.sourceFigure.addFigureListener(this.figureListener);
        }
        INodeConnector connector = new NodeConnectorTool(nodePart).getConnector();
        if (connector == null) {
            this.setHideHandle(true);
            return;
        } else {
            this.setHideHandle(false);
        }
        showFeedback(false, connector);
    }

    private void showFeedback(boolean isMove, INodeConnector connector) {
        if (this.layer == null) {
            this.layer = getLayer(TALEND_FEEDBACK_LAYER);
        }
        if (isHideHandle()) {
            return;
        }
        if (figureIsDisplayed()) {
            if (isMove) {
                if (handle.getLocator() == null) {
                    TalendConnectionHandleLocator locator = new TalendConnectionHandleLocator(getHostFigure());
                    handle.setLocator(locator);
                }
            }
            return;
        }

        if (getHost().getModel() instanceof Node) {
            if (handle == null) {
                handle = new TalendConnectionHandle(this.nodePart);
                TalendConnectionHandleLocator locator = new TalendConnectionHandleLocator(getHostFigure());
                handle.setLocator(locator);
            } else {
                if (handle.getLocator() == null) {
                    TalendConnectionHandleLocator locator = new TalendConnectionHandleLocator(getHostFigure());
                    handle.setLocator(locator);
                }
            }
            handle.setMainNodeConnector(connector);
            layer.add(handle);
            if (!isMove) {
                handle.getLocator().relocate(handle);
            }
            // }

            // locator.addHandle(handle);
            // handle.addMouseMotionListener(this);

            // Register this figure with it's host editpart so mouse events
            // will be propagated to it's host.
            getHost().getViewer().getVisualPartMap().put(handle, getHost());
        }

    }

    @Override
    public void hideSelection() {
        hideFeedback();
    }

    private void hideFeedback() {
        if (figureIsDisplayed()) {
            this.layer.remove(this.handle);
        }
    }

    private boolean figureIsDisplayed() {
        return ((this.handle != null) && (this.layer != null) && (this.layer.getChildren().contains(this.handle)));
    }

    @Override
    public void deactivate() {
        hideSelection();
        super.deactivate();
        this.handle = null;
    }

    @Override
    protected IFigure getLayer(Object layer) {
        return super.getLayer(layer);
    }

    public void performCollapse() {
        hideFeedback();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.editpolicies.SelectionEditPolicy#addSelectionListener()
     */
    @Override
    protected void addSelectionListener() {
        super.addSelectionListener();
    }

    /**
     * Getter for hideHandle.
     *
     * @return the hideHandle
     */
    public boolean isHideHandle() {
        return this.hideHandle;
    }

    /**
     * Sets the hideHandle.
     *
     * @param hideHandle the hideHandle to set
     */
    public void setHideHandle(boolean hideHandle) {
        this.hideHandle = hideHandle;
    }

}
