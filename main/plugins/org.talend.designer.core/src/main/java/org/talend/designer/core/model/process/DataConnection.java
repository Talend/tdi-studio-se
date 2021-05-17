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
import java.util.List;

import org.talend.core.model.process.AbstractConnection;
import org.talend.core.model.process.IDataConnection;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;

/**
 * Virtual connection used for the generated code. <br/>
 *
 * $Id$
 *
 */
public class DataConnection extends AbstractConnection implements IDataConnection {

    /**
     * feature 6355.
     */
    private boolean trace; // trace on connection.

    private String tracesCondition; // if null, all will be enabled for traces.

    private List<String> enabledTraceColumns = new ArrayList<String>();

    private INode linkedNodeForHash;

    public boolean isTraceConnection() {
        return this.trace;
    }

    public void setTraceConnection(boolean trace) {
        this.trace = trace;
    }

    public void setEnabledTraceColumns(List<String> enabledTraceColumns) {
        this.enabledTraceColumns = enabledTraceColumns;
    }

    @Override
    public List<String> getEnabledTraceColumns() {
        return this.enabledTraceColumns;
    }

    public void setTracesCondition(String tracesCondition) {
        this.tracesCondition = tracesCondition;
    }

    @Override
    public String getTracesCondition() {
        return this.tracesCondition;
    }

    /**
     * Only filled if the original link is a from lookup (to tell which is tMap linked for example).
     *
     * @return the linkedNodeForHash
     */
    public INode getLinkNodeForHash() {
        return this.linkedNodeForHash;
    }

    /**
     * Only filled if the original link is a from lookup (to tell which is tMap linked for example).
     *
     * @param linkedNodeForHash the linkedNodeForHash to set
     */
    public void setLinkNodeForHash(INode linkedNodeForHash) {
        this.linkedNodeForHash = linkedNodeForHash;
    }
}
