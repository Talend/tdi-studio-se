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
package org.talend.repository.view.di.viewer.handlers;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.talend.core.model.relationship.AbstractJobParameterRelationshipHandler;
import org.talend.core.model.relationship.Relation;
import org.talend.core.model.relationship.RelationshipItemBuilder;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.ElementValueType;

/**
 * DOC ggu class global comment. Detailled comment
 */
public class SqlTemplateParameterRelationshipHandler extends AbstractJobParameterRelationshipHandler {

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.relationship.AbstractParameterRelationshipHandler#collect(java.util.Map,
     * java.util.Map)
     */
    @Override
    protected Set<Relation> collect(Map<String, ElementParameterType> parametersMap, Map<?, ?> options) {

        Set<Relation> relationSet = new HashSet<Relation>();

        ElementParameterType sqlpatternParamType = parametersMap.get("SQLPATTERN_VALUE"); //$NON-NLS-1$
        // only for SQL Patterns
        if (sqlpatternParamType != null) {
            for (Object o3 : sqlpatternParamType.getElementValue()) {
                if (o3 instanceof ElementValueType && "SQLPATTERNLIST".equals(((ElementValueType) o3).getElementRef())) { //$NON-NLS-1$

                    Relation addedRelation = new Relation();
                    addedRelation.setId(((ElementValueType) o3).getValue());
                    addedRelation.setType(RelationshipItemBuilder.SQLPATTERN_RELATION);
                    addedRelation.setVersion(RelationshipItemBuilder.LATEST_VERSION);
                    relationSet.add(addedRelation);
                }
            }
        }
        return relationSet;
    }

}
