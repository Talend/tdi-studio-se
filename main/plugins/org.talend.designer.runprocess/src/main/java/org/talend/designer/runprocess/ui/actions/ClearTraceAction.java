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
package org.talend.designer.runprocess.ui.actions;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.jface.action.Action;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.IProcess;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.runprocess.trace.TraceConnectionsManager;

/**
 * Clean trace data on a process. <br/>
 *
 * $Id$
 *
 */
public class ClearTraceAction extends Action {

    /** The process to be cleared. */
    private IProcess process;

    /**
     * Constructs a new ClearTraceAction.
     */
    public ClearTraceAction() {
        super("Clean trace tracking"); //$NON-NLS-1$

        setEnabled(false);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    public void run() {
        //
        final TraceConnectionsManager traceConnectionsManager = new TraceConnectionsManager(process);
        traceConnectionsManager.init();
        Set<IConnection> doneConnections = new HashSet<IConnection>();

        IConnection connection = null;
        for (Iterator<? extends INode> i = process.getGraphicalNodes().iterator(); connection == null && i.hasNext();) {
            INode psNode = i.next();
            if (psNode instanceof Node) {
                Node node = (Node) psNode;
                node.getNodeProgressBar().updateState("UPDATE_STATUS", new Double(0)); //$NON-NLS-1$

                node.setErrorFlag(false);
                node.setCompareFlag(false);
                node.setErrorInfo(null);
                node.getNodeError().updateState("UPDATE_STATUS", false); //$NON-NLS-1$
                node.setErrorInfoChange("ERRORINFO", false); //$NON-NLS-1$
            }

            for (IConnection connec : psNode.getOutgoingConnections()) {
                if (!doneConnections.contains(connec)) {
                    connec.setTraceData(null);
                    doneConnections.add(connec);
                }
                // get the related shadow connections
                IConnection[] shadowconns = traceConnectionsManager.getShadowConnenctions(connec.getUniqueName());
                if (shadowconns != null) {
                    for (IConnection sc : shadowconns) {
                        if (!doneConnections.contains(sc)) {
                            sc.setTraceData(null);
                            doneConnections.add(sc);
                        }
                    }
                }
            }
            // currently, only for joblet when expanded.
            IConnection[] nonShadowDataConnections = traceConnectionsManager.getNonShadowDataConnections(psNode);
            if (nonShadowDataConnections != null) {
                for (IConnection conn : nonShadowDataConnections) {
                    if (!doneConnections.contains(conn)) {
                        conn.setTraceData(null);
                        doneConnections.add(conn);
                    }
                }
            }
        }
    }

    /**
     * Getter for process.
     *
     * @return the process
     */
    public IProcess getProcess() {
        return this.process;
    }

    /**
     * Sets the process.
     *
     * @param process the process to set
     */
    public void setProcess(IProcess process) {
        this.process = process;
        setEnabled(process != null);
    }
}
