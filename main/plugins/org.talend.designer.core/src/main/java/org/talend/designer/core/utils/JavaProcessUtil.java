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
package org.talend.designer.core.utils;

import java.io.File;
import java.util.ArrayList;
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
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang.StringUtils;
import org.eclipse.emf.common.util.EList;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.runtime.service.ITaCoKitService;
import org.talend.commons.utils.generation.JavaUtils;
import org.talend.commons.utils.resource.FileExtensions;
import org.talend.core.CorePlugin;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.hadoop.IHadoopClusterService;
import org.talend.core.hadoop.repository.HadoopRepositoryUtil;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.model.components.EComponentType;
import org.talend.core.model.general.ModuleNeeded;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.IContext;
import org.talend.core.model.process.IContextParameter;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.process.IProcess2;
import org.talend.core.model.process.JobInfo;
import org.talend.core.model.process.ProcessUtils;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.routines.RoutinesUtil;
import org.talend.core.model.utils.ContextParameterUtils;
import org.talend.core.model.utils.TalendTextUtils;
import org.talend.core.runtime.maven.MavenConstants;
import org.talend.core.runtime.maven.MavenUrlHelper;
import org.talend.core.runtime.process.LastGenerationInfo;
import org.talend.core.runtime.process.TalendProcessOptionConstants;
import org.talend.core.ui.ITestContainerProviderService;
import org.talend.core.utils.BitwiseOptionUtils;
import org.talend.core.utils.CodesJarResourceCache;
import org.talend.designer.core.IDesignerCoreService;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.model.components.EmfComponent;
import org.talend.designer.core.model.utils.emf.talendfile.RoutinesParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.impl.ElementParameterTypeImpl;
import org.talend.designer.core.ui.editor.process.Process;
import org.talend.designer.maven.utils.MavenVersionHelper;
import org.talend.designer.runprocess.IProcessor;
import org.talend.designer.runprocess.IRunProcessService;
import org.talend.designer.runprocess.ItemCacheManager;
import org.talend.librariesmanager.model.ModulesNeededProvider;
import org.talend.repository.ui.utils.Log4jPrefsSettingManager;
import org.talend.repository.ui.utils.UpdateLog4jJarUtils;

/**
 * DOC xye class global comment. Detailled comment
 */
public class JavaProcessUtil {

    /**
     * get All needed modules of current or sub jobs but without codesjar module of joblet
     */
    public static Set<ModuleNeeded> getNeededModules(final IProcess process, int options) {
        List<ModuleNeeded> modulesNeeded = new ArrayList<ModuleNeeded>();
        // see bug 4939: making tRunjobs work loop will cause a error of "out of memory"
        Set<ProcessItem> searchItems = new HashSet<ProcessItem>();
        if (BitwiseOptionUtils.containOption(options, TalendProcessOptionConstants.MODULES_WITH_CHILDREN)) {
            ProcessItem processItem = null;
            if (process.getVersion() != null) {
                processItem = ItemCacheManager.getProcessItem(process.getId(), process.getVersion());
            } else {
                processItem = ItemCacheManager.getProcessItem(process.getId());
            }
            if (processItem != null) {
                searchItems.add(processItem);
            }
        }

        // call recursive function to get all dependencies from job & subjobs
        getNeededModules(process, searchItems, modulesNeeded, options);
        /*
         * Remove duplicates in the modulesNeeded list after having prioritize the modules. Details in the
         * ModuleNeededComparator class.
         */
        Collections.sort(modulesNeeded, new ModuleNeededComparator());

        Set<String> dedupModulesList = new HashSet<String>();
        Iterator<ModuleNeeded> it = modulesNeeded.iterator();
        ModuleNeeded previousModule = null;
        while (it.hasNext()) {
            ModuleNeeded module = it.next();
            // try to keep only real files (with extension, no matter be jar or other)
            // in some case it's not a real library, but just a text.
            if (!module.getModuleName().contains(".")) { //$NON-NLS-1$
                it.remove();
            } else {
                String coordinate = MavenUrlHelper.getCoordinate(module.getMavenUri());
                
                if (dedupModulesList.contains(coordinate)) {
                    if (module.isMrRequired() && previousModule != null
                            && previousModule.getModuleName().equals(module.getModuleName())) {
                        previousModule.setMrRequired(Boolean.TRUE);
                    }
                    it.remove();
                } else {
                    dedupModulesList.add(coordinate);
                    
                    previousModule = module;
                }
            }
        }

        if (BitwiseOptionUtils.containOption(options, TalendProcessOptionConstants.MODULES_EXCLUDE_SHADED)) {
            new BigDataJobUtil(process).removeExcludedModules(modulesNeeded);
        }

        UpdateLog4jJarUtils.addLog4jToModuleList(modulesNeeded, Log4jPrefsSettingManager.getInstance().isSelectLog4j2(), process);
        return new HashSet<ModuleNeeded>(modulesNeeded);
    }

    // for MapReduce job, if the jar on Xml don't set MRREQUIRED="true", shouldn't add it to
    // DistributedCache
    public static Set<String> getNeededLibraries(final IProcess process, int options) {
        Set<String> libsNeeded = new HashSet<String>();
        for (ModuleNeeded module : getNeededModules(process, options)) {
            libsNeeded.add(module.getModuleName());
        }

        return libsNeeded;
    }

