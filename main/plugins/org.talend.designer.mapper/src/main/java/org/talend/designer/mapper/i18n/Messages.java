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
package org.talend.designer.mapper.i18n;

import java.util.ResourceBundle;

import org.talend.commons.i18n.MessagesCore;

/**
 * class global comment. Detailled comment <br/>
 *
 * $Id: Messages.java 2007-2-3下午03:03:12 bqian $
 *
 */
public class Messages extends MessagesCore {

    private static final String BUNDLE_NAME = "messages"; //$NON-NLS-1$

    private static final String PLUGIN_ID = "org.talend.designer.mapper"; //$NON-NLS-1$

    private final static ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

    private Messages() {
    }

    /**
     * Returns the i18n formatted message for <i>key</i> in the class bundle.
     *
     * @param key - the key for the desired string
     * @return the string for the given key in the class resource bundle
     * @see MessagesCore#getString(String, ResourceBundle)
     */
    public static String getString(final String key) {
        return getString(key, PLUGIN_ID, RESOURCE_BUNDLE);
    }

    /**
     * Returns the i18n formatted message for <i>key</i> and <i>args</i> in the specified bundle.
     *
     * @param key - the key for the desired string
     * @param args - arg to include in the string
     * @return the string for the given key in the given resource bundle
     * @see MessagesCore#getString(String, ResourceBundle, Object[])
     */
    public static String getString(final String key, final Object... args) {
        return MessagesCore.getString(key, PLUGIN_ID, RESOURCE_BUNDLE, args);
    }
}
