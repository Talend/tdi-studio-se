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
package org.talend.repository.ui.wizards.exportjob.scriptsmanager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.osgi.framework.FrameworkUtil;
import org.talend.commons.CommonsPlugin;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.utils.time.TimeMeasure;
import org.talend.core.CorePlugin;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.process.JobInfo;
import org.talend.core.model.process.ProcessUtils;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.relationship.RelationshipItemBuilder;
import org.talend.core.model.repository.IRepositoryPrefConstants;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.runtime.process.IBuildJobHandler;
import org.talend.core.runtime.process.ITalendProcessJavaProject;
import org.talend.core.runtime.repository.build.IBuildResourceParametes;
import org.talend.core.ui.CoreUIPlugin;
import org.talend.core.ui.services.IDesignerCoreUIService;
import org.talend.designer.runprocess.IRunProcessService;
import org.talend.designer.runprocess.ItemCacheManager;
import org.talend.designer.runprocess.ProcessorUtilities;
import org.talend.repository.ProjectManager;
import org.talend.repository.i18n.Messages;
import org.talend.repository.model.IRepositoryNode;
import org.talend.repository.model.IRepositoryNode.ENodeType;
import org.talend.repository.ui.utils.ZipToFile;
import org.talend.repository.ui.wizards.exportjob.JavaJobExportReArchieveCreator;
import org.talend.repository.ui.wizards.exportjob.JavaJobScriptsExportWSWizardPage.JobExportType;
import org.talend.repository.ui.wizards.exportjob.scriptsmanager.JobScriptsManager.ExportChoice;
import org.talend.repository.ui.wizards.exportjob.util.ExportJobUtil;
import org.talend.utils.io.FilesUtils;

/**
 * created by ycbai on 2015年5月15日 Detailled comment
 *
 */
public class BuildJobManager {

    private static BuildJobManager instance = null;

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
                    // unzip to temp folder
                    FilesUtils.unzip(jobTargetFile.getLocation().toPortableString(), tempProFolder.getAbsolutePath());
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
            if (!RelationshipItemBuilder.LATEST_VERSION.equals(version) && version != null && !"".equals(version)
                    && !version.equals(processItem.getProperty().getVersion())) {
                processItem = ItemCacheManager.getProcessItem(processItem.getProperty().getId(), version);
            }
            final String label = processItem.getProperty().getLabel();
            final IBuildJobHandler buildJobHandler = BuildJobFactory.createBuildJobHandler(processItem, context, version,
                    exportChoiceMap, jobExportType);
            ProcessUtils.setJarWithContext(ProcessUtils.needsToHaveContextInsideJar(processItem));
            final IWorkspaceRunnable op = new IWorkspaceRunnable() {

                @Override
                public void run(IProgressMonitor wrMonitor) throws CoreException {
                    try {
                        wrMonitor.beginTask(Messages.getString("JobScriptsExportWizardPage.newExportJobScript", jobExportType),
                                scale * 3);

                        Map<String, Object> prepareParams = new HashMap<String, Object>();
                        prepareParams.put(IBuildResourceParametes.OPTION_ITEMS, true);
                        prepareParams.put(IBuildResourceParametes.OPTION_ITEMS_DEPENDENCIES, true);

                        buildJobHandler.prepare(new SubProgressMonitor(wrMonitor, scale), prepareParams);

                        wrMonitor.worked(scale);
                        TimeMeasure.step(timeMeasureId, "prepare to build job");
                        if (wrMonitor.isCanceled()) {
                            throw new OperationCanceledException(Messages.getString("BuildJobManager.operationCanceled"));
                        }

                        buildJobHandler.build(new SubProgressMonitor(wrMonitor, scale));
                        TimeMeasure.step(timeMeasureId, "build and package");
                        if (wrMonitor.isCanceled()) {
                            throw new OperationCanceledException(Messages.getString("BuildJobManager.operationCanceled"));
                        }
                        wrMonitor.done();
                    } catch (Exception e) {
                        throw new CoreException(new org.eclipse.core.runtime.Status(IStatus.ERROR, FrameworkUtil.getBundle(
                                this.getClass()).getSymbolicName(), "Error", e));
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
            ProcessUtils.setJarWithContext(false);
            IFile jobTargetFile = buildJobHandler.getJobTargetFile();
            if (jobTargetFile != null && jobTargetFile.exists()) {
                IPath jobZipLocation = jobTargetFile.getLocation();
                File jobZipFile = jobZipLocation.toFile();
                String jobZip = jobZipLocation.toString();

                if (needClasspathJar(exportChoiceMap)) {
                    ExportJobUtil.deleteTempFiles();
                    JavaJobExportReArchieveCreator creator = new JavaJobExportReArchieveCreator(jobZip, label);
                    FilesUtils.unzip(jobZip, creator.getTmpFolder() + File.separator + label + "_" + version);
                    creator.buildNewJar();
                    ZipToFile.zipFile(creator.getTmpFolder(), jobZip);
                    creator.deleteTempFiles();
                    TimeMeasure.step(timeMeasureId, "Recreate job jar for classpath");
                }
                // TBD-2500
                Set<ProcessItem> processItems = new HashSet<ProcessItem>();
                processItems.add(processItem);
                // We get the father job childs.
                Set<JobInfo> infos = ProcessorUtilities.getChildrenJobInfo(processItem);
                Iterator<JobInfo> infoIterator = infos.iterator();
                while (infoIterator.hasNext()) {
                    processItems.add(infoIterator.next().getProcessItem());
                }
                TimeMeasure.step(timeMeasureId, "getChildrenJobInfo");

                // We iterate over the job and its childs in order to re-archive them if needed.
                for (ProcessItem pi : processItems) {
                    BDJobReArchieveCreator bdRecreator = new BDJobReArchieveCreator(pi, processItem);
                    bdRecreator.create(jobZipFile);
                }
                TimeMeasure.step(timeMeasureId, "BDJobReArchieveCreator");

                File jobFileTarget = new File(destinationPath);
                if (jobFileTarget.isDirectory()) {
                    jobFileTarget = new File(destinationPath, jobZipFile.getName());
                }
                FilesUtils.copyFile(jobZipFile, jobFileTarget);
                TimeMeasure.step(timeMeasureId, "Copy packaged file to target");
            } else {
                ITalendProcessJavaProject talendJavaProject = getRunProcessService().getTalendJobJavaProject(
                        processItem.getProperty());
                String mvnLogFilePath = talendJavaProject.getProject()
                        .getFile("lastGenerated.log").getLocation().toPortableString(); //$NON-NLS-1$
                String causeMsg = Messages.getString("BuildJobManager.mavenErrorMessage", mvnLogFilePath); //$NON-NLS-1$
                String logMsg = getLogErrorMsg(mvnLogFilePath); //$NON-NLS-1$
                for (String line : logMsg.split("\n")) { //$NON-NLS-1$
                    if (line.startsWith("[ERROR] Tests run")) { //$NON-NLS-1$
                    	causeMsg += ". There exists test case job failure."; //$NON-NLS-1$
                    	break;
                    }
                }
                throw new Exception(Messages.getString("BuildJobManager.mavenErrorMessage", mvnLogFilePath)+"\n"+logMsg,new Throwable(causeMsg)); //$NON-NLS-1$
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
    
    private String getLogErrorMsg(String filepath) throws IOException{
    	BufferedReader reader = null;
    	StringBuffer errorbuffer = new StringBuffer();
    	try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(filepath)));
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
            service = (IRunProcessService) GlobalServiceRegister.getDefault().getService(IRunProcessService.class);
        }
        return service;
    }

}
