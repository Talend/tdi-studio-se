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
package org.talend.designer.core.ui.editor;

import java.util.List;
import java.util.Map;

import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.ContextMenuProvider;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.LayerConstants;
import org.eclipse.gef.editparts.LayerManager;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.gef.ui.actions.GEFActionConstants;
import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.actions.ActionFactory;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.PluginChecker;
import org.talend.core.model.process.EConnectionType;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.INodeConnector;
import org.talend.core.ui.IJobletProviderService;
import org.talend.core.ui.editor.CustomExternalActions;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.ui.action.ActivateElementAction;
import org.talend.designer.core.ui.action.ActivateSubjobAction;
import org.talend.designer.core.ui.action.ActivateSubjobOneComponentAction;
import org.talend.designer.core.ui.action.AddToJobletAction;
import org.talend.designer.core.ui.action.BringForwardAction;
import org.talend.designer.core.ui.action.BringToFrontAction;
import org.talend.designer.core.ui.action.ConnectionCreateAction;
import org.talend.designer.core.ui.action.ConnectionSetAsMainRef;
import org.talend.designer.core.ui.action.DisableParallelizationAction;
import org.talend.designer.core.ui.action.DisplaySubjobAction;
import org.talend.designer.core.ui.action.FilterTraceColumnAction;
import org.talend.designer.core.ui.action.GEFCopyAction;
import org.talend.designer.core.ui.action.GEFPasteAction;
import org.talend.designer.core.ui.action.ModifyConnectionOrderAction;
import org.talend.designer.core.ui.action.ModifyMergeOrderAction;
import org.talend.designer.core.ui.action.ModifyOutputOrderAction;
import org.talend.designer.core.ui.action.NodeBreakpointAction;
import org.talend.designer.core.ui.action.NodeConditionalBreakpointAction;
import org.talend.designer.core.ui.action.OpentRunJobComponentAction;
import org.talend.designer.core.ui.action.ParallelExecutionAction;
import org.talend.designer.core.ui.action.SearchComponentAction;
import org.talend.designer.core.ui.action.SendBackwardAction;
import org.talend.designer.core.ui.action.SendToBackAction;
import org.talend.designer.core.ui.action.SetParallelizationAction;
import org.talend.designer.core.ui.action.ShowBreakpointAction;
import org.talend.designer.core.ui.action.ShowComponentSettingViewerAction;
import org.talend.designer.core.ui.action.TraceDisableAction;
import org.talend.designer.core.ui.action.TraceEnableAction;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.hierarchy.OpenJobHierarchyAction;

/**
 * Class that manages the context menu in the Gef Editor. <br/>
 *
 * $Id$
 *
 */
public class TalendEditorContextMenuProvider extends ContextMenuProvider {

    private ActionRegistry actionRegistry;

    private IWorkbenchPart part;

    public static final String ID = "org.talend.designer.core.ui.editor.TalendEditorContextMenuProvider"; //$NON-NLS-1$

    private static final String GROUP_CONNECTIONS = "ConnectionsGroup"; //$NON-NLS-1$

    private static final String GROUP_OTHER = "OtherGroup"; //$NON-NLS-1$

    private static final String GROUP_BREAKPOINT = "GroupBreakpoint";

    private static boolean enableContextMenu = true;

    public TalendEditorContextMenuProvider(IWorkbenchPart part, EditPartViewer viewer, ActionRegistry registry) {
        super(viewer);
        if (registry == null) {
            throw new IllegalArgumentException();
        }
        this.actionRegistry = registry;
        this.part = part;
    }

