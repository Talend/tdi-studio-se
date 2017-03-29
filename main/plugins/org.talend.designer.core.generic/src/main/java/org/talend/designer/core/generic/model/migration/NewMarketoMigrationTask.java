package org.talend.designer.core.generic.model.migration;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Properties;

import org.talend.designer.core.generic.utils.ParameterUtilTool;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;

public class NewMarketoMigrationTask extends NewComponentFrameworkMigrationTask {

    Boolean hasAPI = Boolean.FALSE;

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
        // Not sure that it's the right place for that...
        //
        // Very old job didn't support REST API
        // This means it has neither USE_REST_API nor USE_SOAP_API so migrate to SOAP
        //
        if (node != null && !hasAPI) {
            ElementParameterType apiParamType = ParameterUtilTool.findParameterType(node, "USE_SOAP_API");
            if (apiParamType == null) {
               ParameterUtilTool.addParameterType(node, "RADIO","USE_SOAP_API", "true");
            }
            hasAPI = true;
        }
        //
        String componentName  = node.getComponentName();
        ElementParameterType paramType = ParameterUtilTool.findParameterType(node, paramName);
        if (node != null && paramType != null) {
            Object value = ParameterUtilTool.convertParameterValue(paramType);
            if("USE_SOAP_API".equals(paramName)){
                if("true".equals(String.valueOf(value))) {
                    paramType.setValue("SOAP");
                } else {
                    paramType.setValue("REST");
                }
            }
            // MAX_RETURN should be taken in account when OPERATION=getMutipleLeads and LEAD_SELECTOR=LeadKeySelector
            // Otherwise, we feed the value with BATCH_SIZE.
            if ("MAX_RETURN".equals(paramName) && "tMarketoInput".equals(componentName)) {
                ElementParameterType operation = ParameterUtilTool.findParameterType(node, "OPERATION");
                ElementParameterType leadSelector = ParameterUtilTool.findParameterType(node, "LEAD_SELECTOR");
                ElementParameterType batchSize = ParameterUtilTool.findParameterType(node, "BATCH_SIZE");
                if (operation != null && leadSelector != null) {
                    Object operationValue = ParameterUtilTool.convertParameterValue(operation);
                    Object leadSelectorValue = ParameterUtilTool.convertParameterValue(leadSelector);
                    if ( !("getMutipleLeads".equals(String.valueOf(operationValue))      &&
                           "LeadKeySelector".equals(String.valueOf(leadSelectorValue)) ) &&
                            batchSize != null) {
                         Object batchSizeValue = ParameterUtilTool.convertParameterValue(batchSize);
                        paramType.setValue(String.valueOf(batchSizeValue));
                    }
                }
            }
            // BATCH_SIZE should be taken in account when OPERATION!=getMutipleLeads and LEAD_SELECTOR!=LeadKeySelector
            // Otherwise, we feed the value with MAX_RETURN.
            if ("BATCH_SIZE".equals(paramName) && "tMarketoInput".equals(componentName)) {
                ElementParameterType operation = ParameterUtilTool.findParameterType(node, "OPERATION");
                ElementParameterType leadSelector = ParameterUtilTool.findParameterType(node, "LEAD_SELECTOR");
                ElementParameterType maxReturn = ParameterUtilTool.findParameterType(node, "MAX_RETURN");
                if (operation != null && leadSelector != null) {
                   Object operationValue = ParameterUtilTool.convertParameterValue(operation);
                   Object leadSelectorValue = ParameterUtilTool.convertParameterValue(leadSelector);
                   if ("getMutipleLeads".equals(String.valueOf(operationValue)) &&
                       "LeadKeySelector".equals(String.valueOf(leadSelectorValue)) &&
                        maxReturn != null) {
                        Object maxReturnValue = ParameterUtilTool.convertParameterValue(maxReturn);
                        paramType.setValue(String.valueOf(maxReturnValue));
                   }
                }
            }
        }
        return paramType;
    }
}
