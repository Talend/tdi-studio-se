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
package org.talend.designer.rowgenerator.external.data;

import org.talend.core.model.components.IODataComponent;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.process.EConnectionType;
import org.talend.core.model.process.IConnection;

/**
 * qzhang class global comment. Detailled comment <br/>
 *
 * $Id: talend-code-templates.xml 1 2007-2-10 下午02:52:12 (星期五, 29 九月 2006) qzhang $
 *
 */
public class IOConnection {

    private String name;

    private IMetadataTable table;

    private EConnectionType connectionType;

    /**
     * amaumont MapperConnection constructor comment.
     *
     * @param connection
     * @param ioDataComponent
     */
    public IOConnection(IConnection connection) {
        super();
        this.name = connection.getName();
        this.table = connection.getMetadataTable();
        this.connectionType = connection.getLineStyle();

    }

    /**
     * amaumont MapperConnection constructor comment.
     *
     * @param connection
     * @param ioDataComponent
     */
    public IOConnection(IODataComponent ioDataComponent) {
        super();
        this.name = ioDataComponent.getName();
        this.table = ioDataComponent.getTable();
        this.connectionType = ioDataComponent.getConnectionType();
    }

    public String getName() {
        return name;
    }

    public EConnectionType getConnectionType() {
        return connectionType;
    }

    public IMetadataTable getTable() {
        return table;
    }

}
