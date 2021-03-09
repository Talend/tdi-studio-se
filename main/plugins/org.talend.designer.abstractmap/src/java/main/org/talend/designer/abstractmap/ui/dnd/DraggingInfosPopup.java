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
package org.talend.designer.abstractmap.ui.dnd;

import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.talend.commons.ui.runtime.ws.WindowSystem;
import org.talend.designer.abstractmap.i18n.Messages;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 * $Id: DraggingInfosPopup.java 54939 2011-02-11 01:34:57Z mhirt $
 *
 */
public final class DraggingInfosPopup {

    private Popup popup;

    private Point cursorPosition;

    private boolean insertionEntryContext;

    private boolean expressionContext;

    private boolean mapOneToOne;

    private Shell parent;

    private boolean outputToOutputMode;

    private boolean dropInvalid = true;

    private DraggingInfosPopup(Shell parent) {
        this.parent = parent;
        init(parent);
    }

    /**
     *
     * DOC amaumont DraggingInfosPopup class global comment. Detailled comment <br/>
     *
     * $Id: DraggingInfosPopup.java 54939 2011-02-11 01:34:57Z mhirt $
     *
     */
    private class Popup extends PopupDialog {

        private Label labelEntries;

        private Composite mainComposite;

        private Label labelWriteMode;

        private Label labelMappingMode;

        private Label labelInsertionEntryMode;

        private Label labelDropInvalid;

        public Popup(Shell parent) {
            super(parent, SWT.ON_TOP, false, false, false, false, null, null);
        }

        @Override
        protected Control createDialogArea(Composite parentComposite) {
            mainComposite = new Composite(parentComposite, SWT.NONE);
            RowLayout rowLayout = new RowLayout(SWT.VERTICAL);
            rowLayout.fill = true;
            rowLayout.pack = true;
            rowLayout.marginWidth = 2;
            rowLayout.marginHeight = 0;
            rowLayout.marginBottom = 5;
            rowLayout.spacing = 0;
            mainComposite.setLayout(rowLayout);
            labelEntries = new Label(mainComposite, SWT.HORIZONTAL);
            labelEntries.setLayoutData(new RowData());

            labelWriteMode = new Label(mainComposite, SWT.HORIZONTAL);
            labelWriteMode.setLayoutData(new RowData());

            labelMappingMode = new Label(mainComposite, SWT.HORIZONTAL);
            labelMappingMode.setLayoutData(new RowData());

            labelInsertionEntryMode = new Label(mainComposite, SWT.HORIZONTAL);
            labelInsertionEntryMode.setLayoutData(new RowData());

            labelDropInvalid = new Label(mainComposite, SWT.HORIZONTAL);
            labelDropInvalid.setLayoutData(new RowData());

            setLabelsVisible(false, false, false, true);

            return mainComposite;
        }

        @Override
        protected void adjustBounds() {

            if (cursorPosition == null) {
                cursorPosition = new Point(0, 0);
            }
            Shell popupShell = popup.getShell();
            Rectangle popupBounds = popupShell.getBounds();
            final Shell mapperShell = popup.getParentShell();

            Point point = mapperShell.getDisplay().map(popupShell, mapperShell, new Point(0, 0));

            Rectangle boundsToRedraw = new Rectangle(point.x, point.y, popupBounds.width, popupBounds.height);

            Point preferredSize = getPreferredSize();
            int width = preferredSize.x;
            int height = preferredSize.y;
            boolean isVisible = popup.isVisible();
            if (width != popupBounds.width || height != popupBounds.height) {
                setVisible(false);
            }
            int newX = cursorPosition.x - width - 20;
            if (newX < 0) {
                newX = 0;
            }
            int newY = cursorPosition.y - height - 20;
            if (WindowSystem.isGTK()) {
                newY -= 25;
            }
            if (newY < 0) {
                newY = 0;
            }
            Rectangle newBounds = new Rectangle(newX, newY, width, height);
            getShell().setBounds(newBounds);

            // to get round refresh problem
            if (isVisible() != isVisible) {
                setVisible(isVisible);
            } else {
                mapperShell.redraw(boundsToRedraw.x, boundsToRedraw.y, boundsToRedraw.width, boundsToRedraw.height, false);
            }
        }

        /**
         * DOC amaumont Comment method "setVisible".
         *
         * @param visible
         */
        public void setVisible(boolean visible) {
            if (visible != getShell().isVisible()) {
                getShell().setVisible(visible);
            }
        }

        public boolean isVisible() {
            return getShell().isVisible();
        }

