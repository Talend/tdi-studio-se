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
package org.talend.designer.core.ui.action;

import java.util.List;

import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.ui.IWorkbenchPart;
import org.talend.core.model.components.ComponentCategory;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.ui.editor.cmd.SetParallelizationCommand;
import org.talend.designer.core.ui.editor.nodecontainer.NodeContainer;
import org.talend.designer.core.ui.editor.nodecontainer.NodeContainerPart;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.nodes.NodePart;
import org.talend.designer.core.ui.editor.subjobcontainer.SubjobContainer;
import org.talend.designer.core.ui.editor.subjobcontainer.SubjobContainerPart;

public class SetParallelizationAction extends SelectionAction {

    public static final String ID = "org.talend.designer.core.ui.editor.action.SetParallelizationAction"; //$NON-NLS-1$

    private static final String INPUT = "Input";

    private static final String OUTPUT = "Output";

    IWorkbenchPart part;

    public SetParallelizationAction(IWorkbenchPart part) {
        super(part);
        this.part = part;
        setId(ID);
        setText(Messages.getString("PropertiesContextAction.parallelization")); //$NON-NLS-1$
    }

    @Override
    protected boolean calculateEnabled() {
        List parts = getSelectedObjects();
        if (parts.isEmpty()) {
            return false;
        }
        if (parts.size() == 1) {
            Object o = parts.get(0);
            if (o instanceof SubjobContainerPart) {
                SubjobContainerPart part = (SubjobContainerPart) o;
                SubjobContainer subjob = (SubjobContainer) part.getModel();
                if (subjob.getProcess().getComponentsType().equals(ComponentCategory.CATEGORY_4_DI.getName())) {
                    if (subjob.isDisplayed()) {
                        return true;
                    } else {
                        return false;
                    }
                } else {
                    return false;
                }

            } else if (o instanceof NodePart) {
                NodePart part = (NodePart) o;
                Node node = (Node) part.getModel();
                if (node.getProcess().getComponentsType().equals(ComponentCategory.CATEGORY_4_DI.getName())) {
                    if (node.isStart()) {
                        return true;
                    } else {
                        return false;
                    }
                } else {
                    return false;
                }

            } else {
                return false;
            }
        }
        return false;
    }

    @Override
    public void run() {
        List editparts = getSelectedObjects();
        if (editparts.size() == 1) {
            Object o = editparts.get(0);
            if (o instanceof NodePart) {
                NodePart part = (NodePart) o;
                Node node = (Node) part.getModel();
                getCommandStack().execute(new SetParallelizationCommand(node));
            } else if (o instanceof SubjobContainerPart) {
                boolean hasStartNode = false;
                List<NodeContainerPart> childNodes = ((SubjobContainerPart) o).getChildren();
                for (NodeContainerPart childNode : childNodes) {
                    NodeContainerPart part = (NodeContainerPart) childNode;
                    NodeContainer node = (NodeContainer) part.getModel();
                    if (node.getNode().isStart()) {
                        hasStartNode = true;
                        getCommandStack().execute(new SetParallelizationCommand(node.getNode()));
                    }
                }
                if (!hasStartNode) {
                    for (NodeContainerPart childNode : childNodes) {
                        NodeContainerPart part = (NodeContainerPart) childNode;
                        NodeContainer node = (NodeContainer) part.getModel();
                        if (node.getNode().isSubProcessStart()) {
                            getCommandStack().execute(new SetParallelizationCommand(node.getNode()));
                        }
                    }
                }
            }
        }
    }
}
