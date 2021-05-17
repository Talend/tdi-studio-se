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
package org.talend.designer.abstractmap.ui.listener;

import org.eclipse.jface.util.TransferDropTargetListener;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ScrollBar;
import org.talend.commons.ui.swt.dnd.DNDKeyAnalyzer;
import org.talend.designer.abstractmap.managers.AbstractMapperManager;
import org.talend.designer.abstractmap.managers.AbstractUIManager;
import org.talend.designer.abstractmap.ui.dnd.DraggingInfosPopup;
import org.talend.designer.abstractmap.ui.dnd.TableEntriesTransfer;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 * $Id: DefaultDropTargetListener.java 898 2006-12-07 11:06:17Z amaumont $
 *
 */
public class DefaultDropTargetListener implements TransferDropTargetListener {

    protected AbstractMapperManager mapperManager;

    private Point lastCursorPosition;

    private long lastDragTime;

    private boolean scrollUp = false;

    public DefaultDropTargetListener(AbstractMapperManager mapperManager) {
        super();
        this.mapperManager = mapperManager;
    }

    protected AbstractMapperManager getMapperManager() {
        return this.mapperManager;
    }

    protected AbstractUIManager getUiManager() {
        return mapperManager.getUiManager();
    }

    public void dragEnter(DropTargetEvent event) {
        // updatePopupPosition(event);
        setPopupVisible(true);
        event.detail = mapperManager.getUiManager().getCurrentDragDetail();
    }

    public void dragLeave(DropTargetEvent event) {
        dragFinished();
    }

    private void dragFinished() {
        setPopupVisible(false);
        if (!isSubclassed()) {
            mapperManager.getUiManager().setDragging(false);
        }
    }

    private void setPopupVisible(boolean visible) {
        AbstractUIManager uiManager = mapperManager.getUiManager();
        DraggingInfosPopup draggingInfosPopup = uiManager.getDraggingInfosPopup();
        draggingInfosPopup.setVisible(visible);
    }

    public void dragOperationChanged(DropTargetEvent event) {
        updatePopupPosition(event);
        detectPressedKeys(event);
        AbstractUIManager uiManager = mapperManager.getUiManager();

        DraggingInfosPopup draggingInfosPopup = uiManager.getDraggingInfosPopup();
        if (uiManager.isCtrlPressed()) {
            draggingInfosPopup.setOverwriteMode(true);
        } else {
            draggingInfosPopup.setOverwriteMode(false);
        }

        if (uiManager.isShiftPressed()) {
            draggingInfosPopup.setMapOneToOneMode(true, true);
        } else {
            draggingInfosPopup.setMapOneToOneMode(false, true);
        }

        int detail = DND.DROP_NONE;

        if (uiManager.isCtrlPressed() && uiManager.isShiftPressed()) {
            detail |= DND.DROP_COPY;
        } else if (uiManager.isCtrlPressed()) {
            detail |= DND.DROP_LINK;
        } else if (uiManager.isShiftPressed()) {
            detail |= DND.DROP_COPY;
        } else {
            detail |= DND.DROP_COPY;
        }

        uiManager.setCurrentDragDetail(detail);

        event.detail = DND.DROP_NONE;
    }

    /**
     * DOC amaumont Comment method "detectPressedKeys".
     *
     * @param event
     */
    protected void detectPressedKeys(DropTargetEvent event) {
        DNDKeyAnalyzer keyAnalyzer = new DNDKeyAnalyzer(event);
        AbstractUIManager uiManager = mapperManager.getUiManager();
        uiManager.setCtrlPressed(keyAnalyzer.isCtrlPressed());
        uiManager.setShiftPressed(keyAnalyzer.isShiftPressed());

    }

    public void dragOver(DropTargetEvent event) {
        // System.out.println(((DropTarget)event.widget).getControl());
        event.detail = DND.DROP_NONE;
        autoScroll(event);
        updatePopupPosition(event);
        setPopupVisible(true);
    }

