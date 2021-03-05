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
package org.talend.designer.core.ui.editor.connections;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.ImageFigure;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.swt.graphics.Image;
import org.talend.commons.ui.runtime.image.ECoreImage;
import org.talend.commons.ui.runtime.image.ImageProvider;

/**
 * DOC Administrator class global comment. Detailled comment
 */
public class ConnectionResumingFigure extends Figure {

    //    private static final String FIELD_SEP = "|"; //$NON-NLS-1$
    //
    //    private static final String FIELD_EQUAL = "="; //$NON-NLS-1$
    //
    // private static final Color BACKGROUND = new Color(null, 220, 220, 220);
    //
    // private static final int MAX_VARIABLE_WIDTH = 70;
    //
    // private static final int MAX_VALUE_WIDTH = 100;

    private Connection connection;

    // private boolean maximized;

    // private ConnectionTraceFigure tooltip = null;

    // private CollapseFigure collapseButton;

    public ConnectionResumingFigure(Connection connection, boolean maximized) {
        ToolbarLayout layout = new ToolbarLayout();
        setLayoutManager(layout);
        this.connection = connection;
        // this.maximized = maximized;
        // if (maximized) {
        // tooltip = new ConnectionTraceFigure(connection, false);
        // this.setToolTip(tooltip);
        // tooltip.setVisible(false);
        // }

    }

    @Override
    public void paint(Graphics graphics) {
        super.paint(graphics);
    }

    public void setResumingCheckPoint(boolean traceFlag) {
        if (traceFlag) {
            Image enableImage = getResumingConnectionImage();
            setPreferredSize(enableImage.getImageData().width, enableImage.getImageData().height);
            this.getChildren().clear();
            ImageFigure figure = new ImageFigure(enableImage);
            add(figure);
            setVisible(true);
        } else {
            setPreferredSize(0, 0);
            setVisible(false);
        }
        // if (tooltip != null) {
        // tooltip.setTraceData(data, flag, traceFlag);
        // }
    }

    /**
     *
     * cLi Comment method "getTraceConnectionImage".
     *
     * feature 6355.
     */
    private Image getResumingConnectionImage() {
        Image image = null;
        image = ImageProvider.getImage(ECoreImage.RESUMING_CHECKPOINT_ICON);
        return image;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.draw2d.Figure#getPreferredSize(int, int)
     */
    @Override
    public Dimension getPreferredSize(int hint, int hint2) {
        return super.getPreferredSize(hint, hint2);
    }

}
