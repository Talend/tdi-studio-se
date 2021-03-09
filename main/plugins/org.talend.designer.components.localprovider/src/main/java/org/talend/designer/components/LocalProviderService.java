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
package org.talend.designer.components;

import java.util.Locale;
import java.util.ResourceBundle;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.talend.designer.core.ILocalProviderService;

/**
 * DOC guanglong.du class global comment. Detailled comment
 */
public class LocalProviderService implements ILocalProviderService {

    /*
     * (non-Jsdoc)
     *
     * @see org.talend.designer.core.ILocalProviderService#getResourceBundle(java.lang.String)
     */
    public ResourceBundle getResourceBundle(String label) {
        return ResourceBundle.getBundle(label, Locale.getDefault(), new ResClassLoader(getClass().getClassLoader()));
    }

    public AbstractUIPlugin getPlugin() {
        return ComponentsLocalProviderPlugin.getDefault();
    }
}
