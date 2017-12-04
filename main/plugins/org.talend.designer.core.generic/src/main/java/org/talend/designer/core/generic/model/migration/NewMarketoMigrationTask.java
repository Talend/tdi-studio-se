package org.talend.designer.core.generic.model.migration;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Properties;

import org.talend.core.utils.TalendQuoteUtils;
import org.talend.designer.core.generic.utils.ParameterUtilTool;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;

public class NewMarketoMigrationTask extends NewComponentFrameworkMigrationTask {

    @Override
    public Date getOrder() {
        return new GregorianCalendar(2017, 2, 24, 10, 0, 0).getTime();
    }

    @Override
    protected Properties getPropertiesFromFile() {
        Properties props = new Properties();
        InputStream in = getClass().getResourceAsStream("NewMarketoMigrationTask.properties");//$NON-NLS-1$
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
        //
        // Very old job didn't support REST API
        // This means it has neither USE_REST_API nor USE_SOAP_API so migrate to
        // SOAP
        //
        if ("USE_SOAP_API".equals(paramName) && paramType == null) {
            ParameterUtilTool.addParameterType(node, "RADIO", "USE_SOAP_API", "true");
            paramType = ParameterUtilTool.findParameterType(node, paramName);
        }
        // Parameter is unassigned, so "LEAD_SELECTOR" parameter isn't processed yet.
        if ("LEAD_SELECTOR_REST".equals(paramName) && paramType == null) {
            ElementParameterType leadSelector = ParameterUtilTool.findParameterType(node, "LEAD_SELECTOR");
            Object selValue = ParameterUtilTool.convertParameterValue(leadSelector);
            ParameterUtilTool.addParameterType(node, "CLOSED_LIST", "LEAD_SELECTOR_REST", String.valueOf(selValue));
            paramType = ParameterUtilTool.findParameterType(node, paramName);
        }
        //
        if (node != null && paramType != null) {
            String componentName = node.getComponentName();
            Object value = ParameterUtilTool.convertParameterValue(paramType);
            if ("USE_SOAP_API".equals(paramName)) {
                if ("true".equals(String.valueOf(value))) {
                    paramType.setValue("SOAP");
                } else {
                    paramType.setValue("REST");
                }
            }
            // Correct typo in tMarketoInput's operation since 563
            if ("OPERATION".equals(paramName) && "getMutipleLeads".equals(String.valueOf(value))) {
                paramType.setValue("getMultipleLeads");
            }
            // Correct ListOperation value
            if ("OPERATION".equals(paramName) && "tMarketoListOperation".equals(componentName)) {
                switch (String.valueOf(value)) {
                case "ADDTOLIST":
                    paramType.setValue("addTo");
                    break;
                case "ISMEMBEROFLIST":
                    paramType.setValue("isMemberOf");
                    break;
                case "REMOVEFROMLIST":
                    paramType.setValue("removeFrom");
                    break;
                }
            }
            // MAX_RETURN should be taken in account when
            // OPERATION=getMutipleLeads and LEAD_SELECTOR=LeadKeySelector
            // Otherwise, we feed the value with BATCH_SIZE.
            if ("MAX_RETURN".equals(paramName) && "tMarketoInput".equals(componentName)) {
                ElementParameterType operation = ParameterUtilTool.findParameterType(node, "OPERATION");
                ElementParameterType leadSelector = ParameterUtilTool.findParameterType(node, "LEAD_SELECTOR");
                ElementParameterType batchSize = ParameterUtilTool.findParameterType(node, "BATCH_SIZE");
                ElementParameterType maxReturn = ParameterUtilTool.findParameterType(node, "MAX_RETURN");
                Object batchSizeValue = ParameterUtilTool.convertParameterValue(batchSize);
                if (operation != null && leadSelector != null) {
                    Object operationValue = ParameterUtilTool.convertParameterValue(operation);
                    Object leadSelectorValue = ParameterUtilTool.convertParameterValue(leadSelector);
                    Object maxReturnValue = ParameterUtilTool.convertParameterValue(maxReturn);
                    if ("getMutipleLeads".equals(String.valueOf(operationValue))
                            && "LeadKeySelector".equals(String.valueOf(leadSelectorValue)) && maxReturnValue != null) {
                        paramType.setValue(String.valueOf(maxReturnValue));
                    } else {
                        paramType.setValue(String.valueOf(batchSizeValue));
                    }
                } else {
                    paramType.setValue(String.valueOf(batchSizeValue));
                }
            }

            if ("LEAD_KEYVALUES".equals(paramName)) {
                paramType.setValue(TalendQuoteUtils.addQuotesIfNotExist(String.valueOf(value)));
            }
            // creates a parameter for REST LeadSelector based on original value
            if ("LEAD_SELECTOR".equals(paramName)) {
                ParameterUtilTool.addParameterType(node, "CLOSED_LIST", "LEAD_SELECTOR_REST", String.valueOf(value));
            }
            // Manage include/exclude REST types
            if ("INCLUDE_TYPES".equals(paramName) || "EXCLUDE_TYPES".equals(paramName)) {
                ElementParameterType isSOAP = ParameterUtilTool.findParameterType(node, "USE_SOAP_API");
                Object isSOAPValue = ParameterUtilTool.convertParameterValue(isSOAP);
                if ("true".equals(String.valueOf(isSOAPValue))) {
                    return paramType;
                }
                String k = "INCLUDE_TYPES".equals(paramName) ? "INCLUDE_TYPES_REST" : "EXCLUDE_TYPES_REST";
                ElementParameterType rest = ParameterUtilTool.findParameterType(node, k);
                return rest;
            }
        }
        return paramType;
    }
}
