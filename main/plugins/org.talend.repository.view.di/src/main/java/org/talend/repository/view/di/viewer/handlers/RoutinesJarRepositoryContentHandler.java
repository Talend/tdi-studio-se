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

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.emf.ecore.resource.Resource;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.image.ECoreImage;
import org.talend.commons.ui.runtime.image.IImage;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.PropertiesPackage;
import org.talend.core.model.properties.Property;
import org.talend.core.model.properties.RoutinesJarItem;
import org.talend.core.model.repository.AbstractRepositoryContentHandler;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.repository.RepositoryViewObject;
import org.talend.core.model.routines.CodesJarInfo;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.repository.model.IRepositoryNode.ENodeType;
import org.talend.repository.model.IRepositoryNode.EProperties;
import org.talend.repository.model.RepositoryNode;

public class RoutinesJarRepositoryContentHandler extends AbstractRepositoryContentHandler {

    @Override
    public Resource create(IProject project, Item item, int classifierID, IPath path) throws PersistenceException {
        return null;
    }

    @Override
    public Resource save(Item item) throws PersistenceException {
        return null;
    }

    @Override
    public Item createNewItem(ERepositoryObjectType type) {
        return null;
    }

    @Override
    public boolean isRepObjType(ERepositoryObjectType type) {
        return ERepositoryObjectType.ROUTINESJAR == type;
    }

    @Override
    public ERepositoryObjectType getRepositoryObjectType(Item item) {
        if (item.eClass() == PropertiesPackage.Literals.ROUTINES_JAR_ITEM) {
            return ERepositoryObjectType.ROUTINESJAR;
        }
        return null;
    }

    @Override
    public IImage getIcon(ERepositoryObjectType type) {
        if (type == ERepositoryObjectType.ROUTINESJAR) {
            return ECoreImage.ROUTINESJAR_ICON;
        }
        return null;
    }

    @Override
    public void addNode(ERepositoryObjectType type, RepositoryNode recBinNode, IRepositoryViewObject repositoryObject,
            RepositoryNode node) {
        Property property = repositoryObject.getProperty();
        if (type != ERepositoryObjectType.ROUTINESJAR || !(property.getItem() instanceof RoutinesJarItem)) {
            return;
        }
        try {
            List<IRepositoryViewObject> innerRoutinesObj = ProxyRepositoryFactory.getInstance()
                    .getAllInnerCodes(CodesJarInfo.create(property));
            for (IRepositoryViewObject innerRoutineObj : innerRoutinesObj) {
                Property innerRoutineProperty = innerRoutineObj.getProperty();
                RepositoryNode innerRoutineNode = new RepositoryNode(new RepositoryViewObject(innerRoutineProperty),
                        node, ENodeType.REPOSITORY_ELEMENT);
                innerRoutineNode.setProperties(EProperties.LABEL, innerRoutineProperty.getLabel());
                innerRoutineNode.setProperties(EProperties.CONTENT_TYPE, ERepositoryObjectType.ROUTINES);
                node.getChildren().add(innerRoutineNode);
            }
        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
        }
    }

}
