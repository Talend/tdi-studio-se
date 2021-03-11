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
package org.talend.repository.generic.util;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.properties.ComponentReferenceProperties;
import org.talend.components.api.wizard.ComponentWizard;
import org.talend.components.api.wizard.ComponentWizardDefinition;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.properties.PropertiesVisitor;
import org.talend.daikon.properties.property.Property;
import org.talend.designer.core.generic.constants.IGenericConstants;
import org.talend.designer.core.generic.utils.ComponentsUtils;
import org.talend.repository.model.IRepositoryNode;

/**
 * created by ycbai on 2016年8月18日 Detailled comment
 *
 */
public class GenericConnectionUtil {

    /**
     * Syncronize the value of <code>name</code> property between component properties and connection item.
     *
     * @param item the item which name property belong to.
     * @return return true if property is updated, otherwise return false;
     */
    public static boolean synNamePropertyWithItem(ConnectionItem item) {
        Connection connection = item.getConnection();
        String compPropertiesStr = connection.getCompProperties();
        if (compPropertiesStr == null) {
            return false;
        }
        ComponentProperties componentProperties = ComponentsUtils.getComponentPropertiesFromSerialized(compPropertiesStr,
                connection);
        if (componentProperties == null) {
            return false;
        }
        Property nameProperty = (Property) componentProperties.getProperty(IGenericConstants.NAME_PROPERTY);
        if (nameProperty == null) {
            return false;
        }
        Object namePropertyVal = nameProperty.getValue();
        String newName = item.getProperty().getLabel();
        if (newName != null && !newName.equals(namePropertyVal)) {
            nameProperty.setValue(newName);
            connection.setCompProperties(componentProperties.toSerialized());
            return true;
        }
        return false;
    }

    public static void synRefProperty(ComponentReferenceProperties<?> refProperties, IProcess process) {
        String refCompInstId = null;
        Property<String> refCompInstIdProp = refProperties.componentInstanceId;
        if (refCompInstIdProp != null) {
            refCompInstId = refCompInstIdProp.getValue();
        }
        if (refCompInstId != null && StringUtils.isNotEmpty(refCompInstId)) {
            for (INode curNode : process.getGeneratingNodes()) {
                if (curNode.getUniqueName().equals(refCompInstId)) {
                    refProperties.setReference(curNode.getComponentProperties());
                    break;
                }
            }
        } else {
            refProperties.setReference(null);
        }

    }

    public static void synRefProperties(Properties properties, IProcess process) throws Exception {
        properties.accept(new PropertiesVisitor() {

            @Override
            public void visit(Properties curProperties, Properties parent) {
                if (curProperties instanceof ComponentReferenceProperties<?>) {
                    synRefProperty((ComponentReferenceProperties) curProperties, process);
                }
            }
        }, null);
    }

    public static List<ComponentWizard> getAllWizards(IRepositoryNode node) {
        if (node == null) {
            return Collections.EMPTY_LIST;
        }
        IRepositoryViewObject repObj = node.getObject();
        if (repObj == null) {
            return Collections.EMPTY_LIST;
        }
        Item item = repObj.getProperty().getItem();
        if(!(item instanceof ConnectionItem)){
            return Collections.EMPTY_LIST;
        }
        ConnectionItem gitem = (ConnectionItem) item;
        Connection connection = (Connection) gitem.getConnection();
        if(connection.getCompProperties() == null){
            return Collections.EMPTY_LIST;
        }
        ComponentProperties componentProperties = ComponentsUtils
                .getComponentPropertiesFromSerialized(connection.getCompProperties(), connection);
        List<ComponentWizard> wizards = GenericWizardServiceFactory.getGenericWizardInternalService()
                .getComponentWizardsForProperties(componentProperties, gitem.getProperty().getId());
        return wizards;
    }

    public static ComponentWizard getEditWizard(IRepositoryNode node) {
        ComponentWizard editWizard = null;
        List<ComponentWizard> allWizards = getAllWizards(node);
        if (allWizards != null && !allWizards.isEmpty()) {
            for (ComponentWizard wizard : allWizards) {
                ComponentWizardDefinition wizardDefinition = wizard.getDefinition();
                if (wizardDefinition.isTopLevel()) {
                    continue;
                }
                String wizardName = wizardDefinition.getName();
                if (wizardName.toLowerCase().contains("edit")) { //$NON-NLS-1$
                    editWizard = wizard;
                    break;
                }
            }
        }
        return editWizard;

    }

}
