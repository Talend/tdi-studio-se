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

import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.update.RepositoryUpdateManager;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.designer.core.model.utils.emf.talendfile.impl.NodeTypeImpl;
import org.talend.designer.core.model.utils.emf.talendfile.impl.ProcessTypeImpl;
import org.talend.designer.core.utils.JavaProcessUtil;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.sdk.component.studio.Lookups;
import org.talend.sdk.component.studio.exception.UserCancelledException;
import org.talend.sdk.component.studio.i18n.Messages;
import org.talend.sdk.component.studio.metadata.model.TaCoKitConfigurationModel;
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
        checkNodeMigration(processItem, null);
    }

    public void checkNodeMigration(final ProcessItem processItem, String currentNodeName) {
        final ProcessTypeImpl process = (ProcessTypeImpl) processItem.getProcess();
        boolean migrated = false;
        for (final Object elem : process.getNode()) {
            NodeTypeImpl node = (NodeTypeImpl) elem;
            if (TaCoKitNode.isTacokit(node)) {
                if (currentNodeName != null) {
                    if (!node.getComponentName().equals(currentNodeName)) {
                        continue;
                    }
                }
                final TaCoKitNode tacokitNode = new TaCoKitNode(node);
                // Check from version properties / component version
                boolean needMigration = JavaProcessUtil.needMigration(node.getComponentName(), node.getElementParameter());
                if (needMigration || tacokitNode.needsMigration()) {
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