    public static Map<String, ModuleNeeded> getNeededLibrariesMap(List<ModuleNeeded> modulesNeeded) {
        Map<String, ModuleNeeded> libsNeeded = new HashMap<String, ModuleNeeded>();
        for (ModuleNeeded module : modulesNeeded) {
            libsNeeded.put(module.getModuleName(), module);
        }
        return libsNeeded;
    }

    @SuppressWarnings("unchecked")
    private static void getNeededModules(final IProcess process, Set<ProcessItem> searchItems,
            List<ModuleNeeded> modulesNeeded, int options) {
        IElementParameter headerParameter = process.getElementParameter(EParameterName.HEADER_LIBRARY.getName());
        if (headerParameter != null) {
            Object value = headerParameter.getValue();
            if (value != null) {
                String headerLibraries = (String) value;
                if (headerLibraries.indexOf(File.separatorChar) > 0
                        && headerLibraries.length() > headerLibraries.lastIndexOf(File.separatorChar) + 1) {
                    String substring = headerLibraries.substring(headerLibraries.lastIndexOf(File.separatorChar) + 1);
                    if (!"".equals(substring)) { //$NON-NLS-1$
                        modulesNeeded.add(getModuleValue(process, substring));
                    }
                }
            }
        }
        IElementParameter footerParameter = process.getElementParameter(EParameterName.FOOTER_LIBRARY.getName());
        if (footerParameter != null) {
            Object value = footerParameter.getValue();
            if (value != null) {
                String footerLibraries = (String) value;
                if (footerLibraries.indexOf(File.separatorChar) > 0
                        && footerLibraries.length() > footerLibraries.lastIndexOf(File.separatorChar) + 1) {
                    String substring = footerLibraries.substring(footerLibraries.lastIndexOf(File.separatorChar) + 1);
                    if (!"".equals(substring)) { //$NON-NLS-1$
                        modulesNeeded.add(getModuleValue(process, substring));
                    }
                }
            }
        }

        IElementParameter elementParameter = process.getElementParameter(EParameterName.DRIVER_JAR.getName());
        if (elementParameter != null && elementParameter.getFieldType() == EParameterFieldType.TABLE) {
            getModulesInTable(process, elementParameter, modulesNeeded);
        }

        boolean addDefault = false;
        if (process instanceof IProcess2) {
            Item item = ((IProcess2) process).getProperty().getItem();
            if (item == null) {
                addDefault = true;
            } else if (item instanceof ProcessItem) {
                modulesNeeded.addAll(ModulesNeededProvider.getCodesModulesNeededForProcess((ProcessItem) item, process,
                        BitwiseOptionUtils.containOption(options, TalendProcessOptionConstants.MODULES_WITH_CODESJAR)));
            }
        } else {
            addDefault = true;
        }
        if (addDefault) {
            Set<ModuleNeeded> optionalJarsOnlyForRoutines = new HashSet<ModuleNeeded>();
            optionalJarsOnlyForRoutines.addAll(ModulesNeededProvider.getSystemRunningModules());
            modulesNeeded.addAll(optionalJarsOnlyForRoutines);
        }

        boolean isTestcaseProcess = ProcessUtils.isTestContainer(process);
        if (isTestcaseProcess) {// if it is a test container, add junit jars.
            addJunitNeededModules(modulesNeeded);
        }

        String hadoopItemId = null;
        List<INode> nodeList = (List<INode>) process.getGeneratingNodes();

        Set<ModuleNeeded> highPriorityLinkedSet = new LinkedHashSet<ModuleNeeded>();
        Set<ModuleNeeded> jdbcCustomizeModulesSet = new HashSet<ModuleNeeded>();
        for (INode node : nodeList) {
            if (hadoopItemId == null) {
                String itemId = getHadoopClusterItemId(node);
                if (itemId != null) {
                    hadoopItemId = itemId;
                }
            }
            if (process instanceof IProcess2) {
                ((IProcess2) node.getProcess()).setNeedLoadmodules(((IProcess2) process).isNeedLoadmodules());
            }

            Set<ModuleNeeded> nodeNeededModules = getNeededModules(node, searchItems, options);
            if (nodeNeededModules != null) {
                Map<String, ModuleNeeded> libsNeededMap = getNeededLibrariesMap(modulesNeeded);
                modulesNeeded.addAll(nodeNeededModules);
                if (node.getComponent().getName().equals("tLibraryLoad") && !isTestcaseProcess) { //$NON-NLS-1$
                    Set<ModuleNeeded> highPriorityModuleNeeded = new HashSet<ModuleNeeded>();
                    for (ModuleNeeded moduleNeeded : nodeNeededModules) {
                        ModuleNeeded existModuleNeeded = libsNeededMap.get(moduleNeeded.getModuleName());
                        if (existModuleNeeded != null) {
                            highPriorityModuleNeeded.add(existModuleNeeded);
                        } else {
                            highPriorityModuleNeeded.add(moduleNeeded);
                        }
                    }
                    highPriorityLinkedSet.addAll(highPriorityModuleNeeded);
                }

                // for JDBC, user set the customize driver jars need the high Priority
                // but after tLibraryLoad
                if (node.getComponent().getComponentType() == EComponentType.GENERIC && !isTestcaseProcess) {
                    List<ModuleNeeded> jdbcNodeModuleNeededList = new ArrayList<ModuleNeeded>();
                    for (IElementParameter curParam : node.getElementParameters()) {
                        if (curParam.getFieldType() == EParameterFieldType.TABLE) {
                            getModulesInTable(process, curParam, jdbcNodeModuleNeededList);
                        }
                    }
                    jdbcCustomizeModulesSet.addAll(jdbcNodeModuleNeededList);
                }
            }
        }
        // in case of multiple Version jars in customize modules, descendOrder
        highPriorityLinkedSet.addAll(descendingOrderModuleList(jdbcCustomizeModulesSet));
        LastGenerationInfo.getInstance().setHighPriorityModuleNeededPerJob(process.getId(), process.getVersion(),
                highPriorityLinkedSet);
        LastGenerationInfo.getInstance().setHighPriorityModuleNeeded(process.getId(), process.getVersion(),
                highPriorityLinkedSet);

        if (hadoopItemId == null) { // Incase it is a bigdata process.
            IElementParameter propertyParam = process.getElementParameter("MR_PROPERTY"); //$NON-NLS-1$
            if (propertyParam != null) {
                IElementParameter repositoryParam =
                        propertyParam.getChildParameters().get(EParameterName.REPOSITORY_PROPERTY_TYPE.getName());
                if (repositoryParam != null) {
                    hadoopItemId = String.valueOf(repositoryParam.getValue());
                }
            }
        }

        if (hadoopItemId != null) {
            useCustomConfsJarIfNeeded(modulesNeeded, hadoopItemId);
        }
        new BigDataJobUtil(process).setExcludedModules(modulesNeeded);
    }

