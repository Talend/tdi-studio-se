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

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.map.MultiKeyMap;
import org.apache.commons.lang.ArrayUtils;
import org.dom4j.Comment;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.talend.commons.CommonsPlugin;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.runtime.xml.XmlUtil;
import org.talend.commons.utils.generation.JavaUtils;
import org.talend.commons.utils.io.FilesUtils;
import org.talend.commons.utils.workbench.resources.ResourceUtils;
import org.talend.core.CorePlugin;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.ICoreService;
import org.talend.core.ILibraryManagerService;
import org.talend.core.PluginChecker;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.model.general.ModuleNeeded;
import org.talend.core.model.process.IContext;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.process.JobInfo;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.properties.Project;
import org.talend.core.model.properties.Property;
import org.talend.core.model.properties.RoutineItem;
import org.talend.core.model.properties.RulesItem;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryPrefConstants;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.routines.RoutinesUtil;
import org.talend.core.model.utils.JavaResourcesHelper;
import org.talend.core.repository.constants.FileConstants;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.repository.utils.Log4jUtil;
import org.talend.core.repository.utils.URIHelper;
import org.talend.core.runtime.CoreRuntimePlugin;
import org.talend.core.runtime.process.ITalendProcessJavaProject;
import org.talend.core.runtime.process.LastGenerationInfo;
import org.talend.core.runtime.process.TalendProcessArgumentConstant;
import org.talend.core.runtime.process.TalendProcessOptionConstants;
import org.talend.core.ui.CoreUIPlugin;
import org.talend.core.ui.services.IDesignerCoreUIService;
import org.talend.core.ui.services.IRulesProviderService;
import org.talend.designer.core.ICamelDesignerCoreService;
import org.talend.designer.core.IDesignerCoreService;
import org.talend.designer.core.model.utils.emf.component.IMPORTType;
import org.talend.designer.core.model.utils.emf.talendfile.ContextType;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.impl.ProcessTypeImpl;
import org.talend.designer.maven.utils.PomIdsHelper;
import org.talend.designer.runprocess.IProcessor;
import org.talend.designer.runprocess.IRunProcessService;
import org.talend.designer.runprocess.ItemCacheManager;
import org.talend.designer.runprocess.ProcessorException;
import org.talend.designer.runprocess.ProcessorUtilities;
import org.talend.repository.ProjectManager;
import org.talend.repository.RepositoryPlugin;
import org.talend.repository.constants.Log4jPrefsConstants;
import org.talend.repository.documentation.ExportFileResource;
import org.talend.repository.i18n.Messages;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.ui.utils.Log4jPrefsSettingManager;
import org.talend.repository.ui.utils.UpdateLog4jJarUtils;
import org.talend.repository.utils.EmfModelUtils;
import org.talend.repository.utils.EsbConfigUtils;
import org.talend.resources.util.EMavenBuildScriptProperties;

/**
 * Manages the job scripts to be exported. <br/>
 *
 * $Id: JobScriptsManager.java 1 2006-12-14 下�?�05:06:49 bqian
 *
 */
public class JobJavaScriptsManager extends JobScriptsManager {

    public JobJavaScriptsManager(Map<ExportChoice, Object> exportChoiceMap, String contextName, String launcher,
            int statisticPort, int tracePort) {
        super(exportChoiceMap, contextName, launcher, statisticPort, tracePort);
    }

    protected static final String USER_BEANS_PATH = JavaUtils.JAVA_BEANS_DIRECTORY;

    protected static final String USER_ROUTINES_PATH = JavaUtils.JAVA_ROUTINES_DIRECTORY;

    private static final String USER_PIGUDF_PATH = JavaUtils.JAVA_PIGUDF_DIRECTORY;

    protected static final String SYSTEM_ROUTINES_PATH = JavaUtils.JAVA_ROUTINES_DIRECTORY + PATH_SEPARATOR
            + JavaUtils.JAVA_SYSTEM_DIRECTORY;

    public static final String SYSTEMROUTINE_JAR = JavaUtils.SYSTEM_ROUTINE_JAR;

    public static final String USERROUTINE_JAR = JavaUtils.USER_ROUTINE_JAR;

    protected static final String USERBEANS_JAR = JavaUtils.USER_BEANS_JAR;

    public static final String USERPIGUDF_JAR = JavaUtils.USER_PIGUDF_JAR;

    private boolean needMappingInSystemRoutine = false;

    private MultiKeyMap compiledModules = new MultiKeyMap();

    private MultiKeyMap excludedModules = new MultiKeyMap();

    private List<String> mavenModules = new ArrayList<String>();

    private ExportFileResource[] exportFileResource;

    public static final String PLUGIN_ID = "org.talend.libraries.apache.storm"; //$NON-NLS-1$

    /**
     * Getter for compiledModules.
     *
     * @return the compiledModules
     */
    protected MultiKeyMap getCompiledModules() {
        return this.compiledModules;
    }

    /**
     * Getter for excludedModules.
     *
     * @return the excludedModules
     */
    protected MultiKeyMap getExcludedModules() {
        return this.excludedModules;
    }

    protected List<String> getMavenModules() {
        return this.mavenModules;
    }

    protected Set<ModuleNeeded> getCompiledModuleNeededs() {
        return getModuleNeededs(getCompiledModules());
    }

    @SuppressWarnings("rawtypes")
    protected Set<ModuleNeeded> getModuleNeededs(MultiKeyMap map) {
        Set<ModuleNeeded> modulesSet = new HashSet<ModuleNeeded>(100);
        Collection allModules = map.values();
        for (Object obj : allModules) {
            if (obj instanceof ModuleNeeded) {
                modulesSet.add((ModuleNeeded) obj);
            } else if (obj instanceof Set) {
                Set set = (Set) obj;
                if (!set.isEmpty()) {
                    modulesSet.addAll(set);
                }
            }
        }
        return modulesSet;
    }

    protected Set<String> getCompiledModuleNames() {
        return getCompiledModuleNames(false);
    }

    protected Set<String> getCompiledModuleNames(boolean isSpecialMR) {
        Set<String> compiledModulesSet = new HashSet<String>(100);

        for (ModuleNeeded module : getCompiledModuleNeededs()) {
            if ((isSpecialMR && module.isMrRequired()) || !isSpecialMR) {
                compiledModulesSet.add(module.getModuleName());
            }
        }
        return compiledModulesSet;
    }

    protected Set<ModuleNeeded> getExcludedModuleNeededs() {
        return getModuleNeededs(getExcludedModules());
    }

    protected Set<String> getExcludedModuleNames() {
        Set<String> providedModulesSet = new HashSet<String>(100);

        for (ModuleNeeded module : getExcludedModuleNeededs()) {
            providedModulesSet.add(module.getModuleName());
        }
        return providedModulesSet;
    }

    protected void analysisModules(String processId, String processVersion) {
        Set<ModuleNeeded> neededModules = LastGenerationInfo.getInstance().getModulesNeededWithSubjobPerJob(processId,
                processVersion);
        for (ModuleNeeded module : neededModules) {
            if (isCompiledLib(module)) {
                addModuleNeededsInMap(getCompiledModules(), processId, processVersion, module);

                // } else if (isExcludedLib(module)) { //FIXME no need, all are in "else"
                // addNameofModules(getExcludedModules(), processId, processVersion, module);
            } else {
                addModuleNeededsInMap(getExcludedModules(), processId, processVersion, module);
            }
        }
    }

    protected void analysisMavenModule(Item item) {
        // will collect the modules for maven pom
    }

    protected void addModuleNeededsInMap(MultiKeyMap modulesMap, String processId, String processVersion, ModuleNeeded module) {
        if (modulesMap != null && module != null) {
            Set<ModuleNeeded> modulesSet = (Set<ModuleNeeded>) modulesMap.get(processId, processVersion);
            if (modulesSet == null) {
                modulesSet = new LinkedHashSet<ModuleNeeded>(50);
                modulesMap.put(processId, processVersion, modulesSet);
            }
            modulesSet.add(module);
        }
    }

    /**
     * DOC informix Comment method "posExportResource".
     *
     * @param process
     * @param exportChoice
     * @param contextName
     * @param launcher
     * @param statisticPort
     * @param tracePort
     * @param i
     * @param processItem
     * @param selectedJobVersion
     * @param resources
     * @param codeOptions
     * @return
     */
    protected List<URL> posExportResource(ExportFileResource[] process, Map<ExportChoice, Object> exportChoice,
            String contextName, String launcher, int statisticPort, int tracePort, int i, IProcess jobProcess,
            ProcessItem processItem, String selectedJobVersion, List<URL> resources, String... codeOptions) {
        resources.addAll(getLauncher(isOptionChoosed(ExportChoice.needLauncher),
                isOptionChoosed(ExportChoice.needParameterValues), isOptionChoosed(ExportChoice.needContext), jobProcess,
                processItem, escapeSpace(contextName), escapeSpace(launcher), statisticPort, tracePort, codeOptions));

        addSourceCode(process, processItem, isOptionChoosed(ExportChoice.needSourceCode), process[i], selectedJobVersion);
        addLog4jSetting(process[i]);
        addDependenciesSourceCode(process, process[i], isOptionChoosed(ExportChoice.needSourceCode));
        addXmlMapping(process[i], isOptionChoosed(ExportChoice.needSourceCode));

        addJobItem(process, processItem, isOptionChoosed(ExportChoice.needJobItem), process[i], selectedJobVersion);

        // addDependencies(process, processItem, isOptionChoosed(ExportChoice.needDependencies)
        // && isOptionChoosed(ExportChoice.needJobItem), process[i]);
        resources.addAll(getJobScripts(processItem, selectedJobVersion, isOptionChoosed(ExportChoice.needJobScript))); // always
        // need
        // job
        // generation

        // workaround for problem on children jobs generation
        processItem.getProcess().getNode();

        addContextScripts(process[i], selectedJobVersion, isOptionChoosed(ExportChoice.needContext));

        // addBuildScripts(process[i], processItem, selectedJobVersion);

        // add ESB configs
        List<URL> esbResources = getEsbConfigs(processItem);
        if (null != esbResources) {
            process[i].addResources(esbResources);
        }

        // add children jobs
        boolean needChildren = true;
        List<URL> childrenList = addChildrenResources(process, processItem, needChildren, process[i], exportChoice,
                selectedJobVersion);
        return childrenList;
    }

