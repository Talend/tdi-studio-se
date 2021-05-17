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
package org.talend.designer.codegen.components.ui;

import org.talend.designer.codegen.i18n.Messages;

/**
 *
 DOC zli class global comment. Detailled comment
 */
public enum LINK_STYLE {
    AUTO(Messages.getString("prefs.configuration.LINK_STYLE.AUTO")), //$NON-NLS-1$
    BEZIER_CURVE(Messages.getString("prefs.configuration.LINK_STYLE.BEZIER_CURVE")), //$NON-NLS-1$
    LINE(Messages.getString("prefs.configuration.LINK_STYLE.LINE")), ; //$NON-NLS-1$

    private String displayName;

    LINK_STYLE(String displayName) {
        this.displayName = displayName;
    }

    public String getName() {
        return this.toString();
    }

    public String getDisplayName() {
        return this.displayName;
    }
}
