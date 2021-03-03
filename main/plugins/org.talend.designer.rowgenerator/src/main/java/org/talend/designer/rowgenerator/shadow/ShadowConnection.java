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
package org.talend.designer.rowgenerator.shadow;

import java.util.List;

import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.process.AbstractConnection;
import org.talend.core.model.process.EConnectionType;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;

/**
 * qzhang class global comment. Detailled comment <br/>
 *
 * $Id: ShadowConnection.java,v 1.1 2007/02/02 06:21:07 pub Exp $
 *
 */
public class ShadowConnection extends AbstractConnection implements IConnection {

    private INode source;

    private INode target;

    /**
     * Constructs a new ShadowConnection.
     */
    public ShadowConnection(INode source, INode target) {
        super();

        this.source = source;
        this.target = target;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IConnection#getLineStyle()
     */
    public EConnectionType getLineStyle() {
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

    public boolean isUseByMetter() {
        return false;
    }

    public List<? extends IElementParameter> getElementParametersWithChildrens() {
        return getElementParameters();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IElement#getElementName()
     */
    public String getElementName() {
        return getUniqueName();
    }

}
