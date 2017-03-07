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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Path;
import org.junit.Assert;
import org.junit.Test;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.utils.VersionUtils;
import org.talend.core.CorePlugin;
import org.talend.core.context.Context;
import org.talend.core.context.RepositoryContext;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.Property;
import org.talend.core.model.routines.RoutinesUtil;
import org.talend.core.runtime.process.ITalendProcessJavaProject;
import org.talend.core.runtime.repository.build.IBuildParametes;
import org.talend.core.runtime.repository.build.IBuildPomCreatorParameters;
import org.talend.core.runtime.repository.build.RepositoryObjectTypeBuildProvider;
import org.talend.designer.core.DesignerPlugin;
import org.talend.designer.core.model.utils.emf.talendfile.ParametersType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;
import org.talend.designer.core.model.utils.emf.talendfile.RoutinesParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.TalendFileFactory;
import org.talend.repository.RepositoryPlugin;
import org.talend.repository.model.IProxyRepositoryFactory;

/**
 * DOC ggu class global comment. Detailled comment
 */
public abstract class AbstractStandardJobBuildProviderTest {

    protected abstract RepositoryObjectTypeBuildProvider createTestBuildProvider();

    @Test
    public void test_createPomCreator_emptyParam() {
        RepositoryObjectTypeBuildProvider provider = createTestBuildProvider();
        Assert.assertNull(provider.createPomCreator(null));
        Assert.assertNull(provider.createPomCreator(Collections.emptyMap()));
    }

    @Test
    public void test_createPomCreator_nullProcessor() {
        Map<String, Object> parameters = new HashMap<String, Object>();
        // parameters.put(IBuildPomCreatorParameters.PROCESSOR, null);

        RepositoryObjectTypeBuildProvider provider = createTestBuildProvider();
        Assert.assertNull(provider.createPomCreator(parameters));
    }

    @Test
    public void test_createPomCreator_nonProcessor() {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(IBuildPomCreatorParameters.PROCESSOR, new Object());

        RepositoryObjectTypeBuildProvider provider = createTestBuildProvider();
        Assert.assertNull(provider.createPomCreator(parameters));
    }

    @Test
    public void test_createPomCreator_nullPomFile() {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(IBuildPomCreatorParameters.PROCESSOR, new TestProcessor());
        // parameters.put(IBuildPomCreatorParameters.FILE_POM, null);

        RepositoryObjectTypeBuildProvider provider = createTestBuildProvider();
        Assert.assertNull(provider.createPomCreator(parameters));
    }

    @Test
    public void test_createPomCreator_nonPomFile() {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(IBuildPomCreatorParameters.PROCESSOR, new TestProcessor());
        parameters.put(IBuildPomCreatorParameters.FILE_POM, new Object());

        RepositoryObjectTypeBuildProvider provider = createTestBuildProvider();
        Assert.assertNull(provider.createPomCreator(parameters));
    }

    @Test
    public void test_createPomCreator_nullItem() {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(IBuildPomCreatorParameters.PROCESSOR, new TestProcessor());
        ITalendProcessJavaProject talendProcessJavaProject = RepositoryPlugin.getDefault().getRunProcessService()
                .getTalendProcessJavaProject();
        parameters.put(IBuildPomCreatorParameters.FILE_POM, talendProcessJavaProject.getProject().getFile("pom_abc.xml"));

        RepositoryObjectTypeBuildProvider provider = createTestBuildProvider();
        Assert.assertNull(provider.createPomCreator(parameters));
    }

    @Test
    public void test_createPomCreator_nonItem() {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(IBuildPomCreatorParameters.PROCESSOR, new TestProcessor());
        ITalendProcessJavaProject talendProcessJavaProject = RepositoryPlugin.getDefault().getRunProcessService()
                .getTalendProcessJavaProject();
        parameters.put(IBuildPomCreatorParameters.FILE_POM, talendProcessJavaProject.getProject().getFile("pom_abc.xml"));
        parameters.put(IBuildParametes.ITEM, new Object());

        RepositoryObjectTypeBuildProvider provider = createTestBuildProvider();
        Assert.assertNull(provider.createPomCreator(parameters));
    }

    @Test
    public void test_createPomCreator_nonArgumentsMap() {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(IBuildPomCreatorParameters.PROCESSOR, new TestProcessor());
        ITalendProcessJavaProject talendProcessJavaProject = RepositoryPlugin.getDefault().getRunProcessService()
                .getTalendProcessJavaProject();
        parameters.put(IBuildPomCreatorParameters.FILE_POM, talendProcessJavaProject.getProject().getFile("pom_abc.xml"));
        parameters.put(IBuildParametes.ITEM, PropertiesFactory.eINSTANCE.createProcessItem());
        parameters.put(IBuildPomCreatorParameters.ARGUMENTS_MAP, new Object());

        RepositoryObjectTypeBuildProvider provider = createTestBuildProvider();
        Assert.assertNull(provider.createPomCreator(parameters));
    }

    protected Item createJobItem() throws PersistenceException {
        final IProxyRepositoryFactory repositoryFactory = DesignerPlugin.getDefault().getRepositoryService()
                .getProxyRepositoryFactory();
        final String testJobId = repositoryFactory.getNextId();
        final String testJobLabel = StandardJobStandaloneBuildProvider.class.getSimpleName() + System.currentTimeMillis();

        // copied from NewProcessWizard
        Property property = PropertiesFactory.eINSTANCE.createProperty();
        property.setAuthor(((RepositoryContext) CorePlugin.getContext().getProperty(Context.REPOSITORY_CONTEXT_KEY)).getUser());
        property.setVersion(VersionUtils.DEFAULT_VERSION);
        property.setStatusCode("");
        property.setId(testJobId);
        property.setDisplayName(testJobLabel);
        property.setLabel(property.getDisplayName());

        ProcessItem processItem = PropertiesFactory.eINSTANCE.createProcessItem();
        processItem.setProperty(property);
        property.setItem(processItem);

        ProcessType process = TalendFileFactory.eINSTANCE.createProcessType();
        ParametersType parameterType = TalendFileFactory.eINSTANCE.createParametersType();
        List<RoutinesParameterType> dependenciesInPreference = RoutinesUtil.createDependenciesInPreference();
        parameterType.getRoutinesParameter().addAll(dependenciesInPreference);
        process.setParameters(parameterType);
        processItem.setProcess(process);

        repositoryFactory.create(processItem, new Path(""));

        return processItem;
    }

}
