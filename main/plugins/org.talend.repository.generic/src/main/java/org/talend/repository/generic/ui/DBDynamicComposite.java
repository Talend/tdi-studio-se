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
package org.talend.repository.generic.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.talend.core.database.EDatabaseTypeName;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.metadata.builder.connection.impl.DatabaseConnectionImpl;
import org.talend.core.model.param.EConnectionParameterName;
import org.talend.core.model.process.EComponentCategory;
import org.talend.core.model.process.Element;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.daikon.properties.presentation.Form;
import org.talend.designer.core.generic.model.GenericElementParameter;

/**
 * DOC hwang  class global comment. Detailled comment
 */
public class DBDynamicComposite extends DynamicComposite{

    public DBDynamicComposite(Composite parentComposite, int styles, EComponentCategory section, Element element,
            ConnectionItem connectionItem, boolean isCompactView, Color backgroundColor, Form form, boolean drivedByForm) {
        super(parentComposite, styles, section, element, connectionItem, isCompactView, backgroundColor, form, drivedByForm);
    }

    @Override
    protected boolean isShouldCreateControl(IElementParameter curParam) {
        if(curParam.getName().equals("name")){
            return false;
        }
        return super.isShouldCreateControl(curParam);
    }

    @Override
    protected void editJDBCParameter(boolean isForEdite, Connection dbConnection,
            GenericElementParameter genericElementParameter) {
        if (dbConnection != null && dbConnection.isContextMode()) {
            return;
        }
        if (isForEdite && dbConnection != null && dbConnection instanceof DatabaseConnectionImpl && EDatabaseTypeName.GENERAL_JDBC
                .getProduct().equalsIgnoreCase(((DatabaseConnectionImpl) dbConnection).getDatabaseType())) {
            if (genericElementParameter.getName().equals(EConnectionParameterName.GENERIC_URL.getDisplayName())) {
                genericElementParameter
                        .setValue(StringUtils.isEmpty(((DatabaseConnectionImpl) dbConnection).getURL()) ? "jdbc:"
                                : ((DatabaseConnectionImpl) dbConnection).getURL());
            }
            if (genericElementParameter.getName().equals(EConnectionParameterName.GENERIC_DRIVER_JAR.getDisplayName())) {

                if (!StringUtils.isEmpty(((DatabaseConnectionImpl) dbConnection).getURL())
                        && !StringUtils.isEmpty(((DatabaseConnectionImpl) dbConnection).getDriverJarPath())) {
                    String driverJarPath = ((DatabaseConnectionImpl) dbConnection).getDriverJarPath();
                    List<Map<String, String>> list = new ArrayList<Map<String, String>>();
                    String[] split = driverJarPath.split(";");
                    for (String jar : split) {
                        if (!StringUtils.isEmpty(jar)) {
                            Map<String, String> map = new HashMap<String, String>();
                            map.put("drivers", jar);
                            list.add(map);
                        }

                    }
                    genericElementParameter.setValue(list);
                }
            }
            if (genericElementParameter.getName()
                    .equalsIgnoreCase(EConnectionParameterName.GENERIC_DRIVER_CLASS.getDisplayName())) {
                genericElementParameter.setValue(((DatabaseConnectionImpl) dbConnection).getDriverClass());
            }
            if (genericElementParameter.getName().equals(EConnectionParameterName.GENERIC_USERNAME.getDisplayName())) {
                genericElementParameter.setValue(((DatabaseConnectionImpl) dbConnection).getUsername());
            }
            if (genericElementParameter.getName().equals(EConnectionParameterName.GENERIC_PASSWORD.getDisplayName())) {
                genericElementParameter.setValue(((DatabaseConnectionImpl) dbConnection).getRawPassword());
            }
            if (genericElementParameter.getName().equals(EConnectionParameterName.GENERIC_MAPPING_FILE.getDisplayName())
                    && !StringUtils.isEmpty(((DatabaseConnectionImpl) dbConnection).getDbmsId())) {
                genericElementParameter.setValue(((DatabaseConnectionImpl) dbConnection).getDbmsId());
            }
        }

    }

    public void setForm(Form form) {
        // set a new form must reset parameter later
        super.form = form;
    }
}
