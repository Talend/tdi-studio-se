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

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PolygonDecoration;
import org.eclipse.draw2d.PolylineConnection;
import org.talend.designer.gefabstractmap.figures.routers.CurveConnectionRouter;
import org.talend.designer.gefabstractmap.figures.routers.LookupConnectionRouter;
import org.talend.designer.gefabstractmap.resource.ColorInfo;
import org.talend.designer.gefabstractmap.resource.ColorProviderMapper;

/**
 * DOC wchen class global comment. Detailled comment
 */
public abstract class FilterConnectionPart extends BaseConnectionEditPart {

    private CurveConnectionRouter curvrRouter;

    private LookupConnectionRouter cr;

    @Override
    protected IFigure createFigure() {
        PolylineConnection connection = new PolylineConnection();
        connection.setTargetDecoration(new PolygonDecoration());
        connection.setForegroundColor(ColorProviderMapper.getColor(ColorInfo.COLOR_UNSELECTED_FILTER_LINK));
        connection.setLineWidth(2);

        return connection;
    }

    protected abstract int calculateConnOffset();

    @Override
    public IFigure getFigure() {
        PolylineConnection figure = (PolylineConnection) super.getFigure();
        if (getTarget() instanceof OutputTablePart) {
            if (curvrRouter == null) {
                curvrRouter = new CurveConnectionRouter();
                figure.setConnectionRouter(curvrRouter);
            }
        } else if (getTarget() instanceof InputTablePart) {
            if (cr == null) {
                cr = new LookupConnectionRouter();
                figure.setConnectionRouter(cr);
            }
        }
        if (cr != null) {
            cr.setOffset(calculateConnOffset());
        }
        return figure;
    }

    @Override
    public void updateForegroundColor(boolean selected) {
        if (selected) {
            getFigure().setForegroundColor(ColorProviderMapper.getColor(ColorInfo.COLOR_SELECTED_FILTER_LINK));
        } else {
            getFigure().setForegroundColor(ColorProviderMapper.getColor(ColorInfo.COLOR_UNSELECTED_FILTER_LINK));
        }
    }
}
