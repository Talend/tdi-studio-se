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
package org.talend.repository.demo.imports;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.ecore.resource.Resource;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.talend.commons.exception.LoginException;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.runtime.model.emf.provider.EmfResourcesFactoryReader;
import org.talend.commons.runtime.model.emf.provider.ResourceOption;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.ITDQItemService;
import org.talend.core.context.Context;
import org.talend.core.context.RepositoryContext;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.model.general.Project;
import org.talend.core.model.general.TalendNature;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.Property;
import org.talend.core.model.properties.RoutineItem;
import org.talend.core.model.properties.User;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.repository.constants.FileConstants;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.repository.utils.XmiResourceManager;
import org.talend.core.runtime.CoreRuntimePlugin;
import org.talend.repository.ProjectManager;
import org.talend.repository.items.importexport.manager.ResourcesManager;
import org.talend.repository.model.RepositoryConstants;
import org.talend.repository.model.ResourceModelUtils;
import org.talend.repository.ui.actions.importproject.DemoProjectBean;
import org.talend.repository.ui.actions.importproject.EDemoProjectFileType;
import org.talend.repository.ui.actions.importproject.ImportProjectsUtilities;
import org.talend.utils.io.FilesUtils;

/**
 * created by ldong on 2014-4-28 Detailled comment
 *
 */
public abstract class DemosImportTest {

	protected Project tempDemoProject;

	protected DemoProjectBean currentDemo;

	protected IPath processItemPath, connectionItemPath, routineItemPath, contextItemPath, bussniessProcessPath,
			sqlPatternPath, documentionPath, processMrPath, tdqProfilingAnaPath, tdqProfilingRepPath,
			tdqLibrariesRuleSqlPath, tdqLibrariesPatternPath, routesItemPath, routeResourcePath, servicesItemPath,
			mdmDatacontainerPath, mdmDataModelath, mdmResourcePath, mdmRolePath, mdmRoutingrulePath,
			mdmTransformerV2Path, mdmViewPath, mdmWorkflowPath;;

	protected String demoName;

	public static final String TEMP_FOLDER_SUFFIEX = ".tmp";

	private static Project originalProject;

    protected String rootPath = null;

	protected Map<String, List<File>> demoDataMap = new HashMap<String, List<File>>();

	@BeforeClass
	public static void recordOriginalProject() throws PersistenceException, CoreException, LoginException {
		Context ctx = CoreRuntimePlugin.getInstance().getContext();
		RepositoryContext repositoryContext = (RepositoryContext) ctx.getProperty(Context.REPOSITORY_CONTEXT_KEY);
		originalProject = repositoryContext.getProject();

		EmfResourcesFactoryReader.INSTANCE.addOption(ResourceOption.DEMO_IMPORTATION, false);
	}

	@AfterClass
	public static void relogonOriginalProject() throws Exception {
		if (originalProject != null) {
			IWorkspaceRunnable runnable = new IWorkspaceRunnable() {

				@Override
				public void run(IProgressMonitor monitor) throws CoreException {
					try {
						ProxyRepositoryFactory.getInstance().logOnProject(originalProject, new NullProgressMonitor());
					} catch (Exception e) {
						throw new CoreException(new org.eclipse.core.runtime.Status(IStatus.ERROR,
								"org.talend.repository.svnprovider.test", e.getMessage(), e)); //$NON-NLS-1$
					}
				}
			};

			ResourcesPlugin.getWorkspace().run(runnable, ResourcesPlugin.getWorkspace().getRoot(),
					IWorkspace.AVOID_UPDATE, new NullProgressMonitor());
		}
		originalProject = null;

		EmfResourcesFactoryReader.INSTANCE.removOption(ResourceOption.DEMO_IMPORTATION, false);
	}

	@After
	public void afterTest() throws Exception {
		removeTempDemoProject();
		ProxyRepositoryFactory.getInstance().logOffProject();
	}

	protected void initDemo(String pluginID) {
		List<DemoProjectBean> allDemos = ImportProjectsUtilities.getAllDemoProjects();
		for (DemoProjectBean demo : allDemos) {
			if (pluginID.equals(demo.getPluginId())) {
				currentDemo = demo;
				break;
			}
		}
	}

