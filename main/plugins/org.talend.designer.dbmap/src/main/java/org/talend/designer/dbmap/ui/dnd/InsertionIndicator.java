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
package org.talend.designer.dbmap.ui.dnd;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.talend.commons.ui.runtime.ws.WindowSystem;
import org.talend.designer.dbmap.managers.MapperManager;
import org.talend.designer.dbmap.managers.UIManager;
import org.talend.designer.dbmap.ui.color.ColorInfo;
import org.talend.designer.dbmap.ui.color.ColorProviderMapper;
import org.talend.designer.dbmap.ui.visualmap.table.DataMapTableView;
import org.talend.designer.dbmap.ui.visualmap.zone.scrollable.TablesZoneView;

/**
 * Only one instance by ZoneView should be necessary. This class create 2 arrows which follow cursor when dragging on a
 * <code>DataMapTableView</code>. Arrows are adjusted at left and right on the limit between two
 * <code>TableItem</code>.
 *
 *
 * $Id: InsertionIndicator.java 898 2006-12-07 11:06:17Z amaumont $
 *
 */
public class InsertionIndicator {

    private final TablesZoneView tablesZoneViewParent;

    private Composite leftArrowDraggingIndicator;

    private Composite rightArrowDraggingIndicator;

    private final MapperManager mapperManager;

    /**
     * current y of the indicator.
     */
    private int indicYPositionRefTable;

    private Listener tablePaintListener;

    private Table draggableTable;

    public static final int HEIGHT_INDICATOR = 10;

    /**
     * last y position of indicator calculated, where origin is origin of TablesZoneView.
     */
    private int lastIndicYPositionRefZone;

    private boolean visible;

    private final Color colorIndicator = ColorProviderMapper.getColor(ColorInfo.COLOR_DRAGGING_INSERTION_INDICATOR);

    private int lastNPixelsCovered;

    /**
     *
     *
     * @param tablesZoneViewParent
     * @param mapperManager
     */
    public InsertionIndicator(TablesZoneView tablesZoneViewParent, MapperManager mapperManager) {
        this.tablesZoneViewParent = tablesZoneViewParent;
        this.mapperManager = mapperManager;
        createComponents();
    }

    /**
     * DOC amaumont Comment method "createComponents".
     */
    private void createComponents() {

        leftArrowDraggingIndicator = new Composite(tablesZoneViewParent, SWT.NONE | SWT.NO_BACKGROUND);
        leftArrowDraggingIndicator.setVisible(false);
        leftArrowDraggingIndicator.setBackgroundMode(SWT.INHERIT_DEFAULT);
        FormData formDataLeftArrow = new FormData();
        formDataLeftArrow.width = 12;
        formDataLeftArrow.height = HEIGHT_INDICATOR;
        formDataLeftArrow.left = new FormAttachment(0, 0);
        formDataLeftArrow.left = new FormAttachment(0, -formDataLeftArrow.width + 2);
        formDataLeftArrow.top = new FormAttachment(0, 0);
        leftArrowDraggingIndicator.setLayoutData(formDataLeftArrow);

        rightArrowDraggingIndicator = new Composite(tablesZoneViewParent, SWT.NONE | SWT.NO_BACKGROUND);
        rightArrowDraggingIndicator.setVisible(false);
        rightArrowDraggingIndicator.setBackgroundMode(SWT.INHERIT_DEFAULT);
        FormData formDataRightArrow = new FormData();
        formDataRightArrow.width = 13;
        formDataRightArrow.height = HEIGHT_INDICATOR;
        formDataRightArrow.left = new FormAttachment(0, 0); // this property is recalculated at each top changement
        formDataRightArrow.top = new FormAttachment(0, 0);
        rightArrowDraggingIndicator.setLayoutData(formDataRightArrow);

        addListeners();
    }

