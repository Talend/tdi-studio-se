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

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.commands.CompoundCommand;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.PlatformUI;
import org.talend.designer.core.ui.AbstractMultiPageTalendEditor;
import org.talend.designer.core.ui.editor.nodecontainer.NodeContainerPart;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.nodes.NodePart;
import org.talend.designer.core.ui.editor.notes.Note;
import org.talend.designer.core.ui.editor.notes.NoteEditPart;
import org.talend.designer.core.ui.editor.process.Process;
import org.talend.designer.core.ui.editor.process.ProcessPart;
import org.talend.designer.core.ui.editor.subjobcontainer.SubjobContainerPart;

/**
 * DOC nrousseau class global comment. Detailled comment <br/>
 *
 */
public class MultiplePasteCommand extends CompoundCommand {

    private List<EditPart> oldSelection;

    private NodesPasteCommand nodeCmd;

    private NotesPasteCommand noteCmd;
    
    private boolean isCheckNodeExist = true;

    public MultiplePasteCommand(List<NodePart> nodeParts, List<NoteEditPart> noteParts, Process process, Point cursorLocation) {
        // List<NodePart> nodePartList = new ArrayList<NodePart>();
        // nodePartList.clear();
        // nodePartList.addAll(nodeParts);
        Point poit = new Point(((Node) nodeParts.get(0).getModel()).getLocation());
        nodeCmd = new NodesPasteCommand(nodeParts, process, cursorLocation);
        nodeCmd.setMultipleCommand(true);
        add(nodeCmd);
        noteCmd = new NotesPasteCommand(noteParts, process, cursorLocation, true, poit);
        noteCmd.setMultipleCommand(true);
        add(noteCmd);
    }

    public void setJobletRefactor(boolean isJobletRefactor) {
        nodeCmd.setJobletRefactor(isJobletRefactor);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.commands.CompoundCommand#execute()
     */
    @Override
    public void execute() {
        nodeCmd.setCheckNodeExist(isCheckNodeExist);
        
        AbstractMultiPageTalendEditor multiPageTalendEditor = (AbstractMultiPageTalendEditor) PlatformUI.getWorkbench()
                .getActiveWorkbenchWindow().getActivePage().getActiveEditor();
        GraphicalViewer viewer = multiPageTalendEditor.getTalendEditor().getViewer();
        oldSelection = new ArrayList<EditPart>();
        for (EditPart editPart : (List<EditPart>) viewer.getSelectedEditParts()) {
            oldSelection.add(editPart);
        }
        // remove the old selection
        viewer.deselectAll();

        super.execute();

        EditPart processPart = (EditPart) viewer.getRootEditPart().getChildren().get(0);
        if (processPart instanceof ProcessPart) { // can only be ProcessPart but still test
            List<EditPart> sel = new ArrayList<EditPart>();
            for (EditPart editPart : (List<EditPart>) processPart.getChildren()) {
                if (editPart instanceof NodePart) {
                    Node currentNode = (Node) editPart.getModel();
                    if (nodeCmd.getNodeContainerList().contains(currentNode.getNodeContainer()) || !isCheckNodeExist) {
                        sel.add(editPart);
                    }
                } else if (editPart instanceof NoteEditPart) {
                    Note currentNode = (Note) editPart.getModel();
                    if (noteCmd.getNoteList().contains(currentNode)) {
                        sel.add(editPart);
                    }
                } else if (editPart instanceof SubjobContainerPart) {// add for the bug TDI-7706
                    for (EditPart subjobChildsPart : (List<EditPart>) editPart.getChildren()) {
                        if (subjobChildsPart instanceof NodeContainerPart) {
                            if (nodeCmd.getNodeContainerList().contains(((NodeContainerPart) subjobChildsPart).getModel())) {
                                NodePart nodePart = ((NodeContainerPart) subjobChildsPart).getNodePart();
                                if (nodePart != null) {
                                    sel.add(nodePart);
                                }
                            }
                        }
                    }
                }
            }
            StructuredSelection s = new StructuredSelection(sel);
            viewer.setSelection(s);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.commands.CompoundCommand#undo()
     */
    @Override
    public void undo() {
        // remove the current selection
        AbstractMultiPageTalendEditor multiPageTalendEditor = (AbstractMultiPageTalendEditor) PlatformUI.getWorkbench()
                .getActiveWorkbenchWindow().getActivePage().getActiveEditor();
        GraphicalViewer viewer = multiPageTalendEditor.getTalendEditor().getViewer();
        viewer.deselectAll();

        super.undo();

        // set the old selection active
        StructuredSelection s = new StructuredSelection(oldSelection);
        viewer.setSelection(s);
    }

    /**
     * bqian Comment method "setSelectedSubjobs". <br>
     * see bug 0004882: Subjob title is not copied when copying/pasting subjobs from one job to another
     *
     * @param subjobParts
     */
    public void setSelectedSubjobs(List<SubjobContainerPart> subjobParts) {
        nodeCmd.setSelectedSubjobs(subjobParts);
    }

    /**
     * Sets the selectedExpandedJoblet.
     *
     * @param selectedExpandedJoblet the selectedExpandedJoblet to set
     */
    public void setSelectedExpandedJoblet(List<NodePart> selectedExpandedJoblet) {
        nodeCmd.setSelectedExpandedJoblet(selectedExpandedJoblet);
    }

    
    public boolean isCheckNodeExist() {
        return isCheckNodeExist;
    }

    
    public void setCheckNodeExist(boolean isCheckNodeExist) {
        this.isCheckNodeExist = isCheckNodeExist;
    }
}
