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
package org.talend.designer.core.ui.editor.properties.notes;

import org.eclipse.gef.commands.CommandStack;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetWidgetFactory;
import org.talend.core.ui.properties.tab.HorizontalTabFactory;
import org.talend.designer.core.ui.AbstractMultiPageTalendEditor;
import org.talend.designer.core.ui.editor.notes.Note;

/**
 * yzhang class global comment. Detailled comment
 */

public abstract class AbstractNotePropertyComposite {

    protected Note note;

    private HorizontalTabFactory tabFactory;

    private IEditorPart multiPageTalendEditor;

    public static final int STANDARD_LABEL_WIDTH = 85;

    public abstract Composite getComposite();

    /**
     * yzhang AbstarctNotePropertyComposite constructor comment.
     */
    public AbstractNotePropertyComposite(Composite parent, Note note, HorizontalTabFactory tabFactory) {

        multiPageTalendEditor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
        this.note = note;
        this.tabFactory = tabFactory;
        createControl(parent);
    }

    public abstract void createControl(Composite parent);

    public TabbedPropertySheetWidgetFactory getWidgetFactory() {
        return this.tabFactory.getWidgetFactory();
    }

    protected CommandStack getCommandStack() {
        if (multiPageTalendEditor instanceof AbstractMultiPageTalendEditor) {
            return (CommandStack) ((AbstractMultiPageTalendEditor) multiPageTalendEditor).getTalendEditor().getAdapter(
                    CommandStack.class);
        }
        return null;
    }
}
