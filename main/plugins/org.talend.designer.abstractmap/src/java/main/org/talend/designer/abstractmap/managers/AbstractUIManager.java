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
package org.talend.designer.abstractmap.managers;

import org.apache.log4j.Level;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.designer.abstractmap.ui.dnd.DraggingInfosPopup;
import org.talend.designer.abstractmap.ui.properties.LINK_STYLE;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 */
public abstract class AbstractUIManager {

    private LINK_STYLE linkStyle;

    private int currentDragDetail;

    private boolean dragging;

    private boolean shiftPressed, ctrlPressed;

    /**
     * DOC amaumont AbstractUIManager constructor comment.
     */
    public AbstractUIManager() {
        super();
    }

    public int getCurrentDragDetail() {
        return this.currentDragDetail;
    }

    public void setCurrentDragDetail(int currentDragDetail) {
        this.currentDragDetail = currentDragDetail;
    }

    /**
     * DOC amaumont Comment method "setDragging".
     *
     * @param b
     */
    public void setDragging(boolean dragging) {
        this.dragging = dragging;
    }

    public boolean isDragging() {
        return this.dragging;
    }

    public boolean isShiftPressed() {
        return shiftPressed;
    }

    public boolean isCtrlPressed() {
        return ctrlPressed;
    }

    public void setCtrlPressed(boolean ctrlPressed) {
        this.ctrlPressed = ctrlPressed;
    }

    public void setShiftPressed(boolean shiftPressed) {
        this.shiftPressed = shiftPressed;
    }

    /**
     * DOC amaumont Comment method "getCountLinks".
     */
    public int getCountLinks() {
        return getAbstractMapperManager().getLinkManager().getCurrentNumberLinks();
    }

    public LINK_STYLE getLinkStyle() {
        if (linkStyle == null) {

            String elementParameterValue = (String) getAbstractMapperManager().getElementParameterValue("LINK_STYLE"); //$NON-NLS-1$
            if ("AUTO".equals(elementParameterValue)) { //$NON-NLS-1$
                if (getCountLinks() > 50) {
                    linkStyle = LINK_STYLE.LINE;
                } else {
                    linkStyle = LINK_STYLE.BEZIER_CURVE;
                }
            } else {
                LINK_STYLE defaultLinkStyle = LINK_STYLE.BEZIER_CURVE;
                if (elementParameterValue != null) {
                    try {
                        linkStyle = LINK_STYLE.valueOf(elementParameterValue);
                    } catch (RuntimeException e) {
                        ExceptionHandler.process(e, Level.WARN);
                        linkStyle = defaultLinkStyle;
                    }
                } else {
                    linkStyle = defaultLinkStyle;
                }
            }
        }
        return linkStyle;
    }

    public void openPropertySetDialog() {
        // do nothing.
    }

    public void openAutoMappingDialog() {
        // do nothing.
    }

    public abstract AbstractMapperManager getAbstractMapperManager();

    public abstract void refreshBackground(boolean forceRecalculate, boolean firstExecutionAfterTime);
    public abstract DraggingInfosPopup getDraggingInfosPopup();
}
