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
package org.talend.repository.ui.wizards.exportjob.scriptsmanager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.maven.model.Model;
import org.apache.maven.model.Profile;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.m2e.core.MavenPlugin;
import org.osgi.framework.FrameworkUtil;
import org.talend.commons.CommonsPlugin;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.runtime.utils.io.FileCopyUtils;
import org.talend.commons.utils.time.TimeMeasure;
import org.talend.core.CorePlugin;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.process.JobInfo;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.relationship.RelationshipItemBuilder;
import org.talend.core.model.repository.IRepositoryPrefConstants;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.runtime.process.IBuildJobHandler;
import org.talend.core.runtime.process.ITalendProcessJavaProject;
import org.talend.core.runtime.process.LastGenerationInfo;
import org.talend.core.runtime.repository.build.IBuildResourceParametes;
import org.talend.core.ui.CoreUIPlugin;
import org.talend.core.ui.services.IDesignerCoreUIService;
import org.talend.designer.maven.model.TalendMavenConstants;
import org.talend.designer.maven.utils.PomUtil;
import org.talend.designer.runprocess.IRunProcessService;
import org.talend.designer.runprocess.ItemCacheManager;
import org.talend.designer.runprocess.ProcessorUtilities;
import org.talend.repository.ProjectManager;
import org.talend.repository.i18n.Messages;
import org.talend.repository.model.IRepositoryNode;
import org.talend.repository.model.IRepositoryNode.ENodeType;
import org.talend.repository.model.RepositoryConstants;
import org.talend.repository.ui.utils.ZipToFile;
import org.talend.repository.ui.wizards.exportjob.JavaJobExportReArchieveCreator;
import org.talend.repository.ui.wizards.exportjob.JavaJobScriptsExportWSWizardPage.JobExportType;
import org.talend.repository.ui.wizards.exportjob.handler.AbstractBuildJobHandler;
import org.talend.repository.ui.wizards.exportjob.scriptsmanager.JobScriptsManager.ExportChoice;
import org.talend.repository.ui.wizards.exportjob.util.ExportJobUtil;
import org.talend.utils.io.FilesUtils;

/**
 * created by ycbai on 2015年5月15日 Detailled comment
 *
 */
public class BuildJobManager {

    private static BuildJobManager instance = null;

    private static Logger LOGGER = Logger.getLogger(BuildJobManager.class);

    private BuildJobManager() {
    }

    public static synchronized BuildJobManager getInstance() {
        if (instance == null) {
            instance = new BuildJobManager();
        }
        return instance;
    }

    private List<ProcessItem> getProcesses(List<? extends IRepositoryNode> nodes) {
        List<ProcessItem> value = new ArrayList<ProcessItem>();
        for (IRepositoryNode node : nodes) {
            if (node.getType() == ENodeType.SYSTEM_FOLDER || node.getType() == ENodeType.SIMPLE_FOLDER) {
                value.addAll(getProcesses(node.getChildren()));
            }
            if (node.getType() == ENodeType.REPOSITORY_ELEMENT) {
                IRepositoryViewObject repositoryObject = node.getObject();
                if (repositoryObject.getProperty().getItem() instanceof ProcessItem) {
                    ProcessItem processItem = (ProcessItem) repositoryObject.getProperty().getItem();
                    value.add(processItem);
                }
            }
        }
        return value;
    }

