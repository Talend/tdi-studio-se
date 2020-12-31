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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.talend.core.CorePlugin;
import org.talend.core.hadoop.IHadoopClusterService;
import org.talend.core.hadoop.repository.HadoopRepositoryUtil;
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
import org.talend.core.model.utils.ContextParameterUtils;
import org.talend.core.model.utils.TalendTextUtils;
import org.talend.core.runtime.process.LastGenerationInfo;
import org.talend.core.runtime.process.TalendProcessOptionConstants;
import org.talend.core.utils.BitwiseOptionUtils;
import org.talend.designer.core.IDesignerCoreService;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.model.components.EmfComponent;
import org.talend.designer.runprocess.ItemCacheManager;
import org.talend.librariesmanager.model.ModulesNeededProvider;

/**
 * DOC xye class global comment. Detailled comment
 */
public class JavaProcessUtil {

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
            } else if (dedupModulesList.contains(module.getModuleName())) {
                if (module.isMrRequired() && previousModule != null
                        && previousModule.getModuleName().equals(module.getModuleName())) {
                    previousModule.setMrRequired(Boolean.TRUE);
                }
                it.remove();
            } else {
                dedupModulesList.add(module.getModuleName());
                previousModule = module;
            }
        }

        if (BitwiseOptionUtils.containOption(options, TalendProcessOptionConstants.MODULES_EXCLUDE_SHADED)) {
            new BigDataJobUtil(process).removeExcludedModules(modulesNeeded);
        }

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

        if (process instanceof IProcess2) {
            Item item = ((IProcess2) process).getProperty().getItem();
            if (item instanceof ProcessItem) {
                modulesNeeded.addAll(ModulesNeededProvider.getModulesNeededForProcess((ProcessItem) item, process));
            }
        }

        boolean isTestcaseProcess = ProcessUtils.isTestContainer(process);
        if (isTestcaseProcess) {// if it is a test container, add junit jars.
            addJunitNeededModules(modulesNeeded);
        }

        String hadoopItemId = null;
        List<INode> nodeList = (List<INode>) process.getGeneratingNodes();

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
                    LastGenerationInfo.getInstance().setHighPriorityModuleNeeded(process.getId(), process.getVersion(),
                            highPriorityModuleNeeded);
                }
            }
        }

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

    public static String getHadoopClusterItemId(INode node) {
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
        if (!EmfComponent.REPOSITORY.equals(propertyType)) {
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

    private static void addJunitNeededModules(List<ModuleNeeded> modulesNeeded) {
        ModuleNeeded junitModule = new ModuleNeeded("junit", "junit.jar", null, true); //$NON-NLS-1$ //$NON-NLS-2$
        junitModule.setModuleLocaion("platform:/plugin/org.junit/junit.jar");
        junitModule.setMavenUri("mvn:org.talend.libraries/junit/6.0.0");
        modulesNeeded.add(junitModule);
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

    private static void getModulesInTable(final IProcess process, IElementParameter curParam,
            List<ModuleNeeded> modulesNeeded) {

        if (!(curParam.getValue() instanceof List)) {
            return;
        }
        List<Map<String, Object>> values = (List<Map<String, Object>>) curParam.getValue();
        if (values != null && !values.isEmpty()) {
            boolean updateCustomMavenUri = false;
            Object[] listItemsValue = curParam.getListItemsValue();
            if (listItemsValue != null && listItemsValue.length > 0 && listItemsValue[0] instanceof IElementParameter) {
                for (Object o : listItemsValue) {
                    IElementParameter param = (IElementParameter) o;
                    if (param.getFieldType() == EParameterFieldType.MODULE_LIST) {
                        for (Map<String, Object> line : values) {
                            String moduleName = (String) line.get(param.getName());
                            if (moduleName != null && !"".equals(moduleName)) { //$NON-NLS-1$

                                boolean isContextMode = ContextParameterUtils.containContextVariables(moduleName);
                                if (isContextMode) {
                                    List<IContext> listContext = process.getContextManager().getListContext();
                                    for (IContext context : listContext) {
                                        List<IContextParameter> contextParameterList =
                                                context.getContextParameterList();
                                        for (IContextParameter contextPara : contextParameterList) {
                                            String var = ContextParameterUtils.getVariableFromCode(moduleName);
                                            if (var.equals(contextPara.getName())) {
                                                String value =
                                                        context.getContextParameter(contextPara.getName()).getValue();
                                                if (StringUtils.isBlank(value)) {
                                                    continue;
                                                }
                                                if (curParam.getName().equals(EParameterName.DRIVER_JAR.getName())
                                                        && value.contains(";")) { //$NON-NLS-1$
                                                    String[] jars = value.split(";"); //$NON-NLS-1$
                                                    for (String jar2 : jars) {
                                                        String jar = jar2;
                                                        jar = jar.substring(jar.lastIndexOf("\\") + 1); //$NON-NLS-1$
                                                        ModuleNeeded module = null;
                                                        String jarName = TalendTextUtils.removeQuotes(jar);
                                                        if (!jarName.toLowerCase().endsWith(".jar")) {
                                                            module = ModulesNeededProvider
                                                                    .getModuleNeededById(jarName);
                                                            if (module == null) {
                                                                module = new ModuleNeeded(null, jarName, null, true);
                                                            }
                                                        } else {
                                                            module = new ModuleNeeded(null, jarName, null, true);
                                                        }
                                                        modulesNeeded.add(module);
                                                    }
                                                } else if (curParam.getName().equals("connection.driverTable") //$NON-NLS-1$
                                                        && value.contains(";")) { //$NON-NLS-1$
                                                    String[] jars = value.split(";"); //$NON-NLS-1$
                                                    for (String jar2 : jars) {
                                                        String jar = jar2;
                                                        jar = jar.substring(jar.lastIndexOf("\\") + 1); //$NON-NLS-1$
                                                        ModuleNeeded module = new ModuleNeeded(null,
                                                                TalendTextUtils.removeQuotes(jar), null, true);
                                                        modulesNeeded.add(module);
                                                    }
                                                } else {
                                                    value = value.substring(value.lastIndexOf("\\") + 1); //$NON-NLS-1$

                                                    ModuleNeeded module = new ModuleNeeded(null,
                                                            TalendTextUtils.removeQuotes(value), null, true);
                                                    modulesNeeded.add(module);
                                                }
                                            }
                                        }
                                    }

                                } else {
                                    ModuleNeeded mn = null;
                                    if (!moduleName.toLowerCase().endsWith(".jar")) {
                                        mn = ModulesNeededProvider.getModuleNeededById(moduleName);
                                        if (mn == null) {
                                            mn = getModuleValue(process, moduleName);
                                        }
                                    } else {
                                        mn = getModuleValue(process, moduleName);
                                    }

                                    String nexusJarVersion = (String) line.get("JAR_NEXUS_VERSION");
                                    if (StringUtils.isNotBlank(nexusJarVersion)) {
                                        String a = moduleName.replaceFirst("[.][^.]+$", "");
                                        mn.setMavenUri("mvn:org.talend.libraries/" + a + "/"
                                                + line.get("JAR_NEXUS_VERSION") + "/jar");

                                    }
                                    modulesNeeded.add(mn);
                                }
                            }
                        }
                    }
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
        return new ModuleNeeded(null, TalendTextUtils.removeQuotes(moduleValue), null, true);
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
                boolean updateCustomMavenUri = false;
                for (int i = 0; i < list.size(); i++) {
                    if (list.get(i) instanceof HashMap) {
                        HashMap map = (HashMap) list.get(i); // JAR_NAME
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

                                    if (StringUtils.isNotBlank((String) map.get("JAR_NEXUS_VERSION"))) {
                                        module = new ModuleNeeded(null, null, true,
                                                "mvn:org.talend.libraries/"
                                                        + TalendTextUtils.removeQuotes(driverName).replaceFirst(
                                                                "[.][^.]+$", "")
                                                        + "/" + (String) map.get("JAR_NEXUS_VERSION") + "/jar");

                                    } else {
                                        String moduleName = TalendTextUtils.removeQuotes(driverName);
                                        if (!moduleName.toLowerCase().endsWith(".jar")) {
                                            module = ModulesNeededProvider.getModuleNeededById(moduleName);
                                            if (module == null) {
                                                module = new ModuleNeeded(null, moduleName, null, true);
                                            }
                                        } else {
                                            module = new ModuleNeeded(null, moduleName, null, true);
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
                IPath path = new Path(values); // if it's path
                String fileName = path.lastSegment(); // get the file name only.
                ModuleNeeded module = new ModuleNeeded(null, fileName, null, true);
                return module;
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
}
