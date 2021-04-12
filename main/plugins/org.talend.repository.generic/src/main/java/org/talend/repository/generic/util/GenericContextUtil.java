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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.language.LanguageManager;
import org.talend.core.model.context.ContextUtils;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.metadata.types.JavaType;
import org.talend.core.model.metadata.types.JavaTypesManager;
import org.talend.core.model.process.IContextParameter;
import org.talend.core.model.properties.ContextItem;
import org.talend.core.model.utils.ContextParameterUtils;
import org.talend.core.runtime.util.GenericTypeUtils;
import org.talend.core.ui.context.model.table.ConectionAdaptContextVariableModel;
import org.talend.core.utils.TalendQuoteUtils;
import org.talend.daikon.NamedThing;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.properties.property.Property;
import org.talend.designer.core.generic.constants.IGenericConstants;
import org.talend.designer.core.generic.utils.ComponentsUtils;
import org.talend.designer.core.model.utils.emf.talendfile.ContextType;
import org.talend.metadata.managment.ui.model.IConnParamName;
import org.talend.metadata.managment.ui.utils.ConnectionContextHelper;
import org.talend.metadata.managment.ui.utils.GenericConnParamName;

/**
 * created by ycbai on 2015年11月27日 Detailled comment
 *
 */
public class GenericContextUtil {

    public static List<IContextParameter> createContextParameters(String prefixName, Connection connection,
            Set<IConnParamName> paramSet) {
        List<IContextParameter> varList = new ArrayList<>();

        if (connection == null || prefixName == null || paramSet == null || paramSet.isEmpty()) {
            return Collections.emptyList();
        }
        String paramPrefix = prefixName + ConnectionContextHelper.LINE;
        String paramName = null;
        for (IConnParamName param : paramSet) {
            if (param instanceof GenericConnParamName) {
                GenericConnParamName connParamName = (GenericConnParamName) param;
                String name = connParamName.getName();
                ComponentProperties componentProperties = getComponentProperties(connection);
                Property<?> property = componentProperties.getValuedProperty(name);
                paramName = paramPrefix + connParamName.getContextVar();
                if(property != null){
                    JavaType type = JavaTypesManager.STRING;
                    if (property.isFlag(Property.Flags.ENCRYPT)) {
                        type = JavaTypesManager.PASSWORD;
                    }
                    if (GenericTypeUtils.isIntegerType(property)) {
                        type = JavaTypesManager.INTEGER;
                    } else if (GenericTypeUtils.isLongType(property)) {
                        type = JavaTypesManager.LONG;
                    } else if (GenericTypeUtils.isFloatType(property)) {
                        type = JavaTypesManager.FLOAT;
                    } else if (GenericTypeUtils.isDoubleType(property)) {
                        type = JavaTypesManager.DOUBLE;
                    }
                    String value = property == null || property.getValue() == null ? null
                            : StringEscapeUtils.escapeJava(String.valueOf(property.getValue()));
                    ConnectionContextHelper.createParameters(varList, paramName, value, type);
                }else{
                    Properties properties = componentProperties.getProperties(name);
                    if(properties != null){
                        List<Map<String, Object>> valueMap = getPropertiesValue(connection, properties, name);
                        for(Map<String, Object> map : valueMap){
                            for(String key : map.keySet()){
                                Object propertyValue = map.get(key);
                                String keyWithPrefix = prefixName + ConnectionContextHelper.LINE + ContextParameterUtils.getValidParameterName(key);
                                if(propertyValue instanceof List){
//                                    ConnectionContextHelper.createParameters(varList, keyWithPrefix, propertyValue, JavaTypesManager.VALUE_LIST);

                                    String value = null;
                                    for(Object obj : (List)propertyValue){
                                        if(value == null){
                                            value = obj.toString();
                                        }else{
                                            value = value + ";" + obj.toString(); //$NON-NLS-1$
                                        }
                                    }
                                    ConnectionContextHelper.createParameters(varList, keyWithPrefix, value, JavaTypesManager.STRING);
                                }
                            }
                        }
                    }
                }

            }
        }

        return varList;
    }

    private static ComponentProperties getComponentProperties(Connection connection) {
        String compPropertiesStr = connection.getCompProperties();
        if (compPropertiesStr != null) {
            return ComponentsUtils.getComponentPropertiesFromSerialized(compPropertiesStr, connection);
        }
        return null;
    }

    private static List<Map<String, Object>> getPropertiesValue(Connection connection, Properties properties,String value){
        List<Map<String, Object>> lines = new ArrayList<Map<String, Object>>();
        for(NamedThing nameThing : properties.getProperties()){
            if(nameThing != null && nameThing instanceof Property){
                Property property = (Property) nameThing;
                Object paramValue = property.getStoredValue();
                if(GenericTypeUtils.isListStringType(property) && paramValue != null){
                    List<String> listString = (List<String>) paramValue;
                    Map<String, Object> line = new LinkedHashMap<String, Object>();
                    line.put(property.getName(),listString);
                    lines.add(line);
                }
            }
        }
        return lines;
    }


