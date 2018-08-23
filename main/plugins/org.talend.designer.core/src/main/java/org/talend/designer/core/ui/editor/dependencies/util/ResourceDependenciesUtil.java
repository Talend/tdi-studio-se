package org.talend.designer.core.ui.editor.dependencies.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.ResourcesPlugin;
import org.talend.camel.core.model.camelProperties.RouteResourceItem;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.utils.io.FilesUtils;
import org.talend.core.model.general.Project;
import org.talend.core.model.metadata.types.ContextParameterJavaTypeManager;
import org.talend.core.model.metadata.types.JavaTypesManager;
import org.talend.core.model.process.IContext;
import org.talend.core.model.process.IContextParameter;
import org.talend.core.model.process.IProcess2;
import org.talend.core.model.properties.Item;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.designer.core.ui.editor.dependencies.model.JobResourceDependencyModel;
import org.talend.repository.ProjectManager;

public class ResourceDependenciesUtil {

    private static final String COMMA_TAG = ",";

    private static final String SEG_TAG = "/";

    private static final String SLASH_TAG = "|";

    private static final String COLON_TAG = ":";

    private static final String REPACE_SLASH_TAG = "\\|";

    private static final String RESOURCES_PROP = "RESOURCES_PROP";

    private static final String ROUTE_RESOURCES_FOLDER = "route_resources";

    private static final String SRC_RESOURCES_FOLDER = "resources";

    private static final String POMS_PROCESS_FOLDER = "poms/jobs/process/";

    private static final String SRC_EXTRESOURCE_FOLDER = "/src/main/ext-resources";

    public static Collection<JobResourceDependencyModel> getResourceDependencies(Item item) {
        return getResourceDependencies((String) item.getProperty().getAdditionalProperties().get(RESOURCES_PROP),
                item.getProperty().getLabel() + "_" + item.getProperty().getVersion());
    }

    public static Collection<JobResourceDependencyModel> getResourceDependencies(IProcess2 process) {
        return getResourceDependencies((String) process.getAdditionalProperties().get(RESOURCES_PROP),
                process.getProperty().getLabel() + "_" + process.getProperty().getVersion());
    }

    private static Collection<JobResourceDependencyModel> getResourceDependencies(String resource, String jobLabel) {
        Collection<JobResourceDependencyModel> models = new ArrayList<JobResourceDependencyModel>();
        if (resource != null) {
            for (String id : resource.split(COMMA_TAG)) {
                String[] parts = id.split(REPACE_SLASH_TAG);
                if (parts.length == 2 || parts.length > 2) {
                    JobResourceDependencyModel model = createDependency(parts[0], parts[1], parts.length > 2 ? parts[2] : null,
                            jobLabel);
                    models.add(model);
                }
            }

        }
        return models;
    }

    public static JobResourceDependencyModel createDependency(String id, String version, String contextInfo, String jobLabel) {
        try {
            final IRepositoryViewObject rvo;
            if (JobResourceDependencyModel.LATEST_VERSION.equals(version)) {
                rvo = ProxyRepositoryFactory.getInstance().getLastVersion(id);
            } else {
                rvo = ProxyRepositoryFactory.getInstance().getSpecificVersion(id, version, true);
            }
            if (rvo != null) {
                final JobResourceDependencyModel model = new JobResourceDependencyModel(
                        (RouteResourceItem) rvo.getProperty().getItem());
                model.setSelectedVersion(version);
                model.setResourceDepPath(ResourceDependenciesUtil.getResourcePath(model, jobLabel, version));
                if (StringUtils.isNotBlank(contextInfo)) {
                    String[] contextPart = contextInfo.split(COLON_TAG);
                    if (contextPart.length == 2) {
                        model.setContextVar(contextPart[0]);
                        model.setContextSource(contextPart[1]);
                    }
                }
                return model;
            }
        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
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
            if (StringUtils.isNotBlank(item.getContextVar())) {
                sb.append(SLASH_TAG);
                sb.append(item.getContextVar() + COLON_TAG + item.getContextSource());
            }
        }
        if (sb.length() > 0) {
            map.put(RESOURCES_PROP, sb.toString());
        } else {
            map.remove(RESOURCES_PROP);
        }
    }

    public static void setContextVarForResources(IProcess2 process, Collection<JobResourceDependencyModel> models) {
        List<IContext> listContext = process.getContextManager().getListContext();
        // for remove resources data clean all context variable
        cleanAllResourceContextVariable(listContext);
        for (JobResourceDependencyModel item : models) {
            if (StringUtils.isNotBlank(item.getContextVar())) {
                for (IContext contextParameters : listContext) {
                    // List<IContextParameter> contextParameterList = contextParameters.getContextParameterList();
                    IContextParameter contextPar = contextParameters.getContextParameter(item.getContextSource(),
                            item.getContextVar());
                    if (contextPar != null) {
                        contextPar.setType(JavaTypesManager.RESOURCE.getId());
                        contextPar.setValue(item.getResourceDepPath());
                    }
                }
            }
        }
        process.getContextManager().fireContextsChangedEvent();
    }