    private List<URL> getEsbConfigs(ProcessItem processItem) {

        boolean samEnabled = false;
        boolean slEnabled = false;

        for (Object node : processItem.getProcess().getNode()) { // loop every node in exported job
            if (node instanceof NodeType) {
                NodeType nodeType = (NodeType) node;
                String componentName = nodeType.getComponentName();
                if ("tESBConsumer".equals(componentName) //$NON-NLS-1$
                        || "tRESTClient".equals(componentName) //$NON-NLS-1$
                        || "tRESTRequest".equals(componentName) //$NON-NLS-1$
                        || "cREST".equals(componentName)) { //$NON-NLS-1$
                    slEnabled |= EmfModelUtils.computeCheckElementValue("SERVICE_LOCATOR", nodeType); //$NON-NLS-1$
                    samEnabled |= EmfModelUtils.computeCheckElementValue("SERVICE_ACTIVITY_MONITOR", nodeType); //$NON-NLS-1$
                } else if ("cSOAP".equals(componentName)) { //$NON-NLS-1$
                    slEnabled |= EmfModelUtils.computeCheckElementValue("ENABLE_SL", nodeType); //$NON-NLS-1$
                    samEnabled |= EmfModelUtils.computeCheckElementValue("ENABLE_SAM", nodeType); //$NON-NLS-1$
                }
                if (samEnabled && slEnabled) {
                    break;
                }
            }
        }

        if (samEnabled || slEnabled) {
            List<URL> esbResources = new ArrayList<URL>();
            try {
                File eclipseEsbFolder = EsbConfigUtils.getEclipseEsbFolder();
                if (samEnabled) {
                    esbResources.add(new File(eclipseEsbFolder, "agent.properties").toURI().toURL()); //$NON-NLS-1$
                }
                if (slEnabled) {
                    esbResources.add(new File(eclipseEsbFolder, "locator.properties").toURI().toURL()); //$NON-NLS-1$
                }
            } catch (MalformedURLException e) {
                RepositoryPlugin
                        .getDefault()
                        .getLog()
                        .log(new Status(IStatus.WARNING, RepositoryPlugin.getDefault().getBundle().getSymbolicName(),
                                "illegal ESB configuration file path - " + e.getMessage())); //$NON-NLS-1$
                return null;
            }
            return esbResources;
        }
        return null;
    }

    /**
     *
     * DOC ggu Comment method "getMavenPropertiesMap".
     *
     * @param item
     * @param privatePackage
     * @param exportService
     * @param bundleClasspath
     * @return
     */
    protected Map<String, String> getMainMavenProperties(Item item) {
        String projectName = getCorrespondingProjectName(item);
        String jobName = item.getProperty().getLabel();
        String jobVersion = item.getProperty().getVersion();

        // set the maven properties
        final Map<String, String> mavenPropertiesMap = new HashMap<String, String>();
        mavenPropertiesMap.put(EMavenBuildScriptProperties.ItemGroupName.getVarScript(),
                PomIdsHelper.getJobGroupId(item.getProperty()));
        mavenPropertiesMap.put(EMavenBuildScriptProperties.ItemProjectName.getVarScript(), projectName);
        mavenPropertiesMap.put(EMavenBuildScriptProperties.ItemName.getVarScript(), jobName);
        mavenPropertiesMap.put(EMavenBuildScriptProperties.ItemVersion.getVarScript(), jobVersion);

        return mavenPropertiesMap;
    }

    protected void setMavenBuildScriptProperties(Document pomDocument, Map<String, String> mavenPropertiesMap) {
        // for groupId, artifactId,version
        Element rootElement = pomDocument.getRootElement();
        replaceMavenBuildScriptProperties(rootElement, mavenPropertiesMap);

        // parent
        Element parentEle = rootElement.element(IMavenProperties.ELE_PARENT);
        if (parentEle != null) {
            replaceMavenBuildScriptProperties(parentEle, mavenPropertiesMap);
        }
        // properties
        Element propertiesEle = rootElement.element(IMavenProperties.ELE_PROPERTIES);
        if (propertiesEle != null) {
            replaceMavenBuildScriptProperties(propertiesEle, mavenPropertiesMap);
        }

    }

    protected void setMavenBuildScriptModules(Document pomDocument) {
        Element rootElement = pomDocument.getRootElement();
        // parent
        Element parentEle = rootElement.element(IMavenProperties.ELE_MODULES);
        if (parentEle != null) {
            removeComments(parentEle);
            List originalElements = parentEle.elements();
            List oldElements = new ArrayList(originalElements);
            originalElements.clear();
            List<String> mavenModules = getMavenModules();
            for (String module : mavenModules) {
                Element moduleElement = parentEle.addElement(IMavenProperties.ELE_MODULE);
                moduleElement.setText(module);
            }
            for (Object eleObj : oldElements) {
                if (eleObj instanceof Element) {
                    parentEle.add((Element) eleObj);
                }
            }
        }
    }

    @SuppressWarnings("rawtypes")
    private void replaceMavenBuildScriptProperties(Element parentElement, Map<String, String> mavenPropertiesMap) {
        Iterator propertiesIt = parentElement.elementIterator();
        while (propertiesIt.hasNext()) {
            Element propEle = (Element) propertiesIt.next();
            final String text = propEle.getText();
            String newText = text;
            boolean replaced = false;
            for (String script : mavenPropertiesMap.keySet()) {
                if (newText.contains(script)) {
                    String value = mavenPropertiesMap.get(script);
                    if (value != null) {
                        replaced = true;
                        newText = newText.replace(script, value);
                    }
                }
            }
            if (replaced && !text.equals(newText)) {
                propEle.setText(newText);
            }
        }
    }

    protected void setMavenDependencyElements(Document pomDocument) {
        addMavenDependencyElements(pomDocument, getCompiledModuleNeededs(), IMavenProperties.LIB_PATH);
        addMavenDependencyElements(pomDocument, getExcludedModuleNeededs(), IMavenProperties.PROVIDED_LIB_PATH);
    }

    protected void addMavenDependencyElements(Document pomDocument, Set<ModuleNeeded> neededModules, String libFolder) {
        Element rootElement = pomDocument.getRootElement();
        Element parentEle = rootElement.element(IMavenProperties.ELE_DEPENDENCIES);
        if (parentEle == null) {
            parentEle = rootElement.addElement(IMavenProperties.ELE_DEPENDENCIES);
        }
        removeComments(parentEle);
        List<ModuleNeeded> modelesList = new ArrayList<ModuleNeeded>();
        modelesList.addAll(neededModules);
        Collections.sort(modelesList, new Comparator<ModuleNeeded>() {

            @Override
            public int compare(ModuleNeeded m1, ModuleNeeded m2) {
                return m1.getModuleName().compareToIgnoreCase(m2.getModuleName());
            }
        });
        for (ModuleNeeded module : modelesList) {
            addMavenDependencyElement(parentEle, module.getModuleName(), libFolder);
        }
    }

    protected void addMavenDependencyElement(Element parentElement, String jarName, String libFolder) {
        String jarNameWithoutExt = jarName;
        if (jarNameWithoutExt.indexOf(".") != -1) { //$NON-NLS-1$
            jarNameWithoutExt = jarNameWithoutExt.substring(0, jarNameWithoutExt.lastIndexOf(".")); //$NON-NLS-1$
        }
        Element dependencyElement = parentElement.addElement(IMavenProperties.ELE_DEPENDENCY);
        Element groupIdElement = dependencyElement.addElement(IMavenProperties.ELE_GROUP_ID);
        groupIdElement.setText(jarNameWithoutExt);
        Element artifactIdElement = dependencyElement.addElement(IMavenProperties.ELE_ARTIFACT_ID);
        artifactIdElement.setText(jarNameWithoutExt);
        Element versionElement = dependencyElement.addElement(IMavenProperties.ELE_VERSION);
        versionElement.setText("1.0"); //$NON-NLS-1$
        Element scopeElement = dependencyElement.addElement(IMavenProperties.ELE_SCOPE);
        scopeElement.setText("system"); //$NON-NLS-1$
        Element systemPathElement = dependencyElement.addElement(IMavenProperties.ELE_SYSTEMPATH);
        systemPathElement.setText(libFolder + jarName);
    }

    protected void updateMavenBuildFileContent(File mavenBuildFile, Map<String, String> mavenPropertiesMap,
            boolean addDependencies, boolean updateModules) throws DocumentException, IOException {
        SAXReader saxReader = new SAXReader();
        Document pomDocument = saxReader.read(mavenBuildFile);
        setMavenBuildScriptProperties(pomDocument, mavenPropertiesMap);
        if (updateModules) {
            setMavenBuildScriptModules(pomDocument);
        }
        if (addDependencies) {
            setMavenDependencyElements(pomDocument);
        }
        saveXmlDocoment(pomDocument, mavenBuildFile);
    }

    protected void updateMavenBuildFileContent(File mavenBuildFile, Map<String, String> mavenPropertiesMap)
            throws DocumentException, IOException {
        updateMavenBuildFileContent(mavenBuildFile, mavenPropertiesMap, false, false);
    }

    protected void createMavenBuildFileFromTemplate(File mavenBuildFile, String mavenScript) throws IOException {
        FileOutputStream mavenBuildFileOutputStream = null;
        try {
            mavenBuildFileOutputStream = new FileOutputStream(mavenBuildFile);
            mavenBuildFileOutputStream.write(mavenScript.getBytes());
        } finally {
            if (mavenBuildFileOutputStream != null) {
                mavenBuildFileOutputStream.close();
            }
        }
    }

    protected void removeComments(Element ele) {
        // remove comments.
        Iterator commentIterator = ele.nodeIterator();
        while (commentIterator.hasNext()) {
            Object commentObj = commentIterator.next();
            if (commentObj instanceof Comment) {
                Comment comment = (Comment) commentObj;
                if (comment.getNodeType() == Node.COMMENT_NODE) {
                    commentIterator.remove();
                }
            }
        }
    }