    /**
     * DOC amaumont Comment method "addListeners".
     */
    private void addListeners() {
        Listener paintListener = new Listener() {

            public void handleEvent(Event event) {

                Composite composite = (Composite) event.widget;

                Rectangle bounds = composite.getBounds();

                // //////////////////////////////////////////////////////////
                // draw image filled with transparent pixels
                RGB transparentColor = new RGB(0, 255, 0);
                PaletteData paletteData = new PaletteData(new RGB[] { transparentColor });

                ImageData imageData = new ImageData(bounds.width, bounds.height, 1, paletteData);
                int transparentPixelValue = imageData.palette.getPixel(transparentColor);
                imageData.transparentPixel = transparentPixelValue;

                GC gc = event.gc;
                final Image image = new Image(gc.getDevice(), imageData);

                gc.drawImage(image, 0, 0);
                image.dispose();
                // ////////////////////////////////////////////////////////////////////////////

                // //////////////////////////////////////////////////////////
                // draw left arrow
                int yCenter = bounds.height / 2;
                int widthExternalArrow = 10;
                gc.setBackground(colorIndicator);
                gc.setForeground(colorIndicator);

                if (composite == leftArrowDraggingIndicator) {
                    // external left arrow
                    Point leftCrossPoint = new Point(widthExternalArrow, yCenter);
                    gc.fillPolygon(new int[] { 0, 0, leftCrossPoint.x, leftCrossPoint.y, 0, bounds.height, });
                    gc.drawLine(leftCrossPoint.x, leftCrossPoint.y, bounds.width, leftCrossPoint.y);

                } else {
                    // external right arrow
                    Point rightCrossPoint = new Point(bounds.width - widthExternalArrow, yCenter);
                    gc.fillPolygon(new int[] { bounds.width, 0, rightCrossPoint.x, rightCrossPoint.y, bounds.width,
                            bounds.height, });
                    gc.drawLine(rightCrossPoint.x, rightCrossPoint.y, -bounds.width, rightCrossPoint.y);
                }
            }
        };

        leftArrowDraggingIndicator.addListener(SWT.Paint, paintListener);
        rightArrowDraggingIndicator.addListener(SWT.Paint, paintListener);
    }

    /**
     * Update position of the indicator at top of <code>itemIndexTarget</code> position of the
     * <code>draggableTable</code>.
     *
     * @param currentTable
     * @param itemIndexTarget
     */
    public void updatePosition(Table currentTable, int itemIndexTarget) {

        // System.out.println(itemIndexTarget);

        this.draggableTable = currentTable;
        removeTablePaintListener();
        if (tablePaintListener == null) {

            tablePaintListener = new Listener() {

                public void handleEvent(Event event) {
                    drawIndicatorLineInTable(event);
                }

            };
        }
        FormLayout formLayout = tablesZoneViewParent.getLayout();

        UIManager uiManager = mapperManager.getUiManager();
        TablesZoneView tablesZoneViewOutputs = uiManager.getTablesZoneViewOutputs();
        Display display = tablesZoneViewOutputs.getDisplay();
        Point tablePositionRefZone = display.map(currentTable, tablesZoneViewParent, new Point(0, 0));

        FormData formDataLeftArrow = (FormData) leftArrowDraggingIndicator.getLayoutData();
        FormData formDataRightArrow = (FormData) rightArrowDraggingIndicator.getLayoutData();
        ScrollBar verticalBar = currentTable.getVerticalBar();
        int offsetVerticalBar = -verticalBar.getSelection() * currentTable.getItemHeight();
        int indicYPositionRefZone = 0;
        if (WindowSystem.isGTK()) {
            if (itemIndexTarget == 0) {
                indicYPositionRefTable = 0 + offsetVerticalBar;
            } else {
                indicYPositionRefTable = itemIndexTarget * (currentTable.getItemHeight() + 2) + offsetVerticalBar;
            }
            indicYPositionRefZone = indicYPositionRefTable + tablePositionRefZone.y + formLayout.marginTop
                    - HEIGHT_INDICATOR / 2;
            indicYPositionRefZone -= currentTable.getItemHeight() + 1;
        } else {
            if (itemIndexTarget == 0) {
                indicYPositionRefTable = 0 + offsetVerticalBar;
            } else {
                indicYPositionRefTable = itemIndexTarget * currentTable.getItemHeight() - 1 + offsetVerticalBar;
            }
            indicYPositionRefZone = indicYPositionRefTable + tablePositionRefZone.y + formLayout.marginTop
                    - HEIGHT_INDICATOR / 2 + 4;
        }

        DataMapTableView dataMapTableView = mapperManager.retrieveDataMapTableView(currentTable);
        Rectangle boundsTableView = dataMapTableView.getBounds();

        int testValue = boundsTableView.y + boundsTableView.height - formLayout.marginTop - HEIGHT_INDICATOR / 2 - 5;
        if (indicYPositionRefZone > testValue) {
            indicYPositionRefZone = testValue;
        }

        currentTable.addListener(SWT.Paint, tablePaintListener);
        if (lastIndicYPositionRefZone != indicYPositionRefZone) {
            formDataLeftArrow.top.offset = indicYPositionRefZone;
            formDataRightArrow.top.offset = indicYPositionRefZone;

            formDataRightArrow.left.offset = currentTable.getSize().x + 2;
            currentTable.redraw();
            tablesZoneViewParent.layout();
        }
        lastIndicYPositionRefZone = indicYPositionRefZone;
    }