    private void updatePopupPosition(DropTargetEvent event) {
        Point cursorPosition = new Point(event.x, event.y);
        if (!cursorPosition.equals(lastCursorPosition)) {
            AbstractUIManager uiManager = mapperManager.getUiManager();
            DraggingInfosPopup draggingInfosPopup = uiManager.getDraggingInfosPopup();
            draggingInfosPopup.setCursorPosition(event.x, event.y);
        }
        // System.out.println("updatePopupPosition:" + lastCursorPosition + "-->" + cursorPosition);
        lastCursorPosition = cursorPosition;
    }

    private void autoScroll(DropTargetEvent event) {
        Composite currentComposite = null;
        Object source = event.getSource();
        if (source instanceof DropTarget) {
            Control control = ((DropTarget) source).getControl();
            if (control instanceof Composite) {
                currentComposite = (Composite) control;
            }
        }
        if (currentComposite == null) {
            return;
        }

        ScrolledComposite parentScrolledComposite = getParentScrolledComposite(currentComposite);
        if (parentScrolledComposite == null) {
            return;
        }

        ScrollBar vBar = parentScrolledComposite.getVerticalBar();

        Control content = parentScrolledComposite.getContent();
        if (content != null) {
            Point location = content.getLocation();
            int vSelection = vBar.getSelection();
            int increment = 0;
            int thumb = vBar.getThumb();
            if (lastCursorPosition == null || lastCursorPosition.y == event.y) {
                long currentTimeMillis = System.currentTimeMillis();
                long time = currentTimeMillis - lastDragTime;

                if (lastDragTime != 0 && time > 100) {
                    Point pointToScrollComposite = parentScrolledComposite.toControl(event.x, event.y);

                    // scroll up
                    if (pointToScrollComposite.y < 40) {
                        increment = -20;
                    }
                    // scroll down
                    else if (thumb - pointToScrollComposite.y < 40) {
                        increment = 20;
                    } else {
                        return;
                    }

                } else {
                    return;
                }
            } else {
                increment = event.y - lastCursorPosition.y;
                if (increment > 0) {
                    increment = increment + 3;
                } else {
                    increment = increment - 3;
                }
            }

            if (vSelection >= 0 && vSelection + increment > 0 && vSelection + increment < vBar.getMaximum()) {
                vBar.setSelection(vSelection + increment);
            }
            content.setLocation(location.x, -vSelection);
            mapperManager.getUiManager().refreshBackground(true, false);
            lastDragTime = System.currentTimeMillis();
        }

        // Event e = new Event();
        // e.data = event.data;
        // e.x = event.x;
        // e.y = event.y;
        // e.detail = event.detail;
        // e.item = event.item;
        // e.count = event.y > lastCursorPosition.y ? 1 : 0;
        // e.display = event.display;
        // e.widget = event.widget;
        // if (parentScrolledComposite != null) {
        // parentScrolledComposite.notifyListeners(SWT.MouseWheel, e);
        // }
    }

    private ScrolledComposite getParentScrolledComposite(Composite composite) {
        if (composite == null) {
            return null;
        }
        if (composite instanceof ScrolledComposite) {
            return (ScrolledComposite) composite;
        }
        return getParentScrolledComposite(composite.getParent());
    }

    public void drop(DropTargetEvent event) {
        dragFinished();
    }

    /**
     * DOC amaumont Comment method "isSubclassed".
     *
     * @return
     */
    private boolean isSubclassed() {
        return getClass() != DefaultDropTargetListener.class;
    }

    public void dropAccept(DropTargetEvent event) {

    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.util.TransferDropTargetListener#getTransfer()
     */
    public Transfer getTransfer() {
        return TableEntriesTransfer.getInstance();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.util.TransferDropTargetListener#isEnabled(org.eclipse.swt.dnd.DropTargetEvent)
     */
    public boolean isEnabled(DropTargetEvent event) {
        return true;
    }

}
