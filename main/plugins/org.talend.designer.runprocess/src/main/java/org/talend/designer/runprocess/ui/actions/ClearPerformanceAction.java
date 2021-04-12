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

import java.util.List;

import org.eclipse.jface.action.Action;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.IPerformance;
import org.talend.core.model.process.IProcess;
import org.talend.designer.core.ui.editor.nodes.Node;

/**
 * Clean performance data on a process. <br/>
 *
 * $Id$
 *
 */
public class ClearPerformanceAction extends Action {

    /** The process to be cleared. */
    private IProcess process;

    /**
     * Constructs a new ClearPerformanceAction.
     */
    public ClearPerformanceAction() {
        super("Clean performance tracking"); //$NON-NLS-1$

        setEnabled(false);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    public void run() {
        IConnection[] conns = process.getAllConnections(null);
        for (int i = 0; i < conns.length; i++) {
            if (conns[i] instanceof IPerformance) {
                ((IPerformance) conns[i]).setPerformanceData(""); //$NON-NLS-1$
            }
        }
        clearJobletPerformance();
    }

    private void clearJobletPerformance(){
    	for(INode inode : process.getGraphicalNodes()){
    		if(!(inode instanceof Node)){
    			return;
    		}
    		Node node = (Node) inode;
    		if(node.isJoblet()&&!node.getNodeContainer().isCollapsed()){
    			List list = node.getNodeContainer().getElements();
    			for(Object obj : list){
    				if(obj instanceof Node){
    					for(IConnection conn:((Node)obj).getOutgoingConnections()){
    						if(!list.contains(conn.getTarget())){
    							continue;
    						}
    						if(conn instanceof IPerformance){
    							((IPerformance) conn).setPerformanceData(""); //$NON-NLS-1$
    						}
    					}
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
