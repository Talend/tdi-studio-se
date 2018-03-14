/**
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.talend.sdk.component.studio.metadata.migration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IResourceRuleFactory;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.LoginException;
import org.talend.commons.exception.PersistenceException;
import org.talend.core.model.general.Project;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.update.RepositoryUpdateManager;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.repository.ProjectManager;
import org.talend.repository.RepositoryWorkUnit;
import org.talend.sdk.component.server.front.model.ConfigTypeNode;
import org.talend.sdk.component.server.front.model.ConfigTypeNodes;
import org.talend.sdk.component.studio.Lookups;
import org.talend.sdk.component.studio.exception.UserCancelledException;
import org.talend.sdk.component.studio.i18n.Messages;
import org.talend.sdk.component.studio.metadata.model.TaCoKitConfigurationItemModel;
import org.talend.sdk.component.studio.metadata.model.TaCoKitConfigurationModel;
import org.talend.sdk.component.studio.util.TaCoKitUtil;
import org.talend.sdk.component.studio.websocket.WebSocketClient.V1ConfigurationType;

/**
 * DOC cmeng  class global comment. Detailled comment
 */
public class TaCoKitMigrationManager {

    private V1ConfigurationType configurationType;

    public TaCoKitMigrationManager() {
        configurationType = Lookups.client().v1().configurationType();
    }

    public void checkMigration(final IProgressMonitor progressMonitor) throws Exception {
        IProgressMonitor monitor = progressMonitor;
        if (monitor == null) {
            monitor = new NullProgressMonitor();
        }
        monitor.subTask(Messages.getString("migration.check.progress.fetchConfigurations")); //$NON-NLS-1$
        ConfigTypeNodes configTypeNodes = Lookups.taCoKitCache().getConfigTypeNodes();
        if (configTypeNodes != null) {
            Map<String, ConfigTypeNode> nodes = configTypeNodes.getNodes();
            if (nodes != null && !nodes.isEmpty()) {
                for (ConfigTypeNode node : nodes.values()) {
                    try {
                        checkMigration(node, progressMonitor);
                    } catch (UserCancelledException e) {
                        throw e;
                    } catch (Exception e) {
                        ExceptionHandler.process(e);
                    }
                }
            }
        }
    }

    public void checkMigration(final ConfigTypeNode configTypeNode, final IProgressMonitor progressMonitor) throws Exception {
        // is logging on
        IProgressMonitor monitor = progressMonitor;
        if (monitor == null) {
            monitor = new NullProgressMonitor();
        }
        checkMonitor(monitor);
        monitor.subTask(Messages.getString("migration.check.progress.currentConfiguration", configTypeNode.getDisplayName())); //$NON-NLS-1$
        ERepositoryObjectType repoObjType = TaCoKitUtil.getOrCreateERepositoryObjectType(configTypeNode);
        ProxyRepositoryFactory repoFactory = ProxyRepositoryFactory.getInstance();
        ProjectManager projManager = ProjectManager.getInstance();
        List<Project> allReferencedProjects = projManager.getAllReferencedProjects();
        Project curProject = ProjectManager.getInstance().getCurrentProject();
        List<Project> projects = new ArrayList<>();
        if (allReferencedProjects != null && !allReferencedProjects.isEmpty()) {
            projects.addAll(allReferencedProjects);
        }
        projects.add(curProject);
        for (Project project : projects) {
            checkMonitor(monitor);
            monitor.subTask(Messages.getString("migration.check.progress.listItems", configTypeNode.getDisplayName(), //$NON-NLS-1$
                    project.getLabel()));
            List<IRepositoryViewObject> allRepoViewObjs = repoFactory.getAll(project, repoObjType, true, true);
            checkMonitor(monitor);
            if (allRepoViewObjs != null && !allRepoViewObjs.isEmpty()) {
                for (IRepositoryViewObject repoViewObj : allRepoViewObjs) {
                    try {
                        ConnectionItem item = (ConnectionItem) repoViewObj.getProperty().getItem();
                        if (checkMigration(item, progressMonitor)) {
                            repoFactory.save(item);
                            RepositoryUpdateManager.updateDBConnection(item, false);
                        }
                    } catch (UserCancelledException e) {
                        throw e;
                    } catch (Exception e) {
                        ExceptionHandler.process(e);
                    }
                }
            }
        }

    }