    public static List<ModuleNeeded> descendingOrderModuleList(Set<ModuleNeeded> moduleList) {
        List<ModuleNeeded> orderedList = new ArrayList<ModuleNeeded>();
        Map<String, List<ModuleNeeded>> multipleVersionHM = new HashMap<String, List<ModuleNeeded>>();
        Set<ModuleNeeded> tmpModuleList = new HashSet<ModuleNeeded>(moduleList);
        Iterator<ModuleNeeded> it = tmpModuleList.iterator();
        Pattern pattern = Pattern.compile("(-\\d+\\.\\d.*)+?(.jar)"); //$NON-NLS-1$
        Pattern versionPattern = Pattern.compile("(?<=-)\\d+\\.\\d.*(?=.jar)"); //$NON-NLS-1$
        while (it.hasNext()) {
            ModuleNeeded module = it.next();
            String moduleName = module.getModuleName();
            Matcher matcher = pattern.matcher(moduleName);
            if (matcher.find()) {
                String matchStr = matcher.group();
                String key = moduleName.substring(0, moduleName.indexOf(matchStr));
                if (multipleVersionHM.get(key) == null) {
                    multipleVersionHM.put(key, new ArrayList<ModuleNeeded>());
                }
                multipleVersionHM.get(key).add(module);
                it.remove();
            }
        }

        for (String key : multipleVersionHM.keySet()) {
            multipleVersionHM.get(key).sort(new Comparator<ModuleNeeded>() {

                @Override
                public int compare(ModuleNeeded o1, ModuleNeeded o2) {
                    String o1Version = "";
                    String o2Version = "";
                    String o1Name = o1.getModuleName();
                    String o2Name = o2.getModuleName();
                    Matcher o1Matcher = versionPattern.matcher(o1Name);
                    if (o1Matcher.find()) {
                        o1Version = o1Matcher.group();
                    }
                    Matcher o2Matcher = versionPattern.matcher(o2Name);
                    if (o2Matcher.find()) {
                        o2Version = o2Matcher.group();
                    }
                    return MavenVersionHelper.compareTo(o2Version, o1Version);
                }

            });

            orderedList.addAll(multipleVersionHM.get(key));
        }
        orderedList.addAll(tmpModuleList);

        return orderedList;
    }

    public static String getHadoopClusterItemId(INode node) {
        return getHadoopClusterItemId(node, true);
    }

    public static String getHadoopClusterItemId(INode node, boolean ignore) {
        IHadoopClusterService hadoopClusterService = HadoopRepositoryUtil.getHadoopClusterService();
        if (hadoopClusterService == null) {
            return null;
        }
        if (isUseExistingConnection(node)) {
            return null;
        }
        IElementParameter propertyElementParameter =
                node.getElementParameterFromField(EParameterFieldType.PROPERTY_TYPE);
        if (propertyElementParameter == null) {
            return null;
        }
        Map<String, IElementParameter> childParameters = propertyElementParameter.getChildParameters();
        String propertyType = (String) childParameters.get(EParameterName.PROPERTY_TYPE.getName()).getValue();
        if (ignore && !EmfComponent.REPOSITORY.equals(propertyType)) {
            return null;
        }
        IElementParameter propertyParam = childParameters.get(EParameterName.REPOSITORY_PROPERTY_TYPE.getName());
        if (propertyParam == null) {
            return null;
        }
        Object propertyValue = propertyParam.getValue();
        if (propertyValue == null) {
            return null;
        }
        String id = String.valueOf(propertyValue);
        Item item = hadoopClusterService.getHadoopClusterItemById(id);
        if (item != null) {
            return id;
        }
        return null;
    }

