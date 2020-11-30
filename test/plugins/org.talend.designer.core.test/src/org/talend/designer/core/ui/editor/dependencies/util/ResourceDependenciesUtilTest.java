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
package org.talend.designer.core.ui.editor.dependencies.util;

import static org.junit.Assert.fail;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.utils.workbench.resources.ResourceUtils;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.language.LanguageManager;
import org.talend.core.model.context.JobContextManager;
import org.talend.core.model.context.JobContextParameter;
import org.talend.core.model.metadata.types.JavaTypesManager;
import org.talend.core.model.process.IContext;
import org.talend.core.model.process.IContextManager;
import org.talend.core.model.process.IContextParameter;
import org.talend.core.model.process.IProcess2;
import org.talend.core.model.properties.ByteArray;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.resources.ResourceItem;
import org.talend.core.model.resources.ResourcesFactory;
import org.talend.core.model.utils.JavaResourcesHelper;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.designer.core.model.utils.emf.talendfile.ParametersType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;
import org.talend.designer.core.model.utils.emf.talendfile.TalendFileFactory;
import org.talend.designer.core.ui.editor.dependencies.model.JobResourceDependencyModel;
import org.talend.designer.core.ui.editor.process.Process;
import org.talend.repository.ProjectManager;

/**
 * DOC jding  class global comment. Detailled comment
 */
public class ResourceDependenciesUtilTest {

    private ResourceItem item;

    private ResourceItem item2;

    private ProcessItem jobItem;

    private ProcessItem processItem;

    private IProcess2 process;

    File fakefile;

    @Before
    public void before() {
        ProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
        item = ResourcesFactory.eINSTANCE.createResourceItem();
        item2 = ResourcesFactory.eINSTANCE.createResourceItem();
        Property property = PropertiesFactory.eINSTANCE.createProperty();
        property.setId(factory.getNextId());
        item.setProperty(property);
        property.setLabel("myResource");
        property.setVersion("0.1");
        property.setItem(item);

        Property property2 = PropertiesFactory.eINSTANCE.createProperty();
        property2.setId(factory.getNextId());
        item2.setProperty(property2);
        property2.setLabel("myResource2");
        property2.setVersion("0.1");
        property2.setItem(item2);

        ByteArray byteArray = PropertiesFactory.eINSTANCE.createByteArray();
        byteArray.setInnerContent(new byte[0]);
        item.setContent(byteArray);
        item.setBindingExtension("txt");
        item2.setContent(byteArray);
        item2.setBindingExtension("txt");
        createJobProperty();
        try {
            factory.create(item, new Path(""));
            factory.create(item2, new Path(""));
            factory.create(processItem, new Path(""));
            factory.create(jobItem, new Path(""));
            // fake file
            IProject project = ResourceUtils.getProject(ProjectManager.getInstance().getCurrentProject().getTechnicalLabel());
            String resourcePath = "resources/" + property.getLabel() + "_" + property.getVersion() + "."
                    + item.getBindingExtension();
            fakefile = project.getFile(new Path(resourcePath)).getLocation().toFile();
            if (!fakefile.exists()) {
                fakefile.createNewFile();
            }
        } catch (Exception e) {
            e.printStackTrace();
            fail("Test ResourceDependenciesUtilTest failure.");
        }

    }

    @Test
    public void testCreateDependency() {

        Property itemproperty = item.getProperty();
        String jobLabel = JavaResourcesHelper.getJobFolderName(processItem.getProperty().getLabel(),
                processItem.getProperty().getVersion());
        Assert.assertEquals("myResource", ResourceDependenciesUtil.createDependency(itemproperty.getId(),
                itemproperty.getVersion(), jobLabel, process).toString());

    }

    @Test
    public void testGetResourcePath() {
    	String projectName = ProjectManager.getInstance().getCurrentProject().getTechnicalLabel().toLowerCase();
        String expectResult = projectName+"/test_0_1/resources/myResource_0.1.txt";
        JobResourceDependencyModel model = new JobResourceDependencyModel(item);
        String jobLabel = JavaResourcesHelper.getJobFolderName(processItem.getProperty().getLabel(),
                processItem.getProperty().getVersion());
        String resourcePath = ResourceDependenciesUtil.getResourcePath(model, jobLabel, null);
        Assert.assertEquals(expectResult, resourcePath);

    }

