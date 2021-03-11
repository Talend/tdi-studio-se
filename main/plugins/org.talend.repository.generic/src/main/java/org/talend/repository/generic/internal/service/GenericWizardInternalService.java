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
package org.talend.repository.generic.internal.service;

import java.lang.reflect.Constructor;
import java.util.List;

import org.talend.commons.exception.ExceptionHandler;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.service.ComponentService;
import org.talend.components.api.wizard.ComponentWizard;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.designer.core.generic.utils.ComponentsUtils;
import org.talend.repository.generic.internal.IGenericWizardInternalService;
import org.talend.repository.model.IRepositoryNode.ENodeType;
import org.talend.repository.model.IRepositoryNode.EProperties;
import org.talend.repository.model.RepositoryNode;

/**
 * created by ycbai on 2015年9月21日 Detailled comment
 *
 */
public class GenericWizardInternalService implements IGenericWizardInternalService {

    @Override
    public ComponentService getComponentService() {
        return ComponentsUtils.getComponentService();
    }

    @Override
    public ComponentWizard getComponentWizard(String name, String location) {
        return getComponentService().getComponentWizard(name, location);
    }

    @Override
    public List<ComponentWizard> getComponentWizardsForProperties(ComponentProperties properties, String location) {
        return getComponentService().getComponentWizardsForProperties(properties, location);
    }

    @Override
    public ComponentWizard getTopLevelComponentWizard(ComponentProperties properties, String location) {
        List<ComponentWizard> componentWizards = getComponentWizardsForProperties(properties, location);
        for (ComponentWizard componentWizard : componentWizards) {
            if (componentWizard.getDefinition().isTopLevel()) {
                return componentWizard;
            }
        }
        return null;
    }

    @Override
    public RepositoryNode createRepositoryNode(RepositoryNode curParentNode, String label, ERepositoryObjectType type,
            ENodeType nodeType) {
        RepositoryNode dynamicNode = new RepositoryNode(null, (RepositoryNode) curParentNode.getRoot(), nodeType);
        dynamicNode.setProperties(EProperties.LABEL, label);
        dynamicNode.setProperties(EProperties.CONTENT_TYPE, type);
        curParentNode.getChildren().add(dynamicNode);
        return dynamicNode;
    }

    @Override
    public ERepositoryObjectType createRepositoryType(String type, String label, String alias, String folder, int ordinal) {
        Constructor<ERepositoryObjectType> dynamicConstructor = getConstructor(ERepositoryObjectType.class, new Class[] {
                String.class, String.class, String.class, String.class, int.class, String[].class });
        ERepositoryObjectType typeObject = null;
        try {
            dynamicConstructor.setAccessible(true);
            typeObject = dynamicConstructor.newInstance(type, label, alias, folder, ordinal,
                    new String[] { ERepositoryObjectType.PROD_DI });
            typeObject.setAParent(ERepositoryObjectType.METADATA);
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }
        return typeObject;
    }

    private <E> Constructor<E> getConstructor(Class<E> clazz, Class<?>[] argTypes) {
        for (Class<?> c = clazz; c != null; c = c.getSuperclass()) {
            try {
                return (Constructor<E>) c.getDeclaredConstructor(argTypes);
            } catch (Exception e) {
                continue;
            }
        }
        return null;
    }

}
