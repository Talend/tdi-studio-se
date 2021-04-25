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
package org.talend.designer.core.ui.preferences;

import org.talend.designer.core.ui.views.RefreshView;

/**
 * DOC wzhang class global comment. Detailled comment
 */
public class I18nPreferencePage extends org.talend.core.ui.preference.I18nPreferencePage {

    @Override
    protected void refreshAll() {
        RefreshView.refreshAll();
    }
}