    protected void saveXmlDocoment(Document document, File outputFile) throws IOException {
        XMLWriter output = null;
        try {
            output = new XMLWriter(new FileWriter(outputFile), OutputFormat.createPrettyPrint());
            output.write(document);
        } finally {
            output.close();
        }
    }

    /**
     * DOC informix Comment method "preExportResource".
     *
     * @param process
     * @param i
     * @param selectedJobVersion
     * @return
     */
    protected String preExportResource(ExportFileResource[] process, int i, final String selectedVersion) {
        String sJobVersion = selectedVersion;
        if (!isMultiNodes() && this.getSelectedJobVersion() != null) {
            sJobVersion = this.getSelectedJobVersion();
        }
        if (progressMonitor != null) {
            progressMonitor
                    .subTask(Messages.getString("JobJavaScriptsManager.buildJob") + process[i].getNode().getObject().getLabel() + "_" + sJobVersion); //$NON-NLS-1$//$NON-NLS-2$
        }
        ProcessorUtilities.setExportConfig(process[i].getDirectoryName(), true);
        return sJobVersion;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.repository.ui.wizards.exportjob.JobScriptsManager#getExportResources(org.talend.core.model.properties
     * .ProcessItem[], boolean, boolean, boolean, boolean, boolean, boolean, boolean, java.lang.String)
     */
    @Override
    public List<ExportFileResource> getExportResources(ExportFileResource[] process, String... codeOptions)
            throws ProcessorException {
        exportFileResource = process;
        for (int i = 0; i < process.length; i++) {
            ProcessItem processItem = (ProcessItem) process[i].getItem();
            String selectedJobVersion = processItem.getProperty().getVersion();
            selectedJobVersion = preExportResource(process, i, selectedJobVersion);

            IProcess jobProcess = null;

            // TODO: option doNotCompileCode is deprecated now.
            // code is just kept like this to avoid too big changes right now.
            if (!isOptionChoosed(ExportChoice.doNotCompileCode)) {
                if (contextName != null) {
                    jobProcess = generateJobFiles(processItem, contextName, selectedJobVersion,
                            statisticPort != IProcessor.NO_STATISTICS || isOptionChoosed(ExportChoice.addStatistics),
                            tracePort != IProcessor.NO_TRACES, isOptionChoosed(ExportChoice.applyToChildren), progressMonitor);
                }
                analysisModules(processItem.getProperty().getId(), selectedJobVersion);
            } else {
                LastGenerationInfo.getInstance().setModulesNeededWithSubjobPerJob(processItem.getProperty().getId(),
                        processItem.getProperty().getVersion(), Collections.<ModuleNeeded> emptySet());
                LastGenerationInfo.getInstance().setLastMainJob(null);
            }
            List<URL> resources = new ArrayList<URL>();
            List<URL> childrenList = new ArrayList<URL>();
            if (CommonsPlugin.isHeadless()) {
                childrenList = posExportResource(process, exportChoice, contextName, launcher, statisticPort, tracePort, i,
                        jobProcess, processItem, selectedJobVersion, resources, codeOptions);
            } else {
                String log4jOption = getLog4jLevel() != null ? TalendProcessArgumentConstant.CMD_ARG_LOG4J_LEVEL
                        + getLog4jLevel().toLowerCase() : null;
                String[] newCodeOptions = codeOptions;
                if (!ArrayUtils.contains(codeOptions, log4jOption)) {
                    newCodeOptions = (String[]) ArrayUtils.add(codeOptions, log4jOption);
                }
                childrenList = posExportResource(process, exportChoice, contextName, launcher, statisticPort, tracePort, i,
                        jobProcess, processItem, selectedJobVersion, resources, newCodeOptions);
            }
            resources.addAll(childrenList);
            process[i].addResources(resources);
            // Gets job designer resouce
            // List<URL> srcList = getSource(processItem, exportChoice.get(ExportChoice.needSource));
            // process[i].addResources(JOB_SOURCE_FOLDER_NAME, srcList);
        }

        // Exports the system libs
        List<ExportFileResource> list = new ArrayList<ExportFileResource>(Arrays.asList(process));

        // Add the java system libraries
        ExportFileResource libResource = getCompiledLibExportFileResource(process);
        list.add(libResource);

        // Gets jobInfo.properties
        // only addClasspathJar not check in preferences ,then export the jobInfo.properties
        boolean addClasspathJar = false;
        IDesignerCoreUIService designerCoreUIService = CoreUIPlugin.getDefault().getDesignerCoreUIService();
        if (designerCoreUIService != null) {
            addClasspathJar = designerCoreUIService.getPreferenceStore().getBoolean(IRepositoryPrefConstants.ADD_CLASSPATH_JAR);
        }
        if (!addClasspathJar) {
            if (!(process.length > 1)) {
                for (ExportFileResource pro : process) {
                    ExportFileResource jobInfoResource = new ExportFileResource(null, PATH_SEPARATOR);
                    if (CommonsPlugin.isHeadless()) {
                        jobInfoResource = new ExportFileResource();
                    }
                    list.add(jobInfoResource);
                    List<URL> jobInfoList = getJobInfoFile(pro, contextName);
                    jobInfoResource.addResources(jobInfoList);
                }
            }
        }

        if (PluginChecker.isRulesPluginLoaded()) {
            // hywang add for 6484,add final drl files or xls files to exported job script
            ExportFileResource ruleFileResource = new ExportFileResource(null, "Rules/rules/final"); //$NON-NLS-1$
            list.add(ruleFileResource);
            try {
                Map<String, List<URL>> map = initUrlForRulesFiles(process);
                Object[] keys = map.keySet().toArray();
                for (Object key : keys) {
                    List<URL> talendDrlFiles = map.get(key.toString());
                    ruleFileResource.addResources(key.toString(), talendDrlFiles);
                }
            } catch (CoreException e) {
                ExceptionHandler.process(e);
            } catch (MalformedURLException e) {
                ExceptionHandler.process(e);
            } catch (PersistenceException e) {
                ExceptionHandler.process(e);
            }
        }
        return list;

    }

    protected ExportFileResource getCompiledLibExportFileResource(ExportFileResource[] processes) {
        return getCompiledLibExportFileResource(processes, false);
    }

    protected ExportFileResource getCompiledLibExportFileResource(ExportFileResource[] processes, boolean isSpecialMR) {
        ExportFileResource libResource = new ExportFileResource(null, LIBRARY_FOLDER_NAME);
        // Gets talend libraries
        List<URL> talendLibraries = getExternalLibraries(isOptionChoosed(ExportChoice.needTalendLibraries), processes,
                getCompiledModuleNames(isSpecialMR));
        if (talendLibraries != null) {
            libResource.addResources(talendLibraries);
        }
        // routines
        addRoutinesResources(processes, libResource);

        // // Add libraries which are needed by build scripts.
        // List<URL> buildScriptLibraries = getBuildScriptLibraries();
        // libResource.addResources(buildScriptLibraries);

        // Add log4jFiles to lib folder if log4j is enable
        // addLog4jXmlToRes(libResource);
        return libResource;
    }

    protected void addRoutinesResources(ExportFileResource[] processes, ExportFileResource libResource) {
        // Gets system routines
        if (isOptionChoosed(ExportChoice.needSystemRoutine)) {
            List<URL> systemRoutineList = getSystemRoutine(processes);
            libResource.addResources(systemRoutineList);
        }
        // Gets user routines
        if (isOptionChoosed(ExportChoice.needUserRoutine)) {
            List<URL> userRoutineList = getUserRoutine(processes);
            libResource.addResources(userRoutineList);
        }
    }

    /**
     * DOC acer Comment method "addContextScripts".
     *
     * @param resource
     * @param boolean1
     */
    protected void addContextScripts(ExportFileResource resource, Boolean needContext) {
        addContextScripts((ProcessItem) resource.getItem(), escapeFileNameSpace((ProcessItem) resource.getItem()), resource
                .getItem().getProperty().getVersion(), resource, needContext);
    }

    /**
     * ftang Comment method "addContextScripts".
     *
     * @param resource
     * @param boolean1
     */
    protected void addContextScripts(ExportFileResource resource, String jobVersion, Boolean needContext) {
        addContextScripts((ProcessItem) resource.getItem(), escapeFileNameSpace((ProcessItem) resource.getItem()), jobVersion,
                resource, needContext);
    }

    /**
     * DOC acer Comment method "addContextScripts".
     *
     * @param resource
     * @param boolean1
     */
    protected void addContextScripts(ProcessItem processItem, String jobName, String jobVersion, ExportFileResource resource,
            Boolean needContext) {
        if (!needContext) {
            return;
        }
        List<URL> list = new ArrayList<URL>(1);
        String projectName = getCorrespondingProjectName(processItem);
        String folderName = JavaResourcesHelper.getJobFolderName(jobName, jobVersion);
        try {
            String jobPackagePath = projectName + PATH_SEPARATOR + folderName + PATH_SEPARATOR
                    + JavaUtils.JAVA_CONTEXTS_DIRECTORY;
            ITalendProcessJavaProject talendProcessJavaProject = RepositoryPlugin.getDefault().getRunProcessService()
                    .getTalendJobJavaProject(processItem.getProperty());
            if (talendProcessJavaProject == null) {
                return;
            }

            IFolder outputFolder = talendProcessJavaProject.getExternalResourcesFolder();
            IFolder contextsFolder = outputFolder.getFolder(jobPackagePath);
            File contextDir = contextsFolder.getLocation().toFile();
            if (contextDir.isDirectory()) {
                list.addAll(getActiveContextFiles(contextDir.listFiles(), processItem));
            }

            // list.add(classRoot.toFile().toURL());

            resource.addResources(jobPackagePath, list);
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }
    }

