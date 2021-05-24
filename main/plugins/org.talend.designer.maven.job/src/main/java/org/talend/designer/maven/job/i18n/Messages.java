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
package org.talend.designer.maven.job.i18n;

import java.util.ResourceBundle;

import org.talend.commons.i18n.MessagesCore;
import org.talend.designer.maven.job.MavenJobPlugin;

/**
 * DOC ggu class global comment. Detailled comment
 */
public class Messages extends MessagesCore {

    private static final String BUNDLE_NAME = "messages"; //$NON-NLS-1$

    private static ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE_NAME);

    public static String getString(final String key) {
        return getString(key, MavenJobPlugin.PLUGIN_ID, resourceBundle);
    }

    public static String getString(final String key, final Object... args) {
        return MessagesCore.getString(key, MavenJobPlugin.PLUGIN_ID, resourceBundle, args);
    }
}
