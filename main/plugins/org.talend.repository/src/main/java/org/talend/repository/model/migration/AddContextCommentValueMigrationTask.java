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

import org.eclipse.emf.common.util.EList;
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

public class AddContextCommentValueMigrationTask extends AbstractItemMigrationTask {

    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2016, 3, 8, 11, 24, 33);
        return gc.getTime();
    }

    @SuppressWarnings("unchecked")
    @Override
    public ExecutionResult execute(Item item) {
        EList<ContextType> contexts = null;
        if (item instanceof ProcessItem) {
            // process, process_mr, process_storm, route, routelet.
            ProcessItem processItem = (ProcessItem) item;
            contexts = processItem.getProcess().getContext();
        } else if (item instanceof JobletProcessItem) {
            JobletProcessItem jobletItem = (JobletProcessItem) item;
            contexts = jobletItem.getJobletProcess().getContext();
        } else if (item instanceof ContextItem) {
            ContextItem contextItem = (ContextItem) item;
            contexts = contextItem.getContext();
        }
        // 2 kinds of situation should be excluded before doing migration to the old context:
        // 1.has null, 2.all same comment values;
        // 1 is from 6.1.0 release and 5.6.2 patched TPS-1109, all null comment value will set to "".
        // for repository context in job, won't change anything because the repository context has been fixed
        // the update action will execute when opening job.
        boolean hasNull = false, isSame = true;
        if (contexts != null && contexts.size() > 1) {
            List<String> firstComments = new ArrayList<String>();
            for (int x = 0; x < contexts.size(); x++) {
                List<ContextParameterType> contextParams = contexts.get(x).getContextParameter();
                for (int y = 0; y < contextParams.size(); y++) {
                    ContextParameterType param = contextParams.get(y);
                    boolean isBuiltin = param.getRepositoryContextId() == null;
                    String comment = param.getComment();
                    if (comment == null) {
                        if (isBuiltin) {
                            param.setComment(""); //$NON-NLS-1$
                            hasNull = true;
                        }
                        continue;
                    }
                    // comments to show in old item before 5.6.1 are always in the first group.
                    if (x == 0) {
                        if (!isBuiltin) {
                            firstComments.add("NOT_BUILTIN"); //$NON-NLS-1$
                        } else {
                            firstComments.add(comment);
                        }
                        continue;
                    }
                    if (isBuiltin && !comment.equals(firstComments.get(y))) {
                        isSame = false;
                    }
                }
            }
            try {
                if (hasNull) {
                    ProxyRepositoryFactory.getInstance().save(item, true);
                    return ExecutionResult.SUCCESS_NO_ALERT;
                }
                if (!isSame) {
                    for (int x = 1; x < contexts.size(); x++) {
                        List<ContextParameterType> contextParams = contexts.get(x).getContextParameter();
                        for (int y = 0; y < contextParams.size(); y++) {
                            ContextParameterType param = contextParams.get(y);
                            String comment = param.getComment();
                            if (param.getRepositoryContextId() == null && !firstComments.get(y).equals(comment)) {
                                if (!firstComments.get(y).equals("NOT_BUILTIN")) { //$NON-NLS-1$
                                    param.setComment(firstComments.get(y));
                                }
                            }
                        }
                    }
                    ProxyRepositoryFactory.getInstance().save(item, true);
                    return ExecutionResult.SUCCESS_NO_ALERT;
                }
            } catch (PersistenceException e) {
                ExceptionHandler.process(e);
                return ExecutionResult.FAILURE;
            }
        }
        return ExecutionResult.NOTHING_TO_DO;
    }

    @Override
    public List<ERepositoryObjectType> getTypes() {
        List<ERepositoryObjectType> toReturn = new ArrayList<ERepositoryObjectType>();
        toReturn.add(ERepositoryObjectType.CONTEXT);
        toReturn.add(ERepositoryObjectType.JOBLET);
        toReturn.add(ERepositoryObjectType.PROCESS);
        toReturn.add(ERepositoryObjectType.PROCESS_MR);
        toReturn.add(ERepositoryObjectType.PROCESS_STORM);
        toReturn.add(ERepositoryObjectType.PROCESS_ROUTE);
        toReturn.add(ERepositoryObjectType.PROCESS_ROUTELET);
        return toReturn;
    }
}
