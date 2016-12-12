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

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.utils.generation.JavaUtils;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.ITDQItemService;
import org.talend.core.PluginChecker;
import org.talend.core.model.process.ProcessUtils;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.properties.Project;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.utils.JavaResourcesHelper;
import org.talend.core.repository.constants.FileConstants;
import org.talend.core.runtime.process.LastGenerationInfo;
import org.talend.core.runtime.process.TalendProcessArgumentConstant;
import org.talend.core.runtime.repository.build.BuildExportManager;
import org.talend.core.service.ITransformService;
import org.talend.core.ui.ITestContainerProviderService;
import org.talend.designer.maven.model.TalendMavenConstants;
import org.talend.designer.runprocess.IProcessor;
import org.talend.designer.runprocess.ProcessorUtilities;
import org.talend.model.bridge.ReponsitoryContextBridge;
import org.talend.repository.documentation.ExportFileResource;
import org.talend.repository.local.ExportItemUtil;
import org.talend.repository.model.RepositoryConstants;
import org.talend.repository.ui.wizards.exportjob.scriptsmanager.JobScriptsManager.ExportChoice;
import org.talend.utils.io.FilesUtils;

/**
 * created by ycbai on 2015年5月13日 Detailled comment
 *
 */
public class BuildJobHandler extends AbstractBuildJobHandler {

    private boolean projectNameLowerCase;

    public BuildJobHandler(ProcessItem processItem, String version, String contextName, Map<ExportChoice, Object> exportChoiceMap) {
        super(processItem, version, contextName, exportChoiceMap);

        setProjectNameLowerCase(true);
        ProcessorUtilities.setExportConfig(JavaUtils.JAVA_APP_NAME, null, null);
    }

    public boolean isProjectNameLowerCase() {
        return projectNameLowerCase;
    }

    public void setProjectNameLowerCase(boolean projectNameLowerCase) {
        this.projectNameLowerCase = projectNameLowerCase;
    }

    @Override
    public IProcessor generateJobFiles(IProgressMonitor monitor) throws Exception {
        LastGenerationInfo.getInstance().getUseDynamicMap().clear();
        LastGenerationInfo.getInstance().getUseRulesMap().clear();

        final Map<String, Object> argumentsMap = new HashMap<String, Object>();

        argumentsMap.put(TalendProcessArgumentConstant.ARG_ENABLE_APPLY_CONTEXT_TO_CHILDREN,
                isOptionChoosed(ExportChoice.applyToChildren));
        //
        argumentsMap.put(TalendProcessArgumentConstant.ARG_ENABLE_STATS, isOptionChoosed(ExportChoice.addStatistics));
        argumentsMap.put(TalendProcessArgumentConstant.ARG_ENABLE_TRACS, isOptionChoosed(ExportChoice.addTracs));
        Properties prop = (Properties) exportChoice.get(ExportChoice.properties);
        if (prop != null) { // add all properties for arugment map.
            Enumeration<Object> keys = prop.keys();
            while (keys.hasMoreElements()) {
                String key = keys.nextElement().toString();
                String value = prop.get(key) == null ? null : prop.get(key).toString();
                argumentsMap.put(key, value);
            }
        }
        // context
        boolean needContext = isOptionChoosed(ExportChoice.needContext);
        if (needContext) {
            argumentsMap.put(TalendProcessArgumentConstant.ARG_NEED_CONTEXT, needContext);
            Object context = this.exportChoice.get(ExportChoice.contextName);
            if (context == null) {
                context = this.contextName;
            }
            argumentsMap.put(TalendProcessArgumentConstant.ARG_CONTEXT_NAME, context);
        }
        boolean onlyDefaultContext = isOptionChoosed(ExportChoice.onlyDefautContext);
        if (onlyDefaultContext) {
            argumentsMap.put(TalendProcessArgumentConstant.ARG_ONLY_DEFAUT_CONTEXT, onlyDefaultContext);
        }

        boolean needParamValues = isOptionChoosed(ExportChoice.needParameterValues);
        if (needParamValues) {
            argumentsMap.put(TalendProcessArgumentConstant.ARG_CONTEXT_PARAMS,
                    this.exportChoice.get(ExportChoice.parameterValuesList));
        }
        // log4j
        boolean log4jEnable = isLog4jEnable();
        argumentsMap.put(TalendProcessArgumentConstant.ARG_ENABLE_LOG4J, log4jEnable);
        if (log4jEnable) {
            boolean needLog4jLevel = isOptionChoosed(ExportChoice.needLog4jLevel);
            if (needLog4jLevel) {
                argumentsMap.put(TalendProcessArgumentConstant.ARG_NEED_LOG4J_LEVEL, needLog4jLevel);
                argumentsMap.put(TalendProcessArgumentConstant.ARG_LOG4J_LEVEL, this.exportChoice.get(ExportChoice.log4jLevel));
            }
        }

        // generation option
        int generationOption = (isOptionChoosed(ExportChoice.includeTestSource) || isOptionChoosed(ExportChoice.executeTests)) ? ProcessorUtilities.GENERATE_ALL_CHILDS
                | ProcessorUtilities.GENERATE_TESTS
                : ProcessorUtilities.GENERATE_ALL_CHILDS;
        if (isOptionChoosed(ExportChoice.doNotCompileCode)) {
            generationOption = generationOption | ProcessorUtilities.GENERATE_WITHOUT_COMPILING;
        }
        argumentsMap.put(TalendProcessArgumentConstant.ARG_GENERATE_OPTION, generationOption);
        
        //deployVersion for ci builder
        String deployVersion = (String) exportChoice.get(ExportChoice.deployVersion);
        if (deployVersion != null) {
            argumentsMap.put(TalendProcessArgumentConstant.ARG_DEPLOY_VERSION, deployVersion);
        }

        IProcessor processor = ProcessorUtilities.generateCode(processItem, contextName, version, argumentsMap, monitor);
        ProcessorUtilities.resetExportConfig();
        return processor;
    }

