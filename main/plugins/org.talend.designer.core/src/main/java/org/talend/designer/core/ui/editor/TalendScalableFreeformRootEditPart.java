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
package org.talend.designer.core.ui.editor;

import org.eclipse.draw2d.ConnectionLayer;
import org.eclipse.draw2d.FreeformLayer;
import org.eclipse.draw2d.FreeformLayeredPane;
import org.eclipse.draw2d.LayeredPane;
import org.eclipse.draw2d.ScalableFigure;
import org.eclipse.draw2d.ScalableFreeformLayeredPane;
import org.eclipse.draw2d.Viewport;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.gef.AutoexposeHelper;
import org.eclipse.gef.editparts.GridLayer;
import org.eclipse.gef.editparts.ScalableFreeformRootEditPart;
import org.eclipse.gef.editparts.ViewportAutoexposeHelper;
import org.eclipse.gef.editparts.ZoomManager;
import org.eclipse.ui.IEditorInput;
import org.talend.commons.ui.gmf.draw2d.AnimatableZoomManager;
import org.talend.designer.core.ui.editor.nodes.SelectionFeedbackEditPolicy;

/**
 * Modification of the default RootEditPart to add the possibility to change the color of the background and change the
 * grid.
 *
 * $Id: TalendScalableFreeformRootEditPart.java 7038 2007-11-15 14:05:48Z plegall $
 *
 */
public class TalendScalableFreeformRootEditPart extends ScalableFreeformRootEditPart {

    public static final String PROCESS_BACKGROUND_LAYER = "processBackgroundLayer"; //$NON-NLS-1$

    public static final String SUBJOB_BACKGROUND_LAYER = "processBackgroundLayer"; //$NON-NLS-1$

    public static final String MAP_REDUCE_LAYER = "mapReduceLayer"; //$NON-NLS-1$

    private IEditorInput editorInput;

    private AnimatableZoomManager zoomManager;

    private double[] zoomLevels = { .05, .1, .25, .5, .75, 1, 1.25, 1.5, 1.75, 2, 4 };

    private GridLayer gridLayer;

    private FeedbackLayer feedbackLayer;

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.editparts.ScalableFreeformRootEditPart#getZoomManager()
     */
    @Override
    public ZoomManager getZoomManager() {
        if (zoomManager == null) {
            zoomManager = new AnimatableZoomManager((ScalableFigure) getScaledLayers(), ((Viewport) getFigure()));
            zoomManager.setZoomLevels(zoomLevels);
            zoomManager.setZoomAnimationStyle(ZoomManager.ANIMATE_ZOOM_IN_OUT);
        }
        return zoomManager;
    }

    public TalendScalableFreeformRootEditPart(IEditorInput editorInput) {
        this.editorInput = editorInput;
    }

    @Override
    protected LayeredPane createPrintableLayers() {
        FreeformLayeredPane layeredPane = new FreeformLayeredPane();
        layeredPane.add(new FreeformLayer(), PRIMARY_LAYER);
        layeredPane.add(new ConnectionLayer(), CONNECTION_LAYER);
        return layeredPane;
    }

    @Override
    protected GridLayer createGridLayer() {
        if (gridLayer == null) {
            gridLayer = new TalendGridLayer();
        }
        return gridLayer;
    }

    @Override
    protected ScalableFreeformLayeredPane createScaledLayers() {
        ScalableFreeformLayeredPane layers = new ScalableFreeformLayeredPane();
        layers.add(new FreeformLayer(), SUBJOB_BACKGROUND_LAYER);
        layers.add(new FreeformLayer(), PROCESS_BACKGROUND_LAYER);
        layers.add(createGridLayer(), GRID_LAYER);
        layers.add(getPrintableLayers(), PRINTABLE_LAYERS);
        layers.add(new FreeformLayer(), MAP_REDUCE_LAYER);
        layers.add(new FreeformLayer(), SelectionFeedbackEditPolicy.TALEND_FEEDBACK_LAYER);
        layers.add(new FeedbackLayer(), SCALED_FEEDBACK_LAYER);
        feedbackLayer = new FeedbackLayer();
        return layers;
    }

    /**
     * Modification fo the default Layer. <br/>
     *
     * $Id: TalendScalableFreeformRootEditPart.java 7038 2007-11-15 14:05:48Z plegall $
     *
     */
    class FeedbackLayer extends FreeformLayer {

        FeedbackLayer() {
            setEnabled(false);
        }
    }

    public IEditorInput getEditorInput() {
        return editorInput;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.editparts.FreeformGraphicalRootEditPart#getAdapter(java.lang.Class)
     */
    @Override
    public Object getAdapter(Class key) {
        if (key == AutoexposeHelper.class) {
            return new ViewportAutoexposeHelper(this, new Insets(100));
        }
        return super.getAdapter(key);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#deactivate()
     */
    @Override
    public void deactivate() {
        super.deactivate();
        editorInput = null;
        zoomManager = null;
        feedbackLayer = null;
        gridLayer = null;
    }
}
