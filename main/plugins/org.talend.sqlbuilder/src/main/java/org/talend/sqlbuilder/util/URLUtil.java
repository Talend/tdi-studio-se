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
package org.talend.sqlbuilder.util;

import java.net.URL;

import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;
import org.talend.sqlbuilder.SqlBuilderPlugin;

/**
 *
 * DOC dev class global comment. Detailled comment <br/>
 *
 * $Id: URLUtil.java,v 1.2 2006/10/26 21:39:06 qiang.zhang Exp $
 *
 */
public final class URLUtil {

    private URLUtil() {
    }

    /**
     * DOC dev Comment method "getResourceURL".
     *
     * @param s
     * @return
     */
    public static URL getResourceURL(String s) {
        if (!initialized) {
            init();
        }
        URL url = null;
        try {
            url = new URL(baseURL, s);
        } catch (Throwable e) {
            SqlBuilderPlugin.log("", e); //$NON-NLS-1$
        }
        return url;
    }

    private static boolean initialized = false;

    /**
     * DOC dev Comment method "init".
     */
    private static void init() {
        SqlBuilderPlugin defaultPlugin = SqlBuilderPlugin.getDefault();

        baseURL = defaultPlugin.getBundle().getEntry("/"); //$NON-NLS-1$
        initialized = true;
    }

    private static URL baseURL;

    /**
     * Return a URL to a file located in your plugin fragment.
     *
     * @param yourPluginId e.g net.sourceforge.sqlexplorer.oracle
     * @param filePath path to file within your fragment e.g. icons/test.gif
     * @return URL to the file.
     */
    public static URL getFragmentResourceURL(String yourPluginId, String filePath) {

        URL url = null;

        try {
            Bundle bundle = Platform.getBundle(yourPluginId);
            URL baseUrl = bundle.getEntry("/"); //$NON-NLS-1$
            url = new URL(baseUrl, filePath);
        } catch (Exception e) {
            SqlBuilderPlugin.log(e.getMessage(), e);
        }

        return url;
    }

}
