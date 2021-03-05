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
import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IWorkbenchPart;
import org.talend.datatools.xml.utils.XSDPopulationUtil2;
import org.talend.designer.xmlmap.model.emf.xmlmap.NodeType;
import org.talend.designer.xmlmap.model.emf.xmlmap.OutputTreeNode;
import org.talend.designer.xmlmap.model.emf.xmlmap.TreeNode;
import org.talend.designer.xmlmap.model.emf.xmlmap.XmlMapData;
import org.talend.designer.xmlmap.parts.TreeNodeEditPart;
import org.talend.designer.xmlmap.ui.tabs.MapperManager;
import org.talend.designer.xmlmap.ui.tabs.TabFolderEditors;
import org.talend.designer.xmlmap.util.XmlMapUtil;

/**
 * DOC hcyi class global comment. Detailled comment
 */
public class RenameTreeNodeAction extends SelectionAction {

    private TreeNode selectedNode;

    private MapperManager mapperManager;

    public static final String ID = "org.talend.designer.xmlmap.editor.actions.RenameTreeNodeAction";

    public RenameTreeNodeAction(IWorkbenchPart part) {
        super(part);
        setId(ID);
        setText("Rename");
    }

    @Override
    public void run() {
        if (selectedNode != null) {
            // validataor
            IInputValidator validataor = new IInputValidator() {

                public String isValid(String newText) {
                    String xpath = XmlMapUtil.getXPath(((TreeNode) selectedNode.eContainer()).getXpath(), newText,
                            selectedNode.getNodeType());
                    EList<TreeNode> children = ((TreeNode) selectedNode.eContainer()).getChildren();
                    boolean exist = false;
                    for (TreeNode child : children) {
                        if (child.getNodeType().equals(selectedNode.getNodeType())) {
                            if (child.getXpath() != null && child.getXpath().equals(xpath) && !child.equals(selectedNode)) {
                                exist = true;
                                break;
                            }
                        }
                    }
                    if (exist) {
                        if (selectedNode.getNodeType().equals(NodeType.ATTRIBUT)) {
                            return "Atribute '" + newText + "' already exist !";
                        } else if (selectedNode.getNodeType().equals(NodeType.ELEMENT)) {
                            return "Element '" + newText + "' already exist !";
                        }
                    }
                    return null;
                }
            };
            String name = selectedNode.getName();
            if (selectedNode.isSubstitution()) {
                name = name.substring(0, name.lastIndexOf(XSDPopulationUtil2.SUBS));
            }
            InputDialog dialog = new InputDialog(null, "Rename Tree Node", "", name, validataor);
            if (dialog.open() == Window.OK) {
                if (selectedNode.isSubstitution()) {
                    selectedNode.setName(dialog.getValue() + XSDPopulationUtil2.SUBS);
                } else {
                    selectedNode.setName(dialog.getValue());
                }
                // refresh
                if (selectedNode.isSubstitution()) {
                    TreeNode realParent = XmlMapUtil.getRealParentNode(selectedNode);
                    if (realParent != null) {
                        selectedNode.setXpath(XmlMapUtil.getXPath(realParent.getXpath(), selectedNode.getName(),
                                selectedNode.getNodeType()));
                    }
                } else {
                    XmlMapData externalEmfData = (XmlMapData) mapperManager.getExternalData();
                    XmlMapUtil.updateXPathAndExpression(externalEmfData, mapperManager.getMapperComponent()
                            .getExpressionManager(), selectedNode, dialog.getValue(), XmlMapUtil.getXPathLength(selectedNode
                            .getXpath()), true);

                }
                TabFolderEditors tabFolderEditors = mapperManager.getMapperUI().getTabFolderEditors();
                if (tabFolderEditors != null) {
                    if (selectedNode instanceof OutputTreeNode) {
                        tabFolderEditors.getOutputTreeSchemaEditor().refresh();
                    } else if (selectedNode instanceof TreeNode) {
                        tabFolderEditors.getInputTreeSchemaEditor().refresh();
                    }
                }
            }
        }
    }

    @Override
    protected boolean calculateEnabled() {
        if (getSelectedObjects().isEmpty()) {
            return false;
        } else {
            boolean enable = true;
            // get the last selection to run the action
            Object s = getSelectedObjects().get(0);
            if (s instanceof List && !((List) s).isEmpty()) {
                List selectedarts = (List) s;
                Object obj = selectedarts.get(selectedarts.size() - 1);
                if (obj instanceof TreeNodeEditPart) {
                    TreeNodeEditPart nodePart = (TreeNodeEditPart) obj;
                    this.selectedNode = (TreeNode) nodePart.getModel();
                    if (NodeType.NAME_SPACE.equals(selectedNode.getNodeType()) || selectedNode.isChoice()) {
                        return false;
                    }
                    int xPathLength = XmlMapUtil.getXPathLength(selectedNode.getXpath());
                    if (xPathLength <= 2) {
                        enable = false;
                    }
                } else {
                    enable = false;
                }
            }

            return enable;
        }
    }

    public void update(Object selection) {
        setSelection(new StructuredSelection(selection));
    }

    public MapperManager getMapperManager() {
        return this.mapperManager;
    }

    public void setMapperManager(MapperManager mapperManager) {
        this.mapperManager = mapperManager;
    }
}
