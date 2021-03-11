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
package org.talend.designer.core.ui.preferences.migration;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.CorePlugin;
import org.talend.core.model.general.Project;
import org.talend.core.model.properties.ImplicitContextSettings;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.StatAndLogsSettings;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.ParametersType;
import org.talend.designer.core.model.utils.emf.talendfile.impl.ElementParameterTypeImpl;
import org.talend.migration.AbstractMigrationTask;
import org.talend.migration.IProjectMigrationTask;
import org.talend.repository.model.IProxyRepositoryFactory;

/**
 * wchen class global comment. Detailled comment
 */
public class ChangeOracleVersionForProjectSetting extends AbstractMigrationTask implements IProjectMigrationTask {

    /*
     * (non-Javadoc)
     *
     * @see org.talend.migration.IMigrationTask#getOrder()
     */
    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2013, 1, 4, 15, 0, 0);
        return gc.getTime();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.migration.IProjectMigrationTask#isApplicableOnItems()
     */
    @Override
    public boolean isApplicableOnItems() {
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
                if (object instanceof ElementParameterTypeImpl) {
                    ElementParameterTypeImpl parameterType = (ElementParameterTypeImpl) object;
                    String name = parameterType.getName();
                    if ("ON_DATABASE_FLAG".equals(name) && !Boolean.valueOf(parameterType.getValue())) {//$NON-NLS-1$
                        break;
                    }
                    if ("DB_VERSION".equals(name)) { //$NON-NLS-1$
                        modified = updateJarValue(parameterType) || modified;
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
                    ElementParameterTypeImpl parameterType = (ElementParameterTypeImpl) object;
                    String name = parameterType.getName();
                    if ("FROM_DATABASE_FLAG_IMPLICIT_CONTEXT".equals(name) && !Boolean.valueOf(parameterType.getValue())) {//$NON-NLS-1$
                        break;
                    }
                    if ("DB_VERSION_IMPLICIT_CONTEXT".equals(name)) { //$NON-NLS-1$
                        modified = updateJarValue(parameterType) || modified;
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

    private boolean updateJarValue(ElementParameterType param) {
        String jarValue = param.getValue();
        if ("ojdbc6.jar".equalsIgnoreCase(jarValue) || "ojdbc6-11g.jar".equalsIgnoreCase(param.getValue())) { //$NON-NLS-1$ //$NON-NLS-2$
            param.setValue("ORACLE_11"); //$NON-NLS-1$
            // no ORACLE_11-6 from repositoy for now
            //param.setValue("ORACLE_11-6"); //$NON-NLS-1$
            return true;
        } else if ("ojdbc5.jar".equalsIgnoreCase(jarValue) || "ojdbc5-11g.jar".equalsIgnoreCase(param.getValue())) { //$NON-NLS-1$ //$NON-NLS-2$
            param.setValue("ORACLE_11"); //$NON-NLS-1$
            return true;
        } else if ("ojdbc14.jar".equalsIgnoreCase(jarValue) || "ojdbc14-10g.jar".equalsIgnoreCase(param.getValue())) { //$NON-NLS-1$ //$NON-NLS-2$
            param.setValue("ORACLE_10"); //$NON-NLS-1$
            return true;
        } else if ("ojdbc14-9i.jar".equalsIgnoreCase(jarValue)) { //$NON-NLS-1$
            param.setValue("ORACLE_9"); //$NON-NLS-1$
            return true;
        } else if ("ojdbc12.jar".equalsIgnoreCase(jarValue) || "ojdbc12-8i.jar".equalsIgnoreCase(param.getValue())) { //$NON-NLS-1$ //$NON-NLS-2$
            param.setValue("ORACLE_8"); //$NON-NLS-1$
            return true;
        }

        return false;
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
