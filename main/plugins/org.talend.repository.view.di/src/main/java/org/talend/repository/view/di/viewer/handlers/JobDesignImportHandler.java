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

import java.io.IOException;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.talend.commons.runtime.model.emf.TalendXMIResource;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.ProcessItem;
import org.talend.designer.core.IDesignerCoreService;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.ParametersType;
import org.talend.repository.items.importexport.handlers.HandlerUtil;
import org.talend.repository.items.importexport.handlers.imports.ImportRepTypeHandler;
import org.talend.repository.items.importexport.handlers.model.ImportItem;
import org.talend.repository.items.importexport.manager.ResourcesManager;

/**
 * DOC ggu class global comment. Detailled comment
 */
public class JobDesignImportHandler extends ImportRepTypeHandler {

    /**
     * DOC ggu JobDesignImportHandler constructor comment.
     */
    public JobDesignImportHandler() {
        super();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.repository.items.importexport.handlers.imports.ImportRepTypeHandler#createItemResource(org.eclipse
     * .emf.common.util.URI)
     */
    @Override
    protected Resource createItemResource(URI pathUri) {
        return new TalendXMIResource(pathUri);
    }

    @Override
    protected void afterApplyMigrationTasks(ImportItem importItem) throws Exception {
        super.afterApplyMigrationTasks(importItem);
        Item tmpItem = importItem.getItem();
        if (tmpItem instanceof ProcessItem) {
            ProcessItem processItem = (ProcessItem) tmpItem;
            ParametersType paType = processItem.getProcess().getParameters();
            boolean statsPSettingRemoved = false;

            // for commanline import project setting
            if (importItem.isRemoveProjectStatslog()) {
                if (paType != null) {
                    String paramName = "STATANDLOG_USE_PROJECT_SETTINGS"; //$NON-NLS-1$
                    EList listParamType = paType.getElementParameter();
                    for (int j = 0; j < listParamType.size(); j++) {
                        ElementParameterType pType = (ElementParameterType) listParamType.get(j);
                        if (pType != null && paramName.equals(pType.getName())) {
                            pType.setValue(Boolean.FALSE.toString());
                            statsPSettingRemoved = true;
                            break;
                        }
                    }
                }
            }

            // 14446: item apply project setting param if use project setting
            String statslogUsePSetting = null;
            String implicitUsePSetting = null;
            if (paType != null) {
                EList listParamType = paType.getElementParameter();
                for (int j = 0; j < listParamType.size(); j++) {
                    ElementParameterType pType = (ElementParameterType) listParamType.get(j);
                    if (pType != null) {
                        if (!statsPSettingRemoved && "STATANDLOG_USE_PROJECT_SETTINGS".equals(pType.getName())) { //$NON-NLS-1$
                            statslogUsePSetting = pType.getValue();
                        }
                        if ("IMPLICITCONTEXT_USE_PROJECT_SETTINGS".equals(pType.getName())) { //$NON-NLS-1$
                            implicitUsePSetting = pType.getValue();
                        }
                        if (statsPSettingRemoved && implicitUsePSetting != null || !statsPSettingRemoved
                                && implicitUsePSetting != null && statslogUsePSetting != null) {
                            break;
                        }
                    }
                }
            }
            if (GlobalServiceRegister.getDefault().isServiceRegistered(IDesignerCoreService.class)) {
                IDesignerCoreService designerCoreService = (IDesignerCoreService) GlobalServiceRegister.getDefault().getService(
                        IDesignerCoreService.class);
                if (statslogUsePSetting != null && Boolean.parseBoolean(statslogUsePSetting)) {
                    designerCoreService.reloadParamFromProjectSettings(paType, "STATANDLOG_USE_PROJECT_SETTINGS"); //$NON-NLS-1$

                }
                if (implicitUsePSetting != null && Boolean.parseBoolean(implicitUsePSetting)) {
                    designerCoreService.reloadParamFromProjectSettings(paType, "IMPLICITCONTEXT_USE_PROJECT_SETTINGS"); //$NON-NLS-1$

                }
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.repository.items.importexport.handlers.imports.ImportRepTypeHandler#copyReferenceFiles(org.talend
     * .repository.items.importexport.ui.wizard.imports.managers.ResourcesManager,
     * org.talend.repository.items.importexport.ui.wizard.imports.models.ItemRecord)
     */
    @Override
    protected boolean copyReferenceFiles(ResourcesManager resManager, ImportItem importItem) throws IOException {
        HandlerUtil.copyScreenshotFile(resManager, importItem);
        return super.copyReferenceFiles(resManager, importItem);
    }

}
