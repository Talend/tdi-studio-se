// ============================================================================
//
// Copyright (C) 2006-2021 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.core.build;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.junit.Assert;
import org.junit.Test;
import org.talend.core.CorePlugin;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.Property;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.runtime.process.ITalendProcessJavaProject;
import org.talend.core.runtime.repository.build.IBuildExportHandler;
import org.talend.core.runtime.repository.build.IBuildJobParameters;
import org.talend.core.runtime.repository.build.IBuildParametes;
import org.talend.core.runtime.repository.build.IBuildPomCreatorParameters;
import org.talend.core.runtime.repository.build.IMavenPomCreator;
import org.talend.core.runtime.repository.build.RepositoryObjectTypeBuildProvider;
import org.talend.designer.core.IDesignerCoreService;
import org.talend.designer.maven.tools.creator.CreateMavenJobPom;
import org.talend.designer.runprocess.IProcessor;
import org.talend.designer.runprocess.ProcessorUtilities;
import org.talend.repository.RepositoryPlugin;
import org.talend.repository.ui.wizards.exportjob.handler.BuildJobHandler;
import org.talend.repository.ui.wizards.exportjob.scriptsmanager.JobScriptsManagerFactory;

/**
 * DOC ggu class global comment. Detailled comment
 */
public class StandardJobStandaloneBuildProviderTest extends AbstractStandardJobBuildProviderTest {

    @Override
    protected RepositoryObjectTypeBuildProvider createTestBuildProvider() {
        return new StandardJobStandaloneBuildProvider();
    }

    @Test
    public void test_createPomCreator_nullAssemblyFile() {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(IBuildPomCreatorParameters.PROCESSOR, new TestProcessor());
        ITalendProcessJavaProject talendProcessJavaProject = RepositoryPlugin.getDefault().getRunProcessService()
                .getTempJavaProject();
        parameters.put(IBuildPomCreatorParameters.FILE_POM, talendProcessJavaProject.getProject().getFile("pom_abc.xml"));
        parameters.put(IBuildParametes.ITEM, PropertiesFactory.eINSTANCE.createProcessItem());
        // parameters.put(IBuildPomCreatorParameters.FILE_ASSEMBLY, null);

        RepositoryObjectTypeBuildProvider provider = createTestBuildProvider();
        Assert.assertNull(provider.createPomCreator(parameters));
    }

    @Test
    public void test_createPomCreator_nonAssemblyFile() {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(IBuildPomCreatorParameters.PROCESSOR, new TestProcessor());
        ITalendProcessJavaProject talendProcessJavaProject = RepositoryPlugin.getDefault().getRunProcessService()
                .getTempJavaProject();
        parameters.put(IBuildPomCreatorParameters.FILE_POM, talendProcessJavaProject.getProject().getFile("pom_abc.xml"));
        parameters.put(IBuildParametes.ITEM, PropertiesFactory.eINSTANCE.createProcessItem());
        parameters.put(IBuildPomCreatorParameters.FILE_ASSEMBLY, new Object());

        RepositoryObjectTypeBuildProvider provider = createTestBuildProvider();
        Assert.assertNull(provider.createPomCreator(parameters));
    }

    @Test
    public void test_createPomCreator_nullWinCP() {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(IBuildPomCreatorParameters.PROCESSOR, new TestProcessor());
        ITalendProcessJavaProject talendProcessJavaProject = RepositoryPlugin.getDefault().getRunProcessService()
                .getTempJavaProject();
        parameters.put(IBuildPomCreatorParameters.FILE_POM, talendProcessJavaProject.getProject().getFile("pom_abc.xml"));
        parameters.put(IBuildParametes.ITEM, PropertiesFactory.eINSTANCE.createProcessItem());
        parameters.put(IBuildPomCreatorParameters.FILE_ASSEMBLY, talendProcessJavaProject.getProject().getFile("pom_abc.xml"));

        RepositoryObjectTypeBuildProvider provider = createTestBuildProvider();
        Assert.assertNull(provider.createPomCreator(parameters));
    }

