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
package org.talend.repository.ui.wizards.exportjob.handler;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.BooleanUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.talend.commons.CommonsPlugin;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.utils.workbench.resources.ResourceUtils;
import org.talend.core.CorePlugin;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.PluginChecker;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.properties.Project;
import org.talend.core.runtime.process.IBuildJobHandler;
import org.talend.core.runtime.process.ITalendProcessJavaProject;
import org.talend.core.runtime.process.LastGenerationInfo;
import org.talend.core.runtime.repository.build.BuildExportManager;
import org.talend.core.runtime.repository.build.IBuildParametes;
import org.talend.core.runtime.repository.build.IBuildResourceParametes;
import org.talend.core.runtime.repository.build.IBuildResourcesProvider;
import org.talend.core.runtime.util.ParametersUtil;
import org.talend.core.services.ICoreTisService;
import org.talend.designer.maven.model.TalendMavenConstants;
import org.talend.designer.runprocess.IProcessor;
import org.talend.designer.runprocess.IRunProcessService;
import org.talend.designer.runprocess.ProcessorUtilities;
import org.talend.repository.ProjectManager;
import org.talend.repository.ui.utils.Log4jPrefsSettingManager;
import org.talend.repository.ui.wizards.exportjob.scriptsmanager.JobScriptsManager.ExportChoice;

/**
 * created by ycbai on 2015年5月13日 Detailled comment
 *
 */
public abstract class AbstractBuildJobHandler implements IBuildJobHandler, IBuildResourceParametes, IBuildParametes {

    protected static final String PATH_SEPARATOR = "/"; //$NON-NLS-1$

    protected static final String COMMA = ","; //$NON-NLS-1$

    protected static final String SPACE = " "; //$NON-NLS-1$

    protected static final String NEGATION = "!"; //$NON-NLS-1$

    protected static final String JOB_EXTENSION = "zip"; //$NON-NLS-1$

    protected static final String JOB_NAME_SEP = "-"; //$NON-NLS-1$

    protected ProcessItem processItem;

    protected String version;

    protected String contextName;

    protected Map<ExportChoice, Object> exportChoice;

    protected ITalendProcessJavaProject talendProcessJavaProject;

    private boolean itemDependencies;

    private final Map<String, Object> argumentsMap = new HashMap<String, Object>();

    public AbstractBuildJobHandler(ProcessItem processItem, String version, String contextName,
            Map<ExportChoice, Object> exportChoiceMap) {
        this.processItem = processItem;
        this.version = version;
        this.contextName = contextName;
        if (exportChoiceMap != null) {
            this.exportChoice = exportChoiceMap;
        } else {
            this.exportChoice = new HashMap<ExportChoice, Object>();
        }
        IRunProcessService runProcessService = CorePlugin.getDefault().getRunProcessService();
        this.talendProcessJavaProject = runProcessService.getTalendJobJavaProject(processItem.getProperty());
        IFolder targetFolder = talendProcessJavaProject.getTargetFolder();
        try {
            ResourceUtils.emptyFolder(targetFolder);
        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
        }
    }

    @Override
    public Map<String, Object> getArguments() {
        return argumentsMap;
    }

    protected boolean isOptionChoosed(Object key) {
        if (key != null) {
            final Object object = exportChoice.get(key);
            if (object instanceof Boolean) {
                return BooleanUtils.isTrue((Boolean) object);
            }
        }
        return false;
    }

    public boolean hasItemDependencies() {
        return itemDependencies;
    }

    protected void setNeedItemDependencies(boolean itemDependencies) {
        this.itemDependencies = itemDependencies;
    }

    protected Project getProject(Item item) {
        Project project = ProjectManager.getInstance().getProject(item);
        return project;
    }

    protected boolean isLog4jEnable() {
        return Log4jPrefsSettingManager.getInstance().isLog4jEnable();
    }

    protected boolean needXmlMappings() {
        // based on the generate codes(ProcessorUtilities.generateCode)
        if (this.version == null) {
            return LastGenerationInfo.getInstance().isUseDynamic(processItem.getProperty().getId(),
                    processItem.getProperty().getVersion());
        }
        return LastGenerationInfo.getInstance().isUseDynamic(processItem.getProperty().getId(), this.version);
    }

    protected boolean needRules() {
        if (this.version == null) {
            return LastGenerationInfo.getInstance().isUseRules(processItem.getProperty().getId(),
                    processItem.getProperty().getVersion());
        }
        return LastGenerationInfo.getInstance().isUseRules(processItem.getProperty().getId(), this.version);
    }

    protected String getProgramArgs() {
        StringBuffer programArgs = new StringBuffer();
        StringBuffer profileArgs = getProfileArgs();
        StringBuffer otherArgs = getOtherArgs();
        if (profileArgs.length() > 0) {
            programArgs.append(profileArgs);
            programArgs.append(SPACE);
        }
        if (otherArgs.length() > 0) {
            programArgs.append(otherArgs);
        }
        return programArgs.toString();
    }

