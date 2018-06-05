package org.talend.sdk.component.studio.metadata.handler;

import org.eclipse.core.runtime.IProgressMonitor;
import org.talend.repository.items.importexport.handlers.imports.IImportResourcesHandler;
import org.talend.repository.items.importexport.handlers.model.ImportItem;
import org.talend.repository.items.importexport.manager.ResourcesManager;

/**
 * Abstract implementation for IImportResourcesHandler which provides empty implementation for all interface methods.
 * Concrete imlpementation ofter doesn't need to override all methods, so this class simplifies overriding
 */
public abstract class AbstractImportResourcesHandler implements IImportResourcesHandler {

    @Override
    public void prePopulate(IProgressMonitor monitor, ResourcesManager resManager) {
        // no-op
    }

    @Override
    public void postPopulate(IProgressMonitor monitor, ResourcesManager resManager, ImportItem[] populatedItemRecords) {
        // no-op
    }

    @Override
    public void preImport(IProgressMonitor monitor, ResourcesManager resManager, ImportItem[] checkedItemRecords,
            ImportItem[] allImportItemRecords) {
        // no-op
    }

    @Override
    public void postImport(IProgressMonitor monitor, ResourcesManager resManager, ImportItem[] importedItemRecords) {
        // no-op
    }

}
