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
package org.talend.designer.dbmap.ui.event;

import org.eclipse.swt.graphics.Point;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 * $Id: ResizeHelper.java 1 2006-09-29 17:06:40Z nrousseau $
 *
 */
public class ResizeHelper {

    /**
     * DOC amaumont ResizeHelper class global comment. Detailled comment <br/>
     *
     */
    public enum RESIZE_MODE {
        HORIZONTAL,
        VERTICAL,
        BOTH,
        NONE,
    }

    private Point lastDragPoint;

    private RESIZE_MODE currentMode;

    public void startDrag(Point point) {
        lastDragPoint = point;
    }

    public void stopDrag() {
        lastDragPoint = null;
    }

    public boolean isDragging() {
        return lastDragPoint != null;
    }

    public Point getLastDragPoint() {
        return lastDragPoint;
    }

    public void setLastDragPoint(Point lastDragPoint) {
        this.lastDragPoint = lastDragPoint;
    }

    public RESIZE_MODE getCurrentMode() {
        return currentMode;
    }

    public void setCurrentMode(RESIZE_MODE currentMode) {
        this.currentMode = currentMode;
    }

}