    /**
     * Called when the context menu is about to show. Actions, whose state is enabled, will appear in the context menu.
     *
     * @see org.eclipse.gef.ContextMenuProvider#buildContextMenu(org.eclipse.jface.action.IMenuManager)
     */
    @Override
    public void buildContextMenu(final IMenuManager menu) {
        if (!isEnableContextMenu()) {
            return;
        }
        // Add standard action groups to the menu
        menu.add(new Separator(GROUP_CONNECTIONS));
        menu.add(new Separator(GEFActionConstants.GROUP_UNDO));
        menu.add(new Separator(GEFActionConstants.GROUP_COPY));
        menu.add(new Separator(GEFActionConstants.GROUP_EDIT));
        menu.add(new Separator(GEFActionConstants.GROUP_REST));
        menu.add(new Separator(GROUP_OTHER));
        menu.add(new Separator(GEFActionConstants.GROUP_VIEW));
        menu.add(new Separator(GROUP_BREAKPOINT));
        IAction action;

        menu.appendToGroup(GEFActionConstants.GROUP_UNDO, // target group id
                getAction(ActionFactory.UNDO.getId())); // action to add
        menu.appendToGroup(GEFActionConstants.GROUP_UNDO, getAction(ActionFactory.REDO.getId()));

        if (part != null) {
            action = getAction(ActionFactory.COPY.getId()); // new GEFCopyAction(part);
            if (action instanceof GEFCopyAction) {
                ((GEFCopyAction) action).update();
            }
            menu.appendToGroup(GEFActionConstants.GROUP_EDIT, action);

            action = getAction(ActionFactory.PASTE.getId()); // new GEFPasteAction(part);
            GEFPasteAction pasteAction = (GEFPasteAction) action;
            Point p = Display.getCurrent().getCursorLocation();

            p = this.getViewer().getControl().toControl(p);

            // gcui see bug 7812:Copy/Paste component in Job designer.
            LayerManager layerManager = (LayerManager) this.getViewer().getEditPartRegistry().get(LayerManager.ID);
            IFigure contentLayer = layerManager.getLayer(LayerConstants.PRINTABLE_LAYERS);
            // System.out.println("X:" + contentLayer.getSize().width + "   Y:" + contentLayer.getSize().height);

            org.eclipse.draw2d.geometry.Point p1 = new org.eclipse.draw2d.geometry.Point(p.x, p.y);
            contentLayer.translateToAbsolute(p1);
            // System.out.println("relative:" + p);

            p.x = 2 * (p.x) - p1.x;
            p.y = 2 * (p.y) - p1.y;
            // System.out.println("absolute:" + p);

            pasteAction.setCursorLocation(p);
            menu.appendToGroup(GEFActionConstants.GROUP_EDIT, action);

            menu.appendToGroup(GEFActionConstants.GROUP_EDIT, getAction(ActionFactory.DELETE.getId()));

            menu.appendToGroup(GEFActionConstants.GROUP_EDIT, getAction(ActionFactory.SELECT_ALL.getId()));

            action = new ActivateElementAction(part);
            ((ActivateElementAction) action).update();
            if (action.isEnabled()) {
                menu.appendToGroup(GEFActionConstants.GROUP_REST, action);
            }

            action = new ActivateSubjobAction(part);
            ((ActivateSubjobAction) action).update();
            if (action.isEnabled()) {
                menu.appendToGroup(GEFActionConstants.GROUP_REST, action);
            }

            action = new ActivateSubjobOneComponentAction(part);
            ((ActivateSubjobOneComponentAction) action).update();
            if (action.isEnabled()) {
                menu.appendToGroup(GEFActionConstants.GROUP_REST, action);
            }
            action = getAction(ConnectionSetAsMainRef.ID);
            if (action.isEnabled()) {
                menu.appendToGroup(GEFActionConstants.GROUP_REST, action);
            }

            action = getAction(ModifyMergeOrderAction.ID);
            if (action.isEnabled()) {
                menu.appendToGroup(GEFActionConstants.GROUP_REST, action);
            }

            action = getAction(ModifyOutputOrderAction.ID);
            if (action.isEnabled()) {
                menu.appendToGroup(GEFActionConstants.GROUP_REST, action);
            }
            List<String> orderActionIDs = ModifyConnectionOrderAction.getOrderActionIDs();
            for (String id : orderActionIDs) {
                action = getAction(id);
                if (action != null && action.isEnabled()) {
                    menu.appendToGroup(GEFActionConstants.GROUP_REST, action);
                }
            }
            MenuManager subMenu = new MenuManager(Messages.getString("TalendEditorContextMenuProvider.Row")); //$NON-NLS-1$
            menu.appendToGroup(GROUP_CONNECTIONS, subMenu);

            action = new ConnectionCreateAction(part, EConnectionType.FLOW_MAIN);
            ((ConnectionCreateAction) action).update();
            List<INodeConnector> connectors = ((ConnectionCreateAction) action).getConnectors();
            if (connectors.size() > 1) {
                for (INodeConnector connector : connectors) {
                    if (connector.isMultiSchema()) {
                        action = new ConnectionCreateAction(part, connector);
                        ((ConnectionCreateAction) action).update();
                        if (action.isEnabled() && connector.isShow()) {
                            List<String> menuList = ((ConnectionCreateAction) action).getMenuList();
                            for (int i = 0; i < menuList.size(); i++) {
                                action = new ConnectionCreateAction(part, connector);
                                ((ConnectionCreateAction) action).update();
                                ((ConnectionCreateAction) action).setText(menuList.get(i));
                                subMenu.add(action);
                            }
                        }
                    } else {
                        action = new ConnectionCreateAction(part, connector);
                        ((ConnectionCreateAction) action).update();
                        if (action.isEnabled() && connector.isShow()) {
                            ((ConnectionCreateAction) action).setText(connector.getMenuName());
                            subMenu.add(action);
                        }
                    }
                }
            } else {
                if (connectors.size() == 1) {
                    action = new ConnectionCreateAction(part, connectors.get(0));
                    ((ConnectionCreateAction) action).update();
                    if (action.isEnabled() && connectors.get(0).isShow()) {
                        List<String> menuList = ((ConnectionCreateAction) action).getMenuList();
                        for (int i = 0; i < menuList.size(); i++) {
                            action = new ConnectionCreateAction(part, connectors.get(0));
                            ((ConnectionCreateAction) action).update();
                            ((ConnectionCreateAction) action).setText(menuList.get(i));
                            subMenu.add(action);
                        }
                    }
                }
            }

            retrieveConnectors(subMenu, EConnectionType.ITERATE);
            retrieveConnectors(subMenu, EConnectionType.ROUTE);
            retrieveConnectors(subMenu, EConnectionType.ROUTE_TRY);
            retrieveConnectors(subMenu, EConnectionType.ROUTE_CATCH);
            retrieveConnectors(subMenu, EConnectionType.ROUTE_FINALLY);
            retrieveConnectors(subMenu, EConnectionType.ROUTE_ENDBLOCK);

            subMenu = new MenuManager("Link"); //$NON-NLS-1$
            menu.appendToGroup(GROUP_CONNECTIONS, subMenu);
            action = new ConnectionCreateAction(part, EConnectionType.TABLE);
            ((ConnectionCreateAction) action).update();
            if (action.isEnabled()) {
                List<String> menuList = ((ConnectionCreateAction) action).getMenuList();
                for (int i = 0; i < menuList.size(); i++) {
                    action = new ConnectionCreateAction(part, EConnectionType.TABLE);
                    ((ConnectionCreateAction) action).update();
                    ((ConnectionCreateAction) action).setText(menuList.get(i));
                    subMenu.add(action);
                }
            }

            subMenu = new MenuManager("Move to joblet"); //$NON-NLS-1$
            menu.appendToGroup(GROUP_OTHER, subMenu);
            action = getMoveToJobletAction(part, null, null);
            if (action != null) {
                ((AddToJobletAction) action).update();
                List<Node> nodeList = ((AddToJobletAction) action).getJobletNodeList();
                Map<INode, IConnection> nodeMap = ((AddToJobletAction) action).getJobletNodeMap();
                if (nodeList != null) {
                    for (Node jobletNode : nodeList) {
                        action = getMoveToJobletAction(part, jobletNode, nodeMap);
                        if (action != null) {
                            ((AddToJobletAction) action).update();
                            action.setText(jobletNode.getLabel());
                            subMenu.add(action);
                        }
                    }
                }
            }

            action = getMoveToJobAction(part);
            if (action != null) {
                ((SelectionAction) action).update();
                if (action.isEnabled()) {
                    menu.appendToGroup(GROUP_OTHER, action);
                }
            }

            subMenu = new MenuManager(Messages.getString("TalendEditorContextMenuProvider.Trigger")); //$NON-NLS-1$
            menu.appendToGroup(GROUP_CONNECTIONS, subMenu);

            /*
             * action = new ConnectionCreateAction(part, EConnectionType.RUN_BEFORE); ((ConnectionCreateAction)
             * action).update(); if (action.isEnabled()) { subMenu.add(action); }
             */

            retrieveConnectors(subMenu, EConnectionType.ON_SUBJOB_OK);

            retrieveConnectors(subMenu, EConnectionType.ON_SUBJOB_ERROR);

            subMenu.add(new Separator());

            retrieveConnectors(subMenu, EConnectionType.SYNCHRONIZE);

            retrieveConnectors(subMenu, EConnectionType.PARALLELIZE);

            subMenu.add(new Separator());

            retrieveConnectors(subMenu, EConnectionType.RUN_IF);

            retrieveConnectors(subMenu, EConnectionType.ROUTE_WHEN);

            retrieveConnectors(subMenu, EConnectionType.ROUTE_OTHER);

            retrieveConnectors(subMenu, EConnectionType.ON_COMPONENT_OK);

            retrieveConnectors(subMenu, EConnectionType.ON_COMPONENT_ERROR);

            retrieveConnectors(subMenu, EConnectionType.STARTS);

            action = new NodeBreakpointAction(part);
            ((NodeBreakpointAction) action).update();
            if (action.isEnabled()) {
                menu.appendToGroup(GEFActionConstants.GROUP_REST, action);
            }
            action = new NodeConditionalBreakpointAction(part);
            ((NodeConditionalBreakpointAction) action).update();
            if (action.isEnabled()) {
                menu.appendToGroup(GEFActionConstants.GROUP_REST, action);
            }
            action = new ShowComponentSettingViewerAction(part);
            ((ShowComponentSettingViewerAction) action).update();
            if (action.isEnabled()) {
                menu.appendToGroup(GROUP_OTHER, action);
            }

            action = new DisplaySubjobAction(part);
            ((SelectionAction) action).update();
            if (action.isEnabled()) {
                menu.appendToGroup(GROUP_OTHER, action);
            }

            if (PluginChecker.isAutoParalelPluginLoaded()) {
                action = new SetParallelizationAction(part);
                ((SelectionAction) action).update();
                if (action.isEnabled()) {
                    menu.appendToGroup(GROUP_OTHER, action);
                }

                action = new DisableParallelizationAction(part);
                ((SelectionAction) action).update();
                if (action.isEnabled()) {
                    menu.appendToGroup(GROUP_OTHER, action);
                }
            }

            // see feature 5027
            action = new ParallelExecutionAction(part);
            ((SelectionAction) action).update();
            if (PluginChecker.isTeamEdition() && action.isEnabled()) {
                menu.appendToGroup(GROUP_OTHER, action);
            }

            action = new SearchComponentAction(part);
            ((SelectionAction) action).update();
            if (action.isEnabled()) {
                menu.appendToGroup(GROUP_OTHER, action);
            }

            action = new TraceEnableAction(part);
            ((SelectionAction) action).update();
            if (action.isEnabled()) {
                menu.appendToGroup(GROUP_BREAKPOINT, action);
            }

            action = new TraceDisableAction(part);
            ((SelectionAction) action).update();
            if (action.isEnabled()) {
                menu.appendToGroup(GROUP_BREAKPOINT, action);
            }

            action = new FilterTraceColumnAction(part);
            ((SelectionAction) action).update();
            if (action.isEnabled()) {
                menu.appendToGroup(GROUP_BREAKPOINT, action);
            }
            action = new ShowBreakpointAction(part);
            ((SelectionAction) action).update();
            if (action.isEnabled()) {
                menu.appendToGroup(GROUP_BREAKPOINT, action);
            }
            action = new OpenJobHierarchyAction(part);
            ((SelectionAction) action).update();
            if (action.isEnabled()) {
                menu.appendToGroup(GROUP_OTHER, action);
            }

            action = new OpentRunJobComponentAction(part);
            ((SelectionAction) action).update();
            if (action.isEnabled()) {
                menu.appendToGroup(GEFActionConstants.GROUP_VIEW, action);
            }

            action = new BringForwardAction(part);
            ((SelectionAction) action).update();
            if (action.isEnabled()) {
                menu.appendToGroup(GEFActionConstants.GROUP_VIEW, action);
            }

            action = new BringToFrontAction(part);
            ((SelectionAction) action).update();
            if (action.isEnabled()) {
                menu.appendToGroup(GEFActionConstants.GROUP_VIEW, action);
            }

            action = new SendBackwardAction(part);
            ((SelectionAction) action).update();
            if (action.isEnabled()) {
                menu.appendToGroup(GEFActionConstants.GROUP_VIEW, action);
            }
            action = new SendToBackAction(part);
            ((SelectionAction) action).update();
            if (action.isEnabled()) {
                menu.appendToGroup(GEFActionConstants.GROUP_VIEW, action);
            }

            List<SelectionAction> instances = CustomExternalActions.getInstances(part);
            for (SelectionAction selectionAction : instances) {
                selectionAction.update();
                if (selectionAction.isEnabled()) {
                    menu.appendToGroup(GEFActionConstants.GROUP_VIEW, selectionAction);
                }
            }

        }
    }

