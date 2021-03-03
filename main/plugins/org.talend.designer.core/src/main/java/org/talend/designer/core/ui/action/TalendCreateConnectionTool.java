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

import org.eclipse.gef.DragTracker;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.requests.CreationFactory;
import org.talend.designer.core.ui.editor.nodes.NodePart;

/**
 * DOC Talend class global comment. Detailled comment
 */
public class TalendCreateConnectionTool extends TalendConnectionCreationTool implements DragTracker {

    public TalendCreateConnectionTool(CreationFactory factory, NodePart nodePart) {
        super(factory, false);
        setUnloadWhenFinished(true);
        this.sourcePart = nodePart;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.tools.AbstractConnectionCreationTool#handleButtonUp(int)
     */
    @Override
    protected boolean handleButtonUp(int button) {
        if (button == 1 && stateTransition(STATE_CONNECTION_STARTED, STATE_TERMINAL)) {
            if (getTargetEditPart() == null) {
                EditPart processPart = getProcessPart(this.sourcePart);
                if (processPart != null) {
                    this.setTargetEditPart(getProcessPart(this.sourcePart));
                    this.getTargetRequest().setType(RequestConstants.REQ_CONNECTION_END);
                }
            }
            return handleConnection();
        }

        super.handleButtonDown(button);
        if (isInState(STATE_CONNECTION_STARTED)) {
            // Fake a drag to cause feedback to be displayed immediately on
            // mouse down.
            handleDrag();
        }
        return true;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.ui.action.TalendConnectionCreationTool#handleCreateConnection()
     */
    @Override
    protected boolean handleCreateConnection() {
        // setAvoidDeactivation(true);
        //
        // EditPartViewer viewer = getCurrentViewer();
        // Command endCommand = getCommand();
        // if (endCommand == null) {
        // if (isInState(STATE_TERMINAL)) {
        // // Fake a drag to cause feedback to be displayed immediately on mouse down.
        // setState(STATE_CONNECTION_STARTED);
        // handleDrag();
        // }
        // return true;
        // }
        // sourcePart.getViewer().getSelectionManager().deselect(sourcePart);
        //
        // setCurrentCommand(endCommand);
        //
        // executeCurrentCommand();
        //
        // selectAddedObject(viewer, getReturnValues(endCommand));
        //
        // setAvoidDeactivation(false);
        // eraseSourceFeedback();
        // deactivate();
        //
        // return true;

        return super.handleCreateConnection();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.tools.TargetingTool#getCommand()
     */
    @Override
    protected Command getCommand() {
        if (getTargetEditPart() == null) {
            return null;
        }
        return getTargetEditPart().getCommand(getTargetRequest());
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.tools.AbstractConnectionCreationTool#deactivate()
     */
    @Override
    public void deactivate() {
        if (!avoidDeactivation()) {
            super.deactivate();
            setTargetRequest(null);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.tools.AbstractConnectionCreationTool#eraseSourceFeedback()
     */
    @Override
    protected void eraseSourceFeedback() {
        if (!avoidDeactivation()) {
            super.eraseSourceFeedback();
        }
    }

}