	protected void initTestPaths() {
		if (currentDemo == null) { // maybe license problem, the demo plugin is not loaded.
			return;
		}
		String demoProjectPath = currentDemo.getDemoProjectFilePath();
		IPath path = new Path(demoProjectPath);
		path.lastSegment();
		if (currentDemo.getDemoProjectFileType().equals(EDemoProjectFileType.ARCHIVE)) {
			demoName = path.lastSegment().substring(0, path.lastSegment().lastIndexOf("."));
		} else {
			demoName = path.lastSegment();
		}
		processItemPath = new Path("/process/"); //$NON-NLS-1$
		connectionItemPath = new Path("/metadata/"); //$NON-NLS-1$
		routineItemPath = new Path("/code/");
		contextItemPath = new Path("/context/");
		bussniessProcessPath = new Path("/businessProcess/");
		sqlPatternPath = new Path("/sqlPatterns/");
		documentionPath = new Path("/documentations/");
		processMrPath = new Path("/process_mr/");
		tdqProfilingAnaPath = new Path("/TDQ_Data Profiling/Analyses/");
		tdqProfilingRepPath = new Path("/TDQ_Data Profiling/Reports/");
		tdqLibrariesRuleSqlPath = new Path("/TDQ_Libraries/Rules/SQL");
		tdqLibrariesPatternPath = new Path("/TDQ_Libraries/Patterns");
		routesItemPath = new Path("/routes/");
		routeResourcePath = new Path("/route_resources/");
		servicesItemPath = new Path("/services/");

		mdmDatacontainerPath = new Path("/MDM/datacontainer/");
		mdmDataModelath = new Path("/MDM/datamodel/");
		mdmResourcePath = new Path("/MDM/resource/");
		mdmRolePath = new Path("/MDM/role/");
		mdmRoutingrulePath = new Path("/MDM/routingrule/");
		mdmTransformerV2Path = new Path("/MDM/transformerV2/");
		mdmViewPath = new Path("/MDM/view/");
		mdmWorkflowPath = new Path("/MDM/workflow/");
	}

	protected void initTempPro() throws PersistenceException, CoreException, LoginException {
		if (currentDemo == null) { // maybe license problem, the demo plugin is not loaded.
			return;
		}
		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {

			@Override
			public void run(IProgressMonitor monitor) throws CoreException {
				try {
					createTempDemoProject(demoName);
					Context ctx = CoreRuntimePlugin.getInstance().getContext();
					RepositoryContext repositoryContext = (RepositoryContext) ctx
							.getProperty(Context.REPOSITORY_CONTEXT_KEY);
					originalProject = repositoryContext.getProject();
					ProxyRepositoryFactory.getInstance().logOnProject(tempDemoProject, new NullProgressMonitor());
					// initilize the DQ sturcture for the original default project
					initDQStructure();
					repositoryContext.setProject(tempDemoProject);
				} catch (Exception e) {
					throw new CoreException(new org.eclipse.core.runtime.Status(IStatus.ERROR,
							"org.talend.repository.svnprovider.test", e.getMessage(), e)); //$NON-NLS-1$
				}
			}
		};

		ResourcesPlugin.getWorkspace().run(runnable, ResourcesPlugin.getWorkspace().getRoot(), IWorkspace.AVOID_UPDATE,
				new NullProgressMonitor());
	}

	protected void initDQStructure() {
		if (GlobalServiceRegister.getDefault().isServiceRegistered(ITDQItemService.class)) {
			ITDQItemService tdqService = (ITDQItemService) GlobalServiceRegister.getDefault()
					.getService(ITDQItemService.class);
			if (tdqService != null) {
				tdqService.createDQStructor();
			}
		}
	}

	protected void createTempDemoProject(String demoName) throws CoreException, PersistenceException {
		Project projectInfor = new Project();
		projectInfor.setLabel(demoName);
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

		tempDemoProject = new Project();
		// Fill project object
		tempDemoProject.setLabel(projectInfor.getLabel());
		tempDemoProject.setDescription(projectInfor.getDescription());
		tempDemoProject.setLanguage(projectInfor.getLanguage());
		tempDemoProject.setAuthor(projectInfor.getAuthor());
		tempDemoProject.setLocal(true);
		tempDemoProject.setTechnicalLabel(technicalLabel);
		XmiResourceManager xmiResourceManager = new XmiResourceManager();
		Resource projectResource = xmiResourceManager.createProjectResource(prj);
		projectResource.getContents().add(tempDemoProject.getEmfProject());
		projectResource.getContents().add(tempDemoProject.getAuthor());
		xmiResourceManager.saveResource(projectResource);
	}

