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
package org.talend.designer.abstractmap.ui.prefs;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.talend.designer.abstractmap.MapPlugin;
import org.talend.designer.abstractmap.ui.properties.LINK_STYLE;

/**
 *
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 * @deprecated moved to ComponentsPreferencePage
 */
public class MapPreferenceInitializer extends AbstractPreferenceInitializer {

    public MapPreferenceInitializer() {
        super();
    }

    @Override
    public void initializeDefaultPreferences() {
        IPreferenceStore prefs = getPluginPreferenceStore();
        prefs.putValue(MapPrefsConstants.LINK_STYLE, LINK_STYLE.AUTO.toString());

    }

    /**
     * DOC amaumont Comment method "getPluginPreferenceStore".
     *
     * @return
     */
    public static IPreferenceStore getPluginPreferenceStore() {
        IPreferenceStore prefs = MapPlugin.getDefault().getPreferenceStore();
        return prefs;
    }

}
