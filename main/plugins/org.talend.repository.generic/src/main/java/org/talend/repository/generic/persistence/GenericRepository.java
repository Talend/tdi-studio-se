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
package org.talend.repository.generic.persistence;

import java.util.List;

import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.cwm.helper.PackageHelper;
import org.talend.daikon.properties.service.Repository;
import org.talend.designer.core.generic.constants.IGenericConstants;
import org.talend.designer.core.generic.utils.ComponentsUtils;
import org.talend.designer.core.generic.utils.SchemaUtils;
import org.talend.repository.generic.model.genericMetadata.GenericMetadataFactory;
import org.talend.repository.generic.model.genericMetadata.SubContainer;
import orgomg.cwm.objectmodel.core.ModelElement;

/**
 * created by ycbai on 2015年9月29日 Detailled comment
 *
 */
public class GenericRepository implements Repository<ComponentProperties> {

    @Override
    public String storeProperties(ComponentProperties properties, String name, String repositoryLocation,
            String schemaPropertyName) {
        if (properties == null) {
            throw new RuntimeException("Properties argument cannot be null!"); //$NON-NLS-1$
        }

        // Add repository value if it is from repository
        List<org.talend.daikon.properties.property.Property> propertyValues = ComponentsUtils.getAllValuedProperties(properties);
        for (org.talend.daikon.properties.property.Property property : propertyValues) {
            property.setTaggedValue(IGenericConstants.REPOSITORY_VALUE, property.getName());
        }

        String serializedProperties = properties.toSerialized();
        if (repositoryLocation.contains(IGenericConstants.REPOSITORY_LOCATION_SEPARATOR)) {// nested properties to be
            ConnectionItem item = getGenericConnectionItem(repositoryLocation.substring(0,
                    repositoryLocation.indexOf(IGenericConstants.REPOSITORY_LOCATION_SEPARATOR)));
            if (item == null) {
                throw new RuntimeException("Failed to find the GenericConnectionItem for location:" + repositoryLocation); //$NON-NLS-1$
            }
            Connection connection = (Connection) item.getConnection();
            orgomg.cwm.objectmodel.core.Package parentContainer = null;
            if (repositoryLocation.endsWith(IGenericConstants.REPOSITORY_LOCATION_SEPARATOR)) {// first nested property
                parentContainer = connection;
            } else {
                parentContainer = getContainer(connection, repositoryLocation);
            }
            ModelElement childElement = null;
            if (schemaPropertyName == null) {// If schema property name is not provided, then create the subcontainer.
                childElement = createContainer(name, serializedProperties);
            } else {
                // Remove Repository Value tag value for schema property.
                org.talend.daikon.properties.property.Property<?> schemaProperty = properties
                        .getValuedProperty(schemaPropertyName);
                if (schemaProperty != null) {
                    schemaProperty.setTaggedValue(IGenericConstants.REPOSITORY_VALUE, null);
                }
//                SchemaUtils.createCatalog(name, properties, connection, schemaPropertyName);
                childElement = SchemaUtils.createSchema(name, properties, schemaPropertyName);
            }
            parentContainer.getOwnedElement().add(childElement);
            return repositoryLocation + IGenericConstants.REPOSITORY_LOCATION_SEPARATOR + name;
        } else {// simple properties to be set
            ConnectionItem item = getGenericConnectionItem(repositoryLocation);
            if (item != null) {
                Connection connection = (Connection) item.getConnection();
                connection.setCompProperties(serializedProperties);
                connection.getOwnedElement().clear();
                try {
                    // in case compProperties lost
                    ProxyRepositoryFactory.getInstance().save(item);
                } catch (PersistenceException e) {
                    ExceptionHandler.process(e);
                }
                return repositoryLocation + IGenericConstants.REPOSITORY_LOCATION_SEPARATOR;
            } else {
                throw new RuntimeException("Failed to find the GenericConnectionItem for location:" + repositoryLocation); //$NON-NLS-1$
            }
        }
    }

    private SubContainer createContainer(String containerName, String serializedProperties) {
        SubContainer subContainer = GenericMetadataFactory.eINSTANCE.createSubContainer();
        subContainer.setName(containerName);
        subContainer.setCompProperties(serializedProperties);
        return subContainer;
    }

    private SubContainer getContainer(Connection connection, String repositoryLocation) {
        SubContainer theContainer = null;
        String containers = repositoryLocation;
        if (containers.indexOf(IGenericConstants.REPOSITORY_LOCATION_SEPARATOR) != -1) {
            containers = containers.substring(repositoryLocation.indexOf(IGenericConstants.REPOSITORY_LOCATION_SEPARATOR) + 1);
            String[] containersArray = containers.split(IGenericConstants.REPOSITORY_LOCATION_SEPARATOR);
            for (String container : containersArray) {
                if (theContainer == null) {
                    theContainer = getTheContainer(connection, container);
                } else {
                    theContainer = getTheContainer(theContainer, container);
                    if (theContainer == null) {
                        throw new RuntimeException("Failed to find the SubContainer named:" + container); //$NON-NLS-1$
                    }
                }
            }
        }
        return theContainer;
    }

    private SubContainer getTheContainer(orgomg.cwm.objectmodel.core.Package parentPackage, String containerName) {
        List<SubContainer> subContainers = PackageHelper.getOwnedElements(parentPackage, SubContainer.class);
        for (SubContainer subContainer : subContainers) {
            if (containerName != null && containerName.equals(subContainer.getName())) {
                return subContainer;
            }
        }
        return null;
    }

    /**
     * DOC sgandon Comment method "getGenericConnectionItem".
     *
     * @param repositoryLocation
     * @return
     */
    private ConnectionItem getGenericConnectionItem(String repositoryLocation) {
        ConnectionItem genItem = null;
        try {
            IRepositoryViewObject repViewObj = ProxyRepositoryFactory.getInstance().getLastVersion(repositoryLocation);
            if (repViewObj != null) {
                Property property = repViewObj.getProperty();
                if (property != null) {
                    Item item = property.getItem();
                    if (item instanceof ConnectionItem) {
                        genItem = (ConnectionItem) item;
                    }
                }
            }
        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
        }
        return genItem;
    }

}
