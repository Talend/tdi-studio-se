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

import static org.junit.Assert.assertEquals;

import java.util.Locale;

import org.junit.Test;
import org.talend.core.CorePlugin;
import org.talend.core.prefs.ITalendCorePrefConstants;

public class ComponentsHelpUtilTest {

    @Test
    public void testCalOnLineHelpURL() {
        String urlSnapshotFr = "https://talend-staging.fluidtopics.net/access/sources/content/topic?pageid=tjava&afs:lang=fr&EnrichVersion=7.3";
        String urlSnapshotEn = "https://talend-staging.fluidtopics.net/access/sources/content/topic?pageid=tjava&afs:lang=en&EnrichVersion=7.3";
        String urlReleaseFr = "https://help.talend.com/access/sources/content/topic?pageid=tjava&afs:lang=fr&EnrichVersion=7.2";
        String urlReleaseEn = "https://help.talend.com/access/sources/content/topic?pageid=tjava&afs:lang=en&EnrichVersion=7.2";
        String originLanguage = CorePlugin.getDefault().getPluginPreferences()
                .getString(ITalendCorePrefConstants.LANGUAGE_SELECTOR);

        ComponentsHelpUtil.resetVersionData("7.3.1.20200207_1942-SNAPSHOT");
        String componentName = "tjava";
        setLanguage(Locale.FRANCE.getLanguage());
        assertEquals(urlSnapshotFr, ComponentsHelpUtil.calOnLineHelpURL(componentName));
        setLanguage(Locale.US.getLanguage());
        assertEquals(urlSnapshotEn, ComponentsHelpUtil.calOnLineHelpURL(componentName));
        setLanguage(Locale.CHINA.getLanguage());
        assertEquals(urlSnapshotEn, ComponentsHelpUtil.calOnLineHelpURL(componentName));

        ComponentsHelpUtil.resetVersionData("7.3.1.20200115_1125-M6");
        setLanguage(Locale.FRANCE.getLanguage());
        assertEquals(urlSnapshotFr, ComponentsHelpUtil.calOnLineHelpURL(componentName));
        setLanguage(Locale.US.getLanguage());
        assertEquals(urlSnapshotEn, ComponentsHelpUtil.calOnLineHelpURL(componentName));
        setLanguage(Locale.CHINA.getLanguage());
        assertEquals(urlSnapshotEn, ComponentsHelpUtil.calOnLineHelpURL(componentName));

        ComponentsHelpUtil.resetVersionData("7.2.1.20190620_1446");
        setLanguage(Locale.FRANCE.getLanguage());
        assertEquals(urlReleaseFr, ComponentsHelpUtil.calOnLineHelpURL(componentName));
        setLanguage(Locale.US.getLanguage());
        assertEquals(urlReleaseEn, ComponentsHelpUtil.calOnLineHelpURL(componentName));
        setLanguage(Locale.CHINA.getLanguage());
        assertEquals(urlReleaseEn, ComponentsHelpUtil.calOnLineHelpURL(componentName));

        ComponentsHelpUtil.resetVersionData("7.2.1.20191209_0253-patch");
        setLanguage(Locale.FRANCE.getLanguage());
        assertEquals(urlReleaseFr, ComponentsHelpUtil.calOnLineHelpURL(componentName));
        setLanguage(Locale.US.getLanguage());
        assertEquals(urlReleaseEn, ComponentsHelpUtil.calOnLineHelpURL(componentName));
        setLanguage(Locale.CHINA.getLanguage());
        assertEquals(urlReleaseEn, ComponentsHelpUtil.calOnLineHelpURL(componentName));

        setLanguage(originLanguage);
        ComponentsHelpUtil.resetVersionData(null);
    }

    private void setLanguage(String language) {
        CorePlugin.getDefault().getPluginPreferences().setValue(ITalendCorePrefConstants.LANGUAGE_SELECTOR, language);
    }

}
