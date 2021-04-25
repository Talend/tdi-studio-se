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
package org.talend.repository.resource.handler;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.emf.ecore.resource.Resource;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.image.IImage;
import org.talend.core.model.properties.FileItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.resources.ResourceItem;
import org.talend.core.model.resources.ResourcesFactory;
import org.talend.core.model.resources.ResourcesPackage;
import org.talend.core.repository.utils.AbstractResourceRepositoryContentHandler;
import org.talend.repository.resource.ui.util.EResourcesImage;


/**
 * DOC jding  class global comment. Detailled comment
 */
public class ResourcesRepositoryContentHandler extends AbstractResourceRepositoryContentHandler {

    @Override
    public Resource create(IProject project, Item item, int classifierID, IPath path) throws PersistenceException {
        if (item.eClass() == ResourcesPackage.Literals.RESOURCE_ITEM) {
            return create(project, (FileItem) item, path, ERepositoryObjectType.RESOURCES);
        }
        return null;
    }

    @Override
    public Resource save(Item item) throws PersistenceException {
        if (item.eClass() == ResourcesPackage.Literals.RESOURCE_ITEM) {
            return save((ResourceItem) item);
        }
        return null;
    }

    @Override
    public Item createNewItem(ERepositoryObjectType type) {
        Item item = null;
        if (type == ERepositoryObjectType.RESOURCES) {
            item = ResourcesFactory.eINSTANCE.createResourceItem();
        }
        return item;
    }

    @Override
    public boolean isRepObjType(ERepositoryObjectType type) {
        return type == ERepositoryObjectType.RESOURCES;
    }

    @Override
    public ERepositoryObjectType getRepositoryObjectType(Item item) {
        if (item.eClass() == ResourcesPackage.Literals.RESOURCE_ITEM) {
            return ERepositoryObjectType.RESOURCES;
        }
        return null;
    }

    @Override
    public IImage getIcon(ERepositoryObjectType type) {
        if (type == ERepositoryObjectType.RESOURCES) {
            return EResourcesImage.RESOURCE_ICON;
        }
        return null;
    }

}
