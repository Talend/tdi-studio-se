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
package org.talend.designer.runprocess.java;

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.talend.commons.utils.VersionUtils;
import org.talend.commons.utils.generation.JavaUtils;
import org.talend.commons.utils.system.EnvironmentUtils;
import org.talend.core.model.general.ModuleNeeded;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.Property;
import org.talend.designer.core.model.utils.emf.talendfile.TalendFileFactory;
import org.talend.designer.core.ui.editor.process.Process;
import org.talend.designer.maven.utils.PomUtil;
import org.talend.designer.runprocess.ProcessorException;
import org.talend.designer.runprocess.ProcessorUtilities;

/**
 * 
 * created by hcyi on Jul 22, 2019 Detailled comment
 *
 */
public class JavaProcessorTest {

    /**
     * Test method for {@link org.talend.designer.runprocess.Processor#replaceSnippet(java.lang.String)}.
     * 
     * @throws ProcessorException
     */
    @Test
    public void testReplaceSnippet() throws ProcessorException {

    }

    @Test
    public void testGetCommandLine4ExportConfig() throws ProcessorException {
        Property property = PropertiesFactory.eINSTANCE.createProperty();
        property.setId("_rHnrstwXEeijXfdWFqSaEA"); //$NON-NLS-1$
        property.setLabel("test"); //$NON-NLS-1$
        property.setVersion(VersionUtils.DEFAULT_VERSION);
        Process process = new Process(property);

        JavaProcessor processor = new JavaProcessor(process, property, false);

        // only for export
        ProcessorUtilities.setExportConfig(JavaUtils.JAVA_APP_NAME, null, null);

        String[] cmd = processor.getCommandLine();

        Assert.assertTrue(cmd.length > 2);
        Assert.assertEquals(processor.extractAheadCommandSegments().toString(), Arrays.asList(cmd).subList(0, 2).toString());
    }

    @Test
    public void testGetCommandLine4ExecutionIsNotStandardJob() throws ProcessorException {
        Property property = PropertiesFactory.eINSTANCE.createProperty();
        property.setId("_rHnrstwXEeijXfdWFqSaEA"); //$NON-NLS-1$
        property.setLabel("test"); //$NON-NLS-1$
        property.setVersion(VersionUtils.DEFAULT_VERSION);
        Process process = new Process(property);

        JavaProcessor processor = new JavaProcessor(process, property, false);
        //
        ProcessorUtilities.setExportConfig(JavaUtils.JAVA_APP_NAME, null, null, false, new Date());

        String[] cmd = processor.getCommandLine();
        Assert.assertFalse(Arrays.asList(cmd).contains(getLocalM2Path()));
    }

    @Test
    public void testGetCommandLine4ExecutionIsStandardJob() throws ProcessorException {
        Property property = PropertiesFactory.eINSTANCE.createProperty();
        property.setId("_rHnrstwXEeijXfdWFqSaEA"); //$NON-NLS-1$
        property.setLabel("test"); //$NON-NLS-1$
        property.setVersion(VersionUtils.DEFAULT_VERSION);

        ProcessItem processItem = PropertiesFactory.eINSTANCE.createProcessItem();
        processItem.setProperty(property);
        processItem.setProcess(TalendFileFactory.eINSTANCE.createProcessType());
        processItem.setState(PropertiesFactory.eINSTANCE.createItemState());

        Process process = new Process(property);

        JavaProcessor processor = new JavaProcessor(process, property, false);
        //
        ProcessorUtilities.setExportConfig(JavaUtils.JAVA_APP_NAME, null, null, false, new Date());

        String[] cmd = processor.getCommandLine();
        Assert.assertTrue(Arrays.asList(cmd).contains(getLocalM2Path()));

        Assert.assertEquals(getLocalM2Path(), Arrays.asList(cmd).get(3));
    }

    @Test
    public void testGetCommandLine4ExecutionIsGuessSchemaJob() throws ProcessorException {
        Property property = PropertiesFactory.eINSTANCE.createProperty();
        property.setId("ID"); //$NON-NLS-1$
        property.setLabel("Mock_job_for_Guess_schema"); //$NON-NLS-1$
        property.setVersion(VersionUtils.DEFAULT_VERSION);

        Process process = new Process(property);

        JavaProcessor processor = new JavaProcessor(process, property, false);
        //
        ProcessorUtilities.setExportConfig(JavaUtils.JAVA_APP_NAME, null, null, false, new Date());

        String[] cmd = processor.getCommandLine();
        Assert.assertTrue(Arrays.asList(cmd).contains(getLocalM2Path()));

        Assert.assertEquals(getLocalM2Path(), Arrays.asList(cmd).get(3));
    }

    @Test
    public void testRemoveLowerVersionArtifacts() throws Exception{
        List<ModuleNeeded> neededModules = new ArrayList<ModuleNeeded>();
        Set<ModuleNeeded> highPriorityModuleNeeded = new HashSet<ModuleNeeded>();
        neededModules.add(createModule("commons-lang3", "1.2.0"));
        neededModules.add(createModule("commons-lang3", "1.3.0"));
        neededModules.add(createModule("commons-lang3", "1.4.0"));

        // can not remove 1.2.0
        highPriorityModuleNeeded.add(createModule("commons-lang3", "1.2.0"));

        // remove 1.3.0
        JavaProcessor.removeLowerVersionArtifacts(neededModules, highPriorityModuleNeeded);
        assert (neededModules.size() == 2);
        for (ModuleNeeded mod : neededModules) {
            if (!mod.getModuleName().equals("commons-lang3-1.2.0.jar")
                    || !mod.getModuleName().equals("commons-lang3-1.4.0.jar")) {
                fail("shoud found 1.2.0 and 1.4.0 only");
            }
        }

        // can remove 1.2.0
        highPriorityModuleNeeded.clear();
        // remove 1.2.0
        JavaProcessor.removeLowerVersionArtifacts(neededModules, highPriorityModuleNeeded);
        assert (neededModules.size() == 1);
        assert (neededModules.get(0).getModuleName().equals("commons-lang3-1.4.0.jar"));
    }

    private ModuleNeeded createModule(String jarName, String version) {
        String mvnURI = "mvn:org.talend.libraries/" + jarName + "-" + version + "/6.0.0/jar";
        ModuleNeeded module = new ModuleNeeded("test", jarName + "-" + version + ".jar", "test", true, null, null, mvnURI);
        return module;
    }

    private String getLocalM2Path() {
        String localM2Path = "-Dtalend.component.manager.m2.repository="; //$NON-NLS-1$
        if (EnvironmentUtils.isWindowsSystem()) {
            localM2Path = localM2Path + "\"" + PomUtil.getLocalRepositoryPath() + "\""; //$NON-NLS-1$ //$NON-NLS-2$
        } else {
            localM2Path = localM2Path + PomUtil.getLocalRepositoryPath();
        }
        return localM2Path;
    }
}
