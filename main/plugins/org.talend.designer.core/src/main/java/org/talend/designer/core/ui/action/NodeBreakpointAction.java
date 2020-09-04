// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
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

import org.eclipse.gef.EditPart;
import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.ui.IWorkbenchPart;
import org.talend.core.CorePlugin;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.context.Context;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.IProcess;
import org.talend.core.ui.branding.IBrandingService;
import org.talend.core.ui.utils.PluginUtil;
import org.talend.designer.core.DesignerPlugin;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.nodes.NodePart;
import org.talend.designer.core.ui.editor.process.Process;
import org.talend.designer.core.ui.editor.process.ProcessPart;

/**
 * Action to toggle a breakpoint on a node. <br/>
 *
 * $Id$
 *
 */
public class NodeBreakpointAction extends SelectionAction {

    public static final String ID = "org.talend.designer.core.ui.editor.action.NodeBreakpointAction"; //$NON-NLS-1$

    private IProcess process;

    private Node node;

    /**
     * Constructs a new NodeBreakpointAction.
     *
     * @param part
     */
    public NodeBreakpointAction(IWorkbenchPart part) {
        super(part);
        setId(ID);
        setImageDescriptor(DesignerPlugin.getImageDescriptor("icons/breakpoint.png")); //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.ui.actions.WorkbenchPartAction#calculateEnabled()
     */
    @Override
    protected boolean calculateEnabled() {
        return canPerformAction();
    }

    /**
     * Test if the selected item is a node.
     *
     * @return true / false
     */
    private boolean canPerformAction() {
        IBrandingService brandingService = (IBrandingService) GlobalServiceRegister.getDefault().getService(
                IBrandingService.class);
        if (!brandingService.getBrandingConfiguration().isAllowDebugMode()) {
            return false;
        }

        if (getSelectedObjects().isEmpty()) {
            return false;
        }
        if (PluginUtil.isMediation()) {
//            return false;
        }
        List parts = getSelectedObjects();
        if (parts.size() == 1) {
            Object o = parts.get(0);
            if (!(o instanceof NodePart)) {
                return false;
            }
            NodePart part = (NodePart) o;
            if (!(part.getModel() instanceof INode)) {
                return false;
            }
            node = (Node) part.getModel();
            if (node.getJobletNode() != null) {
                return false;
            }
            EditPart parentPart = part.getParent();
            while (!(parentPart instanceof ProcessPart)) {
                parentPart = parentPart.getParent();
            }
            if (!(parentPart instanceof ProcessPart)) {
                return false;
            }
            process = (IProcess) ((ProcessPart) parentPart).getModel();

            if (CorePlugin.getContext().getBreakpointNodes(process).contains(node)) {
                setText(Messages.getString("NodeBreakpointAction.removeBreakpoint")); //$NON-NLS-1$
            } else {
                setText(Messages.getString("NodeBreakpointAction.addBreakPoint")); //$NON-NLS-1$
            }
            return true;
        }

        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.action.Action#run()
     */
    public void run() {
        Context context = CorePlugin.getContext();
        if (context.getBreakpointNodes(process).contains(node)) {
            context.removeBreakpoint(process, node);
            node.removeStatus(Process.BREAKPOINT_STATUS);
        } else {
            context.addBreakpoint(process, node);
            node.addStatus(Process.BREAKPOINT_STATUS);
        }
    }

}
