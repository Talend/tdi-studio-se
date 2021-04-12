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
package org.talend.designer.core.generic.model.migration;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.talend.components.common.oauth.OAuth2FlowType;
import org.talend.daikon.NamedThing;
import org.talend.daikon.properties.property.Property;
import org.talend.designer.core.generic.model.GenericElementParameter;
import org.talend.designer.core.generic.utils.ParameterUtilTool;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.ElementValueType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;

/**
 * created by hcyi on Apr 7, 2016 Detailled comment
 *
 */
public class NewSalesforceMigrationTask extends NewComponentFrameworkMigrationTask {

    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2016, 04, 18, 12, 0, 0);
        return gc.getTime();
    }

    @Override
    protected Properties getPropertiesFromFile() {
        Properties props = new Properties();
        InputStream in = getClass().getResourceAsStream("NewSalesforceMigrationTask.properties");//$NON-NLS-1$
        try {
            props.load(in);
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return props;
    }

    @Override
    protected ElementParameterType getParameterType(NodeType node, String paramName) {
    	ElementParameterType paramType = ParameterUtilTool.findParameterType(node, paramName);
    	if(node != null && paramType != null){
    		Object value = ParameterUtilTool.convertParameterValue(paramType);
    		if("MODULENAME".equals(paramName) && "CustomModule".equals(value)){
    			paramName = "CUSTOM_MODULE_NAME";
    			if("tSalesforceInput".equals(node.getComponentName())){
    				paramName = "CUSTOM_MODULE";
    			}
        		ElementParameterType customModuleName = ParameterUtilTool.findParameterType(node, paramName);
        		if(customModuleName != null){
            		String ModuleName = StringUtils.strip(String.valueOf(ParameterUtilTool.convertParameterValue(customModuleName)),"\"");
            		paramType.setValue(ModuleName.replaceAll("\r|\n|\r\n", ""));
        		}
            }else if("CONNECTION".equals(paramName) && value!=null && !"".equals(value)){
        		ElementParameterType useConnection = ParameterUtilTool.findParameterType(node, "USE_EXISTING_CONNECTION");
        		if(useConnection!=null && Boolean.valueOf(String.valueOf(ParameterUtilTool.convertParameterValue(useConnection)))){
        			return paramType;
        		}else{
        			return null;
        		}
        	}else if("API".equals(paramName)){
        		if("soap".equals(value)){
        			paramType.setValue("Query");
        		}else{
        			paramType.setValue("Bulk");
        		}
            } else if ("LOGIN_TYPE".equals(paramName)) {
            	if("BASIC".equals(value)) {
            		paramType.setValue("Basic");
            	} else if("OAUTH".equals(value)) {
            		paramType.setValue("OAuth");

            	}
            }
    	}
    	return paramType;
    }

	public Object getTableValue(ElementParameterType paramType) {

		List<Map<String, Object>> tableValue = null;
		if ("UPSERT_RELATION".equals(paramType.getName())) {
			List<ElementValueType> columns = paramType.getElementValue();
			if (columns != null && columns.size() > 0) {
				Map<String, String> columnMapping = new HashMap<String, String>() {
					{
						put("COLUMN_NAME", "columnName");
						put("LOOKUP_FIELD_NAME", "lookupFieldName");
						put("FIELD_NAME", "fieldName");
						put("LOOKUP_FIELD_MODULE_NAME", "lookupFieldModuleName");
						put("POLYMORPHIC", "polymorphic");
						put("LOOKUP_FIELD_EXTERNAL_ID_NAME",
								"lookupFieldExternalIdName");
					}
				};
				tableValue = new ArrayList<Map<String, Object>>();
				Map<String, Object> line = null;
				for (ElementValueType column : columns) {
					if ("COLUMN_NAME".equals(column.getElementRef())) {
						if (line != null) {
							tableValue.add(line);
						}
						line = new HashMap<String, Object>();
					}
					line.put(columnMapping.get(column.getElementRef()), column.getValue());
				}
				tableValue.add(line);
			}
		}
		return tableValue;
	}

    @Override
    protected void processUnmappedElementParameter(Properties props, NodeType nodeType, GenericElementParameter param,
            NamedThing currNamedThing) {

        if ("tSalesforceInput".equals(nodeType.getComponentName()) && "returnNullValue".equals(param.getName())) {
            if (currNamedThing instanceof Property) {
                ((Property<?>) currNamedThing).setStoredValue(true);
            }
        } else if ("oauth2FlowType".equals(param.getName()) || "connection.oauth2FlowType".equals(param.getName())) {
            if (currNamedThing instanceof Property) {
                ((Property<?>) currNamedThing).setStoredValue(OAuth2FlowType.Implicit_Flow);
            }
        } else {
            super.processUnmappedElementParameter(props, nodeType, param, currNamedThing);
        }
    }
}
