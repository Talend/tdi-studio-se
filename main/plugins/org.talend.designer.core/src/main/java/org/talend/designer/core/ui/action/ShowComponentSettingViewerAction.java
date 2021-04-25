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

import org.eclipse.gef.EditPart;
import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.IProcess;
import org.talend.designer.core.DesignerPlugin;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.nodes.NodePart;
import org.talend.designer.core.ui.editor.process.ProcessPart;
import org.talend.designer.core.ui.views.properties.ComponentSettingsView;

/**
 * DOC qwei class global comment. Detailled comment <br/>
 *
 */
public class ShowComponentSettingViewerAction extends SelectionAction {

    public static final String ID = "org.talend.designer.core.ui.editor.action.ShowComponentSettingViewerAction"; //$NON-NLS-1$

    private IProcess process;

    private Node node;

    /**
     * Constructs a new PropertiesContextAction.
     *
     * @param part
     */
    public ShowComponentSettingViewerAction(IWorkbenchPart part) {
        super(part);
        setId(ID);
        setImageDescriptor(DesignerPlugin.getImageDescriptor("icons/breakpoint.png")); //$NON-NLS-1$
        setText(Messages.getString("PropertiesContextAction.Properties")); //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.ui.actions.WorkbenchPartAction#calculateEnabled()
     */
    @Override
    protected boolean calculateEnabled() {
        List objects = getSelectedObjects();
        if (objects.isEmpty()) {
            return false;
        }
        if (!(objects.get(0) instanceof EditPart)) {
            return false;
        }
        return true;
    }

    /**
     * Test if the selected item is a node.
     *
     * @return true / false
     */
    private boolean canPerformAction() {
        if (getSelectedObjects().isEmpty()) {
            return false;
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

            EditPart parentPart = part.getParent();
            while (!(parentPart instanceof ProcessPart)) {
                parentPart = parentPart.getParent();
            }
            if (!(parentPart instanceof ProcessPart)) {
                return false;
            }
            process = (IProcess) ((ProcessPart) parentPart).getModel();
            setText(Messages.getString("PropertiesContextAction.Properties")); //$NON-NLS-1$

        }
        return true;

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
            page.showView(getViewId());

        } catch (PartInitException e) {
            // TODO Auto-generated catch block
            // e.printStackTrace();
            ExceptionHandler.process(e);
        }

    }

    public String getViewId() {
        // return "org.eclipse.ui.views.PropertySheet"; //$NON-NLS-1$
        return ComponentSettingsView.ID;
    }
}
