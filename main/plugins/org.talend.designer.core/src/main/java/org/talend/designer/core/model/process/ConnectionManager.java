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
package org.talend.designer.core.model.process;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.talend.core.GlobalServiceRegister;
import org.talend.core.PluginChecker;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.process.EConnectionType;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.IConnectionCategory;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.INodeConnector;
import org.talend.core.ui.IJobletProviderService;
import org.talend.core.utils.KeywordsValidator;
import org.talend.designer.core.ConnectionValidatorManager;
import org.talend.designer.core.IConnectionValidator;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.ui.editor.connections.Connection;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.process.Process;

/**
 * DOC nrousseau class global comment. Detailled comment <br/>
 *
 */
public class ConnectionManager {

    private static EConnectionType newlineStyle;

    /**
     *
     * Will return true if the connection can connect or not between source & target.
     *
     * @param source
     * @param target
     * @param connType
     * @param connectionName
     * @return
     */
    private static boolean canConnect(INode source, INode target, EConnectionType connType, String connectionName) {
        if (source.equals(target)) {
            return false;
        }
        if (!target.isActivate() || !source.isActivate()) {
            return false;
        }

        boolean skipSameProcessTest = false;
        if (newlineStyle.equals(EConnectionType.FLOW_MAIN)) {
            int nbMain = 0;
            for (IConnection connec : target.getIncomingConnections()) {
                if (connec.getLineStyle().equals(EConnectionType.FLOW_MAIN)) {
                    nbMain++;
                }
            }
            int maxFlowInput = 0;
            if (target.getConnectorFromName(EConnectionType.FLOW_MAIN.getName()) != null) {
                maxFlowInput = target.getConnectorFromName(EConnectionType.FLOW_MAIN.getName()).getMaxLinkInput();
            }
            if (maxFlowInput > 1 && nbMain >= 1 && (nbMain <= maxFlowInput || maxFlowInput == -1)) {
                // if the component accept several connections on the input, all inputs must come from the same process
                boolean isExtensionComponent = false;
                AbstractProcessProvider findProcessProviderFromPID = AbstractProcessProvider
                        .findProcessProviderFromPID(IComponent.JOBLET_PID);
                if (findProcessProviderFromPID != null) {
                    isExtensionComponent = findProcessProviderFromPID.isExtensionComponent(target);
                }
                if (!isExtensionComponent && !source.sameProcessAs(target, false)) {
                    return false;
                }
                skipSameProcessTest = true;
            }
        }

        if (!skipSameProcessTest && source.sameProcessAs(target, false)) {
            return false;
        }

        // Check existing connections to avoid to have more than one link
        // no matter the type of the connection and the direction
        List<Connection> connections = new ArrayList<Connection>((List<Connection>) source.getOutgoingConnections());

        connections.removeAll(source.getOutgoingConnections(EConnectionType.FLOW_MAIN));

        // connections = source.getOutgoingConnections();
        for (int i = 0; i < connections.size(); i++) {
            if ((connections.get(i)).getTarget().equals(target)) {
                return false;
            }
        }

        connections = new ArrayList<Connection>((List<Connection>) source.getIncomingConnections());

        connections.removeAll(source.getIncomingConnections(EConnectionType.FLOW_MAIN));

        // connections = source.getIncomingConnections();
        for (int i = 0; i < connections.size(); i++) {
            if ((connections.get(i)).getSource().equals(target)) {
                return false;
            }
        }
        if (connType.hasConnectionCategory(IConnectionCategory.DEPENDENCY)) {
            if (!(Boolean) target.getPropertyValue(EParameterName.STARTABLE.getName())) {
                return false;
            }
            boolean isJoblet = false;
            if (PluginChecker.isJobLetPluginLoaded()) {
                IJobletProviderService service = (IJobletProviderService) GlobalServiceRegister.getDefault().getService(
                        IJobletProviderService.class);
                if (service != null) {
                    if (service.isJobletComponent(target) && !connType.hasConnectionCategory(IConnectionCategory.FLOW)) {
                        List<INodeConnector> freeTriggerBuiltConnectors = service.getFreeTriggerBuiltConnectors(target, connType,
                                true);
                        if (freeTriggerBuiltConnectors.isEmpty()) {
                            return false;
                        }
                        isJoblet = true;
                    }

                    // for bug 10973
                    if (service.isTriggerNode(target) && target.getIncomingConnections() != null
                            && target.getIncomingConnections().size() >= 1) {
                        return false;
                    }
                }
            }

            if (!isJoblet && !target.isELTComponent() && !target.isSubProcessStart()) {
                return false;
            }
        }
        connections = (List<Connection>) target.getIncomingConnections();
        for (int i = 0; i < connections.size(); i++) {
            if (connType == EConnectionType.TABLE || connType == EConnectionType.TABLE_REF) {
                if ((connections.get(i)).isActivate()) {
                    if ((connections.get(i)).getName().equals(connectionName)) {
                        return false;
                    }
                }
            }
        }
        boolean targetHasHashLinks = ((Process) target.getProcess()).isThereLinkWithHash(target)
                | newlineStyle.hasConnectionCategory(IConnectionCategory.USE_HASH);
        if (connType.hasConnectionCategory(IConnectionCategory.CONDITION)) {
            if (targetHasHashLinks) {
                return false;
            }
        }
        if (targetHasHashLinks && source.hasRunIfLink()) {
            return false;
        }

        // if (testIfNoStartAfterAddConnection(source, target)) {
        // return false;
        // }

        return true;
    }

