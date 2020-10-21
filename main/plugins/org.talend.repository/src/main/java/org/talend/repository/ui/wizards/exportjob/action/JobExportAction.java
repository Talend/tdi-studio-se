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
package org.talend.repository.ui.wizards.exportjob.action;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.PlatformUI;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.exception.MessageBoxExceptionHandler;
import org.talend.core.CorePlugin;
import org.talend.core.context.Context;
import org.talend.core.context.RepositoryContext;
import org.talend.core.model.genhtml.FileCopyUtils;
import org.talend.core.model.process.JobInfo;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.relationship.RelationshipItemBuilder;
import org.talend.core.model.repository.IRepositoryPrefConstants;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.repository.job.JobResource;
import org.talend.core.model.repository.job.JobResourceManager;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.runtime.process.ITalendProcessJavaProject;
import org.talend.core.ui.CoreUIPlugin;
import org.talend.core.ui.export.ArchiveFileExportOperationFullPath;
import org.talend.core.ui.services.IDesignerCoreUIService;
import org.talend.designer.runprocess.IRunProcessService;
import org.talend.designer.runprocess.ItemCacheManager;
import org.talend.designer.runprocess.ProcessorException;
import org.talend.designer.runprocess.ProcessorUtilities;
import org.talend.repository.documentation.ExportFileResource;
import org.talend.repository.i18n.Messages;
import org.talend.repository.model.IRepositoryNode;
import org.talend.repository.model.IRepositoryNode.ENodeType;
import org.talend.repository.model.IRepositoryNode.EProperties;
import org.talend.repository.ui.utils.ZipToFile;
import org.talend.repository.ui.wizards.exportjob.JavaJobExportReArchieveCreator;
import org.talend.repository.ui.wizards.exportjob.scriptsmanager.JobScriptsManager;
import org.talend.repository.ui.wizards.exportjob.util.ExportJobUtil;

/**
 *
 * class global comment. Detailled comment
 */
public class JobExportAction implements IRunnableWithProgress {

    private static final String LIB = "lib";

	private List<? extends IRepositoryNode> nodes;

    private String jobVersion;

    private JobScriptsManager manager;

    private String directoryName;

    private String bundleVersion;

    private String type = "Job";

    private boolean isBuildSuccessful;

    public JobExportAction(List<? extends IRepositoryNode> nodes, String jobVersion, String bundleVersion,
            JobScriptsManager manager, String directoryName, String type) {
        this(nodes, jobVersion, bundleVersion, manager, directoryName);
        this.type = type;
    }

    public JobExportAction(List<? extends IRepositoryNode> nodes, String jobVersion, String bundleVersion,
            JobScriptsManager manager, String directoryName) {
        super();
        this.nodes = nodes;
        this.jobVersion = jobVersion;
        this.bundleVersion = bundleVersion;
        this.manager = manager;
        this.directoryName = directoryName;
    }

    public JobExportAction(List<? extends IRepositoryNode> nodes, String jobVersion, JobScriptsManager manager,
            String directoryName, String type) {
        this(nodes, jobVersion, jobVersion, manager, directoryName, type);
    }

    public JobExportAction(List<? extends IRepositoryNode> nodes, String jobVersion, JobScriptsManager manager,
            String directoryName) {
        this(nodes, jobVersion, jobVersion, manager, directoryName);
    }

