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
package org.talend.designer.xmlmap.figures.treetools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.emf.common.util.EList;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CommandStack;
import org.talend.designer.gefabstractmap.figures.var.VarToolBarContainer;
import org.talend.designer.gefabstractmap.part.VarTablePart;
import org.talend.designer.xmlmap.figures.table.VarTableManager;
import org.talend.designer.xmlmap.model.emf.xmlmap.VarNode;
import org.talend.designer.xmlmap.model.emf.xmlmap.VarTable;
import org.talend.designer.xmlmap.model.emf.xmlmap.XmlMapData;
import org.talend.designer.xmlmap.model.emf.xmlmap.XmlmapFactory;
import org.talend.designer.xmlmap.parts.VarNodeEditPart;
import org.talend.designer.xmlmap.util.XmlMapUtil;

/**
 * DOC hywang class global comment. Detailled comment
 */
public class VarToolBarFigure extends VarToolBarContainer {

    VarTable parentTable;

    VarTablePart tablePart;

    public VarToolBarFigure(VarTableManager tableManager) {
        super(tableManager);
        parentTable = tableManager.getModel();
        tablePart = tableManager.getEditPart();
        createToolbar();
    }

    @Override
    protected void addVar() {
        CommandStack commandStack = getTableManager().getGraphicalViewer().getEditDomain().getCommandStack();
        commandStack.execute(new Command() {

            @Override
            public void execute() {
                VarNode newNode = XmlmapFactory.eINSTANCE.createVarNode();
                newNode.setType(XmlMapUtil.DEFAULT_DATA_TYPE);
                newNode.setName(XmlMapUtil.findUniqueVarColumnName("Var", parentTable));
                parentTable.getNodes().add(newNode);
                parentTable.setMinimized(false);

                EditPart toSelect = null;

                int index = parentTable.getNodes().indexOf(newNode);
                if (index < tablePart.getChildren().size()) {
                    toSelect = (EditPart) tablePart.getChildren().get(index);
                    tablePart.getViewer().select(toSelect);
                }

                if (!remove.isEnabled()) {
                    remove.setEnabled(true);
                }

            }
        });

    }

    @Override
    protected void removeVar() {
        CommandStack commandStack = getTableManager().getGraphicalViewer().getEditDomain().getCommandStack();
        commandStack.execute(new Command() {

            @Override
            public void execute() {
                List selectedEditParts = getTableManager().getGraphicalViewer().getSelectedEditParts();
                final List<VarNode> toRemove = new ArrayList<VarNode>();

                int minIndex = parentTable.getNodes().size() - 1;
                for (Object obj : selectedEditParts) {
                    if (obj instanceof VarNodeEditPart) {
                        VarNode model = (VarNode) ((VarNodeEditPart) obj).getModel();
                        toRemove.add(model);
                        XmlMapUtil.detachNodeConnections(model, (XmlMapData) parentTable.eContainer(), true);
                        int index = parentTable.getNodes().indexOf(model);
                        if (index < minIndex) {
                            minIndex = index;
                        }
                    }
                }
                parentTable.getNodes().removeAll(toRemove);

                if (!tablePart.getChildren().isEmpty()) {
                    if (minIndex > tablePart.getChildren().size() - 1) {
                        minIndex = tablePart.getChildren().size() - 1;
                    }
                    tablePart.getViewer().select((EditPart) tablePart.getChildren().get(minIndex));
                } else {
                    remove.setEnabled(false);
                }

            }
        });
    }

    @Override
    protected void moveUp() {
        CommandStack commandStack = getTableManager().getGraphicalViewer().getEditDomain().getCommandStack();
        commandStack.execute(new Command() {

            @Override
            public void execute() {
                List selectedEditParts = getTableManager().getGraphicalViewer().getSelectedEditParts();
                List<Integer> indexToMove = new ArrayList<Integer>();
                EList<VarNode> nodes = parentTable.getNodes();
                for (int i = 0; i < selectedEditParts.size(); i++) {
                    Object obj = selectedEditParts.get(i);
                    if (obj instanceof VarNodeEditPart) {
                        VarNode node = (VarNode) ((VarNodeEditPart) obj).getModel();
                        int indexOf = nodes.indexOf(node);
                        if (indexOf != -1 && indexOf > 0) {
                            indexToMove.add(indexOf);
                        }
                    }
                }

                Collections.sort(indexToMove);

                for (int i = 0; i < indexToMove.size(); i++) {
                    int index = indexToMove.get(i);
                    VarNode temp = nodes.get(index);
                    nodes.remove(temp);
                    nodes.add(index - 1, temp);
                }

                for (int i = 0; i < indexToMove.size(); i++) {
                    EditPart part = (EditPart) tablePart.getChildren().get(indexToMove.get(i) - 1);
                    tablePart.getViewer().appendSelection(part);
                }

            }
        });
    }

    @Override
    protected void moveDown() {
        CommandStack commandStack = getTableManager().getGraphicalViewer().getEditDomain().getCommandStack();
        commandStack.execute(new Command() {

            @Override
            public void execute() {
                List selectedEditParts = getTableManager().getGraphicalViewer().getSelectedEditParts();
                List<Integer> indexToMove = new ArrayList<Integer>();
                EList<VarNode> nodes = parentTable.getNodes();
                for (int i = 0; i < selectedEditParts.size(); i++) {
                    Object obj = selectedEditParts.get(i);
                    if (obj instanceof VarNodeEditPart) {
                        VarNode node = (VarNode) ((VarNodeEditPart) obj).getModel();
                        int indexOf = nodes.indexOf(node);
                        if (indexOf != -1 && indexOf < nodes.size() - 1) {
                            indexToMove.add(indexOf);
                        }
                    }
                }
                Collections.sort(indexToMove);
                Collections.reverse(indexToMove);

                for (int i = 0; i < indexToMove.size(); i++) {
                    int index = indexToMove.get(i);
                    VarNode temp = nodes.get(index);
                    nodes.remove(temp);
                    nodes.add(index + 1, temp);
                }

                for (int i = 0; i < indexToMove.size(); i++) {
                    EditPart part = (EditPart) tablePart.getChildren().get(indexToMove.get(i) + 1);
                    tablePart.getViewer().appendSelection(part);
                }
            }
        });
    }

}