    public boolean buildJobs(String destinationPath, List<? extends IRepositoryNode> nodes, List<String> topNames,
            String version, String context, Map<ExportChoice, Object> exportChoiceMap, JobExportType jobExportType,
            IProgressMonitor monitor) throws Exception {
        IProgressMonitor pMonitor = new NullProgressMonitor();
        if (monitor != null) {
            pMonitor = monitor;
        }
        final List<ProcessItem> processes = getProcesses(nodes);
        if (processes.size() == 1) {
            ProcessItem item = processes.get(0);
            buildJob(destinationPath, item, version, context, exportChoiceMap, jobExportType, pMonitor);
        } else {
            int scale = 1000;
            int steps = 3;
            pMonitor.beginTask(
                    Messages.getString("JobScriptsExportWizardPage.newExportJobScript", jobExportType), steps * scale * nodes.size()); //$NON-NLS-1$
            String topName = null;
            if (topNames != null && !topNames.isEmpty()) {
                topName = topNames.get(0);
            } else {
                topName = ProjectManager.getInstance().getCurrentProject().getLabel();
            }
            File desFile = new File(destinationPath);

            File createTempFile = File.createTempFile("building_job", ""); //$NON-NLS-1$ //$NON-NLS-2$
            createTempFile.delete();
            File tempFolder = new File(desFile.getParent() + File.separator + createTempFile.getName());
            if (tempFolder.exists()) {
                tempFolder.delete();
            }
            File tempProFolder = new File(tempFolder, topName);
            tempProFolder.mkdirs();
            File rootPom = null;
            boolean buildMavenSource = !Boolean.parseBoolean(exportChoiceMap.get(ExportChoice.binaries).toString());
            for (int i = 0; i < processes.size(); i++) {
                ProcessItem processItem = processes.get(i);
                pMonitor.setTaskName(Messages.getString("BuildJobManager.building", processItem.getProperty().getLabel()));//$NON-NLS-1$

                IBuildJobHandler buildJobHandler = BuildJobFactory.createBuildJobHandler(processItem, context, processItem
                        .getProperty().getVersion(), exportChoiceMap, jobExportType);

                Map<String, Object> prepareParams = new HashMap<String, Object>();
                prepareParams.put(IBuildResourceParametes.OPTION_ITEMS, true);
                prepareParams.put(IBuildResourceParametes.OPTION_ITEMS_DEPENDENCIES, true);
                buildJobHandler.prepare(new SubProgressMonitor(pMonitor, scale), prepareParams);

                buildJobHandler.build(new SubProgressMonitor(pMonitor, scale));
                IFile jobTargetFile = buildJobHandler.getJobTargetFile();
                if (jobTargetFile != null && jobTargetFile.exists()) {
                    FilesUtils.unzip(jobTargetFile.getLocation().toPortableString(), tempProFolder.getAbsolutePath());
                    // arrange zip structure
                    // build subjob and package them to zip
                    if (buildMavenSource) {
                        rootPom = new File(tempProFolder.getAbsolutePath() + File.separator + TalendMavenConstants.POM_FILE_NAME);
                        Model pomModel = MavenPlugin.getMavenModelManager().readMavenModel(rootPom);
                        File itemFile = new File(
                                tempProFolder.getAbsolutePath() + File.separator + processItem.getProperty().getLabel());
                        File newItemFile = new File(tempProFolder.getAbsolutePath() + File.separator + getArrangedJobPath(
                                pomModel, processItem.getProperty().getLabel(), processItem.getProperty().getVersion()));
                        if (itemFile.exists()) {
                            File[] jarFiles = itemFile.listFiles(FilesUtils.getAcceptJARFilesFilter());
                            for (File jarfile : jarFiles) {
                                FilesUtils.deleteFile(jarfile, true);
                            }
                            FileCopyUtils.syncFolder(itemFile, newItemFile, false);
                            FilesUtils.deleteFolder(itemFile, true);
                        }
                        packageSubJob(tempProFolder.getAbsolutePath(), pomModel, rootPom, processItem, processes);
                    }

                    String zipPath = jobTargetFile.getLocation().toPortableString();
                    if (needClasspathJar(exportChoiceMap)) {
                        JavaJobExportReArchieveCreator creator = new JavaJobExportReArchieveCreator(zipPath, processItem
                                .getProperty().getLabel());
                        creator.setTempFolder(tempFolder.getAbsolutePath());
                        creator.buildNewJar();
                    }
                }
                pMonitor.worked(scale);
            }
            if (buildMavenSource) {
                // tup-19705 refresh export root pom to support use mvn package directly
                refreshExportRootPom(tempProFolder.getAbsolutePath(), rootPom);
            }

            FilesUtils.zip(tempFolder.getAbsolutePath(), destinationPath);
            FilesUtils.deleteFile(tempFolder, true);
            pMonitor.done();
        }

        return true;
    }

    public void buildJob(String destinationPath, ProcessItem itemToExport, String version, String context,
            Map<ExportChoice, Object> exportChoiceMap, JobExportType jobExportType, IProgressMonitor monitor) throws Exception {
        buildJob(destinationPath, itemToExport, version, context, exportChoiceMap, jobExportType, false, monitor);
    }