    public static boolean isUseExistingConnection(INode node) {
        IElementParameter elementParameter = node.getElementParameter(EParameterName.USE_EXISTING_CONNECTION.getName());
        if (elementParameter != null) {
            return Boolean.valueOf(String.valueOf(elementParameter.getValue()));
        }
        return false;
    }

    public static Set<ModuleNeeded> getNeededModules(final INode node, int options) {
        return getNeededModules(node, null, options);
    }

    private static Set<ModuleNeeded> getNeededModules(final INode node, Set<ProcessItem> searchItems, int options) {
        if (searchItems == null) {
            searchItems = new HashSet<ProcessItem>();
        }
        List<ModuleNeeded> modulesNeeded = new ArrayList<ModuleNeeded>();
        addNodeRelatedModules(node.getProcess(), modulesNeeded, node, options);
        // for children job
        if (BitwiseOptionUtils.containOption(options, TalendProcessOptionConstants.MODULES_WITH_CHILDREN)) {
            modulesNeeded.addAll(getChildrenModules(node, searchItems, options));
        }

        return new HashSet<ModuleNeeded>(modulesNeeded);
    }

    private static Set<ModuleNeeded> getChildrenModulesFromProcess(final IProcess process, Set<ProcessItem> searchItems) {
        Set<ModuleNeeded> childrenModules = new HashSet<ModuleNeeded>();
        List<INode> nodeList = (List<INode>) process.getGeneratingNodes();
        if (nodeList != null) {

            for (INode node : nodeList) {
                List<ModuleNeeded> findeModules = getChildrenModules(node, searchItems,
                        TalendProcessOptionConstants.MODULES_WITH_CHILDREN);
                if (findeModules.size() > 0) {
                    childrenModules.addAll(findeModules);
                }
            }
        }
        return childrenModules;
    }

    static List<ModuleNeeded> getChildrenModules(final INode node, Set<ProcessItem> searchItems, int options) {
        List<ModuleNeeded> modulesNeeded = new ArrayList<ModuleNeeded>();
        if (node.getComponent().getName().equals("tRunJob")) { //$NON-NLS-1$
            IElementParameter processIdparam = node.getElementParameter("PROCESS_TYPE_PROCESS"); //$NON-NLS-1$
            IElementParameter processVersionParam =
                    node.getElementParameter(EParameterName.PROCESS_TYPE_VERSION.getName());

            ProcessItem processItem = null;
            if (processVersionParam != null) {
                processItem = ItemCacheManager.getProcessItem((String) processIdparam.getValue(),
                        (String) processVersionParam.getValue());
            } else {
                processItem = ItemCacheManager.getProcessItem((String) processIdparam.getValue());
            }

            String context = (String) node.getElementParameter("PROCESS_TYPE_CONTEXT").getValue(); //$NON-NLS-1$
            if (processItem != null && !searchItems.contains(processItem)) {
                boolean seperated = getBooleanParamValue(node, "USE_INDEPENDENT_PROCESS") //$NON-NLS-1$
                        || getBooleanParamValue(node, "USE_DYNAMIC_JOB"); //$NON-NLS-1$
                if (!seperated || BitwiseOptionUtils.containOption(options,
                        TalendProcessOptionConstants.MODULES_WITH_INDEPENDENT)) {
                    // avoid dead loop of method call
                    searchItems.add(processItem);
                    JobInfo subJobInfo = new JobInfo(processItem, context);
                    IDesignerCoreService service = CorePlugin.getDefault().getDesignerCoreService();
                    IProcess child = service.getProcessFromItem(subJobInfo.getProcessItem());

                    if (!BitwiseOptionUtils.containOption(options,
                            TalendProcessOptionConstants.MODULES_WITH_CHILDREN)) {
                        options |= TalendProcessOptionConstants.MODULES_WITH_CHILDREN;
                    }
                    getNeededModules(child, searchItems, modulesNeeded, options);
                }
            }
        }
        return modulesNeeded;
    }

    private static boolean getBooleanParamValue(final INode node, String paramName) {
        IElementParameter parameter = node.getElementParameter(paramName);
        if (parameter != null && parameter.getValue() != null) {
            return Boolean.parseBoolean(parameter.getValue().toString());
        }
        return false;
    }

    public static void addJunitNeededModules(Collection<ModuleNeeded> modulesNeeded) {
        ModuleNeeded junitModule = new ModuleNeeded("junit-4.13.2", "junit-4.13.2.jar", null, true); //$NON-NLS-1$ //$NON-NLS-2$
        junitModule.setMavenUri("mvn:junit/junit/4.13.2/jar");
        modulesNeeded.add(junitModule);
        //junit4 required dependencies
        ModuleNeeded requiredMoule = new ModuleNeeded("hamcrest-core", "hamcrest-core-1.3.jar", null, true);
        requiredMoule.setMavenUri("mvn:org.hamcrest/hamcrest-core/1.3/jar");
        modulesNeeded.add(requiredMoule);
    }

