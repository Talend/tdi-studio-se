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
package org.talend.designer.alfrescooutput.util;

/**
 * DOC ke class global comment. Detailled comment <br/>
 *
 */
public class StringUtil {

    public static boolean validateLabelForXML(String label) {
        if (label == null) {
            return false;
        }
        if (label.length() < 1) {
            return false;
        }
        char firstChar = label.charAt(0);
        if (!Character.isLetter(firstChar)) {
            return false;
        }
        if (label.toLowerCase().startsWith("xml")) { //$NON-NLS-1$
            return false;
        }
        char[] array = label.toCharArray();
        for (int i = 0; i < array.length; i++) {
            if (Character.isSpaceChar(array[i]) || Character.isWhitespace(array[i])) {
                return false;
            }
        }
        return true;
    }

}
