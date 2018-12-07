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
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IResourceRuleFactory;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.core.model.general.Project;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.properties.Property;
import org.talend.core.model.relationship.RelationshipItemBuilder;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.update.RepositoryUpdateManager;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.repository.model.VersionList;
import org.talend.core.repository.utils.ComponentsJsonModel;
import org.talend.core.repository.utils.ProjectDataJsonProvider;
import org.talend.designer.core.model.utils.emf.talendfile.impl.NodeTypeImpl;
import org.talend.designer.core.model.utils.emf.talendfile.impl.ProcessTypeImpl;
import org.talend.repository.ProjectManager;
import org.talend.repository.RepositoryWorkUnit;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.sdk.component.server.front.model.ConfigTypeNode;
import org.talend.sdk.component.server.front.model.ConfigTypeNodes;
import org.talend.sdk.component.studio.Lookups;
import org.talend.sdk.component.studio.exception.UserCancelledException;
import org.talend.sdk.component.studio.i18n.Messages;
import org.talend.sdk.component.studio.metadata.model.TaCoKitConfigurationModel;
import org.talend.sdk.component.studio.util.TaCoKitUtil;
import org.talend.sdk.component.studio.websocket.WebSocketClient.V1Component;
import org.talend.sdk.component.studio.websocket.WebSocketClient.V1ConfigurationType;
import org.talend.sdk.studio.process.TaCoKitNode;

/**
 * DOC cmeng  class global comment. Detailled comment
 */
public class TaCoKitMigrationManager {

    private V1ConfigurationType configurationClient;

    private final V1Component componentClient = Lookups.client().v1().component();

