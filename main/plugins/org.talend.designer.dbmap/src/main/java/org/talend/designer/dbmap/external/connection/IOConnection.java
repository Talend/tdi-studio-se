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
package org.talend.designer.dbmap.external.connection;

import org.talend.core.model.components.IODataComponent;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.process.EConnectionType;
import org.talend.core.model.process.IConnection;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 * $Id: IOConnection.java 968 2006-12-12 10:59:26Z amaumont $
 *
 */
public class IOConnection {

    private String name;

    private IMetadataTable table;

    private EConnectionType connectionType;

    private String uniqueName;

    /**
     * DOC amaumont MapperConnection constructor comment.
     *
     * @param connection
     * @param ioDataComponent
     */
    public IOConnection(IConnection connection) {
        super();
        this.name = connection.getName();
        this.uniqueName = connection.getUniqueName();
        this.table = connection.getMetadataTable();
        this.connectionType = connection.getLineStyle();

    }

    /**
     * DOC amaumont MapperConnection constructor comment.
     *
     * @param connection
     * @param ioDataComponent
     */
    public IOConnection(IODataComponent ioDataComponent) {
        super();
        this.name = ioDataComponent.getName();
        this.uniqueName = ioDataComponent.getUniqueName();
        this.table = ioDataComponent.getTable();
        this.connectionType = ioDataComponent.getConnectionType();
    }

    public String getName() {
        return name;
    }

    /**
     * Getter for uniqueName.
     *
     * @return the uniqueName
     */
    public String getUniqueName() {
        return this.uniqueName;
    }

    public EConnectionType getConnectionType() {
        return connectionType;
    }

    public IMetadataTable getTable() {
        return table;
    }

}
