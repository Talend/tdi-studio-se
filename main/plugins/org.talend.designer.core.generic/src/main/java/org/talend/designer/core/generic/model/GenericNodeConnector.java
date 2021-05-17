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
package org.talend.designer.core.generic.model;

import java.util.Set;

import org.talend.components.api.component.Connector;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.core.model.process.EConnectionType;
import org.talend.core.model.process.INode;
import org.talend.designer.core.model.components.NodeConnector;

/**
 * created by nrousseau on Apr 22, 2016 Detailled comment
 *
 */
public class GenericNodeConnector extends NodeConnector {

    public static String INPUT_CONNECTOR = Connector.MAIN_NAME;

    private String genericConnectorType;

    private Connector myConnector;

    private boolean output;

    private ComponentProperties componentProperties;

    /**
     * DOC nrousseau GenericNodeConnector constructor comment.
     *
     * @param parentNode
     */
    public GenericNodeConnector(INode parentNode, boolean output) {
        super(parentNode);
        this.output = output;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.model.components.NodeConnector#getMaxLinkOutput()
     */
    @Override
    public int getMaxLinkOutput() {
        if (output && getConnector() != null) {
            return 1;
        }
        return 0;
    }

    /* (non-Javadoc)
     * @see org.talend.designer.core.model.components.NodeConnector#getMaxLinkInput()
     */
    @Override
    public int getMaxLinkInput() {
        if (!output && getConnector() != null) {
            return 1;
        }
        return 0;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.model.components.NodeConnector#getMinLinkInput()
     */
    @Override
    public int getMinLinkInput() {
        return 0;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.model.components.NodeConnector#getMinLinkOutput()
     */
    @Override
    public int getMinLinkOutput() {
        return 0;
    }

    /**
     * DOC nrousseau Comment method "setGenericConnectorType".
     *
     * @param genericConnectorType
     */
    public void setGenericConnectorType(String genericConnectorType) {
        this.genericConnectorType = genericConnectorType;
    }

    private Connector getConnector() {
        if (myConnector == null) {
            Set<? extends Connector> connectors = getComponentProperties().getAvailableConnectors(null, output);
            if (connectors != null) {
                for (Connector connector : connectors) {
                    if (connector.getName().equals(genericConnectorType)) {
                        myConnector = connector;
                        break;
                    }
                }
            }
        }
        return myConnector;

    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.model.components.NodeConnector#getMenuName()
     */
    @Override
    public String getMenuName() {
        if (getConnector() != null) {
            return getConnector().getDisplayName();
        }
        return super.getMenuName();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.model.components.NodeConnector#getName()
     */
    @Override
    public String getName() {
        if (!output) {
            return EConnectionType.FLOW_MAIN.getName();
        }
        return genericConnectorType;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.model.components.NodeConnector#getBaseSchema()
     */
    @Override
    public String getBaseSchema() {
        return EConnectionType.FLOW_MAIN.getName();
    }

    public Connector getComponentConnector() {
        Set<? extends Connector> connectors = getComponentProperties().getPossibleConnectors(output);
        if (connectors != null) {
            for (Connector connector : connectors) {
                if (connector.getName().equals(genericConnectorType)) {
                    return connector;
                }
            }
        }
        return null;
    }


    /**
     * Getter for output.
     * @return the output
     */
    protected boolean isOutput() {
        return this.output;
    }

    public ComponentProperties getComponentProperties() {
        INode parentNode = getParentNode();
        if (parentNode != null) {
            return parentNode.getComponentProperties();
        }
        return this.componentProperties;
    }

    public void setComponentProperties(ComponentProperties componentProperties) {
        this.componentProperties = componentProperties;
    }

}
