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
package org.talend.sbi.engines.client.ui.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.talend.sbi.engines.client.Activator;

/**
 * Preference Initializer for the designer.
 *
 * $Id: PreferenceInitializer.java 5392 2007-08-29 06:24:20Z yzhang $
 *
 */
public class SpagoPreferenceInitializer extends AbstractPreferenceInitializer {

    public static final String SPAGO_STATUS = "SpagoBiPreferencePage.spagoBiCheckButton"; //$NON-NLS-1$

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
     */
    @Override
    public void initializeDefaultPreferences() {
        IPreferenceStore store = Activator.getDefault().getPreferenceStore();
        store.setDefault(SPAGO_STATUS, false);
    }

}
