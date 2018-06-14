package org.talend.sdk.component.studio.metadata.handler;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.relationship.RelationshipItemBuilder;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.repository.model.VersionList;
import org.talend.repository.ProjectManager;
import org.talend.repository.items.importexport.handlers.model.ImportItem;
import org.talend.repository.items.importexport.manager.ResourcesManager;
import org.talend.sdk.component.studio.Lookups;
import org.talend.sdk.component.studio.metadata.migration.TaCoKitMigrationManager;

/**
 * Updates related Jobs after metadata migration (during metadata import)
 */
public class TaCoKitRelatedItemsUpdater extends AbstractImportResourcesHandler {

    @Override
    public void postImport(IProgressMonitor monitor, ResourcesManager resManager, ImportItem[] importedItemRecords) {
        if (importedItemRecords == null) {
            return;
        }
        for (final ImportItem importItem : importedItemRecords) {
            try {
                final List<IRepositoryViewObject> allVersion = ProxyRepositoryFactory.getInstance().getAllVersion(
                        ProjectManager.getInstance().getCurrentProject(), importItem.getItemId(), importItem.getImportPath(),
                        importItem.getRepositoryType());
                VersionList versions = new VersionList(false);
                versions.addAll(allVersion);
                for (IRepositoryViewObject repositoryObject : allVersion) {
                    if (repositoryObject.getProperty().getVersion().equals(importItem.getItemVersion())) {
                        final Item item = repositoryObject.getProperty().getItem();
                        if (ConnectionItem.class.isInstance(item)) {
                            String version = getVersion(item, repositoryObject, versions);
                            manager().updatedRelatedItems((ConnectionItem) item, version, monitor);
                        }
                    }
                }
            } catch (Exception e) {
                ExceptionHandler.process(e);
            }
        }
    }
    
    private String getVersion(final Item item, final IRepositoryViewObject repositoryObject, final VersionList versions) {
        if (repositoryObject == versions.get(0)) {
            return RelationshipItemBuilder.LATEST_VERSION;
        } else {
            return item.getProperty().getVersion();
        }
    }
    
    private TaCoKitMigrationManager manager() {
        return Lookups.taCoKitCache().getMigrationManager();
    }
}
