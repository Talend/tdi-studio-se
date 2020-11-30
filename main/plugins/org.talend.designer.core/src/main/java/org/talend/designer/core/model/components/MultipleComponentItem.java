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
package org.talend.designer.core.model.components;

import java.util.ArrayList;
import java.util.List;

import org.talend.core.model.components.IMultipleComponentConnection;
import org.talend.core.model.components.IMultipleComponentItem;
import org.talend.core.model.process.INode;

/**
 * DOC nrousseau class global comment. Detailled comment <br/>
 *
 * $Id$
 *
 */
public class MultipleComponentItem implements IMultipleComponentItem {

    String name;

    String component;

    List<IMultipleComponentConnection> outputConnections = new ArrayList<IMultipleComponentConnection>();

    List<IMultipleComponentConnection> inputConnections = new ArrayList<IMultipleComponentConnection>();

    public List<IMultipleComponentConnection> getOutputConnections() {
        return this.outputConnections;
    }

    public List<IMultipleComponentConnection> getInputConnections() {
        return this.inputConnections;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.model.components.IMultipleComponentItem#getName()
     */
    public String getName() {
        return this.name;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.model.components.IMultipleComponentItem#setName(java.lang.String)
     */
    public void setName(String name) {
        this.name = name;
    }

    public String getComponent() {
        return this.component;
    }

    public void setComponent(String component) {
        this.component = component;
    }

    @Override
    public void updateNode(INode newNode, INode oldNode) {
        // nothing to do
    }

}
