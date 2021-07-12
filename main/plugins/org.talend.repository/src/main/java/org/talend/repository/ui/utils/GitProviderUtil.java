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
package org.talend.repository.ui.utils;

import org.talend.core.services.IGITProviderService;

/**
 * DOC jding  class global comment. Detailled comment
 */
public class GitProviderUtil {

    public static boolean isProjectInGitMode() {
        IGITProviderService providerService = IGITProviderService.get();
        if (providerService != null) {
            return providerService.isProjectInGitMode();
        }
        return false;
    }
}