    public TaCoKitMigrationManager() {
        configurationClient = Lookups.client().v1().configurationType();
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
                Collection<ConfigTypeNode> topLevelNodes = TaCoKitUtil.filterTopLevelNodes(nodes.values());
                for (ConfigTypeNode node : topLevelNodes) {
                    try {
                        checkMigration(node, monitor);
                    } catch (UserCancelledException e) {
                        throw e;
                    } catch (Exception e) {
                        ExceptionHandler.process(e);
                    }
                }
                // as for it will do migration for all project, need to cache config component to component.index file
                // under .setting folder from all project
                List<ComponentsJsonModel> adaptComponentIndexJson = adaptComponentIndexJson(nodes.values());
                for (final Project project : getAllProjects()) {
                    ProjectDataJsonProvider.saveConfigComponent(project.getTechnicalLabel(), adaptComponentIndexJson);
                }
            }
        }
        checkJobsMigration(monitor);
    }

    public static List<ComponentsJsonModel> adaptComponentIndexJson(Collection<ConfigTypeNode> ConfigTypeNodes) {
        List<ComponentsJsonModel> modelList = new LinkedList<ComponentsJsonModel>();
        for (ConfigTypeNode configTypeNode : ConfigTypeNodes) {
            ComponentsJsonModel model = new ComponentsJsonModel();
            model.setId(configTypeNode.getId());
            model.setVersion(String.valueOf(configTypeNode.getVersion()));
            model.setName(configTypeNode.getName());
            model.setDisplayName(configTypeNode.getDisplayName());
            model.setParentId(configTypeNode.getParentId());
            model.setEdges(configTypeNode.getEdges());
            model.setConfigurationType(configTypeNode.getConfigurationType());
            model.setActions(configTypeNode.getActions());
            model.setProperties(configTypeNode.getProperties());
            modelList.add(model);
        }
        return modelList;

    }

    private void checkJobsMigration(final IProgressMonitor monitor) throws UserCancelledException {
        monitor.subTask(Messages.getString("migration.check.process.checking")); //$NON-NLS-1$
        final ProxyRepositoryFactory repositoryFactory = ProxyRepositoryFactory.getInstance();
        for (final Project project : getAllProjects()) {
            checkMonitor(monitor);
            monitor.subTask(Messages.getString("migration.check.process.project", project.getLabel()));
            try {
                List<IRepositoryViewObject> processeViewObjects = repositoryFactory.getAll(project, ERepositoryObjectType.PROCESS, true, true);
                for (final IRepositoryViewObject processViewObject : processeViewObjects) {
                    final Item item = processViewObject.getProperty().getItem();
                    if (item instanceof ProcessItem) {
                        checkProcessItemMigration((ProcessItem) item, monitor);
                    }
                }
            } catch (PersistenceException e) {
                ExceptionHandler.process(e);
            }
        }
    }

    public void checkProcessItemMigration(final ProcessItem processItem, final IProgressMonitor progressMonitor) throws UserCancelledException {
        IProgressMonitor monitor = progressMonitor;
        if (monitor == null) {
            monitor = new NullProgressMonitor();
        }
        checkMonitor(monitor);
        String label = "";
        try {
            label = processItem.getProperty().getLabel();
        } catch (Exception e) {
            // ignore exception as it happens only during label retrieval and is not critical
        }
        monitor.subTask(Messages.getString("migration.check.process.item", label));
        final ProcessTypeImpl process = (ProcessTypeImpl) processItem.getProcess();
        boolean migrated = false;
        for (final Object elem : process.getNode()) {
            NodeTypeImpl node = (NodeTypeImpl) elem;
            if (TaCoKitNode.isTacokit(node)) {
                final TaCoKitNode tacokitNode = new TaCoKitNode(node);
                if (tacokitNode.needsMigration()) {
                    tacokitNode.migrate(componentClient.migrate(tacokitNode.getId(), tacokitNode.getPersistedVersion(),
                            tacokitNode.getPropertiesToMigrate()));
                    migrated = true;
                }
            }
        }
        if (migrated) {
            final IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
            try {
                factory.save(processItem);
            } catch (PersistenceException e) {
                ExceptionHandler.process(e);
            }
        }
    }

    private List<Project> getAllProjects() {
        final List<Project> allProjects = new ArrayList<>();
        final Project currentProject = ProjectManager.getInstance().getCurrentProject();
        allProjects.add(currentProject);
        final List<Project> referencedProjects = ProjectManager.getInstance().getAllReferencedProjects();
        allProjects.addAll(referencedProjects);
        return allProjects;
    }

    private void checkMigration(final ConfigTypeNode configTypeNode, final IProgressMonitor progressMonitor) throws Exception {
        IProgressMonitor monitor = progressMonitor;
        if (monitor == null) {
            monitor = new NullProgressMonitor();
        }
        checkMonitor(monitor);
        monitor.subTask(Messages.getString("migration.check.progress.currentConfiguration", configTypeNode.getDisplayName())); //$NON-NLS-1$
        ERepositoryObjectType repoObjType = TaCoKitUtil.getOrCreateERepositoryObjectType(configTypeNode);
        ProxyRepositoryFactory repoFactory = ProxyRepositoryFactory.getInstance();

        for (final Project project : getAllProjects()) {
            checkMonitor(monitor);
            monitor.subTask(Messages.getString("migration.check.progress.listItems", configTypeNode.getDisplayName(), //$NON-NLS-1$
                    project.getLabel()));
            List<IRepositoryViewObject> allRepoViewObjs = repoFactory.getAll(project, repoObjType, true, true);
            checkMonitor(monitor);
            if (allRepoViewObjs != null && !allRepoViewObjs.isEmpty()) {
                VersionList latestVersion = new VersionList(false);
                latestVersion.addAll(allRepoViewObjs);
                for (IRepositoryViewObject repoViewObj : allRepoViewObjs) {
                    try {
                        ConnectionItem item = (ConnectionItem) repoViewObj.getProperty().getItem();
                        String itemLabel = ""; //$NON-NLS-1$
                        try {
                            itemLabel = item.getProperty().getLabel();
                        } catch (Exception e) {
                            // ignore
                        }
                        if (checkMigration(item, progressMonitor)) {
                            String version = item.getProperty().getVersion();
                            if (repoViewObj == latestVersion.get(0)) {
                                version = RelationshipItemBuilder.LATEST_VERSION;
                            }
                            monitor.subTask(Messages.getString("migration.check.progress.save", itemLabel, version)); //$NON-NLS-1$
                            repoFactory.save(item);
                            updatedRelatedItems(item, version, progressMonitor);
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

    private boolean checkMigration(final ConnectionItem item, final IProgressMonitor progressMonitor) throws Exception {
        IProgressMonitor monitor = progressMonitor;
        if (monitor == null) {
            monitor = new NullProgressMonitor();
        }
        checkMonitor(monitor);
        String itemLabel = ""; //$NON-NLS-1$
        String version = ""; //$NON-NLS-1$
        try {
            Property property = item.getProperty();
            itemLabel = property.getLabel();
            version = property.getVersion();
        } catch (Exception e) {
            // ignore
        }
        monitor.subTask(Messages.getString("migration.check.progress.start", itemLabel, version)); //$NON-NLS-1$
        TaCoKitConfigurationModel configModel = new TaCoKitConfigurationModel(item.getConnection());
        if (configModel.needsMigration()) {
            migrate(configModel, progressMonitor);
            return true;
        }
        return false;
    }

    private void checkMonitor(final IProgressMonitor monitor) throws UserCancelledException {
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
        String label = configModel.getConnection().getLabel();
        final int storedVersion = configModel.getVersion();
        final int newVersion = configModel.getConfigurationVersion();
        monitor.subTask(Messages.getString("migration.check.progress.execute", label, storedVersion, newVersion)); //$NON-NLS-1$

        Map<String, String> migratedProperties = configurationClient.migrate(configModel.getConfigurationId(),
                configModel.getVersion(), configModel.getProperties());
        configModel.migrate(migratedProperties);
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
                    workspace.run(workspaceMonitor -> ProxyRepositoryFactory.getInstance()
                            .executeRepositoryWorkUnit(new RepositoryWorkUnit(title) {

                                @Override
                                protected void run() {
                                    try {
                                        checkMigration(workspaceMonitor);
                                    } catch (Exception e) {
                                        ExceptionHandler.process(e);
                                    }
                                }
                            }), refreshRule, IWorkspace.AVOID_UPDATE, jobMonitor);
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

    public void updatedRelatedItems(final ConnectionItem item, final String version, final IProgressMonitor progressMonitor) {
        IProgressMonitor monitor = progressMonitor;
        if (monitor == null) {
            monitor = new NullProgressMonitor();
        }
        String label = ""; //$NON-NLS-1$
        try {
            label = item.getProperty().getLabel();
        } catch (Exception e) {
            // ignore
        }

        monitor.subTask(Messages.getString("migration.check.progress.updateRelated", label, version)); //$NON-NLS-1$
        RepositoryUpdateManager.updateDBConnection(item, version, false, false);
    }

}