    @Test
    public void test_createPomCreator_nullLinuxCP() {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(IBuildPomCreatorParameters.PROCESSOR, new TestProcessor());
        ITalendProcessJavaProject talendProcessJavaProject = RepositoryPlugin.getDefault().getRunProcessService()
                .getTempJavaProject();
        parameters.put(IBuildPomCreatorParameters.FILE_POM, talendProcessJavaProject.getProject().getFile("pom_abc.xml"));
        parameters.put(IBuildParametes.ITEM, PropertiesFactory.eINSTANCE.createProcessItem());
        parameters.put(IBuildPomCreatorParameters.FILE_ASSEMBLY, talendProcessJavaProject.getProject().getFile("pom_abc.xml"));
        parameters.put(IBuildPomCreatorParameters.CP_WIN, "");

        RepositoryObjectTypeBuildProvider provider = createTestBuildProvider();
        Assert.assertNull(provider.createPomCreator(parameters));
    }

    @Test
    public void test_createPomCreator() throws Exception {
        Map<String, Object> parameters = new HashMap<String, Object>();

        Item jobItem = createJobItem();

        final IDesignerCoreService designerCoreService = CorePlugin.getDefault().getDesignerCoreService();
        Assert.assertNotNull(designerCoreService);
        final IProcess processFromItem = designerCoreService.getProcessFromItem(jobItem);

        IProcessor processor = ProcessorUtilities.getProcessor(processFromItem, jobItem.getProperty());

        final Class<? extends IProcessor> processorClazz = processor.getClass();
        Assert.assertEquals("org.talend.designer.runprocess.maven.MavenJavaProcessor", processorClazz.getName());

        processor.setContext(processor.getProcess().getContextManager().getDefaultContext());

        parameters.put(IBuildPomCreatorParameters.PROCESSOR, processor);

        final Method getPomFileMethod = processorClazz.getDeclaredMethod("getPomFile");
        getPomFileMethod.setAccessible(true);
        final Object getPomFile = getPomFileMethod.invoke(processor);
        Assert.assertTrue(getPomFile instanceof IFile);
        IFile pomFile = (IFile) getPomFile;
        parameters.put(IBuildPomCreatorParameters.FILE_POM, pomFile);

        final Method getAssemblyFileMethod = processorClazz.getDeclaredMethod("getAssemblyFile");
        getAssemblyFileMethod.setAccessible(true);
        final Object getAssemblyFile = getAssemblyFileMethod.invoke(processor);
        Assert.assertTrue(getPomFile instanceof IFile);
        IFile assemblyFile = (IFile) getAssemblyFile;
        parameters.put(IBuildPomCreatorParameters.FILE_ASSEMBLY, assemblyFile);

        // before set classpath, must do initJobClasspath
        final Method initJobClasspathMethod = processorClazz.getDeclaredMethod("initJobClasspath");
        initJobClasspathMethod.setAccessible(true);
        initJobClasspathMethod.invoke(processor);

        final Field windowsClasspathField = processorClazz.getDeclaredField("windowsClasspath");
        windowsClasspathField.setAccessible(true);
        parameters.put(IBuildPomCreatorParameters.CP_WIN, windowsClasspathField.get(processor));

        final Field unixClasspathField = processorClazz.getDeclaredField("unixClasspath");
        unixClasspathField.setAccessible(true);
        parameters.put(IBuildPomCreatorParameters.CP_LINUX, unixClasspathField.get(processor));

        parameters.put(IBuildParametes.ITEM, processor.getProperty().getItem());

        final Map<String, Object> arguments = processor.getArguments();
        parameters.put(IBuildPomCreatorParameters.ARGUMENTS_MAP, arguments);
        parameters.put(IBuildPomCreatorParameters.OVERWRITE_POM, true);

        // create pom and assembly
        RepositoryObjectTypeBuildProvider provider = createTestBuildProvider();
        final IMavenPomCreator pomCreator = provider.createPomCreator(parameters);
        Assert.assertNotNull("Create the pom creator failure", pomCreator);
        Assert.assertEquals(CreateMavenJobPom.class, pomCreator.getClass());
        pomCreator.create(null);

        pomFile.getParent().refreshLocal(IResource.DEPTH_ONE, null);
        Assert.assertTrue("the pom file " + pomFile.getName() + " is not created ", pomFile.exists());

        assemblyFile.getParent().refreshLocal(IResource.DEPTH_ONE, null);
        Assert.assertTrue("the assembly file " + assemblyFile.getName() + " is not created ", assemblyFile.exists());
    }

    @Test
    public void test_createBuildExportHandler_emptyParameters() {
        RepositoryObjectTypeBuildProvider provider = createTestBuildProvider();
        Assert.assertNull(provider.createBuildExportHandler(null));
        Assert.assertNull(provider.createBuildExportHandler(Collections.emptyMap()));
    }

