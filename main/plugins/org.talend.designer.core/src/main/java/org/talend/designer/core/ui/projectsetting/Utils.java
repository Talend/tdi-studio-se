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
package org.talend.designer.core.ui.projectsetting;

import org.talend.core.language.LanguageManager;
import org.talend.core.model.utils.ContextParameterUtils;
import org.talend.core.model.utils.TalendTextUtils;

/**
 * DOC aimingchen class global comment. Detailled comment
 */
public class Utils {

    /**
     * This function will add quotes only if necessary for the stats & logs.
     *
     * @param str
     * @return
     */
    public static String addQuotes(String str) {
        // function?
        if (str.contains("(") && str.contains(")")) { //$NON-NLS-1$ //$NON-NLS-2$
            return str;
        }

        switch (LanguageManager.getCurrentLanguage()) {
        case JAVA:
            // if the user already added quotes (anywhere) then don't add.
            if (str.contains("\"")) { //$NON-NLS-1$
                return str;
            }
            break;
        default: // PERL
            // if the user already added quotes (anywhere) then don't add.
            if (str.contains("'")) { //$NON-NLS-1$
                return str;
            }
        }
        if (ContextParameterUtils.containContextVariables(str)) {
            return str;
        }
        return TalendTextUtils.addQuotes(str);
    }

    public static String replaceSlash(String str) {
        String tempStr = str.replaceAll("\\\\", "/"); //$NON-NLS-1$ //$NON-NLS-2$
        return tempStr;
    }

}
