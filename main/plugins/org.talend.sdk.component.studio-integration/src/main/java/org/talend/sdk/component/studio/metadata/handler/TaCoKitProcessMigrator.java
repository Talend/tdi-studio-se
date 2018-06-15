package org.talend.sdk.component.studio.metadata.handler;

import java.util.List;
import java.util.Optional;

import org.eclipse.core.runtime.IProgressMonitor;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.repository.ProjectManager;
import org.talend.repository.items.importexport.handlers.model.ImportItem;
import org.talend.repository.items.importexport.manager.ResourcesManager;
import org.talend.sdk.component.studio.Lookups;
import org.talend.sdk.component.studio.metadata.migration.TaCoKitMigrationManager;

public class TaCoKitProcessMigrator extends AbstractImportResourcesHandler {
    
    private final TaCoKitMigrationManager manager = Lookups.taCoKitCache().getMigrationManager();

    @Override
    public void postImport(IProgressMonitor monitor, ResourcesManager resManager, ImportItem[] importedItemRecords) {
        if (importedItemRecords == null) {
            return;
        }
        for (final ImportItem importItem : importedItemRecords) {
            try {
                Optional<ProcessItem> processItem = getItem(importItem);
                if (processItem.isPresent()) {
                    manager.checkProcessItemMigration(processItem.get(), monitor);
                }
            } catch (Exception e) {
                ExceptionHandler.process(e);
            }
        }
    }
    
    /**
     * Loads repository item. Implementation was copied from
     * ImportBasicHandler.applyMigrationTasks()
     * 
     * @param importItem
     *            item which is imported
     * @return Process item stored in repository
     * @throws PersistenceException
     */
    private Optional<ProcessItem> getItem(final ImportItem importItem) throws PersistenceException {
        List<IRepositoryViewObject> allVersion = ProxyRepositoryFactory.getInstance().getAllVersion(
                ProjectManager.getInstance().getCurrentProject(), importItem.getItemId(), importItem.getImportPath(),
                importItem.getRepositoryType());
        for (IRepositoryViewObject repositoryObject : allVersion) {
            if (repositoryObject.getProperty().getVersion().equals(importItem.getItemVersion())) {
                final Item item = repositoryObject.getProperty().getItem();
                if (ProcessItem.class.isInstance(item)) {
                    return Optional.of((ProcessItem) item);
                }
            }
        }
        return Optional.empty();
    }

}
