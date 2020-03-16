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
package org.talend.designer.core.utils;

import java.util.List;
import java.util.Map;

import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.genhtml.IJobSettingConstants;
import org.talend.core.model.param.EConnectionParameterName;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.IProcess;
import org.talend.core.runtime.services.IGenericDBService;
import org.talend.designer.core.model.components.EParameterName;

/**
 * DOC hwang  class global comment. Detailled comment
 */
public class JobSettingUtil {
    
    public static void setTCOMImplicitParameter(INode targetNode, IProcess process) {
        if (targetNode.getElementParameter(EParameterName.LOCAL_SERVICE_NAME.getName()) != null) {
            targetNode.getElementParameter(EParameterName.LOCAL_SERVICE_NAME.getName())
                    .setValue(process.getElementParameter(IJobSettingConstants.LOCAL_SERVICE_NAME_IMPLICIT_CONTEXT).getValue());
        }

        if (targetNode.getElementParameter(EConnectionParameterName.GENERIC_URL.getDisplayName()) != null) {
            targetNode.getElementParameter(EConnectionParameterName.GENERIC_URL.getDisplayName())
                    .setValue(process.getElementParameter(IJobSettingConstants.URL_IMPLICIT_CONTEXT).getValue());
        }
        
        if (targetNode.getElementParameter(EConnectionParameterName.GENERIC_TABLENAME.getDisplayName()) != null) {
            targetNode.getElementParameter(EConnectionParameterName.GENERIC_TABLENAME.getDisplayName())
                    .setValue(process.getElementParameter(IJobSettingConstants.DBNAME_IMPLICIT_CONTEXT).getValue());
        }
        
        if (targetNode.getElementParameter("sql") != null) {
            targetNode.getElementParameter("sql")
                    .setValue(process.getElementParameter(IJobSettingConstants.QUERY_CONDITION_IMPLICIT_CONTEXT).getValue());
        }
        
        if (targetNode.getElementParameter(EConnectionParameterName.GENERIC_DRIVER_JAR.getDisplayName()) != null) {
            IElementParameter elePara = targetNode
                    .getElementParameter(EConnectionParameterName.GENERIC_DRIVER_JAR.getDisplayName());
            Object value = process.getElementParameter(IJobSettingConstants.DRIVER_JAR_IMPLICIT_CONTEXT).getValue();
            getMVNDriverJar(elePara, value);
            elePara.setValue(value);
        }

        if (targetNode.getElementParameter(EConnectionParameterName.GENERIC_DRIVER_CLASS.getDisplayName()) != null) {
            targetNode.getElementParameter(EConnectionParameterName.GENERIC_DRIVER_CLASS.getDisplayName())
                    .setValue(process.getElementParameter(IJobSettingConstants.DRIVER_CLASS_IMPLICIT_CONTEXT).getValue());
        }

        if (targetNode.getElementParameter(EConnectionParameterName.GENERIC_USERNAME.getDisplayName()) != null) {
            targetNode.getElementParameter(EConnectionParameterName.GENERIC_USERNAME.getDisplayName())
                    .setValue(process.getElementParameter(IJobSettingConstants.USER_IMPLICIT_CONTEXT).getValue());
        }

        if (targetNode.getElementParameter(EConnectionParameterName.GENERIC_PASSWORD.getDisplayName()) != null) {
            targetNode.getElementParameter(EConnectionParameterName.GENERIC_PASSWORD.getDisplayName())
                    .setValue(process.getElementParameter(IJobSettingConstants.PASS_IMPLICIT_CONTEXT).getValue());
        }

    }
    

    public static void getMVNDriverJar(IElementParameter elePara, Object value) {
        IGenericDBService dbService = null;
        if (GlobalServiceRegister.getDefault().isServiceRegistered(IGenericDBService.class)) {
            dbService = GlobalServiceRegister.getDefault().getService(IGenericDBService.class);
        }
        if (dbService == null) {
            return;
        }

        if (value instanceof List) {
            List objs = (List) value;
            for (Object obj : objs) {
                if (obj instanceof Map) {
                    Map map = (Map) obj;
                    String driver = (String) map.get("drivers");
                    if (elePara.getName().equals(EConnectionParameterName.GENERIC_DRIVER_JAR.getDisplayName())) {
                        driver = dbService.getMVNPath(driver);
                    }
                    map.put("drivers", driver);
                }
            }
        }
    }

}
