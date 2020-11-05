package org.talend.repository.ui.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.talend.commons.CommonsPlugin;
import org.talend.commons.exception.CommonExceptionHandler;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.components.ComponentCategory;
import org.talend.core.model.general.ModuleNeeded;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.process.IProcess2;
import org.talend.core.model.properties.Property;
import org.talend.core.runtime.maven.MavenUrlHelper;
import org.talend.core.runtime.process.LastGenerationInfo;
import org.talend.designer.runprocess.IRunProcessService;
import org.talend.librariesmanager.model.ModulesNeededProvider;

public class UpdateLog4jJarUtils {

    private static final String LOG4J_VERSION = "2.12.1";

    public static final String[] MODULES_NEED_UPDATE_ORDER = { "spark-assembly-1.6.0-hadoop2.6.0.jar" };

    public static void addLog4jToJarList(Collection<String> jarList, boolean isSelectLog4j2) {
        IProcess process = null;
        if (GlobalServiceRegister.getDefault().isServiceRegistered(IRunProcessService.class)) {
            IRunProcessService processService = (IRunProcessService) GlobalServiceRegister.getDefault()
                    .getService(IRunProcessService.class);
            process = processService.getActiveProcess();
        }
        List<String> modulesUsedBefore = removeLog4jFromJarListAndGetUsedJarBefore(process, jarList);
        addBackJars(jarList, isSelectLog4j2, modulesUsedBefore, process);
    }

    public static void addLog4jToModuleList(Collection<ModuleNeeded> jarList, boolean isSelectLog4j2, IProcess currentProcess) {
        List<ModuleNeeded> modulesUsedBefore = removeLog4jFromModuleListAndGetModulesUsedBefore(currentProcess, jarList);
        addBackModules(jarList, isSelectLog4j2, modulesUsedBefore, currentProcess);
    }

    public static final String[] MODULES_NEED_ADDED_BACK = { "log4j-jcl-"+LOG4J_VERSION+".jar", "log4j-jul-"+LOG4J_VERSION+".jar",
            "log4j-slf4j-impl-"+LOG4J_VERSION+".jar", "log4j-api-"+LOG4J_VERSION+".jar", "log4j-core-"+LOG4J_VERSION+".jar", "jcl-over-slf4j-1.7.25.jar",
            "jul-to-slf4j-1.7.25.jar", "log4j-to-slf4j-"+LOG4J_VERSION+".jar", "slf4j-log4j12-1.7.25.jar", "log4j-1.2.17.jar",
            "log4j-1.2-api-"+LOG4J_VERSION+".jar" };