    @Override
    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
        monitor.beginTask(Messages.getString("JobScriptsExportWizardPage.newExportJobScript", type), IProgressMonitor.UNKNOWN); //$NON-NLS-1$
        try {
            if (nodes != null && nodes.size() > 0) {
                if (nodes.size() == 1 && RelationshipItemBuilder.LATEST_VERSION.equals(jobVersion)) {
                    ProcessItem item = ItemCacheManager.getProcessItem(nodes.get(0).getId(),
                            RelationshipItemBuilder.LATEST_VERSION);
                    String version = item.getProperty().getVersion();
                    isBuildSuccessful = exportJobScript(nodes, version, bundleVersion, monitor);
                } else {
                    isBuildSuccessful = exportJobScript(nodes, jobVersion, bundleVersion, monitor);
                }
            }
        } finally {
            monitor.done();
            ProcessorUtilities.resetExportConfig();
        }
    }

    public boolean isBuildSuccessful() {
        return isBuildSuccessful;
    }

    private static List<ExportFileResource> getProcesses(Collection<? extends IRepositoryNode> nodes, String path) {
        List<ExportFileResource> value = new ArrayList<ExportFileResource>();
        for (IRepositoryNode node : nodes) {
            if (node.getType() == ENodeType.SYSTEM_FOLDER || node.getType() == ENodeType.SIMPLE_FOLDER) {
                value.addAll(getProcesses(node.getChildren(), node.getProperties(EProperties.LABEL).toString() + '/'));
            }
            if (node.getType() == ENodeType.REPOSITORY_ELEMENT) {
                IRepositoryViewObject repositoryObject = node.getObject();
                if (repositoryObject.getProperty().getItem() instanceof ProcessItem) {
                    ProcessItem processItem = (ProcessItem) repositoryObject.getProperty().getItem();
                    ExportFileResource resource = new ExportFileResource(processItem, path + processItem.getProperty().getLabel());
                    resource.setNode(node);
                    value.add(resource);
                }
            }
        }
        return value;
    }

    private boolean exportJobScript(List<? extends IRepositoryNode> nodes, String version, String bundleVersion,
            IProgressMonitor monitor) throws InvocationTargetException {
        manager.setJobVersion(version);
        manager.setBundleVersion(bundleVersion);

        List<ExportFileResource> processes = getProcesses(nodes, "");
        boolean isNotFirstTime = directoryName != null;
        if (isNotFirstTime && processes != null) {
            for (ExportFileResource process : processes) {
                process.setDirectoryName(directoryName);
            }
        }

        try {
            ProxyRepositoryFactory.getInstance().initialize();
        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
        }
        ItemCacheManager.clearCache();

        if (!isMultiNodes()) { // TODO : bug with export?
            for (ExportFileResource process : processes) {
                process.removeAllMap();
                ProcessItem processItem = (ProcessItem) process.getItem();
                if (!processItem.getProperty().getVersion().equals(version)) {
                    // update with the correct version.
                    process.setProcess(ItemCacheManager.getProcessItem(processItem.getProperty().getId(), version));
                }
            }

        }

        manager.setProgressMonitor(monitor);
        List<ExportFileResource> resourcesToExport = null;
        try {
            resourcesToExport = manager.getExportResources(processes.toArray(new ExportFileResource[] {}));
            IStructuredSelection selection = new StructuredSelection(nodes);
            // if job has compile error, will not export to avoid problem if run jobscript
            boolean hasErrors = CorePlugin.getDefault().getRunProcessService().checkExportProcess(selection, true);
            if (hasErrors) {
                manager.deleteTempFiles();
                return false;
            }
        } catch (ProcessorException e) {
            MessageBoxExceptionHandler.process(e);
            throw new InvocationTargetException(e);
        }
        boolean addClasspathJar = true;
        IDesignerCoreUIService designerCoreUIService = CoreUIPlugin.getDefault().getDesignerCoreUIService();
        if (designerCoreUIService != null) {
            addClasspathJar = designerCoreUIService.getPreferenceStore().getBoolean(IRepositoryPrefConstants.ADD_CLASSPATH_JAR);
        }
        if (isMultiNodes() || addClasspathJar) {
            manager.setTopFolder(resourcesToExport);
        }

        cleanLibFolderBeforeExport();
        doArchiveExport(monitor, resourcesToExport);

        clean();
        ProcessorUtilities.resetExportConfig();

        // no need to regenerate if run in export model
        // boolean generated = generatedCodes(version, monitor, processes);
        // if (!generated) {
        // return false;
        // }

        monitor.subTask(Messages.getString("JobScriptsExportWizardPage.newExportSuccess", type)); //$NON-NLS-1$
        if (addClasspathJar) {
            reBuildJobZipFile(processes);
        } else {
            String zipFile = getTempDestinationValue();
            String destinationZipFile = manager.getDestinationPath();
            FileCopyUtils.copy(zipFile, destinationZipFile);
        }
        return true;
    }

    /**
     * DOC ggu Comment method "doArchiveExport".
     *
     * @param monitor
     * @param resourcesToExport
     */
    protected void doArchiveExport(IProgressMonitor monitor, List<ExportFileResource> resourcesToExport) {
        ArchiveFileExportOperationFullPath exporterOperation = new ArchiveFileExportOperationFullPath(resourcesToExport,
                getTempDestinationValue());
        executeExportOperation(exporterOperation, monitor);
    }

    protected void clean() {
        manager.deleteTempFiles();
    }

    /**
     * DOC ggu Comment method "generatedCodes".
     *
     * @param version
     * @param monitor
     * @param processes
     */
    protected boolean generatedCodes(String version, IProgressMonitor monitor, List<ExportFileResource> processes) {
        String projectName = ((RepositoryContext) CorePlugin.getContext().getProperty(Context.REPOSITORY_CONTEXT_KEY))
                .getProject().getLabel();

        List<JobResource> jobResources = new ArrayList<JobResource>();

        for (ExportFileResource process : processes) {
            ProcessItem processItem = (ProcessItem) process.getItem();
            JobInfo jobInfo = new JobInfo(processItem, processItem.getProcess().getDefaultContext(), version);
            jobResources.add(new JobResource(projectName, jobInfo));

            Set<JobInfo> jobInfos = ProcessorUtilities.getChildrenJobInfo(processItem);
            for (JobInfo subjobInfo : jobInfos) {
                jobResources.add(new JobResource(projectName, subjobInfo));
            }
        }

        JobResourceManager reManager = JobResourceManager.getInstance();
        for (JobResource r : jobResources) {
            if (reManager.isProtected(r)) {
                try {
                    ProcessorUtilities.generateCode(r.getJobInfo().getJobId(), r.getJobInfo().getContextName(), r.getJobInfo()
                            .getJobVersion(), false, false, monitor);
                } catch (ProcessorException e) {
                    MessageBoxExceptionHandler.process(e);
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * DOC zli Comment method "getTempDestinationValue".
     *
     * @return
     */
    protected String getTempDestinationValue() {
        String idealSuffix = manager.getOutputSuffix();
        String destinationText = manager.getDestinationPath();
        String tempdestination = JavaJobExportReArchieveCreator.getTmpDestinationFolder();
        if (destinationText.indexOf("\\") != -1) {
            int lastIndexOf = destinationText.lastIndexOf("\\");
            String substring = destinationText.substring(lastIndexOf + 1, destinationText.length());
            tempdestination = tempdestination + "/" + substring;
        }
        if (tempdestination.length() != 0 && !tempdestination.endsWith(File.separator)) {
            int dotIndex = tempdestination.lastIndexOf('.');
            if (dotIndex != -1) {
                // the last path seperator index
                int pathSepIndex = tempdestination.lastIndexOf(File.separator);
                if (pathSepIndex != -1 && dotIndex < pathSepIndex) {
                    tempdestination += idealSuffix;
                }
            } else {
                tempdestination += idealSuffix;
            }
        }
        if (tempdestination.endsWith(jobVersion + manager.getOutputSuffix())) {
            return tempdestination;
        }
        return tempdestination;

    }

    /**
     *
     * DOC aiming Comment method "reBuildJobZipFile".
     *
     * @param processes
     */
    protected void reBuildJobZipFile(List<ExportFileResource> processes) {
        JavaJobExportReArchieveCreator creator = null;
        String zipFile = getTempDestinationValue();
        String destinationZipFile = manager.getDestinationPath();

        String tmpFolder = ExportJobUtil.getTmpFolder();
        try {
            // unzip to tmpFolder
            ZipToFile.unZipFile(zipFile, tmpFolder);
            // build new jar
            for (ExportFileResource process : processes) {
                if (process != null) {
                    String jobFolderName = process.getDirectoryName();
                    int pos = jobFolderName.indexOf("/"); //$NON-NLS-1$
                    if (pos != -1) {
                        jobFolderName = jobFolderName.substring(pos + 1);
                    }
                    if (creator == null) {
                        creator = new JavaJobExportReArchieveCreator(zipFile, jobFolderName);
                    } else {
                        creator.setJobFolerName(jobFolderName);
                    }
                    creator.buildNewJar();
                }
            }
            // Modified by Marvin Wang on Feb.1, 2012 for bug
            if (canCreateNewFile(destinationZipFile)) {
                // rezip the tmpFolder to zipFile
                ZipToFile.zipFile(tmpFolder, destinationZipFile);
            } else {
                MessageDialog.openError(PlatformUI.getWorkbench().getDisplay().getActiveShell(), "Can not create a file",
                        "Can not create a file or have not the permission to create a file!");
            }
        } catch (Exception e) {
            ExceptionHandler.process(e);
        } finally {
            ExportJobUtil.deleteTempFiles();
            JavaJobExportReArchieveCreator.deleteTempDestinationFiles();
            new File(zipFile).delete(); // delete the temp zip file
        }
    }

    /**
     * Added by Marvin Wang on Feb.1, 2012 for estimating if the file can be created. In win7 or other systems, have not
     * the permission to create a file directly under system disk(like C:\).
     *
     * @param disZipFileStr
     * @return
     */
    private boolean canCreateNewFile(String disZipFileStr) {
        boolean flag = true;
        File disZipFile = new File(disZipFileStr);
        File parentFile = disZipFile.getParentFile();
        try {
            if (!parentFile.exists()) {
                parentFile.mkdirs();
            }
            if (!disZipFile.exists()) {
                disZipFile.createNewFile();
            }
        } catch (IOException e) {
            flag = false;
            ExceptionHandler.process(e);
        }
        return flag;
    }

    /**
     * Export the passed resource and recursively export all of its child resources (iff it's a container). Answer a
     * boolean indicating success.
     */
    private boolean executeExportOperation(ArchiveFileExportOperationFullPath op, IProgressMonitor monitor) {
        op.setCreateLeadupStructure(true);
        op.setUseCompression(true);

        try {
            op.run(monitor);
        } catch (InvocationTargetException e) {
            ExceptionHandler.process(e);
        } catch (InterruptedException e) {
            return false;
        }

        IStatus status = op.getStatus();
        if (!status.isOK()) {
            ErrorDialog.openError(PlatformUI.getWorkbench().getDisplay().getActiveShell(), "", null, // no //$NON-NLS-1$
                    // special
                    // message
                    status);
            return false;
        }

        return true;
    }

    private boolean isMultiNodes() {
        return nodes.size() > 1;
    }

    public void cleanLibFolderBeforeExport() {
    	IRunProcessService runProcessService = CorePlugin.getDefault().getRunProcessService();
        ITalendProcessJavaProject talendProcessJavaProject = runProcessService
                .getTalendJobJavaProject(nodes.get(0).getObject().getProperty());

        if (talendProcessJavaProject != null) {
            File libFolder = new File(talendProcessJavaProject.getBundleResourcesFolder().getLocation().toOSString()
                    + File.separator
                    + LIB);

            if (libFolder.exists()) {
	            for (File file : libFolder.listFiles())
	                if (!file.isDirectory() && file.getName().endsWith(".jar"))
	                    file.delete();
            }
        }
    }

}
