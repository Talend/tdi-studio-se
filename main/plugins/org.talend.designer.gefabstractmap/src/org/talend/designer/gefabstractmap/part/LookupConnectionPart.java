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
import org.talend.designer.gefabstractmap.figures.routers.LookupConnectionRouter;
import org.talend.designer.gefabstractmap.resource.ColorInfo;
import org.talend.designer.gefabstractmap.resource.ColorProviderMapper;

/**
 * DOC wchen class global comment. Detailled comment
 */
public abstract class LookupConnectionPart extends BaseConnectionEditPart {

    private LookupConnectionRouter cr;

    @Override
    protected void createEditPolicies() {

    }

    @Override
    protected IFigure createFigure() {
        PolylineConnection connection = new PolylineConnection();
        connection.setTargetDecoration(new PolygonDecoration());
        // connection.setBackgroundColor(ColorConstants.yellow);
        connection.setForegroundColor(ColorProviderMapper.getColor(ColorInfo.COLOR_UNSELECTED_LOOKUP_LINKS));
        connection.setLineWidth(2);
        cr = new LookupConnectionRouter();
        connection.setConnectionRouter(cr);
        return connection;
    }

    protected abstract int calculateConnOffset();

    @Override
    public IFigure getFigure() {
        IFigure figure = super.getFigure();
        if (cr != null) {// && cr.getOffset() == 0
            cr.setOffset(calculateConnOffset());
        }
        return figure;
    }

    @Override
    public void updateForegroundColor(boolean selected) {
        if (selected) {
            getFigure().setForegroundColor(ColorProviderMapper.getColor(ColorInfo.COLOR_SELECTED_LOOKUP_LINKS));
        } else {
            getFigure().setForegroundColor(ColorProviderMapper.getColor(ColorInfo.COLOR_UNSELECTED_LOOKUP_LINKS));
        }
    }

}
