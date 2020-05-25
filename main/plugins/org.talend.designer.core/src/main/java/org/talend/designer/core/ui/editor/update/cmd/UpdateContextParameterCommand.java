// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.core.ui.editor.update.cmd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.common.util.EList;
import org.eclipse.gef.commands.Command;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.PlatformUI;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.model.context.ContextUtils;
import org.talend.core.model.context.JobContext;
import org.talend.core.model.context.JobContextManager;
import org.talend.core.model.context.JobContextParameter;
import org.talend.core.model.context.UpdateContextVariablesHelper;
import org.talend.core.model.process.IContext;
import org.talend.core.model.process.IContextManager;
import org.talend.core.model.process.IContextParameter;
import org.talend.core.model.process.IProcess2;
import org.talend.core.model.properties.ContextItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.update.EUpdateItemType;
import org.talend.core.model.update.EUpdateResult;
import org.talend.core.model.update.UpdateResult;
import org.talend.core.model.update.UpdatesConstants;
import org.talend.core.ui.context.ContextManagerHelper;
import org.talend.core.ui.process.UpdateRunJobComponentContextHelper;
import org.talend.designer.core.DesignerPlugin;
import org.talend.designer.core.model.utils.emf.talendfile.ContextParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.ContextType;
import org.talend.designer.core.model.utils.emf.talendfile.impl.ContextTypeImpl;
import org.talend.metadata.managment.ui.utils.ConnectionContextHelper;
import org.talend.repository.RepositoryPlugin;

/**
 * ggu class global comment. Detailled comment
 */
public class UpdateContextParameterCommand extends Command {

    private UpdateResult result;

    public UpdateContextParameterCommand(UpdateResult result) {
        this.result = result;
    }


    @SuppressWarnings("unchecked")
    @Override
    public void execute() {
        Display.getDefault().syncExec(new Runnable() {

            @Override
            public void run() {
                doExecute();
            }
        });
    }

