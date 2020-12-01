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
package org.talend.designer.core.ui.editor.connections;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editparts.AbstractConnectionEditPart;
import org.eclipse.gef.editpolicies.ConnectionEditPolicy;
import org.eclipse.gef.editpolicies.ConnectionEndpointEditPolicy;
import org.eclipse.gef.requests.GroupRequest;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.talend.core.PluginChecker;
import org.talend.core.model.components.ComponentCategory;
import org.talend.core.model.process.Element;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.IProcess;
import org.talend.designer.core.DesignerPlugin;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.ui.editor.cmd.ConnectionDeleteCommand;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.nodes.NodeFigure;
import org.talend.designer.core.ui.editor.subjobcontainer.SubjobContainer;
import org.talend.designer.core.ui.preferences.TalendDesignerPrefConstants;
import org.talend.designer.core.ui.views.CodeView;
import org.talend.designer.core.ui.views.properties.ComponentSettingsView;

/**
 * Graphical part of the connection of Gef. <br/>
 *
 * $Id$
 *
 */
public class ConnectionPart extends AbstractConnectionEditPart implements PropertyChangeListener {

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#activate()
     */
    @Override
    public void activate() {
        if (!isActive()) {
            super.activate();
            ((Element) getModel()).addPropertyChangeListener(this);
        }
    }

    @Override
    protected void addChildVisual(EditPart childEditPart, int index) {
        IFigure child = ((GraphicalEditPart) childEditPart).getFigure();
        getContentPane().add(child, index);
    }

