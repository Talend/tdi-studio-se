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

public class NewJDBCMigrationTask extends NewComponentFrameworkMigrationTask {

    @Override
    public Date getOrder() {
        return new GregorianCalendar(2017, 8, 18, 18, 0, 0).getTime();
    }

    @Override
    protected Properties getPropertiesFromFile() {
        Properties props = new Properties();
        InputStream in = getClass().getResourceAsStream("NewJDBCMigrationTask.properties");//$NON-NLS-1$
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
            if ("CONNECTION".equals(paramName) && value != null && !"".equals(value)) {
                ElementParameterType useConnection = ParameterUtilTool.findParameterType(node, "USE_EXISTING_CONNECTION");
                if (useConnection != null
                        && !Boolean.valueOf(String.valueOf(ParameterUtilTool.convertParameterValue(useConnection)))) {
                    return null;
                } else {
                    return paramType;
                }
            }
        }
        return paramType;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.designer.core.generic.model.migration.NewComponentFrameworkMigrationTask#isRepositoryParam(java.lang
     * .String)
     */
    @Override
    protected boolean isRepositoryParam(String paramName) {
        if (paramName.equals("connection.jdbcUrl") || paramName.equals("connection.driverTable")
                || paramName.equals("connection.driverClass") || paramName.equals("connection.userPassword.userId")
                || paramName.equals("connection.userPassword.password") || paramName.equals("mappingFile")) {

            return true;
        } else {
            return false;
        }
    }
}