    protected StringBuffer getProfileArgs() {
        StringBuffer profileBuffer = new StringBuffer();
        String property = System.getProperty("maven.additional.params");
        if (property != null) {
            profileBuffer.append(SPACE);
            profileBuffer.append(property);
            profileBuffer.append(SPACE);
        }

        profileBuffer.append(TalendMavenConstants.PREFIX_PROFILE);
        profileBuffer.append(SPACE);

        // should add the default settings always.
        addArg(profileBuffer, true, true, TalendMavenConstants.PROFILE_DEFAULT_SETTING);

        addArg(profileBuffer, isOptionChoosed(ExportChoice.needSourceCode), TalendMavenConstants.PROFILE_INCLUDE_JAVA_SOURCES);
        // if not binaries, need add maven resources
        boolean isBinaries = isOptionChoosed(ExportChoice.binaries);
        addArg(profileBuffer, !isBinaries, TalendMavenConstants.PROFILE_INCLUDE_MAVEN_RESOURCES);
        addArg(profileBuffer, isOptionChoosed(ExportChoice.needJobItem) || itemDependencies,
                TalendMavenConstants.PROFILE_INCLUDE_ITEMS);

        // for binaries
        addArg(profileBuffer, isOptionChoosed(ExportChoice.includeLibs), TalendMavenConstants.PROFILE_INCLUDE_LIBS);
        addArg(profileBuffer, isBinaries, TalendMavenConstants.PROFILE_INCLUDE_BINARIES);

        // add log4j to running.
        addArg(profileBuffer, isLog4jEnable() && isBinaries, TalendMavenConstants.PROFILE_INCLUDE_RUNNING_LOG4J);
        // add log4j for resources.
        addArg(profileBuffer, isLog4jEnable() && !isBinaries, TalendMavenConstants.PROFILE_INCLUDE_LOG4J);

        // the running context is only useful, when binaries
        addArg(profileBuffer, isBinaries && isOptionChoosed(ExportChoice.needContext),
                TalendMavenConstants.PROFILE_INCLUDE_CONTEXTS);

        // for test
        addArg(profileBuffer, isOptionChoosed(ExportChoice.includeTestSource), TalendMavenConstants.PROFILE_INCLUDE_TEST_SOURCES);
        addArg(profileBuffer, isOptionChoosed(ExportChoice.executeTests), TalendMavenConstants.PROFILE_INCLUDE_TEST_REPORTS);

        // xmlMappings folders
        addArg(profileBuffer, needXmlMappings(), TalendMavenConstants.PROFILE_INCLUDE_XMLMAPPINGS);
        addArg(profileBuffer, needXmlMappings() && isBinaries, TalendMavenConstants.PROFILE_INCLUDE_RUNNING_XMLMAPPINGS);

        // If the map doesn't contain the assembly key, then take the default value activation from the POM.
        boolean isAssemblyNeeded = (exportChoice.get(ExportChoice.needAssembly) == null
                || isOptionChoosed(ExportChoice.needAssembly));
        addArg(profileBuffer, isAssemblyNeeded, TalendMavenConstants.PROFILE_PACKAGING_AND_ASSEMBLY);

        // rules
        if (needRules()) {
            addArg(profileBuffer, true, TalendMavenConstants.PROFILE_INCLUDE_RULES);
        }

        // always disable ci-builder from studio/commandline
        addArg(profileBuffer, false, TalendMavenConstants.PROFILE_CI_BUILDER);

        if (canSignJob()) {
            addArg(profileBuffer, true, TalendMavenConstants.PROFILE_SIGNATURE);
        }

        if (isOptionChoosed(ExportChoice.buildImage)) {
            addArg(profileBuffer, true, TalendMavenConstants.PROFILE_DOCKER);
        }

        return profileBuffer;
    }

