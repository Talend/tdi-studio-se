// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
//
// This source code is available under agreement availe at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.core.ui.editor.nodes;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.compare.internal.CompareEditor;
import org.eclipse.draw2d.ChopboxAnchor;
import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.NodeEditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.RootEditPart;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.gef.requests.CreateConnectionRequest;
import org.eclipse.gef.requests.ReconnectRequest;
import org.eclipse.gef.tools.DirectEditManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.talend.commons.exception.CommonExceptionHandler;
import org.talend.commons.exception.LoginException;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.exception.MessageBoxExceptionHandler;
import org.talend.commons.ui.runtime.image.ImageUtils.ICON_SIZE;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.PluginChecker;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.general.Project;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.IConnectionCategory;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.IExternalData;
import org.talend.core.model.process.IExternalNode;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.ProcessUtils;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.JobletProcessItem;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.runtime.CoreRuntimePlugin;
import org.talend.core.ui.IJobletProviderService;
import org.talend.core.ui.ITestContainerProviderService;
import org.talend.core.ui.editor.IJobEditorHandler;
import org.talend.core.ui.editor.JobEditorHandlerManager;
import org.talend.core.ui.images.CoreImageProvider;
import org.talend.core.ui.process.IGraphicalNode;
import org.talend.core.ui.services.IComponentsLocalProviderService;
import org.talend.designer.core.DesignerPlugin;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.model.components.DummyComponent;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.model.components.ExternalUtilities;
import org.talend.designer.core.model.process.AbstractProcessProvider;
import org.talend.designer.core.ui.AbstractMultiPageTalendEditor;
import org.talend.designer.core.ui.editor.ETalendSelectionType;
import org.talend.designer.core.ui.editor.TalendSelectionManager;
import org.talend.designer.core.ui.editor.cmd.ExternalNodeChangeCommand;
import org.talend.designer.core.ui.editor.connections.Connection;
import org.talend.designer.core.ui.editor.connections.ConnectionFigure;
import org.talend.designer.core.ui.editor.connections.TalendBorderItemRectilinearRouter;
import org.talend.designer.core.ui.editor.process.Process;
import org.talend.designer.core.ui.editor.process.ProcessPart;
import org.talend.designer.core.ui.editor.subjobcontainer.SubjobContainerPart;
import org.talend.designer.core.ui.preferences.TalendDesignerPrefConstants;
import org.talend.designer.core.ui.views.CodeView;
import org.talend.designer.core.ui.views.properties.ComponentSettingsView;
import org.talend.designer.runprocess.ItemCacheManager;
import org.talend.repository.ProjectManager;
import org.talend.repository.RepositoryWorkUnit;

/**
 * Graphical part of the node of Gef. <br/>
 *
 * $Id$
 *
 */
public class NodePart extends AbstractGraphicalEditPart implements PropertyChangeListener, NodeEditPart {

    protected DirectEditManager manager;

