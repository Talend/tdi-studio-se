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
        List<String> jarList = new ArrayList<>();
        jarList.add("log4j-1.2.17.jar");//$NON-NLS-1$
        jarList.add("log4j-core-2.12.1.jar");//$NON-NLS-1$
        jarList.add("log4j-api-2.12.1.jar");//$NON-NLS-1$

        boolean addLog4jToJarListLog4j1 = UpdateLog4jJarUtils.addLog4jToJarList(jarList, true);
        assertFalse(addLog4jToJarListLog4j1);
        assertTrue(jarList.contains("log4j-core-2.12.1.jar"));//$NON-NLS-1$
        assertTrue(jarList.contains("log4j-api-2.12.1.jar"));//$NON-NLS-1$
        assertFalse(jarList.contains("log4j-1.2.17.jar"));//$NON-NLS-1$

        boolean addLog4jToJarListLog4j2 = UpdateLog4jJarUtils.addLog4jToJarList(jarList, false);
        assertTrue(addLog4jToJarListLog4j2);
        assertTrue(jarList.contains("log4j-1.2.17.jar"));//$NON-NLS-1$
        assertFalse(jarList.contains("log4j-core-2.12.1.jar"));//$NON-NLS-1$
        assertFalse(jarList.contains("log4j-api-2.12.1.jar"));//$NON-NLS-1$
    }

    @Test
    public void testAddLog4jToModuleList() {
        List<ModuleNeeded> jarList = new ArrayList<>();
        ModuleNeeded log4j = new ModuleNeeded("log4j", "log4j-1.2.17.jar", null, true); //$NON-NLS-1$ //$NON-NLS-2$
        log4j.setMavenUri("mvn:log4j/log4j/1.2.17");//$NON-NLS-1$
        ModuleNeeded log4jCore = new ModuleNeeded("org.apache.logging.log4j", "log4j-core-2.12.1.jar", null, true); //$NON-NLS-1$ //$NON-NLS-2$
        log4jCore.setMavenUri("mvn:org.apache.logging.log4j/log4j-core/2.12.1");//$NON-NLS-1$
        ModuleNeeded log4jApi = new ModuleNeeded("org.apache.logging.log4j", "log4j-api-2.12.1.jar", null, true); //$NON-NLS-1$ //$NON-NLS-2$
        log4jApi.setMavenUri("mvn:org.apache.logging.log4j/log4j-api/2.12.1");//$NON-NLS-1$
        jarList.add(log4j);
        jarList.add(log4jCore);
        jarList.add(log4jApi);

        boolean addLog4jToJarListLog4j1 = UpdateLog4jJarUtils.addLog4jToModuleList(jarList, true);
        assertFalse(addLog4jToJarListLog4j1);
        assertTrue(jarList.contains(log4jCore));
        assertTrue(jarList.contains(log4jApi));
        assertFalse(jarList.contains(log4j));

        boolean addLog4jToJarListLog4j2 = UpdateLog4jJarUtils.addLog4jToModuleList(jarList, false);
        assertTrue(addLog4jToJarListLog4j2);
        assertTrue(jarList.contains(log4j));
        assertFalse(jarList.contains(log4jCore));
        assertFalse(jarList.contains(log4jApi));
    }
}
