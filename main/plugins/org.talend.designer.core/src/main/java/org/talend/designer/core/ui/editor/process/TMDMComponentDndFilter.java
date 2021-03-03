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
package org.talend.designer.core.ui.editor.process;

import org.talend.core.model.components.IComponent;
import org.talend.core.model.metadata.builder.connection.Concept;
import org.talend.core.model.metadata.builder.connection.MDMConnection;
import org.talend.core.model.metadata.builder.connection.MdmConceptType;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.MDMConnectionItem;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.repository.DefaultRepositoryComponentDndFilter;
import org.talend.core.repository.model.repositoryObject.MetadataTableRepositoryObject;
import org.talend.repository.model.RepositoryNode;

/**
 * DOC hcyi class global comment. Detailled comment
 */
public class TMDMComponentDndFilter extends DefaultRepositoryComponentDndFilter {

    private static final String OUTPUT = "Output"; //$NON-NLS-1$

    private static final String INPUT = "Input"; //$NON-NLS-1$

    private static final String RECEIVE = "Receive"; //$NON-NLS-1$

    public TMDMComponentDndFilter() {
        super();
    }

    @Override
    public boolean except(Item item, ERepositoryObjectType type, RepositoryNode seletetedNode, IComponent component,
            String repositoryType) {
        if (component != null && component.getName().startsWith("tMDM")) {
            // for mdm
            MdmConceptType mdmType = null;
            if (item instanceof MDMConnectionItem) {
                MDMConnectionItem mdmItem = (MDMConnectionItem) item;
                if (seletetedNode != null && seletetedNode.getObject() instanceof MetadataTableRepositoryObject) {
                    MetadataTableRepositoryObject object = (MetadataTableRepositoryObject) seletetedNode.getObject();
                    if (mdmItem.getConnection() instanceof MDMConnection) {
                        MDMConnection connection = (MDMConnection) mdmItem.getConnection();
                        for (Object obj : connection.getSchemas()) {
                            if (obj instanceof Concept && object.getLabel().equals(((Concept) obj).getLabel())) {
                                mdmType = ((Concept) obj).getConceptType();
                                if (mdmType != null) {
                                    if (MdmConceptType.INPUT.equals(mdmType) && component.getName().endsWith(INPUT)) {
                                        return false;
                                    } else if (MdmConceptType.OUTPUT.equals(mdmType) && component.getName().endsWith(OUTPUT)) {
                                        return false;
                                    } else if (MdmConceptType.RECEIVE.equals(mdmType) && component.getName().endsWith(RECEIVE)) {
                                        return false;
                                    }
                                }
                            }
                        }
                    }
                } else {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean valid(Item item, ERepositoryObjectType type, RepositoryNode seletetedNode, IComponent component,
            String repositoryType) {
        return false; // no valid component
    }
}