    /**
     * update component properties for context name change DOC jding Comment method
     * "updateCompPropertiesForContextMode".
     * 
     * @param connection DatabaseConnection
     * @param contextVarMap <key:OldContextName, value:NewContextName>
     */
    public static void updateCompPropertiesForContextMode(Connection connection, Map<String, String> contextVarMap) {
        ComponentProperties componentProperties = getComponentProperties(connection);
        if (componentProperties == null) {
            return;
        }
        findOutPropertiesToUpdate(componentProperties, contextVarMap);
        updateComponentProperties(connection, componentProperties);
    }

    private static void findOutPropertiesToUpdate(Properties componentProperties, Map<String, String> contextVarMap) {
        for (NamedThing namedThing : componentProperties.getProperties()) {
            if (namedThing==null) {
                continue;
            }
            if (namedThing instanceof Property) {
                Property property = (Property) namedThing;
                Object paramValue = property.getStoredValue();
                if (paramValue != null) {
                    if (GenericTypeUtils.isListStringType(property)) {
                        List<String> listString = (List<String>) paramValue;
                        for (int i = 0; i < listString.size(); i++) {
                            String str = listString.get(i);
                            if (StringUtils.isNotBlank(str) && contextVarMap.get(str) != null) {
                                listString.set(i, contextVarMap.get(str));
                            }
                        }
                    } else {
                        String newVlue = contextVarMap.get(paramValue.toString());
                        if (newVlue != null) {
                            property.setValue(newVlue);
                        }
                    }
                }
            } else if (namedThing instanceof Properties) {
                Properties compProp = (Properties) namedThing;
                findOutPropertiesToUpdate(compProp, contextVarMap);
            }
        }
    }

    public static void setPropertiesForContextMode(String prefixName, Connection connection, Set<IConnParamName> paramSet) {
        if (connection == null) {
            return;
        }
        ComponentProperties componentProperties = getComponentProperties(connection);
        String originalVariableName = prefixName + ConnectionContextHelper.LINE;
        String genericVariableName = null;
        for (IConnParamName param : paramSet) {
            if (param instanceof GenericConnParamName) {
                GenericConnParamName genericParam = (GenericConnParamName) param;
                String paramVarName = genericParam.getContextVar();
                genericVariableName = originalVariableName + paramVarName;
                matchContextForAttribues(componentProperties, genericParam, genericVariableName, prefixName,false);
            }
        }
        updateComponentProperties(connection, componentProperties);

    }

    public static void setPropertiesForExistContextMode(Connection connection, Set<IConnParamName> paramSet,
            Map<ContextItem, List<ConectionAdaptContextVariableModel>> adaptMap) {
        if (connection == null) {
            return;
        }
        ComponentProperties componentProperties = getComponentProperties(connection);
        ContextItem currentContext = null;
        for (IConnParamName param : paramSet) {
            if (param instanceof GenericConnParamName) {
                String genericVariableName = null;
                GenericConnParamName genericParam = (GenericConnParamName) param;
                if (adaptMap != null && adaptMap.size() > 0) {
                    for (Map.Entry<ContextItem, List<ConectionAdaptContextVariableModel>> entry : adaptMap.entrySet()) {
                        currentContext = entry.getKey();
                        List<ConectionAdaptContextVariableModel> modelList = entry.getValue();
                        for (ConectionAdaptContextVariableModel model : modelList) {
                            if (model.getValue().equals(genericParam.getName())) {
                                genericVariableName = model.getName();
                                break;
                            }
                        }
                    }
                }
                if (genericVariableName != null) {
                    genericVariableName = getCorrectVariableName(currentContext, genericVariableName, genericParam);
                    matchContextForAttribues(componentProperties, genericParam, genericVariableName, currentContext.getProperty().getLabel(),true);
                }
            }

        }
        updateComponentProperties(connection, componentProperties);

    }

    private static String getCorrectVariableName(ContextItem contextItem, String originalVariableName,
            GenericConnParamName genericParam) {
        Set<String> contextVarNames = ContextUtils.getContextVarNames(contextItem);
        if (contextVarNames != null && !contextVarNames.contains(originalVariableName)) {
            for (String varName : contextVarNames) {
                if (varName.endsWith(genericParam.getName())) {
                    return varName;
                }
            }
        }
        return originalVariableName;
    }

    private static void matchContextForAttribues(ComponentProperties componentProperties, IConnParamName param,
            String genericVariableName, String prefixName,boolean isReuse) {
        GenericConnParamName genericParam = (GenericConnParamName) param;
        String paramName = genericParam.getName();
        Properties properties = componentProperties.getProperties(paramName);
        if(properties == null){
            String paramValue = ContextParameterUtils.getNewScriptCode(genericVariableName, ECodeLanguage.JAVA);
            setPropertyValue(componentProperties, paramName, paramValue, true);
        }else{
            matchContextForPrperties(properties, param, genericVariableName, prefixName,isReuse);
        }
    }

