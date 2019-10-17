package org.talend.repository.model.migration;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.EList;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.core.model.migration.AbstractItemMigrationTask;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.JobletProcessItem;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.designer.core.model.utils.emf.talendfile.ContextParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.ContextType;
import org.talend.designer.core.model.utils.emf.talendfile.TalendFileFactory;
import org.talend.designer.core.model.utils.emf.talendfile.impl.ContextParameterTypeImpl;

public class AddMissingContextMigrationTask extends AbstractItemMigrationTask {

    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2019, 9, 26, 15, 51, 33);
        return gc.getTime();
    }

    @SuppressWarnings("unchecked")
    @Override
    public ExecutionResult execute(Item item) {
        EList<ContextType> contexts = null;
        if (item instanceof ProcessItem) {
            ProcessItem processItem = (ProcessItem) item;
            contexts = processItem.getProcess().getContext();
        } else if (item instanceof JobletProcessItem) {
            JobletProcessItem jobletItem = (JobletProcessItem) item;
            contexts = jobletItem.getJobletProcess().getContext();
        }
        try {
            if (contexts != null && contexts.size() > 1) {
                fillContextGroupList(contexts);
                ProxyRepositoryFactory.getInstance().save(item, true);
                return ExecutionResult.SUCCESS_NO_ALERT;
            }
        } catch (Exception e) {
            ExceptionHandler.process(e);
            return ExecutionResult.FAILURE;
        }
        return ExecutionResult.NOTHING_TO_DO;
    }

    private void fillContextGroupList(EList<ContextType> contexts) {
        // calculate all group of context var
        Map<String, ContextParameterTypeImpl> allContextMap = new HashMap<String, ContextParameterTypeImpl>();
        List<Map<String, ContextParameterTypeImpl>> contextList = new ArrayList<>();
        for (int i = 0; i < contexts.size(); i++) {
            Map<String, ContextParameterTypeImpl> contextMap = new HashMap<String, ContextParameterTypeImpl>();
            List<ContextParameterTypeImpl> contextParams = contexts.get(i).getContextParameter();
            for (ContextParameterTypeImpl cp : contextParams) {
                String contextName = cp.getName();
                contextMap.put(contextName, cp);
                if (allContextMap.get(contextName) == null) {
                    allContextMap.put(contextName, cp);
                }
            }
            contextList.add(contextMap);

        }
        // add context var to context group
        for (int i = 0; i < contexts.size(); i++) {
            List<ContextParameterType> contextParams = contexts.get(i).getContextParameter();
            if (contextParams.size() != allContextMap.size()) {
                for (String contextName : allContextMap.keySet()) {
                    if (contextList.get(i).get(contextName) == null) {
                        ContextParameterTypeImpl contextParameterType = allContextMap.get(contextName);
                        ContextParameterType addMissing = TalendFileFactory.eINSTANCE.createContextParameterType();
                        addMissing.setName(contextParameterType.getName());
                        addMissing.setComment("");
                        addMissing.setValue("");
                        addMissing.setRepositoryContextId(contextParameterType.getRepositoryContextId());
                        addMissing.setPromptNeeded(contextParameterType.isPromptNeeded());
                        addMissing.setType(contextParameterType.getType());
                        addMissing.setPrompt(contextParameterType.getPrompt());
                        contexts.get(i).getContextParameter().add(addMissing);
                    }
                }
            }
        }
    }
    @Override
    public List<ERepositoryObjectType> getTypes() {
        List<ERepositoryObjectType> toReturn = new ArrayList<ERepositoryObjectType>();
        toReturn.addAll(ERepositoryObjectType.getAllTypesOfProcess());
        toReturn.addAll(ERepositoryObjectType.getAllTypesOfJoblet());
        return toReturn;
    }
}
