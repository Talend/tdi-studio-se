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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.model.context.ContextUtils;
import org.talend.core.model.context.UpdateContextVariablesHelper;
import org.talend.core.model.migration.AbstractItemMigrationTask;
import org.talend.core.model.properties.ContextItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.JobletProcessItem;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.designer.core.model.utils.emf.talendfile.ContextParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.ContextType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;
import org.talend.repository.model.IProxyRepositoryFactory;

/**
 * ggu class global comment. Detailled comment
 */
public class ReplaceJavaKeywordsNameForContextMigrationTask extends AbstractItemMigrationTask {

    private IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();

    private boolean firstProcess = true;

    // id to variabls map(new name to old one)
    private Map<String, Map<String, String>> contextReplacedNamesMap = null;

    @Override
    public List<ERepositoryObjectType> getTypes() {
        List<ERepositoryObjectType> toReturn = new ArrayList<ERepositoryObjectType>();
        toReturn.add(ERepositoryObjectType.PROCESS);
        toReturn.add(ERepositoryObjectType.JOBLET);
        return toReturn;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.migration.AbstractItemMigrationTask#execute(org.talend.core.model.properties.Item)
     */
    @SuppressWarnings("unchecked")
    @Override
    public ExecutionResult execute(Item item) {
        if (getProject().getLanguage() == ECodeLanguage.PERL) {
            return ExecutionResult.NOTHING_TO_DO;
        }
        // first process the context items.
        if (firstProcess) {
            contextReplacedNamesMap = getReplacedNamesAndReplaceContextVars();
            firstProcess = false;
        }
        // new name to old one
        Map<String, String> jobContextReplacedNamesMap = new HashMap<String, String>();

        boolean changed = false;
        ProcessType process = null;
        if (item instanceof ProcessItem) {
            process = ((ProcessItem) item).getProcess();

        } else if (item instanceof JobletProcessItem) {
            process = ((JobletProcessItem) item).getJobletProcess();

        }
        if (process != null) {
            changed = modifyJobContext(jobContextReplacedNamesMap, process.getContext());
            UpdateContextVariablesHelper.updateProcessForRenamed(process, jobContextReplacedNamesMap);
            // for tRunJob repalce, need wait for finishing the featrue 3310
            // TODO
            // try {
            // UpdateRunJobComponentContextHelper.updateItemRunJobComponentReference(factory,
            // jobContextReplacedNamesMap,
            // process.getName(), null);
            // } catch (PersistenceException e) {
            // //
            // }
        }
        //
        if (changed) {
            try {
                factory.save(item, true);
                return ExecutionResult.SUCCESS_WITH_ALERT;
            } catch (PersistenceException e) {
                ExceptionHandler.process(e);
                return ExecutionResult.FAILURE;
            }
        }
        return ExecutionResult.NOTHING_TO_DO;
    }

    /**
     *
     * ggu Comment method "modifyJobContext".
     *
     * this function will get the replaced variables map.
     */
    @SuppressWarnings("unchecked")
    private boolean modifyJobContext(Map<String, String> jobContextReplacedNamesMap, List<ContextType> contextTypeList) {
        if (contextTypeList == null || contextTypeList.isEmpty()) {
            return false;
        }
        boolean changed = false;
        boolean first = true;
        Set<String> varNames = null;
        for (ContextType contextType : (List<ContextType>) contextTypeList) {
            if (first) {
                varNames = ContextUtils.getContextVarNames(contextType);
            }
            for (ContextParameterType paramType : (List<ContextParameterType>) contextType.getContextParameter()) {
                final String oldName = paramType.getName();
                final String sourceId = paramType.getRepositoryContextId();
                if (first) {
                    if (ContextUtils.isJavaKeyWords(oldName)) { // is java keywords
                        String newName = getNewName(varNames, oldName);
                        String tmpName = getContextNewNameFromMap(contextReplacedNamesMap, sourceId, oldName);
                        if (tmpName != null) { // from repository context
                            newName = tmpName;
                        }
                        jobContextReplacedNamesMap.put(newName, oldName);
                        paramType.setName(newName);
                        changed = true;

                    }
                } else {
                    String newName = getJobContextNewNameFromMap(jobContextReplacedNamesMap, oldName);
                    if (newName != null) { // is key words
                        paramType.setName(newName);
                        changed = true;
                    }

                }
            }
        }
        return changed;
    }

    /**
     *
     * ggu Comment method "getReplacedNamesAndReplaceContextVars".
     *
     * get the replaced names, and replace the context variables at the same time.
     */
    @SuppressWarnings("unchecked")
    private Map<String, Map<String, String>> getReplacedNamesAndReplaceContextVars() {
        // id to map(oldName to newName)
        Map<String, Map<String, String>> contextIdNamesMap = new HashMap<String, Map<String, String>>();
        List<ContextItem> allContextItem = ContextUtils.getAllContextItem();
        if (allContextItem != null) {
            for (ContextItem contextItem : allContextItem) {
                boolean changed = false;
                boolean first = true;
                Set<String> varNames = null;
                for (ContextType contextType : (List<ContextType>) contextItem.getContext()) {
                    if (first) {
                        varNames = ContextUtils.getContextVarNames(contextType);
                    }
                    // oldName to newName
                    Map<String, String> nameMap = new HashMap<String, String>();
                    final String contextId = contextItem.getProperty().getId();

                    for (ContextParameterType paramType : (List<ContextParameterType>) contextType.getContextParameter()) {
                        String oldName = paramType.getName();
                        if (first) {
                            if (ContextUtils.isJavaKeyWords(oldName)) { // is java keywords
                                String newName = getNewName(varNames, oldName);
                                nameMap.put(newName, oldName);
                                paramType.setName(newName);
                                changed = true;
                            }
                        } else {
                            String newName = getContextNewNameFromMap(contextIdNamesMap, contextId, oldName);
                            if (newName != null) { // is java keywords
                                paramType.setName(newName);
                                changed = true;
                            }
                        }
                    }

                    if (first && !nameMap.isEmpty()) {
                        contextIdNamesMap.put(contextId, nameMap);
                    }
                    first = false;
                }
                if (changed) { // save
                    try {
                        factory.save(contextItem, true);
                    } catch (PersistenceException e) {
                        //
                    }
                }

            }
        }
        return contextIdNamesMap;
    }

    /*
     * get the name (old name "true" to new one "true_1").
     */
    private String getNewName(Set<String> varNames, final String oldName) {
        if (oldName == null) {
            return null;
        }
        int i = 0;
        final String tmpName = oldName + "_"; //$NON-NLS-1$
        String newName = tmpName + i;

        if (varNames != null) {
            while (varNames.contains(newName)) {
                newName = tmpName + (++i);
            }
        }
        return newName;

    }

    private String getContextNewNameFromMap(Map<String, Map<String, String>> contextIdNamesMap, final String contextId,
            final String oldName) {
        if (contextIdNamesMap == null || contextIdNamesMap.isEmpty() || contextId == null || oldName == null) {
            return null;
        }
        Map<String, String> nameMap = contextIdNamesMap.get(contextId);
        if (nameMap == null || nameMap.isEmpty()) {
            return null;
        }
        for (String newName : nameMap.keySet()) {
            final String tmpName = nameMap.get(newName);
            if (tmpName != null && tmpName.equals(oldName)) {
                return newName;
            }
        }
        return null;
    }

    private String getJobContextNewNameFromMap(Map<String, String> jobContextMap, final String oldName) {
        if (jobContextMap == null || jobContextMap.isEmpty() || oldName == null) {
            return null;
        }
        for (String newName : jobContextMap.keySet()) {
            final String tmpName = jobContextMap.get(newName);
            if (tmpName != null && tmpName.equals(oldName)) {
                return newName;
            }
        }
        return null;

    }

    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2008, 2, 17, 12, 0, 0);
        return gc.getTime();
    }
}