    @Override
    public void generateTestReports(IProgressMonitor monitor) throws Exception {
        if (!isOptionChoosed(ExportChoice.includeTestSource)) {
            return;
        }
        if (GlobalServiceRegister.getDefault().isServiceRegistered(ITestContainerProviderService.class)) {
            ITestContainerProviderService testContainerService = (ITestContainerProviderService) GlobalServiceRegister
                    .getDefault().getService(ITestContainerProviderService.class);
            if (testContainerService != null) {
                List<IFile> reports = new ArrayList<IFile>();
                List<ProcessItem> testsItems = testContainerService.getAllTestContainers(processItem);
                for (ProcessItem testItem : testsItems) {
                    List<IFile> testReportFiles = testContainerService.getTestReportFiles(testItem);
                    if (testReportFiles.size() > 0) {
                        reports.add(testReportFiles.get(0));
                    }
                }
                IFolder testsFolder = talendProcessJavaProject.getTestsFolder();
                talendProcessJavaProject.cleanFolder(monitor, testsFolder);
                IFolder parentFolder = talendProcessJavaProject.createSubFolder(monitor, testsFolder, processItem.getProperty()
                        .getLabel());
                for (IFile report : reports) {
                    report.copy(parentFolder.getFile(report.getName()).getFullPath(), true, monitor);
                }
            }
        }
    }

    @Override
    public void generateItemFiles(boolean withDependencies, IProgressMonitor monitor) throws Exception {
        IFolder baseFolder = talendProcessJavaProject.getItemsFolder();
        baseFolder = baseFolder.getFolder(JavaResourcesHelper.getJobFolderName(processItem.getProperty().getLabel(), processItem
                .getProperty().getVersion()));
        if (baseFolder.exists()) {
            talendProcessJavaProject.cleanFolder(monitor, baseFolder);
        } else {
            baseFolder.create(true, true, null);
        }
        IFolder itemsFolder = baseFolder.getFolder("items");//$NON-NLS-1$
        itemsFolder.create(true, true, monitor);
        List<Item> items = new ArrayList<Item>();
        items.add(processItem);
        ExportItemUtil exportItemUtil = new ExportItemUtil();
        if (withDependencies) {
            Collection<IRepositoryViewObject> allProcessDependencies = ProcessUtils.getAllProcessDependencies(items);
            for (IRepositoryViewObject repositoryObject : allProcessDependencies) {
                items.add(repositoryObject.getProperty().getItem());
            }
        }
        if(isOptionChoosed(ExportChoice.executeTests)){
        	 IPath path = talendProcessJavaProject.getTargetFolder().getFolder("surefire-reports").getLocation();
             File reports = new File(path.toOSString());
             if (reports.exists()) {
            	 FilesUtils.deleteFolder(reports, false);
             }
        }
        if (isOptionChoosed(ExportChoice.needJobItem)) {
            File destination = new File(itemsFolder.getLocation().toFile().getAbsolutePath());
            exportItemUtil.setProjectNameAsLowerCase(isProjectNameLowerCase());
            exportItemUtil.exportItems(destination, new ArrayList<Item>(items), false, new NullProgressMonitor());
        }
        addDQDependencies(itemsFolder, items);
        addTDMDependencies(itemsFolder, items);
    }

