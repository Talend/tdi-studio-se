package org.talend.repository.model.migration;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.core.model.migration.AbstractItemMigrationTask;
import org.talend.core.model.properties.ContextItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.JobletProcessItem;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.designer.core.model.utils.emf.talendfile.ContextParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.ContextType;

public class FixUnevenItemContextParametersMigrationTask extends AbstractItemMigrationTask {

    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2016, 3, 7, 11, 24, 33);
        return gc.getTime();
    }

    @SuppressWarnings("unchecked")
    @Override
    public ExecutionResult execute(Item item) {
        EList<ContextType> contexts = null;
        String defualtGroupName = null;
        if (item instanceof ProcessItem) {
            // process, process_mr, route, routelet.
            ProcessItem processItem = (ProcessItem) item;
            contexts = processItem.getProcess().getContext();
            defualtGroupName = processItem.getProcess().getDefaultContext();
        } else if (item instanceof JobletProcessItem) {
            JobletProcessItem jobletItem = (JobletProcessItem) item;
            contexts = jobletItem.getJobletProcess().getContext();
            defualtGroupName = jobletItem.getJobletProcess().getDefaultContext();
        } else if (item instanceof ContextItem) {
            ContextItem contextItem = (ContextItem) item;
            contexts = contextItem.getContext();
            defualtGroupName = contextItem.getDefaultContext();
        }
        try {
            ContextType defaultGroup = null;
            for (ContextType context : contexts) {
                if (context.getName().equals(defualtGroupName)) {
                    defaultGroup = context;
                    break;
                }
            }
            if (defaultGroup == null && contexts.size() > 0) {
                defaultGroup = contexts.get(0);
            }

            boolean contextChanged = false;
            if (defaultGroup != null) {
                Map<String, ContextParameterType> paramMap = new HashMap<String, ContextParameterType>();
                List<String> paramNameList = new ArrayList<String>();
                paramMap.putAll(collectDefaultGroupParams(defaultGroup, paramNameList));
                for (ContextType context : contexts) {
                    if (context == defaultGroup) {
                        continue;
                    }

                    Map<String, ContextParameterType> otherGroupParam = collectDefaultGroupParams(context, paramNameList);
                    for (String paramName : otherGroupParam.keySet()) {
                        if (!paramMap.containsKey(paramName)) {
                            paramMap.put(paramName, otherGroupParam.get(paramName));
                        }
                    }
                }
                // make sure all groups have the same param list
                for (ContextType context : contexts) {
                    EList<ContextParameterType> params = context.getContextParameter();
                    List<String> paramNames = new ArrayList<String>(paramNameList);
                    for (ContextParameterType param : params) {
                        if (paramNames.contains(param.getName())) {
                            paramNames.remove(param.getName());
                        }
                    }
                    if (!paramNames.isEmpty()) {
                        contextChanged = true;
                        for (String paramToAdd : paramNames) {
                            ContextParameterType toAdd = paramMap.get(paramToAdd);
                            context.getContextParameter().add(EcoreUtil.copy(toAdd));
                        }
                    }
                }
                // change param order if needed
                for (ContextType context : contexts) {
                    EList<ContextParameterType> params = context.getContextParameter();
                    List<ContextParameterType> copyOfParam = new ArrayList<ContextParameterType>(params);
                    for (int i = 0; i < copyOfParam.size(); i++) {
                        ContextParameterType param = copyOfParam.get(i);
                        int indexOf = paramNameList.indexOf(param.getName());
                        if (i != indexOf) {
                            contextChanged = true;
                            params.remove(param);
                            params.add(indexOf, param);
                        }
                    }

                }

                // make sure params in different groups have the same repository id and type as default group
                for (ContextType context : contexts) {
                    EList<ContextParameterType> params = context.getContextParameter();
                    for (ContextParameterType param : params) {
                        ContextParameterType paramDefault = paramMap.get(param.getName());
                        if (!paramDefault.getType().equals(param.getType())) {
                            contextChanged = true;
                            param.setType(paramDefault.getType());
                        }
                        if (paramDefault.getRepositoryContextId() == null
                                && param.getRepositoryContextId() != null
                                || (paramDefault.getRepositoryContextId() != null && !paramDefault.getRepositoryContextId()
                                        .equals(param.getRepositoryContextId()))) {
                            contextChanged = true;
                            param.setRepositoryContextId(paramDefault.getRepositoryContextId());
                        }
                    }
                }

            }
            if (contextChanged) {
                ProxyRepositoryFactory.getInstance().save(item, true);
                return ExecutionResult.SUCCESS_NO_ALERT;
            }

        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
            return ExecutionResult.FAILURE;
        }
        return ExecutionResult.NOTHING_TO_DO;
    }

    private Map<String, ContextParameterType> collectDefaultGroupParams(ContextType contextType, List<String> paramNames) {
        EList<ContextParameterType> params = contextType.getContextParameter();
        Map<String, ContextParameterType> paramMap = new HashMap<String, ContextParameterType>();
        for (ContextParameterType param : params) {
            paramMap.put(param.getName(), param);
            if (!paramNames.contains(param.getName())) {
                paramNames.add(param.getName());
            }
        }
        return paramMap;

    }

    @Override
    public List<ERepositoryObjectType> getTypes() {
        List<ERepositoryObjectType> toReturn = new ArrayList<ERepositoryObjectType>();
        toReturn.add(ERepositoryObjectType.CONTEXT);
        toReturn.add(ERepositoryObjectType.JOBLET);
        toReturn.add(ERepositoryObjectType.PROCESS);
        toReturn.add(ERepositoryObjectType.PROCESS_MR);
        toReturn.add(ERepositoryObjectType.PROCESS_ROUTE);
        toReturn.add(ERepositoryObjectType.PROCESS_ROUTELET);
        return toReturn;
    }
}
