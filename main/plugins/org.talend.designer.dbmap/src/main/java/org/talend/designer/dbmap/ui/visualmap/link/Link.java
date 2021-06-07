// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.dbmap.ui.visualmap.link;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.talend.commons.ui.swt.drawing.link.IDrawableLink;
import org.talend.commons.ui.swt.drawing.link.IStyleLink;
import org.talend.designer.abstractmap.ui.visualmap.link.ILinkState;
import org.talend.designer.abstractmap.ui.visualmap.link.IMapperLink;
import org.talend.designer.abstractmap.ui.visualmap.link.PointLinkDescriptor;
import org.talend.designer.abstractmap.ui.visualmap.zone.IMapZone;
import org.talend.designer.dbmap.managers.MapperManager;
import org.talend.designer.dbmap.managers.UIManager;
import org.talend.designer.dbmap.ui.MapperUI;
import org.talend.designer.dbmap.ui.visualmap.table.DataMapTableView;
import org.talend.designer.dbmap.ui.visualmap.zone.Zone;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 * $Id$
 *
 */
public class Link implements IMapperLink {

    protected PointLinkDescriptor pointLinkDescriptor1;

    protected PointLinkDescriptor pointLinkDescriptor2;

    protected Point point1;

    protected Point point2;

    private ILinkState linkState;

    protected UIManager uiManager;

    protected MapperManager mapperManager;

    protected int widthTable1;

    protected int widthTable2;

    private Integer level;

    public Link(PointLinkDescriptor pointDescriptor1, PointLinkDescriptor pointDescriptor2, MapperManager mapperManager) {
        super();
        this.pointLinkDescriptor1 = pointDescriptor1;
        this.pointLinkDescriptor2 = pointDescriptor2;
        this.mapperManager = mapperManager;
        this.uiManager = mapperManager.getUiManager();
        calculate();

    }

    public void calculate() {
        point1 = this.uiManager.getTableEntryPosition(pointLinkDescriptor1.getTableEntry(), true);
        point2 = this.uiManager.getTableEntryPosition(pointLinkDescriptor2.getTableEntry(), true);
        DataMapTableView viewTable1 = this.mapperManager.retrieveDataMapTableView(pointLinkDescriptor1.getTableEntry());
        DataMapTableView viewTable2 = this.mapperManager.retrieveDataMapTableView(pointLinkDescriptor2.getTableEntry());
        widthTable1 = viewTable1.getBounds().width + 2 * viewTable1.getBorderWidth();
        widthTable2 = viewTable2.getBounds().width + 2 * viewTable2.getBorderWidth();
    }