    private void doExecute() {
        if (result == null) {
            return;
        }
        Map<String, ContextType> repoContextMap = new HashMap<String, ContextType>();
        String repoDefaultContextName = null;
        if (result.getParameter() != null) {
            if (result.getParameter() instanceof Item) {
                Item item = (Item) result.getParameter();
                EList<?> contextGroups = ContextUtils.getAllContextType(item);
                repoDefaultContextName = ContextUtils.getDefaultContextName(item);
                for (Object contextGroup : contextGroups) {                                                                                                                                                                                                     
                    if (contextGroup instanceof ContextTypeImpl) {
                        String name = ((ContextTypeImpl) contextGroup).getName();
                        repoContextMap.put(name, ((ContextTypeImpl) contextGroup));
                    }
                }
            }
        }
        Object job = result.getJob();
        if (job == null) {
            return;
        }
        if (job instanceof IProcess2) {
            IProcess2 process = (IProcess2) job;
            Set<String> existGroupNameSet = new HashSet<String>();
            ContextParameterMap deleteParameters = new ContextParameterMap();
            Object updateObject = result.getUpdateObject();
            List<IContext> listContext = process.getContextManager().getListContext();
            for (IContext con : listContext) {
                existGroupNameSet.add(con.getName());
            }
            if (updateObject instanceof Set) {
                Set<String> names = (Set<String>) updateObject;

                if (result.getResultType() == EUpdateResult.ADD && result.isChecked()) {
                    // check parameters that have been added to repository context group
                    checkNewRepositoryParameters(process, names);
                    return;
                }
                for (IContext context : listContext) {
                        for (IContextParameter param : context.getContextParameterList()) {
                        Item item = null;
                            if (names != null && names.contains(param.getName())) {
                                switch (result.getResultType()) {
                                case DELETE:
                                item = (Item) result.getParameter();

                                    if (item != null && item.getProperty().getId().equals(param.getSource())
                                            && result.isChecked()) {
                                        // delete it later
                                        deleteParameters.addParameter(context, param);
                                    } else {
                                        param.setSource(IContextParameter.BUILT_IN);
                                    }
                                    break;
                                case UPDATE:
                                    String sourceContextName = null;
                                    if (repoContextMap.containsKey(context.getName())) {
                                        sourceContextName = context.getName();
                                    } else {
                                        sourceContextName = repoDefaultContextName;
                                    }
                                if (sourceContextName != null) {
                                    item = (Item) result.getParameter();

                                    if (item != null && item.getProperty().getId().equals(param.getSource())
                                            && result.isChecked()) {

                                        ContextUtils.updateParameterFromRepository(item, param, sourceContextName);
                                    } else {
                                        param.setSource(IContextParameter.BUILT_IN);
                                    }
                                }
                                    break;
                                case RENAME:
                                    List<Object> parameter = (List<Object>) result.getParameter();
                                    if (parameter.size() >= 3) {
                                    item = (Item) parameter.get(0);
                                        String sourceId = item.getProperty().getId();
                                        String oldName = (String) parameter.get(1);
                                        String newName = (String) parameter.get(2);
                                        if (oldName.equals(param.getName()) && sourceId.equals(param.getSource())) {
                                            if (newName != null) {
                                                param.setName(newName);
                                                ContextUtils.updateParameterFromRepository(item, param, context.getName());
                                            }
                                        }

                                    }
                                    break;
                                case BUIL_IN: // built-in
                                default:
                                    param.setSource(IContextParameter.BUILT_IN);
                                    break;
                                }
                            }
                    }
                }
            }

            if (updateObject instanceof JobContext) {
                if (result.getResultType() == EUpdateResult.ADD && result.getUpdateType() == EUpdateItemType.CONTEXT_GROUP
                        && result.isChecked()) {
                    IContext context = (IContext) updateObject;
                    String name = context.getName();
                    if (!listContext.contains(context) && result.getParameter() instanceof Item) {
                        Item item = (Item) result.getParameter();

                        JobContext newContext = new JobContext(name);
                        List<IContextParameter> newParamList = new ArrayList<IContextParameter>();
                        newContext.setContextParameterList(newParamList);
                        JobContextParameter param = null;

                        // add other context params to the new group
                        for (IContextParameter contextParam : process.getContextManager().getDefaultContext()
                                .getContextParameterList()) {
                            boolean exist = false;
                            for (IContextParameter existParam : context.getContextParameterList()) {
                                if (contextParam.getName().equals(existParam.getName())) {
                                    exist = true;
                                }
                            }
                            if (exist) {
                                continue;
                            }
                            param = (JobContextParameter) contextParam.clone();
                            final Item contextItem = ContextUtils.getRepositoryContextItemById(param.getSource());
                            if (contextItem != null) {
                                ContextType defaultContext = ContextUtils.getContextTypeByName(contextItem, null);
                                if (defaultContext != null) {
                                    final ContextParameterType conParamType = ContextUtils
                                            .getContextParameterTypeByName(defaultContext, param.getName());
                                    if (conParamType != null) {
                                        ContextUtils.updateParameter(conParamType, param);
                                    }
                                }
                            }
                            param.setContext(newContext);
                            newParamList.add(param);
                        }

                        // current context params for the new group
                        for (int i = 0; i < context.getContextParameterList().size(); i++) {
                            IContextParameter oldParam = context.getContextParameterList().get(i);
                            param = (JobContextParameter) oldParam.clone();
                            param.setSource(item.getProperty().getId());
                            param.setContext(newContext);
                            newParamList.add(param);
                        }
                        // Remove if exist,then add.
                        if (existGroupNameSet.contains(name)) {
                            IContext existContext = process.getContextManager().getContext(name);
                            if (existContext != null) {
                                listContext.remove(existContext);
                            }
                        }
                        listContext.add(newContext);
                    }
                } else if (result.getResultType() == EUpdateResult.DELETE
                        && result.getUpdateType() == EUpdateItemType.CONTEXT_GROUP && result.isChecked()) {
                    IContext context = (IContext) updateObject;
                    if (result.getParameter() instanceof Item) {
                        Item item = (Item) result.getParameter();
                        List<IContext> listC = new ArrayList<IContext>(listContext);
                        for (IContext con : listC) {
                            if (con.getName().equals(context.getName())) {
                                for (IContextParameter oldParam : con.getContextParameterList()) {
                                    if (item.getProperty().getId().equals(oldParam.getSource())) {
                                        listContext.remove(con);
                                    }
                                }
                            }
                        }

                    }
                    return;
                } else if (result.getResultType() == EUpdateResult.RENAME
                        && result.getUpdateType() == EUpdateItemType.CONTEXT_GROUP && result.isChecked()) {
                    IContext context = (IContext) updateObject;
                    IContextManager contextManager = process.getContextManager();
                    Map<IContext, String> renameGroupContext = ((JobContextManager) contextManager).getRenameGroupContext();
                    String oldName = renameGroupContext.get(context);
                    if (result.getParameter() instanceof Item) {
                        Item item = (Item) result.getParameter();
                        for (IContext con : listContext) {
                            if (con.getName().equals(oldName)) {
                                for (IContextParameter oldParam : con.getContextParameterList()) {
                                    if (item.getProperty().getId().equals(oldParam.getSource())) {
                                        con.setName(context.getName());
                                    }
                                }
                            }
                        }

                    }
                }
                return;
            }

            // delete parameters
            deleteParameters.removeFromContext();

            // update parameters
            if (result.getResultType() == EUpdateResult.RENAME) {
                List<Object> parameter = (List<Object>) result.getParameter();
                if (parameter.size() >= 3) {
                    String oldName = (String) parameter.get(1);
                    String newName = (String) parameter.get(2);
                    UpdateContextVariablesHelper.updateProcessForRenamed(process, oldName, newName);

                    // tRunJob parameters rename
                    IEditorReference[] reference = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
                            .getEditorReferences();
                    List<IProcess2> processes = RepositoryPlugin.getDefault().getDesignerCoreService()
                            .getOpenedProcess(reference);
                    Map<String, String> renamedMap = new HashMap<String, String>();
                    renamedMap.put(newName, oldName);

                    UpdateRunJobComponentContextHelper.updateOpenedJobRunJobComponentReference(processes, renamedMap,
                            process.getId(), null);
                    try {
                        // perhaps, need optimize.
                        UpdateRunJobComponentContextHelper.updateItemRunJobComponentReference(DesignerPlugin.getDefault()
                                .getProxyRepositoryFactory(), renamedMap, process.getId(), null);
                    } catch (PersistenceException e) {
                        ExceptionHandler.process(e);
                    }
                }
            }
        }

    }