    private static void addBackJars(Collection<String> moduleNeededList, boolean isSelectLog4j2, List<String> modulesUsedBefore,
            IProcess process) {
        boolean usedSlf4jApiJarBefore = false;
        if (isSelectLog4j2) {
            boolean usedlog4jJclBefore = false;
            boolean usedlog4jJulBefore = false;
            boolean usedlog4j1JarBefore = false;
            for (String module : modulesUsedBefore) {
                if (module.matches("log4j-jcl-\\d+\\.\\d+\\.\\d+\\.jar") //$NON-NLS-1$
                        || module.matches("commons-logging-\\d+\\.\\d+\\.\\d+\\.jar")) {//$NON-NLS-1$
                    usedlog4jJclBefore = true;
                }
                if (module.matches("log4j-jul-\\d+\\.\\d+\\.\\d+\\.jar")) { //$NON-NLS-1$
                    usedlog4jJulBefore = true;
                }
                if (module.matches("log4j-\\d+\\.\\d+\\.\\d+\\.jar")) { //$NON-NLS-1$
                    usedlog4j1JarBefore = true;
                }
                if (module.matches("slf4j-api-\\d+\\.\\d+\\.\\d+\\.jar")) {//$NON-NLS-1$
                    usedSlf4jApiJarBefore = true;
                }
            }
            if (process != null) {
                String componentsType = process.getComponentsType();
                if (!usedlog4j1JarBefore && (StringUtils.equals(ComponentCategory.CATEGORY_4_MAPREDUCE.getName(), componentsType)
                        || StringUtils.equals(ComponentCategory.CATEGORY_4_SPARK.getName(), componentsType)
                        || StringUtils.equals(ComponentCategory.CATEGORY_4_SPARKSTREAMING.getName(), componentsType))) {
                    usedlog4j1JarBefore = true;
                }
            }

            if (usedlog4jJclBefore) {
                moduleNeededList.add("log4j-jcl-"+LOG4J_VERSION+".jar");//$NON-NLS-1$
            }
            if (usedlog4jJulBefore) {
                moduleNeededList.add("log4j-jul-"+LOG4J_VERSION+".jar");//$NON-NLS-1$
            }
            if (usedlog4j1JarBefore) {
                moduleNeededList.add("log4j-1.2-api-"+LOG4J_VERSION+".jar");
            }
            moduleNeededList.add("log4j-slf4j-impl-"+LOG4J_VERSION+".jar");//$NON-NLS-1$
            moduleNeededList.add("log4j-api-"+LOG4J_VERSION+".jar");//$NON-NLS-1$
            moduleNeededList.add("log4j-core-"+LOG4J_VERSION+".jar");//$NON-NLS-1$
        } else {
            boolean usedjclOverSlf4jBefore = false;
            for (String module : modulesUsedBefore) {
                if (module.matches("jcl-over-slf4j-\\d+\\.\\d+\\.\\d+\\.jar") //$NON-NLS-1$
                        || module.matches("commons-logging-\\d+\\.\\d+\\.\\d+\\.jar")) {//$NON-NLS-1$
                    usedjclOverSlf4jBefore = true;
                }
                if (module.matches("slf4j-api-\\d+\\.\\d+\\.\\d+\\.jar")) {//$NON-NLS-1$
                    usedSlf4jApiJarBefore = true;
                }
            }
            if (usedjclOverSlf4jBefore) {
                moduleNeededList.add("jcl-over-slf4j-1.7.25.jar");//$NON-NLS-1$
            }

            moduleNeededList.add("log4j-to-slf4j-"+LOG4J_VERSION+".jar");//$NON-NLS-1$
            moduleNeededList.add("slf4j-log4j12-1.7.25.jar");//$NON-NLS-1$
            moduleNeededList.add("log4j-1.2.17.jar");//$NON-NLS-1$
        }
        if (usedSlf4jApiJarBefore) {
            moduleNeededList.add("slf4j-api-1.7.25.jar");
        }
    }

