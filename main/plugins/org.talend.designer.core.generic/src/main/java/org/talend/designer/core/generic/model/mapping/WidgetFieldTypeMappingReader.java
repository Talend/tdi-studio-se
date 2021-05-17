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

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.SafeRunner;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.utils.RegistryReader;
import org.talend.daikon.NamedThing;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.Property;
import org.talend.designer.core.generic.constants.IGenericConstants;

/**
 * created by hcyi on Jan 21, 2016 Detailled comment
 *
 */
class WidgetFieldTypeMappingReader extends RegistryReader {

    private static final Logger log = Logger.getLogger(WidgetFieldTypeMappingReader.class);

    private static final String WIDGET_FIELD_TYPE_ELEMENT_NAME = "Mapping"; //$NON-NLS-1$

    private Map<String, String> allDynamicWidgetFieldType;

    private static WidgetFieldTypeMappingReader instance;

    public static WidgetFieldTypeMappingReader getInstance() {
        if (instance == null) {
            synchronized (WidgetFieldTypeMappingReader.class) {
                if (instance == null) {
                    instance = new WidgetFieldTypeMappingReader();
                }
            }
        }
        return instance;
    }

    protected WidgetFieldTypeMappingReader() {
        super(IGenericConstants.PLUGIN_ID, "fieldType_mapping"); //$NON-NLS-1$
    }

    void init() {
        if (this.allDynamicWidgetFieldType == null) {
            synchronized (WidgetFieldTypeMappingReader.class) {
                this.allDynamicWidgetFieldType = new HashMap<String, String>();
                readRegistry();
            }
        }
    }

    @Override
    protected boolean readElement(final IConfigurationElement element) {
        if (WIDGET_FIELD_TYPE_ELEMENT_NAME.equals(element.getName())) {
            SafeRunner.run(new RegistryReader.RegistrySafeRunnable() {

                @Override
                public void run() throws Exception {
                    String name = element.getAttribute("name"); //$NON-NLS-1$
                    String mapFieldType = element.getAttribute("mapFieldType"); //$NON-NLS-1$
                    allDynamicWidgetFieldType.put(name, mapFieldType);
                }
            });
            return true;
        }
        return false;
    }

    public String getFieldType(String widgetType, NamedThing widgetProperty, String schemaType) {
        init();
        String fieldType = null;
        if (Widget.DEFAULT_WIDGET_TYPE.equals(widgetType)) {
            if (schemaType == null) {
                return EParameterFieldType.LABEL.getName();
            }
            if (widgetProperty != null && widgetProperty instanceof Property) {
                Property prop = (Property) widgetProperty;
                if (prop.isFlag(Property.Flags.ENCRYPT)) {
                    return EParameterFieldType.HIDDEN_TEXT.getName();
                }
            }
            fieldType = this.allDynamicWidgetFieldType.get(widgetType + "+" + schemaType);//$NON-NLS-1$
            if (fieldType != null) {
                return fieldType;
            } else {
                return EParameterFieldType.TEXT.getName();
            }
        } else {
            return this.allDynamicWidgetFieldType.get(widgetType);
        }
    }
}