    IBuildJobHandler buildJobHandler = null;
    public void buildJob(String destinationPath, ProcessItem itemToExport, String version, String context,
            Map<ExportChoice, Object> exportChoiceMap, JobExportType jobExportType, boolean checkCompilationError,
            IProgressMonitor monitor) throws Exception {
        IProgressMonitor pMonitor = new NullProgressMonitor();
        if (monitor != null) {
            pMonitor = monitor;
        }
        final boolean oldMeasureActived = TimeMeasure.measureActive;
        if (!oldMeasureActived) { // not active before.
            TimeMeasure.display = TimeMeasure.displaySteps = TimeMeasure.measureActive = CommonsPlugin.isDebugMode();
        }
        final String timeMeasureId = "Build job for " + itemToExport.getProperty().getLabel() + ' ' + version;
        TimeMeasure.begin(timeMeasureId);

        try {
            final int scale = 1000;
            int total = 4;
            pMonitor.beginTask(Messages.getString("JobScriptsExportWizardPage.newExportJobScript", jobExportType), scale * total); //$NON-NLS-1$
            ProcessItem processItem = itemToExport;
            // get correct version
            if (!RelationshipItemBuilder.LATEST_VERSION.equals(version) && version != null && !"".equals(version) //$NON-NLS-1$
                    && !version.equals(processItem.getProperty().getVersion())) {
                processItem = ItemCacheManager.getProcessItem(processItem.getProperty().getId(), version);
            }
            final String label = processItem.getProperty().getLabel();
            ITalendProcessJavaProject talendJavaProject = getRunProcessService()
                    .getTalendJobJavaProject(processItem.getProperty());

            buildJobHandler = BuildJobFactory.createBuildJobHandler(processItem, context, version, exportChoiceMap,
                    jobExportType);

            boolean isSignJob = false;
            if (buildJobHandler instanceof AbstractBuildJobHandler) {
                AbstractBuildJobHandler absBuildJobHandler = (AbstractBuildJobHandler)buildJobHandler;
                isSignJob = absBuildJobHandler.canSignJob();
            }
            final IWorkspaceRunnable op = new IWorkspaceRunnable() {

                @Override
                public void run(IProgressMonitor wrMonitor) throws CoreException {
                    try {
                        SubMonitor subMonitor = SubMonitor.convert(wrMonitor,
                                Messages.getString("JobScriptsExportWizardPage.newExportJobScript", jobExportType),
                                scale * total);
                        Map<String, Object> prepareParams = new HashMap<>();
                        prepareParams.put(IBuildResourceParametes.OPTION_ITEMS, true);
                        prepareParams.put(IBuildResourceParametes.OPTION_ITEMS_DEPENDENCIES, true);

                        buildJobHandler.prepare(subMonitor.split(scale * 2), prepareParams);
                        subMonitor.worked(scale * 2);
                        TimeMeasure.step(timeMeasureId, "prepare to build job"); //$NON-NLS-1$
                        if (subMonitor.isCanceled()) {
                            throw new OperationCanceledException(Messages.getString("BuildJobManager.operationCanceled")); //$NON-NLS-1$
                        }

                        if (jobExportType == JobExportType.IMAGE || jobExportType == JobExportType.MSESB
                                || jobExportType == JobExportType.MSESB_IMAGE) {
                            IFile logFile = talendJavaProject.getProject().getFile("lastGenerated.log");
                            if (logFile.exists()) {
                                logFile.delete(true, false, subMonitor);
                            }
                        }

                        buildJobHandler.build(subMonitor.split(scale * 2));
                        TimeMeasure.step(timeMeasureId, "build and package"); //$NON-NLS-1$
                        if (subMonitor.isCanceled()) {
                            throw new OperationCanceledException(Messages.getString("BuildJobManager.operationCanceled")); //$NON-NLS-1$
                        }
                        subMonitor.worked(scale * 2);
                    } catch (Exception e) {
                        throw new CoreException(new org.eclipse.core.runtime.Status(IStatus.ERROR, FrameworkUtil.getBundle(
                                this.getClass()).getSymbolicName(), "Error", e)); //$NON-NLS-1$
                    }
                };

            };
            IWorkspace workspace = ResourcesPlugin.getWorkspace();
            try {
                ISchedulingRule schedulingRule = workspace.getRoot();
                // the update the project files need to be done in the workspace runnable to avoid all
                // notification
                // of changes before the end of the modifications.
                workspace.run(op, schedulingRule, IWorkspace.AVOID_UPDATE, pMonitor);
            } catch (CoreException e) {
                Throwable cause = e.getCause();
                if (cause == null) {
                    throw new PersistenceException(e);
                }
                throw new PersistenceException(cause);
            }
            IFile mvnLogFile = talendJavaProject.getProject().getFile("lastGenerated.log"); //$NON-NLS-1$
            String mvnLogFilePath = mvnLogFile.getLocation().toPortableString();
            String causeMsg = Messages.getString("BuildJobManager.mavenErrorMessage", mvnLogFilePath); //$NON-NLS-1$
            String logMsg = getLogErrorMsg(mvnLogFile);

            if (jobExportType != JobExportType.IMAGE && jobExportType != JobExportType.MSESB_IMAGE) {
                IFile targetFile = buildJobHandler.getJobTargetFile();
                if (targetFile != null && targetFile.exists()) {
                    File jobZipFile = targetFile.getLocation().toFile();
                    String jobZip = jobZipFile.toString();
                    if (needClasspathJar(exportChoiceMap)) {
                        ExportJobUtil.deleteTempFiles();
                        JavaJobExportReArchieveCreator creator = new JavaJobExportReArchieveCreator(jobZip, label);
                        FilesUtils.unzip(jobZip, creator.getTmpFolder() + File.separator + label + "_" + version);
                        creator.buildNewJar();
                        ZipToFile.zipFile(creator.getTmpFolder(), jobZip);
                        creator.deleteTempFiles();
                        TimeMeasure.step(timeMeasureId, "Recreate job jar for classpath");
                    }
                    if (!Boolean.parseBoolean(exportChoiceMap.get(ExportChoice.binaries).toString())) {
                        // tup-19705 refresh export root pom to support use mvn package directly
                        List<String> itemLabels = new ArrayList<String>();
                        itemLabels.add(label);
                        ExportJobUtil.deleteTempFiles();
                        String temUnzipPath = ExportJobUtil.getTmpFolder() + File.separator + label + "_" + version;
                        FilesUtils.unzip(jobZip, temUnzipPath);
                        // arrange zip structure
                        File rootPom = new File(temUnzipPath + File.separator + TalendMavenConstants.POM_FILE_NAME);
                        Model pomModel = MavenPlugin.getMavenModelManager().readMavenModel(rootPom);
                        File itemFile = new File(temUnzipPath + File.separator + label);
                        File newItemFile = new File(temUnzipPath + File.separator + getArrangedJobPath(pomModel, label, version));
                        if (itemFile.exists()) {
                            File[] jarFiles = itemFile.listFiles(FilesUtils.getAcceptJARFilesFilter());
                            for (File jarfile : jarFiles) {
                                FilesUtils.deleteFile(jarfile, true);
                            }
                            FileCopyUtils.syncFolder(itemFile, newItemFile, false);
                            FilesUtils.deleteFolder(itemFile, true);
                        }
                        packageSubJob(temUnzipPath, pomModel, rootPom, processItem, null);
                        refreshExportRootPom(temUnzipPath, rootPom);

                        ZipToFile.zipFile(ExportJobUtil.getTmpFolder(), jobZip);
                        ExportJobUtil.deleteTempFiles();
                    }

                    File jobFileTarget = new File(destinationPath);
                    if (jobFileTarget.isDirectory()) {
                        jobFileTarget = new File(destinationPath, jobZipFile.getName());
                    }

                    // retry if jobzip file is scanned by antivirus
                    for (int i = 0; i < 500; i++) {
                        try {
                            FilesUtils.copyFile(jobZipFile, jobFileTarget);
                            break;
                        } catch (Exception e) {
                            LOGGER.warn("Can not copy " + jobZipFile + " to " + jobFileTarget, e);
                            Thread.sleep(100);
                        }
                    }

                    TimeMeasure.step(timeMeasureId, "Copy packaged file to target");
                    if (isSignJob) {
                        for (String line : logMsg.split("\n")) { //$NON-NLS-1$
                            if (line.startsWith("[ERROR]") && line.indexOf(TalendMavenConstants.SIGNER_MAVEN_PLUGIN_NAME) > 0) { //$NON-NLS-1$
                                causeMsg += ". Sign archive failed."; //$NON-NLS-1$
                                throw new Exception(
                                        Messages.getString("BuildJobManager.mavenErrorMessage", mvnLogFilePath) + "\n" + logMsg, //$NON-NLS-1$
                                        new Throwable(causeMsg));
                            }
                        }
                    }
                } else {
                    for (String line : logMsg.split("\n")) { //$NON-NLS-1$
                        if (line.startsWith("[ERROR] Tests run")) { //$NON-NLS-1$
                            causeMsg += ". There exists test case job failure."; //$NON-NLS-1$
                            break;
                        }
                    }
                    throw new Exception(Messages.getString("BuildJobManager.mavenErrorMessage", mvnLogFilePath) + "\n" + logMsg, //$NON-NLS-1$
                            new Throwable(causeMsg));
                }
            } else {
                if (!StringUtils.isBlank(logMsg)) {
                    throw new Exception(Messages.getString("BuildJobManager.mavenErrorMessage", mvnLogFilePath) + "\n" + logMsg, //$NON-NLS-1$ //$NON-NLS-2$
                            new Throwable(causeMsg));
                }
            }
            if (checkCompilationError) {
                CorePlugin.getDefault().getRunProcessService().checkLastGenerationHasCompilationError(false);
            }
            pMonitor.worked(scale);
            pMonitor.done();
        } finally {
            TimeMeasure.end(timeMeasureId);
            ProcessorUtilities.resetExportConfig();
            // if active before, not disable and active still.
            if (!oldMeasureActived) {
                TimeMeasure.display = TimeMeasure.displaySteps = TimeMeasure.measureActive = false;
            }
        }
    }

