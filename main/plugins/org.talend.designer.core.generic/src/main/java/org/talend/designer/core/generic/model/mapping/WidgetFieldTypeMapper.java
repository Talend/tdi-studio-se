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
package org.talend.designer.core.generic.model.mapping;

import org.talend.core.model.process.EParameterFieldType;
import org.talend.daikon.NamedThing;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.EnumProperty;
import org.talend.daikon.properties.property.Property;

/**
 * created by hcyi on Jan 21, 2016 Detailled comment
 *
 */
public class WidgetFieldTypeMapper {

    private static final WidgetFieldTypeMappingReader mappingReader = new WidgetFieldTypeMappingReader();

    public static EParameterFieldType getFieldType(Widget widget, NamedThing widgetProperty) {
        // init
        mappingReader.init();
        Property property = null;
        if (widgetProperty instanceof Property) {
            property = (Property) widgetProperty;
        }
        String fieldType = mappingReader.getFieldType(widget.getWidgetType(), widgetProperty, property != null ? property.getType() : null);
        if (widgetProperty instanceof EnumProperty) {
            return EParameterFieldType.CLOSED_LIST;
        }
        return fieldType != null ? EParameterFieldType.valueOf(fieldType.toUpperCase()) : null;
    }
}