    private static void addBackModules(Collection<ModuleNeeded> moduleNeededList, boolean isSelectLog4j2,
            List<ModuleNeeded> modulesUsedBefore, IProcess process) {
        boolean usedSlf4jApiJarBefore = false;
        if (isSelectLog4j2) {
            boolean usedlog4jJclBefore = false;
            boolean usedlog4jJulBefore = false;
            boolean usedlog4j1JarBefore = false;
            for (ModuleNeeded module : modulesUsedBefore) {
                if (module.getModuleName().matches("log4j-jcl-\\d+\\.\\d+\\.\\d+\\.jar") //$NON-NLS-1$
                        || module.getModuleName().matches("commons-logging-\\d+\\.\\d+\\.\\d+\\.jar")) {//$NON-NLS-1$
                    usedlog4jJclBefore = true;
                }
                if (module.getModuleName().matches("log4j-jul-\\d+\\.\\d+\\.\\d+\\.jar")) { //$NON-NLS-1$
                    usedlog4jJulBefore = true;
                }
                if (module.getModuleName().matches("log4j-\\d+\\.\\d+\\.\\d+\\.jar")) { //$NON-NLS-1$

                    usedlog4j1JarBefore = true;
                }
                if (module.getModuleName().matches("slf4j-api-\\d+\\.\\d+\\.\\d+\\.jar")) {
                    usedSlf4jApiJarBefore = true;
                }
            }
            if (process != null) {
                String componentsType = process.getComponentsType();
                if (!usedlog4j1JarBefore && (StringUtils.equals(ComponentCategory.CATEGORY_4_MAPREDUCE.getName(), componentsType)
                        || StringUtils.equals(ComponentCategory.CATEGORY_4_SPARK.getName(), componentsType)
                        || StringUtils.equals(ComponentCategory.CATEGORY_4_SPARKSTREAMING.getName(), componentsType))) {
                    usedlog4j1JarBefore = true;
                }
            }

            if (usedlog4jJclBefore) {
                ModuleNeeded log4jJcl = new ModuleNeeded("org.apache.logging.log4j", "log4j-jcl-"+LOG4J_VERSION+".jar", null, true); //$NON-NLS-1$ //$NON-NLS-2$
                log4jJcl.setMavenUri("mvn:org.apache.logging.log4j/log4j-jcl/"+LOG4J_VERSION);//$NON-NLS-1$
                moduleNeededList.add(log4jJcl);
            }
            if (usedlog4jJulBefore) {
                ModuleNeeded log4jJul = new ModuleNeeded("org.apache.logging.log4j", "log4j-jul-"+LOG4J_VERSION+".jar", null, true); //$NON-NLS-1$ //$NON-NLS-2$
                log4jJul.setMavenUri("mvn:org.apache.logging.log4j/log4j-jul/"+LOG4J_VERSION);//$NON-NLS-1$
                moduleNeededList.add(log4jJul);
            }
            if (usedlog4j1JarBefore) {
                ModuleNeeded log4j1To2Api = new ModuleNeeded("org.apache.logging.log4j", "log4j-1.2-api-"+LOG4J_VERSION+".jar", null, true); //$NON-NLS-1$ //$NON-NLS-2$
                log4j1To2Api.setMavenUri("mvn:org.apache.logging.log4j/log4j-1.2-api/"+LOG4J_VERSION);//$NON-NLS-1$
                moduleNeededList.add(log4j1To2Api);
            }
            ModuleNeeded log4jSlf4jImpl = new ModuleNeeded("org.apache.logging.log4j", "log4j-slf4j-impl-"+LOG4J_VERSION+".jar", null, true); //$NON-NLS-1$ //$NON-NLS-2$
            log4jSlf4jImpl.setMavenUri("mvn:org.apache.logging.log4j/log4j-slf4j-impl/"+LOG4J_VERSION);//$NON-NLS-1$
            moduleNeededList.add(log4jSlf4jImpl);
            ModuleNeeded log4jApi = new ModuleNeeded("org.apache.logging.log4j", "log4j-api-"+LOG4J_VERSION+".jar", null, true); //$NON-NLS-1$ //$NON-NLS-2$
            log4jApi.setMavenUri("mvn:org.apache.logging.log4j/log4j-api/"+LOG4J_VERSION);//$NON-NLS-1$
            moduleNeededList.add(log4jApi);
            ModuleNeeded log4jCore = new ModuleNeeded("org.apache.logging.log4j", "log4j-core-"+LOG4J_VERSION+".jar", null, true); //$NON-NLS-1$ //$NON-NLS-2$
            log4jCore.setMavenUri("mvn:org.apache.logging.log4j/log4j-core/"+LOG4J_VERSION);//$NON-NLS-1$
            moduleNeededList.add(log4jCore);

        } else {
            boolean usedjclOverSlf4jBefore = false;

            for (ModuleNeeded module : modulesUsedBefore) {
                if (module.getModuleName().matches("jcl-over-slf4j-\\d+\\.\\d+\\.\\d+\\.jar") //$NON-NLS-1$
                        || module.getModuleName().matches("commons-logging-\\d+\\.\\d+\\.\\d+\\.jar")) { //$NON-NLS-1$
                    usedjclOverSlf4jBefore = true;
                }
                if (module.getModuleName().matches("slf4j-api-\\d+\\.\\d+\\.\\d+\\.jar")) {
                    usedSlf4jApiJarBefore = true;
                }
            }
            if (usedjclOverSlf4jBefore) {
                ModuleNeeded jclOverSlf4j = new ModuleNeeded("org.slf4j", "jcl-over-slf4j-1.7.25.jar", null, true); //$NON-NLS-1$ //$NON-NLS-2$
                jclOverSlf4j.setMavenUri("mvn:org.slf4j/jcl-over-slf4j/1.7.25");//$NON-NLS-1$
                moduleNeededList.add(jclOverSlf4j);
            }

            ModuleNeeded log4jToSlf4j = new ModuleNeeded("org.apache.logging.log4j", "log4j-to-slf4j-"+LOG4J_VERSION+".jar", null, true); //$NON-NLS-1$ //$NON-NLS-2$
            log4jToSlf4j.setMavenUri("mvn:org.apache.logging.log4j/log4j-to-slf4j/"+LOG4J_VERSION);//$NON-NLS-1$
            moduleNeededList.add(log4jToSlf4j);
            ModuleNeeded slf4jLog4j12 = new ModuleNeeded("org.slf4j", "slf4j-log4j12-1.7.25.jar", null, true); //$NON-NLS-1$ //$NON-NLS-2$
            slf4jLog4j12.setMavenUri("mvn:org.slf4j/slf4j-log4j12/1.7.25");//$NON-NLS-1$
            moduleNeededList.add(slf4jLog4j12);
            ModuleNeeded log4j = new ModuleNeeded("log4j", "log4j-1.2.17.jar", null, true); //$NON-NLS-1$ //$NON-NLS-2$
            log4j.setMavenUri("mvn:log4j/log4j/1.2.17");//$NON-NLS-1$
            moduleNeededList.add(log4j);
        }
        if (usedSlf4jApiJarBefore) {
            ModuleNeeded slf4jApi = new ModuleNeeded("org.slf4j", "slf4j-api-1.7.25.jar", null, true); //$NON-NLS-1$ //$NON-NLS-2$
            slf4jApi.setMavenUri("mvn:org.slf4j/slf4j-api/1.7.25");//$NON-NLS-1$
            moduleNeededList.add(slf4jApi);
        }

    }