        /**
         * DOC amaumont Comment method "setText".
         *
         * @param text
         */
        public void setCountEntriesText(String text) {
            if (!text.equals(labelEntries.getText())) {
                labelEntries.setText(text);
                adjustBounds();
            }
        }

        /**
         * DOC amaumont Comment method "setText".
         *
         * @param text
         */
        public void setWriteModeText(String text) {
            if (!text.equals(labelWriteMode.getText())) {
                labelWriteMode.setText(text);
                adjustBounds();
            }
        }

        /**
         * DOC amaumont Comment method "setMappingModeText".
         *
         * @param mode
         */
        public void setMappingModeText(String text) {
            if (!text.equals(labelMappingMode.getText())) {
                labelMappingMode.setText(text);
                adjustBounds();
            }
        }

        /**
         * DOC amaumont Comment method "setMappingModeText".
         *
         * @param mode
         */
        public void setInsertionEntryText(String text) {
            if (!text.equals(labelInsertionEntryMode.getText())) {
                labelInsertionEntryMode.setText(text);
                adjustBounds();
            }
        }

        /**
         * DOC amaumont Comment method "setMappingModeText".
         *
         * @param mode
         */
        public void setDropInvalidText(String text) {
            if (!text.equals(labelDropInvalid.getText())) {
                labelDropInvalid.setText(text);
                adjustBounds();
            }
        }

        private Point getPreferredSize() {
            return mainComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        }

        public void setLabelsVisible(boolean boolLabelWriteMode, boolean boolLabelMappingMode,
                boolean boolLabelInsertionEntryMode, boolean boolDropInvalid) {
            ((RowData) labelWriteMode.getLayoutData()).exclude = !boolLabelWriteMode;
            ((RowData) labelMappingMode.getLayoutData()).exclude = !boolLabelMappingMode;
            ((RowData) labelInsertionEntryMode.getLayoutData()).exclude = !boolLabelInsertionEntryMode;
            ((RowData) labelDropInvalid.getLayoutData()).exclude = !boolDropInvalid;
            labelWriteMode.setVisible(boolLabelWriteMode);
            labelMappingMode.setVisible(boolLabelMappingMode);
            labelInsertionEntryMode.setVisible(boolLabelInsertionEntryMode);
            labelDropInvalid.setVisible(boolDropInvalid);
        }

    }

    /**
     * DOC amaumont Comment method "init".
     *
     * @param parentShell
     */
    private void init(Shell parentShell) {
        popup = new Popup(parentShell);
        popup.open();
        popup.setVisible(false);
        setDropInvalid(true, false);
        setOverwriteMode(false);
        setMapOneToOneMode(false, true);
    }

    public static DraggingInfosPopup getNewShell(Shell parent) {
        return new DraggingInfosPopup(parent);
    }

    /**
     * DOC amaumont Comment method "getBounds".
     *
     * @return
     */
    public Rectangle getBounds() {
        return popup.getShell().getBounds();
    }

    /**
     * DOC amaumont Comment method "setVisible".
     *
     * @param b
     */
    public void setVisible(boolean visible) {
        popup.setVisible(visible);
    }

    /**
     * DOC amaumont Comment method "setCountDragginEntries".
     *
     * @param countEntries
     */
    public void setCountDraggingEntries(int countEntries) {
        String text = null;
        if (countEntries <= 1) {
            text = Messages.getString("DraggingInfosPopup.draggingEntry", countEntries); //$NON-NLS-1$
        } else {
            text = Messages.getString("DraggingInfosPopup.draggingEntries", countEntries);//$NON-NLS-1$
        }
        popup.setCountEntriesText(text);
    }

    /**
     * DOC amaumont Comment method "setCountDragginEntries".
     *
     * @param countEntries
     */
    public void setOverwriteMode(boolean overwrite) {
        String mode = null;
        if (overwrite) {
            mode = Messages.getString("DraggingInfosPopup.overwriteMode"); //$NON-NLS-1$
        } else {
            if (WindowSystem.isGTK()) {
                mode = Messages.getString("DraggingInfosPopup.appendMode"); //$NON-NLS-1$
            } else {
                mode = Messages.getString("DraggingInfosPopup.appendModeToOverwrite"); //$NON-NLS-1$
            }
        }
        popup.setWriteModeText(mode);
    }