    @Test
    public void testGetJobExecuteResourceFilePath() {
        String technicalLabel = ProjectManager.getInstance().getCurrentProject().getTechnicalLabel();
        Property jobProperty = processItem.getProperty();
        JobResourceDependencyModel model = new JobResourceDependencyModel(item);
        String projectLocation = null;
        IRepositoryViewObject jobObject = null;
        try {
            jobObject = ProxyRepositoryFactory.getInstance().getSpecificVersion(jobProperty.getId(),
                    jobProperty.getVersion(), true);
            projectLocation = ResourceUtils.getProject(technicalLabel).getLocation().toOSString();
        } catch (PersistenceException e) {
            e.printStackTrace();
            fail("Test ResourceDependenciesUtilTest.testGetJobExecuteResourceFilePath() failure");
        }
        Assert.assertNotNull(jobObject);
        Assert.assertNotNull(projectLocation);
        String relativeResourcePath = technicalLabel.toLowerCase() + "/test_0_1/resources/myResource_0.1.txt";
        IPath expectPath = new Path(projectLocation + "/" + ResourceDependenciesUtil.getProcessFolder(jobObject)
                + "test_0.1/src/main/ext-resources/" + relativeResourcePath);
        IPath fullResourcePath = new Path(
                ResourceDependenciesUtil.getJobExecuteResourceFilePath(model, jobProperty,
                        JavaResourcesHelper.getJobFolderName(jobProperty.getLabel(), jobProperty.getVersion()), null));
        Assert.assertEquals(expectPath.toOSString(), fullResourcePath.toOSString());
        
    }


    @Test
    public void testGetSetContextVarForResources() {
        IContextParameter originalParameter = ResourceDependenciesUtil.getContextOfResouceByProcess(process,
                item.getProperty().getId());
        Assert.assertNotNull(originalParameter);
        Assert.assertTrue(originalParameter.getValue().split("\\|").length > 1);
        Assert.assertEquals(JobResourceDependencyModel.LATEST_VERSION, originalParameter.getValue().split("\\|")[1]);

        List<JobResourceDependencyModel> models = new ArrayList<JobResourceDependencyModel>();
        JobResourceDependencyModel model = new JobResourceDependencyModel(item);
        model.setSelectedVersion("0.1");
        model.setContextVar("resContextPar");
        model.setContextSource(IContextParameter.BUILT_IN);
        models.add(model);
        JobResourceDependencyModel model2 = new JobResourceDependencyModel(item2);
        model.setSelectedVersion("0.1");
        model.setContextVar("resContextPar2");
        model.setContextSource(IContextParameter.BUILT_IN);
        models.add(model2);
        ResourceDependenciesUtil.setContextVarForResources(process, models);
        Assert.assertTrue(process.getContextManager().getDefaultContext().getContextParameterList().size() == 2);

        IContextParameter contextParameter = ResourceDependenciesUtil.getContextOfResouceByProcess(process,
                item.getProperty().getId());
        Assert.assertNotNull(contextParameter);
        Assert.assertTrue(contextParameter.getValue().split("\\|").length > 1);
        Assert.assertEquals("0.1", contextParameter.getValue().split("\\|")[1]);
    }

    @Test
    public void testCopyToExtResourceFolder() {
        ProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
        JobResourceDependencyModel model = new JobResourceDependencyModel(item);
        IRepositoryViewObject resourceObject = null;
        IProject project = null;
        try {
            resourceObject = factory.getLastVersion(item.getProperty().getId());
            project = ResourceUtils.getProject(ProjectManager.getInstance().getCurrentProject().getTechnicalLabel());
        } catch (PersistenceException e) {
            e.printStackTrace();
            fail("Test ResourceDependenciesUtilTest.testCopyToExtResourceFolder() failure");
        }

        if (resourceObject != null && project != null) {
            // for jobLabel all lowercase
            Property property = processItem.getProperty();
            // refer to AggregatorPomsHelper.getJobProjectFolderName() test_0.1 => test_0.1
            String jobProjectFolderName = property.getLabel().toLowerCase() + "_" + property.getVersion();
            // resource path: aa\test_0_1\resources\myResource_0.1.txt
            String resourcePath = ResourceDependenciesUtil.getResourcePath(model,
                    JavaResourcesHelper.getJobFolderName(property.getLabel(), property.getVersion()), null);
            ResourceDependenciesUtil.copyToExtResourceFolder(resourceObject, property.getId(), property.getVersion(), null, null);
            // copy to location:
            // AA\poms\jobs\process\test_0.1\src\main\ext-resources\aa\test_0_1\resources\myResource_0.1.txt
            File file = project
                    .getFile(new Path("poms/jobs/process/" + jobProjectFolderName + "/src/main/ext-resources/" + resourcePath))
                    .getLocation().toFile();
            Assert.assertTrue(file.exists());

            // for jobLabel contains upercase
            Property property2 = jobItem.getProperty();
            // refer to AggregatorPomsHelper.getJobProjectFolderName() Ttest_0.1=>ttest_0.1
            String jobProjectFolderName2 = property2.getLabel().toLowerCase() + "_" + property2.getVersion();
            // resource path: aa\Ttest_0_1\resources\myResource_0.1.txt
            String resourcePath2 = ResourceDependenciesUtil.getResourcePath(model,
                    JavaResourcesHelper.getJobFolderName(property2.getLabel(), property2.getVersion()), null);
            ResourceDependenciesUtil.copyToExtResourceFolder(resourceObject, property2.getId(), property2.getVersion(), null,
                    null);
            // copy to location:
            // AA\poms\jobs\process\ttest_0.1\src\main\ext-resources\aa\Ttest_0_1\resources\myResource_0.1.txt
            File file2 = project
                    .getFile(new Path("poms/jobs/process/" + jobProjectFolderName2 + "/src/main/ext-resources/" + resourcePath2))
                    .getLocation().toFile();
            Assert.assertTrue(file2.exists());
        }
    }


