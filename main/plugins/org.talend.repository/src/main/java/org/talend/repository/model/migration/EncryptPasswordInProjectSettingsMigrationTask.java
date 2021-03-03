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
import org.talend.core.CorePlugin;
import org.talend.core.model.general.Project;
import org.talend.core.model.properties.ImplicitContextSettings;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.StatAndLogsSettings;
import org.talend.cwm.helper.ConnectionHelper;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.ParametersType;
import org.talend.designer.core.model.utils.emf.talendfile.impl.ElementParameterTypeImpl;
import org.talend.migration.AbstractMigrationTask;
import org.talend.migration.IProjectMigrationTask;
import org.talend.repository.model.IProxyRepositoryFactory;

/**
 * DOC hcyi class global comment. Detailled comment
 */
public class EncryptPasswordInProjectSettingsMigrationTask extends AbstractMigrationTask implements IProjectMigrationTask {

    /*
     * (non-Javadoc)
     *
     * @see org.talend.migration.IMigrationTask#getOrder()
     */
    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2014, 5, 27, 12, 0, 0);
        return gc.getTime();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.migration.IProjectMigrationTask#isApplicableOnItems()
     */
    @Override
    public boolean isApplicableOnItems() {
        // TODO Auto-generated method stub
        return false;
    }

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
                        try {
                            if (encryptValueIfNeeded(parameterType)) {
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
                        try {
                            if (encryptValueIfNeeded(parameterType)) {
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
                IProxyRepositoryFactory prf = CorePlugin.getDefault().getProxyRepositoryFactory();
                prf.saveProject(project);
                return ExecutionResult.SUCCESS_NO_ALERT;
            } catch (PersistenceException e) {
                ExceptionHandler.process(e);
                return ExecutionResult.FAILURE;
            }
        }
        return ExecutionResult.NOTHING_TO_DO;
    }

    private boolean encryptValueIfNeeded(ElementParameterType param) throws Exception {
        boolean modified = false;
        boolean encrypted = true;
        try {
            int ind = param.getValue().lastIndexOf(PasswordEncryptUtil.ENCRYPT_KEY);
            if (ind == -1) {
                encrypted = false;
            } else {
                String value = new StringBuilder(param.getValue()).replace(ind, ind + PasswordEncryptUtil.ENCRYPT_KEY.length(),
                        "").toString(); //$NON-NLS-1$
                PasswordEncryptUtil.decryptPassword(value);
            }
        } catch (Exception e) {
            encrypted = false;
        }

        if (!encrypted) {
            param.setValue(PasswordEncryptUtil.encryptPassword(ConnectionHelper.getCleanPassword(param.getValue()))
                    + PasswordEncryptUtil.ENCRYPT_KEY);
            modified = true;
        }
        return modified;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.migration.IProjectMigrationTask#execute(org.talend.core.model.general.Project, boolean)
     */
    @Override
    public ExecutionResult execute(Project project, boolean doSave) {
        return execute(project);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.migration.IProjectMigrationTask#execute(org.talend.core.model.general.Project,
     * org.talend.core.model.properties.Item)
     */
    @Override
    public ExecutionResult execute(Project project, Item item) {
        return ExecutionResult.NOTHING_TO_DO;
    }
}
