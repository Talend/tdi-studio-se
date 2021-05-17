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
import org.talend.core.model.metadata.builder.connection.XmlFileConnection;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.XmlFileConnectionItem;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.repository.DefaultRepositoryComponentDndFilter;
import org.talend.repository.model.RepositoryNode;

/**
 * DOC ggu class global comment. Detailled comment
 */
public class XMLOutputRepositoryComponentDndFilter extends DefaultRepositoryComponentDndFilter {

    public XMLOutputRepositoryComponentDndFilter() {
    }

    @Override
    public String getRepositoryType(Item item, ERepositoryObjectType type) {
        if (item instanceof XmlFileConnectionItem) {
            XmlFileConnection connection = (XmlFileConnection) ((XmlFileConnectionItem) item).getConnection();
            if (!connection.isInputModel()) {
                return "XMLOUTPUT"; //$NON-NLS-1$
            }
        }
        return null;
    }

    @Override
    public boolean except(Item item, ERepositoryObjectType type, RepositoryNode seletetedNode, IComponent component,
            String repositoryType) {
        if (component != null && repositoryType != null && repositoryType.equals("XMLOUTPUT")
                && (component.getName().equals("tExtractXMLField") || component.getName().equals("tFileInputXML"))) {
            return true;
        }
        return false;
    }

}
