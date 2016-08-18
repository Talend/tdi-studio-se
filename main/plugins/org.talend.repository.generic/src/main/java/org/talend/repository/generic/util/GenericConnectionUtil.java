// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
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

import org.talend.components.api.properties.ComponentProperties;
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

}
