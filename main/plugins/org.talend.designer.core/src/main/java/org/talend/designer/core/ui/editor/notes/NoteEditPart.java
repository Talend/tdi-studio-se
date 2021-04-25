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
package org.talend.designer.core.ui.editor.notes;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.gef.tools.DirectEditManager;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.talend.commons.ui.utils.image.ColorUtils;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.ui.AbstractMultiPageTalendEditor;
import org.talend.designer.core.ui.editor.cmd.ChangeNoteOpacityCommand;
import org.talend.designer.core.ui.views.properties.ComponentSettingsView;

/**
 */
public class NoteEditPart extends AbstractGraphicalEditPart implements PropertyChangeListener {

    private DirectEditManager directEditManager;

    @Override
    protected void unregisterVisuals() {
        ((NoteFigure) getFigure()).disposeColors();
        this.disposeColors();
        super.unregisterVisuals();
    }

    private void disposeColors() {
        if (this.foreColor != null && !this.foreColor.isDisposed()) {
            foreColor.dispose();
        }
        if (this.backColor != null && !this.backColor.isDisposed()) {
            backColor.dispose();
        }
        if (this.labelColor != null && !this.labelColor.isDisposed()) {
            labelColor.dispose();
        }

    }

    @Override
    protected IFigure createFigure() {
        return new NoteFigure();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.editparts.AbstractEditPart#setSelected(int)
     */
    @Override
    public void setSelected(int value) {
        super.setSelected(value);
        if (value == SELECTED_PRIMARY) {
            ComponentSettingsView viewer = (ComponentSettingsView) PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                    .getActivePage().findView(ComponentSettingsView.ID);

            if (viewer != null) {
                viewer.setElement((Note) getModel());
            }

        } else if (value == SELECTED_NONE) {
            ComponentSettingsView viewer = (ComponentSettingsView) PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                    .getActivePage().findView(ComponentSettingsView.ID);

            if (viewer == null) {
                return;
            }
            ComponentSettingsView compSettings = viewer;
            compSettings.cleanDisplay();

        }

    }

    @Override
    protected void createEditPolicies() {
        installEditPolicy(EditPolicy.GRAPHICAL_NODE_ROLE, new NoteGraphicalEditPolicy());
        installEditPolicy(EditPolicy.DIRECT_EDIT_ROLE, new NoteDirectEditPolicy());
        installEditPolicy(EditPolicy.COMPONENT_ROLE, new NoteEditPolicy());
        installEditPolicy(EditPolicy.LAYOUT_ROLE, new NoteResizableEditPolicy());
    }

    Color foreColor = null;

    Color backColor = null;

    Color labelColor = null;

    @Override
    protected void refreshVisuals() {
        Note note = (Note) getModel();
        NoteFigure noteFigure = ((NoteFigure) getFigure());
        noteFigure.disposeColors();
        this.disposeColors();
        noteFigure.setLocation(note.getLocation());
        noteFigure.setSize(note.getSize());
        noteFigure.setText(note.getText());
        noteFigure.setOpaque(note.isOpaque());
        foreColor = new Color(null,
                ColorUtils.stringToRGB((String) note.getPropertyValue(EParameterName.NOTE_LINECOLOR.getName())));
        backColor = new Color(null, ColorUtils.stringToRGB((String) note.getPropertyValue(EParameterName.NOTE_COLOR.getName())));
        labelColor = new Color(null,
                ColorUtils.stringToRGB((String) note.getPropertyValue(EParameterName.NOTETXT_COLOR.getName())));
        noteFigure.setBackgroundColor(backColor);
        noteFigure.setForegroundColor(foreColor);
        noteFigure.getLabel().setForegroundColor(labelColor);
        if ((Boolean) note.getPropertyValue(EParameterName.NOTETXT_LEFT.getName())) {
            noteFigure.getLabel().setLabelAlignment(PositionConstants.LEFT);
        }
        if ((Boolean) note.getPropertyValue(EParameterName.NOTETXT_RIGHT.getName())) {
            noteFigure.getLabel().setLabelAlignment(PositionConstants.RIGHT);
        }
        if ((Boolean) note.getPropertyValue(EParameterName.NOTETXT_CENTER.getName())) {
            noteFigure.getLabel().setLabelAlignment(PositionConstants.CENTER);
        }
        if ((Boolean) note.getPropertyValue(EParameterName.NOTETXT_TOP.getName())) {
            noteFigure.getLabel().setTextAlignment(PositionConstants.TOP);
        }
        if ((Boolean) note.getPropertyValue(EParameterName.NOTETXT_BOTTOM.getName())) {
            noteFigure.getLabel().setTextAlignment(PositionConstants.BOTTOM);
        }
        if ((Boolean) note.getPropertyValue(EParameterName.NOTELABEL_CENTER.getName())) {
            noteFigure.getLabel().setTextAlignment(PositionConstants.CENTER);
        }
        if ((Boolean) note.getPropertyValue(EParameterName.FONT_BOLD.getName())
                && (Boolean) note.getPropertyValue(EParameterName.FONT_ITALIC.getName())) {
            Font font = new Font(null, (String) note.getPropertyValue(EParameterName.NOTE_FONT.getName()), Integer.parseInt(note
                    .getPropertyValue(EParameterName.FONT_SIZE.getName()).toString()), SWT.BOLD | SWT.ITALIC);
            noteFigure.getLabel().setFont(font);
        } else if ((Boolean) note.getPropertyValue(EParameterName.FONT_BOLD.getName())) {
            Font font = new Font(null, (String) note.getPropertyValue(EParameterName.NOTE_FONT.getName()), Integer.parseInt(note
                    .getPropertyValue(EParameterName.FONT_SIZE.getName()).toString()), SWT.BOLD);
            noteFigure.getLabel().setFont(font);
        } else if ((Boolean) note.getPropertyValue(EParameterName.FONT_ITALIC.getName())) {
            Font font = new Font(null, (String) note.getPropertyValue(EParameterName.NOTE_FONT.getName()), Integer.parseInt(note
                    .getPropertyValue(EParameterName.FONT_SIZE.getName()).toString()), SWT.ITALIC);
            noteFigure.getLabel().setFont(font);
        } else {
            Font font = new Font(null, (String) note.getPropertyValue(EParameterName.NOTE_FONT.getName()), Integer.parseInt(note
                    .getPropertyValue(EParameterName.FONT_SIZE.getName()).toString()), SWT.NULL);
            noteFigure.getLabel().setFont(font);
        }

    }

    @Override
    public void activate() {
        if (!isActive()) {
            super.activate();
            ((Note) getModel()).addPropertyChangeListener(this);
        }
    }

    @Override
    public void deactivate() {
        if (isActive()) {
            super.deactivate();
            ((Note) getModel()).removePropertyChangeListener(this);
            directEditManager = null;
        }
    }

    @Override
    public void propertyChange(final PropertyChangeEvent changeEvent) {
        refresh();
    }

    @Override
    public void performRequest(Request request) {
        Note note = (Note) getModel();
        if (note.isReadOnly()) {
            return;
        }

        if (request.getType() == RequestConstants.REQ_OPEN) {
            IWorkbenchPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
            if (part instanceof AbstractMultiPageTalendEditor) {
                CommandStack commandStack = (CommandStack) part.getAdapter(CommandStack.class);

                Command command = new ChangeNoteOpacityCommand(note, !note.isOpaque());
                commandStack.execute(command);
            }
        }
        if (request.getType() == RequestConstants.REQ_DIRECT_EDIT) {
            if (directEditManager == null) {
                NoteFigure noteFigure = (NoteFigure) getFigure();
                directEditManager = new NoteDirectEditManager(this, TextCellEditor.class, new NoteCellEditorLocator(noteFigure));
            }
            directEditManager.show();
        }
    }

    /**
     * yzhang Comment method "getNoteDirectEditManager".
     *
     * @return
     */
    public NoteDirectEditManager getDirectEditManager() {
        return (NoteDirectEditManager) directEditManager;
    }
}