    /**
     * method "setCountDragginEntries".
     *
     * if mapOneToOneAuthorized, mapOneToOne is forced to false
     *
     * @param isMapOneToOne
     * @param mapOneToOneAuthorized
     */
    public void setMapOneToOneMode(boolean isMapOneToOne, boolean mapOneToOneAuthorized) {
        String mode = null;
        if (isMapOneToOne && mapOneToOneAuthorized) {
            if (outputToOutputMode) {
                mode = Messages.getString("DraggingInfosPopup.eachSourceEntryExpressionToEachTargetExpression"); //$NON-NLS-1$
            } else {
                mode = Messages.getString("DraggingInfosPopup.eachSourceEntryToEachTargetExpression"); //$NON-NLS-1$
            }
            this.mapOneToOne = isMapOneToOne;
        } else {
            if (outputToOutputMode) {
                if (mapOneToOneAuthorized && !WindowSystem.isGTK()) {
                    mode = Messages
                            .getString("DraggingInfosPopup.allSourceEntriesExpressionToASingleOneTargetExpressionShiftKey"); //$NON-NLS-1$
                } else {
                    mode = Messages.getString("DraggingInfosPopup.allSourceEntriesExpressionToASingleOneTargetExpression"); //$NON-NLS-1$
                }

            } else {
                if (mapOneToOneAuthorized && !WindowSystem.isGTK()) {
                    mode = Messages.getString("DraggingInfosPopup.allSourceEntriesToASingleOneTargetExpressionShiftKey"); //$NON-NLS-1$
                } else {
                    mode = Messages.getString("DraggingInfosPopup.allSourceEntriesToASingleOneTargetExpression"); //$NON-NLS-1$
                }
            }

            this.mapOneToOne = false;
        }
        popup.setMappingModeText(mode);
    }

    public void setOutputToOutputMode(boolean outputToOutputMode) {
        this.outputToOutputMode = outputToOutputMode;
    }

    /**
     * DOC amaumont Comment method "setCursorPosition".
     *
     * @param x
     * @param y
     */
    public void setCursorPosition(int x, int y) {
        this.cursorPosition = new Point(x, y);
        // System.out.println("setCursorPosition");
        popup.adjustBounds();
    }

    public boolean isInsertionEntryContext() {
        return this.insertionEntryContext;
    }

    public void setInsertionEntryContext(boolean insertionEntryMode) {
        this.insertionEntryContext = insertionEntryMode;
        String newText = "null"; //$NON-NLS-1$
        if (this.insertionEntryContext && !this.mapOneToOne) {
            newText = Messages.getString("DraggingInfosPopup.insertAllSelectedEntries"); //$NON-NLS-1$
        } else if (this.insertionEntryContext && this.mapOneToOne) {
            newText = Messages.getString("DraggingInfosPopup.insertRemainingEntries"); //$NON-NLS-1$
        }
        if (!popup.labelInsertionEntryMode.getText().equals(newText)) {
            popup.setInsertionEntryText(newText);
            updateVisibleLabels();
        }
        this.insertionEntryContext = insertionEntryMode;
    }

    public void updateVisibleLabels() {
        // System.out.println(expressionContext + " "+insertionEntryContext);
        popup.setLabelsVisible(expressionContext, expressionContext, this.insertionEntryContext, this.dropInvalid);
        popup.adjustBounds();
    }

    public boolean isExpressionContext() {
        return this.expressionContext;
    }

    public void setExpressionContext(boolean expressionMode) {
        if (expressionMode != this.expressionContext) {
            this.expressionContext = expressionMode;
            updateVisibleLabels();
        }
    }

    public Point getPositionFromMapperShellOrigin() {
        Display display = popup.getShell().getDisplay();
        return display.map(popup.getShell(), this.parent, new Point(0, 0));
    }

    public boolean isMapOneToOne() {
        return this.mapOneToOne;
    }

    public void setDropInvalid(boolean isDropInvalid, boolean isInvalidKeyPressed) {
        String newText = "null"; //$NON-NLS-1$
        if (isDropInvalid) {
            if (WindowSystem.isGTK()) {
                if (isInvalidKeyPressed) {
                    newText = Messages.getString("DraggingInfosPopup.dropInvalidWithKey"); //$NON-NLS-1$
                } else {
                    newText = Messages.getString("DraggingInfosPopup.dropInvalid"); //$NON-NLS-1$
                }
            }
        }
        if (WindowSystem.isGTK()) {
            this.dropInvalid = isDropInvalid;
        } else {
            this.dropInvalid = false;
        }

        if (!popup.labelDropInvalid.getText().equals(newText)) {
            popup.setDropInvalidText(newText);
            updateVisibleLabels();
        }
    }
}
