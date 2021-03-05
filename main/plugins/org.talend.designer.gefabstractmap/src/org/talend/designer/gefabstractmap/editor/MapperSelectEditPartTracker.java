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
package org.talend.designer.gefabstractmap.editor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.requests.DirectEditRequest;
import org.eclipse.gef.tools.SelectEditPartTracker;
import org.eclipse.swt.SWT;
import org.talend.designer.gefabstractmap.part.MapperTablePart;
import org.talend.designer.gefabstractmap.part.TableEntityPart;
import org.talend.designer.gefabstractmap.utils.MapperUtils;

/**
 * DOC talend class global comment. Detailled comment
 */
public class MapperSelectEditPartTracker extends SelectEditPartTracker {

    static EditPart lastSelectedShiftPart = null;

    public MapperSelectEditPartTracker(EditPart owner) {
        super(owner);
    }

    @Override
    protected void performConditionalSelection() {
        super.performConditionalSelection();
    }

    @Override
    protected boolean handleButtonUp(int button) {
        if (isInState(STATE_DRAG) || button == 3) {
            performSelection();
            if (hasSelectionOccurred()) {
                performDirectEdit();
            }
            if (button == 1 && getSourceEditPart().getSelected() != EditPart.SELECTED_NONE) {
                getCurrentViewer().reveal(getSourceEditPart());
            }
            setState(STATE_TERMINAL);
            return true;
        }
        return false;
    }

    @Override
    protected void performDirectEdit() {
        DirectEditRequest req = new DirectEditRequest();
        req.setLocation(getCurrentInput().getMouseLocation());
        new DelayedDirectEditHelper(getSourceEditPart().getViewer(), req, getSourceEditPart());
    }

    @Override
    protected void performSelection() {
        if (hasSelectionOccurred()) {
            return;
        }
        setFlag(FLAG_SELECTION_PERFORMED, true);
        EditPartViewer viewer = getCurrentViewer();
        List selectedObjects = viewer.getSelectedEditParts();

        if (getCurrentInput().isModKeyDown(SWT.MOD1)) {
            lastSelectedShiftPart = null;
            if (selectedObjects.contains(getSourceEditPart())) {
                viewer.deselect(getSourceEditPart());
            } else if (canAppend()) {
                viewer.appendSelection(getSourceEditPart());
            } else {
                viewer.select(getSourceEditPart());
            }
        } else if (getCurrentInput().isShiftKeyDown()) {
            if (lastSelectedShiftPart == null) {
                if (!selectedObjects.isEmpty()) {
                    Object object = selectedObjects.get(selectedObjects.size() - 1);
                    if (object instanceof TableEntityPart) {
                        lastSelectedShiftPart = (EditPart) object;
                    }
                } else {
                    lastSelectedShiftPart = getSourceEditPart();
                }
            }
            if (canAppend()) {
                List<EditPart> toselect = new ArrayList<EditPart>();
                List<EditPart> deSelect = new ArrayList<EditPart>();
                getShiftAffactedEditPart(toselect, deSelect);
                for (EditPart part : deSelect) {
                    viewer.deselect(part);
                }
                for (EditPart part : toselect) {
                    if (!viewer.getSelectedEditParts().contains(part)) {
                        viewer.appendSelection(part);
                    }
                }
            } else {
                viewer.select(getSourceEditPart());
            }
        } else {
            lastSelectedShiftPart = null;
            viewer.select(getSourceEditPart());
        }
    }

    private boolean canAppend() {
        if (getSourceEditPart() instanceof TableEntityPart) {
            EditPart lastPart = lastSelectedShiftPart;
            List selectedEditParts = getCurrentViewer().getSelectedEditParts();
            if (lastPart == null && !selectedEditParts.isEmpty()) {
                lastPart = (EditPart) selectedEditParts.get(selectedEditParts.size() - 1);
            }
            if (lastPart == null) {
                return true;
            } else if (lastPart instanceof TableEntityPart) {
                MapperTablePart lastContaier = MapperUtils.getMapperTablePart((TableEntityPart) lastPart);
                MapperTablePart toAppendContaier = MapperUtils.getMapperTablePart((TableEntityPart) getSourceEditPart());
                if (lastContaier == toAppendContaier) {
                    return true;
                }
            }
        }
        lastSelectedShiftPart = null;
        return false;
    }

    private void getShiftAffactedEditPart(List toSelect, List deSelect) {
        if (lastSelectedShiftPart == null || lastSelectedShiftPart == getSourceEditPart()) {
            toSelect.add(getSourceEditPart());
            return;
        }

        List<TableEntityPart> partList = new ArrayList<TableEntityPart>();
        if (lastSelectedShiftPart instanceof TableEntityPart) {
            MapperTablePart treePart = MapperUtils.getMapperTablePart((TableEntityPart) lastSelectedShiftPart);
            if (treePart != null) {
                partList = MapperUtils.getFlatChildrenPartList(treePart);
            }
        }

        int index = partList.indexOf(lastSelectedShiftPart);
        int index2 = partList.indexOf(getSourceEditPart());
        if (index2 != -1) {
            for (int i = Math.min(index, index2); i < Math.max(index, index2) + 1; i++) {
                if (!toSelect.contains(partList.get(i))) {
                    toSelect.add(partList.get(i));
                }
            }

            List selectedEditParts = getCurrentViewer().getSelectedEditParts();
            for (int i = 0; i < selectedEditParts.size(); i++) {
                if (partList.contains(selectedEditParts.get(i)) && !toSelect.contains(selectedEditParts.get(i))) {
                    deSelect.add(selectedEditParts.get(i));
                }
            }
        }

    }

}
