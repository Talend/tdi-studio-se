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
package org.talend.designer.mapper.handlers;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.properties.Item;
import org.talend.core.model.relationship.AbstractJobItemRelationshipHandler;
import org.talend.core.model.relationship.Relation;
import org.talend.core.model.relationship.RelationshipItemBuilder;
import org.talend.core.service.IDesignerMapperService;
import org.talend.designer.core.model.utils.emf.talendfile.AbstractExternalData;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;

/**
 * DOC ggu class global comment. Detailled comment
 */
public class MapperSchemaTypeItemRelationshipHandler extends AbstractJobItemRelationshipHandler {

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.core.model.relationship.AbstractItemRelationshipHandler#collect(org.talend.core.model.properties.Item)
     */
    @Override
    protected Set<Relation> collect(Item baseItem) {
        ProcessType processType = getProcessType(baseItem);
        if (processType == null) {
            return Collections.emptySet();
        }
        Set<Relation> relationSet = new HashSet<Relation>();

        // handle tMap schema relations...
        if (GlobalServiceRegister.getDefault().isServiceRegistered(IDesignerMapperService.class)) {
            IDesignerMapperService service = (IDesignerMapperService) GlobalServiceRegister.getDefault().getService(
                    IDesignerMapperService.class);

            for (Object o : processType.getNode()) {
                if (o instanceof NodeType) {
                    NodeType currentNode = (NodeType) o;
                    AbstractExternalData nodeData = currentNode.getNodeData();
                    if (nodeData != null) {
                        List<String> schemaIds = service.getRepositorySchemaIds(nodeData);
                        if (schemaIds != null && schemaIds.size() > 0) {
                            for (String schemaId : schemaIds) {
                                Relation addedRelation = new Relation();
                                addedRelation.setId(schemaId);
                                addedRelation.setType(RelationshipItemBuilder.SCHEMA_RELATION);
                                addedRelation.setVersion(RelationshipItemBuilder.LATEST_VERSION);
                                relationSet.add(addedRelation);
                            }
                        }
                    }
                }
            }
        }
        return relationSet;
    }

}