    /**
     * DOC nrousseau Comment method "addTDMDependencies".
     * 
     * @param items
     * @param itemsFolder
     */
    private void addTDMDependencies(IFolder itemsFolder, List<Item> items) {
        ITransformService tdmService = null;
        if (GlobalServiceRegister.getDefault().isServiceRegistered(ITransformService.class)) {
            tdmService = (ITransformService) GlobalServiceRegister.getDefault().getService(ITransformService.class);
        }
        if (tdmService == null) {
            return;
        }
        try {
            // add __tdm dependencies
            ExportFileResource resouece = new ExportFileResource();
            BuildExportManager.getInstance().exportDependencies(resouece, processItem);
            if (!resouece.getAllResources().isEmpty()) {
                final Iterator<String> relativepath = resouece.getRelativePathList().iterator();
                while (relativepath.hasNext()) {
                    String relativePath = relativepath.next();
                    Set<URL> sources = resouece.getResourcesByRelativePath(relativePath);
                    for (URL sourceUrl : sources) {
                        File currentResource = new File(org.talend.commons.utils.io.FilesUtils.getFileRealPath(sourceUrl
                                .getPath()));
                        if (currentResource.exists()) {
                            // the __tdm will be out of items folder, same level for items
                            IFolder targetFolder = ((IFolder) itemsFolder.getParent()).getFolder(relativePath);
                            if (!targetFolder.exists()) {
                                targetFolder.create(true, true, new NullProgressMonitor());
                            }
                            FilesUtils.copyFile(currentResource, new File(targetFolder.getLocation().toPortableString()
                                    + File.separator + currentResource.getName()));
                        }
                    }
                }

            }

            itemsFolder.refreshLocal(IResource.DEPTH_INFINITE, null);

            // add .settings/com.oaklandsw.base.projectProps for tdm, it should be added via ExportItemUtil, here just
            // make sure to export again.
            for (Item item : items) {
                if (tdmService.isTransformItem(item)) {
                    setNeedItemDependencies(true);
                    String itemProjectFolder = getProject(item).getTechnicalLabel();
                    if (isProjectNameLowerCase()) {// should be same as ExportItemUtil.getProjectOutputPath
                        itemProjectFolder = itemProjectFolder.toLowerCase();
                    }
                    IPath targetSettingPath = new Path(itemProjectFolder).append(RepositoryConstants.SETTING_DIRECTORY);
                    IFolder targetSettingsFolder = talendProcessJavaProject.createSubFolder(null, itemsFolder,
                            targetSettingPath.toString());

                    IProject sourceProject = getCorrespondingProjectRootFolder(item);

                    if (sourceProject.exists()) {
                        IFile targetTdmPropsFile = targetSettingsFolder.getFile(FileConstants.TDM_PROPS);
                        IFile sourceTdmPropsFile = sourceProject.getFolder(RepositoryConstants.SETTING_DIRECTORY).getFile(
                                FileConstants.TDM_PROPS);

                        // if have existed, no need copy again.
                        if (sourceTdmPropsFile.exists() && !targetTdmPropsFile.exists()) {
                            sourceTdmPropsFile.copy(targetTdmPropsFile.getFullPath(), true, null);
                        }
                    }
                    break; // only deal with one time.
                }
            }
        } catch (Exception e) {
            // don't block all the export if got exception here
            ExceptionHandler.process(e);
        }
    }

    protected IProject getCorrespondingProjectRootFolder(Item item) throws CoreException {
        // for bug 17685
        org.talend.core.model.properties.Project p = getProject(item);
        IProject project = null;
        if (p != null) {
            project = ResourcesPlugin.getWorkspace().getRoot().getProject(p.getTechnicalLabel().toUpperCase());
            if (project != null) {
                return project;
            }
        }
        return null;
    }

