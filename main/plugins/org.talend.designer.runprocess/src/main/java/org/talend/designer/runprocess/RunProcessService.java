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
package org.talend.designer.runprocess;

import java.util.List;
import java.util.Set;

import org.apache.log4j.Level;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.language.ICodeProblemsChecker;
import org.talend.core.model.general.ModuleNeeded;
import org.talend.core.model.general.Project;
import org.talend.core.model.process.IContext;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.process.IProcess2;
import org.talend.core.model.process.JobInfo;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.runtime.process.ITalendProcessJavaProject;
import org.talend.core.runtime.projectsetting.ProjectPreferenceManager;
import org.talend.designer.core.ui.action.SaveJobBeforeRunAction;
import org.talend.designer.runprocess.i18n.Messages;
import org.talend.designer.runprocess.ui.actions.RunProcessAction;

/**
 * DOC qian class global comment. An implementation of the IRunProcessService. <br/>
 *
 * $Id: talend-code-templates.xml 1 2006-09-29 17:06:40 +0000 (星期五, 29 九月 2006) nrousseau $
 *
 */

public class RunProcessService implements IRunProcessService {

    private IRunProcessService delegateService;

    /**
     * DOC amaumont RunProcessService constructor comment.
     */
    public RunProcessService() {
        super();
        setDelegateService(new DefaultRunProcessService());
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.designer.runprocess.IRunProcessService#createCodeProcessor(org.talend.core.model.process.IProcess,
     * org.talend.core.language.ECodeLanguage, boolean)
     */
    @Override
    public IProcessor createCodeProcessor(IProcess process, Property property, ECodeLanguage language,
            boolean filenameFromLabel) {
        return delegateService.createCodeProcessor(process, property, language, filenameFromLabel);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.runprocess.IRunProcessService#createPerformanceData(java.lang.String)
     */
    @Override
    public IPerformanceData createPerformanceData(String data) {
        return delegateService.createPerformanceData(data);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.runprocess.IRunProcessService#getProject(org.talend.core.language.ECodeLanguage)
     */
    @Override
    public IProject getProject(ECodeLanguage language) throws CoreException {
        return delegateService.getProject(language);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.runprocess.IRunProcessService#getRoutineFilenameExt()
     */
    @Override
    public String getRoutineFilenameExt() {
        return delegateService.getRoutineFilenameExt();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.runprocess.IRunProcessService#getSyntaxChecker(org.talend.core.language.ECodeLanguage)
     */
    @Override
    public ICodeProblemsChecker getSyntaxChecker(ECodeLanguage codeLanguage) {
        return delegateService.getSyntaxChecker(codeLanguage);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.runprocess.IRunProcessService#getSelectedContext()
     */
    @Override
    public IContext getSelectedContext() {
        RunProcessContext activeContext = RunProcessPlugin.getDefault().getRunProcessContextManager().getActiveContext();
        if (activeContext != null) {
            return activeContext.getSelectedContext();
        } else {
            return null;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.runprocess.IRunProcessService#perlExec(java.lang.StringBuffer, java.lang.StringBuffer,
     * org.eclipse.core.runtime.IPath, java.lang.String, org.apache.log4j.Level, java.lang.String, java.lang.String,
     * int, int, java.lang.String[])
     */
    @Override
    public int perlExec(StringBuffer out, StringBuffer err, IPath absCodePath, String contextName, Level level,
            String perlInterpreterLibOption, String perlModuleDirectoryOption, int statOption, int traceOption,
            String... codeOptions) throws ProcessorException {
        return delegateService.perlExec(out, err, absCodePath, contextName, level, perlInterpreterLibOption,
                perlModuleDirectoryOption, statOption, traceOption, codeOptions);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.runprocess.IRunProcessService#removeProcess(org.talend.core.model.process.IProcess)
     */
    @Override
    public void removeProcess(IProcess activeProcess) {
        delegateService.removeProcess(activeProcess);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.runprocess.IRunProcessService#setActiveProcess(org.talend.core.model.process.IProcess)
     */
    @Override
    public void setActiveProcess(IProcess2 activeProcess) {
        delegateService.setActiveProcess(activeProcess);
    }

    @Override
    public void setActiveProcess(IProcess2 activeProcess, boolean refreshUI) {
        delegateService.setActiveProcess(activeProcess, refreshUI);
    }

    /*
     * (non-Javadoc)
     *
     * @seeorg.talend.designer.runprocess.IRunProcessService#setDelegateService(org.talend.designer.runprocess.
     * IRunProcessService)
     */
    @Override
    public void setDelegateService(IRunProcessService delegateService) {
        boolean isValidDelegate = delegateService != null && !(delegateService instanceof RunProcessService);
        if (isValidDelegate) {
            this.delegateService = delegateService;
        } else {
            throw new IllegalArgumentException(Messages.getString("RunProcessService.delegateServiceError")); //$NON-NLS-1$
        }
    }

    @Override
    public void updateLibraries(Set<ModuleNeeded> jobModuleList, IProcess process) {
        delegateService.updateLibraries(jobModuleList, process);
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
        delegateService.updateLibraries(jobModuleList, process, alreadyRetrievedModules);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.designer.runprocess.IRunProcessService#updateLibraries(org.talend.core.model.properties.RoutineItem)
     */
    @Override
    public void updateLibraries(Item routineItem) {
        delegateService.updateLibraries(routineItem);
    }

    @Override
    public void refreshView() {
        delegateService.refreshView();
    }

    @Override
    public void switchToCurProcessView() {
        delegateService.switchToCurProcessView();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.runprocess.IRunProcessService#getJavaProject()
     */
    @Override
    public IJavaProject getJavaProject() throws CoreException {
        return delegateService.getJavaProject();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.runprocess.IRunProcessService#getPauseTime()
     */
    @Override
    public int getPauseTime() {
        return delegateService.getPauseTime();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.runprocess.IRunProcessService#deleteAllJobs(boolean)
     */
    @Override
    public void deleteAllJobs(boolean fromPluginModel) {
        new DeleteAllJobWhenStartUp().startup(fromPluginModel);
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

    @Override
    public IAction getRunProcessAction() {
        return new RunProcessAction();
    }

    @Override
    public boolean enableTraceForActiveRunProcess() {
        RunProcessContext activeContext = RunProcessPlugin.getDefault().getRunProcessContextManager().getActiveContext();
        if (activeContext != null) {
            return activeContext.isMonitorTrace();
        }
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.runprocess.IRunProcessService#saveJobBeforeRun(org.talend.core.model.process.IProcess)
     */
    @Override
    public void saveJobBeforeRun(IProcess activeProcess) {
        new SaveJobBeforeRunAction(activeProcess).run();
    }

    @Override
    public IPreferenceStore getPreferenceStore() {
        return RunProcessPlugin.getDefault().getPreferenceStore();
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
        return delegateService.checkExportProcess(selection, isJob);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.runprocess.IRunProcessService#checkLastGenerationHasCompilationError(boolean)
     */
    @Override
    public void checkLastGenerationHasCompilationError(boolean updateProblemsView) throws ProcessorException {
        delegateService.checkLastGenerationHasCompilationError(updateProblemsView);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.runprocess.IRunProcessService#getResourceFile(java.lang.String)
     */
    @Override
    public String getResourceFilePath(String filePath) {
        return delegateService.getResourceFilePath(filePath);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.runprocess.IRunProcessService#getTemplateStrFromPreferenceStore(java.lang.String)
     */
    @Override
    public String getTemplateStrFromPreferenceStore(String templateType) {
        return delegateService.getTemplateStrFromPreferenceStore(templateType);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.runprocess.IRunProcessService#updateLogFiles(org.eclipse.core.resources.IProject)
     */
    @Override
    public void updateLogFiles(ITalendProcessJavaProject talendJavaProject, boolean isLogForJob) {
        delegateService.updateLogFiles(talendJavaProject, isLogForJob);
    }

    @Override
    public String getLogTemplate(String path) {
        return delegateService.getLogTemplate(path);
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
        delegateService.buildJavaProject();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.runprocess.IRunProcessService#getTalendProcessJavaProject()
     */
    @Override
    public ITalendProcessJavaProject getTalendProcessJavaProject() {
        return delegateService.getTalendProcessJavaProject();
    }

    @Override
    public ProjectPreferenceManager getProjectPreferenceManager() {
        return delegateService.getProjectPreferenceManager();
    }

    @Override
    public Set<String> getLibJarsForBD(IProcess process) {
        return delegateService.getLibJarsForBD(process);
    }

    @Override
    public IFolder getJavaProjectLibFolder() {
        return delegateService.getJavaProjectLibFolder();
    }

    @Override
    public IFolder getJavaProjectExternalResourcesFolder(IProcess process) {
        return (IFolder) delegateService.getJavaProjectExternalResourcesFolder(process);
    }
    
    @Override
    public void updateProjectPomWithTemplate() {
        delegateService.updateProjectPomWithTemplate();
    }

    @Override
    public void storeProjectPreferences(IPreferenceStore preferenceStore) {
        delegateService.storeProjectPreferences(preferenceStore);
    }

    @Override
    public void initMavenJavaProject(IProgressMonitor monitor, Project project) {
        delegateService.initMavenJavaProject(monitor, project);
    }

    @Override
    public ITalendProcessJavaProject getTalendCodeJavaProject(ERepositoryObjectType type) {
        return delegateService.getTalendCodeJavaProject(type);
    }

    @Override
    public ITalendProcessJavaProject getTalendCodeJavaProject(ERepositoryObjectType type, String projectTechName) {
        return delegateService.getTalendCodeJavaProject(type, projectTechName);
    }

    @Override
    public ITalendProcessJavaProject getTalendCodesJarJavaProject(Property property) {
        return delegateService.getTalendCodesJarJavaProject(property);
    }

    @Override
    public ITalendProcessJavaProject getTalendJobJavaProject(Property property) {
        return delegateService.getTalendJobJavaProject(property);
    }

    @Override
    public IFolder getCodeSrcFolder(ERepositoryObjectType type, String projectTechName) {
        return delegateService.getCodeSrcFolder(type, projectTechName);
    }

    @Override
    public ITalendProcessJavaProject getTempJavaProject() {
        return delegateService.getTempJavaProject();
    }

    @Override
    public void clearProjectRelatedSettings() {
        delegateService.clearProjectRelatedSettings();
    }

    @Override
    public void batchDeleteAllVersionTalendJobProject(List<String> idList) {
        delegateService.batchDeleteAllVersionTalendJobProject(idList);
    }

    @Override
    public boolean isExportConfig() {
        return delegateService.isExportConfig();
    }

    @Override
    public void buildCodesJavaProject(IProgressMonitor monitor) {
        delegateService.buildCodesJavaProject(monitor);
    }

    @Override
    public void generatePom(Item item) {
        delegateService.generatePom(item);
    }

    @Override
    public void generatePom(Item item, int option) {
        delegateService.generatePom(item, option);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.runprocess.IRunProcessService#initializeRootPoms()
     */
    @Override
    public void initializeRootPoms(IProgressMonitor monitor) {
        delegateService.initializeRootPoms(monitor);
    }

    @Override
    public boolean isGeneratePomOnly() {
        return delegateService.isGeneratePomOnly();
    }

    @Override
    public void handleJobDependencyLoop(JobInfo mainJobInfo, List<JobInfo> listJobs, IProgressMonitor progressMonitor)
            throws Exception {
        delegateService.handleJobDependencyLoop(mainJobInfo, listJobs, progressMonitor);
    }

    @Override
    public boolean isdebug() {

        return delegateService.isdebug();
    }

    @Override
    public boolean isSelectLog4j2() {
        return delegateService.isSelectLog4j2();
    }

    @Override
    public boolean isCIMode() {
        return delegateService.isCIMode();
    }

    @Override
    public boolean isExcludeDeletedItems(Property property) {
        return delegateService.isExcludeDeletedItems(property);
    }

}
