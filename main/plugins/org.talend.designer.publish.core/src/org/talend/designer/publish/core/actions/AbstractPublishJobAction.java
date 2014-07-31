// ============================================================================
//
// Copyright (C) 2006-2014 Talend Inc. - www.talend.com
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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.talend.core.model.general.ModuleNeeded;
import org.talend.core.model.process.IContext;
import org.talend.core.model.properties.ProcessItem;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.publish.core.models.BundleModel;
import org.talend.designer.publish.core.models.FeatureModel;
import org.talend.designer.publish.core.models.FeaturesModel;
import org.talend.designer.runprocess.IProcessor;
import org.talend.repository.model.IRepositoryNode;
import org.talend.repository.ui.wizards.exportjob.JavaJobScriptsExportWSWizardPage.JobExportType;
import org.talend.repository.ui.wizards.exportjob.action.JobExportAction;
import org.talend.repository.ui.wizards.exportjob.scriptsmanager.JobScriptsManager;
import org.talend.repository.ui.wizards.exportjob.scriptsmanager.JobScriptsManagerFactory;
import org.talend.repository.ui.wizards.exportjob.scriptsmanager.esb.JobJavaScriptOSGIForESBManager;
import org.talend.repository.utils.EmfModelUtils;
import org.talend.repository.utils.JobContextUtils;

public abstract class AbstractPublishJobAction implements IRunnableWithProgress {

	private static final String THMAP_COMPONENT_NAME = "tHMap";

	private final IRepositoryNode node;

    private final String groupId;

    private final String artifactName;

    private final String artifactVersion;

    private final String bundleVersion;

    private String jobVersion;

    public AbstractPublishJobAction(IRepositoryNode node, String groupId,
            String artifactName, String artifactVersion, String bundleVersion, String jobVersion) {
        this.node = node;
        this.groupId = groupId;
        this.artifactName = artifactName;
        this.artifactVersion = artifactVersion;
        this.jobVersion = jobVersion;
        this.bundleVersion = bundleVersion;
    }

    protected abstract void process(ProcessItem processItem, FeaturesModel featuresModel, IProgressMonitor monitor) throws IOException;

    public final void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
        File tmpJob;
        try {
            tmpJob = File.createTempFile("job", ".jar", null);
        } catch (IOException e) {
            throw new InvocationTargetException(e);
        }
        // generate
        JobScriptsManager manager = JobScriptsManagerFactory.createManagerInstance(
                JobScriptsManagerFactory.getDefaultExportChoiceMap(), IContext.DEFAULT, JobScriptsManager.LAUNCHER_ALL,
                IProcessor.NO_STATISTICS, IProcessor.NO_TRACES, JobExportType.OSGI);
        manager.setDestinationPath(tmpJob.getAbsolutePath());
        JobExportAction action = new JobExportAction(Collections.singletonList(node), jobVersion, bundleVersion, manager,
                System.getProperty("java.io.tmpdir"));
        action.run(monitor);
        if (!action.isBuildSuccessful()) {
            return;
        }

        // publish
        monitor.beginTask("Deploy to Artifact Repository....", IProgressMonitor.UNKNOWN);
        // http://jira.talendforge.org/browse/TESB-5426 LiXiaopeng
        try {
            ProcessItem processItem = (ProcessItem) node.getObject().getProperty().getItem();

            FeaturesModel featuresModel = new FeaturesModel(groupId, artifactName, artifactVersion);
            featuresModel.setConfigName(node.getObject().getLabel());
            featuresModel.setContexts(JobContextUtils.getContextsMap(processItem));
            BundleModel bundleModel = new BundleModel(groupId, artifactName, artifactVersion, tmpJob);
            featuresModel.addBundle(bundleModel);

            // [TESB-12036] add talend-data-mapper feature
			NodeType tHMapNode = EmfModelUtils.getComponentByName(processItem, THMAP_COMPONENT_NAME);
			if (tHMapNode != null) {
				featuresModel.addFeature(new FeatureModel(FeaturesModel.TALEND_DATA_MAPPER_FEATURE_NAME,
						FeaturesModel.ESB_FEATURE_VERSION_RANGE));
			}

			Collection<NodeType> tIPaasComponents = EmfModelUtils.getComponentsByName(processItem, "tiPaaSInput", "tiPaasOutput");
			if(!tIPaasComponents.isEmpty()){
				addMissingBundles(featuresModel, ((JobJavaScriptOSGIForESBManager) manager).getExcludedModuleNeededs());
			}

			process(processItem, featuresModel, monitor);
        } catch (IOException e) {
            throw new InvocationTargetException(e);
        } finally {
            if (tmpJob.exists()) {
                tmpJob.delete();
            }
        }
        monitor.done();
    }

    @SuppressWarnings("serial")
    private static final Map<String, BundleModel> BUNDLE_MAPPING = new HashMap<String, BundleModel>() {{
        put("org.apache.servicemix.bundles.dom4j", new BundleModel("org.apache.servicemix.bundles", "org.apache.servicemix.bundles.dom4j", "1.6.1_5"));
        put("org.apache.servicemix.bundles.jaxen", new BundleModel("org.apache.servicemix.bundles", "org.apache.servicemix.bundles.jaxen", "1.1.1_2"));
        put("org.apache.servicemix.bundles.wsdl4j", new BundleModel("org.apache.servicemix.bundles", "org.apache.servicemix.bundles.wsdl4j", "1.6.3_1"));
    }};

    private static void addMissingBundles(FeaturesModel featuresModel, Collection<ModuleNeeded> modules) {
        for (ModuleNeeded moduleNeeded : modules) {
            BundleModel bundleModel = BUNDLE_MAPPING.get(moduleNeeded.getBundleName());
            if (null != bundleModel) {
                featuresModel.addBundle(bundleModel);
            } else {
                // TODO: upload non-maven artifacts to repo; filter assuming (like log4j, activation, etc)
                // new BundleModel("provided_libs", "artifact", "version", studioLocation)
            }
        }
    }
}
