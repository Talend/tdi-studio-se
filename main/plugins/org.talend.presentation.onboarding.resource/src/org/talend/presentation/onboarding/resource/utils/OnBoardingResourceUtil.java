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
package org.talend.presentation.onboarding.resource.utils;

import java.io.IOException;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPersistentPreferenceStore;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.talend.commons.exception.CommonExceptionHandler;

/**
 * created by cmeng on Sep 29, 2015 Detailled comment
 *
 */
public class OnBoardingResourceUtil {

    private static ScopedPreferenceStore preferenceStore;

    public static IPreferenceStore getPreferenceStore() {
        if (preferenceStore == null) {
            preferenceStore = new ScopedPreferenceStore(InstanceScope.INSTANCE, OnBoardingResourceConstants.PLUGIN_ID);
        }

        return preferenceStore;
    }

    public static void savePreferenceStore() {
        if (preferenceStore == null) {
            return;
        }
        try {
            ((IPersistentPreferenceStore) preferenceStore).save();
        } catch (IOException e) {
            CommonExceptionHandler.process(e);
        }
    }
}
