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
package org.talend.designer.core.ui.action;

import java.util.List;

import org.eclipse.gef.ui.actions.Clipboard;
import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.actions.ActionFactory;
import org.talend.core.model.components.IComponent;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.model.process.AbstractProcessProvider;
import org.talend.designer.core.ui.editor.AbstractTalendEditor;
import org.talend.designer.core.ui.editor.connections.ConnLabelEditPart;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.nodes.NodeLabelEditPart;
import org.talend.designer.core.ui.editor.nodes.NodePart;
import org.talend.designer.core.ui.editor.notes.NoteDirectEditManager;
import org.talend.designer.core.ui.editor.notes.NoteEditPart;
import org.talend.designer.core.ui.editor.subjobcontainer.SubjobContainerPart;

/**
 * Action that manage to create a connection from the context menu. A connection type is used to know which kind of
 * connection will be created. <br/>
 *
 * $Id$
 *
 */
public class GEFCopyAction extends SelectionAction {

    // public static final String ID = "org.talend.designer.core.ui.editor.action.NodesCopyAction"; //$NON-NLS-1$

    /**
     * Define the type of the connection and the workbench part who will manage the connection.
     *
     * @param part
     * @param connecType
     */
    public GEFCopyAction(IWorkbenchPart part) {
        super(part);
        setId(ActionFactory.COPY.getId());
        setText(Messages.getString("NodesCopyAction.label")); //$NON-NLS-1$
        ISharedImages sharedImages = part.getSite().getWorkbenchWindow().getWorkbench().getSharedImages();
        setImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_COPY));
        setDisabledImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_COPY_DISABLED));
    }

    @Override
    protected boolean calculateEnabled() {
        List objects = getSelectedObjects();
        if (!objects.isEmpty()) {
            for (Object o : objects) {
                if (o instanceof NoteEditPart) {
                    return true;
                }
                if (o instanceof NodeLabelEditPart) {
                    return true;
                }
                // fix for bug TDI-8325
                if (o instanceof ConnLabelEditPart) {
                    return true;
                }
                if (o instanceof SubjobContainerPart) {
                    return true;
                }
                if (!(o instanceof NodePart) && !(o instanceof NoteEditPart)) {
                    return false;
                }
                AbstractProcessProvider pProvider = AbstractProcessProvider.findProcessProviderFromPID(IComponent.JOBLET_PID);
                if (pProvider != null && !pProvider.canCopyNode((Node) ((NodePart) o).getModel())) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    public void run() {
        List objects = GEFDeleteAction.filterSameObject(getSelectedObjects());
        if (!objects.isEmpty()) {
            Clipboard clipboard = Clipboard.getDefault();

            org.eclipse.swt.dnd.Clipboard systemClipboard = new org.eclipse.swt.dnd.Clipboard(Display.getCurrent());

            boolean noteTextActived = false;

            boolean connectionTextActived = false;

            boolean nodeLabelActived = false;

            Text text = null;

            if (objects.size() == 1) {
                if (objects.get(0) instanceof NoteEditPart) {
                    NoteDirectEditManager directEditManager = ((NoteEditPart) objects.get(0)).getDirectEditManager();
                    if (directEditManager != null && directEditManager.getCellEditor() != null) {
                        noteTextActived = true;
                    }
                } else if (objects.get(0) instanceof ConnLabelEditPart) {
                    ConnLabelEditPart connLabelEditPart = (ConnLabelEditPart) objects.get(0);
                    if (connLabelEditPart.getDirectEditManager() != null
                            && connLabelEditPart.getDirectEditManager().getTextControl() != null) {
                        connectionTextActived = true;
                    }
                } else if (objects.get(0) instanceof NodeLabelEditPart) {
                    NodeLabelEditPart nodeLabelEditPart = (NodeLabelEditPart) objects.get(0);
                    if (nodeLabelEditPart.getDirectEditManager() != null
                            && nodeLabelEditPart.getDirectEditManager().getCellEditor() != null) {
                        nodeLabelActived = true;
                    }
                }
            }

            if (noteTextActived) {

                text = ((NoteEditPart) objects.get(0)).getDirectEditManager().getTextControl();
                clipboard.setContents(text.getSelectionText());

            } else if (connectionTextActived) {

                text = ((ConnLabelEditPart) objects.get(0)).getDirectEditManager().getTextControl();
                clipboard.setContents(text.getSelectionText());

            } else if (nodeLabelActived) {
                text = (Text) ((NodeLabelEditPart) objects.get(0)).getDirectEditManager().getCellEditor().getControl();
                clipboard.setContents(text.getSelectionText());
            } else {
                clipboard.setContents(objects);
            }

            if (text != null && !("").equals(text.getSelectionText())) {
                TextTransfer textTransfer = TextTransfer.getInstance();
                Transfer[] transfers = new Transfer[] { textTransfer };
                Object[] data = new Object[] { text.getSelectionText() };
                systemClipboard.setContents(data, transfers);
            }
        }

        // Refreshes the pasteAction's enable status.
        IWorkbenchPart part = getWorkbenchPart();
        if (part instanceof AbstractTalendEditor) {
            AbstractTalendEditor talendEditor = (AbstractTalendEditor) part;
            IAction action = talendEditor.getActionRegistry().getAction(ActionFactory.PASTE.getId());
            action.setEnabled(true);
        }
    }
}
