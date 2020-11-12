// ============================================================================
//
// Copyright (C) 2006-2020 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.repository.ui.login.connections.network.proxy;

public class LoginProxyPreferencePage extends ProxyPreferencePage {

    @Override
    protected void performApply() {
        if (this.isControlCreated()) {
            super.performApply();
        }
    }

    @Override
    public boolean performOk() {
        if (this.isControlCreated()) {
            return super.performOk();
        } else {
            return true;
        }
    }

    @Override
    protected void performDefaults() {
        if (this.isControlCreated()) {
            super.performDefaults();
        }
    }

}
