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
package org.talend.designer.core.ui.editor.cmd;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.commands.Command;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.ui.PlatformUI;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.ui.AbstractMultiPageTalendEditor;
import org.talend.designer.core.ui.editor.TalendEditor;
import org.talend.designer.core.ui.editor.notes.Note;
import org.talend.designer.core.ui.editor.notes.NoteEditPart;
import org.talend.designer.core.ui.editor.process.Process;
import org.talend.designer.core.ui.editor.process.ProcessPart;

/**
 * Command used to paste all the components.
 *
 * $Id: NodesPasteCommand.java 4549 2007-07-13 05:18:48Z nrousseau $
 *
 */
public class NotesPasteCommand extends Command {

    private boolean isMultiple;

    private Process process;

    private List<EditPart> oldSelection;

    private List<Note> noteList;

    private Point firstNodeLocation;

    private List<NoteEditPart> noteParts;

    private boolean multipleCommand;

    public NotesPasteCommand(List<NoteEditPart> noteParts, Process process, Point cursorLocation, boolean isMultiple, Point point) {
        this.process = process;
        this.isMultiple = isMultiple;
        this.firstNodeLocation = point;
        orderNoteParts(noteParts);
        setLabel(Messages.getString("NotesPasteCommand.label")); //$NON-NLS-1$
        setCursorLocation(cursorLocation);
    }

    Point cursorLocation = null;

    /**
     * Getter for cursorLocation.
     *
     * @return the cursorLocation
     */
    public Point getCursorLocation() {
        return this.cursorLocation;
    }

