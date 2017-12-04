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
package org.talend.designer.core.generic.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.ElementParameterParser;
import org.talend.core.model.process.IElementParameter;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.properties.property.Property;

/**
 * created by nrousseau on Apr 28, 2016 Detailled comment
 *
 */
public class GenericTableUtils {

    public static void setTableValues(Properties tableProperties, List<Map<String, Object>> value, IElementParameter param) {
        List<Map<String, String>> table = ElementParameterParser.createTableValues(value, param);
        for (String column : param.getListItemsDisplayCodeName()) {
            Property property = tableProperties.getValuedProperty(column);
            if (property.getValue() instanceof List) {
                ((List) property.getValue()).clear();
            }
        }
        for (Map<String, String> line : table) {
            for (String column : line.keySet()) {
                Property property = tableProperties.getValuedProperty(column);
                List<String> values = new ArrayList<>();
                if (property.getValue() == null || (!(property.getValue() instanceof List))) {
                    property.setValue(values);
                } else {
                    values = (List) property.getValue();
                }
                values.add(line.get(column));
            }
        }
    }

    public static List<Map<String, Object>> getTableValues(Properties tableProperties, IElementParameter param) {
        List<Map<String, Object>> table = new ArrayList<Map<String, Object>>();
        if (param.getListItemsDisplayCodeName().length == 0) {
            return table;
        }

        Property property = tableProperties.getValuedProperty(param.getListItemsDisplayCodeName()[0]);
        Object value = property.getValue();
        if (value instanceof List) {
            for (int i = 0; i < ((List) value).size(); i++) {
                Map<String, Object> line = new HashMap<String, Object>();
                for (String columnName : param.getListItemsDisplayCodeName()) {
                    Property columnProperty = tableProperties.getValuedProperty(columnName);
                    EParameterFieldType type = getTypeFromColumnName(param, columnName);
                    Object columnValue = columnProperty.getValue();
                    if (columnValue instanceof List) {
                        List values = (List) columnValue;
                        if (type.equals(EParameterFieldType.CHECK)) {
                            if (values.size() > i) {
                                Object o = values.get(i);
                                if (o == null) {
                                    line.put(columnName, Boolean.FALSE);
                                } else {
                                    line.put(columnName, new Boolean(o.toString()));
                                }
                            } else {
                                line.put(columnName, Boolean.FALSE);
                            }
                        } else {
                            line.put(columnName, values.get(i));
                        }
                    } else {
                        if (type.equals(EParameterFieldType.CHECK)) {
                            for (int j = 0; j < ((List) value).size(); j++) {
                                line.put(columnName, Boolean.FALSE);
                            }
                        }
                    }
                }
                table.add(line);
            }

        }
        return table;
    }

    /**
     * DOC nrousseau Comment method "getTypeFromColumnName".
     * 
     * @param param
     * @param columnName
     * @return
     */
    private static EParameterFieldType getTypeFromColumnName(IElementParameter param, String columnName) {
        for (Object o : param.getListItemsValue()) {
            if (o instanceof IElementParameter) {
                IElementParameter elemParam = (IElementParameter) o;
                if (columnName.equals(elemParam.getName())) {
                    return elemParam.getFieldType();
                }
            }
        }
        return EParameterFieldType.TEXT;
    }
}
