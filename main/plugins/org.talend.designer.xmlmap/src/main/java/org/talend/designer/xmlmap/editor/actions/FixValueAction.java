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

import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IWorkbenchPart;
import org.talend.designer.xmlmap.model.emf.xmlmap.NodeType;
import org.talend.designer.xmlmap.model.emf.xmlmap.TreeNode;
import org.talend.designer.xmlmap.model.emf.xmlmap.XmlMapData;
import org.talend.designer.xmlmap.parts.TreeNodeEditPart;
import org.talend.designer.xmlmap.ui.dialog.NameSpaceDialog;
import org.talend.designer.xmlmap.ui.tabs.MapperManager;
import org.talend.designer.xmlmap.util.XmlMapUtil;

/**
 * DOC talend class global comment. Detailled comment
 */
public class FixValueAction extends SelectionAction {

    private MapperManager mapperManager;

    private TreeNode selectedNode;

    public static final String ID = "org.talend.designer.xmlmap.editor.actions.FixValueAction";

    public FixValueAction(IWorkbenchPart part) {
        super(part);
        setId(ID);
        setText("Change Namespace");
    }

    @Override
    public void run() {
        if (selectedNode != null) {
            NameSpaceDialog nsDialog = new NameSpaceDialog(null);
            nsDialog.setCurrentNode(selectedNode);
            nsDialog.setParentNode((TreeNode) selectedNode.eContainer());
            int status = nsDialog.open();
            if (status == nsDialog.OK) {
                String defaultValue = nsDialog.getNSValue();
                if (defaultValue != null) {
                    defaultValue = defaultValue.trim();
                }
                String prefix = nsDialog.getPrefix().trim();

                selectedNode.setDefaultValue(defaultValue);
                if (prefix == null || "".equals(prefix)) {
                    prefix = XmlMapUtil.DEFAULT_NAME_SPACE_PREFIX;
                }
                selectedNode.setName(prefix);
                XmlMapData externalEmfData = (XmlMapData) mapperManager.getExternalData();
                XmlMapUtil.updateXPathAndExpression(externalEmfData, mapperManager.getMapperComponent().getExpressionManager(),
                        selectedNode, prefix, XmlMapUtil.getXPathLength(selectedNode.getXpath()), true);
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
                    this.selectedNode = (TreeNode) nodePart.getModel();
                    boolean isNameSpace = NodeType.NAME_SPACE.equals(selectedNode.getNodeType());
                    if (isNameSpace && (selectedNode.getExpression() == null || "".equals(selectedNode.getExpression()))) {
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

}
