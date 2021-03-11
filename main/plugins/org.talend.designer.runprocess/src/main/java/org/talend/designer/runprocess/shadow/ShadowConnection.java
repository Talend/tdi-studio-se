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
package org.talend.designer.runprocess.shadow;

import java.util.List;

import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.process.AbstractConnection;
import org.talend.core.model.process.EConnectionType;
import org.talend.core.model.process.INode;

/**
 * DOC chuger class global comment. Detailled comment <br/>
 *
 * $Id$
 *
 */
public class ShadowConnection extends AbstractConnection {

    private INode source;

    private INode target;

    private EConnectionType linetype;

    private String lineName;

    /**
     * Constructs a new ShadowConnection.
     */
    public ShadowConnection(INode source, INode target) {
        super();

        this.source = source;
        this.target = target;
    }

    public ShadowConnection(INode source, INode target, EConnectionType linetype, String lineName) {
        super();

        this.source = source;
        this.target = target;
        this.linetype = linetype;
        this.lineName = lineName;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IConnection#getLineStyle()
     */
    public EConnectionType getLineStyle() {
        if (linetype != null) {
            return linetype;
        }
        return EConnectionType.FLOW_MAIN;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IConnection#getMetadataTable()
     */
    public IMetadataTable getMetadataTable() {
        if (source != null) {
            List<IMetadataTable> metadataList = source.getMetadataList();
            for (int i = 0; i < metadataList.size(); i++) {
                if (metadataList.get(i).getTableName().equals(source.getUniqueName())) {
                    return metadataList.get(i);
                }
            }
        }
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IConnection#getName()
     */
    public String getName() {
        if (lineName != null) {
            return lineName;
        }
        return "Row"; //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IConnection#getSource()
     */
    public INode getSource() {
        return source;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IConnection#getTarget()
     */
    public INode getTarget() {
        return target;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IConnection#isActivate()
     */
    public boolean isActivate() {
        return true;
    }

    public String getUniqueName() {
        return getName();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.AbstractConnection#getConnectorName()
     */
    @Override
    public String getConnectorName() {
        if (linetype != null) {
            return linetype.getName();
        }
        return EConnectionType.FLOW_MAIN.getName();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IConnection#getTmpNode()
     */
    public INode getTmpNode() {
        // TODO Auto-generated method stub
        return null;
    }
}
