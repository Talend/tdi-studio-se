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
package org.talend.designer.runprocess.trace;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.PluginChecker;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.IConnectionCategory;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.IProcess;
import org.talend.core.ui.IJobletProviderService;
import org.talend.designer.core.utils.ConnectionUtil;

/**
 * DOC ggu class global comment. Detailled comment
 */
public class TraceConnectionsManager {

    private IProcess process;

    /**
     * key is unique name of connection.
     */
    private Map<String, IConnection> connsMap;

    /**
     * key is unique name of connection, value is the shadow connection of current connection.
     */
    private Map<String, IConnection[]> shadowConnsMap, jobletNonShadowConnsMap;

    private IJobletProviderService jobletService;

    public TraceConnectionsManager(IProcess process) {
        super();
        this.process = process;
        Assert.isNotNull(process);

        if (PluginChecker.isJobLetPluginLoaded()
                && GlobalServiceRegister.getDefault().isServiceRegistered(IJobletProviderService.class)) {
            jobletService = (IJobletProviderService) GlobalServiceRegister.getDefault().getService(IJobletProviderService.class);
        }

    }

    public synchronized void clear() {
        this.process = null;
        if (this.connsMap != null) {
            this.connsMap.clear();
            this.connsMap = null;
        }
        if (this.shadowConnsMap != null) {
            this.shadowConnsMap.clear();
            this.shadowConnsMap = null;
        }
        if (this.jobletNonShadowConnsMap != null) {
            this.jobletNonShadowConnsMap.clear();
            this.jobletNonShadowConnsMap = null;
        }
    }

    public synchronized void init() {
        connsMap = new HashMap<String, IConnection>();
        shadowConnsMap = new HashMap<String, IConnection[]>();
        jobletNonShadowConnsMap = new HashMap<String, IConnection[]>();

        if (process != null) {
            Set<IConnection> allShadowConnections = new HashSet<IConnection>();
            // collect from graphical connections
            Iterator<? extends INode> nodeIterator = process.getGraphicalNodes().iterator();
            while (nodeIterator.hasNext()) {
                INode node = nodeIterator.next();
                if (node.isActivate()) {
                    // all output connections
                    Iterator<? extends IConnection> connIterator = node.getOutgoingConnections().iterator();
                    while (connIterator.hasNext()) {
                        IConnection conn = connIterator.next();
                        String connectionUnifiedName = ConnectionUtil.getConnectionUnifiedName(conn);
                        // because the route if not data, so only check all connection with unique name
                        if (/* isDataConnection(conn) && */isUniqueNameConn(conn) && !connsMap.containsKey(connectionUnifiedName)) {
                            connsMap.put(connectionUnifiedName, conn);

                            IConnection[] shadowConnections = getShadowConnections(conn);
                            if (shadowConnections != null) {
                                shadowConnsMap.put(connectionUnifiedName, shadowConnections);
                                allShadowConnections.addAll(Arrays.asList(shadowConnections));
                            }
                        }
                    }

                    // for Joblet
                    if (jobletService != null && jobletService.isJobletComponent(node)) {
                        /*
                         * find the non-shadow connections in the joblet when this joblet is expaned. if it's not
                         * expanded, should be return null or empty.
                         */
                        IConnection[] jobletConnections = jobletService.getNonShadowDataConnections(node);
                        if (jobletConnections != null) {
                            jobletNonShadowConnsMap.put(node.getUniqueName(), jobletConnections);

                            for (IConnection conn : jobletConnections) {
                                // if shadow, it has been recoded in shadow list.
                                if (!allShadowConnections.contains(conn)) {
                                    String jobletConnUnifiedName = ConnectionUtil.getConnectionUnifiedName(conn);
                                    connsMap.put(jobletConnUnifiedName, conn);

                                    // find shadow connections for inner joblet connection.
                                    IConnection[] shadowConnections = getShadowConnections(conn);
                                    if (shadowConnections != null) {
                                        shadowConnsMap.put(jobletConnUnifiedName, shadowConnections);
                                        allShadowConnections.addAll(Arrays.asList(shadowConnections));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

    }

    private boolean isDataConnection(IConnection conn) {
        // only process the connections with data, like FLOW_MAIN, FLOW_MERGE, FLOW_REF(Lookup)
        return conn != null && conn.getLineStyle().hasConnectionCategory(IConnectionCategory.FLOW);
    }

    private boolean isUniqueNameConn(IConnection conn) {
        return conn != null && conn.getLineStyle().hasConnectionCategory(IConnectionCategory.UNIQUE_NAME);
    }

    /**
     *
     * About the shandow connection, it's only for design, but not existed in the final generated codes. like some
     * joblet connections.
     */
    private IConnection[] getShadowConnections(IConnection conn) {
        //
        Set<IConnection> shadowConns = new HashSet<IConnection>();
        // for Joblet
        if (jobletService != null) {
            IConnection[] jobletShadowConnections = jobletService.getShadowDataConnections(conn);
            if (jobletShadowConnections != null) {
                shadowConns.addAll(Arrays.asList(jobletShadowConnections));
            }
        }
        if (shadowConns.isEmpty()) { // if no, will return null
            return null;
        }
        return shadowConns.toArray(new IConnection[0]);
    }

    /**
     *
     * find the connection by the connection unique name.
     */
    public IConnection finConnectionByUniqueName(String connUniqueName) {
        if (connUniqueName != null) {
            return connsMap.get(connUniqueName);
        }
        return null;
    }

    /**
     * get current connection's shadow connections.
     */
    public IConnection[] getShadowConnenctions(String curConnUniqueName) {
        if (curConnUniqueName != null) {
            return this.shadowConnsMap.get(curConnUniqueName);
        }
        return null;
    }

    /**
     *
     * Get all non-shadow connections for current joblet node. only work for expended joblet.
     */
    public IConnection[] getNonShadowDataConnections(INode node) {
        if (node != null) {
            return this.jobletNonShadowConnsMap.get(node.getUniqueName());
        }
        return null;
    }
}
