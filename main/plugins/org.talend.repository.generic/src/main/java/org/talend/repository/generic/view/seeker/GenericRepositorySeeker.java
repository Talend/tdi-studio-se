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
package org.talend.repository.generic.view.seeker;

import java.util.ArrayList;
import java.util.List;

import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.runtime.services.IGenericWizardService;
import org.talend.repository.metadata.seeker.AbstractMetadataRepoViewSeeker;

/**
 *
 * created by ycbai on 2016年3月18日 Detailled comment
 *
 */
public class GenericRepositorySeeker extends AbstractMetadataRepoViewSeeker {

    @Override
    protected List<ERepositoryObjectType> getValidationTypes() {
        List<ERepositoryObjectType> validationTypes = super.getValidationTypes();
        validationTypes.addAll(getGenericTypes());
        return validationTypes;
    }

    private List<ERepositoryObjectType> getGenericTypes() {
        List<ERepositoryObjectType> repTypes = new ArrayList<>();
        IGenericWizardService wizardService = null;
        if (GlobalServiceRegister.getDefault().isServiceRegistered(IGenericWizardService.class)) {
            wizardService = (IGenericWizardService) GlobalServiceRegister.getDefault().getService(IGenericWizardService.class);
        }
        if (wizardService != null) {
            List<String> genericTypeNames = wizardService.getGenericTypeNames();
            for (String genericType : genericTypeNames) {
                ERepositoryObjectType repObjType = ERepositoryObjectType.valueOf(genericType);
                if (repObjType != null) {
                    repTypes.add(repObjType);
                }
            }
        }
        return repTypes;
    }

}
