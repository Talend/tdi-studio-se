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
package org.talend.sqlbuilder.erdiagram.ui.editor;

import org.eclipse.draw2d.ConnectionLayer;
import org.eclipse.draw2d.FreeformLayer;
import org.eclipse.draw2d.FreeformLayeredPane;
import org.eclipse.draw2d.LayeredPane;
import org.eclipse.draw2d.ScalableFreeformLayeredPane;
import org.eclipse.gef.editparts.GridLayer;
import org.eclipse.gef.editparts.ScalableFreeformRootEditPart;

/**
 * qzhang class global comment. Detailled comment <br/>
 *
 * $Id: talend.epf 1 2006-09-29 17:06:40 +0000 (ææäº, 29 ä¹æ 2006) nrousseau $
 *
 */
public class ErDiagramRootEditPart extends ScalableFreeformRootEditPart {

    public static final String PROCESS_BACKGROUND_LAYER = "processBackgroundLayer"; //$NON-NLS-1$

    @Override
    protected LayeredPane createPrintableLayers() {
        FreeformLayeredPane layeredPane = new FreeformLayeredPane();
        layeredPane.add(new ConnectionLayer(), CONNECTION_LAYER);
        layeredPane.add(new FreeformLayer(), PRIMARY_LAYER);
        return layeredPane;
    }

    @Override
    protected GridLayer createGridLayer() {
        return new GridLayer();
    }

    protected ScalableFreeformLayeredPane createScaledLayers() {
        ScalableFreeformLayeredPane layers = new ScalableFreeformLayeredPane();
        layers.add(new FreeformLayer(), PROCESS_BACKGROUND_LAYER);
        // layers.add(createGridLayer(), GRID_LAYER);
        layers.add(getPrintableLayers(), PRINTABLE_LAYERS);
        layers.add(new FeedbackLayer(), SCALED_FEEDBACK_LAYER);
        return layers;
    }

    /**
     * admin ErDiagramRootEditPart class global comment. Detailled comment <br/>
     *
     * $Id: talend.epf 1 2006-09-29 17:06:40 +0000 (ææäº, 29 ä¹æ 2006) nrousseau $
     *
     */
    class FeedbackLayer extends FreeformLayer {

    }
}
