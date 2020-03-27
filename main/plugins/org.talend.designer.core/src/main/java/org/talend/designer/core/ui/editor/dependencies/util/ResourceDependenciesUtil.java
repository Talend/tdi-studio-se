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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.ecore.EObject;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.utils.io.FilesUtils;
import org.talend.commons.utils.workbench.resources.ResourceUtils;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.general.Project;
import org.talend.core.model.metadata.types.JavaTypesManager;
import org.talend.core.model.process.IContext;
import org.talend.core.model.process.IContextParameter;
import org.talend.core.model.process.IProcess2;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.resources.ResourceItem;
import org.talend.core.model.utils.JavaResourcesHelper;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.runtime.process.ITalendProcessJavaProject;
import org.talend.designer.core.ui.editor.dependencies.model.JobResourceDependencyModel;
import org.talend.designer.runprocess.IRunProcessService;
import org.talend.repository.ProjectManager;

public class ResourceDependenciesUtil {

    private static final String COMMA_TAG = ",";

    private static final String SEG_TAG = "/";

    private static final String SLASH_TAG = "|";

    private static final String COLON_TAG = ":";

    private static final String REPACE_SLASH_TAG = "\\|";

    private static final String RESOURCES_PROP = "RESOURCES_PROP";

    private static final String RESOURCES_FOLDER = ERepositoryObjectType.RESOURCES.getFolder();

    private static final String SRC_RESOURCES_FOLDER = "resources";

    private static final String POMS_JOBS_FOLDER = "poms/jobs/";

    private static final String SRC_EXTRESOURCE_FOLDER = "/src/main/ext-resources";


    public static Collection<JobResourceDependencyModel> getResourceDependencies(IProcess2 process) {
        Property property = process.getProperty();
        String jobLabel = JavaResourcesHelper.getJobFolderName(property.getLabel(), property.getVersion());
        return getResourceDependencies((String) process.getAdditionalProperties().get(RESOURCES_PROP), jobLabel,
                process);
    }

    private static Collection<JobResourceDependencyModel> getResourceDependencies(String resource, String jobLabel,
            IProcess2 process) {
        Collection<JobResourceDependencyModel> models = new ArrayList<JobResourceDependencyModel>();
        if (resource != null) {
            for (String id : resource.split(COMMA_TAG)) {
                String[] parts = id.split(REPACE_SLASH_TAG);
                if (parts.length == 2 || parts.length > 2) {
                    JobResourceDependencyModel model = createDependency(parts[0], parts[1], jobLabel, process);
                    // if the Resource item was force deleted, model would be null
                    if (model != null) {
                        models.add(model);
                    }
                }
            }

        }
        return models;
    }

    public static JobResourceDependencyModel createDependency(String id, String version, String jobLabel,
            IProcess2 process) {
        try {
            String realVersion = version;
            IContextParameter contextPar = getContextOfResouceByProcess(process, id);
            if (contextPar != null && StringUtils.isNotBlank(contextPar.getValue())
                    && contextPar.getValue().split(REPACE_SLASH_TAG).length > 1) {
                realVersion = contextPar.getValue().split(REPACE_SLASH_TAG)[1];
            }

            final IRepositoryViewObject rvo;
            if (JobResourceDependencyModel.LATEST_VERSION.equals(realVersion)) {
                rvo = ProxyRepositoryFactory.getInstance().getLastVersion(id);
            } else {
                rvo = ProxyRepositoryFactory.getInstance().getSpecificVersion(id, realVersion, true);
            }
            if (rvo != null) {
                final JobResourceDependencyModel model = new JobResourceDependencyModel(
                        (ResourceItem) rvo.getProperty().getItem());

                if (contextPar != null) {
                    model.setContextVar(contextPar.getName());
                    model.setContextSource(contextPar.getSource());
                }
                model.setSelectedVersion(realVersion);
                model.setResourceDepPath(ResourceDependenciesUtil.getResourcePath(model, jobLabel, model.getSelectedVersion()));
                return model;
            }
        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
        }
        return null;
    }

    public static IContextParameter getContextOfResouceByProcess(IProcess2 process, String resourceId) {
        List<IContextParameter> contextParameterList = process.getContextManager().getDefaultContext().getContextParameterList();
        for (IContextParameter contextPar : contextParameterList) {
            if (JavaTypesManager.RESOURCE.getId().equals(contextPar.getType())
                    || JavaTypesManager.RESOURCE.getLabel().equals(contextPar.getType())) {
                String resource = contextPar.getValue();
                if (StringUtils.isNotBlank(resource) && resource.contains(SLASH_TAG)) {
                    String[] part = resource.split(REPACE_SLASH_TAG);
                    if (part[0].equals(resourceId)) {
                        return contextPar;
                    }
                }
            }
        }
        return null;
    }

