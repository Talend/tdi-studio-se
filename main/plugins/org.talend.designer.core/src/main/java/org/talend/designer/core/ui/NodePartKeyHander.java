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
package org.talend.designer.core.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.ui.parts.GraphicalViewerKeyHandler;
import org.eclipse.swt.events.KeyEvent;
import org.talend.designer.core.ui.editor.nodes.NodePart;
import org.talend.designer.core.ui.editor.notes.NoteEditPart;
import org.talend.designer.core.ui.editor.process.ProcessPart;
import org.talend.designer.core.ui.editor.subjobcontainer.SubjobContainer;
import org.talend.designer.core.ui.editor.subjobcontainer.SubjobContainerPart;

/**
 * DOC Administrator class global comment. Detailled comment
 */
public class NodePartKeyHander extends GraphicalViewerKeyHandler {

    /**
     * DOC Administrator NodePartKeyHander constructor comment.
     *
     * @param viewer
     */
    public NodePartKeyHander(GraphicalViewer viewer) {
        super(viewer);
        // TODO Auto-generated constructor stub
    }

    @Override
    protected void navigateTo(EditPart part, KeyEvent event) {
        // PTODO need be removed
        SubjobContainerPart subPart = null;
        NoteEditPart noPart = null;
        boolean displayVa = true;
        if (part instanceof SubjobContainerPart) {
            // NodeContainerPart
            SubjobContainerPart focusPart = (SubjobContainerPart) part;
            List subList = focusPart.getParent().getChildren();
            for (int j = 0; j < subList.size(); j++) {

                if (subList.get(j) instanceof SubjobContainerPart) {
                    subPart = (SubjobContainerPart) subList.get(j);
                    SubjobContainer subContainer = (SubjobContainer) subPart.getModel();
                    if (subContainer.isDisplayed() == false) {
                        displayVa = false;
                    }
                } else if (subList.get(j) instanceof NoteEditPart) {
                    displayVa = false;
                }

            }
            if (displayVa == false) {
                part = (EditPart) part.getChildren().get(0);
                if (part != null) {
                    // NodePart
                    part = (EditPart) part.getChildren().get(0);
                }
                if (part == null) {
                    return;
                }
            }

        }

        super.navigateTo(part, event);
    }

    @Override
    protected List getNavigationSiblings() {
        EditPart focusPart = getFocusEditPart();
        boolean displayVa = true;
        if (focusPart.getParent() != null) {

            if (focusPart instanceof SubjobContainerPart) {
                // return getNodePart((SubjobContainerPart) focusPart);
                SubjobContainerPart subConPart = (SubjobContainerPart) focusPart;
                List subList = focusPart.getParent().getChildren();
                for (int j = 0; j < subList.size(); j++) {
                    if (subList.get(j) instanceof SubjobContainerPart) {
                        subConPart = (SubjobContainerPart) subList.get(j);
                        SubjobContainer subContainer = (SubjobContainer) subConPart.getModel();
                        if (subContainer.isDisplayed() == false) {
                            displayVa = false;
                        }
                    } else if (subList.get(j) instanceof NoteEditPart) {
                        NoteEditPart notePart = (NoteEditPart) subList.get(j);
                        return getNodePart((ProcessPart) notePart.getParent());
                    } else if (subList.get(j) instanceof NodePart) {
                        NodePart node = (NodePart) subList.get(j);
                        return getNodePart((ProcessPart) node.getParent().getParent().getParent());
                    }

                }
                if (displayVa == false) {
                    return getNodePart((ProcessPart) focusPart.getParent());
                }

            } else if (focusPart instanceof NodePart) {
                // get all node part for a job.
                return getNodePart((ProcessPart) focusPart.getParent().getParent().getParent());

                // return getNodePart((SubjobContainerPart) focusPart.getParent().getParent());

            } else if (focusPart instanceof NoteEditPart) {
                return getNodePart((ProcessPart) focusPart.getParent());
            }

            return focusPart.getParent().getChildren();
        }
        List list = new ArrayList();
        list.add(focusPart);
        return list;
    }

    private List<EditPart> getNodePart(ProcessPart part) {
        List<EditPart> nodePartList = new ArrayList<EditPart>();
        for (EditPart child : (List<EditPart>) part.getChildren()) {
            if (child instanceof NoteEditPart) {
                nodePartList.add(child);
            }
            for (EditPart c : (List<EditPart>) child.getChildren()) {
                for (EditPart n : (List<EditPart>) c.getChildren()) {
                    if (n instanceof NodePart) {
                        nodePartList.add(n);
                    }
                }
            }
        }
        return nodePartList;
    }

}
