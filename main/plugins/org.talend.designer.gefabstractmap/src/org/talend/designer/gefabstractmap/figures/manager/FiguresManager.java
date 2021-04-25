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
package org.talend.designer.gefabstractmap.figures.manager;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.talend.designer.gefabstractmap.editor.MapperGraphicalViewer;
import org.talend.designer.gefabstractmap.figures.treetools.zone.InputZoneToolBar;
import org.talend.designer.gefabstractmap.figures.treetools.zone.OutputZoneToolBar;
import org.talend.designer.gefabstractmap.model.abstractmap.MapperTable;
import org.talend.designer.gefabstractmap.part.InputTablePart;
import org.talend.designer.gefabstractmap.part.MapperRootEditPart;
import org.talend.designer.gefabstractmap.part.MapperTablePart;
import org.talend.designer.gefabstractmap.part.OutputTablePart;
import org.talend.designer.gefabstractmap.part.TableEntityPart;
import org.talend.designer.gefabstractmap.utils.MapperUtils;

/**
 * created by wchen on 2013-1-14 Detailled comment
 *
 */
public abstract class FiguresManager implements ISelectionChangedListener {

    private MapperGraphicalViewer graphicViewer;

    private MapperRootEditPart mapDataEditPart;

    private InputTablePart currentSelectedInputTable;

    private OutputTablePart currentSelectedOutputTable;

    public FiguresManager(MapperGraphicalViewer graphicViewer) {
        this.graphicViewer = graphicViewer;

    }

    public MapperRootEditPart getRootEditPart() {
        return mapDataEditPart;
    }

    @Override
    public void selectionChanged(SelectionChangedEvent event) {
        mapDataEditPart = (MapperRootEditPart) graphicViewer.getContents();
        if (getRootEditPart() == null) {
            return;
        }
        List selectedEditParts = graphicViewer.getSelectedEditParts();
        if (selectedEditParts.isEmpty()) {
            return;
        }
        boolean outputRemoveEnable = false;
        boolean outputMoveUp = false;
        boolean outputMoveDown = false;
        boolean inputMoveUp = false;
        boolean inputMoveDown = false;
        List<MapperTable> outputTrees = getOutputTables();
        List<MapperTable> inputTrees = getInputTables();

        OutputZoneToolBar outputToolBar = mapDataEditPart.getOutputZoneToolBar();
        InputZoneToolBar inputToolBar = mapDataEditPart.getInputZoneToolBar();

        boolean selectNode = false;

        Object lastSelection = selectedEditParts.get(selectedEditParts.size() - 1);
        if (lastSelection instanceof TableEntityPart) {
            selectNode = true;
            MapperTablePart findTreePart = MapperUtils.getMapperTablePart((TableEntityPart) lastSelection);
            if (findTreePart instanceof InputTablePart) {
                currentSelectedInputTable = (InputTablePart) findTreePart;
            } else if (findTreePart instanceof OutputTablePart) {
                currentSelectedOutputTable = (OutputTablePart) findTreePart;
            }

            // only change connection figures for nodes in the same tree as the lastSelection
            List<TableEntityPart> nodes = new ArrayList<TableEntityPart>();
            for (int i = 0; i < selectedEditParts.size() - 1; i++) {
                Object obj = selectedEditParts.get(i);
                if (obj instanceof TableEntityPart) {
                    MapperTablePart findOtherTreePart = MapperUtils.getMapperTablePart((TableEntityPart) obj);
                    if (findOtherTreePart == findTreePart) {
                        nodes.add((TableEntityPart) obj);
                    }
                }
            }
            nodes.add((TableEntityPart) lastSelection);
            onTreeSelected(nodes, findTreePart);
            // change color for other unselected node in the tree;

        } else if (lastSelection instanceof OutputTablePart) {
            currentSelectedOutputTable = (OutputTablePart) lastSelection;
            onTreeSelected(currentSelectedOutputTable.getChildren(), currentSelectedOutputTable);
        } else if (lastSelection instanceof InputTablePart) {
            currentSelectedInputTable = (InputTablePart) lastSelection;
            onTreeSelected(currentSelectedInputTable.getChildren(), currentSelectedInputTable);

        }

        if (currentSelectedOutputTable != null) {
            MapperTable model = (MapperTable) currentSelectedOutputTable.getModel();
            if (!isErrorRejectTable(model)) {
                outputRemoveEnable = true;
            }
            int index = outputTrees.indexOf(model);
            if (index != -1 && index != 0) {
                if (index != 1 || !isErrorRejectTable(outputTrees.get(0))) {
                    outputMoveUp = true;
                }
            }
            if (index != -1 && index != outputTrees.size() - 1) {
                if (!isErrorRejectTable(model)) {
                    outputMoveDown = true;
                }
            }
        }
        outputToolBar.setRemoveButtonEnable(outputRemoveEnable);
        outputToolBar.setMoveUpEnable(outputMoveUp);
        outputToolBar.setMoveDownEnable(outputMoveDown);

        if (currentSelectedInputTable != null) {
            MapperTable tree = (MapperTable) currentSelectedInputTable.getModel();
            if (!isLookup(tree)) {
                inputMoveUp = false;
                inputMoveDown = false;
            } else {
                int indexOf = inputTrees.indexOf(tree);
                if (indexOf != -1 && indexOf > 1) {
                    inputMoveUp = true;
                }
                if (indexOf > 0 && indexOf != inputTrees.size() - 1) {
                    inputMoveDown = true;
                }
            }
        }
        inputToolBar.setMoveDownEnable(inputMoveDown);
        inputToolBar.setMoveUpEnable(inputMoveUp);

    }

    protected abstract List<MapperTable> getInputTables();

    protected abstract List<MapperTable> getOutputTables();

    protected abstract boolean isErrorRejectTable(MapperTable table);

    protected abstract boolean isLookup(MapperTable table);

    protected void onTreeSelected(List nodes, MapperTablePart selectedTreePart) {
        // selectedTreePart.setTreeOrChildSelected(true);
        /*
         * clean colors for other trees in the same zone must be executed before highlight the selected tree ,or the
         * color for LookupConnection won't be changed
         */
        List children = mapDataEditPart.getChildren();
        for (Object obj : children) {

            MapperTablePart abstractPart = null;
            if (selectedTreePart instanceof InputTablePart && obj instanceof InputTablePart) {
                abstractPart = (InputTablePart) obj;
            } else if (selectedTreePart instanceof OutputTablePart && obj instanceof OutputTablePart) {
                abstractPart = (OutputTablePart) obj;
            }
            if (abstractPart != null) {
                if (abstractPart != selectedTreePart) {
                    abstractPart.highLightHeader(false);
                }
                abstractPart.updateChildrenConnections(abstractPart.getChildren(), false);
            }

        }

        selectedTreePart.highLightHeader(true);
        selectedTreePart.updateChildrenConnections(nodes, true);

    }

    public InputTablePart getCurrentSelectedInputTable() {
        return this.currentSelectedInputTable;
    }

    public OutputTablePart getCurrentSelectedOutputTable() {
        return this.currentSelectedOutputTable;
    }

}
