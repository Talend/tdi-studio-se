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
package org.talend.repository.view.di.viewer.handlers;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.talend.commons.CommonsPlugin;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.ILibraryManagerService;
import org.talend.core.model.general.ILibrariesService;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.RoutinesJarItem;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.routines.RoutineLibraryMananger;
import org.talend.core.model.routines.RoutinesUtil;
import org.talend.core.repository.utils.RoutineUtils;
import org.talend.core.runtime.CoreRuntimePlugin;
import org.talend.core.utils.ProductUtils;
import org.talend.designer.core.model.utils.emf.component.IMPORTType;
import org.talend.repository.items.importexport.handlers.imports.IImportResourcesHandler;
import org.talend.repository.items.importexport.handlers.imports.ImportRepTypeHandler;
import org.talend.repository.items.importexport.handlers.model.ImportItem;
import org.talend.repository.items.importexport.manager.ResourcesManager;
import org.talend.repository.items.importexport.ui.managers.ProviderManager;
import org.talend.repository.items.importexport.ui.managers.ZipFileManager;
import org.talend.repository.view.di.viewer.handlers.util.ImportHandlerUtil;

/**
 * DOC jding  class global comment. Detailled comment
 */
public class RoutinesJarImportHandler extends ImportRepTypeHandler implements IImportResourcesHandler {

    // mvnUrl -> file url
    private static Map<String, URL> jarsToDeploy = new HashMap<String, URL>();

    public RoutinesJarImportHandler() {
        super();
    }

    /**
     * override to use this handler import routinesJar and inner routines
     */
    @Override
    public boolean valid(ImportItem importItem) {
        if (importItem == null || importItem.getItem() == null || importItem.getType() == null) {
            return false;
        }
        ERepositoryObjectType itemType = importItem.getType();
        if (itemType == ERepositoryObjectType.ROUTINESJAR || RoutinesUtil.isInnerCodes(importItem.getProperty())) {
            // if in studio, check the product.
            if (!CommonsPlugin.isHeadless() && isEnableProductChecking()) {
                String currentPerspectiveId = CoreRuntimePlugin.getInstance().getActivedPerspectiveId();
                String[] products = itemType.getProducts();
                if (products != null) {
                    for (String product : products) {
                        String perspectiveId = ProductUtils.getPerspectiveId(product);
                        if (currentPerspectiveId != null && currentPerspectiveId.equals(perspectiveId)) {
                            return true; // match the product and perspective.
                        }
                    }
                    return false; // if enable product check, have set the product.
                }
            }
            return true;
        }

        return false;
    }


    @Override
    protected void beforeCreatingItem(ImportItem selectedImportItem) {
        super.beforeCreatingItem(selectedImportItem);
        if (RoutinesUtil.isInnerCodes(selectedImportItem.getProperty())) {
            RoutineUtils.changeInnerCodePackage(selectedImportItem.getItem(), true);
        }
    }

    @Override
    public void afterImportingItems(IProgressMonitor monitor, ResourcesManager resManager, ImportItem importItem) {
        Item item = importItem.getItem();
        if (item instanceof RoutinesJarItem) {
            RoutinesJarItem routinesJarItem = (RoutinesJarItem) item;
            Map<String, String> extRoutines = new HashMap<String, String>();
            for (IMPORTType type : (List<IMPORTType>) routinesJarItem.getRoutinesJarType().getImports()) {
                extRoutines.put(type.getMODULE(), type.getMVN());
            }
            if (resManager instanceof ProviderManager || resManager instanceof ZipFileManager) {
                ImportHandlerUtil.deployJarToDestForArchive(resManager, extRoutines, jarsToDeploy);
            } else {
                ImportHandlerUtil.deployJarToDest(resManager, extRoutines, jarsToDeploy);
            }
        }
        super.afterImportingItems(monitor, resManager, importItem);
    }

    @Override
    public void prePopulate(IProgressMonitor monitor, ResourcesManager resManager) {
        // do nothing
    }

    @Override
    public void postPopulate(IProgressMonitor monitor, ResourcesManager resManager, ImportItem[] populatedItemRecords) {
        // do nothing
    }

    @Override
    public void preImport(IProgressMonitor monitor, ResourcesManager resManager, ImportItem[] checkedItemRecords,
            ImportItem[] allImportItemRecords) {
        jarsToDeploy.clear();
    }

    @Override
    public void postImport(IProgressMonitor monitor, ResourcesManager resManager, ImportItem[] importedItemRecords) {
        if (jarsToDeploy.size() > 0) {
            boolean canCheckNeedDeploy = false;
            ILibrariesService libService = (ILibrariesService) GlobalServiceRegister.getDefault()
                    .getService(ILibrariesService.class);
            ILibraryManagerService librairesManagerService = (ILibraryManagerService) GlobalServiceRegister.getDefault()
                    .getService(ILibraryManagerService.class);
            if (librairesManagerService != null) {
                canCheckNeedDeploy = true;
            }
            try {
                for (String mvnUrl : jarsToDeploy.keySet()) {
                    URL url = jarsToDeploy.get(mvnUrl);
                    if (canCheckNeedDeploy && !RoutineLibraryMananger.getInstance().needDeploy(url, mvnUrl)) {
                        continue;
                    }
                    libService.deployLibrary(url, mvnUrl, false);
                }
            } catch (Exception e) {
                ExceptionHandler.process(e);
            }

        }
        jarsToDeploy.clear();
    }

}
