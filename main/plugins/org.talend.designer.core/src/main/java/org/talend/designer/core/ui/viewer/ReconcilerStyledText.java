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
package org.talend.designer.core.ui.viewer;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.talend.commons.CommonsPlugin;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.designer.core.i18n.Messages;

/**
 * DOC nrousseau ReconcilerViewer class global comment. Detailled comment
 */
public class ReconcilerStyledText extends StyledText {

    private boolean isDebugMode = false;

    private ReconcilerViewer viewer;

    /**
     * DOC nrousseau ReconcilerStyledText constructor comment.
     *
     * @param parent
     * @param style
     * @param reconcilerViewer TODO
     */
    public ReconcilerStyledText(Composite parent, int style, ReconcilerViewer viewer) {
        super(parent, style);
        this.viewer = viewer;
        isDebugMode = CommonsPlugin.isDebugMode();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.swt.custom.StyledText#getText()
     */
    @Override
    public String getText() {
        IRegion region = viewer.getViewerRegion();
        try {
            return viewer.getDocument().get(region.getOffset(), region.getLength());
        } catch (BadLocationException e) {
            return super.getText();
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.swt.custom.StyledText#setText(java.lang.String)
     */
    @Override
    public void setText(String text) {
        super.setText(text);
        if (viewer.getUndoManager() != null) {
            viewer.getUndoManager().reset();
        }
    }

    @Override
    public Rectangle getTextBounds(int start, int end) {
        /**
         * There is issue in Eclipse4.10 CursorLinePainter#paint CursorLinePainter#updateHighlightLine, the update
         * action is based on lines in the whole document rather than the visible document, so there will be issue with
         * new line in VisibleRegion
         */

        int newStart = start;
        int newEnd = end;
        int charCount = getCharCount();
        boolean illegalArgs = false;
        if (charCount <= newStart) {
            if (charCount == 0 && newStart == 0) {
                // normal case
            } else {
                illegalArgs = true;
                newStart = charCount - 1;
            }
        }
        if (newStart < 0) {
            illegalArgs = true;
            newStart = 0;
        }
        if (charCount <= newEnd) {
            if (charCount == 0 && newEnd == 0) {
                // normal case
            } else {
                illegalArgs = true;
                newEnd = charCount - 1;
            }
        }
        if (newEnd < newStart) {
            illegalArgs = true;
            newEnd = newStart;
        }
        if (illegalArgs && isDebugMode) {
            ExceptionHandler.process(new IllegalArgumentException(
                    Messages.getString("ReconcilerStyledText.illegalArgs", charCount, start, end, newStart, newEnd)));
        }
        if (charCount <= 0 && newStart <= 0 && newEnd <= 0) {
            return new Rectangle(0, 0, 0, 0);
        } else {
            return super.getTextBounds(newStart, newEnd);
        }
    }

}
