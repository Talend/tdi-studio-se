// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.runprocess;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import org.apache.log4j.Level;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.IPersistentPreferenceStore;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.osgi.framework.Bundle;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.utils.workbench.resources.ResourceUtils;
import org.talend.core.CorePlugin;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.IESBService;
import org.talend.core.ILibraryManagerService;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.language.ICodeProblemsChecker;
import org.talend.core.model.components.ComponentCategory;
import org.talend.core.model.general.ModuleNeeded;
import org.talend.core.model.general.Project;
import org.talend.core.model.general.TalendJobNature;
import org.talend.core.model.process.IContext;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.process.IProcess2;
import org.talend.core.model.process.JobInfo;
import org.talend.core.model.process.ProcessUtils;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.ProjectReference;
import org.talend.core.model.properties.Property;
import org.talend.core.model.properties.RoutineItem;
import org.talend.core.model.relationship.Relation;
import org.talend.core.model.relationship.RelationshipItemBuilder;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.runprocess.data.PerformanceData;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.repository.utils.Log4jUtil;
import org.talend.core.runtime.maven.MavenUrlHelper;
import org.talend.core.runtime.process.ITalendProcessJavaProject;
import org.talend.core.runtime.process.TalendProcessArgumentConstant;
import org.talend.core.runtime.process.TalendProcessOptionConstants;
import org.talend.core.runtime.projectsetting.ProjectPreferenceManager;
import org.talend.core.service.IESBMicroService;
import org.talend.core.service.IESBRouteService;
import org.talend.core.ui.ITestContainerProviderService;
import org.talend.designer.maven.model.TalendMavenConstants;
import org.talend.designer.maven.tools.AggregatorPomsHelper;
import org.talend.designer.maven.tools.BuildCacheManager;
import org.talend.designer.maven.tools.MavenPomSynchronizer;
import org.talend.designer.maven.tools.ProjectPomManager;
import org.talend.designer.maven.utils.PomIdsHelper;
import org.talend.designer.maven.utils.PomUtil;
import org.talend.designer.runprocess.i18n.Messages;
import org.talend.designer.runprocess.java.JavaProcessorUtilities;
import org.talend.designer.runprocess.java.TalendJavaProjectManager;
import org.talend.designer.runprocess.language.SyntaxCheckerFactory;
import org.talend.designer.runprocess.mapreduce.MapReduceJavaProcessor;
import org.talend.designer.runprocess.maven.MavenJavaProcessor;
import org.talend.designer.runprocess.prefs.RunProcessPrefsConstants;
import org.talend.designer.runprocess.spark.SparkJavaProcessor;
import org.talend.designer.runprocess.storm.StormJavaProcessor;
import org.talend.designer.runprocess.ui.views.ProcessView;
import org.talend.librariesmanager.model.ModulesNeededProvider;
import org.talend.repository.ProjectManager;
import org.talend.repository.RepositoryWorkUnit;
import org.talend.repository.constants.Log4jPrefsConstants;
import org.talend.repository.ui.utils.Log4jPrefsSettingManager;
import org.talend.utils.io.FilesUtils;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 * $Id: talend-code-templates.xml 1 2006-09-29 17:06:40Z nrousseau $
 *
 */
public class DefaultRunProcessService implements IRunProcessService {

    private static final String ROUTINE_FILENAME_EXT = ".pm"; //$NON-NLS-1$

    private static final String RESOURCE_FILE_PATH = "resources/"; //$NON-NLS-1$

    private static final String RESOURCE_LOG_FILE_PATH = "log/log4j.properties_template";

