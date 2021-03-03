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
package org.talend.designer.mapper.external.connection;

import java.util.List;

import org.talend.core.model.components.IODataComponent;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.process.EConnectionType;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 * $Id$
 *
 */
public class IOConnection {

    private IConnection connection;

    private IMetadataTable table;

    /**
     * DOC amaumont MapperConnection constructor comment.
     *
     * @param connection
     * @param ioDataComponent
     */
    public IOConnection(IConnection connection) {
        super();
        this.connection = connection;
        this.table = connection.getMetadataTable();
    }

    /**
     * DOC amaumont MapperConnection constructor comment.
     *
     * @param connection
     * @param ioDataComponent
     */
    public IOConnection(IODataComponent ioDataComponent) {
        super();
        connection = ioDataComponent.getConnection();
        table = ioDataComponent.getTable();
    }

    public String getName() {
        return connection.getName();
    }

    public EConnectionType getConnectionType() {
        return connection.getLineStyle();
    }

    public IMetadataTable getTable() {
        return table;
    }

    /**
     * @return
     * @see org.talend.core.model.process.IConnection#getCondition()
     */
    public String getCondition() {
        return this.connection.getCondition();
    }

    /**
     * @return
     * @see org.talend.core.model.process.IElement#getElementParameters()
     */
    public List<? extends IElementParameter> getElementParameters() {
        return this.connection.getElementParameters();
    }

    /**
     * @return
     * @see org.talend.core.model.process.IConnection#getSource()
     */
    public INode getSource() {
        return this.connection.getSource();
    }

    /**
     * @return
     * @see org.talend.core.model.process.IConnection#getTarget()
     */
    public INode getTarget() {
        return this.connection.getTarget();
    }

    /**
     * @return
     * @see org.talend.core.model.process.IConnection#getUniqueName()
     */
    public String getUniqueName() {
        return this.connection.getUniqueName();
    }

    /**
     * @return
     * @see org.talend.core.model.process.IConnection#isActivate()
     */
    public boolean isActivate() {
        return this.connection.isActivate();
    }

    /**
     * @return
     * @see org.talend.core.model.process.IElement#isReadOnly()
     */
    public boolean isReadOnly() {
        return this.connection.isReadOnly();
    }

    public IConnection getConnecion() {
        return this.connection;
    }

}
