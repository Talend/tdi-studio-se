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
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.ui.editor.cmd.ChangeActivateStatusSubjobCommand;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.nodes.NodePart;

/**
 * Action used to set the start status on a node with the context menu. <br/>
 *
 * $Id: NodeSetActivateAction.java 3351 2007-05-04 12:14:00 +0000 (ven., 04 mai 2007) plegall $
 *
 */
public class ActivateSubjobOneComponentAction extends SelectionAction {

    public static final String ID = "org.talend.designer.core.ui.editor.action.ActivateSubjobOneComponentAction"; //$NON-NLS-1$

    private static final String TEXT_SET_ACTIVATE_PART = Messages.getString("ActivateSubjobAction.ActivatePart2"); //$NON-NLS-1$

    private static final String TEXT_REM_ACTIVATE_PART = Messages.getString("ActivateSubjobAction.DeactivatePart2"); //$NON-NLS-1$

    public ActivateSubjobOneComponentAction(IWorkbenchPart part) {
        super(part);
        setId(ID);
    }

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
            if (!(part.getModel() instanceof Node)) {
                return false;
            }
            Node node = (Node) part.getModel();
            if (ComponentCategory.CATEGORY_4_CAMEL.getName().equals(node.getProcess().getComponentsType())) {
                return false;
            }
            if (node.getJobletNode() != null) {
                return false;
            }
            if (node.isReadOnly()) {
                return false;
            }
            if (node.getPropertyValue(EParameterName.ACTIVATE.getName()) == null) {
                return false;
            }
            if (node.isSubProcessStart() && node.isStart()) {
                if (node.isActivate()) {
                    setText(TEXT_REM_ACTIVATE_PART);
                } else {
                    setText(TEXT_SET_ACTIVATE_PART);
                }
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.action.Action#run()
     */
    public void run() {
        List editparts = getSelectedObjects();
        if (editparts.size() == 1) {
            NodePart part = (NodePart) editparts.get(0);

            ChangeActivateStatusSubjobCommand changeActivateStatusCommand = new ChangeActivateStatusSubjobCommand(
                    (Node) part.getModel(), true);
            execute(changeActivateStatusCommand);
        }
    }
}