    /**
     * DOC nrousseau Comment method "addNodeRelatedModules".
     *
     * @param process
     * @param modulesNeeded
     * @param node
     */
    public static void addNodeRelatedModules(final IProcess process, List<ModuleNeeded> modulesNeeded, INode node) {
        addNodeRelatedModules(process, modulesNeeded, node, TalendProcessOptionConstants.MODULES_DEFAULT);
    }

    public static void addNodeRelatedModules(final IProcess process, List<ModuleNeeded> modulesNeeded, INode node,
            int options) {
        if (!node.isActivate()) {
            // if node is deactivated, we don't need at all its dependencies.
            return;
        }
        boolean onlyMR = BitwiseOptionUtils.containOption(options, TalendProcessOptionConstants.MODULES_FOR_MR);
        List<ModuleNeeded> moduleList = node.getModulesNeeded();
        for (ModuleNeeded needed : moduleList) {
            if (needed != null) {
                if (needed.isRequired(node.getElementParameters())) {
                    // for MapReduce job, if the jar on Xml don't set MRREQUIRED="true", shouldn't add it to
                    // DistributedCache
                    if (onlyMR && !needed.isMrRequired()) {
                        continue;
                    }
                    modulesNeeded.add(needed);
                }
            }
        }
        for (IElementParameter curParam : node.getElementParameters()) {
            if (curParam.getFieldType() == null) {
                continue; // field can be null in some really specific cases, like for example when preview from
                // wizard.
            }
            if (!curParam.isShow(node.getElementParameters())) {
                continue;
            }
            if (curParam.getFieldType().equals(EParameterFieldType.MODULE_LIST)) {
                Object curParamValue = curParam.getValue();
                if (curParamValue != null) {
                    if (curParamValue instanceof String) {
                        if (StringUtils.isNotEmpty((String) curParamValue)) {
                            modulesNeeded
                                    .add(evaluateOsgiDependency(getModuleValue(process, (String) curParamValue), node));
                        }
                    } else if (curParamValue instanceof List) {
                        getModulesInTable(process, curParam, modulesNeeded);
                    }
                }
            } else if (curParam.getFieldType() == EParameterFieldType.TABLE) {
                getModulesInTable(process, curParam, modulesNeeded);
            }
            findMoreLibraries(process, modulesNeeded, curParam);
        }
    }

    private static void useCustomConfsJarIfNeeded(List<ModuleNeeded> modulesNeeded, String clusterId) {
        IHadoopClusterService hadoopClusterService = HadoopRepositoryUtil.getHadoopClusterService();
        if (hadoopClusterService != null) {
            hadoopClusterService.useCustomConfsJarIfNeeded(modulesNeeded, clusterId);
        }
    }

    private static void getModulesInTable(final IProcess process, IElementParameter curParam, List<ModuleNeeded> modulesNeeded) {
        if (!(curParam.getValue() instanceof List)) {
            return;
        }
        List<Map<String, Object>> values = (List<Map<String, Object>>) curParam.getValue();
        if (values == null || values.isEmpty()) {
            return;
        }
        Object[] listItemsValue = curParam.getListItemsValue();
        if (listItemsValue == null || listItemsValue.length == 0 && !(listItemsValue[0] instanceof IElementParameter)) {
            return;
        }
        for (Object o : listItemsValue) {
            IElementParameter param = (IElementParameter) o;
            if (param.getFieldType() != EParameterFieldType.MODULE_LIST) {
                continue;
            }
            for (Map<String, Object> line : values) {
                String moduleName = (String) line.get(param.getName());
                if (StringUtils.isBlank(moduleName)) {
                    continue;
                }
                boolean isContextMode = ContextParameterUtils.containContextVariables(moduleName);
                if (isContextMode) {
                    List<IContext> listContext = process.getContextManager().getListContext();
                    for (IContext context : listContext) {
                        List<IContextParameter> contextParameterList = context.getContextParameterList();
                        for (IContextParameter contextPara : contextParameterList) {
                            String var = ContextParameterUtils.getVariableFromCode(moduleName);
                            if (!var.equals(contextPara.getName())) {
                                continue;
                            }
                            String value = context.getContextParameter(contextPara.getName()).getValue();
                            if (StringUtils.isBlank(value)) {
                                continue;
                            }
                            if (!value.contains(";")) { //$NON-NLS-1$
                                modulesNeeded
                                        .add(ModuleNeeded.newInstance(null, TalendTextUtils.removeQuotes(value), null, true));
                                continue;
                            }
                            if (curParam.getName().equals(EParameterName.DRIVER_JAR.getName())) {
                                Stream.of(value.split(";")).forEach(s -> { //$NON-NLS-1$
                                    ModuleNeeded module = null;
                                    String jarName = TalendTextUtils.removeQuotes(s);
                                    if (!jarName.toLowerCase().endsWith(".jar")) { //$NON-NLS-1$
                                        module = ModulesNeededProvider.getModuleNeededById(jarName);
                                        // TDQ-18826 set required to true for tdqReportRun when it is Dynamic jars
                                        if (jarName.startsWith("DYNAMIC_") && module != null) {
                                            module.setRequired(true);
                                        }
                                    }
                                    if (module == null) {
                                        module = ModuleNeeded.newInstance(null, TalendTextUtils.removeQuotes(s), null, true);
                                    }
                                    modulesNeeded.add(module);
                                });
                                continue;
                            }
                            if (curParam.getName().equals("connection.driverTable")) { //$NON-NLS-1$
                                Stream.of(value.split(";")).forEach(s -> modulesNeeded //$NON-NLS-1$
                                        .add(ModuleNeeded.newInstance(null, TalendTextUtils.removeQuotes(s), null, true)));
                            }
                        }
                    }
                } else {
                    ModuleNeeded module = null;
                    if (!moduleName.toLowerCase().endsWith(".jar")) { //$NON-NLS-1$
                        module = ModulesNeededProvider.getModuleNeededById(moduleName);
                    }
                    if (module == null) {
                        module = getModuleValue(process, moduleName);
                    }
                    String nexusJarVersion = (String) line.get("JAR_NEXUS_VERSION"); //$NON-NLS-1$
                    if (nexusJarVersion != null) {
                        String mvnUrl = MavenUrlHelper.generateMvnUrl(MavenConstants.DEFAULT_LIB_GROUP_ID,
                                moduleName.replaceFirst("[.][^.]+$", ""), nexusJarVersion, MavenConstants.TYPE_JAR, null);
                        module.setMavenUri(mvnUrl);
                    }
                    modulesNeeded.add(module);
                }
            }
        }
    }

