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
import org.talend.designer.xmlmap.model.emf.xmlmap.AbstractInOutTree;
import org.talend.designer.xmlmap.model.emf.xmlmap.NodeType;
import org.talend.designer.xmlmap.model.emf.xmlmap.OutputTreeNode;
import org.talend.designer.xmlmap.model.emf.xmlmap.OutputXmlTree;
import org.talend.designer.xmlmap.parts.OutputTreeNodeEditPart;
import org.talend.designer.xmlmap.ui.tabs.MapperManager;
import org.talend.designer.xmlmap.util.XmlMapUtil;

/**
 * DOC talend class global comment. Detailled comment
 */
public class SetAggregateAction extends SelectionAction {

    public static String ID = "xml map set as aggregate action";

    private OutputTreeNodeEditPart nodePart;

    private MapperManager mapperManager;

    public SetAggregateAction(IWorkbenchPart part) {
        super(part);
        setId(ID);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.ui.actions.WorkbenchPartAction#calculateEnabled()
     */
    @Override
    protected boolean calculateEnabled() {
        if (getSelectedObjects().isEmpty()) {
            return false;
        }
        Object s = getSelectedObjects().get(0);
        if (s instanceof List && !((List) s).isEmpty()) {
            List selectedarts = (List) s;
            Object obj = selectedarts.get(selectedarts.size() - 1);
            if (obj instanceof OutputTreeNodeEditPart) {
                nodePart = (OutputTreeNodeEditPart) obj;
                OutputTreeNode model = (OutputTreeNode) nodePart.getModel();
                // root can't be aggregate
                if (NodeType.NAME_SPACE.equals(model.getNodeType()) || !(model.eContainer() instanceof OutputTreeNode)
                        || model.isChoice() || model.isSubstitution()) {
                    return false;
                }
                if (!XmlMapUtil.isExpressionEditable(model)) {
                    return false;
                }

                boolean isInputMultiLoops = false;

                if (mapperManager.getMainInputTree() != null && mapperManager.getMainInputTree().isMultiLoops()) {
                    isInputMultiLoops = true;
                }

                AbstractInOutTree abstractTree = XmlMapUtil.getAbstractInOutTree(model);
                if (abstractTree instanceof OutputXmlTree) {
                    OutputXmlTree outputTree = ((OutputXmlTree) abstractTree);
                    if (outputTree.isAllInOne()) {
                        return false;
                    }
                    // fixed for TDI-20808 ,disable group and aggregate for 501
                    if (isInputMultiLoops && outputTree.isMultiLoops()) {
                        return false;
                    }
                }

                if (!model.isAggregate()) {
                    setText("As aggregate element");
                } else {
                    setText("Remove aggregate element");
                }
            } else {
                return false;
            }
        }

        return true;
    }

    public void update(Object selection) {
        setSelection(new StructuredSelection(selection));
    }

    @Override
    public void run() {
        OutputTreeNode model = (OutputTreeNode) nodePart.getModel();
        model.setAggregate(!model.isAggregate());
    }

    public void setMapperManager(MapperManager mapperManager) {
        this.mapperManager = mapperManager;
    }

}
