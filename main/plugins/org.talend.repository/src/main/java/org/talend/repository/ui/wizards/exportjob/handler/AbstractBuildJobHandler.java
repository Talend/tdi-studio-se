// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.BooleanUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.talend.commons.CommonsPlugin;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.utils.workbench.resources.ResourceUtils;
import org.talend.core.CorePlugin;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.ICoreService;
import org.talend.core.ITDQSurvivorshipService;
import org.talend.core.model.process.ProcessUtils;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.properties.Project;
import org.talend.core.runtime.process.IBuildJobHandler;
import org.talend.core.runtime.process.ITalendProcessJavaProject;
import org.talend.core.runtime.process.LastGenerationInfo;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.maven.model.TalendMavenConstants;
import org.talend.designer.runprocess.IRunProcessService;
import org.talend.repository.ProjectManager;
import org.talend.repository.ui.utils.Log4jPrefsSettingManager;
import org.talend.repository.ui.wizards.exportjob.scriptsmanager.JobScriptsManager.ExportChoice;

/**
 * created by ycbai on 2015年5月13日 Detailled comment
 *
 */
public abstract class AbstractBuildJobHandler implements IBuildJobHandler {

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
        this.talendProcessJavaProject = runProcessService.getTalendProcessJavaProject();
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

    protected boolean needPigUDFs() {
        if (this.version == null) {
            return LastGenerationInfo.getInstance().isUsePigUDFs(processItem.getProperty().getId(),
                    processItem.getProperty().getVersion());
        }
        return LastGenerationInfo.getInstance().isUsePigUDFs(processItem.getProperty().getId(), this.version);
    }

    protected boolean needDQSurvivorshipRules() {
        // when needJobItem or itemDependencies is true, the survivorship rules will be included.so return false here.
        if (isOptionChoosed(ExportChoice.needJobItem) || itemDependencies) {
            return false;
        }

        Collection<NodeType> survivorshipNodes = null;
        if (GlobalServiceRegister.getDefault().isServiceRegistered(ITDQSurvivorshipService.class)) {
            ITDQSurvivorshipService tdqSurvShipService = (ITDQSurvivorshipService) GlobalServiceRegister.getDefault().getService(
                    ITDQSurvivorshipService.class);
            if (tdqSurvShipService != null) {
                survivorshipNodes = tdqSurvShipService.getSurvivorshipNodesOfProcess(processItem);
            }
        }
        if (survivorshipNodes != null && !survivorshipNodes.isEmpty()) {
            return true;
        }
        return false;
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
        profileBuffer.append(TalendMavenConstants.PREFIX_THREAD);
        profileBuffer.append(SPACE);
        profileBuffer.append(TalendMavenConstants.PREFIX_THREAD_VALUE);
        profileBuffer.append(SPACE);
        profileBuffer.append(TalendMavenConstants.PREFIX_PROFILE);
        profileBuffer.append(SPACE);

        // should add the default settings always.
        addArg(profileBuffer, true, true, TalendMavenConstants.PROFILE_DEFAULT_SETTING);

        addArg(profileBuffer, isOptionChoosed(ExportChoice.needSourceCode), TalendMavenConstants.PROFILE_INCLUDE_JAVA_SOURCES);
        // if not binaries, need add maven resources
        boolean isBinaries = isOptionChoosed(ExportChoice.binaries);
        addArg(profileBuffer, !isBinaries, TalendMavenConstants.PROFILE_INCLUDE_MAVEN_RESOURCES);
        // DQ survivorship rules when items not exported.
        if (needDQSurvivorshipRules()) {
            addArg(profileBuffer, true, TalendMavenConstants.PROFILE_INCLUDE_SURVIVORSHIP_RULES);
        }
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
                TalendMavenConstants.PROFILE_INCLUDE_CONTEXTS, ProcessUtils.jarNeedsToContainContext());

        // for test
        addArg(profileBuffer, isOptionChoosed(ExportChoice.includeTestSource), TalendMavenConstants.PROFILE_INCLUDE_TEST_SOURCES);
        addArg(profileBuffer, isOptionChoosed(ExportChoice.executeTests), TalendMavenConstants.PROFILE_INCLUDE_TEST_REPORTS);

        // xmlMappings folders
        addArg(profileBuffer, needXmlMappings(), TalendMavenConstants.PROFILE_INCLUDE_XMLMAPPINGS);
        addArg(profileBuffer, needXmlMappings() && isBinaries, TalendMavenConstants.PROFILE_INCLUDE_RUNNING_XMLMAPPINGS);
        if (needXmlMappings()) {
            if (GlobalServiceRegister.getDefault().isServiceRegistered(ICoreService.class)) {
                ICoreService coreService = (ICoreService) GlobalServiceRegister.getDefault().getService(ICoreService.class);
                coreService.synchronizeMapptingXML();
                coreService.syncLog4jSettings();
            }
        }
        // If the map doesn't contain the assembly key, then take the default value activation from the POM.
        boolean isAssemblyNeeded = exportChoice.get(ExportChoice.needAssembly) == null
                || isOptionChoosed(ExportChoice.needAssembly);
        addArg(profileBuffer, isAssemblyNeeded, TalendMavenConstants.PROFILE_PACKAGING_AND_ASSEMBLY);

        // rules
        if (needRules()) {
            addArg(profileBuffer, true, TalendMavenConstants.PROFILE_INCLUDE_RULES);
        }
        // pigudfs
        if (needPigUDFs()) {
            addArg(profileBuffer, isBinaries, TalendMavenConstants.PROFILE_INCLUDE_PIGUDFS_BINARIES);
            addArg(profileBuffer, !isBinaries, TalendMavenConstants.PROFILE_INCLUDE_PIGUDFS_JAVA_SOURCES);
        }
        return profileBuffer;
    }

    protected StringBuffer getOtherArgs() {
        StringBuffer otherArgsBuffer = new StringBuffer();

        if (!isOptionChoosed(ExportChoice.executeTests)) {
            otherArgsBuffer.append(TalendMavenConstants.ARG_SKIPTESTS);
        } else {
            otherArgsBuffer.append("-fn");
        }
        otherArgsBuffer.append(" -Dmaven.main.skip=true");

        // if debug
        if (CommonsPlugin.isDebugMode()) {
            otherArgsBuffer.append(" -X");
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

    private void addArg(StringBuffer commandBuffer, boolean include, String arg, boolean isHD) {
        if (isHD) {
            commandBuffer.append(COMMA);
            commandBuffer.append(arg);
        } else {
            addArg(commandBuffer, false, include, arg);
        }
    }

    @Override
    public IFile getJobTargetFile() {
        if (talendProcessJavaProject == null) {
            return null;
        }
        IFolder targetFolder = talendProcessJavaProject.getTargetFolder();
        IFile jobFile = null;
        try {
            targetFolder.refreshLocal(IResource.DEPTH_ONE, null);
            // we only build one zip at a time, so just get the zip file to be able to manage some pom customizations.
            for (IResource resource : targetFolder.members()) {
                if (resource instanceof IFile) {
                    IFile file = (IFile) resource;
                    if (JOB_EXTENSION.equals(file.getFileExtension())) {
                        jobFile = file;
                        break;
                    }
                }
            }
        } catch (CoreException e) {
            ExceptionHandler.process(e);
        }
        return jobFile;
    }

}
