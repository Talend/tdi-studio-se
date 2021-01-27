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

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.Property;
import org.talend.core.model.relationship.Relation;
import org.talend.core.model.relationship.RelationshipItemBuilder;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.repository.model.AbstractCheckDeleteItemReference;
import org.talend.core.repository.model.ItemReferenceBean;
import org.talend.core.repository.ui.actions.DeleteActionCache;
import org.talend.repository.model.IProxyRepositoryFactory;

/**
 * DOC jding  class global comment. Detailled comment
 */
public class CheckCodeJarsDeleteReference extends AbstractCheckDeleteItemReference {

    @Override
    protected Collection<ItemReferenceBean> checkItemReferenceBeans(IProxyRepositoryFactory factory,
            DeleteActionCache deleteActionCache, IRepositoryViewObject repoObject) {
        Set<ItemReferenceBean> list = new HashSet<ItemReferenceBean>();
        ERepositoryObjectType objectType = repoObject.getRepositoryObjectType();
        if (!ERepositoryObjectType.getAllTypesOfCodesJar().contains(objectType)) {
            return list;
        }
        if (ERepositoryObjectType.ROUTINESJAR != null && ERepositoryObjectType.ROUTINESJAR.equals(objectType)) {
            collectDeleteReference(factory, repoObject, RelationshipItemBuilder.ROUTINES_JAR_RELATION, list);
        } else if (ERepositoryObjectType.BEANSJAR != null && ERepositoryObjectType.BEANSJAR.equals(objectType)) {
            collectDeleteReference(factory, repoObject, RelationshipItemBuilder.BEANS_JAR_RELATION, list);
        }

        return list;
    }

    private void collectDeleteReference(IProxyRepositoryFactory factory, IRepositoryViewObject repoObject, String relationType,
            Set<ItemReferenceBean> list) {
        String codeJarId = repoObject.getId();
        List<Relation> relations = RelationshipItemBuilder.getInstance().getAllVersionItemsRelatedTo(codeJarId, relationType,
                true);
        if (relations.isEmpty()) {
            return;
        }
        try {
            for (Relation relation : relations) {
                IRepositoryViewObject relatedObj = factory.getSpecificVersion(relation.getId(), relation.getVersion(), true);
                if (relatedObj == null) {
                    continue;
                }
                final Property property = relatedObj.getProperty();
                final Item item = property.getItem();
                final ItemReferenceBean bean = new ItemReferenceBean();
                bean.setItemName(repoObject.getLabel());
                bean.setItemVersion(repoObject.getVersion());
                bean.setItemType(repoObject.getRepositoryObjectType());
                bean.setItemDeleted(repoObject.isDeleted());
                bean.setReferenceItemName(property.getLabel());
                bean.setReferenceItemVersion(property.getVersion());
                bean.setReferenceItemType(ERepositoryObjectType.getItemType(item));
                bean.setReferenceItemPath(item.getState().getPath());
                bean.setReferenceProjectName(relatedObj.getProjectLabel());
                bean.setReferenceItemDeleted(relatedObj.isDeleted());
                if (!list.add(bean)) {
                    for (ItemReferenceBean b : list) {
                        if (b.equals(bean)) {
                            b.addNodeNum();
                            break;
                        }
                    }
                }
            }
        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
        }
    }

}
