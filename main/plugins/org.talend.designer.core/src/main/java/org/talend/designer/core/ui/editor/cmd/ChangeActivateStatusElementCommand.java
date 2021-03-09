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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.gef.commands.Command;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.IConnectionCategory;
import org.talend.core.model.process.INode;
import org.talend.core.service.IMRProcessService;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.ui.editor.connections.Connection;
import org.talend.designer.core.ui.editor.jobletcontainer.AbstractJobletContainer;
import org.talend.designer.core.ui.editor.nodecontainer.NodeContainer;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.process.Process;

/**
 * Command that will set or remove the start status on a node. <br/>
 *
 * $Id: ChangeActivateStatusNodeCommand.java 3351 2007-05-04 12:14:00 +0000 (ven., 04 mai 2007) plegall $
 *
 */
public class ChangeActivateStatusElementCommand extends Command {

    private final List<Node> nodeList;

    private List<Connection> connectionList;

    private boolean value;

    /**
     * Gives the node where the status will be set or removed.
     *
     * @param connectionList
     *
     * @param newStartNode
     */
    public ChangeActivateStatusElementCommand(boolean activate, List<Node> nodeList, List<Connection> connectionList) {
        this.nodeList = nodeList;
        this.connectionList = connectionList;
        value = activate;
        if (activate) {
            setLabel(Messages.getString("ChangeActivateStatusNodeCommand.Label.Active")); //$NON-NLS-1$
        } else {
            setLabel(Messages.getString("ChangeActivateStatusNodeCommand.Label.Deactive")); //$NON-NLS-1$
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
        if (GlobalServiceRegister.getDefault().isServiceRegistered(IMRProcessService.class)) {
            IMRProcessService mrService = (IMRProcessService) GlobalServiceRegister.getDefault()
                    .getService(IMRProcessService.class);
            if (mrService != null) {
                List<INode> mrNodeList = new ArrayList<INode>();
                for (Node node : nodeList) {
                    if (node.isMapReduceStart() && !mrNodeList.contains(node)) {
                        mrNodeList.add(node);
                    }
                }
                mrService.refreshMRStatus(mrNodeList);
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute() {

        Process process = null;
        if (nodeList != null && nodeList.size() > 0) {
            process = (Process) nodeList.get(0).getProcess();
        } else if (connectionList != null && connectionList.size() != 0) {
            process = (Process) connectionList.get(0).getSource().getProcess();
        }
        process.setActivate(false);
        for (Connection connection : connectionList) {
            connection.setPropertyValue(EParameterName.ACTIVATE.getName(), value);
        }
        List uniqueNameList = new ArrayList();
        for (Node node : nodeList) {
            uniqueNameList.add(node.getUniqueName());
            if (isSameSchemaInputOutput(node)) {
                node.setPropertyValue(EParameterName.DUMMY.getName(), !value);
            }
            node.setPropertyValue(EParameterName.ACTIVATE.getName(), value);
        }

        for (Connection connection : connectionList) {
            connection.updateAllId();
        }

        if (nodeList != null && nodeList.size() > 0) {
            List<INode> updatedSourceNode = new ArrayList<INode>();
            for (Node node : nodeList) {
                for (IConnection connection : node.getIncomingConnections()) {
                    INode source = connection.getSource();
                    if (!updatedSourceNode.contains(source)) {
                        updatedSourceNode.add(source);
                        source.getOutgoingConnections().get(0).updateAllId();
                    }
                }
            }
        }

        dummyMiddleElement(false);

        process.setActivate(true);
        process.checkStartNodes();
        process.checkProcess();

        refreshPropertyView();
        refreshMRStatus();
    }

    private boolean isSameSchemaInputOutput(Node node) {
        boolean hasInput = false;
        IMetadataTable inputMetadata = null;
        for (Connection connection : (List<Connection>) node.getIncomingConnections()) {
            if (connection.getLineStyle().hasConnectionCategory(IConnectionCategory.FLOW)) {
                if (hasInput) {
                    // if the flag is already set, that means that there is more than one input link
                    // so don't accept the dummy
                    return false;
                }
                hasInput = true;
                inputMetadata = connection.getMetadataTable();
            }
        }

        if (!hasInput) {
            return false;
        }

        for (Connection outputConnection : (List<Connection>) node.getOutgoingConnections()) {
            if (outputConnection.getLineStyle().hasConnectionCategory(IConnectionCategory.FLOW)) {
                IMetadataTable outputMeta = outputConnection.getMetadataTable();
                if (!inputMetadata.sameMetadataAs(outputMeta,
                        IMetadataColumn.OPTIONS_IGNORE_KEY | IMetadataColumn.OPTIONS_IGNORE_NULLABLE
                                | IMetadataColumn.OPTIONS_IGNORE_COMMENT | IMetadataColumn.OPTIONS_IGNORE_PATTERN
                                | IMetadataColumn.OPTIONS_IGNORE_DBCOLUMNNAME | IMetadataColumn.OPTIONS_IGNORE_DBTYPE)) {
                    // input and output schema is different, so don't accept dummy
                    return false;
                }
            }
        }
        return true;
    }

    private boolean hasActivateInputOutput(Node node) {
        boolean hasActivateInput = false;
        for (Connection connection : (List<Connection>) node.getIncomingConnections()) {
            if (connection.isActivate() && connection.getLineStyle().hasConnectionCategory(IConnectionCategory.FLOW)) {
                hasActivateInput = true;
                break;
            }
        }

        boolean hasActivateOutput = false;
        for (Connection outputConnection : (List<Connection>) node.getOutgoingConnections()) {
            if (outputConnection.isActivate()
                    && outputConnection.getLineStyle().hasConnectionCategory(IConnectionCategory.FLOW)) {
                hasActivateOutput = true;
            }
        }
        return hasActivateOutput && hasActivateInput;
    }

    @Override
    public void undo() {
        Process process = null;
        if (nodeList != null && nodeList.size() > 0) {
            process = (Process) nodeList.get(0).getProcess();
        } else if (connectionList != null && connectionList.size() != 0) {
            process = (Process) connectionList.get(0).getSource().getProcess();
        }
        process.setActivate(false);

        dummyMiddleElement(true);
        for (Node node : nodeList) {
            if (isSameSchemaInputOutput(node)) {
                node.setPropertyValue(EParameterName.DUMMY.getName(), value);
            }
            node.setPropertyValue(EParameterName.ACTIVATE.getName(), !value);
        }
        for (Connection connection : connectionList) {
            connection.setPropertyValue(EParameterName.ACTIVATE.getName(), !value);
        }

        for (Connection connection : connectionList) {
            connection.updateAllId();
        }

        if (nodeList != null && nodeList.size() > 0) {
            List<INode> updatedSourceNode = new ArrayList<INode>();
            for (Node node : nodeList) {
                for (IConnection connection : node.getIncomingConnections()) {
                    INode source = connection.getSource();
                    if (!updatedSourceNode.contains(source)) {
                        updatedSourceNode.add(source);
                        source.getOutgoingConnections().get(0).updateAllId();
                    }
                }
            }
        }

        process.setActivate(true);
        process.checkStartNodes();
        process.checkProcess();
        refreshPropertyView();
        refreshMRStatus();
    }

    @Override
    public void redo() {
        this.execute();
    }

    private void dummyMiddleElement(boolean undo) {

        Map<List<INode>, List<IConnection>> middConnMap = getAllMiddleConnections();
        Set<Entry<List<INode>, List<IConnection>>> middSet = middConnMap.entrySet();
        Iterator<Entry<List<INode>, List<IConnection>>> middIte = middSet.iterator();
        List<IConnection> notSameForConn = new ArrayList<IConnection>();
        List<INode> notSameForNode = new ArrayList<INode>();
        List<Node> connNodeList = new ArrayList<Node>();
        while (middIte.hasNext()) {
            Entry<List<INode>, List<IConnection>> entry = middIte.next();
            List<INode> nodeList = entry.getKey();
            List<IConnection> connList = entry.getValue();
            if (nodeList.size() == 2) {
                INode firNode = nodeList.get(0);
                INode secNode = nodeList.get(1);
                if (!notSameForNode.contains(firNode)) {
                    notSameForNode.add(firNode);
                }
                if (!notSameForNode.contains(secNode)) {
                    notSameForNode.add(secNode);
                }
                for (IConnection conn : connList) {
                    if (!notSameForConn.contains(conn)) {
                        notSameForConn.add(conn);
                    } else {
                        continue;
                    }
                    Node source = (Node) conn.getSource();
                    if (source.getUniqueName() != firNode.getUniqueName()
                            && (source.getUniqueName() != secNode.getUniqueName())) {
                        if (isSameSchemaInputOutput(source)) {
                            conn.setPropertyValue(EParameterName.ACTIVATE.getName(), true);
                            source.setPropertyValue(EParameterName.DUMMY.getName(), true);
                            // source.setPropertyValue(EParameterName.ACTIVATE.getName(), source.isActivate());
                            connNodeList.add(source);
                        }
                    }

                    Node target = (Node) conn.getTarget();
                    if (target.getUniqueName() != firNode.getUniqueName()
                            && (target.getUniqueName() != secNode.getUniqueName())) {
                        if (isSameSchemaInputOutput(target)) {
                            conn.setPropertyValue(EParameterName.ACTIVATE.getName(), true);
                            target.setPropertyValue(EParameterName.DUMMY.getName(), true);
                            // target.setPropertyValue(EParameterName.ACTIVATE.getName(), target.isActivate());
                            connNodeList.add(target);
                        }
                    }
                }

            }

        }

        for (Node node : connNodeList) {
            node.setPropertyValue(EParameterName.ACTIVATE.getName(), node.isActivate());
        }

        if (!value) {
            for (Node node : nodeList) {
                if (node.isActivate()) {
                    continue;
                }

                Map<IConnection, Node> outMiddleNodes = getAllOutMiddleNodes(node);
                Map<IConnection, Node> inMiddleNodes = getAllInMiddleNodes(node);

                Set<Entry<IConnection, Node>> outSet = outMiddleNodes.entrySet();
                Iterator<Entry<IConnection, Node>> outIte = outSet.iterator();
                while (outIte.hasNext()) {
                    Entry<IConnection, Node> en = outIte.next();
                    IConnection enConn = en.getKey();
                    if (!notSameForConn.contains(enConn)) {
                        // if (enConn.isActivate()) {
                        enConn.setPropertyValue(EParameterName.ACTIVATE.getName(), value);
                        if (!notSameForNode.contains(enConn.getTarget()) && !hasActivateInputOutput((Node) enConn.getTarget())) {
                            enConn.getTarget().setPropertyValue(EParameterName.DUMMY.getName(), value);
                        }
                        // }
                    }

                }

                Set<Entry<IConnection, Node>> inSet = inMiddleNodes.entrySet();
                Iterator<Entry<IConnection, Node>> inIte = inSet.iterator();
                while (inIte.hasNext()) {
                    Entry<IConnection, Node> en = inIte.next();
                    IConnection enConn = en.getKey();
                    if (!notSameForConn.contains(enConn)) {
                        // if (enConn.isActivate()) {
                        enConn.setPropertyValue(EParameterName.ACTIVATE.getName(), value);
                        if (!notSameForNode.contains(enConn.getSource()) && !hasActivateInputOutput((Node) enConn.getSource())) {
                            enConn.getSource().setPropertyValue(EParameterName.DUMMY.getName(), value);
                        }
                        // }
                    }

                }
            }
        }

    }

    private Map<IConnection, Node> getAllOutMiddleNodes(INode node) {
        Map<IConnection, Node> middleNodeMap = new HashMap<IConnection, Node>();
        for (IConnection outConn : node.getOutgoingConnections()) {
            if (outConn.getLineStyle().hasConnectionCategory(IConnectionCategory.FLOW)) {

                if (!((Node) outConn.getTarget()).isActivate()) {
                    middleNodeMap.put(outConn, null);
                    if (isSameSchemaInputOutput((Node) outConn.getTarget())) {
                        middleNodeMap.putAll(getAllOutMiddleNodes(outConn.getTarget()));
                    }
                } else {
                    middleNodeMap.put(outConn, (Node) outConn.getTarget());
                }
            }
        }
        return middleNodeMap;
    }

    private Map<IConnection, Node> getAllInMiddleNodes(INode node) {
        Map<IConnection, Node> middleNodeMap = new HashMap<IConnection, Node>();
        for (IConnection inConn : node.getIncomingConnections()) {
            if (inConn.getLineStyle().hasConnectionCategory(IConnectionCategory.FLOW)) {
                if (!((Node) inConn.getSource()).isActivate()) {
                    middleNodeMap.put(inConn, null);

                    if (isSameSchemaInputOutput((Node) inConn.getSource())) {
                        middleNodeMap.putAll(getAllInMiddleNodes(inConn.getSource()));
                    }
                } else {
                    middleNodeMap.put(inConn, (Node) inConn.getSource());
                }
            }
        }
        return middleNodeMap;
    }

    private Map<List<INode>, List<IConnection>> getAllMiddleConnections() {
        Map<List<INode>, List<IConnection>> middConnMap = new HashMap<List<INode>, List<IConnection>>();
        Process process;
        if (nodeList.size() > 0) {
            process = (Process) nodeList.get(0).getProcess();
        } else {
            process = (Process) connectionList.get(0).getSource().getProcess();
        }
        List<? extends INode> nodes = process.getGraphicalNodes();
        List<INode> jobletandnodeList = new ArrayList<INode>();
        for (INode node : nodes) {
            if (((Node) node).isJoblet() && !((Node) node).getNodeContainer().isCollapsed()
                    && (((Node) node).getNodeContainer() instanceof AbstractJobletContainer)) {
                for (NodeContainer nc : ((AbstractJobletContainer) ((Node) node).getNodeContainer()).getNodeContainers()) {
                    jobletandnodeList.add(nc.getNode());
                }
            } else {
                jobletandnodeList.add(node);
            }
        }
        for (INode node : jobletandnodeList) {

            if (node.isActivate()) {
                // MiddleNodes Map<IConnection, Node> connection->Node
                // if Node deactivate then connection->null
                Map<IConnection, Node> outMiddleNodes = getAllOutMiddleNodes(node);
                Map<IConnection, Node> inMiddleNodes = getAllInMiddleNodes(node);

                Set<Entry<IConnection, Node>> outSet = outMiddleNodes.entrySet();
                Iterator<Entry<IConnection, Node>> outIte = outSet.iterator();
                boolean haveActivateTarget = false;
                List<INode> nodeList = new ArrayList<INode>();
                List<IConnection> connList = new ArrayList<IConnection>();
                while (outIte.hasNext()) {
                    Entry<IConnection, Node> en = outIte.next();
                    Node enNode = en.getValue();
                    IConnection enConn = en.getKey();
                    if (enNode != null) {
                        haveActivateTarget = true;
                        if (!nodeList.contains(node)) {
                            nodeList.add(node);
                        }
                        if (!nodeList.contains(enNode)) {
                            nodeList.add(enNode);
                        }
                    }
                    if (enConn != null && !connList.contains(enConn)) {
                        connList.add(enConn);
                    }
                }
                if (!haveActivateTarget) {
                    outMiddleNodes.clear();
                }
                if (!nodeList.isEmpty() && !connList.isEmpty()) {
                    Set<Entry<List<INode>, List<IConnection>>> middSet = middConnMap.entrySet();
                    Iterator<Entry<List<INode>, List<IConnection>>> middIte = middSet.iterator();
                    boolean exist = false;
                    while (middIte.hasNext()) {
                        Entry<List<INode>, List<IConnection>> entry = middIte.next();
                        List<INode> enNodeList = entry.getKey();
                        if (enNodeList.size() == 2 && nodeList.size() == 2) {
                            if (enNodeList.get(0).getUniqueName().equals(nodeList.get(1).getUniqueName())
                                    && enNodeList.get(1).getUniqueName().equals(nodeList.get(0).getUniqueName())) {
                                exist = true;
                            } else if (enNodeList.get(0).getUniqueName().equals(nodeList.get(0).getUniqueName())
                                    && enNodeList.get(1).getUniqueName().equals(nodeList.get(1).getUniqueName())) {
                                exist = true;
                            }
                        }
                    }
                    if (!exist) {
                        List<IConnection> exactConnections = getExactConnectionsBetweenNodes(node, nodeList, connList, true);
                        middConnMap.put(nodeList, exactConnections);
                    }
                }

                Set<Entry<IConnection, Node>> inSet = inMiddleNodes.entrySet();
                Iterator<Entry<IConnection, Node>> inIte = inSet.iterator();
                boolean haveActivateSource = false;
                nodeList = new ArrayList<INode>();
                connList = new ArrayList<IConnection>();
                while (inIte.hasNext()) {
                    Entry<IConnection, Node> en = inIte.next();
                    Node enNode = en.getValue();
                    IConnection enConn = en.getKey();
                    if (enNode != null) {
                        haveActivateSource = true;
                        if (!nodeList.contains(node)) {
                            nodeList.add(node);
                        }
                        if (!nodeList.contains(enNode)) {
                            nodeList.add(enNode);
                        }
                    }
                    if (enConn != null && !connList.contains(enConn)) {
                        connList.add(enConn);
                    }
                }
                if (!haveActivateSource) {
                    inMiddleNodes.clear();
                }
                if (!nodeList.isEmpty() && !connList.isEmpty()) {
                    Set<Entry<List<INode>, List<IConnection>>> middSet = middConnMap.entrySet();
                    Iterator<Entry<List<INode>, List<IConnection>>> middIte = middSet.iterator();
                    boolean exist = false;
                    while (middIte.hasNext()) {
                        Entry<List<INode>, List<IConnection>> entry = middIte.next();
                        List<INode> enNodeList = entry.getKey();
                        if (enNodeList.size() == 2 && nodeList.size() == 2) {
                            if (enNodeList.get(0).getUniqueName().equals(nodeList.get(1).getUniqueName())
                                    && enNodeList.get(1).getUniqueName().equals(nodeList.get(0).getUniqueName())) {
                                exist = true;
                            } else if (enNodeList.get(0).getUniqueName().equals(nodeList.get(0).getUniqueName())
                                    && enNodeList.get(1).getUniqueName().equals(nodeList.get(1).getUniqueName())) {
                                exist = true;
                            }
                        }
                    }
                    if (!exist) {
                        List<IConnection> exactConnections = getExactConnectionsBetweenNodes(node, nodeList, connList, false);
                        middConnMap.put(nodeList, exactConnections);
                    }
                }
            }
        }
        // middConnMap=> key(BaseNode, Node1, Node2......);
        // BaseNode->(0/some deactivate node)->Node1;BaseNode->(0/some deactivate node)->Node2;
        // middConnMap=> value(connections between Node1 and Node2).
        return middConnMap;
    }

    public static List<IConnection> getExactConnectionsBetweenNodes(INode node, List<INode> nodeList, List<IConnection> connList,
            boolean isOutgoing) {
        List<IConnection> exactList = new ArrayList<IConnection>(connList);
        List<INode> targetNodeList = new ArrayList<INode>(nodeList);
        targetNodeList.remove(node);
        Map<INode, Set<INode>> pathhm = new HashMap<INode, Set<INode>>();
        for (INode targetNode : targetNodeList) {
            Set<INode> pathNodes = new HashSet<INode>();
            pathNodes.add(node);
            // Got the passby Node between node and tatgetNode
            boolean pathFound = getPathsNodes(node, targetNode, pathNodes, isOutgoing);
            if (pathFound) {
                pathhm.put(targetNode, pathNodes);
            }
        }

        Collection<Set<INode>> pathSets = pathhm.values();
        if (!pathSets.isEmpty()) {
            Iterator<IConnection> connectionIte = exactList.iterator();
            while (connectionIte.hasNext()) {
                IConnection connection = (IConnection) connectionIte.next();

                boolean rightIn = false;

                Iterator<Set<INode>> pathIte = pathSets.iterator();
                while (pathIte.hasNext()) {
                    Set<INode> nodes = (Set<INode>) pathIte.next();
                    // if sourceNode and targetNode of the connection don't exist in the NodePath then this connection
                    // doesn't belong to this flow of BaseNode->(0/some deactivate node)->Node1
                    // maybe belongs to other flow of BaseNode but all flow deactivate.
                    if (nodes.contains(connection.getSource()) && nodes.contains(connection.getTarget())) {
                        rightIn = true;
                    }
                }

                if (!rightIn) {
                    connectionIte.remove();
                }

            }
        }

        return exactList;
    }

    private static boolean getPathsNodes(INode node, INode targetNode, Set<INode> nodes, boolean isOutgoing) {
        List<? extends IConnection> connections = null;
        if (node.equals(targetNode)) {
            return true;
        }
        if (isOutgoing) {
            connections = node.getOutgoingConnections();
        } else {
            connections = node.getIncomingConnections();
        }
        for (IConnection connection : connections) {
            INode deepNode = null;
            if (isOutgoing) {
                deepNode = connection.getTarget();
            } else {
                deepNode = connection.getSource();
            }
            boolean flag = getPathsNodes(deepNode, targetNode, nodes, isOutgoing);
            if (flag) {
                nodes.add(deepNode);
                return true;
            }
        }
        return false;
    }
}
