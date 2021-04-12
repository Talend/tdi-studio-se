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
package org.talend.repository.generic.util;

import org.talend.core.runtime.services.IGenericWizardService;
import org.talend.repository.generic.internal.IGenericWizardInternalService;
import org.talend.repository.generic.internal.service.GenericWizardInternalService;
import org.talend.repository.generic.service.GenericWizardService;

/**
 * created by ycbai on 2015年9月16日 Detailled comment
 *
 */
public class GenericWizardServiceFactory {

    private static IGenericWizardService service = null;

    private static IGenericWizardInternalService internalService = null;

    public static IGenericWizardService getGenericWizardService() {
        if (service == null) {
            service = new GenericWizardService();
        }
        return service;
    }

    public static IGenericWizardInternalService getGenericWizardInternalService() {
        if (internalService == null) {
            internalService = new GenericWizardInternalService();
        }
        return internalService;
    }

}