    @After
    public void after() {
        try {
            ProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
            factory.deleteObjectPhysical(factory.getLastVersion(item.getProperty().getId()));
            factory.deleteObjectPhysical(factory.getLastVersion(item2.getProperty().getId()));
            factory.deleteObjectPhysical(factory.getLastVersion(processItem.getProperty().getId()));
            factory.deleteObjectPhysical(factory.getLastVersion(jobItem.getProperty().getId()));
            if (fakefile != null && fakefile.exists()) {
                fakefile.delete();
            }
        } catch (PersistenceException e) {
            e.printStackTrace();
            fail("Test ResourceDependenciesUtilTest failure, cannot delete ResourceItem.");
        }
    }

    public void createJobProperty() {
        Property property = PropertiesFactory.eINSTANCE.createProperty();
        String id = ProxyRepositoryFactory.getInstance().getNextId();
        property.setId(id);
        property.setLabel("test");
        property.setVersion("0.1");

        processItem = PropertiesFactory.eINSTANCE.createProcessItem();
        processItem.setProperty(property);
        property.setItem(processItem);

        process = new Process(property);
        ProcessType processtype = TalendFileFactory.eINSTANCE.createProcessType();
        ParametersType parameterType = TalendFileFactory.eINSTANCE.createParametersType();
        processtype.setParameters(parameterType);
        processItem.setProcess(processtype);
        IContextManager contextManager = process.getContextManager();
        if (contextManager == null) {
            contextManager = new JobContextManager();
        }
        addContextParameter(contextManager);

        List<JobResourceDependencyModel> models = new ArrayList<JobResourceDependencyModel>();
        models.add(new JobResourceDependencyModel(item));
        models.add(new JobResourceDependencyModel(item));
        ResourceDependenciesUtil.saveResourceDependency(process.getAdditionalProperties(), models);

        jobItem = PropertiesFactory.eINSTANCE.createProcessItem();
        Property jobProperty = PropertiesFactory.eINSTANCE.createProperty();
        jobProperty.setId(ProxyRepositoryFactory.getInstance().getNextId());
        jobProperty.setLabel("Ttest");
        jobProperty.setVersion("0.1");
        jobItem.setProperty(jobProperty);
        jobProperty.setItem(jobItem);
        ProcessType processtype2 = TalendFileFactory.eINSTANCE.createProcessType();
        processtype2.setParameters(TalendFileFactory.eINSTANCE.createParametersType());
        jobItem.setProcess(processtype2);
    }

    private void addContextParameter(IContextManager contextManager) {
        String lastVersion = JobResourceDependencyModel.LATEST_VERSION;
        String[] contextNames = new String[] {"resContextPar","resContextPar2"};
        String[] contextValues = new String[] {
                ResourceDependenciesUtil.createResourceContextValue(new JobResourceDependencyModel(item)),
                ResourceDependenciesUtil.createResourceContextValue(new JobResourceDependencyModel(item2)) };
        for (int i = 0; i < 2; i++) {
            JobContextParameter contextParam = new JobContextParameter();
            contextParam.setName(contextNames[i]);
            ECodeLanguage curLanguage = LanguageManager.getCurrentLanguage();
            contextParam.setType(JavaTypesManager.RESOURCE.getId());
            contextParam.setPrompt(contextNames[i] + "?"); //$NON-NLS-1$
            contextParam.setValue(contextValues[i]);
            contextParam.setComment(""); //$NON-NLS-1$
            contextParam.setSource(IContextParameter.BUILT_IN); // $NON-NLS-1$
            for (int j = 0; j < contextManager.getListContext().size(); j++) {
                IContext context = contextManager.getListContext().get(j);
                IContextParameter toAdd = contextParam.clone();
                toAdd.setContext(context);
                context.getContextParameterList().add(toAdd);
            }
        }
        contextManager.fireContextsChangedEvent();
    }

}
