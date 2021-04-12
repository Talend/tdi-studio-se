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
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IWorkbenchPart;
import org.talend.designer.xmlmap.model.emf.xmlmap.NodeType;
import org.talend.designer.xmlmap.model.emf.xmlmap.TreeNode;
import org.talend.designer.xmlmap.model.emf.xmlmap.XmlmapFactory;
import org.talend.designer.xmlmap.parts.TreeNodeEditPart;
import org.talend.designer.xmlmap.ui.dialog.NameSpaceDialog;
import org.talend.designer.xmlmap.ui.tabs.MapperManager;
import org.talend.designer.xmlmap.util.XmlMapUtil;

/**
 * DOC talend class global comment. Detailled comment
 */
public class CreateNameSpaceAction extends SelectionAction {

    private TreeNode parent;

    private MapperManager mapperManager;

    private boolean input;

    public static final String ID = "org.talend.designer.xmlmap.editor.actions.CreateNameSapceInput";

    private GraphicalViewer graphicViewer;

    public CreateNameSpaceAction(IWorkbenchPart part, GraphicalViewer graphicViewer) {
        super(part);
        this.graphicViewer = graphicViewer;
        setText("Set A Namespace");
        setId(ID);
    }

    @Override
    public void run() {
        String prefix = null;
        String defaultValue = null;

        NameSpaceDialog nsDialog = new NameSpaceDialog(null);
        nsDialog.setParentNode(parent);
        int status = nsDialog.open();
        if (status == nsDialog.OK) {
            defaultValue = nsDialog.getNSValue();
            if (defaultValue != null) {
                defaultValue = defaultValue.trim();
            }
            prefix = nsDialog.getPrefix().trim();
        }
        if (status == nsDialog.CANCEL) {
            return;
        }

        TreeNode createdNode = null;
        if (input) {
            createdNode = XmlmapFactory.eINSTANCE.createTreeNode();
        } else {
            createdNode = XmlmapFactory.eINSTANCE.createOutputTreeNode();
        }
        createdNode.setName(prefix);
        createdNode.setNodeType(NodeType.NAME_SPACE);
        createdNode.setDefaultValue(defaultValue);
        createdNode.setType(XmlMapUtil.DEFAULT_DATA_TYPE);
        String label = createdNode.getName();
        if (prefix == null || "".equals(prefix)) {
            label = XmlMapUtil.DEFAULT_NAME_SPACE_PREFIX;
            createdNode.setName(XmlMapUtil.DEFAULT_NAME_SPACE_PREFIX);
        }

        String parentXpath = parent.getXpath();
        if (parent.isChoice() || parent.isSubstitution()) {
            TreeNode realPrant = XmlMapUtil.getRealParentNode(parent);
            if (realPrant != null) {
                parentXpath = realPrant.getXpath();
            }
        }
        createdNode.setXpath(XmlMapUtil.getXPath(parentXpath, label, NodeType.NAME_SPACE));

        final EList<TreeNode> children = parent.getChildren();
        int index = 0;
        for (int i = 0; i < children.size(); i++) {
            final TreeNode treeNode = children.get(i);
            if (treeNode.getNodeType() == NodeType.NAME_SPACE) {
                if (i == children.size() - 1) {
                    index = children.size();
                }
                continue;
            } else {
                index = i;
                break;
            }
        }
        children.add(index, createdNode);

        // children.add(createdNode);

        // if (input) {
        // mapperManager.inputTreeSchemaBeanListModified();
        // } else {
        // mapperManager.outputTreeSchemaBeanListModified();
        // }
        Object object = graphicViewer.getEditPartRegistry().get(createdNode);
        if (object instanceof TreeNodeEditPart) {
            graphicViewer.select((TreeNodeEditPart) object);
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
                    if (isElement && !XmlMapUtil.DOCUMENT.equals(parent.getType()) && parent.eContainer() instanceof TreeNode) {
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

    public void setMapperManager(MapperManager mapperManager) {
        this.mapperManager = mapperManager;
    }

    public boolean isInput() {
        return input;
    }

    public void setInput(boolean input) {
        this.input = input;
    }

}
