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
package org.talend.expressionbuilder.ui;

import java.util.Stack;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;

/**
 * yzhang class global comment. Detailled comment <br/>
 *
 * $Id: UndoRecorder.java 上午11:12:22 2007-9-13 +0000 (2007-9-13) yzhang $
 *
 */

public class ExpressionRecorder {

    private final Stack<String> stack;

    private final Button button;

    private boolean isUndo;

    private int undoCounter;

    private Point cursorPosition;

    /**
     * yzhang ExpressionRecorder constructor comment.
     */
    public ExpressionRecorder(Button button) {
        stack = new Stack<String>();
        this.button = button;
    }

    /**
     * yzhang Comment method "addRecored".
     *
     * @param expression
     */
    public void pushRecored(String expression) {
        if (!isUndo) {
            if (stack.size() == 0) {
                stack.push(""); //$NON-NLS-1$
            }
            stack.push(expression);
            if (!button.isEnabled()) {
                button.setEnabled(true);
            }
            undoCounter = 0;
        } else {
            return;
        }
    }

    /**
     * yzhang Comment method "undo".
     */
    public void undo() {
        isUndo = true;
    }

    /**
     * yzhang Comment method "undoFinished".
     */
    public void undoFinished() {
        isUndo = false;
    }

    /**
     * yzhang Comment method "popRecored".
     *
     * @return
     */
    public String popRecored() {
        if (stack.size() != 0) {
            if (undoCounter == 0) {
                stack.pop();
            }
            String expression = stack.pop();
            undoCounter++;
            if (stack.size() == 0) {
                button.setEnabled(false);
            }
            return expression;
        }
        ExpressionComposite expressionComposite = ExpressionBuilderDialog.getExpressionComposite();
        return expressionComposite.textControl.getText();
    }

    /**
     * Getter for cursorPosition.
     *
     * @return the cursorPosition
     */
    public Point getCursorPosition() {
        return this.cursorPosition;
    }

    /**
     * Sets the cursorPosition.
     *
     * @param cursorPosition the cursorPosition to set
     */
    public void setCursorPosition(Point cursorPosition) {
        this.cursorPosition = cursorPosition;
    }

}