    private static List<ModuleNeeded> removeLog4jFromModuleListAndGetModulesUsedBefore(IProcess process,
            Collection<ModuleNeeded> jarList) {
        Set<ModuleNeeded> highPriorityModuleNeeded = new LinkedHashSet<>();
        if (process instanceof IProcess2) {
            highPriorityModuleNeeded = getHighPriorityModuleNeeded((IProcess2) process);
        }
        List<ModuleNeeded> modulesUsedBefore = new ArrayList<ModuleNeeded>();
        Iterator<ModuleNeeded> iterator = jarList.iterator();
        while (iterator.hasNext()) {
            ModuleNeeded module = iterator.next();
            getSpecialModulesUsedBefore(modulesUsedBefore, module);
            if (highPriorityModuleNeeded != null && !highPriorityModuleNeeded.contains(module)
                    && isNeedRemoveModule(module, module.getModuleName())) {
                iterator.remove();
                if (CommonsPlugin.isDebugMode()) {
                    String processName = "";
                    if (process != null) {
                        processName = process.getName();
                    }
                    String warning = module.getModuleName() + " is removed for " + processName;//$NON-NLS-1$
                    CommonExceptionHandler.warn(warning);
                }
            }
        }
        return modulesUsedBefore;
    }

    public static final String[] SPECIALMODULESUSEDBEFORES = { "log4j-jcl-\\d+\\.\\d+\\.\\d+\\.jar", //$NON-NLS-1$
            "log4j-jul-\\d+\\.\\d+\\.\\d+\\.jar", "jcl-over-slf4j-\\d+\\.\\d+\\.\\d+\\.jar", //$NON-NLS-1$//$NON-NLS-2$
            "jul-to-slf4j-\\d+\\.\\d+\\.\\d+\\.jar", "commons-logging-\\d+\\.\\d+\\.\\d+\\.jar", //$NON-NLS-1$
            "log4j-\\d+\\.\\d+\\.\\d+\\.jar" };//$NON-NLS-1$

    public static final String[] NEED_REMOVE_THE_SAME_VERSION_MODULES = { "slf4j-api-\\d+\\.\\d+\\.\\d+\\.jar" };//$NON-NLS-1$

    private static List<ModuleNeeded> getSpecialModulesUsedBefore(List<ModuleNeeded> modulesUsedBefore, ModuleNeeded module) {
        for (String moduleUsedBefore : SPECIALMODULESUSEDBEFORES) {
            if (module.getModuleName().matches(moduleUsedBefore)) {
                modulesUsedBefore.add(module);
            }
        }

        for (String moduleUsedBefore : NEED_REMOVE_THE_SAME_VERSION_MODULES) {
            if (module.getModuleName().matches(moduleUsedBefore)) {
                modulesUsedBefore.add(module);
            }
        }
        return modulesUsedBefore;
    }

