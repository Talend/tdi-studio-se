// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.repository.resource.delete;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.core.model.metadata.types.JavaTypesManager;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.properties.Property;
import org.talend.core.model.relationship.Relation;
import org.talend.core.model.relationship.RelationshipItemBuilder;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.repository.model.AbstractCheckDeleteItemReference;
import org.talend.core.repository.model.ItemReferenceBean;
import org.talend.core.repository.ui.actions.DeleteActionCache;
import org.talend.designer.core.model.utils.emf.talendfile.ContextParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.ContextType;
import org.talend.repository.model.IProxyRepositoryFactory;

/**
 * DOC jding  class global comment. Detailled comment
 */
public class CheckJobResourceDeleteReference extends AbstractCheckDeleteItemReference {

    private static final String REPACE_SLASH_TAG = "\\|";

    /* (non-Javadoc)
     * @see org.talend.core.repository.model.AbstractCheckDeleteItemReference#checkItemReferenceBeans(org.talend.repository.model.IProxyRepositoryFactory, org.talend.core.repository.ui.actions.DeleteActionCache, org.talend.core.model.repository.IRepositoryViewObject)
     */
    @Override
    protected Collection<ItemReferenceBean> checkItemReferenceBeans(IProxyRepositoryFactory factory,
            DeleteActionCache deleteActionCache, IRepositoryViewObject repoObject) {
        String resourceId = repoObject.getId();
        Set<ItemReferenceBean> list = new HashSet<ItemReferenceBean>();
        List<Relation> relations = RelationshipItemBuilder.getInstance().getAllVersionItemsRelatedTo(resourceId,
                RelationshipItemBuilder.RESOURCE_RELATION, true);
        if (relations.isEmpty()) {
            return list;
        }
        try {
            for (Relation relation : relations) {
                IRepositoryViewObject relatedObj = factory.getSpecificVersion(relation.getId(), relation.getVersion(), true);
                if (relatedObj == null) {
                    // redundant relation
                    continue;
                }
                final Property property = relatedObj.getProperty();
                final Item item = property.getItem();
                String resRelatedVersion = getResourceRelatedVersion(resourceId, relatedObj);
                if (resRelatedVersion != null) {
                    final ItemReferenceBean bean = new ItemReferenceBean();
                    bean.setItemName(repoObject.getLabel());
                    bean.setItemVersion(resRelatedVersion);
                    bean.setItemType(ERepositoryObjectType.RESOURCES);
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
            }
        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
        }
        return list;
    }

    private String getResourceRelatedVersion(String resourceId, IRepositoryViewObject relatedObj) {
        Item item = relatedObj.getProperty().getItem();
        ProcessItem processItem = null;
        if (item instanceof ProcessItem) {
            processItem = (ProcessItem) item;
        } else {
            return null;
        }

        Set<String> resourceList = new HashSet<String>();
        List<ContextType> contexts = processItem.getProcess().getContext();
        for (ContextType context : contexts) {
            List<ContextParameterType> contextParameter = context.getContextParameter();
            for (ContextParameterType contextParameterType : contextParameter) {
                if (JavaTypesManager.RESOURCE.getId().equals(contextParameterType.getType())
                        || JavaTypesManager.RESOURCE.getLabel().equals(contextParameterType.getType())) {
                    resourceList.add(contextParameterType.getValue());
                }
            }
        }
        if (resourceList.isEmpty()) {
            return null;
        }
        for (String res : resourceList) {
            if (StringUtils.isBlank(res)) {
                continue;
            }
            String[] resInfo = res.split(REPACE_SLASH_TAG);
            if (resInfo.length > 1) {
                if (resourceId.equals(resInfo[0])) {
                    return resInfo[1];
                }
            }
        }
        return null;
    }

}