    private boolean isDrop;

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.editparts.AbstractEditPart#setSelected(int)
     */
    @Override
    @SuppressWarnings("unchecked")
    public void setSelected(final int value) {
        if (value == SELECTED) {
            super.setSelected(SELECTED_PRIMARY);
        } else {
            super.setSelected(value);
        }
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        if (value == SELECTED_NONE) {
            ComponentSettingsView viewer = (ComponentSettingsView) page.findView(ComponentSettingsView.ID);
            if (viewer == null) {
                return;
            }
            ComponentSettingsView compSettings = viewer;
            compSettings.cleanDisplay();
            return;
        }
        IEditorPart activeEditor = page.getActiveEditor();
        if (activeEditor instanceof AbstractMultiPageTalendEditor) {
            GraphicalViewer designerViewer = ((AbstractMultiPageTalendEditor) activeEditor).getTalendEditor().getViewer();
            Control ctrl = designerViewer.getControl();
            String helpLink = (String) ((Node) getModel()).getPropertyValue(EParameterName.HELP.getName());
            String requiredHelpLink = ((Process) ((Node) getModel()).getProcess()).getBaseHelpLink()
                    + ((Node) getModel()).getComponent().getName();
            if (helpLink == null || "".equals(helpLink) || !requiredHelpLink.equals(helpLink)) { //$NON-NLS-1$
                helpLink = ((Process) ((Node) getModel()).getProcess()).getBaseHelpLink()
                        + ((Node) getModel()).getComponent().getName();
            }
            if (ctrl != null) {
                PlatformUI.getWorkbench().getHelpSystem().setHelp(ctrl, helpLink);
            }
            // This code was to update automatically the help view, to set the documentation of the selected component
            // remove for TDI-24396
            //            IViewPart view = page.findView("org.eclipse.help.ui.HelpView"); //$NON-NLS-1$
            // if (view != null) {
            // PlatformUI.getWorkbench().getHelpSystem().displayHelp(helpLink);
            // }
            TalendSelectionManager selectionManager = (TalendSelectionManager) designerViewer.getSelectionManager();
            if (value == SELECTED || value == SELECTED_PRIMARY) {
                ComponentSettingsView viewer = (ComponentSettingsView) page.findView(ComponentSettingsView.ID);
                if (viewer == null) {
                    return;
                }

                if (selectionManager.getSelectionType() == ETalendSelectionType.SINGLE) {
                    ComponentSettingsView compSettings = viewer;
                    compSettings.setElement((Node) getModel());
                    if (((Node) getModel()).getComponent() instanceof DummyComponent) {
                        MessageDialog.openWarning(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
                                Messages.getString("NodePart_warning"), //$NON-NLS-1$
                                Messages.getString("NodePart_componentNotLoaded")); //$NON-NLS-1$
                        return;
                    }

                    CodeView.refreshCodeView((Node) getModel());
                } else if (!viewer.isCleaned() && selectionManager.getSelectionType() == ETalendSelectionType.MULTIPLE) {
                    ComponentSettingsView compSettings = viewer;
                    compSettings.cleanDisplay();
                } else if (isDrop()) {
                    if (value == SELECTED || value == SELECTED_PRIMARY) {
                        ComponentSettingsView compSettings = viewer;
                        compSettings.setElement((Node) getModel());
                        if (((Node) getModel()).getComponent() instanceof DummyComponent) {
                            MessageDialog.openWarning(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
                                    Messages.getString("NodePart_warning"), //$NON-NLS-1$
                                    Messages.getString("NodePart_componentNotLoaded")); //$NON-NLS-1$
                            return;
                        }

                        CodeView.refreshCodeView((Node) getModel());
                    }
                }

            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#activate()
     */
    @Override
    public void activate() {
        if (!isActive()) {
            super.activate();
            ((Node) getModel()).addPropertyChangeListener(this);
        }
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
            ((Node) getModel()).removePropertyChangeListener(this);
        }

    }

    @Override
    protected void unregisterVisuals() {
        if (((NodeFigure) getFigure()).connection != null) {
            ((NodeFigure) getFigure()).connection.disposeColors();
        }
        if (getRoot() != null) {
            super.unregisterVisuals();
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#getModelSourceConnections()
     */
    @Override
    protected List getModelSourceConnections() {
        return ((INode) this.getModel()).getOutgoingConnections();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#getModelTargetConnections()
     */
    @Override
    protected List getModelTargetConnections() {
        return ((INode) this.getModel()).getIncomingConnections();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.editparts.AbstractEditPart#refreshVisuals()
     */
    @Override
    @SuppressWarnings("unchecked")
    protected void refreshVisuals() {
        Node node = (Node) this.getModel();
        Point loc = node.getLocation();
        Rectangle rectangle = new Rectangle(loc, node.getSize());
        ((GraphicalEditPart) getParent()).setLayoutConstraint(this, getFigure(), rectangle);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#createFigure()
     */
    @Override
    protected IFigure createFigure() {
        NodeFigure nodeFigure;
        // EditPart parentPart = getParent();
        // while (!(parentPart instanceof ProcessPart)) {
        // parentPart = parentPart.getParent();
        // }

        nodeFigure = new NodeFigure((Node) this.getModel());

        if (((INode) getModel()).isStart()) {
            nodeFigure.setStart(true);
        } else {
            nodeFigure.setStart(false);
        }
        if (((Node) getModel()).isSetShowHint()) {
            nodeFigure.setHint(((Node) getModel()).getShowHintText());
        }
        nodeFigure.setDummy(((Node) getModel()).isDummy());
        if (((INode) getModel()).isActivate()) {
            nodeFigure.setAlpha(-1);
        } else {
            nodeFigure.setAlpha(Node.ALPHA_VALUE);
        }
        return nodeFigure;
    }

    // ------------------------------------------------------------------------
    // Abstract methods from AbstractEditPart

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.editparts.AbstractEditPart#createEditPolicies()
     */
    @Override
    protected void createEditPolicies() {
        installEditPolicy(EditPolicy.COMPONENT_ROLE, new NodeEditPolicy());
        installEditPolicy(EditPolicy.GRAPHICAL_NODE_ROLE, new NodeGraphicalEditPolicy());
        installEditPolicy(EditPolicy.LAYOUT_ROLE, new NodeResizableEditPolicy());
        installEditPolicy(SelectionFeedbackEditPolicy.SELECTION_FEEDBACK, new SelectionFeedbackEditPolicy(this));
    }

    // ------------------------------------------------------------------------
    // Abstract methods from PropertyChangeListener

    /*
     * (non-Javadoc)
     *
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    @Override
    public void propertyChange(final PropertyChangeEvent changeEvent) {
        boolean needUpdateSubjob = false;
        if (!this.isActive()) {
            return;
        }
        if (changeEvent.getPropertyName().equals(Node.LOCATION)) {
            refreshVisuals();
            needUpdateSubjob = true;
        } else if (changeEvent.getPropertyName().equals(Node.PERFORMANCE_DATA)) {
            refreshVisuals();
            getParent().refresh();
            needUpdateSubjob = true;
        } else if (changeEvent.getPropertyName().equals(Node.INPUTS)) {
            refreshTargetConnections();
            needUpdateSubjob = true;
        } else if (changeEvent.getPropertyName().equals(Node.OUTPUTS)) {
            refreshSourceConnections();
            refreshTargetConnections();
            needUpdateSubjob = true;
        } else if (changeEvent.getPropertyName().equals(Node.SIZE)) {
            refreshVisuals();
            getParent().refresh();
            needUpdateSubjob = true;
        } else if (changeEvent.getPropertyName().equals(Node.ICON_CHANGE)) {
            changeIcon((Node) changeEvent.getSource());
            refreshVisuals();
        } else if (changeEvent.getPropertyName().equals(EParameterName.ACTIVATE.getName())) {
            if (((INode) getModel()).isActivate()) {
                ((NodeFigure) figure).setDummy(((Node) getModel()).isDummy());
                ((NodeFigure) figure).setAlpha(-1);
                ((NodeFigure) figure).repaint();
                refreshVisuals();
            } else {
                ((NodeFigure) figure).setDummy(((Node) getModel()).isDummy());
                ((NodeFigure) figure).setAlpha(Node.ALPHA_VALUE);
                ((NodeFigure) figure).repaint();
                refreshVisuals();
            }
        } else if (changeEvent.getPropertyName().equals(EParameterName.DUMMY.getName())) {
            if (((INode) getModel()).isActivate()) {
                ((NodeFigure) figure).setDummy(((Node) getModel()).isDummy());
                ((NodeFigure) figure).setAlpha(-1);
            } else {
                ((NodeFigure) figure).setDummy(((Node) getModel()).isDummy());
                ((NodeFigure) figure).setAlpha(Node.ALPHA_VALUE);
            }
            ((NodeFigure) figure).repaint();
            refreshVisuals();
        } else if (changeEvent.getPropertyName().equals(EParameterName.START.getName())) {
            if (((INode) getModel()).isStart()) {
                ((NodeFigure) figure).setStart(true);
                ((NodeFigure) figure).repaint();
                refreshVisuals();
            } else {
                ((NodeFigure) figure).setStart(false);
                ((NodeFigure) figure).repaint();
                refreshVisuals();
            }
        } else if (changeEvent.getPropertyName().equals(EParameterName.HINT.getName())) {
            if (((Node) getModel()).isSetShowHint()) {
                ((NodeFigure) figure).setHint(((Node) getModel()).getShowHintText());
            } else {
                ((NodeFigure) figure).setHint(""); //$NON-NLS-1$
            }
            needUpdateSubjob = true;
        } else if (changeEvent.getPropertyName().equals(EParameterName.CONNECTION_FORMAT.getName())) {
            Node node = (Node) getModel();

            for (IConnection conn : ((Node) getModel()).getOutgoingConnections()) {
                String connIdName = null;
                String oldName = conn.getUniqueName();
                if (conn instanceof Connection && conn.getLineStyle().hasConnectionCategory(IConnectionCategory.FLOW)) {
                    node.getProcess().removeUniqueConnectionName(oldName);
                    if (node.getProcess().checkValidConnectionName(node.getConnectionName(), false)) {
                        connIdName = node.getProcess().generateUniqueConnectionName(node.getConnectionName());
                    } else {
                        connIdName = node.getProcess().generateUniqueConnectionName("row"); //$NON-NLS-1$
                    }
                    if (node.getProcess().checkValidConnectionName(connIdName)) {
                        ((Connection) conn).setUniqueName(connIdName);
                        node.getProcess().addUniqueConnectionName(connIdName);
                        ((Connection) conn).setName(connIdName);
                    } else {
                        node.getProcess().addUniqueConnectionName(oldName);
                    }
                }
            }
        } else if (changeEvent.getPropertyName().equals(EParameterName.REPAINT.getName())) {
            ((NodeFigure) figure).repaint();
            refreshVisuals();
            EditPart parentPart = getParent();
            if (parentPart != null) {
                parentPart.refresh();
            }
            needUpdateSubjob = true;
        }
        if (needUpdateSubjob) {
            EditPart editPart = getParent();
            if (editPart != null) {
                while ((!(editPart instanceof ProcessPart)) && (!(editPart instanceof SubjobContainerPart))) {
                    editPart = editPart.getParent();
                }
                if (editPart instanceof SubjobContainerPart) {
                    editPart.refresh();
                }
            }
        }
    }

    // ------------------------------------------------------------------------
    // Abstract methods from NodeEditPart

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.NodeEditPart#getSourceConnectionAnchor(org.eclipse.gef.ConnectionEditPart)
     */
    @Override
    public ConnectionAnchor getSourceConnectionAnchor(final ConnectionEditPart connection) {
        // return new ChopboxAnchor(getFigure());
        if (connection.getModel() instanceof Connection) {
            if (((Connection) connection.getModel()).getLineStyle().hasConnectionCategory(IConnectionCategory.FLOW)) {
                ((NodeFigure) getFigure()).addSourceConnection((ConnectionFigure) connection.getFigure());
            }
        }
        Connection conn = (Connection) connection.getModel();
        NodeAnchor anchor = new NodeAnchor((NodeFigure) getFigure(), (IGraphicalNode) conn.getSource(),
                (IGraphicalNode) conn.getTarget(), false);
        anchor.setConnection(conn);
        return anchor;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.NodeEditPart#getTargetConnectionAnchor(org.eclipse.gef.ConnectionEditPart)
     */
    @Override
    public ConnectionAnchor getTargetConnectionAnchor(final ConnectionEditPart connection) {
        // return new ChopboxAnchor(getFigure());

        if (connection.getModel() instanceof Connection) {
            if (((Connection) connection.getModel()).getLineStyle().hasConnectionCategory(IConnectionCategory.FLOW)) {
                ((NodeFigure) getFigure()).setTargetConnection((ConnectionFigure) connection.getFigure());
            }
        }
        Connection conn = (Connection) connection.getModel();
        sourceAnchor = null;
        NodeAnchor anchor = new NodeAnchor((NodeFigure) getFigure(), (IGraphicalNode) conn.getSource(),
                (IGraphicalNode) conn.getTarget(), true);
        anchor.setConnection(conn);
        return anchor;
    }

    NodeAnchor sourceAnchor = null;

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.NodeEditPart#getSourceConnectionAnchor(org.eclipse.gef.Request)
     */
    @Override
    public ConnectionAnchor getSourceConnectionAnchor(final Request request) {
        if (!DesignerPlugin.getDefault().getPreferenceStore().getBoolean(TalendDesignerPrefConstants.EDITOR_LINESTYLE)) {
            return new ChopboxAnchor(getFigure());
        }
        if (!(request instanceof CreateConnectionRequest)) {
            if (request instanceof ReconnectRequest) {
                IFigure figure = ((ReconnectRequest) request).getConnectionEditPart().getFigure();
                if (figure instanceof ConnectionFigure) {
                    ((ConnectionFigure) figure).setConnectionRouter(new TalendBorderItemRectilinearRouter(request));
                }
            }
            return new ChopboxAnchor(getFigure());
        }
        CreateConnectionRequest connReq = (CreateConnectionRequest) request;
        Node source = (Node) ((NodePart) connReq.getSourceEditPart()).getModel();
        // Node target = (Node) ((NodePart) connReq.getTargetEditPart()).getModel();
        // // System.out.println("getSource=> location:" + connReq.getLocation() + " / source:" + source.getLocation() +
        // "
        // // / target:"
        // // + target.getLocation());
        sourceAnchor = new DummyNodeAnchor((NodeFigure) getFigure(), source, false);
        return sourceAnchor;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.NodeEditPart#getTargetConnectionAnchor(org.eclipse.gef.Request)
     */
    @Override
    public ConnectionAnchor getTargetConnectionAnchor(final Request request) {
        if (!DesignerPlugin.getDefault().getPreferenceStore().getBoolean(TalendDesignerPrefConstants.EDITOR_LINESTYLE)) {
            return new ChopboxAnchor(getFigure());
        }
        if (!(request instanceof CreateConnectionRequest)) {
            if (request instanceof ReconnectRequest) {
                IFigure figure = ((ReconnectRequest) request).getConnectionEditPart().getFigure();
                if (figure instanceof ConnectionFigure) {
                    ((ConnectionFigure) figure).setConnectionRouter(new TalendBorderItemRectilinearRouter(request));
                }
            }
            return new ChopboxAnchor(getFigure());
        }
        CreateConnectionRequest connReq = (CreateConnectionRequest) request;
        Node source = (Node) ((NodePart) connReq.getSourceEditPart()).getModel();
        Node target = (Node) ((NodePart) connReq.getTargetEditPart()).getModel();
        // // System.out.println("getTarget=> location:" + connReq.getLocation() + " / source:" + source.getLocation() +
        // "
        // // / target:"
        // // + target.getLocation());
        if (sourceAnchor != null) {
            sourceAnchor.setTarget(target);
        }
        return new DummyNodeAnchor((NodeFigure) getFigure(), source, target, true);
    }

    @Override
    public void performRequest(Request req) {
        Node node = (Node) getModel();
        IExternalData oldExternalData = node.getExternalData();
        if (req.getType().equals(RequestConstants.REQ_OPEN)) {
            IExternalNode externalNode = null;
            if (node.isExternalNode()) {
                IElementParameter externalParam = node.getElementParameterFromField(EParameterFieldType.EXTERNAL);
                if (externalParam != null && externalParam.isShow(node.getElementParameters())) {
                    externalNode = ExternalUtilities.getExternalNodeReadyToOpen(node);
                }
            }

            IWorkbenchPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
            if (externalNode != null && (part instanceof AbstractMultiPageTalendEditor)) {
                boolean isOriginalNode = false;
                ITestContainerProviderService testContainerService = null;
                if (GlobalServiceRegister.getDefault().isServiceRegistered(ITestContainerProviderService.class)) {
                    testContainerService = (ITestContainerProviderService) GlobalServiceRegister.getDefault().getService(
                            ITestContainerProviderService.class);
                    if (testContainerService != null) {
                        isOriginalNode = testContainerService.isOriginalNode(node);
                        if (isOriginalNode) {
                            testContainerService.renameConnection(node, true);
                        }
                    }
                }
                int returnValue = externalNode.open(getViewer().getControl().getShell());
                if (!node.isReadOnly()) {
                    if (returnValue == SWT.OK) {
                        Command cmd = new ExternalNodeChangeCommand(node, externalNode, oldExternalData);
                        CommandStack cmdStack = (CommandStack) part.getAdapter(CommandStack.class);
                        cmdStack.execute(cmd);
                    } else {
                        externalNode.setExternalData(oldExternalData);
                    }
                }
                if (isOriginalNode && (testContainerService != null)) {
                    testContainerService.renameConnection(node, false);
                }
                
            } else if (externalNode != null && (part instanceof CompareEditor)) {
                externalNode.setReadOnly(true);
                externalNode.open(getViewer().getControl().getShell());
            }
            else {
                // add for feature 13361
                boolean isJoblet = false;

                if (PluginChecker.isJobLetPluginLoaded()) {
                    AbstractProcessProvider jobletProcessProvider = AbstractProcessProvider
                            .findProcessProviderFromPID(IComponent.JOBLET_PID);
                    final IJobletProviderService service = (IJobletProviderService) GlobalServiceRegister.getDefault()
                            .getService(IJobletProviderService.class);
                    boolean isAvoidShowJobletAfterDoubleClick = false;
                    if (GlobalServiceRegister.getDefault().isServiceRegistered(IComponentsLocalProviderService.class)) {
                        IComponentsLocalProviderService componentService = (IComponentsLocalProviderService) GlobalServiceRegister
                                .getDefault().getService(IComponentsLocalProviderService.class);
                        if (componentService != null) {
                            isAvoidShowJobletAfterDoubleClick = componentService.isAvoidToShowJobletAfterDoubleClick();
                        }
                    }
                    if (service != null && service.isJobletComponent(node) && !isAvoidShowJobletAfterDoubleClick) {
                        isJoblet = true;
                        String version = (String) node.getPropertyValue(EParameterName.PROCESS_TYPE_VERSION.getName());
                        final Item jobletItem = jobletProcessProvider.getJobletItem(node, version);
                        RepositoryWorkUnit<Object> repositoryWorkUnit = new RepositoryWorkUnit<Object>("", this) {

                            @Override
                            protected void run() throws LoginException, PersistenceException {
                                if (jobletItem != null) {
                                    service.openJobletItem((JobletProcessItem) jobletItem);
                                }
                            }
                        };
                        repositoryWorkUnit.setAvoidUnloadResources(true);
                        CoreRuntimePlugin.getInstance().getProxyRepositoryFactory().executeRepositoryWorkUnit(repositoryWorkUnit);
                    }
                }

                if (!isJoblet) {
                    IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
                    String processName = (String) node.getPropertyValue(EParameterName.PROCESS_TYPE_PROCESS.getName());
                    String version = (String) node.getPropertyValue(EParameterName.PROCESS_TYPE_VERSION.getName());

                    boolean isAvoidShowJobAfterDoubleClick = false;
                    if (GlobalServiceRegister.getDefault().isServiceRegistered(IComponentsLocalProviderService.class)) {
                        IComponentsLocalProviderService service = (IComponentsLocalProviderService) GlobalServiceRegister
                                .getDefault().getService(IComponentsLocalProviderService.class);
                        if (service != null) {
                            isAvoidShowJobAfterDoubleClick = service.isAvoidToShowJobAfterDoubleClick();
                        }
                    }
                    // bug 20796
                    boolean isSelectUseDynamic = false;
                    Object useDynamicJobValue = node.getPropertyValue(EParameterName.USE_DYNAMIC_JOB.getName());
                    if (useDynamicJobValue != null && useDynamicJobValue instanceof Boolean) {
                        isSelectUseDynamic = (Boolean) useDynamicJobValue;
                    }
                    if (processName != null && !"".equals(processName) && !isAvoidShowJobAfterDoubleClick && !isSelectUseDynamic) { //$NON-NLS-1$
                        ItemCacheManager.clearCache();
                        ProcessItem processItem = ItemCacheManager.getProcessItem(processName, version);
                        if (processItem == null) {
                            return;
                        }
                        processName = ProcessUtils.getPureItemId(processName);
                        Property updatedProperty = null;
                        try {
                            updatedProperty = ProxyRepositoryFactory
                                    .getInstance()
                                    .getLastVersion(new Project(ProjectManager.getInstance().getProject(processItem)),
                                            processName).getProperty();
                        } catch (PersistenceException e) {
                            CommonExceptionHandler.process(e);
                        }
                        // update the property of the node repository object
                        // node.getObject().setProperty(updatedProperty);

                        processItem = (ProcessItem) updatedProperty.getItem();
                        final ProcessItem item = processItem;
                        RepositoryWorkUnit<Object> repositoryWorkUnit = new RepositoryWorkUnit<Object>("", this) {

                            @Override
                            protected void run() throws LoginException, PersistenceException {
                                try {
                                    if (item != null) {
                                        ERepositoryObjectType repObjType = ERepositoryObjectType.getItemType(item);
                                        IJobEditorHandler editorInputFactory = JobEditorHandlerManager.getInstance()
                                                .extractEditorInputFactory(repObjType.getType());
                                        editorInputFactory.openJobEditor(editorInputFactory.createJobEditorInput(item, true));
                                    }
                                } catch (PartInitException e) {
                                    MessageBoxExceptionHandler.process(e);
                                }
                            }
                        };
                        repositoryWorkUnit.setAvoidUnloadResources(true);
                        CoreRuntimePlugin.getInstance().getProxyRepositoryFactory().executeRepositoryWorkUnit(repositoryWorkUnit);
                    } else {
                        try {
                            // modified for feature 2454.
                            page.showView(ComponentSettingsView.ID);
                        } catch (PartInitException e) {
                            CommonExceptionHandler.process(e);
                        }
                    }
                }
            }
        }
        super.performRequest(req);
    }

    @Override
    protected void refreshSourceConnections() {
        int i;
        ConnectionEditPart editPart;
        Object model;

        Map modelToEditPart = new HashMap();
        List editParts = getSourceConnections();

        for (i = 0; i < editParts.size(); i++) {
            editPart = (ConnectionEditPart) editParts.get(i);
            modelToEditPart.put(editPart.getModel(), editPart);
        }

        List modelObjects = getModelSourceConnections();

        // List<? extends INodeConnector> connList = node.getListConnector();
        if (modelObjects == null) {
            modelObjects = new ArrayList();
        }
        for (i = 0; i < modelObjects.size(); i++) {
            model = modelObjects.get(i);

            if (model instanceof IConnection) {
                INode sourcenode = ((IConnection) model).getSource();
                INode targetnode = ((IConnection) model).getSource();
                String connectorName = ((IConnection) model).getConnectorName();
                if (sourcenode.getConnectorFromName(connectorName) == null
                        && targetnode.getConnectorFromName(connectorName) == null) {
                    return;
                }
            }
            if (i < editParts.size() && ((EditPart) editParts.get(i)).getModel() == model) {
                continue;
            }

            editPart = (ConnectionEditPart) modelToEditPart.get(model);
            if (editPart != null) {
                reorderSourceConnection(editPart, i);
            } else {
                editPart = createOrFindConnection(model);
                addSourceConnection(editPart, i);
            }
        }

        // Remove the remaining EditParts
        List trash = new ArrayList();
        for (; i < editParts.size(); i++) {
            trash.add(editParts.get(i));
        }
        for (i = 0; i < trash.size(); i++) {
            removeSourceConnection((ConnectionEditPart) trash.get(i));
        }
    }

    @Override
    protected void refreshTargetConnections() {
        int i;
        ConnectionEditPart editPart;
        Object model;

        Map mapModelToEditPart = new HashMap();
        List connections = getTargetConnections();

        for (i = 0; i < connections.size(); i++) {
            editPart = (ConnectionEditPart) connections.get(i);
            mapModelToEditPart.put(editPart.getModel(), editPart);
        }

        List modelObjects = getModelTargetConnections();
        if (modelObjects == null) {
            modelObjects = new ArrayList();
        }

        for (i = 0; i < modelObjects.size(); i++) {
            model = modelObjects.get(i);
            if (model instanceof IConnection) {
                INode sourcenode = ((IConnection) model).getSource();
                INode targetnode = ((IConnection) model).getSource();
                String connectorName = ((IConnection) model).getConnectorName();
                if (sourcenode.getConnectorFromName(connectorName) == null
                        && targetnode.getConnectorFromName(connectorName) == null) {
                    return;
                }
            }
            if (i < connections.size() && ((EditPart) connections.get(i)).getModel() == model) {
                continue;
            }

            editPart = (ConnectionEditPart) mapModelToEditPart.get(model);
            if (editPart != null) {
                reorderTargetConnection(editPart, i);
            } else {
                editPart = createOrFindConnection(model);
                addTargetConnection(editPart, i);
            }
        }

        // Remove the remaining Connection EditParts
        List trash = new ArrayList();
        for (; i < connections.size(); i++) {
            trash.add(connections.get(i));
        }
        for (i = 0; i < trash.size(); i++) {
            removeTargetConnection((ConnectionEditPart) trash.get(i));
        }
    }

    /**
     * Getter for isDrop.
     *
     * @return the isDrop
     */
    public boolean isDrop() {
        return this.isDrop;
    }

    /**
     * Sets the isDrop.
     *
     * @param isDrop the isDrop to set
     */
    public void setDrop(boolean isDrop) {
        this.isDrop = isDrop;
    }

    @Override
    public RootEditPart getRoot() {
        if (getParent() != null) {
            return getParent().getRoot();
        }
        return null;
    }

    public void changeIcon(Node node) {
        ((NodeFigure) getFigure()).getImageFigure().setImage(
                CoreImageProvider.getComponentIcon(node.getComponent(), ICON_SIZE.ICON_32));
    }
}
