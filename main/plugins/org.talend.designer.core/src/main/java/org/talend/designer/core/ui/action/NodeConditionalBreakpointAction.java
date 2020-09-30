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
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.model.process.EComponentCategory;
import org.talend.core.model.process.INode;
import org.talend.core.ui.utils.PluginUtil;
import org.talend.designer.core.DesignerPlugin;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.nodes.NodePart;
import org.talend.designer.core.ui.editor.process.ProcessPart;
import org.talend.designer.core.ui.views.properties.ComponentSettingsView;

/**
 * Action to toggle a conditional breakpoint on a camel route node, for ESB only. <br/>
 *
 * $Id$
 *
 */
public class NodeConditionalBreakpointAction extends ShowComponentSettingViewerAction {

    public static final String ID = "org.talend.designer.core.ui.editor.action.NodeConditionalBreakpointAction"; //$NON-NLS-1$

    private Node node;

    /**
     * Constructs a new PropertiesContextAction.
     *
     * @param part
     */
    public NodeConditionalBreakpointAction(IWorkbenchPart part) {
        super(part);
        setId(ID);
        setImageDescriptor(DesignerPlugin.getImageDescriptor("icons/breakpoint.png")); //$NON-NLS-1$
        setText(Messages.getString("NodeConditionalBreakpointAction.conditionalBreakpoint")); //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.ui.actions.WorkbenchPartAction#calculateEnabled()
     */
    @Override
    protected boolean calculateEnabled() {
        if (!PluginUtil.isMediation()) {
            return false;
        }
        List objects = getSelectedObjects();
        if (objects.isEmpty()) {
            return false;
        }
        if (!(objects.get(0) instanceof EditPart)) {
            return false;
        }
        List parts = getSelectedObjects();
        if (parts.size() == 1) {
            Object o = parts.get(0);
            if (!(o instanceof NodePart)) {
                return false;
            }
            NodePart part = (NodePart) o;
            if (part.getModel() instanceof INode) {
                INode n = (INode) part.getModel();
                if (0 == n.getIncomingConnections().size()) {
                    return false;
                }
            } else {
                return false;
            }
            node = (Node) part.getModel();
            if (node.getJobletNode() != null) {
                return false;
            }
            if (node.getStatus() != org.talend.designer.core.ui.editor.process.Process.BREAKPOINT_STATUS) {
                return false;
            }
            EditPart parentPart = part.getParent();
            while (!(parentPart instanceof ProcessPart)) {
                parentPart = parentPart.getParent();
            }
            if (!(parentPart instanceof ProcessPart)) {
                return false;
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
        IWorkbench workbench = PlatformUI.getWorkbench();
        IWorkbenchPage page = workbench.getActiveWorkbenchWindow().getActivePage();
        try {
            ComponentSettingsView view = (ComponentSettingsView) page.showView(getViewId());
            view.selectTab(EComponentCategory.BREAKPOINT_CAMEL);
        } catch (PartInitException e) {
            ExceptionHandler.process(e);
        }

    }

    public String getViewId() {
        return ComponentSettingsView.ID;
    }
}