    private void packageSubJob(String zipLocation, Model pomModel, File rootPom, ProcessItem item,
            final List<ProcessItem> checkedProcesses)
            throws Exception {
        List<ProcessItem> dependenciesItems = new ArrayList<ProcessItem>();
        JobInfo mainJobInfo = LastGenerationInfo.getInstance().getLastMainJob();
        for (JobInfo jobInfo : mainJobInfo.getProcessor().getBuildChildrenJobs()) {
            if (checkedProcesses != null && checkedProcesses.contains(jobInfo.getProcessItem())) {
                continue;
            }
            dependenciesItems.add(jobInfo.getProcessItem());
        }

        ITalendProcessJavaProject mainProject = getRunProcessService().getTalendJobJavaProject(item.getProperty());
        String mainTechLabel = ProjectManager.getInstance().getProject(mainProject.getPropery()).getTechnicalLabel();
        File file = new File(zipLocation);

        for (ProcessItem processItem : dependenciesItems) {
            ITalendProcessJavaProject project = getRunProcessService().getTalendJobJavaProject(processItem.getProperty());
            String techLabel = ProjectManager.getInstance().getProject(project.getPropery()).getTechnicalLabel();
            File srcfile = project.getProject().getFolder(new Path("src")).getLocation().toFile();
            File pomfile = project.getProjectPom().getLocation().toFile();
            String destpath = null;
            File refRootPom = null;
            if (!mainTechLabel.equals(techLabel)) {
                String techpath = file.getParent() + File.separator + techLabel;
                refRootPom = new File(techpath + File.separator + TalendMavenConstants.POM_FILE_NAME);
                File parentPomFolder = pomfile.getParentFile();
                int nb = 10;
                while (parentPomFolder != null && !parentPomFolder.getName().equals(RepositoryConstants.POMS_DIRECTORY)) {
                    parentPomFolder = parentPomFolder.getParentFile();
                    nb--;
                    if (nb < 0) {
                        break;
                    }
                }
                if (parentPomFolder != null && parentPomFolder.exists()) {
                    File refPomFile = new File(
                            parentPomFolder.getAbsolutePath() + File.separator + TalendMavenConstants.POM_FILE_NAME);
                    if (refPomFile.exists()) {
                        // copy reference project pom to export zip
                        FilesUtils.copyFile(refPomFile, refRootPom);
                        Model refPomModel = MavenPlugin.getMavenModelManager().readMavenModel(refPomFile);
                        destpath = techpath + File.separator + getArrangedJobPath(refPomModel,
                                processItem.getProperty().getLabel(), processItem.getProperty().getVersion());
                    }
                }
            } else {
                destpath = zipLocation + File.separator + getArrangedJobPath(pomModel, processItem.getProperty().getLabel(),
                        processItem.getProperty().getVersion());
            }

            File destFile = new File(destpath);
            if (!destFile.exists()) {
                destFile.mkdirs();
            }

            FileCopyUtils.syncFolder(srcfile, new File(destpath + File.separator + "src"), false);
            FilesUtils.copyFile(pomfile, new File(destpath + File.separator + TalendMavenConstants.POM_FILE_NAME));
            if (!mainTechLabel.equals(techLabel) && refRootPom != null && refRootPom.exists()) {
                refreshExportRootPom(refRootPom.getParent(), refRootPom);
            }
        }
        // add reference project pom to main exported pom
        for (String fileLabel : rootPom.getParentFile().getParentFile().list()) {
            if (!file.getName().equals(fileLabel)) {
                pomModel.addModule("../" + fileLabel);
                PomUtil.savePom(null, pomModel, rootPom);
            }
        }

    }

