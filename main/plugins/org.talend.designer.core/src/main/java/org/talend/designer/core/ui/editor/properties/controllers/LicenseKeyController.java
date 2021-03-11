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
package org.talend.designer.core.ui.editor.properties.controllers;

import org.talend.core.ui.properties.tab.IDynamicProperty;
import org.talend.designer.core.i18n.Messages;

public class LicenseKeyController extends PasswordController {

    protected static final String LICENSEKEY = "LICENSEKEY"; //$NON-NLS-1$

    public LicenseKeyController(IDynamicProperty dp) {
        super(dp);
    }

    protected String getDialogTitle() {
        return Messages.getString("LicenseKeyController.NewLicenseKey"); //$NON-NLS-1$
    }
}
