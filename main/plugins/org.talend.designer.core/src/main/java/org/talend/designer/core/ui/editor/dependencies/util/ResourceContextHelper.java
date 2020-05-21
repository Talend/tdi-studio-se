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
package org.talend.designer.core.ui.editor.dependencies.util;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.talend.commons.exception.BusinessException;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.language.LanguageManager;
import org.talend.core.model.context.ContextUtils;
import org.talend.core.model.context.JobContextManager;
import org.talend.core.model.context.JobContextParameter;
import org.talend.core.model.metadata.types.ContextParameterJavaTypeManager;
import org.talend.core.model.metadata.types.JavaTypesManager;
import org.talend.core.model.process.IContext;
import org.talend.core.model.process.IContextManager;
import org.talend.core.model.process.IContextParameter;
import org.talend.core.model.process.IProcess2;
import org.talend.core.model.properties.ContextItem;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.update.RepositoryUpdateManager;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.runtime.CoreRuntimePlugin;
import org.talend.core.ui.context.ContextTemplateComposite;
import org.talend.core.ui.context.model.table.ContextTableTabChildModel;
import org.talend.core.ui.context.model.table.ContextTableTabParentModel;
import org.talend.core.ui.context.nattableTree.ContextNatTableUtils;
import org.talend.core.ui.editor.command.ContextAddParameterCommand;
import org.talend.core.utils.TalendQuoteUtils;
import org.talend.designer.core.IDesignerCoreService;
import org.talend.designer.core.ui.editor.dependencies.model.JobContextTreeNode;
import org.talend.designer.core.ui.editor.dependencies.model.JobResourceDependencyModel;
import org.talend.repository.ProjectManager;

public class ResourceContextHelper {

    private IProcess2 process;

    private CommandStack commandStack;

    public Set<String> Context_builtIn_cache = new HashSet<String>();

    public ResourceContextHelper(IProcess2 process, CommandStack commandStack) {
        super();
        this.process = process;
        this.commandStack = commandStack;
    }

    public void initContextBuiltInCache() {
        List<IContextParameter> contexts = ContextTemplateComposite
                .computeContextTemplate(process.getContextManager().getListContext());
        for (IContextParameter contextPar : contexts) {
            if (IContextParameter.BUILT_IN.equals(contextPar.getSource())) {
                Context_builtIn_cache.add(contextPar.getName());
            }
        }
    }

    public boolean checkContextIsNotBuiltIn(String contextName) {
        if (Context_builtIn_cache.contains(contextName)) {
            return false;
        }
        return true;
    }

    public boolean checkIfContextVarIsInUse(String contextVar, String contextSource, String resStatePath) {
        IContext defaultContext = process.getContextManager().getDefaultContext();
        IContextParameter contextParameter = defaultContext.getContextParameter(contextSource, contextVar);
        if (contextParameter != null && StringUtils.isNotBlank(contextParameter.getValue())) {
            return true;
        }
        return false;
    }

    public void addContextParameterForResource() {
        IContextParameter parameter = createNewResourceEntry();
        IContextManager contextManager = process.getContextManager();
        if (parameter == null || contextManager == null) {
            return;
        }
        parameter.setSource(IContextParameter.BUILT_IN);
        commandStack.execute(new ContextAddParameterCommand(contextManager, parameter));

        if (ContextUtils.isPropagateContextVariable()) {
            if (contextManager != null && contextManager instanceof JobContextManager) {
                JobContextManager jobManger = (JobContextManager) contextManager;
                jobManger.setModified(true);
                jobManger.addNewParameters(parameter.getName());
            }
        }
    }


    public JobContextTreeNode createRootJobContextTreeNode() {
        JobContextTreeNode rootNode = new JobContextTreeNode(0, null, null, "root");
        for (JobContextTreeNode node : constructJobContextTreeNodes()) {
            if (node.getParent() != null) {
                continue;
            }
            rootNode.addChild(node);
        }
        return rootNode;
    }