    private static boolean canConnect(INode source, INode target, EConnectionType connType, String connectionName,
            boolean refactorJoblet) {
        if (source.equals(target)) {
            return false;
        }
        if (!refactorJoblet && (!target.isActivate() || !source.isActivate())) {
            return false;
        }

        boolean skipSameProcessTest = false;
        if (newlineStyle.equals(EConnectionType.FLOW_MAIN)) {
            int nbMain = 0;
            for (IConnection connec : target.getIncomingConnections()) {
                if (connec.getLineStyle().equals(EConnectionType.FLOW_MAIN)) {
                    nbMain++;
                }
            }
            int maxFlowInput = 0;
            if (target.getConnectorFromName(EConnectionType.FLOW_MAIN.getName()) != null) {
                maxFlowInput = target.getConnectorFromName(EConnectionType.FLOW_MAIN.getName()).getMaxLinkInput();
            }
            if (maxFlowInput > 1 && nbMain >= 1 && (nbMain <= maxFlowInput || maxFlowInput == -1)) {
                // if the component accept several connections on the input, all inputs must come from the same process
                boolean isExtensionComponent = false;
                AbstractProcessProvider findProcessProviderFromPID = AbstractProcessProvider
                        .findProcessProviderFromPID(IComponent.JOBLET_PID);
                if (findProcessProviderFromPID != null) {
                    isExtensionComponent = findProcessProviderFromPID.isExtensionComponent(target);
                }
                if (!isExtensionComponent && !source.sameProcessAs(target, false)) {
                    return false;
                }
                skipSameProcessTest = true;
            }
        }

        if (!skipSameProcessTest && source.sameProcessAs(target, false)) {
            return false;
        }

        // limit the use of the tUnite, avoid a conflict in case source link is in a merge part, and target use merge.
        if (!source.getLinkedMergeInfo().isEmpty()
                && (!target.getLinkedMergeInfo().isEmpty() || target.getComponent().useMerge())) {
            return false;
        }

        // Check existing connections to avoid to have more than one link
        // no matter the type of the connection and the direction
        List<Connection> connections = new ArrayList<Connection>((List<Connection>) source.getOutgoingConnections());

        connections.removeAll(source.getOutgoingConnections(EConnectionType.FLOW_MAIN));

        // connections = source.getOutgoingConnections();
        for (int i = 0; i < connections.size(); i++) {
            if ((connections.get(i)).getTarget().equals(target)) {
                return false;
            }
        }

        connections = new ArrayList<Connection>((List<Connection>) source.getIncomingConnections());

        connections.removeAll(source.getIncomingConnections(EConnectionType.FLOW_MAIN));

        // connections = source.getIncomingConnections();
        for (int i = 0; i < connections.size(); i++) {
            if ((connections.get(i)).getSource().equals(target)) {
                return false;
            }
        }
        if (connType.hasConnectionCategory(IConnectionCategory.DEPENDENCY)) {
            if (!(Boolean) target.getPropertyValue(EParameterName.STARTABLE.getName())) {
                return false;
            }
            boolean isJoblet = false;
            if (PluginChecker.isJobLetPluginLoaded()) {
                IJobletProviderService service = (IJobletProviderService) GlobalServiceRegister.getDefault().getService(
                        IJobletProviderService.class);
                if (service != null) {
                    if (service.isJobletComponent(target) && !connType.hasConnectionCategory(IConnectionCategory.FLOW)) {
                        List<INodeConnector> freeTriggerBuiltConnectors = service.getFreeTriggerBuiltConnectors(target, connType,
                                true);
                        if (freeTriggerBuiltConnectors.isEmpty()) {
                            return false;
                        }
                        isJoblet = true;
                    }

                    // for bug 10973
                    if (service.isTriggerNode(target) && target.getIncomingConnections() != null
                            && target.getIncomingConnections().size() >= 1) {
                        return false;
                    }
                }
            }

            if (!isJoblet && !target.isELTComponent() && !target.isSubProcessStart()) {
                return false;
            }
        }
        connections = (List<Connection>) target.getIncomingConnections();
        for (int i = 0; i < connections.size(); i++) {
            if (connType == EConnectionType.TABLE || connType == EConnectionType.TABLE_REF) {
                if ((connections.get(i)).isActivate()) {
                    if ((connections.get(i)).getName().equals(connectionName)) {
                        return false;
                    }
                }
            }
        }
        boolean targetHasHashLinks = ((Process) target.getProcess()).isThereLinkWithHash(target)
                | newlineStyle.hasConnectionCategory(IConnectionCategory.USE_HASH);
        if (connType.hasConnectionCategory(IConnectionCategory.CONDITION)) {
            if (targetHasHashLinks) {
                return false;
            }
        }
        if (targetHasHashLinks && source.hasRunIfLink()) {
            return false;
        }

        // if (testIfNoStartAfterAddConnection(source, target)) {
        // return false;
        // }

        return true;
    }

