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
package org.talend.repository.generic.handler;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.runtime.services.IGenericWizardService;
import org.talend.repository.items.importexport.handlers.imports.MetadataConnectionImportHandler;

/**
 * created by ycbai on 2015年11月9日 Detailled comment
 *
 */
public class GenericImportHandler extends MetadataConnectionImportHandler {

    public GenericImportHandler() {
        super();
        appendGenericTypes();
    }

    private void appendGenericTypes() {
        IGenericWizardService wizardService = null;
        if (GlobalServiceRegister.getDefault().isServiceRegistered(IGenericWizardService.class)) {
            wizardService = (IGenericWizardService) GlobalServiceRegister.getDefault().getService(IGenericWizardService.class);
        }
        if (wizardService != null) {
            List<String> genericTypeNames = wizardService.getGenericTypeNames();
            for (String genericType : genericTypeNames) {
                ERepositoryObjectType repObjType = ERepositoryObjectType.valueOf(genericType);
                if (repObjType != null) {
                    checkedItemTypes.add(repObjType);
                    if (StringUtils.isNotEmpty(repObjType.getFolder())) {
                        checkedBasePathes.add(repObjType.getFolder());
                    }
                }
            }
        }
    }

}
