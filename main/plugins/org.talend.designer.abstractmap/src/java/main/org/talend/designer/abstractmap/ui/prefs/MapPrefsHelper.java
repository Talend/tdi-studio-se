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

import org.eclipse.jface.preference.IPreferenceStore;
import org.talend.designer.abstractmap.MapPlugin;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 */
public final class MapPrefsHelper {

    private static MapPrefsHelper instance;

    public static MapPrefsHelper getInstance() {
        if (instance == null) {
            instance = new MapPrefsHelper();
        }
        return instance;
    }

    /**
     * DOC amaumont RunRemoteProcessPrefsHelper constructor comment.
     */
    private MapPrefsHelper() {
        super();
    }

    public IPreferenceStore getPreferenceStore() {
        return MapPlugin.getDefault().getPreferenceStore();
    }

}
