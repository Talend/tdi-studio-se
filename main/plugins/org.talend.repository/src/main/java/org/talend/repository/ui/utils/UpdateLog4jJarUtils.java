package org.talend.repository.ui.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.talend.core.model.general.ModuleNeeded;

public class UpdateLog4jJarUtils {

    public static boolean addLog4jToJarList(Collection<String> jarList, boolean isSelectLog4j2) {
        List<String> moduleNeededList = new ArrayList<String>();
        List<String> moduleDeleteList = new ArrayList<String>();
        if (isSelectLog4j2) {
            boolean foundLog4j2CoreJar = false;
            boolean foundLog4j2ApiJar = false;
//            boolean foundLog4j2AdapterJar = false;
            for (String jar : jarList) {
                if (jar.matches("log4j-\\d+\\.\\d+\\.\\d+\\.jar")) { //$NON-NLS-1$
                    moduleDeleteList.add(jar);
                }
                if (jar.matches("log4j-core-\\d+\\.\\d+\\.\\d+\\.jar")) { //$NON-NLS-1$
                    foundLog4j2CoreJar = true;
                }
                if (jar.matches("log4j-api-\\d+\\.\\d+\\.\\d+\\.jar")) { //$NON-NLS-1$
                    foundLog4j2ApiJar = true;
                }
//                if (jar.matches("log4j-\\d+\\.\\d+\\-api-2.12.1.jar")) { //$NON-NLS-1$
//                    foundLog4j2AdapterJar = true;
//                }
            }
            if (!foundLog4j2CoreJar) {
                moduleNeededList.add("log4j-core-2.12.1.jar");//$NON-NLS-1$

            }
            if (!foundLog4j2ApiJar) {
                moduleNeededList.add("log4j-api-2.12.1.jar");//$NON-NLS-1$
            }
//            if (!foundLog4j2AdapterJar) {
//                moduleNeededList.add("log4j-1.2-api-2.12.1.jar");//$NON-NLS-1$
//            }

        } else {
            boolean foundLog4jJar = false;
            for (String jar : jarList) {
                if (jar.matches("log4j-\\d+\\.\\d+\\.\\d+\\.jar")) { //$NON-NLS-1$
                    foundLog4jJar = true;
                }
                if (jar.matches("log4j-core-\\d+\\.\\d+\\.\\d+\\.jar")) { //$NON-NLS-1$
                    moduleDeleteList.add(jar);
                }
                if (jar.matches("log4j-api-\\d+\\.\\d+\\.\\d+\\.jar")) { //$NON-NLS-1$
                    moduleDeleteList.add(jar);
                }
//                if (jar.matches("log4j-\\d+\\.\\d+\\-api-2.12.1.jar")) { //$NON-NLS-1$
//                    moduleDeleteList.add(jar);
//                }
            }
            if (!foundLog4jJar) {
                moduleNeededList.add("log4j-1.2.17.jar");//$NON-NLS-1$
            }

        }
        jarList.removeAll(moduleDeleteList);
        jarList.addAll(moduleNeededList);

        return moduleNeededList.size() > 0;
    }

    public static boolean addLog4jToModuleList(Collection<ModuleNeeded> jarList, boolean isSelectLog4j2) {

        List<ModuleNeeded> moduleNeededList = new ArrayList<ModuleNeeded>();
        List<ModuleNeeded> moduleDeleteList = new ArrayList<ModuleNeeded>();
        if (isSelectLog4j2) {
            boolean foundLog4j2CoreJar = false;
            boolean foundLog4j2ApiJar = false;
//            boolean foundLog4j2AdapterJar = false;
            for (ModuleNeeded jar : jarList) {
                if (jar.getModuleName().matches("log4j-\\d+\\.\\d+\\.\\d+\\.jar")) { //$NON-NLS-1$
                    moduleDeleteList.add(jar);
                }
                if (jar.getModuleName().matches("log4j-core-\\d+\\.\\d+\\.\\d+\\.jar")) { //$NON-NLS-1$
                    foundLog4j2CoreJar = true;
                }
                if (jar.getModuleName().matches("log4j-api-\\d+\\.\\d+\\.\\d+\\.jar")) { //$NON-NLS-1$
                    foundLog4j2ApiJar = true;
                }
//                if (jar.getModuleName().matches("log4j-\\d+\\.\\d+\\-api-2.12.1.jar")) { //$NON-NLS-1$
//                    foundLog4j2AdapterJar = true;
//                }
            }

            if (!foundLog4j2CoreJar) {
                ModuleNeeded log4jCore = new ModuleNeeded("org.apache.logging.log4j", "log4j-core-2.12.1.jar", null, true); //$NON-NLS-1$ //$NON-NLS-2$
                log4jCore.setMavenUri("mvn:org.apache.logging.log4j/log4j-core/2.12.1");//$NON-NLS-1$
                moduleNeededList.add(log4jCore);

            }
            if (!foundLog4j2ApiJar) {
                ModuleNeeded log4jApi = new ModuleNeeded("org.apache.logging.log4j", "log4j-api-2.12.1.jar", null, true); //$NON-NLS-1$ //$NON-NLS-2$
                log4jApi.setMavenUri("mvn:org.apache.logging.log4j/log4j-api/2.12.1");//$NON-NLS-1$
                moduleNeededList.add(log4jApi);
            }
//            if (!foundLog4j2AdapterJar) {
//                ModuleNeeded log4jCore = new ModuleNeeded("org.apache.logging.log4j", "log4j-1.2-api-2.12.1.jar", null, true); //$NON-NLS-1$ //$NON-NLS-2$
//                log4jCore.setMavenUri("mvn:org.apache.logging.log4j/log4j-1.2-api/2.12.1");//$NON-NLS-1$
//                moduleNeededList.add(log4jCore);
//            }

        } else {
            boolean foundLog4jJar = false;
            for (ModuleNeeded jar : jarList) {
                if (jar.getModuleName().matches("log4j-\\d+\\.\\d+\\.\\d+\\.jar")) { //$NON-NLS-1$
                    foundLog4jJar = true;
                }
                if (jar.getModuleName().matches("log4j-core-\\d+\\.\\d+\\.\\d+\\.jar")) { //$NON-NLS-1$
                    moduleDeleteList.add(jar);
                }
                if (jar.getModuleName().matches("log4j-api-\\d+\\.\\d+\\.\\d+\\.jar")) { //$NON-NLS-1$
                    moduleDeleteList.add(jar);
                }
//                if (jar.getModuleName().matches("log4j-\\d+\\.\\d+\\-api-2.12.1.jar")) { //$NON-NLS-1$
//                    moduleDeleteList.add(jar);
//                }
            }
            if (!foundLog4jJar) {
                ModuleNeeded log4j = new ModuleNeeded("log4j", "log4j-1.2.17.jar", null, true); //$NON-NLS-1$ //$NON-NLS-2$
                log4j.setMavenUri("mvn:log4j/log4j/1.2.17");//$NON-NLS-1$
                moduleNeededList.add(log4j);
            }

        }

        jarList.removeAll(moduleDeleteList);
        jarList.addAll(moduleNeededList);

        return moduleNeededList.size() > 0;
    }
}
