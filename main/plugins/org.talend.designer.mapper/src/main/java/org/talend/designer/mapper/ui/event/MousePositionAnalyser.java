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
package org.talend.designer.mapper.ui.event;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 * $Id$
 *
 */
public class MousePositionAnalyser {

    private Composite composite;

    private static final int DEFAULT_VALUE = 10;

    public static final int DEFAULT_LEFT_BORDER_OFFSET = DEFAULT_VALUE;

    public static final int DEFAULT_RIGHT_BORDER_OFFSET = DEFAULT_VALUE;

    public static final int DEFAULT_BOTTOM_BORDER_OFFSET = DEFAULT_VALUE;

    private int leftBorderOffset = DEFAULT_LEFT_BORDER_OFFSET;

    private int rightBorderOffset = DEFAULT_RIGHT_BORDER_OFFSET;

    private int bottomBorderOffset = DEFAULT_BOTTOM_BORDER_OFFSET;

    public MousePositionAnalyser() {
        super();
        init();
    }

    private void init() {
    }

    public MousePositionAnalyser(Composite composite) {
        super();
        this.composite = composite;
        init();
    }

    public Composite getComposite() {
        return composite;
    }

    public void setComposite(Composite composite) {
        this.composite = composite;
    }

    public boolean isOnLeftBottomCorner(Point position) {
        return isOnLeftBorder(position) && isOnBottomBorder(position);
    }

    public boolean isOnRightBottomCorner(Point position) {
        return isOnRightBorder(position) && isOnBottomBorder(position);
    }

    public boolean isOnRightBorder(Point position) {
        assert (composite != null);
        return position.x >= getWidth() - rightBorderOffset;
    }

    private int getWidth() {
        return getSize().x;
    }

    private int getHeight() {
        return getSize().y;
    }

    private Point getSize() {
        return composite.getSize();
    }

    public boolean isOnLeftBorder(Point position) {
        return position.x <= leftBorderOffset;
    }

    public boolean isOnBottomBorder(Point position) {
        assert (composite != null);
        return position.y >= getHeight() - bottomBorderOffset;
    }

    public int getBottomBorderOffset() {
        return bottomBorderOffset;
    }

    public void setBottomBorderOffset(int bottomBorderOffset) {
        this.bottomBorderOffset = bottomBorderOffset;
    }

    public int getLeftBorderOffset() {
        return leftBorderOffset;
    }

    public void setLeftBorderOffset(int leftBorderOffset) {
        this.leftBorderOffset = leftBorderOffset;
    }

    public int getRightBorderOffset() {
        return rightBorderOffset;
    }

    public void setRightBorderOffset(int rightBorderOffset) {
        this.rightBorderOffset = rightBorderOffset;
    }

}
