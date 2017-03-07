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
package org.talend.designer.core.build;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.junit.Assert;
import org.junit.Test;
import org.talend.core.CorePlugin;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.properties.Item;
import org.talend.core.runtime.repository.build.IBuildParametes;
import org.talend.core.runtime.repository.build.IBuildPomCreatorParameters;
import org.talend.core.runtime.repository.build.IMavenPomCreator;
import org.talend.core.runtime.repository.build.RepositoryObjectTypeBuildProvider;
import org.talend.designer.core.IDesignerCoreService;
import org.talend.designer.maven.tools.creator.CreateMavenStandardJobOSGiPom;
import org.talend.designer.runprocess.IProcessor;
import org.talend.designer.runprocess.ProcessorUtilities;

/**
 * DOC ggu class global comment. Detailled comment
 */
public class StandardJobOSGiBundleBuildProviderTest extends AbstractStandardJobBuildProviderTest {

    class StandardJobOSGiBundleBuildProviderTestClass extends StandardJobOSGiBundleBuildProvider {

    }

    @Override
    protected RepositoryObjectTypeBuildProvider createTestBuildProvider() {
        return new StandardJobOSGiBundleBuildProviderTestClass();
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

        parameters.put(IBuildParametes.ITEM, processor.getProperty().getItem());

        final Map<String, Object> arguments = processor.getArguments();
        parameters.put(IBuildPomCreatorParameters.ARGUMENTS_MAP, arguments);
        parameters.put(IBuildPomCreatorParameters.OVERWRITE_POM, true);

        // create pom and assembly
        RepositoryObjectTypeBuildProvider provider = createTestBuildProvider();
        final IMavenPomCreator pomCreator = provider.createPomCreator(parameters);
        Assert.assertNotNull("Create the pom creator failure", pomCreator);
        Assert.assertEquals(CreateMavenStandardJobOSGiPom.class, pomCreator.getClass());
        pomCreator.create(null);

        pomFile.getParent().refreshLocal(IResource.DEPTH_ONE, null);
        Assert.assertTrue("the pom file " + pomFile.getName() + " is not created ", pomFile.exists());
    }
}
