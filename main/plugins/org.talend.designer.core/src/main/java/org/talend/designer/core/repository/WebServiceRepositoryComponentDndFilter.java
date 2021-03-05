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
package org.talend.designer.core.repository;

import org.talend.core.model.components.IComponent;
import org.talend.core.model.metadata.builder.connection.WSDLSchemaConnection;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.WSDLSchemaConnectionItem;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.repository.DefaultRepositoryComponentDndFilter;
import org.talend.repository.model.RepositoryNode;

/**
 * ggu class global comment. Detailled comment
 */
public class WebServiceRepositoryComponentDndFilter extends DefaultRepositoryComponentDndFilter {

    public WebServiceRepositoryComponentDndFilter() {
    }

    @Override
    public String getRepositoryType(Item item, ERepositoryObjectType type) {
        // only process for webservice, others will be processed by default
        if (item instanceof WSDLSchemaConnectionItem) {
            WSDLSchemaConnection connection = (WSDLSchemaConnection) ((WSDLSchemaConnectionItem) item).getConnection();
            if (!connection.isIsInputModel()) {
                return "WEBSERVICE"; //$NON-NLS-1$
            } else {
                return "WSDL"; //$NON-NLS-1$
            }
        }
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.repository.DefaultRepositoryComponentDndFilter#except(org.talend.core.model.properties.Item,
     * org.talend.core.model.repository.ERepositoryObjectType, org.talend.repository.model.RepositoryNode,
     * org.talend.core.model.components.IComponent, java.lang.String)
     */
    @Override
    public boolean except(Item item, ERepositoryObjectType type, RepositoryNode seletetedNode, IComponent component,
            String repositoryType) {
        if (!(item instanceof WSDLSchemaConnectionItem) || component == null || repositoryType == null) {
            return false;
        }
        return !repositoryType.equals(component.getRepositoryType());
    }

}
