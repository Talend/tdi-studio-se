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
package org.talend.designer.core.model.components;

import org.talend.core.model.components.IMultipleComponentConnection;
import org.talend.core.model.components.IMultipleComponentItem;

/**
 * DOC nrousseau class global comment. Detailled comment <br/>
 *
 * $Id$
 *
 */
public class MultipleComponentConnection implements IMultipleComponentConnection {

    String nameTarget;

    IMultipleComponentItem source;

    IMultipleComponentItem target;

    String connectionType;

    String connectorName;

    public MultipleComponentConnection(String cType, String targetName) {
        connectionType = cType;
        nameTarget = targetName;
        connectorName = cType;
    }

    public String getConnectorName() {
        return connectorName;
    }

    public void setConnectorName(String connectorName) {
        this.connectorName = connectorName;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.model.components.IMultipleComponentConnection#getConnectionType()
     */
    public String getConnectionType() {
        return this.connectionType;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.model.components.IMultipleComponentConnection#setConnectionType(org.talend.core.model.process.EConnectionType)
     */
    public void setConnectionType(String connectionType) {
        this.connectionType = connectionType;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.model.components.IMultipleComponentConnection#getNameTarget()
     */
    public String getNameTarget() {
        return this.nameTarget;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.model.components.IMultipleComponentConnection#setNameTarget(java.lang.String)
     */
    public void setNameTarget(String nameTarget) {
        this.nameTarget = nameTarget;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.model.components.IMultipleComponentConnection#getSource()
     */
    public IMultipleComponentItem getSource() {
        return this.source;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.model.components.IMultipleComponentConnection#setSource(org.talend.core.model.components.IMultipleComponentItem)
     */
    public void setSource(IMultipleComponentItem source) {
        this.source = source;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.model.components.IMultipleComponentConnection#getTarget()
     */
    public IMultipleComponentItem getTarget() {
        return this.target;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.model.components.IMultipleComponentConnection#setTarget(org.talend.core.model.components.IMultipleComponentItem)
     */
    public void setTarget(IMultipleComponentItem target) {
        this.target = target;
    }
}