    private static boolean testIfNoStartAfterAddConnection(Node source, Node target) {
        // connection is added only to test if there is still a start
        // it will be removed after the test
        INode targetStartNode = target.getProcessStartNode(true);
        INode sourceStartNode = source.getProcessStartNode(true);
        Connection connection = new Connection(source, target, newlineStyle, false);
        ((List<IConnection>) source.getOutgoingConnections()).add(connection);
        ((List<IConnection>) target.getIncomingConnections()).add(connection);
        boolean noStart = (!((Node) sourceStartNode).checkIfCanBeStart())
                && ((targetStartNode == null) || (!((Node) targetStartNode).checkIfCanBeStart()));
        ((List<IConnection>) source.getOutgoingConnections()).remove(connection);
        ((List<IConnection>) target.getIncomingConnections()).remove(connection);
        return noStart;
    }

    private static int countNbMergeOutgoing(INode node) {
        return countNbMergeOutgoing(node, new HashSet<INode>());
    }

    private static int countNbMergeOutgoing(INode node, Set<INode> checkedNode) {
        int curNb = 0;

        if (checkedNode.contains(node)) {
            return 0;
        }
        checkedNode.add(node);

        if (node.getComponent().useMerge()) {
            // if the component use merge even if there is no connection, then add one merge.
            curNb++;
        }

        for (IConnection curConnec : node.getOutgoingConnections()) {
            if (curConnec.getLineStyle().equals(EConnectionType.FLOW_MERGE)) {
                curNb++;
            } else if (curConnec.getLineStyle().equals(EConnectionType.FLOW_MAIN)) {
                // if main, then test the next component to check if there is a merge
                curNb += countNbMergeOutgoing(curConnec.getTarget(), checkedNode);
            }
        }
        return curNb;
    }

