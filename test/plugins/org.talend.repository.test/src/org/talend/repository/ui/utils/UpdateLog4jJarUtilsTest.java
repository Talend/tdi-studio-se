package org.talend.repository.ui.utils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.talend.core.model.general.ModuleNeeded;

public class UpdateLog4jJarUtilsTest {

    @Test
    public void testAddLog4jToJarList() {
        List<String> allLog4jModulesName = getAllLog4jModulesName();
        UpdateLog4jJarUtils.addLog4jToJarList(allLog4jModulesName, true);// is log4j2
        List<String> modules4log4j2 = new ArrayList<>();
        for (String moule : allLog4jModulesName) {
            modules4log4j2.add(moule);
        }
        assertTrue(modules4log4j2.contains("log4j-core-2.12.1.jar"));
        assertTrue(modules4log4j2.contains("log4j-api-2.12.1.jar"));
        assertTrue(modules4log4j2.contains("log4j-jcl-2.12.1.jar"));
        assertTrue(modules4log4j2.contains("log4j-jul-2.12.1.jar"));
        assertTrue(modules4log4j2.contains("log4j-slf4j-impl-2.12.1.jar"));

        assertFalse(modules4log4j2.contains("jcl-over-slf4j-1.7.25.jar"));
        assertFalse(modules4log4j2.contains("log4j-to-slf4j-2.12.1.jar"));
        assertFalse(modules4log4j2.contains("slf4j-log4j12-1.7.25.jar"));
        assertFalse(modules4log4j2.contains("log4j-1.2.17.jar"));

        allLog4jModulesName = getAllLog4jModulesName();
        UpdateLog4jJarUtils.addLog4jToJarList(allLog4jModulesName, false);// is log4j1
        List<String> modules4log4j1 = new ArrayList<>();
        for (String moule : allLog4jModulesName) {
            modules4log4j1.add(moule);
        }
        assertFalse(modules4log4j1.contains("log4j-core-2.12.1.jar"));
        assertFalse(modules4log4j1.contains("log4j-api-2.12.1.jar"));
        assertFalse(modules4log4j1.contains("log4j-jcl-2.12.1.jar"));
        assertFalse(modules4log4j1.contains("log4j-jul-2.12.1.jar"));
        assertFalse(modules4log4j1.contains("log4j-slf4j-impl-2.12.1.jar"));

        assertTrue(modules4log4j1.contains("jcl-over-slf4j-1.7.25.jar"));
        assertTrue(modules4log4j1.contains("jul-to-slf4j-1.7.25.jar"));
        assertTrue(modules4log4j1.contains("log4j-to-slf4j-2.12.1.jar"));
        assertTrue(modules4log4j1.contains("slf4j-log4j12-1.7.25.jar"));
        assertTrue(modules4log4j1.contains("log4j-1.2.17.jar"));

    }

    @Test
    public void testAddLog4jToModuleList() {
        List<ModuleNeeded> allLog4jModules = getAllLog4jModules();
        UpdateLog4jJarUtils.addLog4jToModuleList(allLog4jModules, true, null);// is log4j2
        List<String> modules4log4j2 = new ArrayList<>();
        for (ModuleNeeded moule : allLog4jModules) {
            modules4log4j2.add(moule.getMavenUri());
        }
        assertTrue(modules4log4j2.contains("mvn:org.apache.logging.log4j/log4j-core/2.12.1/jar"));
        assertTrue(modules4log4j2.contains("mvn:org.apache.logging.log4j/log4j-api/2.12.1/jar"));
        assertTrue(modules4log4j2.contains("mvn:org.apache.logging.log4j/log4j-jcl/2.12.1/jar"));
        assertTrue(modules4log4j2.contains("mvn:org.apache.logging.log4j/log4j-jul/2.12.1/jar"));
        assertTrue(modules4log4j2.contains("mvn:org.apache.logging.log4j/log4j-slf4j-impl/2.12.1/jar"));

        assertFalse(modules4log4j2.contains("mvn:org.slf4j/jcl-over-slf4j/1.7.25/jar"));
        assertFalse(modules4log4j2.contains("mvn:org.apache.logging.log4j/log4j-to-slf4j/2.12.1/jar"));
        assertFalse(modules4log4j2.contains("mvn:org.slf4j/slf4j-log4j12/1.7.25/jar"));
        assertFalse(modules4log4j2.contains("mvn:log4j/log4j/1.2.17/jar"));

        allLog4jModules = getAllLog4jModules();
        UpdateLog4jJarUtils.addLog4jToModuleList(allLog4jModules, false, null);// is log4j1
        List<String> modules4log4j1 = new ArrayList<>();
        for (ModuleNeeded moule : allLog4jModules) {
            modules4log4j1.add(moule.getMavenUri());
        }
        assertFalse(modules4log4j1.contains("mvn:org.apache.logging.log4j/log4j-core/2.12.1/jar"));
        assertFalse(modules4log4j1.contains("mvn:org.apache.logging.log4j/log4j-api/2.12.1/jar"));
        assertFalse(modules4log4j1.contains("mvn:org.apache.logging.log4j/log4j-jcl/2.12.1/jar"));
        assertFalse(modules4log4j1.contains("mvn:org.apache.logging.log4j/log4j-jul/2.12.1/jar"));
        assertFalse(modules4log4j1.contains("mvn:org.apache.logging.log4j/log4j-slf4j-impl/2.12.1/jar"));

        assertTrue(modules4log4j1.contains("mvn:org.slf4j/jcl-over-slf4j/1.7.25/jar"));
        assertTrue(modules4log4j1.contains("mvn:org.slf4j/jul-to-slf4j/1.7.25/jar"));
        assertTrue(modules4log4j1.contains("mvn:org.apache.logging.log4j/log4j-to-slf4j/2.12.1/jar"));
        assertTrue(modules4log4j1.contains("mvn:org.slf4j/slf4j-log4j12/1.7.25/jar"));
        assertTrue(modules4log4j1.contains("mvn:log4j/log4j/1.2.17/jar"));

    }

