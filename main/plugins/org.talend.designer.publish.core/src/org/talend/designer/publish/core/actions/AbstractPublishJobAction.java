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
package org.talend.designer.publish.core.actions;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.StructuredSelection;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.general.ModuleNeeded;
import org.talend.core.model.properties.ProcessItem;
import org.talend.designer.publish.core.models.BundleModel;
import org.talend.designer.publish.core.models.FeaturesModel;
import org.talend.designer.runprocess.IRunProcessService;
import org.talend.repository.model.IRepositoryNode;
import org.talend.repository.ui.wizards.exportjob.JavaJobScriptsExportWSWizardPage.JobExportType;
import org.talend.repository.ui.wizards.exportjob.scriptsmanager.BuildJobManager;
import org.talend.repository.ui.wizards.exportjob.scriptsmanager.JobScriptsManager;
import org.talend.repository.ui.wizards.exportjob.scriptsmanager.JobScriptsManager.ExportChoice;
import org.talend.repository.utils.EmfModelUtils;
import org.talend.repository.utils.JobContextUtils;

public abstract class AbstractPublishJobAction implements IRunnableWithProgress {

    private static final String THMAP_COMPONENT_NAME = "tHMap";

    protected final IRepositoryNode node;

    private final String groupId;

    private final String artifactName;

    private final String artifactVersion;

    private final String bundleVersion;

    private final String jobVersion;

    protected JobExportType exportType;

    private JobScriptsManager jobScriptsManager;

    private Map<ExportChoice, Object> exportChoiceMap;

    public AbstractPublishJobAction(IRepositoryNode node, String groupId, String artifactName, String artifactVersion,
            String bundleVersion, String jobVersion) {
        this.node = node;
        this.groupId = groupId;
        this.artifactName = artifactName;
        this.artifactVersion = artifactVersion;
        this.jobVersion = jobVersion;
        this.bundleVersion = bundleVersion;
        this.exportType = JobExportType.OSGI;
    }

    public AbstractPublishJobAction(IRepositoryNode node, String groupId, String artifactName, String artifactVersion,
            String bundleVersion, String jobVersion, JobExportType exportType, JobScriptsManager jobScriptsManager) {
        this(node, groupId, artifactName, artifactVersion, bundleVersion, jobVersion);
        this.jobScriptsManager = jobScriptsManager;
    }

    public AbstractPublishJobAction(IRepositoryNode node, String groupId, String artifactName, String artifactVersion,
            String bundleVersion, String jobVersion, JobExportType exportType, Map<ExportChoice, Object> exportChoiceMap) {
        this(node, groupId, artifactName, artifactVersion, bundleVersion, jobVersion);
        this.exportType = exportType;
        this.exportChoiceMap = exportChoiceMap;
    }

    protected abstract void process(ProcessItem processItem, FeaturesModel featuresModel, IProgressMonitor monitor)
            throws IOException, InterruptedException;

