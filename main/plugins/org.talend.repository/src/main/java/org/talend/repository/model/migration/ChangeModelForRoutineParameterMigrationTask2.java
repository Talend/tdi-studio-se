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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.language.LanguageManager;
import org.talend.core.model.general.Project;
import org.talend.core.model.migration.AbstractJobMigrationTask;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.properties.RoutineItem;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.designer.core.model.utils.emf.talendfile.ConnectionType;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.ElementValueType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ParametersType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;
import org.talend.designer.core.model.utils.emf.talendfile.RoutinesParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.TalendFileFactory;
import org.talend.repository.ProjectManager;
import org.talend.repository.model.IProxyRepositoryFactory;

/**
 * This migration will:
 *
 * For jobs:<br>
 * - If the job already have some dependencies from 4.1.1 or 4.1.2, it will keep original dependencies, and check if
 * some might be missing.<br>
 * - If the job have no dependencies (might be migration bug from 4.1.1 or 4.1.2), or old migration. Then add all
 * routines directly as dependency<br>
 * .
 *
 * For joblets:<br>
 * - We only add system routines by default as dependency, other routines will be only added if found somewhere in the
 * dependency<br>
 *
 */
public class ChangeModelForRoutineParameterMigrationTask2 extends AbstractJobMigrationTask {

    @Override
    public ExecutionResult execute(Project project) {
        ExecutionResult result = super.execute(project);
        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public ExecutionResult execute(Item item) {
        try {
            List<String> possibleRoutines = new ArrayList<String>();
            List<String> routinesToAdd = new ArrayList<String>();
            String additionalString = LanguageManager.getCurrentLanguage() == ECodeLanguage.JAVA ? "." : "";

            IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
            List<IRepositoryViewObject> routines = factory.getAll(ProjectManager.getInstance().getCurrentProject(),
                    ERepositoryObjectType.ROUTINES);
            // always add the system, others must be checked
            for (IRepositoryViewObject object : routines) {
                if (((RoutineItem) object.getProperty().getItem()).isBuiltIn()) {
                    routinesToAdd.add(object.getLabel());
                } else {
                    possibleRoutines.add(object.getLabel());
                }
            }
            for (Project project : ProjectManager.getInstance().getAllReferencedProjects()) {
                List<IRepositoryViewObject> refRoutines = factory.getAll(project, ERepositoryObjectType.ROUTINES);
                for (IRepositoryViewObject object : refRoutines) {
                    if (!((RoutineItem) object.getProperty().getItem()).isBuiltIn()) {
                        if (!possibleRoutines.contains(object.getLabel())) {
                            possibleRoutines.add(object.getLabel());
                            routines.add(object);
                        }
                    }
                }
            }
            List<RoutinesParameterType> oldList = Collections.EMPTY_LIST;
            ProcessType processType = getProcessType(item);
            if (null != processType) {
                if (processType.getParameters() != null && processType.getParameters().getRoutinesParameter() == null) {
                    ParametersType parameterType = TalendFileFactory.eINSTANCE.createParametersType();
                    processType.setParameters(parameterType);
                    if (item instanceof ProcessItem) {
                        routinesToAdd.addAll(possibleRoutines);
                        possibleRoutines.clear();
                    }
                } else {
                    if (processType.getParameters() == null) {
                        processType.setParameters(TalendFileFactory.eINSTANCE.createParametersType());
                    }
                    oldList = new ArrayList<RoutinesParameterType>(processType.getParameters().getRoutinesParameter());
                    if (oldList.isEmpty() && item instanceof ProcessItem) {
                        routinesToAdd.addAll(possibleRoutines);
                        possibleRoutines.clear();
                    }
                    processType.getParameters().getRoutinesParameter().clear();
                }
                if (processType.getRoutinesDependencies() != null && !processType.getRoutinesDependencies().isEmpty()) {
                    processType.getRoutinesDependencies().clear();
                }
                List<RoutinesParameterType> routinesDependencies = (List<RoutinesParameterType>) processType.getParameters()
                        .getRoutinesParameter();

                if (!possibleRoutines.isEmpty()) {
                    // only check the part below in case there is any user routine.
                    // check possible routines to setup in process
                    for (ElementParameterType param : (List<ElementParameterType>) processType.getParameters()
                            .getElementParameter()) {
                        for (String routine : possibleRoutines) {
                            if (!routinesToAdd.contains(routine) && param.getValue() != null
                                    && param.getValue().contains(routine + additionalString)) {
                                routinesToAdd.add(routine);
                            }
                            for (ElementValueType elementValue : (List<ElementValueType>) param.getElementValue()) {
                                if (!routinesToAdd.contains(routine) && elementValue.getValue() != null
                                        && elementValue.getValue().contains(routine + additionalString)) {
                                    routinesToAdd.add(routine);
                                }
                            }
                        }
                    }

                    // check possible routines to setup in nodes
                    for (NodeType node : ((List<NodeType>) processType.getNode())) {
                        for (ElementParameterType param : (List<ElementParameterType>) node.getElementParameter()) {
                            for (String routine : possibleRoutines) {
                                if (!routinesToAdd.contains(routine) && param.getValue() != null
                                        && param.getValue().contains(routine + additionalString)) {
                                    routinesToAdd.add(routine);
                                }
                                for (ElementValueType elementValue : (List<ElementValueType>) param.getElementValue()) {
                                    if (!routinesToAdd.contains(routine) && elementValue.getValue() != null
                                            && elementValue.getValue().contains(routine + additionalString)) {
                                        routinesToAdd.add(routine);
                                    }
                                }
                            }
                        }
                    }

                    // check possible routines to setup in connections
                    for (ConnectionType connection : ((List<ConnectionType>) processType.getConnection())) {
                        for (ElementParameterType param : (List<ElementParameterType>) connection.getElementParameter()) {
                            for (String routine : possibleRoutines) {
                                if (!routinesToAdd.contains(routine) && param.getValue() != null
                                        && param.getValue().contains(routine + additionalString)) {
                                    routinesToAdd.add(routine);
                                }
                                for (ElementValueType elementValue : (List<ElementValueType>) param.getElementValue()) {
                                    if (!routinesToAdd.contains(routine) && elementValue.getValue() != null
                                            && elementValue.getValue().contains(routine + additionalString)) {
                                        routinesToAdd.add(routine);
                                    }
                                }
                            }
                        }
                    }
                }

                // just in case some routine dependencies exist before but are lost... (migration from 4.1.1 or 4.1.2)
                for (RoutinesParameterType routine : oldList) {
                    if (!routinesToAdd.contains(routine.getName())) {
                        routinesDependencies.add(routine);
                    }
                }

                for (IRepositoryViewObject object : routines) {
                    if (routinesToAdd.contains(object.getLabel())) {
                        RoutinesParameterType routinesParameterType = TalendFileFactory.eINSTANCE.createRoutinesParameterType();
                        routinesParameterType.setId(object.getId());
                        routinesParameterType.setName(object.getLabel());
                        routinesDependencies.add(routinesParameterType);
                    }
                }

                factory.save(item, true);
            }
            return ExecutionResult.SUCCESS_NO_ALERT;

        } catch (Exception e) {
            ExceptionHandler.process(e);
            return ExecutionResult.FAILURE;
        }
    }

    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2011, 11, 04, 12, 0, 0);
        return gc.getTime();
    }
}