    private static List<String> getSpecialJarsUsedBefore(List<String> jarsUsedBefore, String jar) {
        for (String moduleUsedBefore : SPECIALMODULESUSEDBEFORES) {
            if (jar.matches(moduleUsedBefore)) {
                jarsUsedBefore.add(jar);
            }
        }
        for (String moduleUsedBefore : NEED_REMOVE_THE_SAME_VERSION_MODULES) {
            if (jar.matches(moduleUsedBefore)) {
                jarsUsedBefore.add(jar);
            }
        }
        return jarsUsedBefore;

    }

    private static Set<ModuleNeeded> getHighPriorityModuleNeeded(IProcess2 process) {
        Set<ModuleNeeded> highPriorityModuleNeeded = null;
        if (process != null) {
            Property property = process.getProperty();
            highPriorityModuleNeeded = LastGenerationInfo.getInstance().getHighPriorityModuleNeeded(property.getId(),
                    property.getVersion());
        }
        return highPriorityModuleNeeded == null ? new LinkedHashSet<>() : highPriorityModuleNeeded;
    }

    private static List<String> removeLog4jFromJarListAndGetUsedJarBefore(IProcess process, Collection<String> jarList) {
        Set<ModuleNeeded> highPriorityModuleNeeded = new LinkedHashSet<>();
        if (process instanceof IProcess2) {
            highPriorityModuleNeeded = getHighPriorityModuleNeeded((IProcess2) process);
        }
        List<String> jarsUsedBefore = new ArrayList<String>();
        Iterator<String> iterator = jarList.iterator();
        while (iterator.hasNext()) {
            String jar = iterator.next();
            getSpecialJarsUsedBefore(jarsUsedBefore, jar);
            if (!isHighPriorityModuleNeeded(highPriorityModuleNeeded, jar) && isNeedRemoveModule(null, jar)) {
                iterator.remove();
            }
        }
        return jarsUsedBefore;
    }

