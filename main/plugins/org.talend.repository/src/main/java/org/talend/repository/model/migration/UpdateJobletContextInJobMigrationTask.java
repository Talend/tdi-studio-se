package org.talend.repository.model.migration;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.EList;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.core.model.context.ContextUtils;
import org.talend.core.model.migration.AbstractItemMigrationTask;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.JobletProcessItem;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.designer.core.model.utils.emf.talendfile.ContextParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.ContextType;

public class UpdateJobletContextInJobMigrationTask extends AbstractItemMigrationTask {

    private Map<String, IRepositoryViewObject> map = new HashMap<String, IRepositoryViewObject>();

    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2016, 5, 17, 16, 0, 0);
        return gc.getTime();
    }

    @Override
    public ExecutionResult execute(Item item) {
        if (item instanceof ProcessItem) {
            ProcessItem processItem = (ProcessItem) item;
            EList<ContextType> contexts = processItem.getProcess().getContext();
            if (contexts.size() < 2) {
                return ExecutionResult.NOTHING_TO_DO;
            }
            EList<ContextParameterType> params1 = contexts.get(0).getContextParameter();
            boolean isModified = false;
            for (ContextParameterType param1 : params1) {
                String id = param1.getRepositoryContextId();
                if (id == null) {
                    continue;
                }
                IRepositoryViewObject obj = null;
                try {
                    if (map.get(id) != null) {
                        obj = map.get(id);
                    } else {
                        obj = ProxyRepositoryFactory.getInstance().getLastVersion(id);
                        if (obj == null) {
                            continue;
                        }
                        map.put(id, obj);
                    }
                } catch (PersistenceException e) {
                    continue;
                }
                Item jobletItem = obj.getProperty().getItem();
                if (jobletItem != null && jobletItem instanceof JobletProcessItem) {
                    List<ContextType> joblectContexts = ((JobletProcessItem) jobletItem).getJobletProcess().getContext();
                    for (int i = 1; i < contexts.size(); i++) {
                        ContextType context = contexts.get(i);
                        List<ContextParameterType> params2 = context.getContextParameter();
                        ContextType joblectContext = ContextUtils.getContextTypeByName(joblectContexts, context.getName());
                        ContextParameterType jobletContextParam = ContextUtils.getContextParameterTypeByName(joblectContext,
                                param1.getName());
                        for (ContextParameterType param2 : params2) {
                            if (param2.getName().equals(param1.getName()) && param2.getRepositoryContextId() == null) {
                                param2.setRepositoryContextId(id);
                                if (jobletContextParam != null) {
                                    param2.setValue(jobletContextParam.getValue());
                                    param2.setRawValue(jobletContextParam.getRawValue());
                                }
                                isModified = true;
                                break;
                            }
                        }
                    }
                }
            }
            if (isModified) {
                try {
                    ProxyRepositoryFactory.getInstance().save(item, true);
                } catch (PersistenceException e) {
                    ExceptionHandler.process(e);
                    return ExecutionResult.FAILURE;
                }
                return ExecutionResult.SUCCESS_NO_ALERT;
            }
        }
        return ExecutionResult.NOTHING_TO_DO;
    }

    @Override
    public List<ERepositoryObjectType> getTypes() {
        return ERepositoryObjectType.getAllTypesOfProcess();
    }
}