    private static int countNbMerge(Node source, Node target) {
        // Map<INode, Integer> infoSource = source.getSubProcessStartNode(false).getLinkedMergeInfo();
        // Map<INode, Integer> infoTarget = target.getSubProcessStartNode(false).getLinkedMergeInfo();
        return countNbMergeOutgoing(source.getSubProcessStartNode(false))
                + countNbMergeOutgoing(target.getSubProcessStartNode(false));
    }

    public static boolean canConnectToSource(INode oldSource, INode newSource, INode target, EConnectionType lineStyle,
            String connectorName, String connectionName) {
        return canConnectToSource(oldSource, newSource, target, lineStyle, connectorName, connectionName, false);
    }

    /**
     * Will return true if the connection can connect or not between source & target.
     *
     * @param oldSource
     * @param newSource
     * @param target
     * @param connType
     * @param connectionName
     * @return
     */
    public static boolean canConnectToSource(INode oldSource, INode newSource, INode target, EConnectionType lineStyle,
            String connectorName, String connectionName, boolean isNewComponent) {
        if (newSource.getConnectorFromName(connectorName) == null || !newSource.getConnectorFromName(connectorName).isShow()) {
            // if the new source don't contain the kind of link, then we can't connect the link.
            return false;
        }
        if (PluginChecker.isJobLetPluginLoaded()) {
            IJobletProviderService service = (IJobletProviderService) GlobalServiceRegister.getDefault().getService(
                    IJobletProviderService.class);
            if (service != null) {
                // can't connect to joblet's node, , bug 21411
                if (service.isJobletComponent(oldSource.getJobletNode()) || service.isJobletComponent(target.getJobletNode())) {
                    return false;
                }
            }
        }
        int maxOutput = newSource.getConnectorFromName(connectorName).getMaxLinkOutput();
        if (maxOutput != -1 && (newSource.getConnectorFromName(connectorName).getCurLinkNbOutput() >= maxOutput)) {
            return false;
        }

        // fix bug 0004935: Error on job save
        if (checkCircle(newSource, target)) {
            return false;
        }

        newlineStyle = lineStyle;
        if (!canConnect(newSource, target, lineStyle, connectionName)) {
            return false;
        }

        // if (!newConnectionType.equals(EConnectionType.LOOKUP)) {
        // INodeConnector nodeConnectorSource;
        // nodeConnectorSource = newSource.getConnectorFromType(newConnectionType);
        // if (nodeConnectorSource.getMaxLinkOutput() != -1) {
        // if (nodeConnectorSource.getCurLinkNbOutput() >= nodeConnectorSource.getMaxLinkOutput()) {
        // return false;
        // }
        // }
        // }

        // check extensionPoints
        if (!isNewComponent) {
            Set<IConnectionValidator> connectionValidators = ConnectionValidatorManager.getConnectionValidators();
            for (IConnectionValidator validator : connectionValidators) {
                boolean canConnectToSource = validator.canConnectToSource(oldSource, newSource, target, lineStyle, connectorName,
                        connectionName);
                if (!canConnectToSource) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Will return true if the connection can connect or not between source & target.
     *
     * @param source
     * @param oldTarget
     * @param newTarget
     * @param connType
     * @param connectionName
     * @return
     */
    public static boolean canConnectToTarget(INode source, INode oldTarget, INode newTarget, EConnectionType lineStyle,
            String connectorName, String connectionName) {

        newlineStyle = lineStyle;
        boolean isMainConn = lineStyle == EConnectionType.FLOW_MAIN;
        if (source.equals(newTarget)) {
            return false;
        }
        final INode designSubjobStartNode = source.getDesignSubjobStartNode();
        if ((designSubjobStartNode.getOutgoingConnections(EConnectionType.ON_SUBJOB_OK).size() != 0 || designSubjobStartNode
                .getOutgoingConnections(EConnectionType.ON_SUBJOB_ERROR).size() != 0)
                && !newTarget.checkIfCanBeStart()
                && isMainConn && !((Node) newTarget).isJoblet()) {
            return false;
        }
        if ((designSubjobStartNode.getIncomingConnections(EConnectionType.ON_SUBJOB_OK).size() != 0 || designSubjobStartNode
                .getIncomingConnections(EConnectionType.ON_SUBJOB_ERROR).size() != 0)
                && !newTarget.checkIfCanBeStart()
                && isMainConn && !((Node) newTarget).isJoblet()) {
            return false;
        }
        if(newlineStyle == EConnectionType.ON_SUBJOB_OK || newlineStyle == EConnectionType.ON_SUBJOB_ERROR) {
        	if(!newTarget.getUniqueName().equals(newTarget.getDesignSubjobStartNode().getUniqueName())) {
        		return false;
        	}
        }
        if (newTarget.getJobletNode() != null) {
            return false;
        }
        if (PluginChecker.isJobLetPluginLoaded()) {
            IJobletProviderService service = (IJobletProviderService) GlobalServiceRegister.getDefault().getService(
                    IJobletProviderService.class);
            if (service != null) {
                if (service.isTriggerNode(newTarget) && !service.canConnectTriggerNode(newTarget, lineStyle)) {
                    return false;
                }
                // can't connect from joblet's node, bug 21411
                if (service.isJobletComponent(source.getJobletNode())) {
                    return false;
                }
            }
        }
        INode processStartNode = source.getProcessStartNode(true);
        // if the target is the start of the (source) process, then can't connect.
        if (processStartNode.equals(newTarget)) {
            return false;
        }

        // to avoid different db elt input link to map.
        if (newTarget.isELTComponent() && newTarget.getComponent().getName().endsWith("Map")) { //$NON-NLS-1$
            String targetName = newTarget.getComponent().getOriginalFamilyName();
            String sourceName = processStartNode.getComponent().getOriginalFamilyName();
            if (!targetName.equals(sourceName) && !(lineStyle.hasConnectionCategory(IConnectionCategory.DEPENDENCY))) {
                return false;
            }
            if (source.isELTComponent() && source.getComponent().getName().endsWith("Map")) { //$NON-NLS-1$
                List<? extends IConnection> tableLines = source.getOutgoingConnections(EConnectionType.TABLE);
                if (null != tableLines && 0 < tableLines.size()) {// if the map has table out line, means it is a start
                                                                  // node, so could not have table_ref out line
                    return false;
                }
                newlineStyle = EConnectionType.TABLE_REF;
            }
        }

        // fix bug 0004935: Error on job save
        if (checkCircle(source, newTarget)) {
            return false;
        }

        if (newTarget.isFileScaleComponent()) {
            if (newlineStyle.hasConnectionCategory(IConnectionCategory.FLOW) && !connectorName.equals("FSCOMBINE")) { //$NON-NLS-1$
                return false;
            }
        }

        // TDI-25765 : avoid any connection for components not accepting PIG
        if (newlineStyle.hasConnectionCategory(IConnectionCategory.FLOW) && "PIGCOMBINE".equals(connectorName)) { //$NON-NLS-1$
            if (!newTarget.getComponent().getName().startsWith("tPig")) { //$NON-NLS-1$
                return false;
            }
        }
        if (newTarget.getComponent() != null && newTarget.getComponent().getName().startsWith("tPig")) { //$NON-NLS-1$
            if (newlineStyle.hasConnectionCategory(IConnectionCategory.FLOW) && !"PIGCOMBINE".equals(connectorName)) { //$NON-NLS-1$
                return false;
            }
        }

        // TDI-29775 : avoid any connection for components not accepting SPARK
        if (newlineStyle.hasConnectionCategory(IConnectionCategory.FLOW) && "SPARKCOMBINE".equals(connectorName)) { //$NON-NLS-1$
            if (!newTarget.getComponent().getName().startsWith("tSpark")) { //$NON-NLS-1$
                return false;
            }
        }
        if (newTarget.getComponent() != null && newTarget.getComponent().getName().startsWith("tSpark")) { //$NON-NLS-1$
            if (newlineStyle.hasConnectionCategory(IConnectionCategory.FLOW) && !"SPARKCOMBINE".equals(connectorName)) { //$NON-NLS-1$
                return false;
            }
        }
        // for bug 10378
        // if (PluginChecker.isJobLetPluginLoaded()) {
        // if (EComponentType.JOBLET_INPUT_OUTPUT.equals(newTarget.getComponent().getComponentType())) {
        // if (newlineStyle.hasConnectionCategory(IConnectionCategory.FLOW)) {
        // boolean isFilterRowComponent = "tFilterRow".equals(source.getComponent().getName());
        // if (isFilterRowComponent && connectorName != null
        //                            && (connectorName.equals("FILTER") || connectorName.equals("REJECT"))) {//$NON-NLS-1$//$NON-NLS-1$
        // return false;
        // }
        // }
        // }
        // }

        // Modify Connection Type depending old and new target.
        if (newlineStyle.hasConnectionCategory(IConnectionCategory.FLOW)) {
            // if the connection type is not the default one, then we don't change automatically.
            // && newlineStyle.getName().equals(newConnectionType)) {

            newlineStyle = EConnectionType.FLOW_MAIN;
            if (newTarget.getComponent().useLookup()) {
                int nbMain = 0;
                for (IConnection connec : newTarget.getIncomingConnections()) {
                    if (connec.getLineStyle().equals(EConnectionType.FLOW_MAIN)) {
                        nbMain++;
                    }
                }
                if (nbMain >= 1) {
                    newlineStyle = EConnectionType.FLOW_REF;
                } else {
                    newlineStyle = EConnectionType.FLOW_MAIN;
                }
            } else if (newTarget.getComponent().useMerge()) {
                newlineStyle = EConnectionType.FLOW_MERGE;
            }
        }
        boolean isJoblet = false;
        if (PluginChecker.isJobLetPluginLoaded()) {
            IJobletProviderService service = (IJobletProviderService) GlobalServiceRegister.getDefault().getService(
                    IJobletProviderService.class);
            if (service != null && service.isJobletComponent(newTarget)
                    && !lineStyle.hasConnectionCategory(IConnectionCategory.FLOW)) {
                List<INodeConnector> inputConnector = service.getFreeTriggerBuiltConnectors(newTarget, lineStyle, true);
                if (inputConnector.isEmpty()) {
                    return false;
                }
                isJoblet = true;
            }
        }
        if (!isJoblet) {
            INodeConnector connectorFromType = newTarget.getConnectorFromType(newlineStyle);
            if (connectorFromType == null || !connectorFromType.isShow()) {
                return false;
            }
            int maxInput = connectorFromType.getMaxLinkInput();
            if (maxInput != -1 && (connectorFromType.getCurLinkNbInput() >= maxInput)) {
                return false;
            }
        }
        if (!canConnect(source, newTarget, lineStyle, connectionName)) {
            return false;
        }

        // if (!newConnectionType.equals(EConnectionType.LOOKUP)) {
        // INodeConnector nodeConnectorTarget;
        // nodeConnectorTarget = newTarget.getConnectorFromType(newConnectionType);
        // if (nodeConnectorTarget.getMaxLinkInput() != -1) {
        // if (nodeConnectorTarget.getCurLinkNbInput() >= nodeConnectorTarget.getMaxLinkInput()) {
        // return false;
        // }
        // }
        // }

        // check extensionPoints
        Set<IConnectionValidator> connectionValidators = ConnectionValidatorManager.getConnectionValidators();
        for (IConnectionValidator validator : connectionValidators) {
            boolean canConnectToTarget = validator.canConnectToTarget(source, oldTarget, newTarget, lineStyle, connectorName,
                    connectionName);
            if (!canConnectToTarget) {
                return false;
            }
        }

        return true;
    }

    public static boolean canConnectToTarget(INode source, INode oldTarget, INode newTarget, EConnectionType lineStyle,
            String connectorName, String connectionName, boolean refactorJoblet) {

        newlineStyle = lineStyle;
        if (source.equals(newTarget)) {
            return false;
        }
        if (PluginChecker.isJobLetPluginLoaded()) {
            IJobletProviderService service = (IJobletProviderService) GlobalServiceRegister.getDefault().getService(
                    IJobletProviderService.class);
            if (service != null) {
                if (service.isTriggerNode(newTarget) && !service.canConnectTriggerNode(newTarget, lineStyle)) {
                    return false;
                }
                // can't connect from joblet's node, bug 21411
                if (service.isJobletComponent(source.getJobletNode())) {
                    return false;
                }
            }
        }
        INode processStartNode = source.getProcessStartNode(true);
        // if the target is the start of the (source) process, then can't connect.
        if (processStartNode.equals(newTarget)) {
            return false;
        }

        // to avoid different db elt input link to map.
        if (newTarget.isELTComponent() && newTarget.getComponent().getName().endsWith("Map")) { //$NON-NLS-1$
            String targetName = newTarget.getComponent().getOriginalFamilyName();
            String sourceName = processStartNode.getComponent().getOriginalFamilyName();
            if (!targetName.equals(sourceName) && !(lineStyle.hasConnectionCategory(IConnectionCategory.DEPENDENCY))) {
                return false;
            }
        }

        // fix bug 0004935: Error on job save
        if (checkCircle(source, newTarget)) {
            return false;
        }

        if (newTarget.isFileScaleComponent()) {
            if (newlineStyle.hasConnectionCategory(IConnectionCategory.FLOW) && !connectorName.equals("FSCOMBINE")) { //$NON-NLS-1$
                return false;
            }
        }
        // for bug 10378
        // if (PluginChecker.isJobLetPluginLoaded()) {
        // if (EComponentType.JOBLET_INPUT_OUTPUT.equals(newTarget.getComponent().getComponentType())) {
        // if (newlineStyle.hasConnectionCategory(IConnectionCategory.FLOW)) {
        // boolean isFilterRowComponent = "tFilterRow".equals(source.getComponent().getName());
        // if (isFilterRowComponent && connectorName != null
        //                            && (connectorName.equals("FILTER") || connectorName.equals("REJECT"))) {//$NON-NLS-1$//$NON-NLS-1$
        // return false;
        // }
        // }
        // }
        // }

        // Modify Connection Type depending old and new target.
        if (newlineStyle.hasConnectionCategory(IConnectionCategory.FLOW)) {
            // if the connection type is not the default one, then we don't change automatically.
            // && newlineStyle.getName().equals(newConnectionType)) {

            newlineStyle = EConnectionType.FLOW_MAIN;
            if (newTarget.getComponent().useLookup()) {
                int nbMain = 0;
                for (IConnection connec : newTarget.getIncomingConnections()) {
                    if (connec.getLineStyle().equals(EConnectionType.FLOW_MAIN)) {
                        nbMain++;
                    }
                }
                if (nbMain >= 1) {
                    newlineStyle = EConnectionType.FLOW_REF;
                } else {
                    newlineStyle = EConnectionType.FLOW_MAIN;
                }
            } else if (newTarget.getComponent().useMerge()) {
                newlineStyle = EConnectionType.FLOW_MERGE;
            }
        }
        boolean isJoblet = false;
        if (PluginChecker.isJobLetPluginLoaded()) {
            IJobletProviderService service = (IJobletProviderService) GlobalServiceRegister.getDefault().getService(
                    IJobletProviderService.class);
            if (service != null && service.isJobletComponent(newTarget)
                    && !lineStyle.hasConnectionCategory(IConnectionCategory.FLOW)) {
                List<INodeConnector> inputConnector = service.getFreeTriggerBuiltConnectors(newTarget, lineStyle, true);
                if (inputConnector.isEmpty()) {
                    return false;
                }
                isJoblet = true;
            }
        }
        if (!isJoblet) {
            INodeConnector connectorFromType = newTarget.getConnectorFromType(newlineStyle);
            if (connectorFromType == null || !connectorFromType.isShow()) {
                return false;
            }
            int maxInput = connectorFromType.getMaxLinkInput();
            if (maxInput != -1 && (connectorFromType.getCurLinkNbInput() >= maxInput)) {
                return false;
            }
        }
        if (!canConnect(source, newTarget, lineStyle, connectionName, refactorJoblet)) {
            return false;
        }

        // if (!newConnectionType.equals(EConnectionType.LOOKUP)) {
        // INodeConnector nodeConnectorTarget;
        // nodeConnectorTarget = newTarget.getConnectorFromType(newConnectionType);
        // if (nodeConnectorTarget.getMaxLinkInput() != -1) {
        // if (nodeConnectorTarget.getCurLinkNbInput() >= nodeConnectorTarget.getMaxLinkInput()) {
        // return false;
        // }
        // }
        // }

        return true;
    }

    /**
     * To call after the canConnect only, this will give the new connection type after connect.
     *
     * @return the newConnectionType
     */
    public static EConnectionType getNewConnectionType() {
        return newlineStyle;
    }

    /**
     *
     * Not used yet.
     *
     * @param source
     * @param target
     * @param connType
     * @param connectionName
     * @return
     */
    public static boolean canRename(INode source, INode target, EConnectionType connType, String connectionName) {
        boolean canRename = true;
        if (connType.hasConnectionCategory(IConnectionCategory.UNIQUE_NAME)) {
            if (!(source.getProcess().checkValidConnectionName(connectionName)) || KeywordsValidator.isKeyword(connectionName)
                    || "ErrorReject".equals(connectionName) || "context".equals(connectionName)) {
                canRename = false;
            }
        } else if (connType == EConnectionType.TABLE || connType == EConnectionType.TABLE_REF) {
            if (connectionName.equals("")) { //$NON-NLS-1$
                canRename = false;
            } else {
                canRename = checkConnectionValue(connectionName);
                if (canRename) {
                    List<? extends IConnection> cons = target.getIncomingConnections();
                    for (Object element : cons) {
                        Connection conn = (Connection) element;
                        if (conn.getName().equals(connectionName)) {
                            canRename = false;
                            break;
                        }
                    }
                }
            }
        }
        return canRename;
    }

    private static boolean checkConnectionValue(String value) {
        if (KeywordsValidator.isKeyword(value) || KeywordsValidator.isSqlKeyword(value)) {
            return false;
        }
        if (value.contains(".")) { //$NON-NLS-1$
            int indexOf = value.indexOf("."); //$NON-NLS-1$
            String preString = value.substring(0, indexOf);
            if (KeywordsValidator.isKeyword(preString) || KeywordsValidator.isSqlKeyword(preString)) {
                return false;
            } else {
                String postString = value.substring(indexOf + 1);
                return checkConnectionValue(postString);
            }
        }
        return true;

    }

    /**
     * DOC bqian Comment method "checkCircle".
     *
     * @param newTarget
     * @param source
     * @return
     */
    public static boolean checkCircle(INode source, INode newTarget) {
        // get All the source nodes of the source
        List<INode> list = new ArrayList<INode>();
        getAllSourceNode(source, list, new HashSet<String>());

        if (list.contains(newTarget)) {
            return true;
        }
        if (source.equals(newTarget)) {
            return true;
        }
        return false;
    }

    /**
     * DOC bqian Comment method "getAllSourceNode".
     *
     * @param source
     * @param list
     * @param processedSet
     */
    private static void getAllSourceNode(INode source, List<INode> list, Set<String> processedSet) {
        if (processedSet.contains(source.getUniqueName())) {
            return;
        }
        processedSet.add(source.getUniqueName());
        List<? extends IConnection> connections = source.getIncomingConnections();
        for (IConnection connection : connections) {
            INode node = connection.getSource();
            list.add(node);
            getAllSourceNode(node, list, processedSet);
        }
    }
}
