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
package org.talend.repository.ui.wizards.exportjob.scriptsmanager;

import java.util.List;
import java.util.Map;

import org.talend.core.model.process.IContext;
import org.talend.core.model.properties.Item;
import org.talend.designer.runprocess.ProcessorException;
import org.talend.repository.documentation.ExportFileResource;

/**
 * Manages the job scripts to be exported. <br/>
 *
 * $Id: JobScriptsManager.java 1 2006-12-14 下午05:06:49 ftang
 *
 */
public class JobPerlScriptsManager extends JobScriptsManager {

    public JobPerlScriptsManager(Map<ExportChoice, Object> exportChoiceMap, String contextName, String launcher,
            int statisticPort, int tracePort) {
        super(exportChoiceMap, contextName, launcher, statisticPort, tracePort);
    }

    private static final String SYSTEM_ROUTINES_FOLDER_NAME = "system"; //$NON-NLS-1$

    @Override
    public List<ExportFileResource> getExportResources(ExportFileResource[] process, IContext context, String... codeOptions)
            throws ProcessorException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<ExportFileResource> getExportResources(ExportFileResource[] process, String... codeOptions)
            throws ProcessorException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected String getCorrespondingProjectName(Item item) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Gets the export resources.
     *
     * @param process
     * @param needLauncher
     * @param needSystemRoutine
     * @param needUserRoutine
     * @param needModule
     * @param needJob
     * @param needContext
     * @return
     */
    // @Override
    // public List<ExportFileResource> getExportResources(ExportFileResource[] process, Map<ExportChoice, Object>
    // exportChoice,
    // IContext context, String launcher, int statisticPort, int tracePort, String... codeOptions) throws
    // ProcessorException {
    //
    //        ProcessorUtilities.setExportConfig("perl", "", LIBRARY_FOLDER_NAME); //$NON-NLS-1$ //$NON-NLS-2$
    //
    // for (int i = 0; i < process.length; i++) {
    // ProcessItem processItem = (ProcessItem) process[i].getItem();
    // String selectedJobVersion = getSelectedJobVersion();
    // selectedJobVersion = preExportResource(process, i, selectedJobVersion);
    // IProcess jobProcess = null;
    // if (!isOptionChoosed(exportChoice, ExportChoice.doNotCompileCode)) {
    // jobProcess = generateJobFiles(processItem, context, selectedJobVersion,
    // statisticPort != IProcessor.NO_STATISTICS, statisticPort != IProcessor.NO_TRACES, isOptionChoosed(
    // exportChoice, ExportChoice.applyToChildren), progressMonitor);
    // }
    // List<URL> resources = new ArrayList<URL>();
    // String contextName = context.getName();
    // if (contextName != null) {
    // boolean needChildren = posExportResource(process, exportChoice, contextName, launcher, statisticPort, tracePort,
    // i, jobProcess, processItem, selectedJobVersion, resources, codeOptions);
    // addChildrenResources(process, processItem, needChildren, process[i], exportChoice, contextName,
    // selectedJobVersion);
    // }
    // process[i].addResources(resources);
    // }
    // return Arrays.asList(process);
    // }
    //
    // /**
    // * Gets the export resources.
    // *
    // * @param process
    // * @param needLauncher
    // * @param needSystemRoutine
    // * @param needUserRoutine
    // * @param needModule
    // * @param needJob
    // * @param needContext
    // * @return
    // */
    // @Override
    // public List<ExportFileResource> getExportResources(ExportFileResource[] process, Map<ExportChoice, Object>
    // exportChoice,
    // String contextName, String launcher, int statisticPort, int tracePort, String... codeOptions)
    // throws ProcessorException {
    //
    //        ProcessorUtilities.setExportConfig("perl", "", LIBRARY_FOLDER_NAME); //$NON-NLS-1$ //$NON-NLS-2$
    //
    // for (int i = 0; i < process.length; i++) {
    // ProcessItem processItem = (ProcessItem) process[i].getItem();
    // String selectedJobVersion = getSelectedJobVersion();
    // selectedJobVersion = preExportResource(process, i, selectedJobVersion);
    // IProcess jobProcess = null;
    // if (!isOptionChoosed(exportChoice, ExportChoice.doNotCompileCode)) {
    // jobProcess = generateJobFiles(processItem, contextName, selectedJobVersion,
    // statisticPort != IProcessor.NO_STATISTICS, statisticPort != IProcessor.NO_TRACES, isOptionChoosed(
    // exportChoice, ExportChoice.applyToChildren), progressMonitor);
    // }
    // List<URL> resources = new ArrayList<URL>();
    // boolean needChildren = posExportResource(process, exportChoice, contextName, launcher, statisticPort, tracePort,
    // i,
    // jobProcess, processItem, selectedJobVersion, resources, codeOptions);
    // addChildrenResources(process, processItem, needChildren, process[i], exportChoice, contextName,
    // selectedJobVersion);
    // process[i].addResources(resources);
    // }
    // return Arrays.asList(process);
    // }
    //
    // /**
    // * DOC informix Comment method "posExportResource".
    // *
    // * @param process
    // * @param exportChoice
    // * @param contextName
    // * @param launcher
    // * @param statisticPort
    // * @param tracePort
    // * @param i
    // * @param processItem
    // * @param selectedJobVersion
    // * @param resources
    // * @param codeOptions
    // * @return
    // */
    // private boolean posExportResource(ExportFileResource[] process, Map<ExportChoice, Object> exportChoice, String
    // contextName,
    // String launcher, int statisticPort, int tracePort, int i, IProcess jobProcess, ProcessItem processItem,
    // String selectedJobVersion, List<URL> resources, String... codeOptions) {
    // resources.addAll(getLauncher(isOptionChoosed(exportChoice, ExportChoice.needLauncher),
    // isOptionChoosed(exportChoice,
    // ExportChoice.setParameterValues), isOptionChoosed(exportChoice, ExportChoice.needContext), jobProcess,
    // processItem, escapeSpace(contextName), escapeSpace(launcher), statisticPort, tracePort, codeOptions));
    //
    // // Gets system routines.
    // List<URL> systemRoutineList = getSystemRoutine(isOptionChoosed(exportChoice, ExportChoice.needSystemRoutine));
    // if (systemRoutineList.size() > 0) {
    // process[i].addResources(LIBRARY_FOLDER_NAME + PATH_SEPARATOR + ILibrariesService.SOURCE_PERL_ROUTINES_FOLDER
    // + PATH_SEPARATOR + SYSTEM_ROUTINES_FOLDER_NAME, systemRoutineList);
    // }
    // // Gets user routines.
    // String projectName = getCorrespondingProjectName(processItem);
    // try {
    // List<URL> userRoutineList = getUserRoutine(projectName, isOptionChoosed(exportChoice,
    // ExportChoice.needUserRoutine));
    // if (userRoutineList.size() > 0) {
    // process[i].addResources(LIBRARY_FOLDER_NAME + PATH_SEPARATOR + ILibrariesService.SOURCE_PERL_ROUTINES_FOLDER
    // + PATH_SEPARATOR + projectName, userRoutineList);
    // }
    // } catch (MalformedURLException e) {
    // ExceptionHandler.process(e);
    // }
    //
    // addJobItem(process, processItem, isOptionChoosed(exportChoice, ExportChoice.needJobItem), process[i],
    // selectedJobVersion);
    // List<URL> talendLibraries = getTalendLibraries(isOptionChoosed(exportChoice, ExportChoice.needTalendLibraries));
    // if (talendLibraries.size() > 0) {
    //            process[i].addResources(LIBRARY_FOLDER_NAME + PATH_SEPARATOR + "talend", talendLibraries); //$NON-NLS-1$
    // }
    // resources.addAll(getJobScripts(processItem, isOptionChoosed(exportChoice, ExportChoice.needJobScript)));
    // addDependencies(process, processItem, isOptionChoosed(exportChoice, ExportChoice.needDependencies), process[i]);
    // resources.addAll(getContextScripts(processItem, isOptionChoosed(exportChoice, ExportChoice.needContext)));
    // boolean needChildren = isOptionChoosed(exportChoice, ExportChoice.needJobScript)
    // && isOptionChoosed(exportChoice, ExportChoice.needContext);
    // return needChildren;
    // }
    //
    // /**
    // * DOC informix Comment method "preExportResource".
    // *
    // * @param process
    // * @param i
    // * @param selectedJobVersion
    // * @return
    // */
    // private String preExportResource(ExportFileResource[] process, int i, String selectedJobVersion) {
    // if (selectedJobVersion == null) {
    // selectedJobVersion = process[i].getItem().getProperty().getVersion();
    // }
    // if (progressMonitor != null) {
    // progressMonitor
    //                    .subTask(Messages.getString("JobPerlScriptsManager.exportJob") + process[i].getNode().getObject().getLabel() + selectedJobVersion); //$NON-NLS-1$
    // }
    // return selectedJobVersion;
    // }
    //
    // /**
    // * Gets user routine.
    // *
    // * @param needUserRoutine
    // * @return
    // * @throws MalformedURLException
    // */
    // private List<URL> getUserRoutine(String projectName, boolean needUserRoutine) throws MalformedURLException {
    // List<URL> list = new ArrayList<URL>();
    // if (!needUserRoutine) {
    // return list;
    // }
    // ILibrariesService librariesService = CorePlugin.getDefault().getLibrariesService();
    // String folderPath = librariesService.getLibrariesPath() + PATH_SEPARATOR +
    // ILibrariesService.SOURCE_PERL_ROUTINES_FOLDER
    // + PATH_SEPARATOR + projectName;
    // File file = new File(folderPath);
    // File[] files = file.listFiles();
    // if (files != null) {
    // for (int i = 0; i < files.length; i++) {
    // File tempFile = files[i];
    // list.add(tempFile.toURL());
    // }
    // }
    //
    // return list;
    // }
    //
    // /**
    // * Gets system routine.
    // *
    // * @param needSystemRoutine
    // * @return
    // */
    // private List<URL> getSystemRoutine(boolean needSystemRoutine) {
    // List<URL> list = new ArrayList<URL>();
    // if (!needSystemRoutine) {
    // return list;
    // }
    // ILibrariesService librariesService = CorePlugin.getDefault().getLibrariesService();
    // String path = librariesService.getLibrariesPath() + PATH_SEPARATOR +
    // ILibrariesService.SOURCE_PERL_ROUTINES_FOLDER
    // + PATH_SEPARATOR + SYSTEM_ROUTINES_FOLDER_NAME;
    // File file = new File(path);
    // File[] files = file.listFiles();
    // for (int i = 0; i < files.length; i++) {
    // File tempFile = files[i];
    // try {
    // list.add(tempFile.toURL());
    // } catch (MalformedURLException e) {
    // ExceptionHandler.process(e);
    // }
    // }
    // return list;
    // }
    //
    // private void addComponentModules(ProcessItem processItem, ExportFileResource resource) {
    // EList nList = processItem.getProcess().getNode();
    // Set set = new HashSet(nList);
    // for (Iterator iter = set.iterator(); iter.hasNext();) {
    // NodeType nType = (NodeType) iter.next();
    // String componentName = nType.getComponentName();
    // IComponent component = ComponentsFactoryProvider.getInstance().get(componentName);
    // List<URL> modules = getComponentModules(componentName);
    // resource.addResources(LIBRARY_FOLDER_NAME + PATH_SEPARATOR + componentName, modules);
    //
    // // get the modules that this component depends on.
    // for (ModuleNeeded module : component.getModulesNeeded()) {
    // // for intance, split the "DtMysqlOutput::Mysql" to {"DtMysqlOutput","Mysql"}
    //                String[] string = module.getModuleName().split("::"); //$NON-NLS-1$
    // if (string.length != 2) {
    // continue;
    // }
    //                resource.addResources(LIBRARY_FOLDER_NAME + PATH_SEPARATOR + string[0], getComponentModules(string[0] + "/" //$NON-NLS-1$
    //                        + string[1] + ".pm")); //$NON-NLS-1$
    // }
    // }
    // }
    //
    // private List<URL> getComponentModules(String componentName) {
    // List<URL> modules = new ArrayList<URL>();
    // ILibrariesService librariesService = CorePlugin.getDefault().getLibrariesService();
    // String path = librariesService.getLibrariesPath() + PATH_SEPARATOR + componentName;
    // File file = new File(path);
    // if (!file.exists()) {
    // return modules;
    // }
    //
    // if (file.isFile()) {
    // try {
    // modules.add(file.toURL());
    // } catch (MalformedURLException e) {
    // ExceptionHandler.process(e);
    // }
    // return modules;
    // }
    //
    // File[] files = file.listFiles();
    // for (int i = 0; i < files.length; i++) {
    // File tempFile = files[i];
    // try {
    // modules.add(tempFile.toURL());
    // } catch (MalformedURLException e) {
    // ExceptionHandler.process(e);
    // }
    // }
    // return modules;
    // }
    //
    // IResource[] resouces = null;
    //
    // /**
    // * Gets all the perl files in the project .Perl.
    // *
    // * @param refresh If it is true, reload files from project.
    // * @return
    // */
    // private IResource[] getAllPerlFiles(boolean refresh) {
    // if (resouces == null || refresh) {
    // try {
    // IProject perlProject = RepositoryPlugin.getDefault().getRunProcessService().getProject(ECodeLanguage.PERL);
    // resouces = perlProject.members();
    // } catch (Exception e) {
    // ExceptionHandler.process(e);
    // resouces = new IResource[0];
    // }
    // }
    // return resouces;
    // }
    //
    // private IResource[] getAllPerlFiles() {
    // return getAllPerlFiles(false);
    // }
    //
    // /**
    // * Gets required perl libraries.
    // *
    // * @param needModel
    // * @return
    // */
    // private List<URL> getTalendLibraries(boolean needLibraries) {
    // List<URL> libraries = new ArrayList<URL>();
    // if (needLibraries) {
    // try {
    // ILibrariesService service = CorePlugin.getDefault().getLibrariesService();
    // libraries = service.getTalendRoutines();
    // } catch (Exception e) {
    // ExceptionHandler.process(e);
    // }
    // }
    // return libraries;
    // }
    //
    // /**
    // * Gets Job Scripts.
    // *
    // * @param process
    // * @param needJob
    // * @return
    // */
    // private List<URL> getJobScripts(ProcessItem process, boolean needJob) {
    // List<String> list = new ArrayList<String>();
    // if (needJob) {
    // try {
    // String version = process.getProperty().getVersion();
    // if (!isMultiNodes() && getSelectedJobVersion() != null) {
    // version = getSelectedJobVersion();
    // }
    // String rootProjectName = PerlResourcesHelper.getRootProjectName(process);
    // String fileName = PerlResourcesHelper.getJobFileName(rootProjectName, process.getProperty().getLabel(), version);
    // list.add(fileName);
    // } catch (Exception e) {
    // ExceptionHandler.process(e);
    // }
    // }
    // IResource[] resources = this.getAllPerlFiles(true);
    // return getResourcesURL(resources, list);
    // }
    //
    // /*
    // * (non-Javadoc)
    // *
    // * @see org.talend.repository.ui.wizards.exportjob.scriptsmanager.JobScriptsManager#getCurrentProjectName()
    // */
    // @Override
    // protected String getCorrespondingProjectName(Item item) {
    // return PerlResourcesHelper.getRootProjectName(item);
    // }
    //
    // private void addChildrenResources(ExportFileResource[] allResources, ProcessItem process, boolean needChildren,
    // ExportFileResource curResource, Map<ExportChoice, Object> exportChoice, String contextName,
    // String... selectedJobVersion) {
    // List<String> list = new ArrayList<String>();
    // if (needChildren) {
    // String projectName = getCorrespondingProjectName(process);
    // try {
    // List<ProcessItem> processedJob = new ArrayList<ProcessItem>();
    // getChildrenJobAndContextName(allResources, process.getProperty().getLabel(), list, process, projectName,
    // processedJob, curResource, exportChoice, contextName, selectedJobVersion);
    // } catch (Exception e) {
    // ExceptionHandler.process(e);
    // }
    //
    // }
    //
    // IResource[] resources = this.getAllPerlFiles();
    // curResource.addResources(getResourcesURL(resources, list));
    // }
    //
    // private void getChildrenJobAndContextName(ExportFileResource[] allResources, String rootName, List<String> list,
    // ProcessItem process, String projectName, List<ProcessItem> processedJob, ExportFileResource curResource,
    // Map<ExportChoice, Object> exportChoice, String fatherContext, String... selectedJobVersion) {
    // if (processedJob.contains(process)) {
    // // prevent circle
    // return;
    // }
    // try {
    // process = (ProcessItem)
    // ProxyRepositoryFactory.getInstance().getUptodateProperty(process.getProperty()).getItem();
    // } catch (PersistenceException e) {
    // e.printStackTrace();
    // }
    // processedJob.add(process);
    // addComponentModules(process, curResource);
    // addJobItem(allResources, process, isOptionChoosed(exportChoice, ExportChoice.needJobItem), curResource,
    // selectedJobVersion);
    // addDependencies(allResources, process, isOptionChoosed(exportChoice, ExportChoice.needDependencies),
    // curResource);
    // Set<JobInfo> subjobInfos = ProcessorUtilities.getChildrenJobInfo(process);
    // for (JobInfo subjobInfo : subjobInfos) {
    // if (subjobInfo.getJobName().equals(rootName)) {
    // continue;
    // }
    // String rootProjectName = PerlResourcesHelper.getRootProjectName(subjobInfo.getProcessItem());
    //
    // String jobScriptName = PerlResourcesHelper.getJobFileName(rootProjectName, subjobInfo.getJobName(), subjobInfo
    // .getJobVersion());
    // String contextName = null;
    // if (isOptionChoosed(exportChoice, ExportChoice.applyToChildren)) {
    // // see bug 0003862: Export job with the flag "Apply to children" if the child don't have the
    // // same context fails.
    // ProcessItem processItem = ItemCacheManager.getProcessItem(subjobInfo.getJobId(), subjobInfo.getJobVersion());
    // if (ProcessorUtilities.checkIfContextExisted(processItem, fatherContext)) {
    // contextName = fatherContext;
    // } else {
    // // use the default context of subjob
    // contextName = processItem.getProcess().getDefaultContext();
    // }
    // } else {
    // contextName = escapeSpace(subjobInfo.getContextName());
    // }
    //
    // String contextFullName = PerlResourcesHelper.getContextFileName(rootProjectName, subjobInfo.getJobName(),
    // subjobInfo
    // .getJobVersion(), contextName);
    //
    // addToList(list, jobScriptName);
    // addToList(list, contextFullName);
    //
    // getChildrenJobAndContextName(allResources, rootName, list, subjobInfo.getProcessItem(), projectName,
    // processedJob,
    // curResource, exportChoice, fatherContext);
    // }
    // }
    //
    // /**
    // * Gets context scripts.
    // *
    // * @param process
    // * @param needContext
    // * @return
    // */
    // private List<URL> getContextScripts(ProcessItem process, boolean needContext) {
    // List<String> list = new ArrayList<String>();
    // if (needContext) {
    // List<String> contexts = getJobContexts(process);
    // String version = process.getProperty().getVersion();
    // if (!isMultiNodes() && getSelectedJobVersion() != null) {
    // version = getSelectedJobVersion();
    // }
    // String rootProjectName = PerlResourcesHelper.getRootProjectName(process);
    // for (String contextName : contexts) {
    // String contextFileName = PerlResourcesHelper.getContextFileName(rootProjectName,
    // process.getProperty().getLabel(), version, contextName);
    // list.add(contextFileName);
    // }
    // }
    //
    // IResource[] resources = this.getAllPerlFiles();
    // return getResourcesURL(resources, list);
    // }
    //
    // /*
    // * (non-Javadoc)
    // *
    // *
    // @seeorg.talend.repository.ui.wizards.exportjob.scriptsmanager.JobScriptsManager#getSource(org.talend.core.model.
    // * properties.ProcessItem, boolean)
    // */
    // @Override
    // protected void addJobItem(ExportFileResource[] allResources, ProcessItem processItem, boolean needSource,
    // ExportFileResource curResource, String... selectedJobVersion) {
    // if (!needSource) {
    // return;
    // }
    //
    // // getItemResource(processItem, resource, basePath, selectedJobVersion);
    // // super.addSource(processItem, needSource, resource, basePath, selectedJobVersion);
    // // Get java src
    // try {
    // String projectName = getCorrespondingProjectName(processItem);
    // String jobName = processItem.getProperty().getLabel();
    // String jobVersion = processItem.getProperty().getVersion();
    // if (!isMultiNodes() && selectedJobVersion != null && selectedJobVersion.length == 1) {
    // jobVersion = selectedJobVersion[0];
    // }
    //
    // IPath projectFilePath =
    // getCorrespondingProjectRootPath(processItem).append(FileConstants.LOCAL_PROJECT_FILENAME);
    //
    // String processPath = processItem.getState().getPath();
    //            processPath = processPath == null || processPath.equals("") ? "" : processPath; //$NON-NLS-1$ //$NON-NLS-2$
    // ERepositoryObjectType itemType = ERepositoryObjectType.getItemType(processItem);
    // IPath typeFolderPath = new Path(ERepositoryObjectType.getFolderName(itemType));
    // IPath emfFileRootPath = getEmfFileRootPath(processItem);
    // IPath itemFilePath = emfFileRootPath.append(processPath).append(
    //                    jobName + "_" + jobVersion + "." + FileConstants.ITEM_EXTENSION); //$NON-NLS-1$ //$NON-NLS-2$
    // IPath propertiesFilePath = emfFileRootPath.append(processPath).append(
    //                    jobName + "_" + jobVersion + "." + FileConstants.PROPERTIES_EXTENSION); //$NON-NLS-1$ //$NON-NLS-2$
    // // project file
    // checkAndAddProjectResource(allResources, curResource, JOB_ITEMS_FOLDER_NAME + PATH_SEPARATOR + projectName,
    // FileLocator.toFileURL(projectFilePath.toFile().toURL()));
    //
    // List<URL> emfFileUrls = new ArrayList<URL>();
    // emfFileUrls.add(FileLocator.toFileURL(itemFilePath.toFile().toURL()));
    // emfFileUrls.add(FileLocator.toFileURL(propertiesFilePath.toFile().toURL()));
    // curResource.addResources(JOB_ITEMS_FOLDER_NAME + PATH_SEPARATOR + projectName + PATH_SEPARATOR
    // + typeFolderPath.toOSString(), emfFileUrls);
    //
    // } catch (Exception e) {
    // ExceptionHandler.process(e);
    // }
    // }

}