    public boolean checkMigration(final ConnectionItem item, final IProgressMonitor progressMonitor) throws Exception {
        IProgressMonitor monitor = progressMonitor;
        if (monitor == null) {
            monitor = new NullProgressMonitor();
        }
        checkMonitor(monitor);
        String itemLabel = ""; //$NON-NLS-1$
        try {
            itemLabel = item.getProperty().getLabel();
        } catch (Exception e) {
            // ignore
        }
        monitor.subTask(Messages.getString("migration.check.progress.start", itemLabel)); //$NON-NLS-1$
        TaCoKitConfigurationItemModel itemModel = new TaCoKitConfigurationItemModel(item, false);
        TaCoKitConfigurationModel configModel = itemModel.getConfigurationModel();
        if (isNeedMigration(configModel)) {
            migrate(configModel, progressMonitor);
            return true;
        }
        return false;
    }

    private void checkMonitor(final IProgressMonitor monitor) throws Exception {
        if (monitor != null && monitor.isCanceled()) {
            throw new UserCancelledException(Messages.getString("migration.check.cancel")); //$NON-NLS-1$
        }
    }

    public void migrate(final TaCoKitConfigurationModel configModel, final IProgressMonitor progressMonitor) throws Exception {
        IProgressMonitor monitor = progressMonitor;
        if (monitor == null) {
            monitor = new NullProgressMonitor();
        }

        checkMonitor(monitor);
        String label = ""; //$NON-NLS-1$
        String storedVersion = ""; //$NON-NLS-1$
        String newVersion = ""; //$NON-NLS-1$
        try {
            label = configModel.getConnection().getLabel();
        } catch (Exception e) {
            // ignore
        }
        try {
            storedVersion = configModel.getStoredVersion();
        } catch (Exception e) {
            // ignore
        }
        try {
            newVersion = String.valueOf(configModel.getConfigTypeNodeVersion());
        } catch (Exception e) {
            // ignore
        }
        monitor.subTask(Messages.getString("migration.check.progress.execute", label, storedVersion, newVersion)); //$NON-NLS-1$

        Map migrationResult = configurationType.migrate(configModel.getConfigurationId(),
                Integer.valueOf(configModel.getStoredVersion()), configModel.getPropertiesWithoutBuiltIn());
        configModel.storeVersion(String.valueOf(configModel.getConfigTypeNodeVersion()));
        configModel.clearProperties(false);
        configModel.getProperties().putAll(migrationResult);

        configModel.storeVersion(String.valueOf(configModel.getConfigTypeNodeVersion()));
    }

    public boolean isNeedMigration(final TaCoKitConfigurationModel configModel) throws Exception {
        int configVersion = configModel.getConfigTypeNodeVersion();
        return !TaCoKitUtil.equals(configModel.getStoredVersion(), String.valueOf(configVersion));
    }

    public void runMigrationJob() throws Exception {
        final String title = Messages.getString("migration.check.title"); //$NON-NLS-1$
        final Job migrationJob = new Job(title) {

            @Override
            protected IStatus run(final IProgressMonitor jobMonitor) {
                IWorkspace workspace = ResourcesPlugin.getWorkspace();
                IResourceRuleFactory ruleFactory = workspace.getRuleFactory();
                ProjectManager projectManager = ProjectManager.getInstance();
                ISchedulingRule refreshRule = ruleFactory
                        .refreshRule(projectManager.getResourceProject(projectManager.getCurrentProject().getEmfProject()));
                try {
                    workspace.run(new IWorkspaceRunnable() {

                        @Override
                        public void run(final IProgressMonitor workspaceMonitor) throws CoreException {
                            ProxyRepositoryFactory.getInstance()
                                    .executeRepositoryWorkUnit(new RepositoryWorkUnit(title) {

                                @Override
                                protected void run() throws LoginException, PersistenceException {
                                    try {
                                        checkMigration(workspaceMonitor);
                                    } catch (Exception e) {
                                        ExceptionHandler.process(e);
                                    }
                                }
                            });

                        }
                    }, refreshRule, IWorkspace.AVOID_UPDATE, jobMonitor);
                } catch (CoreException e) {
                    ExceptionHandler.process(e);
                }
                return Status.OK_STATUS;
            }
        };
        migrationJob.setUser(false);
        migrationJob.schedule();
        migrationJob.join();
    }

}