    private static ModuleNeeded getModuleValue(final IProcess process, String moduleValue) {
        if (ContextParameterUtils.isContainContextParam(moduleValue)) {
            String var = ContextParameterUtils.getVariableFromCode(moduleValue);
            if (var != null) {
                IContext selectedContext = CorePlugin.getDefault().getRunProcessService().getSelectedContext();
                if (selectedContext == null) {
                    selectedContext = process.getContextManager().getDefaultContext();
                }
                IContextParameter param = selectedContext.getContextParameter(var);
                if (param != null) {
                    // add only the file name without path
                    ModuleNeeded module = getModuleNeededForContextParam(param);
                    return module;
                }
            }
        }
        return ModuleNeeded.newInstance(null, moduleValue, null, true);
    }

    /**
     * DOC YeXiaowei Comment method "findMoreLibraries".
     *
     * @param neededLibraries
     * @param curParam
     */
    public static void findMoreLibraries(final IProcess process, List<ModuleNeeded> modulesNeeded,
            IElementParameter curParam) {
        Object value = curParam.getValue();
        String name = curParam.getName();
        if (name.equals("DRIVER_JAR") || name.equals("connection.driverTable")) { //$NON-NLS-1$
            // added for bug 13592. new parameter DRIVER_JAR was used for jdbc connection
            if (value != null && value instanceof List) {
                List list = (List) value;
                for (Object element : list) {
                    if (element instanceof HashMap) {
                        HashMap map = (HashMap) element; // JAR_NAME
                        Object object = null;
                        if (name.equals("DRIVER_JAR")) { //$NON-NLS-1$
                            object = map.get("JAR_NAME"); //$NON-NLS-1$
                        } else {
                            object = map.get("drivers");//$NON-NLS-1$
                        }
                        if (object != null && object instanceof String) {
                            String driverName = (String) object;
                            if (!"".equals(driverName)) { //$NON-NLS-1$
                                boolean isContextMode = ContextParameterUtils.containContextVariables(driverName);
                                if (isContextMode) {
                                    getModulesInTable(process, curParam, modulesNeeded);
                                } else {
                                    ModuleNeeded module = null;
                                    String moduleName = TalendTextUtils.removeQuotes(driverName);
                                    if (StringUtils.isNotBlank((String) map.get("JAR_NEXUS_VERSION"))) {
                                        String mvnUrl = MavenUrlHelper.generateMvnUrl(MavenConstants.DEFAULT_LIB_GROUP_ID,
                                                moduleName.replaceFirst("[.][^.]+$", ""), (String) map.get("JAR_NEXUS_VERSION"),
                                                MavenConstants.TYPE_JAR, null);
                                        module = ModuleNeeded.newInstance(null, mvnUrl, null, true);
                                    } else {
                                        if (!moduleName.toLowerCase().endsWith(".jar")) {
                                            module = ModulesNeededProvider.getModuleNeededById(moduleName);
                                            // TDQ-18826 set required to true for tdqReportRun when it is Dynamic jars
                                            if (moduleName.startsWith("DYNAMIC_") && module != null) {
                                                module.setRequired(true);
                                            }
                                        }
                                        if (module == null) {
                                            module = ModuleNeeded.newInstance(null, moduleName, null, true);
                                        }
                                    }
                                    modulesNeeded.add(module);
                                }
                            }
                        }
                    }
                }
            }
        } else if (name.equals("DB_VERSION")) { //$NON-NLS-1$
            String jdbcName = (String) value;
            if (jdbcName != null) {
                String jars = (jdbcName).replaceAll(TalendTextUtils.QUOTATION_MARK, "").replaceAll( //$NON-NLS-1$
                        TalendTextUtils.SINGLE_QUOTE, ""); //$NON-NLS-1$
                String separator = ";"; //$NON-NLS-1$
                if (jars.contains(separator)) {
                    for (String jar : jars.split(separator)) {
                        if (!jar.contains(".")) { //$NON-NLS-1$
                            continue;
                        }
                        ModuleNeeded module = new ModuleNeeded(null, jar, null, true);
                        modulesNeeded.add(module);
                    }
                } else if (jars.contains(".")) { //$NON-NLS-1$
                    ModuleNeeded module = new ModuleNeeded(null, jars, null, true);
                    modulesNeeded.add(module);
                }
            }
        } else if (name.equals("HOTLIBS")) { //$NON-NLS-1$
            List<Map<String, Object>> tableValues = (List<Map<String, Object>>) value;
            Object[] listItemsValue = curParam.getListItemsValue();
            for (Map<String, Object> line : tableValues) {
                Object libPath = line.get("LIBPATH"); //$NON-NLS-1$
                if (libPath == null) {
                    continue;
                }
                String text = null;
                if (libPath instanceof String && !StringUtils.isEmpty((String) libPath)) {
                    text = (String) libPath;
                } else if (libPath instanceof Integer && listItemsValue != null) {
                    int index = ((Integer) libPath).intValue();
                    if (index > -1 && index < listItemsValue.length && listItemsValue[index] != null) {
                        if (listItemsValue[index] instanceof IElementParameter) {
                            text = (String) ((IElementParameter) listItemsValue[index]).getValue();
                        } else {
                            text = listItemsValue[index].toString();
                        }
                    }
                }
                if (text != null && text.contains(".")) { //$NON-NLS-1$
                    boolean isContextMode = ContextParameterUtils.containContextVariables(text);
                    if (isContextMode) {
                        List<IContext> listContext = process.getContextManager().getListContext();
                        String var = ContextParameterUtils.getVariableFromCode(text);
                        for (IContext context : listContext) {
                            List<IContextParameter> contextParameterList = context.getContextParameterList();
                            for (IContextParameter contextPara : contextParameterList) {
                                String paramName = contextPara.getName();
                                if (var.equals(paramName)) {
                                    ModuleNeeded module = getModuleNeededForContextParam(contextPara);
                                    if (module != null && !modulesNeeded.contains(module)) {
                                        module.setDynamic(true);
                                        modulesNeeded.add(module);
                                    }
                                }
                            }
                        }
                    } else {
                        ModuleNeeded module = new ModuleNeeded(null, TalendTextUtils.removeQuotes(text), null, true);
                        module.setDynamic(true);
                        modulesNeeded.add(module);
                    }
                }
            }
        } else if (name.equals(EParameterName.HADOOP_CUSTOM_JARS.getDisplayName())) {
            String jarsName = (String) value;
            if (StringUtils.isNotEmpty(jarsName)) {
                String jars = jarsName.replaceAll(TalendTextUtils.QUOTATION_MARK, "").replaceAll( //$NON-NLS-1$
                        TalendTextUtils.SINGLE_QUOTE, ""); //$NON-NLS-1$
                String separator = ";"; //$NON-NLS-1$
                for (String jar : jars.split(separator)) {
                    ModuleNeeded module = new ModuleNeeded(null, jar, null, true);
                    modulesNeeded.add(module);
                }
            }
        }
    }

