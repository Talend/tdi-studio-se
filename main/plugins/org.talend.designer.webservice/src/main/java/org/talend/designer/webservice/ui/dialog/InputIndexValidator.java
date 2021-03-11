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
package org.talend.designer.webservice.ui.dialog;

import org.eclipse.jface.dialogs.IInputValidator;

/**
 * DOC gcui class global comment. Detailled comment
 */
public class InputIndexValidator implements IInputValidator {

    public String isValid(String newText) {

        try {
            int index = Integer.parseInt(newText);
            if (index < 0) {
                return "Please input a right number of index";
            }
        } catch (NumberFormatException err) {
            return "Please input a number of index";
        }
        return null;
    }
}
