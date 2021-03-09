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

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.CorePlugin;
import org.talend.core.model.general.Project;
import org.talend.core.model.properties.ImplicitContextSettings;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.StatAndLogsSettings;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.ElementValueType;
import org.talend.designer.core.model.utils.emf.talendfile.ParametersType;
import org.talend.migration.AbstractMigrationTask;
import org.talend.migration.IProjectMigrationTask;
import org.talend.migration.IMigrationTask.ExecutionResult;
import org.talend.repository.model.IProxyRepositoryFactory;

/**
 * DOC hwang  class global comment. Detailled comment
 */
public class ChangeDriverJarForProjectSetting extends AbstractMigrationTask implements IProjectMigrationTask {

    @Override
    public Date getOrder() {
        return new GregorianCalendar(2017, 12, 19, 15, 0, 0).getTime();
    }

    @Override
    public boolean isApplicableOnItems() {
        return false;
    }

    @Override
    public ExecutionResult execute(Project project) {

        ProxyRepositoryFactory prf = ProxyRepositoryFactory.getInstance();
        Resource projectResource = project.getEmfProject().eResource();
        if (projectResource == null) {
            if (project.getEmfProject() != null && project.getEmfProject().eIsProxy()) {
                ResourceSet resourceSet = prf.getRepositoryFactoryFromProvider().getResourceManager().resourceSet;
                project.setEmfProject((org.talend.core.model.properties.Project) EcoreUtil.resolve(project.getEmfProject(),
                        resourceSet));
                projectResource = project.getEmfProject().eResource();
            }
        }
        org.talend.core.model.properties.Project emfProject = project.getEmfProject();
        StatAndLogsSettings statAndLogs = emfProject.getStatAndLogsSettings();
        boolean modified = false;
        if (statAndLogs != null && statAndLogs.getParameters() != null) {
            ParametersType parameters = statAndLogs.getParameters();
            List elementParameter = parameters.getElementParameter();
            for (Object object : elementParameter) {
                if (object instanceof ElementParameterType) {
                    ElementParameterType parameterType = (ElementParameterType) object;
                    if(parameterType.getName().equals("DRIVER_JAR")){
                        List eValue = parameterType.getElementValue();
                        if(eValue != null){
                            for(Object obj : eValue){
                                if(obj instanceof ElementValueType){
                                    ((ElementValueType)obj).setElementRef("drivers");
                                    modified = true;
                                }
                            }
                        }
                    }
                }
            }
        }

        ImplicitContextSettings implicitContext = emfProject.getImplicitContextSettings();
        if (implicitContext != null && implicitContext.getParameters() != null) {
            ParametersType parameters = implicitContext.getParameters();
            List elementParameter = parameters.getElementParameter();
            for (Object object : elementParameter) {
                if (object instanceof ElementParameterType) {
                    ElementParameterType parameterType = (ElementParameterType) object;
                    if(parameterType.getName().equals("DRIVER_JAR_IMPLICIT_CONTEXT")){
                        List eValue = parameterType.getElementValue();
                        if(eValue != null){
                            for(Object obj : eValue){
                                if(obj instanceof ElementValueType){
                                    ((ElementValueType)obj).setElementRef("drivers");
                                    modified = true;
                                }
                            }
                        }
                    }
                }
            }
        }
        if (modified) {
            try {
                prf.saveProject(project);
                return ExecutionResult.SUCCESS_NO_ALERT;
            } catch (PersistenceException e) {
                ExceptionHandler.process(e);
                return ExecutionResult.FAILURE;
            }
        }
        return ExecutionResult.NOTHING_TO_DO;
    }

    @Override
    public ExecutionResult execute(Project project, boolean doSave) {
        return execute(project);
    }

    @Override
    public ExecutionResult execute(Project project, Item item) {
        return ExecutionResult.NOTHING_TO_DO;
    }

}
