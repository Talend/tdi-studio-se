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
package org.talend.designer.gefabstractmap.editor;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Viewport;
import org.eclipse.gef.editparts.ScalableRootEditPart;

/**
 * WCHEN talend class global comment. Detailled comment
 */
public class MapperScalableRootEditPart extends ScalableRootEditPart {

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.editparts.ScalableRootEditPart#createFigure()
     */
    @Override
    protected IFigure createFigure() {
        Viewport figure = (Viewport) super.createFigure();
        figure.setContentsTracksWidth(true);
        return figure;
    }

}