    /**
     * User may delete some contexts after generating the context files. So we will only export those files that match
     * any existing context name. See bug 0003568: Three contexts file exported, while only two contexts in the job.
     *
     * @param listFiles The generated context files.
     * @param processItem The current process item that will be exported.
     * @return An url list of context files.
     * @throws MalformedURLException
     */
    private List<URL> getActiveContextFiles(File[] listFiles, ProcessItem processItem) throws MalformedURLException {
        List<URL> contextFileUrls = new ArrayList<URL>();
        try {
            // get all context name from process
            Set<String> contextNames = new HashSet<String>();
            for (String contextName : LastGenerationInfo.getInstance().getContextPerJob(processItem.getProperty().getId(),
                    processItem.getProperty().getVersion())) {
                contextNames.add(contextName.replace(" ", "")); //$NON-NLS-1$ //$NON-NLS-2$
            }
            for (File file : listFiles) {
                String fileName = file.getName();
                // remove file extension
                fileName = fileName.substring(0, fileName.lastIndexOf('.'));
                if (contextNames.contains(fileName)) {
                    // if the file match any existing context, add this file to list
                    contextFileUrls.add(file.toURL());
                }
            }
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }
        return contextFileUrls;
    }

    /*
     * (non-Javadoc)
     *
     * @seeorg.talend.repository.ui.wizards.exportjob.scriptsmanager.JobScriptsManager#getSource(org.talend.core.model.
     * properties.ProcessItem, boolean)
     */
    @Override
    protected void addJobItem(ExportFileResource[] allResources, ProcessItem processItem, boolean needSource,
            ExportFileResource resource, String... selectedJobVersion) {
        // getItemResource(processItem, resource, basePath, selectedJobVersion);
        // super.addSource(processItem, needSource, resource, basePath, selectedJobVersion);
        // Get java src
        if (!needSource) {
            return;
        }
        try {
            // ProxyRepositoryFactory.getSpecificVersion(projectprocessItem.getProperty().getId,);

            String projectName = getCorrespondingProjectName(processItem);

            IPath projectFilePath = getCorrespondingProjectRootPath(processItem).append(FileConstants.LOCAL_PROJECT_FILENAME);

            IPath itemFilePath = getProcessItemPath(processItem, null, false, selectedJobVersion);
            IPath propertiesFilePath = itemFilePath.removeFileExtension().addFileExtension(FileConstants.PROPERTIES_EXTENSION);
            IPath screenshotFilePath = itemFilePath.removeFileExtension().addFileExtension(FileConstants.SCREENSHOT_EXTENSION);

            // project file
            checkAndAddProjectResource(allResources, resource, JOB_ITEMS_FOLDER_NAME + PATH_SEPARATOR + projectName,
                    FileLocator.toFileURL(projectFilePath.toFile().toURL()));

            List<URL> emfFileUrls = new ArrayList<URL>();
            emfFileUrls.add(FileLocator.toFileURL(itemFilePath.toFile().toURL()));
            emfFileUrls.add(FileLocator.toFileURL(propertiesFilePath.toFile().toURL()));
            emfFileUrls.add(FileLocator.toFileURL(screenshotFilePath.toFile().toURL()));

            ERepositoryObjectType itemType = ERepositoryObjectType.getItemType(processItem);
            IPath typeFolderPath = new Path(ERepositoryObjectType.getFolderName(itemType));
            String relativePath = JOB_ITEMS_FOLDER_NAME + PATH_SEPARATOR + projectName + PATH_SEPARATOR
                    + typeFolderPath.toOSString();
            IPath itemFolderPath = itemFilePath.makeRelativeTo(projectFilePath.removeLastSegments(1).append(typeFolderPath))
                    .removeLastSegments(1);
            String itemFolderPathString = null;
            if (itemFolderPath != null) {
                itemFolderPathString = itemFolderPath.toString();
            }
            if (itemFolderPathString != null && !"".equals(itemFolderPathString)) { //$NON-NLS-1$
                relativePath = relativePath + PATH_SEPARATOR + itemFolderPathString;
            }
            resource.addResources(relativePath, emfFileUrls);
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }
    }

    protected IPath getProcessItemPath(ProcessItem processItem, String path, boolean flag, String... selectedJobVersion)
            throws Exception {
        String processPath = processItem.getState().getPath();
        if (path != null) {
            processPath = path;
        }
        processPath = processPath == null || processPath.equals("") ? "" : processPath; //$NON-NLS-1$ //$NON-NLS-2$
        IPath emfFileRootPath = getEmfFileRootPath(processItem);

        String jobName = processItem.getProperty().getLabel();
        String jobVersion = processItem.getProperty().getVersion();
        if (!isMultiNodes() && selectedJobVersion != null && selectedJobVersion.length == 1) {
            jobVersion = selectedJobVersion[0];
        }
        IPath itemPath = emfFileRootPath.append(processPath).append(
                jobName + '_' + jobVersion + '.' + FileConstants.ITEM_EXTENSION);

        File itemFile = itemPath.toFile();
        if (!flag && !itemFile.exists()) {
            IRepositoryViewObject specificVersion = CoreRuntimePlugin.getInstance().getProxyRepositoryFactory()
                    .getSpecificVersion(processItem.getProperty().getId(), jobVersion, true);
            if (specificVersion != null) {
                Property property = specificVersion.getProperty();
                URI uri = property.eResource().getURI();
                IPath fullPath = URIHelper.convert(uri);
                IPath relativePath = fullPath.makeRelativeTo(emfFileRootPath);
                String realProcessPath = relativePath.removeLastSegments(1).removeFirstSegments(2).toString();

                return getProcessItemPath(processItem, realProcessPath, true, selectedJobVersion);
            }
        }

        return itemPath;
    }

    protected void addSourceCode(ExportFileResource[] allResources, ProcessItem processItem, boolean needSource,
            ExportFileResource resource, String... selectedJobVersion) {
        // getItemResource(processItem, resource, basePath, selectedJobVersion);
        // super.addSource(processItem, needSource, resource, basePath, selectedJobVersion);
        // Get java src
        if (!needSource) {
            return;
        }
        try {
            String projectName = getCorrespondingProjectName(processItem);
            String jobName = processItem.getProperty().getLabel();
            String jobVersion = processItem.getProperty().getVersion();
            if (!isMultiNodes() && selectedJobVersion != null && selectedJobVersion.length == 1) {
                jobVersion = selectedJobVersion[0];
            }

            String jobFolderName = JavaResourcesHelper.getJobFolderName(jobName, jobVersion);

            IPath path = getSrcRootLocation(processItem);
            path = path.append(projectName).append(jobFolderName);

            FilenameFilter filter = new FilenameFilter() {

                @Override
                public boolean accept(File dir, String name) {
                    return name.toLowerCase().endsWith(".java"); //$NON-NLS-1$
                }
            };
            List<URL> javaFileUrls = new ArrayList<URL>();
            File file = path.toFile();
            if (file.exists() && file.isDirectory()) {
                for (File curFile : file.listFiles(filter)) {
                    javaFileUrls.add(FileLocator.toFileURL(curFile.toURL()));
                }
            }

            resource.addResources(JOB_SOURCE_FOLDER_NAME + PATH_SEPARATOR + projectName + PATH_SEPARATOR + jobFolderName,
                    javaFileUrls);

        } catch (Exception e) {
            ExceptionHandler.process(e);
        }
    }

    protected void addDependenciesSourceCode(ExportFileResource[] process, ExportFileResource resource, boolean needSource) {
        if (!needSource) {
            return;
        }
        try {
            // get different routines.

            IRunProcessService service = CorePlugin.getDefault().getRunProcessService();
            ITalendProcessJavaProject talendProcessJavaProject = service.getTalendCodeJavaProject(ERepositoryObjectType.ROUTINES);
            if (talendProcessJavaProject == null) {
                return;
            }
            IFolder srcFolder = talendProcessJavaProject.getSrcFolder();
            IFolder systemRoutineFolder = srcFolder.getFolder(SYSTEM_ROUTINES_PATH);
            List<URL> systemRoutinesFileUrls = new ArrayList<URL>();
            if (systemRoutineFolder.exists()) {
                for (IResource fileResource : systemRoutineFolder.members()) {
                    if (fileResource instanceof IFile
                            && ((IFile) fileResource).getFileExtension().equals(ECodeLanguage.JAVA.getExtension())) {
                        systemRoutinesFileUrls.add(fileResource.getLocationURI().toURL());
                    }
                }

                resource.addResources(JOB_SOURCE_FOLDER_NAME + PATH_SEPARATOR + SYSTEM_ROUTINES_PATH, systemRoutinesFileUrls);
            }
            // bug TDI-8647
            systemRoutinesFileUrls.clear(); // empty and re-use it
            systemRoutineFolder = srcFolder.getFolder(SYSTEM_ROUTINES_PATH + PATH_SEPARATOR
                    + JavaUtils.JAVA_SYSTEM_ROUTINES_API_DIRECTORY);
            if (systemRoutineFolder.exists()) {
                for (IResource fileResource : systemRoutineFolder.members()) {
                    if (fileResource instanceof IFile
                            && ((IFile) fileResource).getFileExtension().equals(ECodeLanguage.JAVA.getExtension())) {
                        systemRoutinesFileUrls.add(fileResource.getLocationURI().toURL());
                    }
                }

                resource.addResources(JOB_SOURCE_FOLDER_NAME + PATH_SEPARATOR + SYSTEM_ROUTINES_PATH + PATH_SEPARATOR
                        + JavaUtils.JAVA_SYSTEM_ROUTINES_API_DIRECTORY, systemRoutinesFileUrls);
            }
            // add for routines
            boolean useBeans = false;
            if (GlobalServiceRegister.getDefault().isServiceRegistered(ICamelDesignerCoreService.class)) {
                ICamelDesignerCoreService camelService = (ICamelDesignerCoreService) GlobalServiceRegister.getDefault()
                        .getService(ICamelDesignerCoreService.class);
                if (camelService.isInstanceofCamel(process[0].getItem())) {
                    useBeans = true;
                    talendProcessJavaProject = service.getTalendCodeJavaProject(ERepositoryObjectType.BEANS);
                }
            }
            addRoutinesSourceCodes(process, resource, talendProcessJavaProject, useBeans);
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }
    }

