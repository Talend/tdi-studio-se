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
package org.talend.designer.components.lookup.common;

import org.talend.core.model.process.IMatchingMode;

/**
 *
 * DOC amaumont  class global comment. Detailled comment
 */
public interface ICommonLookup {

    /**
     *
     * DOC amaumont AdvancedLookup class global comment. Detailled comment <br/>
     *
     */
    public enum MATCHING_MODE implements IMatchingMode {
        ALL_ROWS,
        ALL_MATCHES,
        FIRST_MATCH,
        LAST_MATCH,
        UNIQUE_MATCH, ;

        public static MATCHING_MODE parse(String matchingMode) {
            MATCHING_MODE multipleMatchingModeResult = null;
            MATCHING_MODE[] multipleMatchingModes = values();
            for (MATCHING_MODE multipleMatchingMode : multipleMatchingModes) {
                if (multipleMatchingMode.toString().equals(matchingMode)) {
                    multipleMatchingModeResult = multipleMatchingMode;
                    break;
                }
            }
            return multipleMatchingModeResult;
        }

    }

}
