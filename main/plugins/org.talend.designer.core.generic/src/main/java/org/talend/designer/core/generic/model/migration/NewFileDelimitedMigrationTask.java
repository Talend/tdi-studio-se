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
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Properties;

import org.talend.designer.core.generic.utils.ParameterUtilTool;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;

public class NewFileDelimitedMigrationTask extends NewComponentFrameworkMigrationTask {

    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2016, 8, 19, 12, 0, 0);
        return gc.getTime();
    }

    @Override
    protected Properties getPropertiesFromFile() {
        Properties props = new Properties();
        InputStream in = getClass().getResourceAsStream("NewFileDelimitedMigrationTask.properties");//$NON-NLS-1$
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
        if (node != null && paramType != null) {
            Object value = ParameterUtilTool.convertParameterValue(paramType);
            if ("CSV_OPTION".equals(paramName)) {
                if("true".equals(String.valueOf(value))){
                    ElementParameterType csvRowSeparator = ParameterUtilTool.findParameterType(node, "CSVROWSEPARATOR");
                    ElementParameterType rowSeparator = ParameterUtilTool.findParameterType(node, "ROWSEPARATOR");
                    if(csvRowSeparator != null && rowSeparator != null){
                        rowSeparator.setValue(String.valueOf(ParameterUtilTool.convertParameterValue(csvRowSeparator)));
                    }
                }
            } else if ("FILENAME".equals(paramName) && "tFileOutputDelimited".equals(node.getComponentName())) {
                ElementParameterType useStream = ParameterUtilTool.findParameterType(node, "USESTREAM");
                if (useStream != null && Boolean.valueOf(String.valueOf(ParameterUtilTool.convertParameterValue(useStream)))) {
                    ElementParameterType streamName = ParameterUtilTool.findParameterType(node, "STREAMNAME");
                    paramType.setValue(String.valueOf(ParameterUtilTool.convertParameterValue(streamName)));
                }
            }
        }
        return paramType;
    }

}
