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
import org.talend.commons.ui.gmf.util.DisplayUtils;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.ui.dialog.FilterColumnDialog;
import org.talend.designer.core.ui.editor.connections.ConnLabelEditPart;
import org.talend.designer.core.ui.editor.connections.Connection;
import org.talend.designer.core.ui.editor.connections.ConnectionPart;
import org.talend.designer.core.ui.editor.connections.ConnectionTraceEditPart;

/**
 * hwang class global comment. Detailled comment
 */
public class FilterTraceColumnAction extends SelectionAction {

    private static final String SETUP_TRACES_TITLE = Messages.getString("FilterTraceColumnAction.SetupTraces"); //$NON-NLS-1$

    private static final String CONDITION = "CONDITION"; //$NON-NLS-1$

    private static final String COLUMN = "COLUMN"; //$NON-NLS-1$

    private Connection connection;

    private IWorkbenchPart part;

    public FilterTraceColumnAction(IWorkbenchPart part) {
        super(part);
        this.part = part;

        setText(SETUP_TRACES_TITLE);
        setToolTipText(SETUP_TRACES_TITLE);
        setDescription(SETUP_TRACES_TITLE);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.ui.actions.WorkbenchPartAction#calculateEnabled()
     */
    @Override
    protected boolean calculateEnabled() {
        List parts = getSelectedObjects();
        if (parts.size() != 1) {
            return false;
        }
        Object input = parts.get(0);

        if (input instanceof ConnectionPart) {
            ConnectionPart connPart = (ConnectionPart) input;
            List childParts = connPart.getChildren();
            for (Object part : childParts) {
                if (part != null && part instanceof ConnectionTraceEditPart) {
                    connection = (Connection) connPart.getModel();
                    return connection.enableTraces() && connection.checkTraceShowEnable();
                }
            }
        }
        if (input instanceof ConnLabelEditPart) {
            ConnLabelEditPart labelPart = (ConnLabelEditPart) input;
            ConnectionPart connPart = (ConnectionPart) labelPart.getParent();
            List childParts = connPart.getChildren();
            for (Object part : childParts) {
                if (part != null && part instanceof ConnectionTraceEditPart) {
                    connection = (Connection) connPart.getModel();
                    return connection.enableTraces() && connection.checkTraceShowEnable();
                }
            }
        }
        if (input instanceof ConnectionTraceEditPart) {
            ConnectionTraceEditPart connTrace = (ConnectionTraceEditPart) input;
            if (connTrace.getParent() instanceof ConnectionPart) {
                ConnectionPart connPart = (ConnectionPart) connTrace.getParent();
                connection = (Connection) connPart.getModel();
                return connection.enableTraces() && connection.checkTraceShowEnable();
            }
        }
        return false;
    }

    @Override
    public void run() {
        FilterColumnDialog dialog = new FilterColumnDialog(DisplayUtils.getDefaultShell(false), connection,
                this
                .getCommandStack());
        dialog.open();
    }

}