    public List<JobContextTreeNode> constructJobContextTreeNodes() {
        IContextManager manager = process.getContextManager();
        List<JobContextTreeNode> treeNodes = new LinkedList<JobContextTreeNode>();
        List<IContextParameter> contextDatas = computeContextTemplate(manager.getDefaultContext());
        List<ContextTableTabParentModel> listofData = ContextNatTableUtils.constructContextDatas(contextDatas);

        for (ContextTableTabParentModel contextModel : listofData) {
            if (contextModel.hasChildren()) {
                // currently will not display repository context variable, later improve should uncomment follow
                // JobContextTreeNode parentTreeNode = createJobContextTreeNode(contextModel.getOrder(), contextModel,
                // null,
                // contextModel.getSourceName(), treeNodes);
                // List<ContextTabChildModel> childModels = contextModel.getChildren();
                // for (ContextTabChildModel childModel : childModels) {
                // createJobContextTreeNode(contextModel.getOrder(), childModel, parentTreeNode,
                // childModel.getContextParameter().getName(), treeNodes);
                // }
            } else {
                createJobContextTreeNode(contextModel.getOrder(), contextModel, null,
                        contextModel.getContextParameter().getName(),
                        treeNodes);
            }
        }
        return treeNodes;
    }

    private JobContextTreeNode createJobContextTreeNode(int orderId, Object data,
            JobContextTreeNode parent, String currentNodeName, List<JobContextTreeNode> treeNodes) {
        IContextParameter contextParameter = ((ContextTableTabParentModel) data).getContextParameter();
        JobContextTreeNode datum = null;
        if ((JavaTypesManager.RESOURCE.getId().equals(contextParameter.getType())
                || JavaTypesManager.RESOURCE.getLabel().equals(contextParameter.getType()))
                && StringUtils.isBlank(contextParameter.getValue())) {
            datum = new JobContextTreeNode(orderId, data, parent, currentNodeName);
            treeNodes.add(datum);

        }
        return datum;
    }

    private List<IContextParameter> computeContextTemplate(IContext context) {
        List<IContextParameter> contextTemplate = new ArrayList<IContextParameter>();
        if (context != null) {
            List<IContextParameter> paras = context.getContextParameterList();
            for (IContextParameter contextParameter : paras) {
                contextTemplate.add(contextParameter.clone());
            }

        }
        return contextTemplate;
    }

    public static final String NEW_PARAM_NAME = "new"; //$NON-NLS-1$

    public JobContextParameter createNewResourceEntry() {
        List<IContextParameter> listParams = process.getContextManager().getDefaultContext().getContextParameterList();
        Integer numParam = new Integer(1);
        boolean paramNameFound;
        String paramName = null;
        do { // look for a new name
            paramNameFound = true;
            paramName = NEW_PARAM_NAME + numParam;
            for (int i = 0; i < listParams.size(); i++) {
                if (paramName.equals(listParams.get(i).getName())) {
                    paramNameFound = false;
                }
            }
            if (!paramNameFound) {
                numParam++;
            }
        } while (!paramNameFound);

        JobContextParameter contextParam = new JobContextParameter();
        contextParam.setName(paramName);
        ECodeLanguage curLanguage = LanguageManager.getCurrentLanguage();
        contextParam.setType(JavaTypesManager.RESOURCE.getId());
        contextParam.setPrompt(paramName + "?"); //$NON-NLS-1$
        String defaultValue;
        if (curLanguage == ECodeLanguage.JAVA) {
            defaultValue = ContextParameterJavaTypeManager
                    .getDefaultValueFromJavaIdType(ContextParameterJavaTypeManager.getDefaultJavaType().getId(), false);
        } else {
            defaultValue = TalendQuoteUtils.addQuotes(""); //$NON-NLS-1$
        }
        contextParam.setValue(defaultValue);
        contextParam.setComment(""); //$NON-NLS-1$
        contextParam.setSource(""); //$NON-NLS-1$
        return contextParam;
    }