    protected void addRoutinesSourceCodes(ExportFileResource[] process, ExportFileResource resource,
            ITalendProcessJavaProject talendProcessJavaProject, boolean useBeans) throws Exception {
        List<IRepositoryViewObject> collectRoutines = new ArrayList<IRepositoryViewObject>();
        String include;
        if (useBeans) {
            include = USER_BEANS_PATH;
        } else {
            include = USER_ROUTINES_PATH;
        }
        collectRoutines.addAll(collectRoutines(process, include));
        collectRoutines.addAll(collectRoutines(process, USER_PIGUDF_PATH));

        Set<String> dependedRoutines = new HashSet<String>();
        for (IRepositoryViewObject obj : collectRoutines) {
            dependedRoutines.add(obj.getLabel() + "." //$NON-NLS-1$
                    + ECodeLanguage.JAVA.getExtension());
        }
        IFolder srcFolder = talendProcessJavaProject.getSrcFolder();
        IFolder folder = null;
        if (useBeans) {
            folder = srcFolder.getFolder(USER_BEANS_PATH);
        } else {
            folder = srcFolder.getFolder(JavaUtils.JAVA_ROUTINES_DIRECTORY);
        }
        List<URL> userRoutinesFileUrls = new ArrayList<URL>();
        if (folder.exists()) {
            for (IResource fileResource : folder.members()) {
                if (fileResource instanceof IFile
                        && ((IFile) fileResource).getFileExtension().equals(ECodeLanguage.JAVA.getExtension())
                        && dependedRoutines.contains(((IFile) fileResource).getName())) {
                    userRoutinesFileUrls.add(fileResource.getLocationURI().toURL());
                }
            }

            if (useBeans) {
                resource.addResources(JOB_SOURCE_FOLDER_NAME + PATH_SEPARATOR + USER_BEANS_PATH, userRoutinesFileUrls);
            } else {
                resource.addResources(JOB_SOURCE_FOLDER_NAME + PATH_SEPARATOR + JavaUtils.JAVA_ROUTINES_DIRECTORY,
                        userRoutinesFileUrls);
            }
        }

    }

    protected void addXmlMapping(ExportFileResource resource, boolean needSource) {
        try {
            boolean hasDynamicMetadata = false;
            if (resource.getItem() instanceof ProcessItem) {
                List<JobInfo> list = new ArrayList<JobInfo>();

                hasDynamicMetadata = LastGenerationInfo.getInstance().isUseDynamic(resource.getItem().getProperty().getId(),
                        resource.getItem().getProperty().getVersion());

            }
            if (hasDynamicMetadata) {
                needMappingInSystemRoutine = true;
                if (needSource) {
                    IRunProcessService service = CorePlugin.getDefault().getRunProcessService();
                    ITalendProcessJavaProject talendProcessJavaProject = service.getTalendJobJavaProject(resource.getItem().getProperty());
                    if (talendProcessJavaProject == null) {
                        return;
                    }
                    // for db mapping xml
                    IFolder xmlMappingFolder = talendProcessJavaProject.getExternalResourcesFolder()
                            .getFolder(JavaUtils.JAVA_XML_MAPPING);
                    if (GlobalServiceRegister.getDefault().isServiceRegistered(IRunProcessService.class)) {
                        IRunProcessService runService = (IRunProcessService) GlobalServiceRegister.getDefault().getService(IRunProcessService.class);
                        ITalendProcessJavaProject talendJavaProject = runService.getTalendJobJavaProject(resource.getItem().getProperty());
                        if (GlobalServiceRegister.getDefault().isServiceRegistered(ICoreService.class)) {
                            ICoreService coreService = (ICoreService) GlobalServiceRegister.getDefault().getService(ICoreService.class);
                            coreService.synchronizeMapptingXML(talendJavaProject);
                            coreService.syncLog4jSettings(talendJavaProject);
                        }
                    }
                    List<URL> xmlMappingFileUrls = new ArrayList<URL>();
                    if (xmlMappingFolder.exists()) {
                        for (IResource fileResource : xmlMappingFolder.members()) {
                            if (XmlUtil.isXMLFile(fileResource.getName())) {
                                xmlMappingFileUrls.add(fileResource.getLocationURI().toURL());
                            }
                        }
                        resource.addResources(JOB_SOURCE_FOLDER_NAME + PATH_SEPARATOR + JavaUtils.JAVA_XML_MAPPING,
                                xmlMappingFileUrls);

                    }
                }
            }
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }
    }

    protected void addLog4jSetting(ExportFileResource resource) {
        try {
            if (Log4jPrefsSettingManager.getInstance().isLog4jEnable()
                    && GlobalServiceRegister.getDefault().isServiceRegistered(IRunProcessService.class)) {
                IRunProcessService processService = (IRunProcessService) GlobalServiceRegister.getDefault().getService(
                        IRunProcessService.class);
                ITalendProcessJavaProject talendProcessJavaProject = processService.getTalendJobJavaProject(resource.getItem().getProperty());
                if (talendProcessJavaProject != null) {
                    IFolder resourcesFolder = talendProcessJavaProject.getExternalResourcesFolder();
                    IFile log4jFile = null;
                    if (Log4jPrefsSettingManager.getInstance().isSelectLog4j2()) {
                        log4jFile = resourcesFolder.getFile(Log4jPrefsConstants.LOG4J2_FILE_NAME);
                    } else {
                        log4jFile = resourcesFolder.getFile(Log4jPrefsConstants.LOG4J_FILE_NAME);
                    }
                    if (log4jFile.exists()) {
                        List<URL> log4jFileUrls = new ArrayList<URL>();
                        log4jFileUrls.add(FileLocator.toFileURL(log4jFile.getLocationURI().toURL()));
                        resource.addResources(PATH_SEPARATOR, log4jFileUrls);
                    }
                }
            }
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }
    }

    protected String calculateLibraryPathFromDirectory(String directory) {
        int nb = directory.split(PATH_SEPARATOR).length - 1;
        String path = "../"; //$NON-NLS-1$
        for (int i = 0; i < nb; i++) {
            path = path.concat("../"); //$NON-NLS-1$
        }
        return path + LIBRARY_FOLDER_NAME;
    }

    protected List<URL> addChildrenResources(ExportFileResource[] allResources, ProcessItem process, boolean needChildren,
            ExportFileResource resource, Map<ExportChoice, Object> exportChoice, String... selectedJobVersion) {
        List<JobInfo> list = new ArrayList<JobInfo>();
        String projectName = getCorrespondingProjectName(process);
        List<URL> allJobScripts = new ArrayList<URL>();
        try {
            List<ProcessItem> processedJob = new ArrayList<ProcessItem>();
            getChildrenJobAndContextName(allResources, process.getProperty().getLabel(), list, process, projectName,
                    processedJob, allJobScripts, resource, exportChoice, selectedJobVersion);
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }

        return allJobScripts;
    }

    protected void getChildrenJobAndContextName(ExportFileResource[] allResources, String rootName, List<JobInfo> list,
            ProcessItem process, String projectName, List<ProcessItem> processedJob, List<URL> allJobScripts,
            ExportFileResource resource, Map<ExportChoice, Object> exportChoice, String... selectedJobVersion) {
        if (processedJob.contains(process)) {
            // prevent circle
            return;
        }
        processedJob.add(process);
        ProjectManager projectManager = ProjectManager.getInstance();
        Project project = projectManager.getProject(process);
        String childProjectName = projectName;
        if (project != null) {
            childProjectName = project.getTechnicalLabel().toLowerCase(); // hywang modify for 7932
        }
        allJobScripts.addAll(getJobScripts(process, childProjectName, process.getProperty().getLabel(),
                process.getProperty().getVersion(), isOptionChoosed(ExportChoice.needJobScript)));
        addContextScripts(process, process.getProperty().getLabel(), process.getProperty().getVersion(), resource,
                isOptionChoosed(ExportChoice.needContext));
        addJobItem(allResources, process, isOptionChoosed(ExportChoice.needJobItem), resource);
        addDependencies(allResources, process, isOptionChoosed(ExportChoice.needDependencies)
                && isOptionChoosed(ExportChoice.needJobItem), resource);
        addSourceCode(allResources, process, isOptionChoosed(ExportChoice.needSourceCode), resource);
        addDependenciesSourceCode(allResources, resource, isOptionChoosed(ExportChoice.needSourceCode));
        Set<JobInfo> subjobInfos = ProcessorUtilities.getChildrenJobInfo(process);
        for (JobInfo subjobInfo : subjobInfos) {
            if (list.contains(subjobInfo)) {
                continue;
            }
            list.add(subjobInfo);
            getChildrenJobAndContextName(allResources, rootName, list, subjobInfo.getProcessItem(), projectName, processedJob,
                    allJobScripts, resource, exportChoice);
        }
    }

    /**
     * It's preferable to use the function with additional parameter neededLibraries. <br>
     * Right now all the needed librairies in jobs can be retrieved just after the code generation with the method
     * ProcessorUtilities.getNeededModules(), which will be really faster and memory used will be much lower.
     *
     * @deprecated
     */
    @Deprecated
    protected List<URL> getExternalLibraries(boolean needLibraries, ExportFileResource[] process) {
        return getExternalLibraries(needLibraries, process, null);
    }