    /**
     * Sets the cursorLocation.
     *
     * @param cursorLocation the cursorLocation to set
     */
    public void setCursorLocation(Point cursorLocation) {
        this.cursorLocation = cursorLocation;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.commands.Command#canExecute()
     */
    @Override
    public boolean canExecute() {
        return !process.isReadOnly();
    }

    private void orderNoteParts(List<NoteEditPart> noteParts) {
        this.noteParts = new ArrayList<NoteEditPart>();

        Point curLocation;

        NoteEditPart toAdd = null;

        List<NoteEditPart> restToOrder = new ArrayList<NoteEditPart>();
        restToOrder.addAll(noteParts);

        for (NoteEditPart copiedNodePart : noteParts) {
            curLocation = null;
            for (NoteEditPart partToOrder : restToOrder) {
                Note copiedNote = (Note) partToOrder.getModel();
                if (curLocation == null) {
                    curLocation = copiedNote.getLocation();
                    toAdd = partToOrder;
                } else {
                    if (curLocation.y >= copiedNote.getLocation().y) {
                        if (curLocation.x >= copiedNote.getLocation().x) {
                            curLocation = copiedNote.getLocation();
                            toAdd = partToOrder;
                        }
                    }
                }
            }
            if (toAdd != null) {
                this.noteParts.add(toAdd);
                restToOrder.remove(toAdd);
            }
        }
    }

    /**
     *
     * Will return a empty location for a component from a given point.
     *
     * @param location
     * @return
     */
    private Point findLocationForNote(final Point location, final Dimension size, int index, int firstIndex) {
        Point newLocation = findLocationForNoteInProcess(location, size);
        newLocation = findLocationForNoteInContainerList(newLocation, size, index, firstIndex);
        return newLocation;
    }

    @SuppressWarnings("unchecked")
    private Point findLocationForNoteInProcess(final Point location, Dimension size) {
        Rectangle copiedRect = new Rectangle(location.x, location.y, size.width, size.height);
        Point newLocation = new Point(location);
        for (Note node : process.getNotes()) {
            Rectangle currentRect = new Rectangle(node.getLocation().x, node.getLocation().y, node.getSize().width, node
                    .getSize().height);
            if (currentRect.intersects(copiedRect)) {
                newLocation.x += TalendEditor.GRID_SIZE;
                newLocation.y += TalendEditor.GRID_SIZE;
                return findLocationForNoteInProcess(newLocation, size);
            }
        }
        return newLocation;
    }

    private Point findLocationForNoteInContainerList(final Point location, Dimension size, int index, int firstIndex) {
        Rectangle copiedRect = new Rectangle(location.x, location.y, size.width, size.height);
        Point newLocation = new Point(location);
        if (!isMultiple) {
            for (Note node : noteList) {
                Rectangle currentRect = new Rectangle(node.getLocation().x, node.getLocation().y, node.getSize().width, node
                        .getSize().height);
                if (currentRect.intersects(copiedRect)) {
                    newLocation = computeTheDistance(index, firstIndex, newLocation);
                    // return findLocationForNoteInContainerList(newLocation, size, index, firstIndex);
                }
            }
        } else {
            if (getCursorLocation() == null) {
                return newLocation;
            }
            newLocation = computeTheDistance(index, firstIndex, newLocation);
        }
        return newLocation;
    }

    private Point computeTheDistance(int index, int firstIndex, Point location) {
        Point currentNodeLocation = null;
        if (!isMultiple) {
            firstNodeLocation = ((Note) noteParts.get(firstIndex).getModel()).getLocation();
        }
        currentNodeLocation = ((Note) noteParts.get(index).getModel()).getLocation();

        int distanceX = firstNodeLocation.x - currentNodeLocation.x;
        int distanceY = firstNodeLocation.y - currentNodeLocation.y;
        location.x = location.x - distanceX;
        location.y = location.y - distanceY;
        return location;
    }

    @SuppressWarnings("unchecked")
    private void createNoteList() {
        int firstIndex = 0;
        int index = 0;
        noteList = new ArrayList<Note>();

        // create the notes
        for (NoteEditPart copiedNodePart : noteParts) {
            Note copiedNote = (Note) copiedNodePart.getModel();
            Note pastedNote = new Note();
            pastedNote.setOpaque(copiedNote.isOpaque());
            pastedNote.setText(copiedNote.getText());
            pastedNote.setSize(copiedNote.getSize());

            // see bug 0005571: Copy/Paste of Note doesn't keep Format
            EParameterName[] params = new EParameterName[] { EParameterName.NOTE_FONT, EParameterName.FONT_SIZE,
                    EParameterName.FONT_BOLD, EParameterName.FONT_ITALIC, EParameterName.NOTE_LINECOLOR,
                    EParameterName.NOTE_COLOR, EParameterName.NOTETXT_COLOR, EParameterName.NOTETXT_LEFT,
                    EParameterName.NOTETXT_RIGHT, EParameterName.NOTETXT_CENTER, EParameterName.NOTELABEL_CENTER,
                    EParameterName.NOTETXT_TOP, EParameterName.NOTETXT_BOTTOM };
            for (EParameterName param : params) {
                Object value = copiedNote.getElementParameter(param.getName()).getValue();
                pastedNote.getElementParameter(param.getName()).setValue(value);
            }
            pastedNote.setOpaque(copiedNote.isOpaque());

            pastedNote.setProcess(process);
            Point location = null;
            if (getCursorLocation() == null) {
                location = copiedNote.getLocation();
            } else {
                location = getCursorLocation();
                index = noteParts.indexOf(copiedNodePart);
            }
            if (process.isGridEnabled()) {
                // replace the component to set it on the grid if it's enabled
                int tempVar = location.x / TalendEditor.GRID_SIZE;
                location.x = tempVar * TalendEditor.GRID_SIZE;
                tempVar = location.y / TalendEditor.GRID_SIZE;
                location.y = tempVar * TalendEditor.GRID_SIZE;
            }
            pastedNote.setLocation(findLocationForNote(location, copiedNote.getSize(), index, firstIndex));

            noteList.add(pastedNote);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute() {
        // create the note list to paste
        createNoteList();

        AbstractMultiPageTalendEditor multiPageTalendEditor = (AbstractMultiPageTalendEditor) PlatformUI.getWorkbench()
                .getActiveWorkbenchWindow().getActivePage().getActiveEditor();
        GraphicalViewer viewer = multiPageTalendEditor.getTalendEditor().getViewer();

        // save old selection
        if (!multipleCommand) {
            oldSelection = new ArrayList<EditPart>();
            for (EditPart editPart : (List<EditPart>) viewer.getSelectedEditParts()) {
                oldSelection.add(editPart);
            }
            // remove the old selection
            viewer.deselectAll();
        }

        // creates the different notes
        for (Note note : noteList) {
            process.addNote(note);
        }
        // set the new note as the current selection
        if (!multipleCommand) {
            EditPart processPart = (EditPart) viewer.getRootEditPart().getChildren().get(0);
            if (processPart instanceof ProcessPart) { // can only be ProcessPart but still test
                List<EditPart> sel = new ArrayList<EditPart>();
                for (EditPart editPart : (List<EditPart>) processPart.getChildren()) {
                    if (editPart instanceof NoteEditPart) {
                        Note currentNode = (Note) editPart.getModel();
                        if (noteList.contains(currentNode)) {
                            sel.add(editPart);
                        }
                    }
                }
                StructuredSelection s = new StructuredSelection(sel);
                viewer.setSelection(s);
            }
        }
        process.checkStartNodes();
        process.checkProcess();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void undo() {
        // remove the current selection
        AbstractMultiPageTalendEditor multiPageTalendEditor = (AbstractMultiPageTalendEditor) PlatformUI.getWorkbench()
                .getActiveWorkbenchWindow().getActivePage().getActiveEditor();
        GraphicalViewer viewer = multiPageTalendEditor.getTalendEditor().getViewer();
        if (!multipleCommand) {
            viewer.deselectAll();
        }

        for (Note note : noteList) {
            process.removeNote(note);
        }

        // set the old selection active
        if (!multipleCommand) {
            StructuredSelection s = new StructuredSelection(oldSelection);
            viewer.setSelection(s);
        }

        process.checkStartNodes();
        process.checkProcess();
    }

    /**
     * Getter for multipleCommand.
     *
     * @return the multipleCommand
     */
    public boolean isMultipleCommand() {
        return multipleCommand;
    }

    /**
     * Sets the multipleCommand.
     *
     * @param multipleCommand the multipleCommand to set
     */
    public void setMultipleCommand(boolean multipleCommand) {
        this.multipleCommand = multipleCommand;
    }

    /**
     * Getter for noteList.
     *
     * @return the noteList
     */
    public List<Note> getNoteList() {
        return noteList;
    }
}