    public String updateRepositoryContext(JobResourceDependencyModel originalModel, JobContextTreeNode result) {
        String sourceId = null;
        if (result == null) {
            sourceId = originalModel.getContextSource();
        } else {
            sourceId = ((ContextTableTabChildModel) result.getTreeData()).getSourceId();
        }
        if (IContextParameter.BUILT_IN.equals(sourceId) || sourceId == null) {
            return null;
        }
        ProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
        try {
            IRepositoryViewObject object = factory.getLastRefVersion(ProjectManager.getInstance().getCurrentProject(), sourceId);
            if (object == null) {
                return null;
            }
            boolean resetToDefault = false;
            String defaultType = null;
            String defaultValue = null;
            /**
             * if after want to set repository context value to default while deleting resource dependency or changing
             * relationship between resource and context, uncomment follow
             */
            // if (result == null || (sourceId.equals(originalModel.getContextSource())
            // && !result.getName().equals(originalModel.getContextVar()))) {
            // resetToDefault = true;
            // ECodeLanguage curLanguage = LanguageManager.getCurrentLanguage();
            // if (curLanguage == ECodeLanguage.JAVA) {
            // defaultType = ContextParameterJavaTypeManager.getDefaultJavaType().getId();
            // defaultValue = ContextParameterJavaTypeManager
            // .getDefaultValueFromJavaIdType(ContextParameterJavaTypeManager.getDefaultJavaType().getId(), false);
            // } else {
            // defaultType = MetadataTalendType.getDefaultTalendType();
            // defaultValue = TalendQuoteUtils.addQuotes(""); //$NON-NLS-1$
            // }
            // }
            factory.lock(object);
            ContextItem contextItem = (ContextItem) object.getProperty().getItem();
            IContextManager contextManager = new JobContextManager(contextItem.getContext(), contextItem.getDefaultContext());
            for (IContext context : contextManager.getListContext()) {
                if (result != null) {
                    IContextParameter contextParameter = context.getContextParameter(result.getName());
                    contextParameter.setType(JavaTypesManager.RESOURCE.getId());
                    contextParameter.setValue(originalModel.getPathUrl());
                }

                // set original to default
                if (resetToDefault) {
                    IContextParameter originalContext = context.getContextParameter(originalModel.getContextVar());
                    if (originalContext != null && defaultType != null && defaultValue != null) {
                        originalContext.setType(defaultType);
                        originalContext.setValue(defaultValue);
                    }
                }

            }

            contextManager.saveToEmf(contextItem.getContext(), false);
            RepositoryUpdateManager.updateContext((JobContextManager) contextManager, contextItem);
            final IWorkspaceRunnable op = new IWorkspaceRunnable() {

                public void run(IProgressMonitor monitor) throws CoreException {
                    try {
                        factory.save(contextItem);
                    } catch (PersistenceException e) {
                        ExceptionHandler.process(e);
                    }
                }
            };
            IRunnableWithProgress iRunnableWithProgress = new IRunnableWithProgress() {

                public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                    IWorkspace workspace = ResourcesPlugin.getWorkspace();
                    try {
                        ISchedulingRule schedulingRule = workspace.getRoot();
                        // the update the project files need to be done in the workspace runnable to avoid all
                        // notification
                        // of changes before the end of the modifications.
                        workspace.run(op, schedulingRule, IWorkspace.AVOID_UPDATE, monitor);
                    } catch (CoreException e) {
                        throw new InvocationTargetException(e);
                    }

                }
            };
            try {
                new ProgressMonitorDialog(null).run(true, true, iRunnableWithProgress);
            } catch (InvocationTargetException e) {
                ExceptionHandler.process(e);
            } catch (InterruptedException e) {
                //
            }
            updateRelatedView();
            factory.unlock(object);
            ProxyRepositoryFactory.getInstance().saveProject(ProjectManager.getInstance().getCurrentProject());

        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
            return e.toString();
        } catch (BusinessException e1) {
            // Nothing to do
        }
        return null;
    }

    private void updateRelatedView() {
        IDesignerCoreService designerCoreService = CoreRuntimePlugin.getInstance().getDesignerCoreService();
        if (designerCoreService != null) {
            designerCoreService.switchToCurContextsView();
            // for tRunJob component
            designerCoreService.switchToCurComponentSettingsView();
            // for 2608
            designerCoreService.switchToCurJobSettingsView();
        }
    }

}