    private void refreshExportRootPom(String pomLocation, File rootPom) throws Exception {
        if (rootPom.exists()) {
            Model pomModel = MavenPlugin.getMavenModelManager().readMavenModel(rootPom);
            List<Profile> profiles = pomModel.getProfiles();
            Iterator<Profile> profileIte = profiles.iterator();
            while (profileIte.hasNext()) {
                Profile profile = profileIte.next();
                if (TalendMavenConstants.PROFILE_CI_BUILDER.equals(profile.getId())) {
                    profileIte.remove();
                }
            }
            List<String> modules = pomModel.getModules();
            Iterator<String> modulesIte = modules.iterator();
            while (modulesIte.hasNext()) {
                File sourcefile = null;
                String module = modulesIte.next();
                if (module.contains("../")) {
                    sourcefile = new File(pomLocation).getParentFile();
                    sourcefile = new File(sourcefile.getAbsolutePath() + File.separator + module.replaceAll("../", ""));
                } else {
                    sourcefile = new File(pomLocation + File.separator + module);
                }
                if (sourcefile != null && !sourcefile.exists() && !sourcefile.isDirectory()) {
                    modulesIte.remove();
                }
            }
            PomUtil.savePom(null, pomModel, rootPom);
        }
    }

    private String getArrangedJobPath(Model pomModel, String label, String version) throws Exception {
        if (pomModel != null) {
            List<String> modules = pomModel.getModules();
            for (String module : modules) {
                if (module.contains(label + "_" + version)) {
                    return module;
                }
            }
        }
        return "";
    }

