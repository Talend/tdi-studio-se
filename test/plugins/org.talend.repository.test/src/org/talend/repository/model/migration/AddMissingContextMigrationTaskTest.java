package org.talend.repository.model.migration;

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.resource.Resource;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.talend.commons.exception.LoginException;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.utils.workbench.resources.ResourceUtils;
import org.talend.core.context.Context;
import org.talend.core.context.RepositoryContext;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.model.general.Project;
import org.talend.core.model.general.TalendNature;
import org.talend.core.model.properties.ItemState;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.Property;
import org.talend.core.model.properties.User;
import org.talend.core.model.repository.RepositoryObject;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.repository.utils.XmiResourceManager;
import org.talend.core.runtime.CoreRuntimePlugin;
import org.talend.designer.core.model.utils.emf.talendfile.ContextParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.ContextType;
import org.talend.designer.core.model.utils.emf.talendfile.TalendFileFactory;
import org.talend.designer.core.model.utils.emf.talendfile.impl.ContextParameterTypeImpl;
import org.talend.repository.ProjectManager;


public class AddMissingContextMigrationTaskTest {

    private static Project originalProject;

    private static Project sampleProject;

    private ProcessItem testItem;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        createTempProject();
        Context ctx = CoreRuntimePlugin.getInstance().getContext();
        RepositoryContext repositoryContext = (RepositoryContext) ctx.getProperty(Context.REPOSITORY_CONTEXT_KEY);
        originalProject = repositoryContext.getProject();
        repositoryContext.setProject(sampleProject);
    }

    @AfterClass
    public static void afterAllTests() throws PersistenceException, CoreException {
        removeTempProject();
        Context ctx = CoreRuntimePlugin.getInstance().getContext();
        RepositoryContext repositoryContext = (RepositoryContext) ctx.getProperty(Context.REPOSITORY_CONTEXT_KEY);
        repositoryContext.setProject(originalProject);
        originalProject = null;
        sampleProject = null;
    }

    @Before
    public void setUp() throws Exception {
        testItem = createTempProcessItem();
    }

    @After
    public void tearDown() throws Exception {
        RepositoryObject objToDelete = new RepositoryObject(testItem.getProperty());
        ProxyRepositoryFactory.getInstance().deleteObjectPhysical(objToDelete);
        testItem = null;
    }

    @Test
    public void testAddMissingContext() {
        // dev
        String[] paramDev = new String[] { "param1", "param2", "param3" };
        ContextType devGroup = createContextType("dev", paramDev);
        testItem.getProcess().setDefaultContext("dev");
        // test
        String[] paramTest = new String[] { "param1", "param3" }; // test group missing param2
        ContextType testGroup = createContextType("test", paramTest);
        // prop
        String[] paramProp = new String[] { "param1", "param2" }; // prop group missing param3
        ContextType propGroup = createContextType("prop", paramProp);

        testItem.getProcess().getContext().add(devGroup);
        testItem.getProcess().getContext().add(testGroup);
        testItem.getProcess().getContext().add(propGroup);
        AddMissingContextMigrationTask migration = new AddMissingContextMigrationTask();
        migration.execute(testItem);
        EList<ContextType> contexts = testItem.getProcess().getContext();
        boolean hasParam2 = false;
        List<ContextParameterTypeImpl> contextParams = contexts.get(1).getContextParameter();
        for(ContextParameterTypeImpl cp:contextParams) {
            if("param2".equals(cp.getName())){
                hasParam2 = true;
            }
        }
        Assert.assertTrue(hasParam2);
        boolean hasParam3 = false;
        List<ContextParameterTypeImpl> contextParams2 = contexts.get(2).getContextParameter();
        for (ContextParameterTypeImpl cp : contextParams2) {
            if ("param3".equals(cp.getName())) {
                hasParam3 = true;
            }
        }
        Assert.assertTrue(hasParam3);
    }

    private static void createTempProject() throws CoreException, PersistenceException, LoginException {
        Project projectInfor = new Project();
        projectInfor.setLabel("testauto");
        projectInfor.setDescription("no desc");
        projectInfor.setLanguage(ECodeLanguage.JAVA);
        User user = PropertiesFactory.eINSTANCE.createUser();
        user.setLogin("testauto@talend.com");
        projectInfor.setAuthor(user);
        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();

        String technicalLabel = Project.createTechnicalName(projectInfor.getLabel());
        IProject prj = root.getProject(technicalLabel);

        final IWorkspace workspace = ResourcesPlugin.getWorkspace();

        try {
            IProjectDescription desc = null;
            if (prj.exists()) {
                prj.delete(true, null); // always delete to avoid conflicts between 2 tests
            }
            desc = workspace.newProjectDescription(technicalLabel);
            desc.setNatureIds(new String[] { TalendNature.ID });
            desc.setComment(projectInfor.getDescription());

            prj.create(desc, null);
            prj.open(IResource.DEPTH_INFINITE, null);
            prj.setDefaultCharset("UTF-8", null);
        } catch (CoreException e) {
            throw new PersistenceException(e);
        }

        sampleProject = new Project();
        // Fill project object
        sampleProject.setLabel(projectInfor.getLabel());
        sampleProject.setDescription(projectInfor.getDescription());
        sampleProject.setLanguage(projectInfor.getLanguage());
        sampleProject.setAuthor(projectInfor.getAuthor());
        sampleProject.setLocal(true);
        sampleProject.setTechnicalLabel(technicalLabel);
        XmiResourceManager xmiResourceManager = new XmiResourceManager();
        Resource projectResource = xmiResourceManager.createProjectResource(prj);
        projectResource.getContents().add(sampleProject.getEmfProject());
        projectResource.getContents().add(sampleProject.getAuthor());
        xmiResourceManager.saveResource(projectResource);
    }

    protected static void removeTempProject() throws PersistenceException, CoreException {
        // clear the folder, same as it should be in a real logoffProject.
        ProjectManager.getInstance().getFolders(sampleProject.getEmfProject()).clear();
        final IProject project = ResourceUtils.getProject(sampleProject);
        project.delete(true, null);
    }

    private ProcessItem createTempProcessItem() throws PersistenceException {
        ProcessItem processItem = PropertiesFactory.eINSTANCE.createProcessItem();
        Property myProperty = PropertiesFactory.eINSTANCE.createProperty();
        myProperty.setId(ProxyRepositoryFactory.getInstance().getNextId());
        ItemState itemState = PropertiesFactory.eINSTANCE.createItemState();
        itemState.setDeleted(false);
        itemState.setPath("");
        processItem.setState(itemState);
        processItem.setProperty(myProperty);
        myProperty.setLabel("myJob");
        myProperty.setVersion("0.1");
        processItem.setProcess(TalendFileFactory.eINSTANCE.createProcessType());
        ProxyRepositoryFactory.getInstance().create(processItem, new Path(""));
        return processItem;
    }

    private ContextType createContextType(String contextName, String[] paramNames) {
        ContextType context = TalendFileFactory.eINSTANCE.createContextType();
        context.setName(contextName);
        for (String paramName : paramNames) {
            ContextParameterType param = TalendFileFactory.eINSTANCE.createContextParameterType();
            param.setName(paramName);
            param.setType("id_String");
            context.getContextParameter().add(param);
        }
        return context;
    }
}