    /**
     * Gets required java jars.
     *
     * @param process
     *
     * @param boolean1
     * @return
     */
    protected List<URL> getExternalLibraries(boolean needLibraries, ExportFileResource[] process,
            final Set<String> neededLibraries) {
        List<URL> list = new ArrayList<URL>();
        if (!needLibraries) {
            return list;
        }
        IFolder libFolder = null;
        if (GlobalServiceRegister.getDefault().isServiceRegistered(IRunProcessService.class)) {
            IRunProcessService processService = (IRunProcessService) GlobalServiceRegister.getDefault().getService(
                    IRunProcessService.class);
            libFolder = processService.getJavaProjectLibFolder();
        }
        if (libFolder == null) {
            return list;
        }
        File file = libFolder.getLocation().toFile();
        File[] files = file.listFiles(FilesUtils.getAcceptModuleFilesFilter());
        // Lists all the needed jar files
        Set<String> listModulesReallyNeeded = new HashSet<String>();
        if (neededLibraries == null) {
            // in case export as been done with option "not recompile", then libraires can't be retrieved when build.
            IDesignerCoreService designerService = RepositoryPlugin.getDefault().getDesignerCoreService();
            for (ExportFileResource resource : process) {
                ProcessItem item = (ProcessItem) resource.getItem();
                String version = item.getProperty().getVersion();
                if (!isMultiNodes() && this.getSelectedJobVersion() != null) {
                    version = this.getSelectedJobVersion();
                }
                ProcessItem selectedProcessItem;
                if (resource.getNode() != null) {
                    selectedProcessItem = ItemCacheManager.getProcessItem(resource.getNode().getRoot().getProject(), item
                            .getProperty().getId(), version);
                } else {
                    // if no node given, take in the current project only
                    selectedProcessItem = ItemCacheManager.getProcessItem(item.getProperty().getId(), version);
                }
                IProcess iProcess = designerService.getProcessFromProcessItem(selectedProcessItem);
                Set<String> processNeededLibraries = iProcess.getNeededLibraries(TalendProcessOptionConstants.MODULES_WITH_CHILDREN);
                if (processNeededLibraries != null) {
                    listModulesReallyNeeded.addAll(processNeededLibraries);
                }
            }
        } else {
            listModulesReallyNeeded.addAll(neededLibraries);
        }

        // jar from routines
        List<IRepositoryViewObject> collectRoutines = new ArrayList<IRepositoryViewObject>();
        boolean useBeans = false;
        if (GlobalServiceRegister.getDefault().isServiceRegistered(ICamelDesignerCoreService.class)) {
            ICamelDesignerCoreService camelService = (ICamelDesignerCoreService) GlobalServiceRegister.getDefault().getService(
                    ICamelDesignerCoreService.class);
            if (camelService.isInstanceofCamel(process[0].getItem())) {
                useBeans = true;
            }
        }
        String include = null;
        if (useBeans) {
            include = USER_BEANS_PATH;
        } else {
            include = USER_ROUTINES_PATH;
        }
        collectRoutines.addAll(collectRoutines(process, include));
        collectRoutines.addAll(collectRoutines(process, USER_PIGUDF_PATH));

        for (IRepositoryViewObject object : collectRoutines) {
            Item item = object.getProperty().getItem();
            if (item instanceof RoutineItem) {
                RoutineItem routine = (RoutineItem) item;
                EList imports = routine.getImports();
                for (Object o : imports) {
                    IMPORTType type = (IMPORTType) o;
                    listModulesReallyNeeded.add(type.getMODULE());
                }
            }
        }
        if (Log4jPrefsSettingManager.getInstance().isLog4jEnable()) {
            addLog4jToJarList(listModulesReallyNeeded);
        }
        for (File tempFile : files) {
            try {
                if (listModulesReallyNeeded.contains(tempFile.getName())) {
                    list.add(tempFile.toURL());
                }
            } catch (MalformedURLException e) {
                ExceptionHandler.process(e);
            }
        }
        return list;
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
    }

    /**
     * Gets required java jars.
     *
     * @param process
     *
     * @param boolean1
     * @return
     */
    protected List<URL> getLog4jFiles() {
        List<URL> list = new ArrayList<URL>();
        if (Log4jUtil.isEnable()
                && Boolean.parseBoolean(Log4jPrefsSettingManager.getInstance().getValueOfPreNode(
                        Log4jPrefsConstants.LOG4J_ENABLE_NODE))
                && GlobalServiceRegister.getDefault().isServiceRegistered(IRunProcessService.class)) {
            IRunProcessService processService = (IRunProcessService) GlobalServiceRegister.getDefault().getService(
                    IRunProcessService.class);
            try {
                ITalendProcessJavaProject talendProcessJavaProject = processService.getTempJavaProject();
                if (talendProcessJavaProject != null) {
                    IFolder resourcesFolder = talendProcessJavaProject.getExternalResourcesFolder();
                    IFile log4jFile = null;
                    if (Log4jPrefsSettingManager.getInstance().isSelectLog4j2()) {
                        log4jFile = resourcesFolder.getFile(Log4jPrefsConstants.LOG4J2_FILE_NAME);
                    } else {
                        log4jFile = resourcesFolder.getFile(Log4jPrefsConstants.LOG4J_FILE_NAME);
                    }
                    if (log4jFile.exists()) {
                        list.add(FileLocator.toFileURL(log4jFile.getLocationURI().toURL()));
                    }
                }
            } catch (MalformedURLException e) {
                ExceptionHandler.process(e);
            } catch (Exception e) {
                ExceptionHandler.process(e);
            }
        }
        return list;
    }

    protected void addLog4jXmlToRes(ExportFileResource libResource) {
        List<URL> list = new ArrayList<URL>();
        if (Log4jPrefsSettingManager.getInstance().isLog4jEnable()
                && GlobalServiceRegister.getDefault().isServiceRegistered(IRunProcessService.class)) {
            IRunProcessService processService = (IRunProcessService) GlobalServiceRegister.getDefault().getService(
                    IRunProcessService.class);
            try {
                ITalendProcessJavaProject talendProcessJavaProject = processService.getTalendJobJavaProject(libResource.getItem().getProperty());
                if (talendProcessJavaProject != null) {
                    IFolder resourcesFolder = talendProcessJavaProject.getExternalResourcesFolder();
                    IFile log4jFile = null;
                    if (Log4jPrefsSettingManager.getInstance().isSelectLog4j2()) {
                        log4jFile = resourcesFolder.getFile(Log4jPrefsConstants.LOG4J2_FILE_NAME);
                    } else {
                        log4jFile = resourcesFolder.getFile(Log4jPrefsConstants.LOG4J_FILE_NAME);
                    }
                    if (log4jFile.exists()) {
                        list.add(log4jFile.getLocationURI().toURL());
                    }
                }
            } catch (MalformedURLException e) {
                ExceptionHandler.process(e);
            } catch (Exception e) {
                ExceptionHandler.process(e);
            }
        }
        libResource.addResources(list);
    }

    protected void addLog4jToJarList(Collection<String> jarList) {
        UpdateLog4jJarUtils.addLog4jToJarList(jarList, Log4jPrefsSettingManager.getInstance().isSelectLog4j2());
    }

    /**
     * Gets Job Scripts.
     *
     * @param process
     * @param needJob
     * @param needContext
     * @return
     */
    protected List<URL> getJobScripts(ProcessItem process, boolean needJob) {

        String projectName = getCorrespondingProjectName(process);
        return this.getJobScripts(process, projectName, escapeFileNameSpace(process), process.getProperty().getVersion(), needJob);
    }

    /**
     * Gets JobInfo properties.
     *
     * @param process
     * @return
     */
    protected List<URL> getJobInfoFile(ExportFileResource process, String contextName) {
        List<URL> list = new ArrayList<URL>();
        try {
            String tmpFoler = getTmpFolder();
            String jobInfoPath = tmpFoler + File.separator + JOBINFO_FILE;

            boolean addStat = false;// TDI-23641, in studio, false always.
            if (CommonsPlugin.isHeadless()) {
                addStat = isOptionChoosed(ExportChoice.addStatistics);
            }
            JobInfoBuilder jobInfoBuilder = new JobInfoBuilder(process, contextName,
                    isOptionChoosed(ExportChoice.applyToChildren), addStat, jobInfoPath);
            jobInfoBuilder.buildProperty();
            File jobInfoFile = new File(jobInfoPath);
            URL url = jobInfoFile.toURL();
            list.add(url);
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }
        return list;
    }

    /**
     * Gets Job Scripts.
     *
     * @param process
     * @param version
     * @param needJob
     * @return
     */
    protected List<URL> getJobScripts(ProcessItem process, String version, boolean needJob) {
        String projectName = getCorrespondingProjectName(process);
        return this.getJobScripts(process, projectName, escapeFileNameSpace(process), version, needJob);
    }

    protected Object getProcessParameterValue(ProcessItem process, String parameterName) {
        EList<ElementParameterType> parameters = process.getProcess().getParameters().getElementParameter();
        for (ElementParameterType pt : parameters) {
            if (pt.getName().equals(parameterName)) {
                return pt.getValue();
            }
        }

        return null;
    }

    /**
     * Gets Job Scripts.
     *
     * @param projectName TODO
     * @param needJob
     * @param process
     * @param needContext
     *
     * @return
     */
    protected List<URL> getJobScripts(ProcessItem process, String projectName, String jobName, String jobVersion, boolean needJob) {
        List<URL> list = new ArrayList<URL>(1);
        if (!needJob) {
            return list;
        }
        String jobFolderName = JavaResourcesHelper.getJobFolderName(jobName, jobVersion);

        try {
            File jarFile = new File(getTmpFolder() + File.separatorChar + jobFolderName + FileConstants.JAR_FILE_SUFFIX);
            // Exports the jar file
            File classRootFileLocation = getJobClassRootFileLocation(process.getProperty());
            if (classRootFileLocation == null) {
                return Collections.emptyList();
            }
            JarBuilder jarbuilder = new JarBuilder(classRootFileLocation, jarFile);

            // builds the jar file of the job classes,needContext specifies whether inclucdes the context.
            // add the job
            String jobPath = projectName + PATH_SEPARATOR + jobFolderName;
            jarbuilder.setIncludeDir(Collections.singleton(jobPath));
            // filter the context
            String contextPath = jobPath + PATH_SEPARATOR + JavaUtils.JAVA_CONTEXTS_DIRECTORY;
            jarbuilder.setExcludeDir(Collections.singleton(contextPath));

            jarbuilder.buildJar();

            list.add(jarFile.toURI().toURL());
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }
        return list;
    }

    protected List<String> getRelatedJobFolderNames(ProcessItem process) {
        return this.getRelatedJobFolderNames(process, new HashSet<String>());
    }

