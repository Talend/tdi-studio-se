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
package org.talend.repository.model.migration;

import java.util.Date;
import java.util.GregorianCalendar;

import org.eclipse.emf.common.util.EList;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.database.EDatabaseTypeName;
import org.talend.core.database.conn.version.EDatabaseVersion4Drivers;
import org.talend.core.model.migration.AbstractJobMigrationTask;
import org.talend.core.model.properties.Item;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;
import org.talend.designer.core.model.utils.emf.talendfile.impl.ElementParameterTypeImpl;

/**
 * DOC hcyi class global comment. Detailled comment
 */
public class UpdateJobSettingsForMysqlMigrationTask extends AbstractJobMigrationTask {

    public UpdateJobSettingsForMysqlMigrationTask() {
        //
    }

    private static final ProxyRepositoryFactory FACTORY = ProxyRepositoryFactory.getInstance();

    private boolean updateJarValue(ElementParameterType param) {
        if ("mysql-connector-java-5.1.0-bin.jar".equalsIgnoreCase(param.getValue())//$NON-NLS-1$
                || EDatabaseVersion4Drivers.getDriversStr(EDatabaseTypeName.MYSQL.getDisplayName(),
                        EDatabaseVersion4Drivers.MYSQL_5.getVersionValue()).equalsIgnoreCase(param.getValue())) { //$NON-NLS-1$
            param.setValue("MYSQL_5"); //$NON-NLS-1$
            return true;
        }
        return false;
    }

    @Override
    public ExecutionResult execute(Item item) {
        ProcessType processType = getProcessType(item);
        if (processType.getParameters() != null) {
            @SuppressWarnings("rawtypes")
            EList elementParameter = processType.getParameters().getElementParameter();
            boolean modified = false;
            for (int i = 0; i < elementParameter.size(); i++) {
                final Object object = elementParameter.get(i);
                if (object instanceof ElementParameterTypeImpl) {
                    ElementParameterTypeImpl parameterType = (ElementParameterTypeImpl) object;
                    String name = parameterType.getName();
                    if ("DB_VERSION_IMPLICIT_CONTEXT".equals(name)) { //$NON-NLS-1$
                        modified = updateJarValue(parameterType) || modified;
                    }
                    if ("DB_VERSION".equals(name)) { //$NON-NLS-1$
                        modified = updateJarValue(parameterType) || modified;
                    }
                }
            }

            if (modified) {
                try {
                    FACTORY.save(item, true);
                    return ExecutionResult.SUCCESS_NO_ALERT;
                } catch (PersistenceException e) {
                    ExceptionHandler.process(e);
                    return ExecutionResult.FAILURE;
                }
            }
        }
        return ExecutionResult.NOTHING_TO_DO;

    }

    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2012, 11, 19, 12, 0, 0);
        return gc.getTime();
    }
}