    public static void saveResourceDependency(Map<Object, Object> map, Collection<JobResourceDependencyModel> models) {
        final StringBuilder sb = new StringBuilder();
        for (JobResourceDependencyModel item : models) {
            if (item.isBuiltIn()) {
                continue;
            }
            if (sb.length() != 0) {
                sb.append(COMMA_TAG);
            }
            sb.append(item.getItem().getProperty().getId());
            sb.append(SLASH_TAG);
            sb.append(item.getSelectedVersion());
        }
        if (sb.length() > 0) {
            map.put(RESOURCES_PROP, sb.toString());
        } else {
            map.remove(RESOURCES_PROP);
        }
    }

    public static void setContextVarForResources(IProcess2 process, Collection<JobResourceDependencyModel> models) {
        IContext defaultContext = process.getContextManager().getDefaultContext();
        List<IContext> listContext = process.getContextManager().getListContext();
        // for remove resources data clean all context variable
        cleanAllResourceContextVariable(listContext, defaultContext.getName());
        for (JobResourceDependencyModel item : models) {
            if (StringUtils.isNotBlank(item.getContextVar())) {
                IContextParameter contextPar = defaultContext.getContextParameter(item.getContextSource(), item.getContextVar());
                if (contextPar != null) {
                    contextPar.setType(JavaTypesManager.RESOURCE.getId());
                    contextPar.setValue(createResourceContextValue(item));
                }
            }
        }
        process.getContextManager().fireContextsChangedEvent();
    }

    private static void cleanAllResourceContextVariable(List<IContext> listContext, String contextName) {
        for (IContext context : listContext) {
            if (!context.getName().equals(contextName)) {
                continue;
            }
            for (IContextParameter contextPar : context.getContextParameterList()) {
                if (contextPar.getType().equals(JavaTypesManager.RESOURCE.getId())
                        && StringUtils.isNotBlank(contextPar.getValue())
                        && IContextParameter.BUILT_IN.equals(contextPar.getSource())) {
                    contextPar.setValue("");
                }
            }
        }
    }

    public static void cleanContextFromAllEnvir(IProcess2 process, String resourceId) {
        List<IContext> listContext = process.getContextManager().getListContext();
        for (IContext context : listContext) {
            for (IContextParameter contextPar : context.getContextParameterList()) {
                if (contextPar.getType().equals(JavaTypesManager.RESOURCE.getId())
                        && StringUtils.isNotBlank(contextPar.getValue())
                        && IContextParameter.BUILT_IN.equals(contextPar.getSource())
                        && resourceId.equals(contextPar.getValue().split(REPACE_SLASH_TAG)[0])) {
                    contextPar.setValue("");
                }
            }
        }
    }

    public static void copyToExtResourceFolder(IRepositoryViewObject repoObject, String jobId, String jobVersion, String version,
            String rootJobLabel) {
        JobResourceDependencyModel model = new JobResourceDependencyModel((ResourceItem) repoObject.getProperty().getItem());
        copyToExtResourceFolder(model, jobId, jobVersion, version, rootJobLabel);
    }

    public static String getResourcePath(JobResourceDependencyModel model, String jobLabel, String newVersion) {
        Project currentProject = ProjectManager.getInstance().getCurrentProject();
        ResourceItem item = model.getItem();
        String version = item.getProperty().getVersion();
        if (StringUtils.isNotBlank(newVersion) && !model.LATEST_VERSION.equals(newVersion)) {
            version = newVersion;
        } else if (model.LATEST_VERSION.equals(newVersion)) {
            try {
                IRepositoryViewObject repoObject = ProxyRepositoryFactory.getInstance()
                        .getLastVersion(model.getItem().getProperty().getId());
                if (repoObject != null) {
                    version = repoObject.getVersion();
                }
            } catch (PersistenceException e) {
                ExceptionHandler.process(e);
            }
        }

        StringBuffer resourcePath = new StringBuffer();
        if (!"".equals(item.getState().getPath())) {
            resourcePath.append(item.getState().getPath() + SEG_TAG);
        }
        Path p = new Path(item.getProperty().getLabel());
        resourcePath.append(p.removeFileExtension().lastSegment());
        resourcePath.append("_" + version + "." + item.getBindingExtension());
        // Local_Project/testjob_0_2/resources/test_0.1.txt
        /*
         * Local_Project project Label need to lower case avoid the exception of
         * org.eclipse.core.internal.resources.ResourceException: A resource exists with a different case caused by
         * ext-resources/local_project/testjob_0_2/contexts
         */
        String newFilePath = JavaResourcesHelper.getProjectFolderName(currentProject.getTechnicalLabel()) + SEG_TAG + jobLabel
                + SEG_TAG + SRC_RESOURCES_FOLDER
                + SEG_TAG + resourcePath.toString();
        return newFilePath;
    }

