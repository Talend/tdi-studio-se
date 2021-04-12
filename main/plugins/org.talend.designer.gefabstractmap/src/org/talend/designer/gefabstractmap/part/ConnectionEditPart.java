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
package org.talend.designer.gefabstractmap.part;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PolygonDecoration;
import org.eclipse.draw2d.PolylineConnection;
import org.talend.designer.gefabstractmap.figures.routers.CurveConnectionRouter;

/**
 * wchen class global comment. Detailled comment
 */
public class ConnectionEditPart extends BaseConnectionEditPart {

    private CurveConnectionRouter curvrRouter;

    @Override
    protected IFigure createFigure() {
        PolylineConnection connection = new PolylineConnection();
        connection.setTargetDecoration(new PolygonDecoration());
        curvrRouter = new CurveConnectionRouter();
        connection.setForegroundColor(ColorConstants.gray);
        connection.setLineWidth(2);
        connection.setConnectionRouter(curvrRouter);
        return connection;
    }

    public void updateForegroundColor(boolean selected) {
        if (selected) {
            getFigure().setForegroundColor(ColorConstants.yellow);
        } else {
            getFigure().setForegroundColor(ColorConstants.gray);
        }
    }

}
