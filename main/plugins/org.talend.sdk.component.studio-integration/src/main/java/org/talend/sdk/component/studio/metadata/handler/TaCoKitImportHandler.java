package org.talend.sdk.component.studio.metadata.handler;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.repository.ProjectManager;
import org.talend.repository.items.importexport.handlers.imports.MetadataConnectionImportHandler;
import org.talend.repository.items.importexport.handlers.model.ImportItem;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.sdk.component.server.front.model.ConfigTypeNode;
import org.talend.sdk.component.studio.Lookups;
import org.talend.sdk.component.studio.metadata.migration.TaCoKitMigrationManager;
import org.talend.sdk.component.studio.metadata.model.TaCoKitConfigurationModel;
import org.talend.sdk.component.studio.util.TaCoKitUtil;

/**
 * Connection Import Handler for Tacokit components. It is used during metadata
 * import
 */
public class TaCoKitImportHandler extends MetadataConnectionImportHandler {

    /**
     * Constructor fills up Tacokit item types present in the product. Metadata for
     * non-existent Component won't be imported
     */
    public TaCoKitImportHandler() {
        final Map<String, ConfigTypeNode> configTypeNodes = Lookups.taCoKitCache().getConfigTypeNodeMap();
        try {
            for (final ConfigTypeNode node : configTypeNodes.values()) {
                // filter parent nodes
                if (StringUtils.isBlank(node.getParentId())) {
                    continue;
                }
                final ERepositoryObjectType type = TaCoKitUtil.getOrCreateERepositoryObjectType(node);
                checkedItemTypes.add(type);
            }
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }
    }

    /**
     * Applies migration using following algorithm:
     * <ol>
     *  <li>Retrieve imported metadata from repository (as migration is applied after import)</li>
     *  <li>Check if migration is needed. Migration is needed, when imported metadata has lower version than current</li>
     *  <li>Apply migration on Metadata Connection item</li>
     *  <li>Set migrated item to ImportItem</li>
     * </ol>
     * 
     * @param importItem item which is being imported
     * @param monitor progress monitor
     */
    @Override
    public void applyMigrationTasks(final ImportItem importItem, final IProgressMonitor monitor) {
        try {
            getItem(importItem).ifPresent(connectionItem -> {
                final TaCoKitConfigurationModel configuration = new TaCoKitConfigurationModel(connectionItem.getConnection());
                if (configuration.needsMigration()) {
                    try {
                        manager().migrate(configuration, monitor);
                        importItem.setProperty(connectionItem.getProperty());
                        final IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
                        factory.save(connectionItem);
                    } catch (Exception e) {
                        logError(e);
                    }
                }
            });
            super.applyMigrationTasks(importItem, monitor);
        } catch (Exception e) {
            logError(e);
        }
    }

    /**
     * Loads repository item. Implementation was copied from
     * ImportBasicHandler.applyMigrationTasks()
     * 
     * @param importItem
     *            item which is imported
     * @return Connection item stored in repository
     * @throws PersistenceException
     */
    private Optional<ConnectionItem> getItem(final ImportItem importItem) throws PersistenceException {
        List<IRepositoryViewObject> allVersion = ProxyRepositoryFactory.getInstance().getAllVersion(
                ProjectManager.getInstance().getCurrentProject(), importItem.getItemId(), importItem.getImportPath(),
                importItem.getRepositoryType());
        for (IRepositoryViewObject repositoryObject : allVersion) {
            if (repositoryObject.getProperty().getVersion().equals(importItem.getItemVersion())) {
                final Item item = repositoryObject.getProperty().getItem();
                if (ConnectionItem.class.isInstance(item)) {
                    return Optional.of((ConnectionItem) item);
                }
            }
        }
        return Optional.empty();
    }
    
    private TaCoKitMigrationManager manager() {
        return Lookups.taCoKitCache().getMigrationManager();
    }

}