	protected void removeTempDemoProject() throws PersistenceException, CoreException {
		if (tempDemoProject != null) {
			ProjectManager.getInstance().getFolders(tempDemoProject.getEmfProject()).clear();
			final IProject project = ResourceModelUtils.getProject(tempDemoProject);
			project.delete(true, null);
		}
        if (StringUtils.isNotBlank(rootPath)) {
            final File workingFolder = new File(rootPath);
            File tmpdir = new File(System.getProperty("java.io.tmpdir")); //$NON-NLS-1$
            if (workingFolder.toString().startsWith(tmpdir.toString())) { // remove from temp
                FilesUtils.deleteFolder(workingFolder, true);
            }
        }
	}

	protected int getUserRoutineSize() throws PersistenceException {
		List<IRepositoryViewObject> all = ProxyRepositoryFactory.getInstance().getAll(ERepositoryObjectType.ROUTINES);
		int currentRoutineItemsSize = 0;
		for (IRepositoryViewObject obj : all) {
			Property property = obj.getProperty();
			Item item = property.getItem();
			if (item instanceof RoutineItem && !((RoutineItem) item).isBuiltIn()) { // only check for user routine.
				currentRoutineItemsSize++;
			}
		}
		return currentRoutineItemsSize;
	}

	protected List<File> getDemoRoutineItemsFile(File baseFolder) {
		List<File> demoRoutineResult = new ArrayList<File>();
		List<File> demoRoutineItemsFiles = DemoImportTestUtil.collectProjectFilesFromDirectory(baseFolder,
				FileConstants.ITEM_EXTENSION, true);
		// remove the code, only routines left, and add system, so should return
		// routines/system
		IPath systemRoutinesPath = new Path(ERepositoryObjectType.ROUTINES.getFolder()).removeFirstSegments(1)
				.append(RepositoryConstants.SYSTEM_DIRECTORY);
		for (File f : demoRoutineItemsFiles) {
			IPath itemPath = new Path(f.getAbsolutePath()).makeRelativeTo(new Path(baseFolder.getAbsolutePath()));
			if (!systemRoutinesPath.isPrefixOf(itemPath)) { // ignore the system one
				demoRoutineResult.add(f);
			}

		}
		return demoRoutineResult;
	}

	protected void doRoutinesItemsTest(String rootPath) throws PersistenceException {
		int currentRoutineItemsSize = getUserRoutineSize();
		int demoRoutineItemsSize = 0;
		List<File> demoRoutineFileList = getDemoItemFileList(rootPath + File.separator + routineItemPath);
		if (demoRoutineFileList != null) {
			demoRoutineItemsSize = demoRoutineFileList.size();
		}
		Assert.assertTrue(demoRoutineItemsSize > 0);
		Assert.assertTrue(currentRoutineItemsSize > 0);
		Assert.assertEquals(demoRoutineItemsSize, currentRoutineItemsSize);
	}

	protected void collectDemoData(String rootPath) throws PersistenceException {
		Map<String, String> map = getCollectFolderMap(rootPath);
		for (String path : map.keySet()) {
			Assert.assertTrue(new File(path).exists());
			File tempFolder = new File(path);
			List<File> itemFiles = DemoImportTestUtil.collectProjectFilesFromDirectory(tempFolder, map.get(path), true);
			demoDataMap.put(path, itemFiles);
		}
		File tempRoutineItemsFolder = new File(rootPath + File.separator + routineItemPath);
		List<File> routineFileList = getDemoRoutineItemsFile(tempRoutineItemsFolder);
		demoDataMap.put(rootPath + File.separator + routineItemPath, routineFileList);
	}

	protected List<File> getDemoItemFileList(String folderPath) {
		return demoDataMap.get(folderPath);
	}

	protected Map<String, String> getCollectFolderMap(String rootPath) {
		Map<String, String> map = new HashMap<String, String>();
		return map;
	}

	protected String getRootPath(ResourcesManager resManager) {
		Iterator path = resManager.getPaths().iterator();
		String firstFilePath = ((Path) path.next()).toPortableString();
		String tempFolderPath = firstFilePath.substring(0,
				firstFilePath.indexOf(TEMP_FOLDER_SUFFIEX) + TEMP_FOLDER_SUFFIEX.length());
		Assert.assertTrue(new File(tempFolderPath).exists());
		return tempFolderPath + File.separator + demoName;
	}

}
