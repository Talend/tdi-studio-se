// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
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

import org.apache.commons.lang.StringUtils;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.properties.ComponentReferenceProperties;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.IProcess;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.properties.PropertiesVisitor;
import org.talend.daikon.properties.property.Property;
import org.talend.designer.core.generic.constants.IGenericConstants;
import org.talend.designer.core.generic.utils.ComponentsUtils;
import org.talend.repository.generic.model.genericMetadata.GenericConnection;
import org.talend.repository.generic.model.genericMetadata.GenericConnectionItem;

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
    public static boolean synNamePropertyWithItem(GenericConnectionItem item) {
        GenericConnection connection = (GenericConnection) item.getConnection();
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

}
