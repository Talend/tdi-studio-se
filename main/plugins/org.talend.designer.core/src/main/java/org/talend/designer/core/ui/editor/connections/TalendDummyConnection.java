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

import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Translatable;
import org.eclipse.gmf.runtime.draw2d.ui.figures.PolylineConnectionEx;
import org.eclipse.gmf.runtime.draw2d.ui.mapmode.IMapMode;

/**
 * DOC Talend class global comment. Detailled comment
 */
public class TalendDummyConnection extends PolylineConnectionEx implements IMapMode {

    public TalendDummyConnection() {
        this.setRoundedBendpointsRadius(32);
        // if
        // (!DesignerPlugin.getDefault().getPreferenceStore().getBoolean(TalendDesignerPrefConstants.EDITOR_LINESTYLE))
        // {
        // this.setTargetDecoration(new PolygonDecoration());
        // this.setLineWidth(1);
        // return;
        // }
        // this.setLineWidth(2);
        // PointList template = new PointList();
        // PolygonDecoration targetDecoration = new DecorationFigure(this, false);
        // targetDecoration.setScale(1, 1);
        // template.addPoint(new Point(-11, -5.5));
        // template.addPoint(new Point(-2, -5.5));
        // template.addPoint(0, -1);
        // template.addPoint(0, 1);
        // template.addPoint(new Point(-2, 5.5));
        // template.addPoint(new Point(-11, 5.5));
        // targetDecoration.setTemplate(template);
        // setTargetDecoration(targetDecoration);
        //
        // PolygonDecoration sourceDecoration = new DecorationFigure(this, true);
        // sourceDecoration.setScale(1, 1);
        // template = new PointList();
        //
        // template.addPoint(new Point(0, 5.5));
        // template.addPoint(new Point(-9, 5.5));
        // template.addPoint(-11, 1);
        // template.addPoint(-11, -1);
        // template.addPoint(new Point(-9, -5.5));
        // template.addPoint(new Point(0, -5.5));
        //
        // sourceDecoration.setTemplate(template);
        // setSourceDecoration(sourceDecoration);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gmf.runtime.draw2d.ui.mapmode.IMapMode#LPtoDP(int)
     */
    @Override
    public int LPtoDP(int logicalUnit) {
        return logicalUnit;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gmf.runtime.draw2d.ui.mapmode.IMapMode#DPtoLP(int)
     */
    @Override
    public int DPtoLP(int deviceUnit) {
        return deviceUnit;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gmf.runtime.draw2d.ui.mapmode.IMapMode#LPtoDP(org.eclipse.draw2d.geometry.Translatable)
     */
    @Override
    public Translatable LPtoDP(Translatable t) {
        t.performScale(32.0);
        return t;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gmf.runtime.draw2d.ui.mapmode.IMapMode#DPtoLP(org.eclipse.draw2d.geometry.Translatable)
     */
    @Override
    public Translatable DPtoLP(Translatable t) {
        t.performScale(32.0);
        return t;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gmf.runtime.draw2d.ui.figures.PolylineConnectionEx#outlineShape(org.eclipse.draw2d.Graphics)
     */
    @Override
    protected void outlineShape(Graphics g) {
        super.outlineShape(g);
        this.getPoints();
        this.getConnectionRouter();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.draw2d.PolylineConnection#getSourceAnchor()
     */
    @Override
    public ConnectionAnchor getSourceAnchor() {
        return super.getSourceAnchor();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.draw2d.PolylineConnection#getTargetAnchor()
     */
    @Override
    public ConnectionAnchor getTargetAnchor() {
        return super.getTargetAnchor();
    }

}