    protected List<String> getRelatedJobFolderNames(ProcessItem process, Set<String> jobNameVersionChecked) {
        List<String> jobFolderNames = new ArrayList<String>();
        String projectName = getCorrespondingProjectName(process);
        String jobName = process.getProperty().getLabel();
        String jobVersion = process.getProperty().getVersion();
        String id = projectName + ":" + jobName + "_" + jobVersion; //$NON-NLS-1$ //$NON-NLS-2$
        if (jobNameVersionChecked.contains(id)) {
            return jobFolderNames; // no need to add more to the list, just return the empty list
        }
        jobNameVersionChecked.add(id);
        String jobFolderName = JavaResourcesHelper.getJobFolderName(jobName, jobVersion);
        jobFolderNames.add(projectName + ":" + jobFolderName); //$NON-NLS-1$
        Set<JobInfo> subjobInfos = ProcessorUtilities.getChildrenJobInfo(process);
        for (JobInfo subjobInfo : subjobInfos) {
            jobFolderNames.addAll(getRelatedJobFolderNames(subjobInfo.getProcessItem(), jobNameVersionChecked));
        }
        return jobFolderNames;
    }

    /**
     * Gets all the perl files in the project .Perl.
     *
     * @param name
     * @param projectName
     *
     * @return
     */
    protected String getJobClassRootLocation(Property property) throws Exception {
        File classRootFileLocation = getJobClassRootFileLocation(property);
        if (classRootFileLocation == null) {
            return ""; //$NON-NLS-1$
        }
        return classRootFileLocation.toURI().toURL().getPath();
    }

    protected String getCodeClassRootLocation(ERepositoryObjectType type) throws Exception {
        File classRootFileLocation = getCodeClassRootFileLocation(type);
        if (classRootFileLocation == null) {
            return ""; //$NON-NLS-1$
        }
        return classRootFileLocation.toURI().toURL().getPath();
    }

    protected File getCodeClassRootFileLocation(ERepositoryObjectType type) {
        ITalendProcessJavaProject talendProcessJavaProject = RepositoryPlugin.getDefault().getRunProcessService()
                .getTalendCodeJavaProject(type);
        if (talendProcessJavaProject == null) {
            return null;
        }
        IFolder outputFolder = talendProcessJavaProject.getOutputFolder();
        return outputFolder.getLocation().toFile();
    }

    protected File getJobClassRootFileLocation(Property property) {
        ITalendProcessJavaProject talendProcessJavaProject = RepositoryPlugin.getDefault().getRunProcessService()
                .getTalendJobJavaProject(property);
        if (talendProcessJavaProject == null) {
            return null;
        }
        IFolder outputFolder = talendProcessJavaProject.getOutputFolder();
        return outputFolder.getLocation().toFile();
    }

    @Deprecated
    protected File getClassRootFileLocation() {
        ITalendProcessJavaProject talendProcessJavaProject = RepositoryPlugin.getDefault().getRunProcessService().getTempJavaProject();
        if (talendProcessJavaProject == null) {
            return null;
        }
        IFolder outputFolder = talendProcessJavaProject.getOutputFolder();
        return outputFolder.getLocation().toFile();
    }

    /**
     * Get the path of .JAVA/src
     *
     * @throws Exception
     */
    protected IPath getSrcRootLocation(ProcessItem processItem) throws Exception {
        ITalendProcessJavaProject talendProcessJavaProject = RepositoryPlugin.getDefault().getRunProcessService()
                .getTalendJobJavaProject(processItem.getProperty());
        if (talendProcessJavaProject == null) {
            return new Path(""); //$NON-NLS-1$
        }
        IProject project = talendProcessJavaProject.getProject();
        IJavaProject javaProject = talendProcessJavaProject.getJavaProject();
        IPackageFragmentRoot[] pp = javaProject.getAllPackageFragmentRoots();
        IPackageFragmentRoot src = null;
        for (IPackageFragmentRoot root : pp) {
            if (root.getKind() == IPackageFragmentRoot.K_SOURCE) {
                src = root;
                break;
            }
        }

        IPath root = project.getParent().getLocation();
        root = root.append(src.getPath());
        return root;
    }

    /**
     * Gets system routine.
     *
     * @param needSystemRoutine
     * @return
     */
    protected List<URL> getSystemRoutine(ExportFileResource[] process) {
        try {
            List<String> include = new ArrayList<String>();
            include.add(SYSTEM_ROUTINES_PATH);
            if (needMappingInSystemRoutine) {
                include.add(JavaUtils.JAVA_XML_MAPPING);
            }

            File jarFile = new File(getTmpFolder() + File.separatorChar + SYSTEMROUTINE_JAR);

            // make a jar file of system routine classes
            File classRootFileLocation = getCodeClassRootFileLocation(ERepositoryObjectType.ROUTINES);
            if (classRootFileLocation == null) {
                return Collections.emptyList();
            }
            JarBuilder jarbuilder = new JarBuilder(classRootFileLocation, jarFile);
            jarbuilder.setIncludeDir(include);
            jarbuilder.setIncludeRoutines(getRoutineDependince(process, true, USER_ROUTINES_PATH));
            jarbuilder.buildJar();

            return Collections.singletonList(jarFile.toURI().toURL());
        } catch (Exception e) {
            ExceptionHandler.process(e);
            return Collections.emptyList();
        }
    }

    /**
     * Gets user routine.
     *
     * @param needUserRoutine
     * @return
     */
    protected List<URL> getUserRoutine(ExportFileResource[] process) {
        try {
            boolean useBeans = false;
            if (GlobalServiceRegister.getDefault().isServiceRegistered(ICamelDesignerCoreService.class)) {
                ICamelDesignerCoreService service = (ICamelDesignerCoreService) GlobalServiceRegister.getDefault().getService(
                        ICamelDesignerCoreService.class);
                if (service.isInstanceofCamel(process[0].getItem())) {
                    useBeans = true;
                }
            }
            String includePath;
            String jar;
            File classRootFileLocation;
            if (useBeans) {
                classRootFileLocation = getCodeClassRootFileLocation(ERepositoryObjectType.BEANS);
                includePath = USER_BEANS_PATH;
                jar = USERBEANS_JAR;
            } else {
                classRootFileLocation = getCodeClassRootFileLocation(ERepositoryObjectType.ROUTINES);
                includePath = USER_ROUTINES_PATH;
                jar = USERROUTINE_JAR;
            }
            List<URL> urlList = new ArrayList<URL>();

            File jarFile = new File(getTmpFolder() + File.separatorChar + jar);

            if (classRootFileLocation == null) {
                return Collections.emptyList();
            }
            JarBuilder jarbuilder = new JarBuilder(classRootFileLocation, jarFile);
            jarbuilder.setIncludeDir(Collections.singleton(includePath));
            jarbuilder.setIncludeRoutines(getRoutineDependince(process, false, includePath));
            jarbuilder.setExcludeDir(Arrays.asList(SYSTEM_ROUTINES_PATH, USER_ROUTINES_PATH, // remove all
                    USER_BEANS_PATH, USER_PIGUDF_PATH));
            jarbuilder.buildJar();
            urlList.add(jarFile.toURI().toURL());

            return urlList;
        } catch (Exception e) {
            ExceptionHandler.process(e);
            return Collections.emptyList();
        }
    }

