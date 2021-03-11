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
package org.talend.designer.codegen;

import java.util.HashMap;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.talend.commons.CommonsPlugin;
import org.talend.commons.exception.BusinessException;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.utils.Timer;
import org.talend.commons.utils.workbench.resources.ResourceUtils;
import org.talend.core.CorePlugin;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.IService;
import org.talend.core.context.Context;
import org.talend.core.context.RepositoryContext;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.model.components.ComponentCompilations;
import org.talend.core.model.general.Project;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.User;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.repository.model.RepositoryFactoryProvider;
import org.talend.core.repository.utils.ProjectHelper;
import org.talend.designer.codegen.i18n.Messages;
import org.talend.repository.model.RepositoryConstants;

/***/
public class CodeGenInit implements IApplication {

    ProxyRepositoryFactory repositoryFactory = ProxyRepositoryFactory.getInstance();

    @Override
    public Object start(IApplicationContext context) throws Exception {
        System.setProperty("force_full_codegen", "true");
        Timer.getTimer("CodeGenInit").start(); //$NON-NLS-1$
        CommonsPlugin.setHeadless(true);
        initLocalRepository();
        init(ECodeLanguage.JAVA);

        removeLinkedResources();
        addMarkersForTemplatesNextInitialization();
        saveWorkspace();
        Timer.getTimer("CodeGenInit").stop(); //$NON-NLS-1$
        Timer.getTimer("CodeGenInit").print(); //$NON-NLS-1$
        return EXIT_OK;
    }

    private void addMarkersForTemplatesNextInitialization() {
        info(Messages.getString("CodeGenInit.addMarkers")); //$NON-NLS-1$
        ComponentCompilations.addMarkers();
    }

    private void saveWorkspace() throws CoreException {
        info(Messages.getString("CodeGenInit.saveWorkspace")); //$NON-NLS-1$
        ResourcesPlugin.getWorkspace().save(true, new NullProgressMonitor());
    }

    private void removeLinkedResources() throws CoreException {
        info(Messages.getString("CodeGenInit.removeLink")); //$NON-NLS-1$

        IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(".JETEmitters"); //$NON-NLS-1$
        project.accept(new IResourceVisitor() {

            @Override
            public boolean visit(IResource resource) throws CoreException {
                if (resource.isLinked()) {
                    resource.delete(true, new NullProgressMonitor());
                }
                return true;
            }
        }, IResource.DEPTH_ONE, false);
    }

    private void init(ECodeLanguage language) throws Exception, InterruptedException {
        info(Messages.getString("CodeGenInit.createProject", language.getName())); //$NON-NLS-1$
        createAndLogonProject(language);
        info(Messages.getString("CodeGenInit.initTemplate")); //$NON-NLS-1$
        new CodeGeneratorManager().initTemplate();
        info(Messages.getString("CodeGenInit.deleteProject", language)); //$NON-NLS-1$
        deleteProject(language);
    }

    @Override
    public void stop() {
    }

    public static void info(String message) {
        IStatus status = new Status(IStatus.INFO, CodeGeneratorActivator.PLUGIN_ID, message);
        CodeGeneratorActivator.getDefault().getLog().log(status);
    }

    public void init() throws Exception {
        CodeGeneratorManager codeGeneratorManager = new CodeGeneratorManager();
        IStatus status = codeGeneratorManager.initTemplate();
        if (status.getCode() != IStatus.OK) {
            throw new Exception(status.getException());
        }
    }

    private void createAndLogonProject(ECodeLanguage language) throws Exception {
        Project project = getProject(language);
        if (project == null) {
            Project projectInfor = ProjectHelper.createProject(getProjectName(language), "", //$NON-NLS-1$
                    language.getName(), createUser());
            project = repositoryFactory.createProject(projectInfor);
        }
        repositoryFactory.logOnProject(project, new NullProgressMonitor());
    }

    private void deleteProject(ECodeLanguage language) throws Exception {
        IProject project = ResourceUtils.getProject(getProjectName(language).toUpperCase());
        if (project != null) {
            project.delete(true, new NullProgressMonitor());
        }
    }

    private String getProjectName(ECodeLanguage language) {
        return "codegen_" + language.getName() + "_temp_project"; //$NON-NLS-1$ //$NON-NLS-2$
    }

    private Project getProject(ECodeLanguage language) throws PersistenceException, BusinessException {
        for (Project project : repositoryFactory.readProject()) {
            if (project.getLanguage().equals(language)) {
                return project;
            }
        }

        return null;
    }

    private void initLocalRepository() throws PersistenceException {
        RepositoryContext repositoryContext = new RepositoryContext();
        repositoryContext.setUser(createUser());
        HashMap<String, String> fields = new HashMap<String, String>();
        repositoryContext.setFields(fields);

        Context ctx = CorePlugin.getContext();
        ctx.putProperty(Context.REPOSITORY_CONTEXT_KEY, repositoryContext);

        repositoryFactory.setRepositoryFactoryFromProvider(RepositoryFactoryProvider
                .getRepositoriyById(RepositoryConstants.REPOSITORY_LOCAL_ID));
        repositoryFactory.initialize();
    }

    private User createUser() {
        User user = PropertiesFactory.eINSTANCE.createUser();
        user.setLogin("user@talend.com"); //$NON-NLS-1$
        return user;
    }

    /***/
    static class CodeGeneratorManager {

        private IStatus status;

        public IStatus initTemplate() throws InterruptedException {
            final Job initializeTemplatesJob = getCodeGenerationService().initializeTemplates();
            Job.getJobManager().addJobChangeListener(new JobChangeAdapter() {

                @Override
                public void done(IJobChangeEvent event) {
                    if (event.getJob().equals(initializeTemplatesJob)) {
                        setStatus(event.getResult());
                    }
                }
            });

            while (status == null) {
                Thread.sleep(1000);
            }

            return status;
        }

        private void setStatus(IStatus result) {
            this.status = result;
        }

        private ICodeGeneratorService getCodeGenerationService() {
            IService service = GlobalServiceRegister.getDefault().getService(ICodeGeneratorService.class);
            return (ICodeGeneratorService) service;
        }
    }

}