    private List<String> getAllLog4jModulesName() {
        List<String> jarList = new ArrayList<>();
        // log2
        jarList.add("log4j-core-2.12.1.jar");//$NON-NLS-1$
        jarList.add("log4j-api-2.12.1.jar");//$NON-NLS-1$
        jarList.add("log4j-jcl-2.12.1.jar");//$NON-NLS-1$
        jarList.add("log4j-jul-2.12.1.jar");//$NON-NLS-1$
        jarList.add("log4j-slf4j-impl-2.12.1.jar");//$NON-NLS-1$
        // log1
        jarList.add("jcl-over-slf4j-1.7.25.jar");//$NON-NLS-1$
        jarList.add("jul-to-slf4j-1.7.25.jar");//$NON-NLS-1$
        jarList.add("log4j-to-slf4j-2.12.1.jar");//$NON-NLS-1$
        jarList.add("slf4j-log4j12-1.7.25.jar");//$NON-NLS-1$
        jarList.add("log4j-1.2.17.jar");//$NON-NLS-1$
        return jarList;
    }
    private List<ModuleNeeded> getAllLog4jModules() {
        List<ModuleNeeded> jarList = new ArrayList<>();
        // log2
        ModuleNeeded log4jCore = new ModuleNeeded("org.apache.logging.log4j", "log4j-core-2.12.1.jar", null, true); //$NON-NLS-1$ //$NON-NLS-2$
        log4jCore.setMavenUri("mvn:org.apache.logging.log4j/log4j-core/2.12.1");//$NON-NLS-1$
        jarList.add(log4jCore);
        ModuleNeeded log4jApi = new ModuleNeeded("org.apache.logging.log4j", "log4j-api-2.12.1.jar", null, true); //$NON-NLS-1$ //$NON-NLS-2$
        log4jApi.setMavenUri("mvn:org.apache.logging.log4j/log4j-api/2.12.1");//$NON-NLS-1$
        jarList.add(log4jApi);
        ModuleNeeded log4jJcl = new ModuleNeeded("org.apache.logging.log4j", "log4j-jcl-2.12.1.jar", null, true); //$NON-NLS-1$ //$NON-NLS-2$
        log4jJcl.setMavenUri("mvn:org.apache.logging.log4j/log4j-jcl/2.12.1");//$NON-NLS-1$
        jarList.add(log4jJcl);
        ModuleNeeded log4jJul = new ModuleNeeded("org.apache.logging.log4j", "log4j-jul-2.12.1.jar", null, true); //$NON-NLS-1$ //$NON-NLS-2$
        log4jJul.setMavenUri("mvn:org.apache.logging.log4j/log4j-jul/2.12.1");//$NON-NLS-1$
        jarList.add(log4jJul);
        ModuleNeeded log4jSlf4jImpl = new ModuleNeeded("org.apache.logging.log4j", "log4j-slf4j-impl-2.12.1.jar", null, true); //$NON-NLS-1$ //$NON-NLS-2$
        log4jSlf4jImpl.setMavenUri("mvn:org.apache.logging.log4j/log4j-slf4j-impl/2.12.1");//$NON-NLS-1$
        jarList.add(log4jSlf4jImpl);

        // log1
        ModuleNeeded jclOverSlf4j = new ModuleNeeded("org.slf4j", "jcl-over-slf4j-1.7.25.jar", null, true); //$NON-NLS-1$ //$NON-NLS-2$
        jclOverSlf4j.setMavenUri("mvn:org.slf4j/jcl-over-slf4j/1.7.25");//$NON-NLS-1$
        jarList.add(jclOverSlf4j);
        ModuleNeeded julToSlf4j = new ModuleNeeded("org.slf4j", "jul-to-slf4j-1.7.25.jar", null, true); //$NON-NLS-1$ //$NON-NLS-2$
        julToSlf4j.setMavenUri("mvn:org.slf4j/jul-to-slf4j/1.7.25");//$NON-NLS-1$
        jarList.add(julToSlf4j);
        ModuleNeeded log4jToSlf4j = new ModuleNeeded("org.apache.logging.log4j", "log4j-to-slf4j-2.12.1.jar", null, true); //$NON-NLS-1$ //$NON-NLS-2$
        log4jToSlf4j.setMavenUri("mvn:org.apache.logging.log4j/log4j-to-slf4j/2.12.1");//$NON-NLS-1$
        jarList.add(log4jToSlf4j);
        ModuleNeeded slf4jLog4j12 = new ModuleNeeded("org.slf4j", "slf4j-log4j12-1.7.25.jar", null, true); //$NON-NLS-1$ //$NON-NLS-2$
        slf4jLog4j12.setMavenUri("mvn:org.slf4j/slf4j-log4j12/1.7.25");//$NON-NLS-1$
        jarList.add(slf4jLog4j12);
        ModuleNeeded log4j = new ModuleNeeded("log4j", "log4j-1.2.17.jar", null, true); //$NON-NLS-1$ //$NON-NLS-2$
        log4j.setMavenUri("mvn:log4j/log4j/1.2.17");//$NON-NLS-1$
        jarList.add(log4j);
        return jarList;
    }
}
