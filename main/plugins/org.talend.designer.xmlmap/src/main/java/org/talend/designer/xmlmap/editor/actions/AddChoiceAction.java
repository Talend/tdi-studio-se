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
package org.talend.designer.xmlmap.editor.actions;

import java.util.List;

import org.eclipse.emf.common.util.EList;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IWorkbenchPart;
import org.talend.datatools.xml.utils.XSDPopulationUtil2;
import org.talend.designer.xmlmap.model.emf.xmlmap.Connection;
import org.talend.designer.xmlmap.model.emf.xmlmap.NodeType;
import org.talend.designer.xmlmap.model.emf.xmlmap.OutputTreeNode;
import org.talend.designer.xmlmap.model.emf.xmlmap.TreeNode;
import org.talend.designer.xmlmap.model.emf.xmlmap.XmlmapFactory;
import org.talend.designer.xmlmap.parts.TreeNodeEditPart;
import org.talend.designer.xmlmap.ui.tabs.MapperManager;
import org.talend.designer.xmlmap.util.XmlMapUtil;

/**
 * wchen class global comment. Detailled comment
 */
public class AddChoiceAction extends SelectionAction {

    private TreeNode parent;

    private boolean input;

    private MapperManager mapperManager;

    public static final String ID = "org.talend.designer.xmlmap.editor.actions.AddChoiceAction";

    private GraphicalViewer graphicViewer;

    public AddChoiceAction(IWorkbenchPart part, GraphicalViewer graphicViewer) {
        super(part);
        this.graphicViewer = graphicViewer;
        setId(ID);
        setText("Add Choice");
    }

    @Override
    public void run() {
        TreeNode treeNode = null;
        boolean needWarning = false;
        if (input) {
            treeNode = XmlmapFactory.eINSTANCE.createTreeNode();
            if (!parent.getOutgoingConnections().isEmpty()) {
                needWarning = true;
            }
        } else {
            treeNode = XmlmapFactory.eINSTANCE.createOutputTreeNode();
            EList<Connection> incomingConnections = parent.getIncomingConnections();
            if (!incomingConnections.isEmpty()) {
                needWarning = true;
            }
        }
        boolean canContinue = true;
        // Shell shell = this.part.getSite().getShell();
        if (needWarning) {
            canContinue = MessageDialog.openConfirm(null, "Warning",
                    "Do you want to disconnect the existing linker and then add a choice for the selected element ?");
        }

        if (canContinue) {
            XmlMapUtil.detachNodeConnections(parent, mapperManager.getExternalData(), false);
            treeNode.setName(parent.getName() + XSDPopulationUtil2.CHOICE);
            treeNode.setNodeType(NodeType.ELEMENT);
            treeNode.setXpath(XmlMapUtil.getXPath(this.parent.getXpath(), treeNode.getName(), treeNode.getNodeType()));
            treeNode.setChoice(true);
            parent.getChildren().add(treeNode);

            if (!input) {
                OutputTreeNode output = (OutputTreeNode) parent;
                if (!XmlMapUtil.isExpressionEditable(output) && output.isAggregate()) {
                    output.setAggregate(false);
                }
            }

            Object object = graphicViewer.getEditPartRegistry().get(treeNode);
            if (object instanceof TreeNodeEditPart) {
                graphicViewer.select((TreeNodeEditPart) object);
            }

        }

    }

    @Override
    protected boolean calculateEnabled() {
        if (getSelectedObjects().isEmpty()) {
            return false;
        } else {
            // get the last selection to run the action
            Object s = getSelectedObjects().get(0);
            if (s instanceof List && !((List) s).isEmpty()) {
                List selectedarts = (List) s;
                Object object = selectedarts.get(selectedarts.size() - 1);
                if (object instanceof TreeNodeEditPart) {
                    TreeNodeEditPart nodePart = (TreeNodeEditPart) object;
                    this.parent = (TreeNode) nodePart.getModel();

                    if (parent.isChoice() || parent.isSubstitution()) {
                        return false;
                    }

                    // can't create two or more choice under a node
                    EList<TreeNode> children = parent.getChildren();
                    for (int i = 0; i < children.size(); i++) {
                        if (children.get(i).isChoice()) {
                            return false;
                        }
                    }

                    boolean isElement = NodeType.ELEMENT.equals(parent.getNodeType());
                    if (isElement && XmlMapUtil.getXPathLength(parent.getXpath()) > 2) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public void update(Object selection) {
        setSelection(new StructuredSelection(selection));
    }

    public boolean isInput() {
        return input;
    }

    public void setInput(boolean input) {
        this.input = input;
    }

    public void setMapperManager(MapperManager mapperManager) {
        this.mapperManager = mapperManager;
    }
}