    @Override
    protected void unregisterVisuals() {
        super.unregisterVisuals();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#deactivate()
     */
    @Override
    public void deactivate() {
        if (isActive()) {
            super.deactivate();
            ((Element) getModel()).removePropertyChangeListener(this);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.editparts.AbstractConnectionEditPart#deactivateFigure()
     */
    @Override
    protected void deactivateFigure() {
        ConnectionFigure connFig = (ConnectionFigure) this.getFigure();
        IFigure targetFig = connFig.getTargetAnchor().getOwner();
        if (targetFig != null && targetFig instanceof NodeFigure) {
            ((NodeFigure) targetFig).removeTargetConnection(connFig);
        }
        IFigure sourceFig = connFig.getSourceAnchor().getOwner();
        if (sourceFig != null && sourceFig instanceof NodeFigure) {
            ((NodeFigure) sourceFig).removeSourceConnection(connFig);
        }
        super.deactivateFigure();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.editparts.AbstractEditPart#setSelected(int)
     */
    @Override
    public void setSelected(final int value) {
        super.setSelected(value);
        List cl = this.getChildren();
        for (int i = 0; i < cl.size(); i++) {
            if (((EditPart) cl.get(i)).getSelected() != value) {
                ((EditPart) cl.get(i)).setSelected(value);
            }
        }

        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        if (value == SELECTED_PRIMARY) {
            IViewPart view = page.findView(ComponentSettingsView.ID);
            if (view != null) {
                ComponentSettingsView compSettings = (ComponentSettingsView) view;
                compSettings.setElement((Connection) getModel());
                CodeView.refreshCodeView((Connection) getModel());
            }
        } else if (value == SELECTED_NONE) {
            IViewPart view = page.findView(ComponentSettingsView.ID);
            if (view != null) {
                ComponentSettingsView compSettings = (ComponentSettingsView) view;
                compSettings.cleanDisplay();
            }
        }

    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.editparts.AbstractEditPart#createEditPolicies()
     */
    @Override
    protected void createEditPolicies() {
        // Selection handle edit policy.
        // Makes the connection show a feedback, when selected by the user.
        installEditPolicy(EditPolicy.CONNECTION_ENDPOINTS_ROLE, new ConnectionEndpointEditPolicy());
        // Allows the removal of the connection model element
        installEditPolicy(EditPolicy.CONNECTION_ROLE, new ConnectionEditPolicy() {

            @Override
            protected Command getDeleteCommand(GroupRequest request) {
                if (((Connection) getModel()).isReadOnly()) {
                    return null;
                }
                List<Connection> connectionList = new ArrayList<Connection>();
                for (int i = 0; i < request.getEditParts().size(); i++) {
                    if (request.getEditParts().get(i) instanceof ConnectionPart) {
                        connectionList.add(((Connection) ((ConnectionPart) request.getEditParts().get(i)).getModel()));
                    }
                }
                return new ConnectionDeleteCommand(connectionList);
            }
        });
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.editparts.AbstractConnectionEditPart#createFigure()
     */
    @Override
    protected IFigure createFigure() {
        IConnection conn = (IConnection) getModel();
        ConnectionFigure connection = new ConnectionFigure(conn, conn.getSourceNodeConnector().getConnectionProperty(
                conn.getLineStyle()), conn.getSource());

        if (DesignerPlugin.getDefault().getPreferenceStore().getBoolean(TalendDesignerPrefConstants.EDITOR_LINESTYLE)) {
            connection.setLineWidth(2);
            connection.setConnectionRouter(new TalendBorderItemRectilinearRouter());
        } else {
            connection.setLineWidth(1);
        }

        if (((Connection) getModel()).isActivate()) {
            connection.setAlpha(-1);
        } else {
            connection.setAlpha(Connection.ALPHA_VALUE);
        }
        if (PluginChecker.isAutoParalelPluginLoaded()) {
            connection.updateStatus();
        }

        return connection;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    @Override
    public void propertyChange(final PropertyChangeEvent event) {
        String property = event.getPropertyName();
        ConnectionFigure connectionFigure = (ConnectionFigure) figure;
        if (Connection.LINESTYLE_PROP.equals(property)) {
            connectionFigure.setConnectionProperty(((Connection) getModel()).getSourceNodeConnector().getConnectionProperty(
                    ((Connection) getModel()).getLineStyle()));
            refreshChildren();
        }
        if (Connection.NAME.equals(property)) {
            refreshChildren();
        }

        if (EParameterName.MONITOR_CONNECTION.getName().equals(property)) {
            connectionFigure.repaint();
            refreshChildren();
        }

        if (EParameterName.NONE.getName().equals(property) || EParameterName.PARTITIONER.getName().equals(property)
                || EParameterName.DEPARTITIONER.getName().equals(property)
                || EParameterName.REPARTITIONER.getName().equals(property)) {
            connectionFigure.updateStatus();
            connectionFigure.repaint();
            refreshChildren();
        }

        if (property.equals(EParameterName.ACTIVATE.getName())) {
            if (((Connection) getModel()).isActivate()) {
                connectionFigure.setAlpha(-1);
                connectionFigure.repaint();
                refreshVisuals();
            } else {
                connectionFigure.setAlpha(Connection.ALPHA_VALUE);
                connectionFigure.repaint();
                refreshVisuals();
            }
        }

    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.editparts.AbstractEditPart#getModelChildren()
     */
    @Override
    protected List getModelChildren() {
        List<Element> elements;
        elements = new ArrayList<Element>();
        elements.add(((Connection) getModel()).getConnectionLabel());
        elements.add(((Connection) getModel()).getPerformance());

        if (((Connection) getModel()).getResuming() != null) {
            elements.add(((Connection) getModel()).getResuming());
        }

        boolean monitorSupport = true;
        if (getParent() != null && getRoot() != null) {
            EditPart contents = getRoot().getContents();
            if (contents.getModel() instanceof IProcess) {
                IProcess currentProcess = (IProcess) contents.getModel();
                if (ComponentCategory.CATEGORY_4_MAPREDUCE.getName().endsWith(currentProcess.getComponentsType())) {
                    monitorSupport = false;
                }
            }
        } else {
            IProcess currentProcess = ((Connection) getModel()).getSource().getProcess();
            if (ComponentCategory.CATEGORY_4_MAPREDUCE.getName().endsWith(currentProcess.getComponentsType())) {
                monitorSupport = false;
            }
        }

        if (monitorSupport) {
            if (((Connection) getModel()).getConnectionTrace() != null) {
                elements.add(((Connection) getModel()).getConnectionTrace());
            }

            // Add monitor label
            if (((Connection) getModel()).isMonitorConnection()) {
                elements.add(((Connection) getModel()).getMonitorLabel());
            }
        }

        return elements;
    }

    @Override
    public boolean isSelectable() {
        Connection conn = (Connection) this.getModel();
        Node source = (Node) conn.getSource();
        Node target = (Node) conn.getTarget();
        SubjobContainer sourceSubjob = source.getNodeContainer().getSubjobContainer();
        SubjobContainer targetSubjob = target.getNodeContainer().getSubjobContainer();
        if (sourceSubjob != null) {
            if ((sourceSubjob == targetSubjob) && sourceSubjob.isCollapsed()) {
                return false;
            } else if (!conn.isActivate() && sourceSubjob.isCollapsed()) {
                if (sourceSubjob.getSubjobStartNode().getUniqueName().equals(source.getUniqueName())) {
                    return super.isSelectable();
                }
                return false;
            }
        }

        return super.isSelectable();
    }
}