    private static ModuleNeeded getModuleNeededForContextParam(IContextParameter contextParam) {
        if (contextParam != null) {
            String values = contextParam.getValue();
            if (StringUtils.isNotBlank(values)) {
                return ModuleNeeded.newInstance(null, values, null, true);
            }
        }
        return null;
    }

    /**
     *
     * DOC hcyi Comment method "getContextOriginalValue".
     *
     * @param process
     * @param contextStr
     */
    public static String getContextOriginalValue(final IProcess process, String contextStr) {
        String originalValue = null;
        IContext context = process.getContextManager().getDefaultContext();
        if (context != null) {
            List<IContextParameter> contextParameterList = context.getContextParameterList();
            for (IContextParameter contextPara : contextParameterList) {
                String var = ContextParameterUtils.getVariableFromCode(contextStr);
                if (var.equals(contextPara.getName())) {
                    originalValue = context.getContextParameter(contextPara.getName()).getValue();
                    return originalValue;
                }
            }
        }
        return null;
    }

    /**
     * DOC ycbai Comment method "getRealParamValueByRunProcess".
     * <p>
     * Get the real parameter value by resolving the selected context on the run process view.
     * </p>
     *
     * @param process
     * @param value
     * @return
     */
    public static String getRealParamValueByRunProcess(IProcess process, String value) {
        IContext selectedContext = CorePlugin.getDefault().getRunProcessService().getSelectedContext();
        return getRealParamValue(process, value, selectedContext);
    }

    /**
     * DOC ycbai Comment method "getRealParamValue".
     * <p>
     * Get the real parameter value by resolving the default context of process.
     * </p>
     *
     * @param process
     * @param value
     * @return
     */
    public static String getRealParamValue(IProcess process, String value) {
        return getRealParamValue(process, value, null);
    }

    /**
     * DOC ycbai Comment method "getRealParamValue".
     * <p>
     * Get the real parameter value by resolving the selected context.
     * </p>
     *
     * @param process
     * @param value
     * @param selectedContext
     * @return
     */
    public static String getRealParamValue(IProcess process, String value, IContext selectedContext) {
        if (ContextParameterUtils.isContainContextParam(value)) {
            String var = ContextParameterUtils.getVariableFromCode(value);
            if (var != null) {
                if (selectedContext == null) {
                    selectedContext = process.getContextManager().getDefaultContext();
                }
                IContextParameter param = selectedContext.getContextParameter(var);
                if (param != null) {
                    return param.getValue();
                }
            }
        }
        return value;
    }

