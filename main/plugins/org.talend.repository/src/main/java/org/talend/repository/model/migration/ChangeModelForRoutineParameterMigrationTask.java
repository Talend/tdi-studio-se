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
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.CorePlugin;
import org.talend.core.model.general.Project;
import org.talend.core.model.migration.AbstractJobMigrationTask;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.relationship.RelationshipItemBuilder;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.routines.RoutinesUtil;
import org.talend.designer.core.model.utils.emf.talendfile.ItemInforType;
import org.talend.designer.core.model.utils.emf.talendfile.ParametersType;
import org.talend.designer.core.model.utils.emf.talendfile.RoutinesParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.TalendFileFactory;

/**
 * DOC guanglong.du class global comment. Detailled comment
 */
public class ChangeModelForRoutineParameterMigrationTask extends AbstractJobMigrationTask {

    public List<ERepositoryObjectType> getTypes() {
        List<ERepositoryObjectType> toReturn = new ArrayList<ERepositoryObjectType>();
        toReturn.add(ERepositoryObjectType.PROCESS);
        return toReturn;
    }

    @Override
    public ExecutionResult execute(Project project) {
        ExecutionResult result = super.execute(project);
        RelationshipItemBuilder.getInstance().saveRelations();
        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public ExecutionResult execute(Item item) {
        if (!(item instanceof ProcessItem)) {
            return ExecutionResult.NOTHING_TO_DO;
        }
        try {
            ProcessItem item2 = (ProcessItem) item;
            List<ItemInforType> oldRoutinesDependencies = (List<ItemInforType>) item2.getProcess().getRoutinesDependencies();
            if (item2.getProcess().getParameters() == null || item2.getProcess().getParameters().getRoutinesParameter() == null) {
                ParametersType parameterType = TalendFileFactory.eINSTANCE.createParametersType();
                item2.getProcess().setParameters(parameterType);
            }
            List<RoutinesParameterType> routinesDependencies = (List<RoutinesParameterType>) item2.getProcess().getParameters()
                    .getRoutinesParameter();

            List<IRepositoryViewObject> viewObjects = RoutinesUtil.getCurrentSystemRoutines();
            boolean modified = false;
            for (ItemInforType added : oldRoutinesDependencies) {
                if (added.isSystem()) {
                    for (int i = 0; i < viewObjects.size(); i++) {
                        IRepositoryViewObject viewObject = viewObjects.get(i);
                        if (viewObject.getLabel().equals(added.getIdOrName())) {
                            RoutinesParameterType routinesParameterType = TalendFileFactory.eINSTANCE
                                    .createRoutinesParameterType();
                            routinesParameterType.setId(viewObject.getId());
                            routinesParameterType.setName(added.getIdOrName());
                            routinesDependencies.add(routinesParameterType);
                            modified = true;
                        }
                    }

                } else {
                    IRepositoryViewObject userRoutines = RoutinesUtil.getUserRoutines(added.getIdOrName());
                    if (userRoutines != null) {
                        RoutinesParameterType routinesParameterType = TalendFileFactory.eINSTANCE.createRoutinesParameterType();
                        routinesParameterType.setId(added.getIdOrName());
                        routinesParameterType.setName(userRoutines.getLabel());
                        routinesDependencies.add(routinesParameterType);
                        modified = true;
                    }

                }

            }
            if (modified) {
                oldRoutinesDependencies.clear();
                CorePlugin.getDefault().getRepositoryService().getProxyRepositoryFactory().save(item, true);
                RelationshipItemBuilder.getInstance().addOrUpdateItem(item, true);
                return ExecutionResult.SUCCESS_WITH_ALERT;
            }

        } catch (Exception e) {
            ExceptionHandler.process(e);
            return ExecutionResult.FAILURE;
        }
        return ExecutionResult.NOTHING_TO_DO;
    }

    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2010, 11, 04, 12, 0, 0);
        return gc.getTime();
    }

    @Override
    public boolean isDeprecated() {
        return true;
    }

}