    public static void copyToExtResourceFolder(JobResourceDependencyModel model, String jobId, String jobVersion,
            String newVersion, String rootJobLabel) {
        IRepositoryViewObject jobObject = null;
        try {
            jobObject = ProxyRepositoryFactory.getInstance().getSpecificVersion(jobId, jobVersion, true);
        } catch (PersistenceException e1) {
            ExceptionHandler.process(e1);
        }
        if (jobObject == null) {
            return;
        }
        Property property = jobObject.getProperty();
        String jobLabel = JavaResourcesHelper.getJobFolderName(property.getLabel(), property.getVersion());

        ResourceItem item = model.getItem();
        String version = item.getProperty().getVersion();
        if (StringUtils.isNotBlank(newVersion) && !model.LATEST_VERSION.equals(newVersion)) {
            version = newVersion;
        }
        String fileSuffix = "_" + version + "." + item.getBindingExtension();
        IProject project = getWorkspaceProject(item);
        if (project == null) {
            return;
        }

        String itemResPath = model.getPathUrl() + fileSuffix;
        File resourceFile = project.getFile(new Path(RESOURCES_FOLDER + SEG_TAG + itemResPath)).getLocation().toFile();
        if (resourceFile.exists()) {
            File targetFile = new File(getJobExecuteResourceFilePath(model, property, jobLabel, newVersion));
            try {
                FilesUtils.copyFile(resourceFile, targetFile);
            } catch (IOException e) {
                ExceptionHandler.process(e);
            }
        }
    }

    public static String getJobExecuteResourceFilePath(JobResourceDependencyModel model, Property jobProperty,String jobLabel,String resourceVersion) {
        String path = null;
        IRunProcessService service = null;
        if (GlobalServiceRegister.getDefault().isServiceRegistered(IRunProcessService.class)) {
            service = GlobalServiceRegister.getDefault().getService(IRunProcessService.class);
        }
        if (service == null) {
            return path;
        }
        String filePath = getResourcePath(model, jobLabel, resourceVersion);
        ITalendProcessJavaProject talendJobJavaProject = service.getTalendJobJavaProject(jobProperty);
        path = talendJobJavaProject.getExternalResourcesFolder().getFile(new Path(filePath)).getLocation().toOSString();
        return path;
    }

    private static IProject getWorkspaceProject(EObject object) {
        IProject project = null;
        try {
            org.talend.core.model.properties.Project emfProject = ProjectManager.getInstance().getProject(object);
            if (emfProject != null) {
                project = ResourceUtils.getProject(emfProject.getTechnicalLabel());
            }
        } catch (PersistenceException e1) {
            ExceptionHandler.process(e1);
        }
        return project;
    }

    public static void deleteFromResourceFolder(JobResourceDependencyModel model, String jobId, String jobVersion) {
        IRepositoryViewObject jobObject = null;
        try {
            jobObject = ProxyRepositoryFactory.getInstance().getSpecificVersion(jobId, jobVersion, true);
        } catch (PersistenceException e1) {
            ExceptionHandler.process(e1);
        }
        if (jobObject == null) {
            return;
        }
        Property property = jobObject.getProperty();
        StringBuffer labelBuf = new StringBuffer();
        if (StringUtils.isNotBlank(property.getItem().getState().getPath())) {
            labelBuf.append(property.getItem().getState().getPath() + "/");
        }
        labelBuf.append(property.getLabel() + "_" + property.getVersion());
        String jobLabel = labelBuf.toString();

        ResourceItem item = model.getItem();
        Path p = new Path(item.getProperty().getLabel());
        String itenName = p.removeFileExtension().lastSegment();
        Project currentProject = ProjectManager.getInstance().getCurrentProject();
        IProject project = null;
        try {
            project = ResourceUtils.getProject(currentProject.getTechnicalLabel());
        } catch (PersistenceException e1) {
            ExceptionHandler.process(e1);
        }
        if (project == null) {
            return;
        }

        String statePath = "";
        if (!"".equals(item.getState().getPath())) {
            statePath = item.getState().getPath() + SEG_TAG;
        }
        String extResPath = getProcessFolder(jobObject) + jobLabel + SRC_EXTRESOURCE_FOLDER;
        // for job testjob_0.2 => testjob_0_2
        String checkversion = jobLabel.substring(jobLabel.lastIndexOf("_"));
        if (checkversion.contains(".")) {
            jobLabel = jobLabel.substring(0, jobLabel.lastIndexOf("_")) + checkversion.replace(".", "_");
        }
        String newFilePath = currentProject.getLabel().toLowerCase() + SEG_TAG + jobLabel + SEG_TAG + SRC_RESOURCES_FOLDER
                + SEG_TAG
                + statePath;
        File targetFolder = project.getFile(new Path(extResPath + SEG_TAG + newFilePath)).getLocation().toFile();
        if (targetFolder.exists() && targetFolder.isDirectory()) {
            for (File file : targetFolder.listFiles()) {
                if (file.isDirectory()) {
                    continue;
                }
                if (itenName.equals(file.getName().substring(0, file.getName().lastIndexOf("_")))) {
                    file.delete();
                }
            }
        }
    }

    public static String getProcessFolder(IRepositoryViewObject jobObject) {
        String folder = jobObject.getRepositoryObjectType().getFolder();
        return POMS_JOBS_FOLDER + folder + "/";
    }

    public static String createResourceContextValue(JobResourceDependencyModel model) {
        return model.getItem().getProperty().getId() + SLASH_TAG + model.getSelectedVersion();
    }

}