    private void addDQDependencies(IFolder parentFolder, List<Item> items) throws Exception {
        if (GlobalServiceRegister.getDefault().isServiceRegistered(ITDQItemService.class)) {
            ITDQItemService tdqItemService = (ITDQItemService) GlobalServiceRegister.getDefault().getService(
                    ITDQItemService.class);
            for (Item item : items) {
                if (tdqItemService != null && tdqItemService.hasProcessItemDependencies(Arrays.asList(new Item[] { item }))) {
                    setNeedItemDependencies(true);
                    // add .Talend.definition file
                    String defIdxFolderName = "TDQ_Libraries"; //$NON-NLS-1$
                    String defIdxFileName = ".Talend.definition"; //$NON-NLS-1$
                    Project pro = getProject(processItem);
                    IFolder itemsProjectFolder = parentFolder.getFolder(pro.getTechnicalLabel().toLowerCase());
                    File itemsFolderDir = new File(parentFolder.getLocation().toFile().getAbsolutePath());
                    IProject project = ReponsitoryContextBridge.getRootProject();
                    String defIdxRelativePath = defIdxFolderName + PATH_SEPARATOR + defIdxFileName;
                    IFile defIdxFile = project.getFile(defIdxRelativePath);
                    if (defIdxFile.exists()) {
                        File defIdxFileSource = new File(project.getLocation().makeAbsolute().append(defIdxFolderName)
                                .append(defIdxFileName).toFile().toURI());
                        File defIdxFileTarget = new File(itemsProjectFolder.getFile(defIdxRelativePath).getLocation().toFile()
                                .getAbsolutePath());
                        FilesUtils.copyFile(defIdxFileSource, defIdxFileTarget);
                    }
                    // add report header image & template files
                    String reportingBundlePath = PluginChecker.getBundlePath("org.talend.dataquality.reporting"); //$NON-NLS-1$
                    File imageFolder = new File(reportingBundlePath + PATH_SEPARATOR + "images"); //$NON-NLS-1$
                    if (imageFolder.exists()) {
                        FilesUtils.copyDirectory(imageFolder, itemsFolderDir);
                    }
                    File templateFolder = new File(reportingBundlePath + PATH_SEPARATOR + "reports"); //$NON-NLS-1$ 
                    if (templateFolder.exists() && templateFolder.isDirectory()) {
                        FilesUtils.copyDirectory(templateFolder, itemsFolderDir);
                    }
                }
            }
            // TDQ-12474 copy the "metadata/survivorship/rulePackage" to ".Java.src.main.resources". so that it will be used by
            // maven command 'include-survivorship-rules' to export.
            if (!isOptionChoosed(ExportChoice.needJobItem)) {
                ExportFileResource resouece = new ExportFileResource();
                BuildExportManager.getInstance().exportDependencies(resouece, processItem);
                if (!resouece.getAllResources().isEmpty()) {
                    final Iterator<String> relativepath = resouece.getRelativePathList().iterator();
                    String pathStr = "metadata/survivorship"; //$NON-NLS-1$
                    IFolder targetFolder = talendProcessJavaProject.getResourcesFolder();
                    if (targetFolder.exists()) {
                        IFolder survFolder = targetFolder.getFolder(new Path(pathStr));
                        // only copy self job rules, clear the 'survivorship' folder before copy.
                        if (survFolder.exists()) {
                            survFolder.delete(true, null);
                        }
                        while (relativepath.hasNext()) {
                            String relativePath = relativepath.next();
                            Set<URL> sources = resouece.getResourcesByRelativePath(relativePath);
                            for (URL sourceUrl : sources) {
                                File currentResource = new File(org.talend.commons.utils.io.FilesUtils.getFileRealPath(sourceUrl
                                        .getPath()));
                                if (currentResource.exists()) {
                                    FilesUtils.copyDirectory(currentResource, new File(targetFolder.getLocation()
                                            .toPortableString() + File.separator + pathStr));
                                }
                            }
                        }

                    }
                }
                parentFolder.refreshLocal(IResource.DEPTH_INFINITE, null);

            }
        }
    }

    @Override
    public void build(IProgressMonitor monitor) throws Exception {
        final IWorkspaceRunnable runnable = new IWorkspaceRunnable() {

            @Override
            public void run(IProgressMonitor monitor) throws CoreException {
                try {
                    buildDelegate(monitor);
                } catch (Exception e) {
                    ExceptionHandler.process(e);
                }
            }
        };

        try {
            IWorkspace workspace = ResourcesPlugin.getWorkspace();
            workspace.run(runnable, workspace.getRoot(), IWorkspace.AVOID_UPDATE, monitor);
        } catch (CoreException e) {
            throw new InvocationTargetException(e);
        }

    }

    protected void buildDelegate(IProgressMonitor monitor) throws Exception {
        final Map<String, Object> argumentsMap = new HashMap<String, Object>();
        argumentsMap.put(TalendProcessArgumentConstant.ARG_GOAL, TalendMavenConstants.GOAL_PACKAGE);
        argumentsMap.put(TalendProcessArgumentConstant.ARG_PROGRAM_ARGUMENTS, getProgramArgs());

        talendProcessJavaProject.buildModules(monitor, null, argumentsMap);
    }
}