    private static ModuleNeeded evaluateOsgiDependency(ModuleNeeded module, INode node) {
        if (node == null) {
            return module;
        }
        IProcess rawProcess = node.getProcess();
        if (!(rawProcess instanceof IProcess2)) {
            return module;
        }
        IProcess2 process = (IProcess2) rawProcess;
        if (!"CAMEL".equals(process.getComponentsType())) {
            return module;
        }
        String uniqueName = node.getUniqueName();
        if (uniqueName == null || !uniqueName.startsWith("cMessagingEndpoint")) {
            return module;
        }
        String moduleName = module.getModuleName();
        if (moduleName == null) {
            return module;
        }
        Map<Object, Object> additionalProperties = process.getAdditionalProperties();
        if (additionalProperties != null) {
            Object bundleClassPath = additionalProperties.get("Bundle-ClassPath");
            if (bundleClassPath instanceof String) {
                StringTokenizer bcp = new StringTokenizer((String) bundleClassPath, ",", false);
                while (bcp.hasMoreTokens()) {
                    String token = bcp.nextToken();
                    if (token.startsWith(moduleName)) {
                        return module;
                    }
                }
            }
        }
        module.getExtraAttributes().put("IS_OSGI_EXCLUDED", "true");
        return module;
    }

    public static boolean needMigration(String componentName, EList parameters) {
        if (parameters != null) {
            Map<String, String> properties = new HashMap<>();
            for (final Object elem : parameters) {
                ElementParameterTypeImpl parameter = (ElementParameterTypeImpl) elem;
                if (EParameterFieldType.TECHNICAL.name().equals(parameter.getField())
                        && parameter.getName().endsWith(".__version")) { //$NON-NLS-1$
                    properties.put(parameter.getName(), parameter.getValue());
                }
            }
            if (properties.size() > 0) {
                try {
                    ITaCoKitService tacokitService = ITaCoKitService.getInstance();
                    if (tacokitService != null) {
                        return tacokitService.isNeedMigration(componentName, properties);
                    }
                } catch (Exception e) {
                    ExceptionHandler.process(e);
                }
            }
        }
        return false;
    }

    @Deprecated
    public static List<String> getCodesExportJars(IProcess process) {
        if (process instanceof Process && GlobalServiceRegister.getDefault().isServiceRegistered(IRunProcessService.class)) {
            IRunProcessService service = GlobalServiceRegister.getDefault().getService(IRunProcessService.class);
            IProcessor processor = service.createCodeProcessor(process, ((Process) process).getProperty(), ECodeLanguage.JAVA,
                    true);
            if (processor != null) {
                return new ArrayList<>(getCodesExportJars(processor));
            }
        }
        List<String> codesJars = new ArrayList<>();
        // add routines always.
        codesJars.add(JavaUtils.ROUTINES_JAR);

        // Beans
        if (ProcessUtils.isRequiredBeans(process)) {
            codesJars.add(JavaUtils.BEANS_JAR);
        }

        // codesjar
        if (process instanceof Process) {
            codesJars.addAll(getCodesJarExportJarFromRoutinesParameterType(((Process) process).getRoutineDependencies()));
        }
        return codesJars;
    }

    public static Set<String> getCodesExportJars(IProcessor processor) {
        Set<String> codesJars = new HashSet<>();
        // add routines always.
        codesJars.add(JavaUtils.ROUTINES_JAR);

        // Beans
        if (ProcessUtils.isRequiredBeans(processor.getProcess())) {
            codesJars.add(JavaUtils.BEANS_JAR);
        }

        // codesjar
        codesJars.addAll(getCodesJarExportJarsWithChildren(processor));
        return codesJars;
    }

    private static Set<String> getCodesJarExportJarsWithChildren(IProcessor processor) {
        Set<String> codesJars = new HashSet<>();
        Item item = processor.getProperty().getItem();
        ITestContainerProviderService testContainerService = ITestContainerProviderService.get();
        if (testContainerService != null && testContainerService.isTestContainerItem(item)) {
            try {
                item = testContainerService.getParentJobItem(item);
            } catch (PersistenceException e) {
                ExceptionHandler.process(e);
            }
        }
        codesJars.addAll(getCodesJarExportJarFromRoutinesParameterType(RoutinesUtil.getRoutinesParametersFromItem(item)));
        processor.getBuildChildrenJobsAndJoblets().forEach(info -> codesJars
                .addAll(getCodesJarExportJarFromRoutinesParameterType(RoutinesUtil.getRoutinesParametersFromJobInfo(info))));
        return codesJars;
    }

    private static Set<String> getCodesJarExportJarFromRoutinesParameterType(List<RoutinesParameterType> routinesParameters) {
        return routinesParameters.stream().filter(r -> r.getType() != null)
                .map(r -> CodesJarResourceCache.getCodesJarById(r.getId())).filter(info -> info != null)
                .map(info -> info.getLabel().toLowerCase() + FileExtensions.JAR_FILE_SUFFIX)
                .collect(Collectors.toSet());
    }

}