    public void draw(GC gc, Rectangle boundsOfDrawing) {
        StyleLinkFactory drawableLinkFactory = mapperManager.getUiManager().getStyleLinkFactory();

        IStyleLink styleLink = drawableLinkFactory.getStyleLink(this);
        IDrawableLink drawableLink = styleLink.getDrawableLink();
        if (drawableLink == null) {
            throw new NullPointerException();
        }

        IMapZone zone1 = pointLinkDescriptor1.getZone();
        IMapZone zone2 = pointLinkDescriptor2.getZone();

        int yOffsetPoint1 = uiManager.getVerticalScrolledOffsetBar((Zone) zone1);
        int yOffsetPoint2 = uiManager.getVerticalScrolledOffsetBar((Zone) zone2);

        boolean leftSidePosition = ((zone1 == Zone.INPUTS || zone1 == Zone.OUTPUTS) && zone1 == zone2);

        int point1xForTraceLink = point1.x + (leftSidePosition ? 0 : widthTable1 - 4);
        int point2xForTraceLink = point2.x + (leftSidePosition ? 0 : 0);

        int point1yOffset = point1.y + yOffsetPoint1;
        int point2yOffset = point2.y + yOffsetPoint2;

        drawableLink.setPoint1(new Point(point1xForTraceLink, point1yOffset));
        drawableLink.setPoint2(new Point(point2xForTraceLink, point2yOffset));
        drawableLink.setBoundsOfCalculate(boundsOfDrawing);

        Rectangle boundsOfSolidLine = new Rectangle(boundsOfDrawing.x, boundsOfDrawing.y, boundsOfDrawing.width,
                boundsOfDrawing.height);
        boundsOfSolidLine.y = boundsOfDrawing.y + MapperUI.OFFSET_VISIBLES_POINTS;
        boundsOfSolidLine.height = boundsOfDrawing.height - 2 * MapperUI.OFFSET_VISIBLES_POINTS;

        drawableLink.getStyle().apply(gc);

        // gc.setForeground(new Color(gc.getDevice(), 200, 186, 225)); violet pastel
        // gc.setForeground(new Color(gc.getDevice(), 160, 40, 210)); // violet
        // gc.setForeground(new Color(gc.getDevice(), 200, 200, 0)); //
        // gc.setForeground(new Color(gc.getDevice(), 255, 150, 20)); // orange
        // gc.setForeground(new Color(gc.getDevice(), 255, 200, 70)); // orange pastel

        if (!boundsOfSolidLine.contains(new Point(point1xForTraceLink, point1yOffset))
                || !boundsOfSolidLine.contains(new Point(point2xForTraceLink, point2yOffset))) {
            // gc.setLineDash(new int[] { 1, spaceBetweenPoints, 1 });
            gc.setLineStyle(SWT.LINE_DOT);
            // gc.setLineStyle(SWT.LINE_CUSTOM);
            // System.out.println("SWT.LINE_DOT");
        } else {
            gc.setLineStyle(SWT.LINE_SOLID);
            // System.out.println("SWT.LINE_SOLID");
        }

        if (this.level != null) {
            drawableLink.setConnectorWidth(this.level * 5);
        }

        drawableLink.draw(gc);
    }

    /**
     * Getter for linkState.
     *
     * @return the linkState
     */
    public ILinkState getState() {
        return this.linkState;
    }

    /**
     * Sets the linkState.
     *
     * @param state the linkState to set
     */
    public void setState(ILinkState state) {
        this.linkState = state;
    }

    /**
     * Getter for point1.
     *
     * @return the point1
     */
    public Point getPoint1() {
        return this.point1;
    }

    /**
     * Sets the point1.
     *
     * @param point1 the point1 to set
     */
    public void setPoint1(Point point1) {
        this.point1 = point1;
    }

    /**
     * Getter for point2.
     *
     * @return the point2
     */
    public Point getPoint2() {
        return this.point2;
    }

    /**
     * Sets the point2.
     *
     * @param point2 the point2 to set
     */
    public void setPoint2(Point point2) {
        this.point2 = point2;
    }

    /**
     * Getter for pointDescriptor1.
     *
     * @return the pointDescriptor1
     */
    public PointLinkDescriptor getPointLinkDescriptor1() {
        return this.pointLinkDescriptor1;
    }

    /**
     * Sets the pointDescriptor1.
     *
     * @param pointDescriptor1 the pointDescriptor1 to set
     */
    public void setPointLinkDescriptor1(PointLinkDescriptor pointDescriptor1) {
        this.pointLinkDescriptor1 = pointDescriptor1;
    }

    /**
     * Getter for pointDescriptor2.
     *
     * @return the pointDescriptor2
     */
    public PointLinkDescriptor getPointLinkDescriptor2() {
        return this.pointLinkDescriptor2;
    }

    /**
     * Sets the pointDescriptor2.
     *
     * @param pointDescriptor2 the pointDescriptor2 to set
     */
    public void setPointLinkDescriptor2(PointLinkDescriptor pointDescriptor2) {
        this.pointLinkDescriptor2 = pointDescriptor2;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.dbmap.ui.visualmap.link.IMapperLink#getLevel()
     */
    public Integer getLevel() {
        return level;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.dbmap.ui.visualmap.link.IMapperLink#setLevel(java.lang.Integer)
     */
    public void setLevel(Integer level) {
        this.level = level;
    }

}