    private String getLogErrorMsg(IFile file) throws IOException, CoreException {
        file.refreshLocal(IResource.DEPTH_ZERO, null);
        if (!file.exists()) {
            return ""; //$NON-NLS-1$
        }
    	BufferedReader reader = null;
    	StringBuffer errorbuffer = new StringBuffer();
    	try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(file.getLocation().toFile())));
			String line;
			while ((line = reader.readLine()) != null) { //$NON-NLS-1$
				if (line.startsWith("[ERROR]")) { //$NON-NLS-1$
					errorbuffer.append(line + "\n"); //$NON-NLS-1$
                }
			}
			return errorbuffer.toString();
		} catch (IOException e) {
			 ExceptionHandler.process(e);
			return null;
		}finally {
			 if (reader != null) {
				 reader.close();
			 }
		}
    }

    private boolean needClasspathJar(Map<ExportChoice, Object> exportChoiceMap) {
        boolean addClasspathJar = false;
        // only binaries need classpath jar.
        Object isBinaries = exportChoiceMap.get(ExportChoice.binaries);
        if (isBinaries == null || !(isBinaries instanceof Boolean) || !(Boolean) isBinaries) {
            return false;
        }
        IDesignerCoreUIService designerCoreUIService = CoreUIPlugin.getDefault().getDesignerCoreUIService();
        if (designerCoreUIService != null) {
            addClasspathJar = designerCoreUIService.getPreferenceStore().getBoolean(IRepositoryPrefConstants.ADD_CLASSPATH_JAR);
        }
        return addClasspathJar;
    }

    private IRunProcessService getRunProcessService() {
        IRunProcessService service = null;
        if (GlobalServiceRegister.getDefault().isServiceRegistered(IRunProcessService.class)) {
            service = GlobalServiceRegister.getDefault().getService(IRunProcessService.class);
        }
        return service;
    }

}