    protected Collection<File> getRoutineDependince(ExportFileResource[] process, boolean system, String type) {
        Collection<File> userRoutines = null;
        try {
            File classRootFileLocation = getCodeClassRootFileLocation(ERepositoryObjectType.ROUTINES);
            if (classRootFileLocation == null) {
                return Collections.emptyList();
            }
            userRoutines = getAllFiles(classRootFileLocation, type);

            Collection<IRepositoryViewObject> collectRoutines = collectRoutines(process, system, type);

            Iterator<File> iterator = userRoutines.iterator();
            while (iterator.hasNext()) {
                File file = iterator.next();
                boolean found = false;
                for (IRepositoryViewObject object : collectRoutines) {
                    Item item = object.getProperty().getItem();
                    /*
                     * only support like "ABC.class", "ABC$1.class" and "ABC$XYZ.class",
                     *
                     * Do not support the class in one routine file.
                     */
                    String pattern = item.getProperty().getLabel() + "(\\$.+)*\\.class"; //$NON-NLS-1$
                    if (file.getName().matches(pattern)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    iterator.remove();
                }
            }

        } catch (Exception e) {
            ExceptionHandler.process(e);
        }
        return userRoutines;

    }

    protected Collection<IRepositoryViewObject> collectRoutines(ExportFileResource[] process, boolean system, String type) {
        Collection<IRepositoryViewObject> allRoutines = new ArrayList<IRepositoryViewObject>();
        for (IRepositoryViewObject object : collectRoutines(process, type)) {
            Item item = object.getProperty().getItem();
            if (item instanceof RoutineItem && (((RoutineItem) item).isBuiltIn() == system)) {
                allRoutines.add(object);
            }
        }

        return allRoutines;
    }

    protected Collection<IRepositoryViewObject> collectRoutines(ExportFileResource[] process, String type) {
        List<IRepositoryViewObject> toReturn = new ArrayList<IRepositoryViewObject>();
        if (USER_BEANS_PATH.equals(type)) {
            ERepositoryObjectType beansType = null;
            if (GlobalServiceRegister.getDefault().isServiceRegistered(ICamelDesignerCoreService.class)) {
                ICamelDesignerCoreService service = (ICamelDesignerCoreService) GlobalServiceRegister.getDefault().getService(
                        ICamelDesignerCoreService.class);
                beansType = service.getBeansType();
            }

            try {
                toReturn = ProxyRepositoryFactory.getInstance().getAll(beansType);
            } catch (PersistenceException e) {
                ExceptionHandler.process(e);
                toReturn = Collections.emptyList();
            }
        } else {
            Set<String> allRoutinesNames = new HashSet<String>();
            ERepositoryObjectType objectType = ERepositoryObjectType.ROUTINES;

            if (ERepositoryObjectType.ROUTINES == objectType) {
                for (ExportFileResource resource : process) {
                    if (resource.getItem() instanceof ProcessItem) {
                        Set<String> routinesNeededForJob = LastGenerationInfo.getInstance().getRoutinesNeededWithSubjobPerJob(
                                resource.getItem().getProperty().getId(), resource.getItem().getProperty().getVersion());
                        if (routinesNeededForJob != null) {
                            allRoutinesNames.addAll(routinesNeededForJob);
                        }
                    }

                }
            }

            if (allRoutinesNames.isEmpty()) {
                toReturn.addAll(RoutinesUtil.getCurrentSystemRoutines());
            } else {
                toReturn.addAll(collectRoutinesFromRepository(allRoutinesNames, objectType));
            }

        }
        return toReturn;
    }

    private Collection<IRepositoryViewObject> collectRoutinesFromRepository(Set<String> allRoutinesNames,
            ERepositoryObjectType type) {
        IProxyRepositoryFactory factory = CorePlugin.getDefault().getProxyRepositoryFactory();
        List<IRepositoryViewObject> toReturn = new ArrayList<IRepositoryViewObject>();
        try {
            List<IRepositoryViewObject> availableRoutines = factory
                    .getAll(ProjectManager.getInstance().getCurrentProject(), type);
            for (IRepositoryViewObject object : availableRoutines) {
                if (allRoutinesNames.contains(object.getLabel())) {
                    allRoutinesNames.remove(object.getLabel());
                    toReturn.add(object);
                }
            }
            if (allRoutinesNames.isEmpty()) {
                return toReturn;
            }
            for (org.talend.core.model.general.Project project : ProjectManager.getInstance().getAllReferencedProjects()) {
                for (IRepositoryViewObject object : factory.getAll(project, type)) {
                    if (allRoutinesNames.contains(object.getLabel())) {
                        allRoutinesNames.remove(object.getLabel());
                        toReturn.add(object);
                    }
                }
                if (allRoutinesNames.isEmpty()) {
                    return toReturn;
                }
            }
        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
            toReturn.addAll(RoutinesUtil.getCurrentSystemRoutines());
        }
        return toReturn;
    }

    @Override
    public List<String> getJobContextsComboValue(ProcessItem processItem) {
        List<String> contextNameList = new ArrayList<String>();
        for (Object o : ((ProcessTypeImpl) processItem.getProcess()).getContext()) {
            if (o instanceof ContextType) {
                ContextType context = (ContextType) o;
                if (contextNameList.contains(context.getName())) {
                    continue;
                }
                contextNameList.add(context.getName());
            }
        }
        return contextNameList;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.repository.ui.wizards.exportjob.scriptsmanager.JobScriptsManager#getCurrentProjectName()
     */
    @Override
    protected String getCorrespondingProjectName(Item item) {
        return JavaResourcesHelper.getProjectFolderName(item);
    }

    protected List<URL> getLib(List<ModuleNeeded> libs, Boolean needLib) {
        List<URL> list = new ArrayList<URL>();
        if (!needLib) {
            return list;
        }

        try {
            org.talend.core.model.general.Project projecdddt = ProjectManager.getInstance().getCurrentProject();
            IProject fsProject = null;
            try {
                fsProject = ResourceUtils.getProject(projecdddt);
            } catch (PersistenceException e2) {
                ExceptionHandler.process(e2);
            }
            IPath temPath = fsProject.getLocation().append(File.separator + "temp"); //$NON-NLS-1$
            ILibraryManagerService repositoryBundleService = CorePlugin.getDefault().getRepositoryBundleService();
            if (repositoryBundleService != null) {
                repositoryBundleService.retrieve(new HashSet<ModuleNeeded>(libs), temPath.toString(), true);
            }
            File file = temPath.toFile();
            File[] files = file.listFiles(new FilenameFilter() {

                @Override
                public boolean accept(File dir, String name) {
                    return name.toLowerCase().endsWith(FileConstants.JAR_FILE_SUFFIX)
                            || name.toLowerCase().endsWith(FileConstants.PROPERTIES_FILE_SUFFIX)
                            || name.toLowerCase().endsWith(FileConstants.ZIP_FILE_SUFFIX) ? true : false;
                }
            });

            Set<String> moduleNames = new HashSet<String>();
            for (ModuleNeeded m : libs) {
                moduleNames.add(m.getModuleName());
            }

            for (File tempFile : files) {
                try {
                    if (moduleNames.contains(tempFile.getName())) {
                        list.add(tempFile.toURL());
                    }
                } catch (MalformedURLException e) {
                    ExceptionHandler.process(e);
                }
            }
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }
        return list;
    }

    /**
     * DOC hywang Comment method "initUrlForDrlFiles".
     *
     * @param process
     * @param talendDrlFiles
     * @throws PersistenceException
     * @throws CoreException
     * @throws MalformedURLException
     */
    private Map<String, List<URL>> initUrlForRulesFiles(ExportFileResource[] process) throws PersistenceException, CoreException,
            MalformedURLException {

        Map<String, List<URL>> map = new HashMap<String, List<URL>>();
        List<URL> urlList = new ArrayList<URL>();

        String processLabelAndVersion = null;
        IFile file;
        Item item = null;
        ProcessItem pi = null;
        ITalendProcessJavaProject talendJavaProject = null;
        if (PluginChecker.isRulesPluginLoaded()) {
            IProxyRepositoryFactory factory = CorePlugin.getDefault().getProxyRepositoryFactory();
            IRulesProviderService rulesService = (IRulesProviderService) GlobalServiceRegister.getDefault().getService(
                    IRulesProviderService.class);
            IRunProcessService runService = CorePlugin.getDefault().getRunProcessService();

            for (ExportFileResource proces : process) { // loop every exported job
                if (!urlList.isEmpty()) {
                    urlList = new ArrayList<URL>();
                }
                item = (proces).getItem();

                if (item instanceof ProcessItem) {
                    pi = (ProcessItem) item;
                    processLabelAndVersion = JavaResourcesHelper.getJobFolderName(pi.getProperty().getLabel(), pi.getProperty()
                            .getVersion());
                    talendJavaProject = runService.getTalendJobJavaProject(pi.getProperty());
                }
                for (int j = 0; j < pi.getProcess().getNode().size(); j++) { // loop every node in every exported job
                    if (pi.getProcess().getNode().get(j) instanceof NodeType) {
                        NodeType node = (NodeType) pi.getProcess().getNode().get(j);
                        if (rulesService.isRuleComponent(node)) {
                            for (Object obj : node.getElementParameter()) {
                                if (obj instanceof ElementParameterType) {
                                    ElementParameterType elementParameter = (ElementParameterType) obj;
                                    if (elementParameter.getName().equals("PROPERTY:REPOSITORY_PROPERTY_TYPE")) { //$NON-NLS-1$
                                        String id = elementParameter.getValue();
                                        if (factory.getLastVersion(id).getProperty().getItem() != null) {
                                            if (factory.getLastVersion(id).getProperty().getItem() instanceof RulesItem) {
                                                RulesItem rulesItem = (RulesItem) factory.getLastVersion(id).getProperty()
                                                        .getItem();
                                                file = rulesService.getFinalRuleFile(rulesItem, processLabelAndVersion, talendJavaProject);
                                                if (file != null) {
                                                    URL url = file.getLocationURI().toURL();
                                                    urlList.add(url);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                map.put(processLabelAndVersion, urlList);
            }
        }
        return map;
    }

    private Collection<File> getAllFiles(File rootFile, String childPath) {
        final Collection<File> list = new ArrayList<File>();
        new File(rootFile, childPath).listFiles(new java.io.FilenameFilter() {

            @Override
            public boolean accept(java.io.File dir, String name) {
                File file = new java.io.File(dir, name);
                if (file.isFile()) {
                    list.add(file);
                    return true;
                }
                return false;
            }
        });

        return list;
    }

    @Override
    public List<ExportFileResource> getExportResources(ExportFileResource[] process, IContext context, String... codeOptions)
            throws ProcessorException {
        contextName = context.getName();
        return getExportResources(process, codeOptions);
    }

    @Override
    public void setTopFolder(List<ExportFileResource> resourcesToExport) {
        String topFolder = getRootFolderName(this.topFolderName);
        for (ExportFileResource fileResource : resourcesToExport) {
            String directory = fileResource.getDirectoryName();
            fileResource.setDirectoryName(topFolder + "/" + directory); //$NON-NLS-1$
        }
    }

    /**
     *
     * DOC ggu Comment method "isCompiledLib".
     *
     * The modudle will be use to compile and run the job.
     */
    protected boolean isCompiledLib(ModuleNeeded module) {
        return module != null; // should add all by default
    }

    protected boolean isExcludedLib(ModuleNeeded module) {
        return false; // default, no exclued lib
    }

    public List<File> getLibPath(boolean isSpecialMR) {
        List<File> ret = new ArrayList<File>();
        if (exportFileResource != null) {
            ExportFileResource libResource = getCompiledLibExportFileResource(exportFileResource, isSpecialMR);
            Collection<Set<URL>> col = libResource.getAllResources();
            // this from org.talend.libraries.apache.storm/lib
            URL stormLibUrl = null;
            File file = null;
            try {
                stormLibUrl = FileLocator.toFileURL(FileLocator.find(Platform.getBundle(PLUGIN_ID), new Path("lib"), null));
                file = new File(stormLibUrl.getFile());
            } catch (IOException e) {
                ExceptionHandler.process(e);
            }
            for (Set<URL> set : col) {
                Iterator<URL> it = set.iterator();
                while (it.hasNext()) {
                    URL url = it.next();
                    // for storm not include the jar from libraries.apache.strom
                    if (!isSpecialMR && stormLibUrl != null && file != null) {
                        File[] jars = file.listFiles();
                        String name = url.getFile().substring(url.getFile().lastIndexOf("/") + 1, url.getFile().length());
                        boolean isExist = false;
                        for (File jarFile : jars) {
                            if (jarFile.getName().equals(name)) {
                                isExist = true;
                            }
                        }
                        if (!isExist) {
                            ret.add(new File(url.getFile()));
                        }
                    } else {
                        ret.add(new File(url.getFile()));
                    }
                }
            }
        }
        return ret;
    }
}