    @Test
    public void test_createBuildExportHandler_nullItem() {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("fakeKey", "fakeValue");

        RepositoryObjectTypeBuildProvider provider = createTestBuildProvider();
        Assert.assertNull(provider.createBuildExportHandler(parameters));
    }

    @Test
    public void test_createBuildExportHandler_nonItem() {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(IBuildParametes.ITEM, "fakeValue");

        RepositoryObjectTypeBuildProvider provider = createTestBuildProvider();
        Assert.assertNull(provider.createBuildExportHandler(parameters));

        parameters.put(IBuildParametes.ITEM, PropertiesFactory.eINSTANCE.createJobletProcessItem());
        Assert.assertNull(provider.createBuildExportHandler(parameters));
    }

    @Test
    public void test_createBuildExportHandler_nullVersion() {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(IBuildParametes.ITEM, PropertiesFactory.eINSTANCE.createProcessItem());

        RepositoryObjectTypeBuildProvider provider = createTestBuildProvider();
        Assert.assertNull(provider.createBuildExportHandler(parameters));
    }

    @Test
    public void test_createBuildExportHandler_nullContextGroup() {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(IBuildParametes.ITEM, PropertiesFactory.eINSTANCE.createProcessItem());
        parameters.put(IBuildParametes.VERSION, "0.1");

        RepositoryObjectTypeBuildProvider provider = createTestBuildProvider();
        Assert.assertNull(provider.createBuildExportHandler(parameters));
    }

    @Test
    public void test_createBuildExportHandler_nonChoiceMap() {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(IBuildParametes.ITEM, PropertiesFactory.eINSTANCE.createProcessItem());
        parameters.put(IBuildParametes.VERSION, "0.1");
        parameters.put(IBuildJobParameters.CONTEXT_GROUP, "Dev");
        parameters.put(IBuildJobParameters.CHOICE_OPTION, "xxx");

        RepositoryObjectTypeBuildProvider provider = createTestBuildProvider();
        Assert.assertNull(provider.createBuildExportHandler(parameters));
    }

    @Test
    public void test_createBuildExportHandler_nullChoiceMap() {
        Map<String, Object> parameters = new HashMap<String, Object>();
        Property property = PropertiesFactory.eINSTANCE.createProperty();
        property.setLabel("testBuildExport_NullMap");
        property.setVersion("1.0");
        property.setId(ProxyRepositoryFactory.getInstance().getNextId());
        ProcessItem item = PropertiesFactory.eINSTANCE.createProcessItem();
        item.setProperty(property);
        parameters.put(IBuildParametes.ITEM, item);
        parameters.put(IBuildParametes.VERSION, "0.1");
        parameters.put(IBuildJobParameters.CONTEXT_GROUP, "Dev");
        // parameters.put(IBuildJobParameters.CHOICE_OPTION, null);

        RepositoryObjectTypeBuildProvider provider = createTestBuildProvider();
        final IBuildExportHandler buildExportHandler = provider.createBuildExportHandler(parameters);
        Assert.assertNotNull(buildExportHandler);
        Assert.assertEquals(BuildJobHandler.class, buildExportHandler.getClass());
    }

    @Test
    public void test_createBuildExportHandler() {
        Map<String, Object> parameters = new HashMap<String, Object>();
        Property property = PropertiesFactory.eINSTANCE.createProperty();
        property.setLabel("testBuildExport");
        property.setVersion("1.0");
        property.setId(ProxyRepositoryFactory.getInstance().getNextId());
        ProcessItem item = PropertiesFactory.eINSTANCE.createProcessItem();
        item.setProperty(property);
        parameters.put(IBuildParametes.ITEM, item);
        parameters.put(IBuildParametes.VERSION, "0.1");
        parameters.put(IBuildJobParameters.CONTEXT_GROUP, "Dev");
        parameters.put(IBuildJobParameters.CHOICE_OPTION, JobScriptsManagerFactory.getDefaultExportChoiceMap());

        RepositoryObjectTypeBuildProvider provider = createTestBuildProvider();
        final IBuildExportHandler buildExportHandler = provider.createBuildExportHandler(parameters);
        Assert.assertNotNull(buildExportHandler);
        Assert.assertEquals(BuildJobHandler.class, buildExportHandler.getClass());
    }
}
