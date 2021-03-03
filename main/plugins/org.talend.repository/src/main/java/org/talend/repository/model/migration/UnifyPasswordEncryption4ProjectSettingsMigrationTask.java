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
import java.util.List;

import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.utils.PasswordEncryptUtil;
import org.talend.core.model.general.Project;
import org.talend.core.model.migration.AbstractProjectMigrationTask;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.properties.ImplicitContextSettings;
import org.talend.core.model.properties.StatAndLogsSettings;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.cwm.helper.ConnectionHelper;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.ParametersType;
import org.talend.designer.core.model.utils.emf.talendfile.impl.ElementParameterTypeImpl;

/**
 * created by ggu on Aug 21, 2014 Detailled comment
 *
 */
public class UnifyPasswordEncryption4ProjectSettingsMigrationTask extends AbstractProjectMigrationTask {

    ProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();

    /*
     * (non-Javadoc)
     *
     * @see org.talend.migration.IProjectMigrationTask#execute(org.talend.core.model.general.Project)
     */
    @Override
    public ExecutionResult execute(Project project) {
        org.talend.core.model.properties.Project emfProject = project.getEmfProject();
        StatAndLogsSettings statAndLogs = emfProject.getStatAndLogsSettings();
        boolean modified = false;
        if (statAndLogs != null && statAndLogs.getParameters() != null) {
            ParametersType parameters = statAndLogs.getParameters();
            List elementParameter = parameters.getElementParameter();
            for (int i = 0; i < elementParameter.size(); i++) {
                final Object object = elementParameter.get(i);
                if (object instanceof ElementParameterType) {
                    ElementParameterType parameterType = (ElementParameterType) object;
                    String name = parameterType.getName();
                    // variable name used for Stat&Logs
                    if ("PASS".equals(name)) { //$NON-NLS-1$
                        parameterType.setField(EParameterFieldType.PASSWORD.getName());
                        try {
                            if (reencryptValueIfNeeded(parameterType)) {
                                modified = true;
                            }
                        } catch (Exception e) {
                            ExceptionHandler.process(e);
                            return ExecutionResult.FAILURE;
                        }
                    }
                }
            }

        }
        ImplicitContextSettings implicitContext = emfProject.getImplicitContextSettings();
        if (implicitContext != null && implicitContext.getParameters() != null) {
            ParametersType parameters = implicitContext.getParameters();
            List elementParameter = parameters.getElementParameter();
            for (int i = 0; i < elementParameter.size(); i++) {
                final Object object = elementParameter.get(i);
                if (object instanceof ElementParameterTypeImpl) {
                    ElementParameterType parameterType = (ElementParameterType) object;
                    String name = parameterType.getName();
                    // variable name used for implicit context
                    if ("PASS_IMPLICIT_CONTEXT".equals(name)) { //$NON-NLS-1$
                        parameterType.setField(EParameterFieldType.PASSWORD.getName());
                        try {
                            if (reencryptValueIfNeeded(parameterType)) {
                                modified = true;
                            }
                        } catch (Exception e) {
                            ExceptionHandler.process(e);
                            return ExecutionResult.FAILURE;
                        }
                    }
                }
            }
        }
        if (modified) {
            try {
                factory.saveProject(project);
                return ExecutionResult.SUCCESS_NO_ALERT;
            } catch (PersistenceException e) {
                ExceptionHandler.process(e);
                return ExecutionResult.FAILURE;
            }
        }
        return ExecutionResult.NOTHING_TO_DO;
    }

    private boolean reencryptValueIfNeeded(ElementParameterType param) throws Exception {
        String value = param.getValue();
        value = ConnectionHelper.getCleanPassword(value);
        int index = value.lastIndexOf(PasswordEncryptUtil.ENCRYPT_KEY);
        if (index != -1) {
            value = new StringBuilder(value).replace(index, index + PasswordEncryptUtil.ENCRYPT_KEY.length(), "").toString(); //$NON-NLS-1$
            String rawValue = PasswordEncryptUtil.decryptPassword(value);
            value = rawValue;
        }
        param.setRawValue(value);
        return true;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.migration.IProjectMigrationTask#getOrder()
     */
    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2014, 8, 22, 12, 0, 0);
        return gc.getTime();
    }

}
