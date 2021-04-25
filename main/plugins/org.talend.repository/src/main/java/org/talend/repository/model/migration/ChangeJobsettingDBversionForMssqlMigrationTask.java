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
import org.talend.core.database.conn.version.EDatabaseVersion4Drivers;
import org.talend.core.model.migration.AbstractJobMigrationTask;
import org.talend.core.model.properties.Item;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;
import org.talend.designer.core.model.utils.emf.talendfile.impl.ElementParameterTypeImpl;

/**
 * created by hcyi on Nov 29, 2017 Detailled comment
 *
 */
public class ChangeJobsettingDBversionForMssqlMigrationTask extends AbstractJobMigrationTask {

    private static final ProxyRepositoryFactory FACTORY = ProxyRepositoryFactory.getInstance();

    @Override
    public ExecutionResult execute(Item item) {
        ProcessType processType = getProcessType(item);
        if(processType == null){
            return ExecutionResult.NOTHING_TO_DO;
        }
        if (processType.getParameters() != null) {
            @SuppressWarnings("rawtypes")
            EList elementParameter = processType.getParameters().getElementParameter();
            boolean modified = false;
            for (int i = 0; i < elementParameter.size(); i++) {
                final Object object = elementParameter.get(i);
                if (object instanceof ElementParameterTypeImpl) {
                    ElementParameterTypeImpl parameterType = (ElementParameterTypeImpl) object;
                    String name = parameterType.getName();
                    if ("DB_TYPE".equals(name) && parameterType.getValue().startsWith("tMSSql")) { //$NON-NLS-1$//$NON-NLS-2$
                        modified = updateJarValue(elementParameter);
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

    private boolean updateJarValue(EList elementParameter) {
        for (int i = 0; i < elementParameter.size(); i++) {
            final Object object = elementParameter.get(i);
            if (object instanceof ElementParameterTypeImpl) {
                ElementParameterTypeImpl parameterType = (ElementParameterTypeImpl) object;
                String name = parameterType.getName();
                if ("DB_VERSION".equals(name)) {//$NON-NLS-1$
                    String value = parameterType.getValue();
                    if (value.equals(EDatabaseVersion4Drivers.MSSQL.getVersionValue())
                            || value.equals(EDatabaseVersion4Drivers.MSSQL_PROP.getVersionValue())) {
                        // do nothing
                    } else {
                        parameterType.setValue(EDatabaseVersion4Drivers.MSSQL.getVersionValue());
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2017, 11, 29, 17, 0, 0);
        return gc.getTime();
    }
}