    private void retrieveConnectors(MenuManager subMenu, EConnectionType connType) {
        if (connType != null) {
            ConnectionCreateAction action = new ConnectionCreateAction(part, connType);
            action.update();
            List<INodeConnector> connectors = action.getConnectors();
            for (INodeConnector connector : connectors) {
                action = new ConnectionCreateAction(part, connector);
                action.update();
                if (action.isEnabled()) {
                    action.setText(connector.getMenuName());
                    subMenu.add(action);
                }
            }
        }
    }

    private IAction getAction(final String actionId) {
        return actionRegistry.getAction(actionId);
    }

    /**
     * Getter for enableContextMenu.
     *
     * @return the enableContextMenu
     */
    public static boolean isEnableContextMenu() {
        return enableContextMenu;
    }

    /**
     * Sets the enableContextMenu.
     *
     * @param enableContextMenu the enableContextMenu to set
     */
    public static void setEnableContextMenu(boolean enableContextMenu) {
        TalendEditorContextMenuProvider.enableContextMenu = enableContextMenu;
    }

    public SelectionAction getMoveToJobletAction(IWorkbenchPart part, Node jobletNode, Map<INode, IConnection> nodeMap) {
        if (PluginChecker.isJobLetPluginLoaded()) {
            IJobletProviderService service = (IJobletProviderService) GlobalServiceRegister.getDefault().getService(
                    IJobletProviderService.class);
            if (service != null) {
                Action moveToJobletAction = service.getMoveToJobletAction(part, jobletNode, nodeMap);
                if (moveToJobletAction instanceof SelectionAction) {
                    return (SelectionAction) moveToJobletAction;
                }
            }
        }
        return null;
    }

    public SelectionAction getMoveToJobAction(IWorkbenchPart part) {
        if (PluginChecker.isJobLetPluginLoaded()) {
            IJobletProviderService service = (IJobletProviderService) GlobalServiceRegister.getDefault().getService(
                    IJobletProviderService.class);
            if (service != null) {
                Action moveToJobAction = service.getMoveToJobAction(part);
                if (moveToJobAction instanceof SelectionAction) {
                    return (SelectionAction) moveToJobAction;
                }
            }
        }
        return null;
    }
}