    private static void matchContextForPrperties(Properties properties,IConnParamName param,
            String genericVariableName, String prefixName,boolean isReuse){
        for(NamedThing nameThing : properties.getProperties()){
            if(nameThing != null && nameThing instanceof Property){
                Property property = (Property) nameThing;
                Object paramValue = property.getStoredValue();
                if(GenericTypeUtils.isListStringType(property) && paramValue != null){
//                    String propertyValue = ContextParameterUtils.getNewScriptCode(prefixName + ConnectionContextHelper.LINE
//                            + ContextParameterUtils.getValidParameterName(property.getName()), LanguageManager.getCurrentLanguage());
                	String propertyValue = "";//for driver's param: fixdb_old_ ==> context.fixdb_old_
                	if(isReuse) {
                		 propertyValue = ContextParameterUtils.getNewScriptCode(genericVariableName, ECodeLanguage.JAVA);
                	}else {
                		propertyValue = ContextParameterUtils.getNewScriptCode(prefixName + ConnectionContextHelper.LINE
                              + ContextParameterUtils.getValidParameterName(property.getName()), LanguageManager.getCurrentLanguage());
                	}
                    property.setTaggedValue(IGenericConstants.IS_CONTEXT_MODE, true);
                    List<String> driverList = new ArrayList<String>();
                    driverList.add(propertyValue);
                    property.setValue(driverList);
                }
            }
        }
    }

    private static void updateComponentProperties(Connection conn, ComponentProperties componentProperties) {
        String serializedProperties = componentProperties.toSerialized();
        conn.setCompProperties(serializedProperties);
    }

    public static void revertPropertiesForContextMode(Connection connection, ContextType contextType) {
        ComponentProperties componentProperties = getComponentProperties(connection);
        revertPropertiesValues(componentProperties, contextType);
        updateComponentProperties(connection, componentProperties);
    }

    public static void revertPropertiesValues(Properties componentProperties, ContextType contextType) {
        List<NamedThing> props = componentProperties.getProperties();
        for (NamedThing namedThing : props) {
            if (namedThing instanceof Properties) {
                revertPropertiesValues((Properties) namedThing, contextType);
            } else if (namedThing instanceof Property) {
                Property property = (Property) namedThing;
                if (ComponentsUtils.isSupportContext(property)) {
                    String value = String.valueOf(property.getStoredValue());
                    Object storedValue =  property.getStoredValue();
                    if (value != null && ContextParameterUtils.isContainContextParam(value)) {
                        if(storedValue instanceof List){
                            List<String> valueList = ContextParameterUtils.getOriginalList(contextType, value);
                            property.setTaggedValue(IGenericConstants.IS_CONTEXT_MODE, false);
                            property.setValue(valueList);
                        }else{
                            String valueFromContext = ContextParameterUtils.getOriginalValue(contextType, value);
                            property.setTaggedValue(IGenericConstants.IS_CONTEXT_MODE, false);
                            if (GenericTypeUtils.isBooleanType(property)) {
                                property.setValue(new Boolean(valueFromContext));
                            } else if (GenericTypeUtils.isIntegerType(property) && !valueFromContext.isEmpty()) {
                                property.setValue(Integer.valueOf(valueFromContext));
                            } else if (GenericTypeUtils.isLongType(property) && !valueFromContext.isEmpty()) {
                                property.setValue(Long.valueOf(valueFromContext));
                            } else if (GenericTypeUtils.isFloatType(property) && !valueFromContext.isEmpty()) {
                                property.setValue(Float.valueOf(valueFromContext));
                            } else if (GenericTypeUtils.isDoubleType(property) && !valueFromContext.isEmpty()) {
                                property.setValue(Double.valueOf(valueFromContext));
                            } else if (GenericTypeUtils.isEnumType(property)) {
                                List<?> propertyPossibleValues = ((Property<?>) property).getPossibleValues();
                                if (propertyPossibleValues != null) {
                                    for (Object possibleValue : propertyPossibleValues) {
                                        if (possibleValue.toString().equals(valueFromContext)) {
                                            property.setValue(possibleValue);
                                        }
                                    }
                                }
                            }else {
                                property.setValue(TalendQuoteUtils.removeQuotes(valueFromContext));
                            }
                        }
                    }
                }
            }
        }
    }

    private static void setPropertyValue(ComponentProperties componentProperties, String propertyName, String propertyValue,
            boolean isContextMode) {
        Property property = componentProperties.getValuedProperty(propertyName);
        if (property != null) {
            property.setTaggedValue(IGenericConstants.IS_CONTEXT_MODE, isContextMode);
            property.setValue(propertyValue);
        }
    }

}