    private static void cleanAllResourceContextVariable(List<IContext> listContext) {
        for (IContext context : listContext) {
            for (IContextParameter contextPar : context.getContextParameterList()) {
                if (contextPar.getType().equals(JavaTypesManager.RESOURCE.getId())
                        && StringUtils.isNotBlank(contextPar.getValue())) {
                    contextPar.setType(ContextParameterJavaTypeManager.getDefaultJavaType().getId());
                    contextPar.setValue("");
                }
            }
        }
    }

    public static void copyToExtResourceFolder(IRepositoryViewObject repoObject, String jobLabel, String version) {
        JobResourceDependencyModel model = new JobResourceDependencyModel((RouteResourceItem) repoObject.getProperty().getItem());
        copyToExtResourceFolder(model, jobLabel, version);
    }

    public static String getResourcePath(JobResourceDependencyModel model, String jobLabel, String newVersion) {
        Project currentProject = ProjectManager.getInstance().getCurrentProject();
        RouteResourceItem item = model.getItem();
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
        String statePath = "";
        if (!"".equals(item.getState().getPath())) {
            statePath = item.getState().getPath() + SEG_TAG;
        }
        String fileSuffix = "_" + version + "." + item.getBindingExtension();
        // for job testjob_0.2 => testjob_0_2
        String checkversion = jobLabel.substring(jobLabel.lastIndexOf("_"));
        if (checkversion.contains(".")) {
            jobLabel = jobLabel.substring(0, jobLabel.lastIndexOf("_")) + checkversion.replace(".", "_");
        }
        // Local_Project/testjob_0_2/resources/test_0.1.txt
        /*
         * Local_Project project Label need to lower case avoid the exception of
         * org.eclipse.core.internal.resources.ResourceException: A resource exists with a different case caused by
         * ext-resources/local_project/testjob_0_2/contexts
         */
        String newFilePath = currentProject.getLabel().toLowerCase() + SEG_TAG + jobLabel + SEG_TAG + SRC_RESOURCES_FOLDER
                + SEG_TAG + statePath
                + item.getName() + fileSuffix;
        return newFilePath;
    }

    public static void copyToExtResourceFolder(JobResourceDependencyModel model, String jobLabel, String newVersion) {
        RouteResourceItem item = model.getItem();
        Project currentProject = ProjectManager.getInstance().getCurrentProject();
        String version = item.getProperty().getVersion();
        if (StringUtils.isNotBlank(newVersion) && !model.LATEST_VERSION.equals(newVersion)) {
            version = newVersion;
        }
        String fileSuffix = "_" + version + "." + item.getBindingExtension();
        String projectWorkspace = ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString() + SEG_TAG
                + currentProject.getTechnicalLabel();
        String itemResPath = model.getPathUrl() + fileSuffix;
        File resourceFile = new File(projectWorkspace + SEG_TAG + ROUTE_RESOURCES_FOLDER + SEG_TAG + itemResPath);
        if (resourceFile.exists()) {
            String extResPath = POMS_PROCESS_FOLDER + jobLabel + SRC_EXTRESOURCE_FOLDER;
            String newFilePath = getResourcePath(model, jobLabel, newVersion);
            File targetFile = new File(projectWorkspace + SEG_TAG + extResPath + SEG_TAG + newFilePath);
            if (!targetFile.exists()) {
                try {
                    FilesUtils.copyFile(resourceFile, targetFile);
                } catch (IOException e) {
                    ExceptionHandler.process(e);
                }
            }
        }
    }

    public static void deleteFromResourceFolder(JobResourceDependencyModel model, String jobLabel) {
        RouteResourceItem item = model.getItem();
        Project currentProject = ProjectManager.getInstance().getCurrentProject();
        String projectWorkspace = ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString() + SEG_TAG
                + currentProject.getTechnicalLabel();
        String statePath = "";
        if (!"".equals(item.getState().getPath())) {
            statePath = item.getState().getPath() + SEG_TAG;
        }
        String extResPath = POMS_PROCESS_FOLDER + jobLabel + SRC_EXTRESOURCE_FOLDER;
        // for job testjob_0.2 => testjob_0_2
        String checkversion = jobLabel.substring(jobLabel.lastIndexOf("_"));
        if (checkversion.contains(".")) {
            jobLabel = jobLabel.substring(0, jobLabel.lastIndexOf("_")) + checkversion.replace(".", "_");
        }
        String newFilePath = currentProject.getLabel().toLowerCase() + SEG_TAG + jobLabel + SEG_TAG + SRC_RESOURCES_FOLDER
                + SEG_TAG
                + statePath;
        File targetFolder = new File(projectWorkspace + SEG_TAG + extResPath + SEG_TAG + newFilePath);
        if (targetFolder.exists() && targetFolder.isDirectory()) {
            for (File file : targetFolder.listFiles()) {
                if (file.isDirectory()) {
                    continue;
                }
                if (item.getName().equals(file.getName().substring(0, file.getName().lastIndexOf("_")))) {
                    file.delete();
                }
            }
        }
    }

}
