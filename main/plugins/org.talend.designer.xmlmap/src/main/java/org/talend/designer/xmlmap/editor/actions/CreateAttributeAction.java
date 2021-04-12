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
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IWorkbenchPart;
import org.talend.designer.xmlmap.model.emf.xmlmap.NodeType;
import org.talend.designer.xmlmap.model.emf.xmlmap.TreeNode;
import org.talend.designer.xmlmap.model.emf.xmlmap.XmlmapFactory;
import org.talend.designer.xmlmap.parts.TreeNodeEditPart;
import org.talend.designer.xmlmap.ui.tabs.MapperManager;
import org.talend.designer.xmlmap.util.XmlMapUtil;
import org.talend.metadata.managment.ui.wizard.metadata.xml.utils.StringUtil;

/**
 * wchen class global comment. Detailled comment
 */
public class CreateAttributeAction extends SelectionAction {

    private TreeNode parent;

    private boolean input;

    private MapperManager mapperManager;

    public static final String ID = "org.talend.designer.xmlmap.editor.actions.CreateAttributeAction";

    private GraphicalViewer graphicViewer;

    public CreateAttributeAction(IWorkbenchPart part, GraphicalViewer graphicViewer) {
        super(part);
        this.graphicViewer = graphicViewer;
        setId(ID);
        setText("Create Attribute");
    }

    @Override
    public void run() {
        TreeNode treeNode = null;
        if (input) {
            treeNode = XmlmapFactory.eINSTANCE.createTreeNode();
        } else {
            treeNode = XmlmapFactory.eINSTANCE.createOutputTreeNode();
        }

        // IInputValidator validataor = new IInputValidator() {
        //
        // public String isValid(String newText) {
        // String xpath = XmlMapUtil.getXPath(parent.getXpath(), newText, NodeType.ATTRIBUT);
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
        // return "Atribute '" + newText + "' already exist !";
        // } else {
        // return null;
        // }
        // }
        //
        // };
        String label = "";
        InputDialog dialog = new InputDialog(null, "Create New Attribute", "Input the new attribute's valid label", "", null);
        int open = -1;
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
            treeNode.setName(label);
            treeNode.setNodeType(NodeType.ATTRIBUT);
            String parentXpath = parent.getXpath();
            if (parent.isChoice() || parent.isSubstitution()) {
                TreeNode realPrant = XmlMapUtil.getRealParentNode(parent);
                if (realPrant != null) {
                    parentXpath = realPrant.getXpath();
                }
            }
            treeNode.setXpath(XmlMapUtil.getXPath(parentXpath, treeNode.getName(), treeNode.getNodeType()));
            treeNode.setType(XmlMapUtil.DEFAULT_DATA_TYPE);
            final EList<TreeNode> children = parent.getChildren();
            int index = 0;
            for (int i = 0; i < children.size(); i++) {
                final TreeNode child = children.get(i);
                if (child.getNodeType() == NodeType.NAME_SPACE || child.getNodeType() == NodeType.ATTRIBUT) {
                    if (i == children.size() - 1) {
                        index = children.size();
                    }
                    continue;
                } else {
                    index = i;
                    break;
                }
            }
            children.add(index, treeNode);
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