    /**
     * DOC hcw Comment method "checkAddedParameters".
     *
     * @param process
     * @param names
     */
    private void checkNewRepositoryParameters(IProcess2 process, Set<String> names) {
        // add context in repository
        ContextManagerHelper helper = new ContextManagerHelper(process.getContextManager());
        Set<ContextItem> contextItemList = helper.getContextItems();
        ContextItem item = (ContextItem) result.getParameter();
        // this job contains the repository context group
        if (contextItemList.contains(item)) {
            ContextType contextType = ContextUtils.getContextTypeByName(item, null);
            for (String paramName : names) {
                ContextParameterType contextParameterType = ContextUtils.getContextParameterTypeByName(contextType, paramName);
                // check if there is a parameter with same name
                // IContextParameter paramExisted = helper.getExistedContextParameter(contextParameterType.getName());
                // if (paramExisted == null) {
                helper.addContextParameterType(contextParameterType);
                // }
            }
        }

        String remark = result.getRemark();
        ConnectionContextHelper.showContextGroupDialog(process, item, process.getContextManager(), names,
                remark != null && remark.endsWith(UpdatesConstants.CONTEXT_MODE));
    }

    /**
     *
     * DOC hcw UpdateContextParameterCommand class global comment. Detailled comment
     */
    static class ContextParameterMap {

        private Map<IContext, Set<IContextParameter>> map = new HashMap<IContext, Set<IContextParameter>>();

        public void addParameter(IContext item, IContextParameter param) {
            Set<IContextParameter> params = map.get(item);
            if (params == null) {
                params = new HashSet<IContextParameter>();
                map.put(item, params);
            }
            params.add(param);
        }

        public void removeFromContext() {
            if (map.size() > 0) {
                for (IContext context : map.keySet()) {
                    Set<IContextParameter> params = map.get(context);
                    for (IContextParameter param : params) {
                        context.getContextParameterList().remove(param);
                    }
                }
            }
        }
    }
}
