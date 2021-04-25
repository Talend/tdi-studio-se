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
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.talend.core.PluginChecker;
import org.talend.core.model.process.EComponentCategory;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.ui.editor.connections.ConnLabelEditPart;
import org.talend.designer.core.ui.editor.connections.Connection;
import org.talend.designer.core.ui.editor.connections.ConnectionPart;
import org.talend.designer.core.ui.editor.connections.ConnectionTraceEditPart;
import org.talend.designer.core.ui.views.properties.ComponentSettingsView;

/**
 * DOC Administrator class global comment. Detailled comment
 */
public class ShowBreakpointAction extends SelectionAction {

    private static final String SHOW_BREAKPOINT_TITLE = Messages.getString("ShowBreakpointAction.ShowBreakpoint"); //$NON-NLS-1$

    private Connection connection;

    private IWorkbenchPart part;

    /**
     * DOC Administrator ShowBreakpointAction constructor comment.
     *
     * @param part
     */
    public ShowBreakpointAction(IWorkbenchPart part) {
        super(part);
        this.part = part;
        // TODO Auto-generated constructor stub
        setText(SHOW_BREAKPOINT_TITLE);
        setToolTipText(SHOW_BREAKPOINT_TITLE);
        setDescription(SHOW_BREAKPOINT_TITLE);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.ui.actions.WorkbenchPartAction#calculateEnabled()
     */
    @Override
    protected boolean calculateEnabled() {
        // TODO Auto-generated method stub
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
                    return connection.enableTraces() && PluginChecker.isTraceDebugPluginLoaded();
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
                    return connection.enableTraces() && PluginChecker.isTraceDebugPluginLoaded();
                }
            }
        }
        if (input instanceof ConnectionTraceEditPart) {
            ConnectionTraceEditPart connTrace = (ConnectionTraceEditPart) input;
            if (connTrace.getParent() instanceof ConnectionPart) {
                ConnectionPart connPart = (ConnectionPart) connTrace.getParent();
                connection = (Connection) connPart.getModel();
                return connection.enableTraces() && PluginChecker.isTraceDebugPluginLoaded();
            }
        }
        return false;
    }

    public void run() {
        IWorkbench workbench = PlatformUI.getWorkbench();
        IWorkbenchPage page = workbench.getActiveWorkbenchWindow().getActivePage();
        ComponentSettingsView view;
        try {
            view = (ComponentSettingsView) page.showView(ComponentSettingsView.ID);
            view.setElement(connection);
            view.selectTab(EComponentCategory.BREAKPOINT);
        } catch (PartInitException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public String getViewId() {
        // return "org.eclipse.ui.views.PropertySheet"; //$NON-NLS-1$
        return ComponentSettingsView.ID;
    }
}
