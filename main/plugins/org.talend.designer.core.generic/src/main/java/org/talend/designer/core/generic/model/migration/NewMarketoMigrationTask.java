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
        return new GregorianCalendar(2017, 3, 8, 10, 0, 0).getTime();
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
        }
        return paramType;
    }
}
