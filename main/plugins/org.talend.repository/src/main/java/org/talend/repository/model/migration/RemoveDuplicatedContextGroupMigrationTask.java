package org.talend.repository.model.migration;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.core.model.migration.AbstractItemMigrationTask;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.JobletProcessItem;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.designer.core.model.utils.emf.talendfile.ContextType;

public class RemoveDuplicatedContextGroupMigrationTask extends AbstractItemMigrationTask {

    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2016, 5, 12, 16, 0, 0);
        return gc.getTime();
    }

    @Override
    public ExecutionResult execute(Item item) {
        List<?> contexts = null;
        if (item instanceof ProcessItem) {
            // process, process_mr, route, routelet.
            ProcessItem processItem = (ProcessItem) item;
            contexts = processItem.getProcess().getContext();
        } else if (item instanceof JobletProcessItem) {
            JobletProcessItem jobletItem = (JobletProcessItem) item;
            contexts = jobletItem.getJobletProcess().getContext();
        }
        Set<String> nameSet = new HashSet<String>();
        Iterator<?> iterator = contexts.listIterator();
        int count = 0;
        while (iterator.hasNext()) {
            Object obj = iterator.next();
            if (obj instanceof ContextType) {
                ContextType context = (ContextType) obj;
                if (nameSet.contains(context.getName())) {
                    iterator.remove();
                    count++;
                } else {
                    nameSet.add(context.getName());
                }
            }
        }
        if (count > 0) {
            try {
                ProxyRepositoryFactory.getInstance().save(item, true);
                return ExecutionResult.SUCCESS_NO_ALERT;
            } catch (PersistenceException e) {
                ExceptionHandler.process(e);
                return ExecutionResult.FAILURE;
            }
        } else {
            return ExecutionResult.NOTHING_TO_DO;
        }
    }

    @Override
    public List<ERepositoryObjectType> getTypes() {
        return ERepositoryObjectType.getAllTypesOfProcess();
    }

}
