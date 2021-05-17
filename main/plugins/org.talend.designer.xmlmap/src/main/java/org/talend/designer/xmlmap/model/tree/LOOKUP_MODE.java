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
package org.talend.designer.xmlmap.model.tree;

import org.talend.core.model.process.ILookupMode;

/**
 *
 * DOC amaumont class global comment. Detailled comment
 */
public enum LOOKUP_MODE implements ILookupMode {
    LOAD_ONCE,
    LOAD_ONCE_AND_UPDATE,
    RELOAD,
    CACHE_OR_RELOAD, ;

    public static LOOKUP_MODE parse(String matchingMode) {
        LOOKUP_MODE multipleMatchingModeResult = null;
        LOOKUP_MODE[] multipleMatchingModes = values();
        for (LOOKUP_MODE multipleMatchingMode : multipleMatchingModes) {
            if (multipleMatchingMode.toString().equals(matchingMode)) {
                multipleMatchingModeResult = multipleMatchingMode;
                break;
            }
        }
        return multipleMatchingModeResult;
    }

}
