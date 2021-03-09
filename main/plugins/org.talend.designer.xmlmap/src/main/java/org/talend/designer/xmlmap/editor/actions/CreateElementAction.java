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
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IWorkbenchPart;
import org.talend.designer.xmlmap.model.emf.xmlmap.Connection;
import org.talend.designer.xmlmap.model.emf.xmlmap.NodeType;
import org.talend.designer.xmlmap.model.emf.xmlmap.OutputTreeNode;
import org.talend.designer.xmlmap.model.emf.xmlmap.TreeNode;
import org.talend.designer.xmlmap.model.emf.xmlmap.XmlmapFactory;
import org.talend.designer.xmlmap.parts.TreeNodeEditPart;
import org.talend.designer.xmlmap.ui.tabs.MapperManager;
import org.talend.designer.xmlmap.util.XmlMapUtil;
import org.talend.metadata.managment.ui.wizard.metadata.xml.utils.StringUtil;

/**
 * wchen class global comment. Detailled comment
 */
public class CreateElementAction extends SelectionAction {

    private TreeNode parent;

    private boolean input;

    private MapperManager mapperManager;

    public static final String ID = "org.talend.designer.xmlmap.editor.actions.CreateElementAction";

    private GraphicalViewer graphicViewer;

    public CreateElementAction(IWorkbenchPart part, GraphicalViewer graphicViewer) {
        super(part);
        this.graphicViewer = graphicViewer;
        setId(ID);
        setText("Create Sub-Element");
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
            OutputTreeNode outputTreeNode = (OutputTreeNode) treeNode;
            EList<Connection> incomingConnections = parent.getIncomingConnections();
            if (!incomingConnections.isEmpty()) {
                needWarning = true;
            }
        }
        boolean canContinue = true;
        // Shell shell = this.part.getSite().getShell();
        if (needWarning) {
            canContinue = MessageDialog.openConfirm(null, "Warning",
                    "Do you want to disconnect the existing linker and then add an sub element for the selected element ?");
        }

        if (canContinue) {

            // IInputValidator validataor = new IInputValidator() {
            //
            // public String isValid(String newText) {
            // String xpath = XmlMapUtil.getXPath(parent.getXpath(), newText, NodeType.ELEMENT);
            // EList<TreeNode> children = parent.getChildren();
            // boolean exist = false;
            // for (TreeNode child : children) {
            // if (child.getXpath() != null && child.getXpath().equals(xpath)) {
            // exist = true;
            // break;
            // }
            // }
            //
            // if (exist) {
            // return "Element '" + newText + "' already exist !";
            // } else {
            // return null;
            // }
            // }
            //
            // };

            InputDialog dialog = new InputDialog(null, "Create New Element", "Input the new element's valid label", "", null);
            int open = -1;
            String label = "";
            while (!StringUtil.validateLabelForXML(label)) {
                open = dialog.open();
                if (open == InputDialog.OK) {
                    label = dialog.getValue().trim();
                }
                if (open == InputDialog.CANCEL) {
                    return;
                }
            }
            if (open == Window.OK) {
                XmlMapUtil.detachNodeConnections(parent, mapperManager.getExternalData(), false);
                treeNode.setName(label);
                treeNode.setNodeType(NodeType.ELEMENT);
                String parentXpath = parent.getXpath();
                if (parent.isChoice() || parent.isSubstitution()) {
                    TreeNode realPrant = XmlMapUtil.getRealParentNode(parent);
                    if (realPrant != null) {
                        parentXpath = realPrant.getXpath();
                    }
                }
                treeNode.setXpath(XmlMapUtil.getXPath(parentXpath, treeNode.getName(), treeNode.getNodeType()));
                treeNode.setType(XmlMapUtil.DEFAULT_DATA_TYPE);
                parent.getChildren().add(treeNode);
                parent.setExpression("");

                if (!input) {
                    OutputTreeNode output = (OutputTreeNode) parent;
                    if (!XmlMapUtil.isExpressionEditable(output) && output.isAggregate()) {
                        output.setAggregate(false);
                    }
                }

            }

            if (open == Window.OK && mapperManager != null) {
                // if (input) {
                // mapperManager.inputTreeSchemaBeanListModified();
                // } else {
                // mapperManager.outputTreeSchemaBeanListModified();
                // }
                Object object = graphicViewer.getEditPartRegistry().get(treeNode);
                if (object instanceof TreeNodeEditPart) {
                    graphicViewer.select((TreeNodeEditPart) object);
                }

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
