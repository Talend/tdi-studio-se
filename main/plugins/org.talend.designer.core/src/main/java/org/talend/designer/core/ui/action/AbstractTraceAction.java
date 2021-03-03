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
import org.talend.core.model.process.IElementParameter;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.ui.editor.cmd.PropertyChangeCommand;
import org.talend.designer.core.ui.editor.connections.ConnLabelEditPart;
import org.talend.designer.core.ui.editor.connections.Connection;
import org.talend.designer.core.ui.editor.connections.ConnectionPart;
import org.talend.designer.core.ui.editor.connections.ConnectionTraceEditPart;

/**
 * ggu class global comment. Detailled comment
 */
public abstract class AbstractTraceAction extends SelectionAction {

    public AbstractTraceAction(IWorkbenchPart part, String text, String tipText, String desc) {
        super(part);
        setText(text);
        setToolTipText(tipText);
        setDescription(desc);
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
                    Connection conn = (Connection) connPart.getModel();

                    if (conn.enableTraces() && conn.checkTraceShowEnable()) {
                        IElementParameter element = conn.getElementParameter(EParameterName.TRACES_CONNECTION_ENABLE.getName());
                        Boolean flag = (Boolean) element.getValue();
                        if (flag != isEnableAction()) {
                            return true;
                        }
                    }
                }
            }
        }
        if (input instanceof ConnLabelEditPart) {
            ConnLabelEditPart labelPart = (ConnLabelEditPart) input;
            ConnectionPart connPart = (ConnectionPart) labelPart.getParent();
            List childParts = connPart.getChildren();
            for (Object part : childParts) {
                if (part != null && part instanceof ConnectionTraceEditPart) {
                    Connection conn = (Connection) connPart.getModel();

                    if (conn.enableTraces() && conn.checkTraceShowEnable()) {
                        IElementParameter element = conn.getElementParameter(EParameterName.TRACES_CONNECTION_ENABLE.getName());
                        Boolean flag = (Boolean) element.getValue();
                        if (flag != isEnableAction()) {
                            return true;
                        }
                    }
                }
            }
        }
        if (input instanceof ConnectionTraceEditPart) {
            ConnectionTraceEditPart tracePart = (ConnectionTraceEditPart) input;
            ConnectionPart connPart = (ConnectionPart) tracePart.getParent();

            Connection conn = (Connection) connPart.getModel();

            if (conn.enableTraces() && conn.checkTraceShowEnable()) {
                IElementParameter element = conn.getElementParameter(EParameterName.TRACES_CONNECTION_ENABLE.getName());
                Boolean flag = (Boolean) element.getValue();
                if (flag != isEnableAction()) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void run() {

        List selection = getSelectedObjects();

        Object input = selection.get(0);
        if (input instanceof ConnectionPart) {
            ConnectionPart connPart = (ConnectionPart) input;
            List childParts = connPart.getChildren();
            for (Object part : childParts) {
                if (part != null && part instanceof ConnectionTraceEditPart) {
                    ConnectionTraceEditPart tracePart = (ConnectionTraceEditPart) part;

                    Connection conn = (Connection) connPart.getModel();
                    execute(new PropertyChangeCommand(conn, EParameterName.TRACES_CONNECTION_ENABLE.getName(), isEnableAction()));
                    tracePart.refresh();
                    break;
                }
            }
        }
        if (input instanceof ConnLabelEditPart) {
            ConnLabelEditPart labelPart = (ConnLabelEditPart) input;
            ConnectionPart connPart = (ConnectionPart) labelPart.getParent();
            List childParts = connPart.getChildren();
            for (Object part : childParts) {
                if (part != null && part instanceof ConnectionTraceEditPart) {
                    ConnectionTraceEditPart tracePart = (ConnectionTraceEditPart) part;

                    Connection conn = (Connection) connPart.getModel();
                    execute(new PropertyChangeCommand(conn, EParameterName.TRACES_CONNECTION_ENABLE.getName(), isEnableAction()));
                    tracePart.refresh();
                    break;
                }
            }
        }
        if (input instanceof ConnectionTraceEditPart) {
            ConnectionTraceEditPart tracePart = (ConnectionTraceEditPart) input;
            ConnectionPart connPart = (ConnectionPart) tracePart.getParent();

            Connection conn = (Connection) connPart.getModel();
            execute(new PropertyChangeCommand(conn, EParameterName.TRACES_CONNECTION_ENABLE.getName(), isEnableAction()));

            tracePart.refresh();
        }

    }

    protected abstract boolean isEnableAction();

}