    private static boolean isHighPriorityModuleNeeded(Set<ModuleNeeded> highPriorityModuleNeeded, String jar) {
        if (highPriorityModuleNeeded != null) {
            for (ModuleNeeded moduel : highPriorityModuleNeeded) {
                if (StringUtils.equals(moduel.getModuleName(), jar)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static final String[] NEEDREMOVEMODULES = { "jcl-over-slf4j-\\d+\\.\\d+\\.\\d+\\.jar", //$NON-NLS-1$
            "log4j-to-slf4j-\\d+\\.\\d+\\.\\d+\\.jar", //$NON-NLS-1$ //$NON-NLS-2$
            "log4j-to-slf4j-\\d+\\.\\d+\\.\\d+\\.jar", "slf4j-log4j12-\\d+\\.\\d+\\.\\d+\\.jar", "log4j-\\d+\\.\\d+\\.\\d+\\.jar", //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
            "log4j-jcl-\\d+\\.\\d+\\.\\d+\\.jar", "log4j-jul-\\d+\\.\\d+\\.\\d+\\.jar", //$NON-NLS-1$//$NON-NLS-2$
            "log4j-slf4j-impl-\\d+\\.\\d+\\.\\d+\\.jar", "log4j-1.2-api-\\d+\\.\\d+\\.\\d+\\.jar", //$NON-NLS-1$//$NON-NLS-2$
            "log4j-core-\\d+\\.\\d+\\.\\d+\\.jar", "log4j-api-\\d+\\.\\d+\\.\\d+\\.jar", //$NON-NLS-1$//$NON-NLS-2$
            "slf4j-standard-\\d+\\.\\d+\\.\\d+\\.jar", "slf4j-api-\\d+\\.\\d+\\.\\d+\\.jar" };//$NON-NLS-1$ //$NON-NLS-2$

    private static boolean isNeedRemoveModule(ModuleNeeded module, String moduleName) {
        for (String needRemoveModuleName : NEEDREMOVEMODULES) {
            if (moduleName.matches(needRemoveModuleName)) {
                return true;
            }
        }
        if (module == null) {
            module = ModulesNeededProvider.getModuleNeededById(moduleName);
        }
        if (module != null && module.getMavenUri() != null) {
            String[] mvnSplit = module.getMavenUri().split(MavenUrlHelper.SEPERATOR);
            if (mvnSplit != null && mvnSplit.length > 0) {
                if (StringUtils.equals(mvnSplit[0], MavenUrlHelper.MVN_PROTOCOL + "ch.qos.logback")) {//$NON-NLS-1$
                    return true;
                }
            }
        }
        return false;
    }

    public static void sortClassPath4Log4j(Set<ModuleNeeded> highPriorityModuleNeeded, List<ModuleNeeded> neededModules) {
        Collections.sort(neededModules, new Comparator<ModuleNeeded>() {

            @Override
            public int compare(ModuleNeeded o1, ModuleNeeded o2) {
                if (highPriorityModuleNeeded == null) {

                    return 0;
                }
                for (String moduleName : MODULES_NEED_ADDED_BACK) {
                    if (StringUtils.equals(moduleName, o1.getModuleName())
                            && !StringUtils.equals(moduleName, o2.getModuleName())) {
                        return -1;
                    }
                    if (!StringUtils.equals(moduleName, o1.getModuleName())
                            && StringUtils.equals(moduleName, o2.getModuleName())) {
                        return 1;
                    }
                }
                return 0;

            }
        });

        Collections.sort(neededModules, new Comparator<ModuleNeeded>() {

            @Override
            public int compare(ModuleNeeded o1, ModuleNeeded o2) {
                for (String moduleName : MODULES_NEED_UPDATE_ORDER) {
                    if (StringUtils.equals(moduleName, o1.getModuleName())
                            && !StringUtils.equals(moduleName, o2.getModuleName())) {
                        return -1;
                    }
                    if (!StringUtils.equals(moduleName, o1.getModuleName())
                            && StringUtils.equals(moduleName, o2.getModuleName())) {
                        return 1;
                    }
                }
                return 0;

            }
        });

        Collections.sort(neededModules, new Comparator<ModuleNeeded>() {

            @Override
            public int compare(ModuleNeeded o1, ModuleNeeded o2) {
                if (highPriorityModuleNeeded == null) {
                    return 0;
                }
                for (ModuleNeeded module : highPriorityModuleNeeded) {
                    if (StringUtils.equals(module.getModuleName(), o1.getModuleName())
                            && !StringUtils.equals(module.getModuleName(), o2.getModuleName())) {
                        return -1;
                    }
                    if (!StringUtils.equals(module.getModuleName(), o1.getModuleName())
                            && StringUtils.equals(module.getModuleName(), o2.getModuleName())) {
                        return 1;
                    }
                }
                return 0;

            }
        });
    }

    public static void sortClassPath4log4j(Set<ModuleNeeded> highPriorityModuleNeeded, List<String> libNames) {
        Collections.sort(libNames, new Comparator<String>() {

            @Override
            public int compare(String o1, String o2) {

                for (String moduleName : UpdateLog4jJarUtils.MODULES_NEED_ADDED_BACK) {
                    if (StringUtils.equals(moduleName, o1) && !StringUtils.equals(moduleName, o2)) {
                        return -1;
                    }
                    if (!StringUtils.equals(moduleName, o1) && StringUtils.equals(moduleName, o2)) {
                        return 1;
                    }
                }
                return 0;

            }
        });

        Collections.sort(libNames, new Comparator<String>() {

            @Override
            public int compare(String o1, String o2) {
                for (String moduleName : UpdateLog4jJarUtils.MODULES_NEED_UPDATE_ORDER) {
                    if (StringUtils.equals(moduleName, o1) && !StringUtils.equals(moduleName, o2)) {
                        return -1;
                    }
                    if (!StringUtils.equals(moduleName, o1) && StringUtils.equals(moduleName, o2)) {
                        return 1;
                    }
                }
                return 0;

            }
        });

        Collections.sort(libNames, new Comparator<String>() {

            @Override
            public int compare(String o1, String o2) {
                if (highPriorityModuleNeeded == null) {
                    return 0;
                }
                for (ModuleNeeded module : highPriorityModuleNeeded) {
                    if (StringUtils.equals(module.getModuleName(), o1) && !StringUtils.equals(module.getModuleName(), o2)) {
                        return -1;
                    }
                    if (!StringUtils.equals(module.getModuleName(), o1) && StringUtils.equals(module.getModuleName(), o2)) {
                        return 1;
                    }
                }
                return 0;

            }
        });
    }
}
