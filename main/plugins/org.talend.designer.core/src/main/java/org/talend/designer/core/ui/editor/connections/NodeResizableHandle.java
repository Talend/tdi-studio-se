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

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Locator;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.handles.ResizeHandle;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.RGB;
import org.talend.commons.ui.utils.image.ColorUtils;

/**
 * DOC Talend class global comment. Detailled comment
 */
public class NodeResizableHandle extends ResizeHandle {

    /**
     * DOC Talend NodeResizableHandle constructor comment.
     *
     * @param owner
     * @param direction
     */
    public NodeResizableHandle(GraphicalEditPart owner, int direction) {
        super(owner, direction);
    }

    public NodeResizableHandle(GraphicalEditPart owner, Locator loc, Cursor c) {
        super(owner, loc, c);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.handles.SquareHandle#getFillColor()
     */
    @Override
    protected Color getFillColor() {
        return (isPrimary()) ? ColorUtils.getCacheColor(new RGB(176, 222, 154)) : ColorConstants.white;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.handles.SquareHandle#getBorderColor()
     */
    @Override
    protected Color getBorderColor() {
        return (isPrimary()) ? ColorConstants.gray : ColorConstants.black;
    }

}
