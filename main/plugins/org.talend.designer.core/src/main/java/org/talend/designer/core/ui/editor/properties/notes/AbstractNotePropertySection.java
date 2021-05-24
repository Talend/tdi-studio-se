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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.gef.commands.CommandStack;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.views.properties.tabbed.AbstractPropertySection;
import org.talend.designer.core.ui.AbstractMultiPageTalendEditor;
import org.talend.designer.core.ui.editor.notes.Note;
import org.talend.designer.core.ui.editor.notes.NoteEditPart;

/**
 */
public abstract class AbstractNotePropertySection extends AbstractPropertySection implements PropertyChangeListener {

    protected Note note;

    private AbstractMultiPageTalendEditor multiPageTalendEditor;

    protected CommandStack getCommandStack() {
        return (CommandStack) multiPageTalendEditor.getTalendEditor().getAdapter(CommandStack.class);
    }

    @Override
    public void setInput(IWorkbenchPart part, ISelection selection) {
        super.setInput(part, selection);

        if (part instanceof AbstractMultiPageTalendEditor) {
            multiPageTalendEditor = (AbstractMultiPageTalendEditor) part;
        } else {
            multiPageTalendEditor = (AbstractMultiPageTalendEditor) PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                    .getActivePage().getActiveEditor();
        }

        if (getSelection() instanceof IStructuredSelection) {
            IStructuredSelection structuredSelection = (IStructuredSelection) getSelection();
            if (structuredSelection.getFirstElement() instanceof NoteEditPart) {
                NoteEditPart noteEditPart = (NoteEditPart) structuredSelection.getFirstElement();
                note = (Note) noteEditPart.getModel();
            }
        }
    }

    @Override
    public void aboutToBeShown() {
        note.addPropertyChangeListener(this);
    }

    @Override
    public void aboutToBeHidden() {
        note.removePropertyChangeListener(this);
    }

    public void propertyChange(PropertyChangeEvent evt) {
        refresh();
    }

}