    @Override
    public final void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
        switch (exportType) {
        case POJO:
            exportJobForPOJO(monitor);
            break;
        case OSGI:
            exportJobForOSGI(monitor);
            break;
        case IMAGE:
            exportJobForImage(monitor);
            break;
        default:
            exportJobForOSGI(monitor);
            break;
        }
        // http://jira.talendforge.org/browse/TESB-5426 LiXiaopeng
        monitor.done();
    }

    private void exportJobForOSGI(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
        File tmpJob = null;
        try {
            tmpJob = File.createTempFile("item", ".jar", null);

            // TDI-32861, because for publish job, so means, must be binaries
            exportChoiceMap.put(ExportChoice.binaries, true);
            exportChoiceMap.put(ExportChoice.includeLibs, true);
            
            // TESB-26145 adding context to published job
            exportChoiceMap.put(ExportChoice.needContext, true);

            ProcessItem processItem = (ProcessItem) node.getObject().getProperty().getItem();
            exportItemForDQComponent(processItem);

            String contextName = (String) exportChoiceMap.get(ExportChoice.contextName);
            if (contextName == null) {
                contextName = processItem.getProcess().getDefaultContext();
            }
            BuildJobManager.getInstance().buildJob(tmpJob.getAbsolutePath(), processItem, jobVersion, contextName,
                    exportChoiceMap, exportType, monitor);
            if (GlobalServiceRegister.getDefault().isServiceRegistered(IRunProcessService.class)) {
                IRunProcessService service = (IRunProcessService) GlobalServiceRegister.getDefault().getService(
                        IRunProcessService.class);
                boolean hasError = service.checkExportProcess(new StructuredSelection(node), true);
                if (hasError) {
                    return;
                }
            }

            monitor.beginTask("Deploy to Artifact Repository....", IProgressMonitor.UNKNOWN);
            FeaturesModel featuresModel = getFeatureModel(tmpJob);
            process(processItem, featuresModel, monitor);
        } catch (InterruptedException e) {
            throw e;
        } catch (Exception e) {
            throw new InvocationTargetException(e);
        } finally {
            if (tmpJob != null && tmpJob.exists()) {
                tmpJob.delete();
            }
        }
    }

    /**
     * TDQ-15391: when have tDqReportRun, must always export items.
     *
     * @param processItem
     */
    private void exportItemForDQComponent(ProcessItem processItem) {
        if (EmfModelUtils.getComponentByName(processItem, "tDqReportRun") != null) { //$NON-NLS-1$
            exportChoiceMap.put(ExportChoice.needJobItem, true);
        }
    }

    private void exportJobForPOJO(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
        File tmpJob = null;
        try {
            tmpJob = File.createTempFile("item", ".zip", null);
            // jobScriptsManager.setDestinationPath(tmpJob.getAbsolutePath());
            // JobExportAction action = new JobExportAction(Collections.singletonList(node), jobVersion,
            // jobScriptsManager, null,
            // "job");
            // action.run(monitor);
            // if (!action.isBuildSuccessful()) {
            // return;
            // }

            // TDI-32861, because for publish job, so means, must be binaries
            exportChoiceMap.put(ExportChoice.binaries, true);
            exportChoiceMap.put(ExportChoice.includeLibs, true);
            exportChoiceMap.put(ExportChoice.addStatistics, true);

            ProcessItem processItem = (ProcessItem) node.getObject().getProperty().getItem();
            exportItemForDQComponent(processItem);

            String contextName = (String) exportChoiceMap.get(ExportChoice.contextName);
            if (contextName == null) {
                contextName = processItem.getProcess().getDefaultContext();
            }
            BuildJobManager.getInstance().buildJob(tmpJob.getAbsolutePath(), processItem, jobVersion, contextName,
                    exportChoiceMap, exportType, monitor);
            if (GlobalServiceRegister.getDefault().isServiceRegistered(IRunProcessService.class)) {
                IRunProcessService service = (IRunProcessService) GlobalServiceRegister.getDefault().getService(
                        IRunProcessService.class);
                boolean hasError = service.checkExportProcess(new StructuredSelection(node), true);
                if (hasError) {
                    return;
                }
            }

            monitor.beginTask("Deploy to Artifact Repository....", IProgressMonitor.UNKNOWN);
            FeaturesModel featuresModel = getFeatureModel(tmpJob);
            process(processItem, featuresModel, monitor);
        } catch (InterruptedException e) {
            throw e;
        } catch (Exception e) {
            throw new InvocationTargetException(e);
        } finally {
            if (tmpJob != null && tmpJob.exists()) {
                tmpJob.delete();
            }
        }
    }

    private void exportJobForImage(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
        try {
            exportChoiceMap.put(ExportChoice.buildImage, true);
            exportChoiceMap.put(ExportChoice.pushImage, true);
            exportChoiceMap.put(ExportChoice.needAssembly, true);
            exportChoiceMap.put(ExportChoice.binaries, true);
            exportChoiceMap.put(ExportChoice.includeLibs, true);
            exportChoiceMap.put(ExportChoice.addStatistics, true);

            ProcessItem processItem = (ProcessItem) node.getObject().getProperty().getItem();
            exportItemForDQComponent(processItem);

            String contextName = (String) exportChoiceMap.get(ExportChoice.contextName);
            if (contextName == null) {
                contextName = processItem.getProcess().getDefaultContext();
            }
            BuildJobManager.getInstance().buildJob(null, processItem, jobVersion, contextName,
                    exportChoiceMap, exportType, monitor);
            if (GlobalServiceRegister.getDefault().isServiceRegistered(IRunProcessService.class)) {
                IRunProcessService service = (IRunProcessService) GlobalServiceRegister.getDefault()
                        .getService(IRunProcessService.class);
                boolean hasError = service.checkExportProcess(new StructuredSelection(node), true);
                if (hasError) {
                    return;
                }
            }
        } catch (InterruptedException e) {
            throw e;
        } catch (Exception e) {
            throw new InvocationTargetException(e);
        }
    }

    private FeaturesModel getFeatureModel(File tmpJob) {
        ProcessItem processItem = (ProcessItem) node.getObject().getProperty().getItem();
        FeaturesModel featuresModel = new FeaturesModel(groupId, artifactName, artifactVersion);
        featuresModel.setConfigName(node.getObject().getLabel());
        featuresModel.setContexts(JobContextUtils.getContextsMap(processItem));
        BundleModel bundleModel = new BundleModel(groupId, artifactName, artifactVersion, tmpJob);
        featuresModel.addBundle(bundleModel);
        return featuresModel;
    }

    protected String getArtifactVersion() {
        return artifactVersion;
    }

    protected void processModules(FeaturesModel featuresModel, Collection<ModuleNeeded> modules) {
    }

}
