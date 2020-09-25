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
package org.talend.designer.core.generic.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.param.EConnectionParameterName;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.ElementParameterParser;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.runtime.maven.MavenUrlHelper;
import org.talend.core.runtime.services.IGenericDBService;
import org.talend.core.utils.TalendQuoteUtils;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.properties.property.Property;
import org.talend.librariesmanager.model.service.CustomUriManager;

/**
 * created by nrousseau on Apr 28, 2016 Detailled comment
 *
 */
public class GenericTableUtils {

    public static void setTableValues(Properties tableProperties, List<Map<String, Object>> value, IElementParameter param) {
        List<Map<String, String>> table = ElementParameterParser.createTableValues(value, param);
        IGenericDBService dbService = null;
        if (GlobalServiceRegister.getDefault().isServiceRegistered(IGenericDBService.class)) {
            dbService = (IGenericDBService) GlobalServiceRegister.getDefault().getService(
                    IGenericDBService.class);
        }
        for (String column : param.getListItemsDisplayCodeName()) {
            Property property = tableProperties.getValuedProperty(column);
            if (property.getValue() instanceof List) {
                property.setValue(new ArrayList<>());
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
                List<String> valueList = new ArrayList<>();
                if(dbService != null){
                    for(String v:values){
                        if(param.getName().equals(EConnectionParameterName.GENERIC_DRIVER_JAR.getDisplayName())){
                            v = dbService.getMVNPath(v);
                        }
                        valueList.add(v);
                    }
                    property.setValue(valueList);
                }
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
                        } else if (values.size() > i){
                            if(param.getName().equals(EConnectionParameterName.GENERIC_DRIVER_JAR.getDisplayName())){
                                line.put(columnName, getDriverJarPath((String)values.get(i)));
                            }else{
                                line.put(columnName, values.get(i));
                            }
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

    public static String getDriverJarPaths(List<String> listString){
        StringBuffer jars = null;
        if (listString == null) {
            return null;
        }
        for(String str : listString){
            if(jars == null){
                jars = new StringBuffer();
            }else{
                jars.append(";");
            }
            jars.append(getDriverJarPath(str));
        }
        if(jars == null){
            return null;
        }
        return jars.toString();
    }

    public static String getDriverJarPath(String mvnPath){
        String mvnUrl = TalendQuoteUtils.removeQuotesIfExist(mvnPath);
        if (MavenUrlHelper.isMvnUrl(mvnUrl)) {
            for(String key : CustomUriManager.getInstance().keySet()){
                String value = CustomUriManager.getInstance().get(key);
                if(mvnUrl.equals(value)){
                    mvnUrl = key;
                    break;
                }
            }
        }
        return mvnUrl;
    }
}
