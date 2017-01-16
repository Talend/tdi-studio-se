package org.talend.repository.model.migration;

import static org.junit.Assert.*;

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.ecore.resource.Resource;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.talend.commons.exception.BusinessException;
import org.talend.commons.exception.LoginException;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.utils.workbench.resources.ResourceUtils;
import org.talend.core.context.Context;
import org.talend.core.context.RepositoryContext;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.model.general.Project;
import org.talend.core.model.general.TalendNature;
import org.talend.core.model.properties.ContextItem;
import org.talend.core.model.properties.ItemState;
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
import org.talend.repository.ProjectManager;

public class AddContextCommentValueMigrationTaskTest {
    
    private static Project originalProject;

    private static Project sampleProject;

    private ContextItem testItem;

    @BeforeClass
    public static void beforeAllTests() throws PersistenceException, LoginException, CoreException {
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
    public void testBefore() throws PersistenceException {
        testItem = createTempContextItem();
    }

    @After
    public void testAfter() throws PersistenceException, BusinessException {
        RepositoryObject objToDelete = new RepositoryObject(testItem.getProperty());
        ProxyRepositoryFactory.getInstance().deleteObjectPhysical(objToDelete);
        testItem = null;
    }
    
    @Test
    public void testAddContextCommentValue() {
        testItem.setDefaultContext("Default");
        String[] paramNames = new String[]{"p1","p2","p3"};
        String[] comments = new String[]{"c1","c2","c3"};
        // context item before 5.6.1
        // comments always in the first group of context no matter it's default group or not.
        testItem.getContext().add(createContextType("DEV", paramNames, comments));
        testItem.getContext().add(createContextType("PROD", paramNames, null));
        testItem.getContext().add(createContextType("Default", paramNames, null));
        AddContextCommentValueMigrationTask task = new AddContextCommentValueMigrationTask();
        task.execute(testItem);
        List<ContextType> contexts = testItem.getContext();
        for(ContextType context : contexts) {
            List<ContextParameterType> params = context.getContextParameter();
            for (ContextParameterType param : params) {
                if (param.getName().equals("p1")) {
                    assertEquals("c1", param.getComment());
                } else if (param.getName().equals("p2")) {
                    assertEquals("c2", param.getComment());
                } else if (param.getName().equals("p3")) {
                    assertEquals("c3", param.getComment());
                }
            }
        }
    }
    
    private ContextType createContextType(String contextName, String[] paramNames, String[] comments) {
        ContextType context = TalendFileFactory.eINSTANCE.createContextType();
        context.setName(contextName);
        for (int i = 0; i<paramNames.length; i++) {
            ContextParameterType param = TalendFileFactory.eINSTANCE.createContextParameterType();
            param.setName(paramNames[i]);
            param.setType("id_String");
            if (comments != null) {
                param.setComment(comments[i]);
            } else {
                param.setComment("");
            }
            context.getContextParameter().add(param);
        }
        return context;
    }

    private ContextItem createTempContextItem() throws PersistenceException {
        ContextItem contextItem = PropertiesFactory.eINSTANCE.createContextItem();
        Property myProperty = PropertiesFactory.eINSTANCE.createProperty();
        myProperty.setId(ProxyRepositoryFactory.getInstance().getNextId());
        ItemState itemState = PropertiesFactory.eINSTANCE.createItemState();
        itemState.setDeleted(false);
        itemState.setPath("");
        contextItem.setState(itemState);
        contextItem.setProperty(myProperty);
        myProperty.setLabel("context1");
        myProperty.setVersion("0.1");
        ProxyRepositoryFactory.getInstance().create(contextItem, new Path(""));
        return contextItem;
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

}
