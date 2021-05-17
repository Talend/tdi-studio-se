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
package org.talend.designer.core.utils;

import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.program.Program;
import org.talend.commons.utils.VersionUtils;
import org.talend.core.CorePlugin;
import org.talend.core.PluginChecker;
import org.talend.core.prefs.ITalendCorePrefConstants;

public class ComponentsHelpUtil {

    private static final String JVM_PARAM_ONLINE_HELP_VERSION = "online.help.version"; //$NON-NLS-1$

    private static String INTERNAL_VERSION = null;

    private static Boolean IS_RELEASE_VERSION = null;

    private static String PRODUCT_BASE_VERSION = null;

    static {
        initVersionData();
    }

    public static void initVersionData() {
        if (INTERNAL_VERSION == null) {
            INTERNAL_VERSION = VersionUtils.getInternalVersion();
        }
        if (IS_RELEASE_VERSION == null) {
            IS_RELEASE_VERSION = INTERNAL_VERSION.indexOf("-") < 0 //$NON-NLS-1$
                    || INTERNAL_VERSION.toLowerCase().indexOf("patch") >= 0;
            PRODUCT_BASE_VERSION = INTERNAL_VERSION.substring(0, 3);
        }
    }

    public static boolean isUseOnLineHelp() {
        return true;
    }

    public static String calOnLineHelpURL(String componentName) {
        StringBuilder sb = new StringBuilder();
        if (IS_RELEASE_VERSION) {
            sb.append("https://help.talend.com/access/sources/content/topic?pageid="); //$NON-NLS-1$
        } else {
            sb.append("https://talend-staging.fluidtopics.net/access/sources/content/topic?pageid="); //$NON-NLS-1$
        }
        sb.append(componentName.toLowerCase());
        sb.append("&afs:lang=").append(getLanguage()); //$NON-NLS-1$
        sb.append("&EnrichVersion="); //$NON-NLS-1$
        if (!StringUtils.isEmpty(System.getProperty(JVM_PARAM_ONLINE_HELP_VERSION))) {
            sb.append(System.getProperty(JVM_PARAM_ONLINE_HELP_VERSION));
        } else {
            sb.append(PRODUCT_BASE_VERSION);
        }
        return sb.toString();
    }

    public static void openLineHelp(String componentName) {
        String url = calOnLineHelpURL(componentName);
        Program.launch(url);
    }

    public static String getLanguage() {
        String language = CorePlugin.getDefault().getPluginPreferences().getString(ITalendCorePrefConstants.LANGUAGE_SELECTOR);
        if (StringUtils.isBlank(language)) {
            language = Locale.getDefault().getLanguage();
        }
        return Locale.FRENCH.getLanguage().equals(language) ? "fr" : "en"; //$NON-NLS-1$ //$NON-NLS-2$
    }

    public static void resetVersionData(String version) {
        INTERNAL_VERSION = version;
        IS_RELEASE_VERSION = null;
        PRODUCT_BASE_VERSION = null;
        initVersionData();
    }
}