    protected StringBuffer getOtherArgs() {
        StringBuffer otherArgsBuffer = new StringBuffer();

        if (isOptionChoosed(ExportChoice.buildImage)) {
            String dockerHost = (String) exportChoice.get(ExportChoice.dockerHost);
            if (dockerHost != null) {
                otherArgsBuffer.append("-Djkube.docker.host=" + dockerHost + " "); //$NON-NLS-1$ //$NON-NLS-2$
            }
            String imageName = (String) exportChoice.get(ExportChoice.imageName);
            if (imageName != null) {
                otherArgsBuffer.append("-Dtalend.docker.name=" + imageName + " "); //$NON-NLS-1$ //$NON-NLS-2$
            }
            String imageTag = (String) exportChoice.get(ExportChoice.imageTag);
            if (imageName != null) {
                otherArgsBuffer.append("-Dtalend.docker.tag=" + imageTag + " "); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }

        if (isOptionChoosed(ExportChoice.pushImage)) {
            String registry = (String) exportChoice.get(ExportChoice.pushRegistry);
            if (registry != null) {
                otherArgsBuffer.append("-Djkube.docker.push.registry=" + registry + " "); //$NON-NLS-1$ //$NON-NLS-2$
            }
            String username = (String) exportChoice.get(ExportChoice.registryUsername);
            if (username != null) {
                otherArgsBuffer.append("-Djkube.docker.username=" + username + " "); //$NON-NLS-1$ //$NON-NLS-2$
            }
            String password = (String) exportChoice.get(ExportChoice.registryPassword);
            if (password != null) {
                otherArgsBuffer.append("-Djkube.docker.password=" + password + " "); //$NON-NLS-1$ //$NON-NLS-2$
            }

        }

        if (!isOptionChoosed(ExportChoice.executeTests)) {
            otherArgsBuffer.append(TalendMavenConstants.ARG_SKIPTESTS);
        } else {
            otherArgsBuffer.append(TalendMavenConstants.ARG_TEST_FAILURE_IGNORE);
        }
        if (canSignJob()) {
            otherArgsBuffer.append(" " + TalendMavenConstants.ARG_LICENSE_PATH + "=\"" + getLicenseFile().getAbsolutePath() + "\"");
            otherArgsBuffer.append(" " + TalendMavenConstants.ARG_SESSION_ID + "=" + getSessionId());
        }
        otherArgsBuffer.append(" -Dmaven.main.skip=true"); //$NON-NLS-1$

        // if debug
        if (CommonsPlugin.isDebugMode()) {
            otherArgsBuffer.append(" -X"); //$NON-NLS-1$
        }
        return otherArgsBuffer;
    }

    protected void addArg(StringBuffer commandBuffer, boolean isFirst, boolean include, String arg) {
        if (!isFirst) {
            commandBuffer.append(COMMA);
        }
        if (!include) {
            commandBuffer.append(NEGATION);
        }
        commandBuffer.append(arg);
    }

    protected void addArg(StringBuffer commandBuffer, boolean include, String arg) {
        addArg(commandBuffer, false, include, arg);
    }

    public boolean canSignJob() {
        if (PluginChecker.isTIS() && getLicenseFile() != null && !isOptionChoosed(ExportChoice.buildImage)) {
            return true;
        }
        return false;
    }

    protected File getLicenseFile() {
        if (GlobalServiceRegister.getDefault().isServiceRegistered(ICoreTisService.class)) {
            ICoreTisService coreTisService = (ICoreTisService) GlobalServiceRegister.getDefault()
                    .getService(ICoreTisService.class);
            File licenseFile = coreTisService.getLicenseFile();
            if (licenseFile.exists() && !coreTisService.isLicenseExpired()) {
                return licenseFile;
            }
        }
        return null;
    }

    private String getSessionId() {
        if (GlobalServiceRegister.getDefault().isServiceRegistered(ICoreTisService.class)) {
            ICoreTisService coreTisService = (ICoreTisService) GlobalServiceRegister.getDefault()
                    .getService(ICoreTisService.class);
            return coreTisService.generateSignerSessionId();
        }
        return null;
    }

    @Override
    public IFolder getTargetFolder() {
        if (talendProcessJavaProject == null) {
            return null;
        }
        return talendProcessJavaProject.getTargetFolder();
    }

    @Override
    public IFile getJobTargetFile() {
        IFolder targetFolder = getTargetFolder();
        IFile jobFile = null;
        try {
            targetFolder.refreshLocal(IResource.DEPTH_ONE, null);
            // we only build one zip at a time, so just get the zip file to be able to manage some pom customizations.
            for (IResource resource : targetFolder.members()) {
                if (resource instanceof IFile) {
                    IFile file = (IFile) resource;
                    if (ProcessorUtilities.isExportJobAsMicroService()) {
                        if ("jar".equals(file.getFileExtension())) {
                            jobFile = file;
                            break;
                        }
                    } else {
                        if (JOB_EXTENSION.equals(file.getFileExtension())) {
                            jobFile = file;
                            break;
                        }
                    }
                }
            }
        } catch (CoreException e) {
            ExceptionHandler.process(e);
        }
        return jobFile;
    }

    @Override
    public void prepare(IProgressMonitor monitor, Map<String, Object> parameters) throws Exception {
        if (parameters == null) {
            parameters = new HashMap<String, Object>();
        }
        parameters.put(OBJ_PROCESS_ITEM, processItem);
        parameters.put(VERSION, version);
        parameters.put(OBJ_PROCESS_JAVA_PROJECT, talendProcessJavaProject);

        // generate sources
        IProcessor processor = generateJobFiles(monitor);
        parameters.put(OBJ_ITEM_DEPENDENCIES, processor.getBuildChildrenJobs());

        // export items
        if (ParametersUtil.hasBoolFlag(parameters, OPTION_ITEMS)) {
            final boolean withDependencies = ParametersUtil.hasBoolFlag(parameters, OPTION_ITEMS_DEPENDENCIES);
            generateItemFiles(withDependencies, monitor);
        }

        final IBuildResourcesProvider[] resourcesProviders = BuildExportManager.getInstance().getResourcesProviders();
        for (IBuildResourcesProvider provider : resourcesProviders) {
            provider.prepare(monitor, parameters);
        }
    }

}
