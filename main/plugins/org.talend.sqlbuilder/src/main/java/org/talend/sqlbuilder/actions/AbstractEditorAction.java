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
package org.talend.sqlbuilder.actions;

import org.eclipse.jface.action.Action;
import org.talend.sqlbuilder.ui.editor.ISQLEditor;

/**
 * Abstract implementation for a sql editor actions. Extend this class to add new actions to the sql editor.
 *
 * @author Davy Vanherbergen
 *
 */
public abstract class AbstractEditorAction extends Action {

    public abstract String getText();

    public String getToolTipText() {
        return getText();
    }

    public abstract void run();

    protected ISQLEditor editor;

    /**
     * Sets the editor.
     *
     * @param editor the editor to set
     */
    public final void setEditor(ISQLEditor editor) {
        this.editor = editor;
    }

    /**
     * Getter for editor.
     *
     * @return the editor
     */
    public ISQLEditor getEditor() {
        return this.editor;
    }

}
