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

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.PrecisionDimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.ResizableEditPolicy;
import org.eclipse.gef.requests.ChangeBoundsRequest;
import org.eclipse.swt.widgets.Canvas;
import org.talend.designer.core.ui.editor.TalendScalableFreeformRootEditPart;
import org.talend.designer.core.ui.editor.cmd.ResizeNoteCommand;
import org.talend.designer.core.ui.editor.process.Process;
import org.talend.designer.core.ui.editor.process.ProcessPart;

/**
 */
public class NoteResizableEditPolicy extends ResizableEditPolicy {

    @Override
    protected Command getResizeCommand(ChangeBoundsRequest request) {
        Object parent = getHost().getParent().getModel();
        if (!(parent instanceof Process)) {
            return null;
        }

        Note note = (Note) getHost().getModel();
        if (note.isReadOnly()) {
            return null;
        }

       TalendScalableFreeformRootEditPart rootEditPart= (TalendScalableFreeformRootEditPart) getHost().getRoot();
       double scale = 1/rootEditPart.getZoomManager().getZoom();
        return new ResizeNoteCommand(note, new Dimension(note.getSize().width + request.getSizeDelta().getScaled(scale).width,
                note.getSize().height + request.getSizeDelta().getScaled(scale).height));
    }

}
