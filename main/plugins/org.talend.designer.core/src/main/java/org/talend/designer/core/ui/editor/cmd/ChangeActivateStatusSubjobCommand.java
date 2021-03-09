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
package org.talend.designer.core.ui.editor.cmd;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.gef.commands.Command;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.INode;
import org.talend.core.service.IMRProcessService;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.ui.editor.nodecontainer.NodeContainer;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.process.Process;

/**
 * Command that will set or remove the start status on a node. <br/>
 *
 * $Id: ChangeActivateStatusNodeCommand.java 3351 2007-05-04 12:14:00 +0000 (ven., 04 mai 2007) plegall $
 *
 */
public class ChangeActivateStatusSubjobCommand extends Command {

    private Node node;

    private boolean value;

    private boolean oneComponent;

    /**
     * Gives the node where the status will be set or removed.
     *
     * @param newStartNode
     */
    public ChangeActivateStatusSubjobCommand(Node node, boolean oneComponent) {
        this.node = node;
        this.oneComponent = oneComponent;
        if (node.isStart()) {
            if (!oneComponent) {
                if (node.isActivate()) {
                    value = false;
                    setLabel(Messages.getString("ChangeActivateStatusSubjobCommand.Label.DeactiveComplete2")); //$NON-NLS-1$
                } else {
                    value = true;
                    setLabel(Messages.getString("ChangeActivateStatusSubjobCommand.Label.ActiveComplete2")); //$NON-NLS-1$
                }
            } else {
                if (node.isActivate()) {
                    value = false;
                    setLabel(Messages.getString("ChangeActivateStatusSubjobCommand.Label.DeactivePart2")); //$NON-NLS-1$
                } else {
                    value = true;
                    setLabel(Messages.getString("ChangeActivateStatusSubjobCommand.Label.ActivePart2")); //$NON-NLS-1$
                }
            }

        } else {
            if (node.isActivate()) {
                value = false;
                setLabel(Messages.getString("ChangeActivateStatusSubjobCommand.Label.DeactivePart2")); //$NON-NLS-1$
            } else {
                value = true;
                setLabel(Messages.getString("ChangeActivateStatusSubjobCommand.Label.ActivePart2")); //$NON-NLS-1$
            }
        }
    }

    private void refreshPropertyView() {
        // IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        // IViewPart view = page.findView("org.eclipse.ui.views.PropertySheet"); //$NON-NLS-1$
        // PropertySheet sheet = (PropertySheet) view;
        // if (sheet.getCurrentPage() instanceof TabbedPropertySheetPage) {
        // TabbedPropertySheetPage tabbedPropertySheetPage = (TabbedPropertySheetPage) sheet.getCurrentPage();
        // if (tabbedPropertySheetPage.getCurrentTab() != null) {
        // tabbedPropertySheetPage.refresh();
        // }
        // }
    }

    private void refreshMRStatus() {
        Process process = (Process) node.getProcess();
        if (GlobalServiceRegister.getDefault().isServiceRegistered(IMRProcessService.class)) {
            IMRProcessService mrService = (IMRProcessService) GlobalServiceRegister.getDefault()
                    .getService(IMRProcessService.class);
            if (mrService != null) {
                List<INode> mrNodeList = new ArrayList<INode>();
                for (INode node : process.getGraphicalNodes()) {
                    if ((((Node) node).isMapReduceStart()) && !mrNodeList.contains(node)) {
                        mrNodeList.add(node);
                    }
                }
                mrService.refreshMRStatus(mrNodeList);
            }
        }
    }

    @Override
    public void execute() {
        Process process = (Process) node.getProcess();
        process.setActivateSubjob(node, value, oneComponent);

        List<Node> nodeList = new ArrayList<Node>();
        List<NodeContainer> nodeContainers = node.getNodeContainer().getSubjobContainer().getNodeContainers();
        for (NodeContainer container : nodeContainers) {
            nodeList.add(container.getNode());
        }
        for (Node nodeInSubjob : nodeList) {
            for (IConnection connection : nodeInSubjob.getIncomingConnections()) {
                INode source = connection.getSource();
                if (!nodeList.contains(source)) {
                    source.getOutgoingConnections().get(0).updateAllId();
                }
            }
        }

        process.checkStartNodes();
        process.checkProcess();
        refreshPropertyView();
        refreshMRStatus();
    }

    @Override
    public void undo() {
        Process process = (Process) node.getProcess();
        process.setActivateSubjob(node, !value, oneComponent);

        List<Node> nodeList = new ArrayList<Node>();
        List<NodeContainer> nodeContainers = node.getNodeContainer().getSubjobContainer().getNodeContainers();
        for (NodeContainer container : nodeContainers) {
            nodeList.add(container.getNode());
        }
        for (Node nodeInSubjob : nodeList) {
            for (IConnection connection : nodeInSubjob.getIncomingConnections()) {
                INode source = connection.getSource();
                if (!nodeList.contains(source)) {
                    source.getOutgoingConnections().get(0).updateAllId();
                }
            }
        }

        process.checkStartNodes();
        process.checkProcess();
        refreshPropertyView();
        refreshMRStatus();
    }

    @Override
    public void redo() {
        this.execute();
    }
}
