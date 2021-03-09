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
package org.talend.repository.ui.actions.importproject;

import org.talend.repository.i18n.Messages;

/**
 * DOC Administrator class global comment. Detailled comment <br/>
 *
 * @author ftang, 2007-07-02
 *
 */
public enum EDemoProjectFileType {
    FOLDER("folder"), //$NON-NLS-1$
    ARCHIVE("archive"); //$NON-NLS-1$

    private String name;

    private EDemoProjectFileType(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public static EDemoProjectFileType getDemoProjectFileTypeName(String name) {
        for (EDemoProjectFileType demoProjectFileType : EDemoProjectFileType.values()) {
            if (demoProjectFileType.getName().equals(name)) {
                return demoProjectFileType;
            }
        }
        throw new UnsupportedOperationException(Messages.getString("EDemoProjectFileType.unknowType")); //$NON-NLS-1$
    }
}