    /**
     * DOC amaumont Comment method "removeTablePaintListener".
     */
    private void removeTablePaintListener() {
        if (this.draggableTable != null && tablePaintListener != null) {
            draggableTable.removeListener(SWT.Paint, tablePaintListener);
        }
        tablePaintListener = null;
    }

    /**
     * Hide left and right arrows, and redraw current table without line indication.
     */
    private void hide() {
        removeTablePaintListener();
        if (draggableTable != null) {
            draggableTable.redraw();
            draggableTable.layout();
        }
        lastIndicYPositionRefZone = 0;
        leftArrowDraggingIndicator.setVisible(false);
        rightArrowDraggingIndicator.setVisible(false);

    }

    /**
     * Hide left and right arrows, and redraw current table without line indication.
     */
    private void show() {
        draggableTable.redraw();
        leftArrowDraggingIndicator.setVisible(true);
        rightArrowDraggingIndicator.setVisible(true);
    }

    public void setVisible(boolean visible) {
        if (this.visible != visible) {
            if (visible) {
                show();
            } else {
                hide();
            }
        }
        this.visible = visible;
    }

    public boolean isVisible() {
        return this.visible;
    }

    private void drawIndicatorLineInTable(Event event) {
        if (visible) {
            GC gc = event.gc;
            Rectangle area = draggableTable.getClientArea();
            int y = 0;
            if (WindowSystem.isGTK()) {
                y = indicYPositionRefTable;
            } else {
                y = draggableTable.getHeaderHeight() + indicYPositionRefTable;
            }

            gc.setForeground(colorIndicator);
            gc.drawLine(0, y, area.width, y);
        }
    }

    public void redraw() {
        if (draggableTable != null) {
            draggableTable.redraw();
        }
    }

    /**
     * DOC amaumont Comment method "isIntersect".
     *
     * @param boundsPopupFromMapperShellOrigin
     * @return
     */
    public boolean isLeftArrowMustBeRefreshed(Rectangle boundsPopupFromMapperShellOrigin) {
        Shell parentShell = tablesZoneViewParent.getShell();
        Display display = parentShell.getDisplay();
        Point positionFromMapperShellOrigin = display.map(leftArrowDraggingIndicator, parentShell, new Point(0, 0));
        Rectangle leftArrowDraggingIndicatorBounds = leftArrowDraggingIndicator.getBounds();
        Rectangle boundsFromPositionOrigin = new Rectangle(positionFromMapperShellOrigin.x - 4,
                positionFromMapperShellOrigin.y - 4, leftArrowDraggingIndicatorBounds.width + 8,
                leftArrowDraggingIndicatorBounds.height + 8);
        Rectangle rectangleIntersect = boundsFromPositionOrigin.intersection(boundsPopupFromMapperShellOrigin);
        int nPixelsCoveredByPopup = rectangleIntersect.width * rectangleIntersect.height;
        boolean response = false;
        if (nPixelsCoveredByPopup < this.lastNPixelsCovered) {
            response = true;
        }
        this.lastNPixelsCovered = nPixelsCoveredByPopup;
        return response;
    }

    public void setLefArrowVisible(boolean isVisible) {
        leftArrowDraggingIndicator.setVisible(isVisible);
    }

    public void setRightArrowVisible(boolean isVisible) {
        rightArrowDraggingIndicator.setVisible(isVisible);
    }

    public boolean isRightArrowVisible() {
        return rightArrowDraggingIndicator.getVisible();
    }

}