    private static final String RESOURCE_COMMONLOG_FILE_PATH = "log/common-logging.properties_template";

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.runprocess.IRunProcessFactory#getSyntaxChecker(org.talend.core.model.temp.ECodeLanguage)
     */
    @Override
    public ICodeProblemsChecker getSyntaxChecker(ECodeLanguage codeLanguage) {
        return SyntaxCheckerFactory.getInstance().getSyntaxChecker(codeLanguage);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.runprocess.IRunProcessFactory#setActiveProcess(org.talend.core.model.process.IProcess)
     */
    @Override
    public void setActiveProcess(IProcess2 activeProcess) {
        RunProcessPlugin.getDefault().getRunProcessContextManager().setActiveProcess(activeProcess);
    }

    @Override
    public void setActiveProcess(IProcess2 activeProcess, boolean refreshUI) {
        RunProcessPlugin.getDefault().getRunProcessContextManager().setActiveProcess(activeProcess, refreshUI);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.runprocess.IRunProcessService#getSelectedContext()
     */
    @Override
    public IContext getSelectedContext() {
        return RunProcessPlugin.getDefault().getRunProcessContextManager().getActiveContext().getSelectedContext();
    }

    /**
     * DOC qian Removes IProcess.
     *
     * @param activeProcess IProcess
     */
    @Override
    public void removeProcess(IProcess activeProcess) {
        RunProcessPlugin.getDefault().getRunProcessContextManager().removeProcess(activeProcess);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.runprocess.IRunProcessFactory#exec(java.lang.StringBuffer, java.lang.StringBuffer,
     * org.eclipse.core.runtime.IPath, java.lang.String, org.apache.log4j.Level, java.lang.String, int, int,
     * java.lang.String)
     */
    @Override
    public int perlExec(StringBuffer out, StringBuffer err, IPath absCodePath, String contextName, Level level,
            String perlInterpreterLibOption, String perlModuleDirectoryOption, int statOption, int traceOption,
            String... codeOptions) throws ProcessorException {
        int i = 0;
        return i;

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.talend.designer.runprocess.IRunProcessFactory#createCodeProcessor(org.talend.core.model.process.IProcess,
     * boolean)
     */
    @Override
    public IProcessor createCodeProcessor(IProcess process, Property property, ECodeLanguage language,
            boolean filenameFromLabel) {
        switch (language) {
        case JAVA:
            return createJavaProcessor(process, property, filenameFromLabel);
        default:
            return createJavaProcessor(process, property, filenameFromLabel);
        }
    }

    /**
     * DOC xue Comment method "createJavaProcessor".
     *
     * @param process
     * @param filenameFromLabel
     * @return
     */
    protected IProcessor createJavaProcessor(IProcess process, Property property, boolean filenameFromLabel) {
        boolean isTestContainer = false;
        if (GlobalServiceRegister.getDefault().isServiceRegistered(ITestContainerProviderService.class)) {
            ITestContainerProviderService testContainerService = (ITestContainerProviderService) GlobalServiceRegister
                    .getDefault().getService(ITestContainerProviderService.class);
            if (testContainerService != null && property != null && property.getItem() != null) {
                isTestContainer = testContainerService.isTestContainerItem(property.getItem());
            }
        }
        if (isTestContainer) {
            return new MavenJavaProcessor(process, property, filenameFromLabel);
        }
        // check for ESB Runtime
        if (GlobalServiceRegister.getDefault().isServiceRegistered(IESBRunContainerService.class)) {
            IESBRunContainerService runContainerService = (IESBRunContainerService) GlobalServiceRegister.getDefault()
                    .getService(IESBRunContainerService.class);
            if (runContainerService != null) {
                IProcessor processor = runContainerService.createJavaProcessor(process, property, filenameFromLabel);
                if (processor != null) {
                    return processor;
                }
            }
        }

        IESBMicroService microService = null;

        IESBRouteService routeService = null;

        IESBService soapService = null;

        if (GlobalServiceRegister.getDefault().isServiceRegistered(IESBMicroService.class)) {
            microService = (IESBMicroService) GlobalServiceRegister.getDefault().getService(IESBMicroService.class);

            if (ProcessorUtilities.isExportJobAsMicroService()) {
                if (microService != null) {
                    IProcessor processor = microService.createJavaProcessor(process, property, filenameFromLabel, false);
                    if (processor != null) {
                        return processor;
                    }
                }
            }
            if (property != null && property.getAdditionalProperties() != null
                    && "REST_MS".equals(property.getAdditionalProperties().get(TalendProcessArgumentConstant.ARG_BUILD_TYPE))) {
                if (microService != null) {
                    IProcessor processor = microService.createJavaProcessor(process, property, filenameFromLabel, false);
                    if (processor != null) {
                        return processor;
                    }
                }
            }
        }

        if (GlobalServiceRegister.getDefault().isServiceRegistered(IESBRouteService.class)) {
            routeService = (IESBRouteService) GlobalServiceRegister.getDefault().getService(IESBRouteService.class);
        }

        // Create maven processer for SOAP service, @see org.talend.designer.runprocess.java.TalendJavaProjectManager
        if (process == null && GlobalServiceRegister.getDefault().isServiceRegistered(IESBService.class)) {
            soapService = (IESBService) GlobalServiceRegister.getDefault().getService(IESBService.class);
            if (property.getItem() != null && soapService.isServiceItem(property.getItem().eClass().getClassifierID())) {
                return (IProcessor) soapService.createJavaProcessor(process, property, filenameFromLabel);
            }
        }

        if (ComponentCategory.CATEGORY_4_MAPREDUCE.getName().equals(process.getComponentsType())) {
            return new MapReduceJavaProcessor(process, property, filenameFromLabel);
        } else if (ComponentCategory.CATEGORY_4_SPARK.getName().equals(process.getComponentsType())) {
            return new SparkJavaProcessor(process, property, filenameFromLabel);
        } else if (ComponentCategory.CATEGORY_4_STORM.getName().equals(process.getComponentsType())) {
            return new StormJavaProcessor(process, property, filenameFromLabel);
        } else if (ComponentCategory.CATEGORY_4_SPARKSTREAMING.getName().equals(process.getComponentsType())) {
            return new SparkJavaProcessor(process, property, filenameFromLabel);
        } else if (ComponentCategory.CATEGORY_4_CAMEL.getName().equals(process.getComponentsType())) {
            if ("ROUTE_MICROSERVICE"
                    .equals(property.getAdditionalProperties().get(TalendProcessArgumentConstant.ARG_BUILD_TYPE))) {
                if (microService != null) {
                    IProcessor processor = microService.createJavaProcessor(process, property, filenameFromLabel, true);
                    if (processor != null) {
                        return processor;
                    }
                }
            } else {
                if (routeService != null) {
                    return routeService.createJavaProcessor(process, property, filenameFromLabel, false);
                }
            }

            return new MavenJavaProcessor(process, property, filenameFromLabel);
        } else {
            // If OSGI contains new processor, need to add built type in args map

            if (property != null) {
                boolean servicePart = false;
                List<Relation> relations = RelationshipItemBuilder.getInstance().getItemsRelatedTo(property.getId(),
                        property.getVersion(), RelationshipItemBuilder.JOB_RELATION);

                for (Relation relation : relations) {
                    if (RelationshipItemBuilder.SERVICES_RELATION.equals(relation.getType())) {
                        servicePart = true;
                        break;
                    }
                }
                if ("OSGI".equals(property.getAdditionalProperties().get(TalendProcessArgumentConstant.ARG_BUILD_TYPE))
                        || servicePart) {
                    if (GlobalServiceRegister.getDefault().isServiceRegistered(IESBService.class)) {
                        soapService = (IESBService) GlobalServiceRegister.getDefault().getService(IESBService.class);
                        return soapService.createOSGIJavaProcessor(process, property, filenameFromLabel);
                    }
                }

                return new MavenJavaProcessor(process, property, filenameFromLabel);

            } else {
                return new MavenJavaProcessor(process, property, filenameFromLabel);
            }

        }
    }

    @Override
    public IPerformanceData createPerformanceData(String data) {
        return new PerformanceData(data);
    }

    @Override
    public String getRoutineFilenameExt() {
        return ROUTINE_FILENAME_EXT;
    }

    @Override
    public IProject getProject(ECodeLanguage language) throws CoreException {
        ITalendProcessJavaProject talendJavaProject = JavaProcessorUtilities.getTalendJavaProject();
        if (talendJavaProject != null) {
            return talendJavaProject.getProject();
        }
        return null;
    }

    @Override
    public IJavaProject getJavaProject() throws CoreException {
        ITalendProcessJavaProject talendJavaProject = JavaProcessorUtilities.getTalendJavaProject();
        if (talendJavaProject != null) {
            return talendJavaProject.getJavaProject();
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @seeorg.talend.designer.runprocess.IRunProcessService#setDelegateService(org.talend.designer.runprocess.
     * IRunProcessService)
     */
    @Override
    public void setDelegateService(IRunProcessService delegateService) {
        throw new UnsupportedOperationException(Messages.getString("DefaultRunProcessService.methodCalledError")); //$NON-NLS-1$
    }

    @Override
    public void updateLibraries(Set<ModuleNeeded> jobModuleList, IProcess process) {
        JavaProcessorUtilities.computeLibrariesPath(new HashSet<ModuleNeeded>(jobModuleList), process);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.talend.designer.runprocess.IRunProcessService#updateLibraries(org.talend.core.model.properties.RoutineItem)
     */
    @Override
    public void updateLibraries(RoutineItem routineItem) {
        Set<ModuleNeeded> modulesForRoutine = ModulesNeededProvider.updateModulesNeededForRoutine(routineItem);
        File libDir = getJavaProjectLibFolder().getLocation().toFile();
        if (libDir == null) {
            return;
        }
        ILibraryManagerService repositoryBundleService = CorePlugin.getDefault().getRepositoryBundleService();
        repositoryBundleService.retrieve(modulesForRoutine, libDir.getAbsolutePath(), true);
        repositoryBundleService.installModules(modulesForRoutine, null);
        CorePlugin.getDefault().getLibrariesService().checkLibraries();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.runprocess.IRunProcessService#updateLibraries(java.util.Set,
     * org.talend.core.model.process.IProcess, java.util.Set)
     */
    @Override
    public void updateLibraries(Set<ModuleNeeded> jobModuleList, IProcess process, Set<ModuleNeeded> alreadyRetrievedModules)
            throws ProcessorException {
        JavaProcessorUtilities.computeLibrariesPath(new HashSet<ModuleNeeded>(jobModuleList), process, alreadyRetrievedModules);
    }

    @Override
    public void refreshView() {
        ProcessView view = ProcessView.findProcessView();
        if (view != null) {
            view.refresh();
        }
    }

    @Override
    public void switchToCurProcessView() {
        ProcessView view = ProcessView.findProcessView();
        if (view != null) {
            view.refresh(true);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.runprocess.IRunProcessService#getPauseTime()
     */
    @Override
    public int getPauseTime() {
        return RunProcessPlugin.getDefault().getPreferenceStore().getInt(RunProcessPrefsConstants.STRACESTIME);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.runprocess.IRunProcessService#needDeleteAllJobs()
     */
    @Override
    public boolean needDeleteAllJobs() {
        return !DeleteAllJobWhenStartUp.executed;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.runprocess.IRunProcessService#deleteAllJobx(boolean)
     */
    @Override
    public void deleteAllJobs(boolean fromPluginModel) {
        new DeleteAllJobWhenStartUp().startup(fromPluginModel);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.runprocess.IRunProcessService#getRunProcessAction()
     */
    @Override
    public IAction getRunProcessAction() {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.runprocess.IRunProcessService#enableTraceForActiveRunProcess()
     */
    @Override
    public boolean enableTraceForActiveRunProcess() {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.runprocess.IRunProcessService#saveJobBeforeRun(org.talend.core.model.process.IProcess)
     */
    @Override
    public void saveJobBeforeRun(IProcess activeProcess) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.runprocess.IRunProcessService#getPreferenceStore()
     */
    @Override
    public IPreferenceStore getPreferenceStore() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IProcess getActiveProcess() {
        if (RunProcessPlugin.getDefault().getRunProcessContextManager().getActiveContext() == null) {
            return null;
        }

        return RunProcessPlugin.getDefault().getRunProcessContextManager().getActiveContext().getProcess();
    }

    @Override
    public boolean checkExportProcess(IStructuredSelection selection, boolean isJob) {
        return JobErrorsChecker.checkExportErrors(selection, isJob);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.runprocess.IRunProcessService#checkLastGenerationHasCompilationError(boolean)
     */
    @Override
    public void checkLastGenerationHasCompilationError(boolean updateProblemsView) throws ProcessorException {
        JobErrorsChecker.checkLastGenerationHasCompilationError(updateProblemsView);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.runprocess.IRunProcessService#getResourceFile(java.lang.String)
     */
    @Override
    public String getResourceFilePath(String filePath) {
        Bundle b = Platform.getBundle(RunProcessPlugin.PLUGIN_ID);
        URL url = null;
        try {
            url = FileLocator.toFileURL(FileLocator.find(b, new Path(RESOURCE_FILE_PATH + filePath), null));
        } catch (IOException e) {
            ExceptionHandler.process(e);
        }

        if (url != null) {
            return url.getFile();
        } else {
            return null;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.runprocess.IRunProcessService#getTemplateStrFromPreferenceStore(java.lang.String)
     */
    @Override
    public String getTemplateStrFromPreferenceStore(String nodeType) {
        return Log4jPrefsSettingManager.getInstance().getValueOfPreNode(nodeType);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.runprocess.IRunProcessService#updateLogFiles(org.eclipse.core.resources.IProject)
     */
    @Override
    public void updateLogFiles(ITalendProcessJavaProject talendJavaProject, boolean isLogForJob) {
        // create the .prefs file and save log4j.xml and common-logging.properties's content into it
        if (!Log4jPrefsSettingManager.getInstance().isLog4jPrefsExist()) {
            Log4jPrefsSettingManager.getInstance().createTalendLog4jPrefs(Log4jPrefsConstants.LOG4J_ENABLE_NODE,
                    Log4jUtil.isEnable() ? Boolean.TRUE.toString() : Boolean.FALSE.toString());
            Log4jPrefsSettingManager.getInstance().createTalendLog4jPrefs(Log4jPrefsConstants.LOG4J_CONTENT_NODE,
                    getLogTemplate(RESOURCE_LOG_FILE_PATH));
            Log4jPrefsSettingManager.getInstance().createTalendLog4jPrefs(Log4jPrefsConstants.COMMON_LOGGING_NODE,
                    getLogTemplate(RESOURCE_COMMONLOG_FILE_PATH));
        }
        // if directly init or modify log4j,need handle with the log4j under .setting/,if not,means execute or export
        // job,need to copy the latest log4j from .setting/ to /java/src
        if (talendJavaProject == null) {
            return;
        }
        try {
            // get the .setting folder where we need to keep the log4j file
            // IFolder prefSettingFolder = ResourceUtils.getFolder(
            // ResourceModelHelper.getProject(ProjectManager.getInstance().getCurrentProject()), ".settings", false);

            IFolder resFolder = talendJavaProject.getExternalResourcesFolder();
            IFile log4jFile = resFolder.getFile(Log4jPrefsConstants.LOG4J_FILE_NAME);
            // TUP-3014, update the log4j in .Java always.
            // if (isLogForJob) { // when execute or export job need the log4j files under .src folder
            String log4jStr = getTemplateStrFromPreferenceStore(Log4jPrefsConstants.LOG4J_CONTENT_NODE);
            if (log4jStr != null) {
                File ljFile = new File(log4jFile.getLocation().toOSString());
                FileOutputStream ljFileOutputStream = null;
                try {
                    ljFile.getParentFile().mkdirs();
                    ljFileOutputStream = new FileOutputStream(ljFile);
                    ljFileOutputStream.write(log4jStr.getBytes());
                } finally {
                    if (ljFileOutputStream != null) {
                        ljFileOutputStream.close();
                    }
                }
                resFolder.refreshLocal(IResource.DEPTH_ONE, null);
            }
            // }
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }
    }

    @Override
    public String getLogTemplate(String path) {
        IRunProcessService service = null;
        if (GlobalServiceRegister.getDefault().isServiceRegistered(IRunProcessService.class)) {
            service = (IRunProcessService) GlobalServiceRegister.getDefault().getService(IRunProcessService.class);
        }
        if (service == null) {
            return "";
        }

        File templateFile = new File(service.getResourceFilePath(path));
        if (!templateFile.exists()) {
            return "";
        }

        return getLogTemplateString(templateFile);
    }

    private String getLogTemplateString(File templateScriptFile) {
        if (templateScriptFile != null && templateScriptFile.exists()) {
            try {
                return new Scanner(templateScriptFile).useDelimiter("\\A").next(); //$NON-NLS-1$
            } catch (FileNotFoundException e) {
                ExceptionHandler.process(e);
            }
        }
        return "";
    }

    @Override
    public boolean isJobRunning() {
        final RunProcessContext activeContext = RunProcessPlugin.getDefault().getRunProcessContextManager().getActiveContext();

        if (activeContext == null) {
            return false;
        }

        return activeContext.isRunning();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.runprocess.IRunProcessService#buildJavaProject()
     */
    @Override
    public void buildJavaProject() {
        //
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.runprocess.IRunProcessService#getTalendProcessJavaProject()
     */
    @Override
    public ITalendProcessJavaProject getTalendProcessJavaProject() {
        return null;
    }

    @Override
    public ProjectPreferenceManager getProjectPreferenceManager() {
        return RunProcessPlugin.getDefault().getProjectPreferenceManager();
    }

    @Override
    public Set<String> getLibJarsForBD(IProcess process) {
        if (process instanceof IProcess2) {
            return JavaProcessorUtilities.extractLibNamesOnlyForMapperAndReducer((IProcess2) process);
        }
        return new HashSet<>();
    }

    @Override
    public IFolder getJavaProjectLibFolder() {
        return JavaProcessorUtilities.getJavaProjectLibFolder2();
    }

    @Override
    public void updateProjectPomWithTemplate() {
        try {
            ProjectPomManager manager = new ProjectPomManager();
            manager.updateFromTemplate(null);
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }
    }

    @Override
    public void storeProjectPreferences(IPreferenceStore preferenceStore) {
        RepositoryWorkUnit workUnit = new RepositoryWorkUnit("Store project preferences") { //$NON-NLS-1$

            @Override
            protected void run() {
                try {
                    if (preferenceStore instanceof IPersistentPreferenceStore) {
                        ((IPersistentPreferenceStore) preferenceStore).save();
                    }
                } catch (IOException e) {
                    ExceptionHandler.process(e);
                }
            }
        };
        workUnit.setAvoidUnloadResources(true);
        ProxyRepositoryFactory.getInstance().executeRepositoryWorkUnit(workUnit);
    }

    @Override
    public void initMavenJavaProject(IProgressMonitor monitor, Project project) {
        TalendJavaProjectManager.initJavaProjects(monitor, project);
    }

    @Override
    public ITalendProcessJavaProject getTalendCodeJavaProject(ERepositoryObjectType type) {
        return TalendJavaProjectManager.getTalendCodeJavaProject(type);
    }

    @Override
    public ITalendProcessJavaProject getTalendCodeJavaProject(ERepositoryObjectType type, String projectTechName) {
        ITalendProcessJavaProject codeProject = TalendJavaProjectManager.getTalendCodeJavaProject(type, projectTechName);
        if (codeProject == null) {
            // try to recover from any damage of pom.
            try {
                AggregatorPomsHelper helper = new AggregatorPomsHelper(projectTechName);
                IFile pomFile = helper.getCodeFolder(type).getFile(TalendMavenConstants.POM_FILE_NAME);
                helper.updateCodeProjectPom(new NullProgressMonitor(), type, pomFile);
                codeProject = TalendJavaProjectManager.getTalendCodeJavaProject(type, projectTechName);
            } catch (Exception e) {
                ExceptionHandler.process(e);
            }
        }
        return codeProject;
    }

    @Override
    public ITalendProcessJavaProject getTalendJobJavaProject(Property property) {
        return TalendJavaProjectManager.getTalendJobJavaProject(property);
    }

    @Override
    public IFolder getCodeSrcFolder(ERepositoryObjectType type, String projectTechName) {
        return new AggregatorPomsHelper(projectTechName).getCodeSrcFolder(type);
    }

    @Override
    public ITalendProcessJavaProject getTempJavaProject() {
        return TalendJavaProjectManager.getTempJavaProject();
    }

    @Override
    public void clearProjectRelatedSettings() {
        try {
            PomIdsHelper.resetPreferencesManagers();
            MavenPomSynchronizer.removeChangeLibrariesListener();
            TalendJavaProjectManager.deleteEclipseProjectByNatureId(TalendJobNature.ID);
            BuildCacheManager.getInstance().clearAllCaches();
        } catch (CoreException e) {
            ExceptionHandler.process(e);
        }
    }

    @Override
    public boolean isExportConfig() {
        return ProcessorUtilities.isExportConfig();
    }

    public boolean isdebug() {
        return ProcessorUtilities.isdebug();
    }

    @Override
    public void buildCodesJavaProject(IProgressMonitor monitor) {
        try {
            AggregatorPomsHelper.buildAndInstallCodesProject(monitor, ERepositoryObjectType.ROUTINES);
            if (ProcessUtils.isRequiredPigUDFs(null)) {
                AggregatorPomsHelper.buildAndInstallCodesProject(monitor, ERepositoryObjectType.PIG_UDF);
            }
            if (ProcessUtils.isRequiredBeans(null)) {
                AggregatorPomsHelper.buildAndInstallCodesProject(monitor, ERepositoryObjectType.valueOf("BEANS")); //$NON-NLS-1$
            }
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }
    }

    @Override
    public void generatePom(Item item) {
        TalendJavaProjectManager.generatePom(item, TalendProcessOptionConstants.GENERATE_NO_CODEGEN);
    }

    @Override
    public void generatePom(Item item, int option) {
        TalendJavaProjectManager.generatePom(item, option);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.runprocess.IRunProcessService#initializeRootPoms()
     */
    @Override
    public void initializeRootPoms(IProgressMonitor monitor) {
        try {
            AggregatorPomsHelper helper = new AggregatorPomsHelper();
            helper.installRootPom(false);
            AggregatorPomsHelper.updateAllCodesProjectNeededModules(monitor);
            List<ProjectReference> references = ProjectManager.getInstance().getCurrentProject().getProjectReferenceList(true);
            for (ProjectReference ref : references) {
                initRefPoms(new Project(ref.getReferencedProject()));
            }
            helper.updateRefProjectModules(references, monitor);
            helper.updateCodeProjects(monitor, true);
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }
    }

    private void initRefPoms(Project refProject) throws Exception {
        for (ProjectReference ref : refProject.getProjectReferenceList(true)) {
            initRefPoms(new Project(ref.getReferencedProject()));
        }
        AggregatorPomsHelper refHelper = new AggregatorPomsHelper(refProject.getTechnicalLabel());

        // install ref project pom.
        refHelper.installRootPom(true);

        // install ref codes project.
        IProgressMonitor monitor = new NullProgressMonitor();
        installRefCodeProject(ERepositoryObjectType.ROUTINES, refHelper, monitor);

        if (ProcessUtils.isRequiredPigUDFs(null, refProject)) {
            installRefCodeProject(ERepositoryObjectType.PIG_UDF, refHelper, monitor);
        }

        if (ProcessUtils.isRequiredBeans(null, refProject)) {
            installRefCodeProject(ERepositoryObjectType.valueOf("BEANS"), refHelper, monitor); //$NON-NLS-1$
        }
        
        deleteRefProjects(refProject, refHelper);

    }
    
    private void installRefCodeProject(ERepositoryObjectType codeType, AggregatorPomsHelper refHelper, IProgressMonitor monitor)
            throws Exception, CoreException {
        if (!refHelper.getProjectRootPom().exists()) {
            return;
        }
        
        String projectTechName = refHelper.getProjectTechName();
        ITalendProcessJavaProject codeProject = TalendJavaProjectManager.getExistingTalendCodeProject(codeType, projectTechName);
        if (codeProject != null) {
            codeProject.buildWholeCodeProject();
            Map<String, Object> argumentsMap = new HashMap<>();
            argumentsMap.put(TalendProcessArgumentConstant.ARG_GOAL, TalendMavenConstants.GOAL_INSTALL);
            argumentsMap.put(TalendProcessArgumentConstant.ARG_PROGRAM_ARGUMENTS, TalendMavenConstants.ARG_MAIN_SKIP);
            codeProject.buildModules(monitor, null, argumentsMap);
        }
    }
    
    private void deleteRefProjects(Project refProject, AggregatorPomsHelper refHelper) throws Exception {
        IProgressMonitor monitor = new NullProgressMonitor();
        
        deleteRefProject(ERepositoryObjectType.ROUTINES, refHelper, monitor);
        
        if (ProcessUtils.isRequiredPigUDFs(null, refProject)) {
        	deleteRefProject(ERepositoryObjectType.PIG_UDF, refHelper, monitor);
        }

        if (ProcessUtils.isRequiredBeans(null, refProject)) {
        	deleteRefProject(ERepositoryObjectType.valueOf("BEANS"), refHelper, monitor); //$NON-NLS-1$
        }

    }
    
    private void deleteRefProject(ERepositoryObjectType codeType, AggregatorPomsHelper refHelper, IProgressMonitor monitor)
            throws Exception, CoreException {
    	
        if (!refHelper.getProjectRootPom().exists()) {
            return;
        }
        
        String projectTechName = refHelper.getProjectTechName();
        ITalendProcessJavaProject codeProject = TalendJavaProjectManager.getExistingTalendCodeProject(codeType, projectTechName);
       	
        if (codeProject != null) {
            codeProject.getProject().delete(false, true, monitor);
            TalendJavaProjectManager.removeFromCodeJavaProjects(codeType, projectTechName);
        }
    }

    @Override
    public boolean isGeneratePomOnly() {
        return ProcessorUtilities.isGeneratePomOnly();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.talend.designer.runprocess.IRunProcessService#handleJobDependencyLoop(org.talend.core.model.process.JobInfo,
     * java.util.List, org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public void handleJobDependencyLoop(JobInfo mainJobInfo, List<JobInfo> listJobs, IProgressMonitor progressMonitor)
            throws Exception {
        IProject mainProject = mainJobInfo.getCodeFile().getProject();

        ProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
        Set<String> childJobDependencies = new HashSet<String>();
        List<IFile> childPoms = new ArrayList<IFile>();
        for (JobInfo info : listJobs) {
            IRepositoryViewObject specificVersion = factory.getSpecificVersion(info.getJobId(), info.getJobVersion(), true);
            Property property = specificVersion.getProperty();
            String groupId = PomIdsHelper.getJobGroupId(property);
            String artifactId = PomIdsHelper.getJobArtifactId(property);
            String version = PomIdsHelper.getJobVersion(property);
            String generateMvnUrl = MavenUrlHelper.generateMvnUrl(groupId, artifactId, version, "jar", null);
            childJobDependencies.add(generateMvnUrl);
            if (info.equals(mainJobInfo)) {
                continue;
            }
            childPoms.add(info.getPomFile());

            // copy source code to the main project
            IFile codeFile = info.getCodeFile();
            IPath refPath = codeFile.getProjectRelativePath();
            IFolder targetFolder = mainProject.getFolder(refPath.removeLastSegments(1));
            if (!targetFolder.exists()) {
                ResourceUtils.createFolder(targetFolder);
            }
            FilesUtils.copyDirectory(new File(codeFile.getLocation().toPortableString()),
                    new File(targetFolder.getLocation().toPortableString()));
        }

        PomUtil.updateMainJobDependencies(mainJobInfo.getPomFile(), childPoms, childJobDependencies, progressMonitor);
    }

}
