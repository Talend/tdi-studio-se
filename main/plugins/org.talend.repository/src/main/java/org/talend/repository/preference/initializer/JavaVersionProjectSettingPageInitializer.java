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
package org.talend.repository.preference.initializer;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.preference.IPreferenceStore;
import org.talend.commons.utils.generation.JavaUtils;
import org.talend.core.runtime.CoreRuntimePlugin;

public class JavaVersionProjectSettingPageInitializer extends AbstractPreferenceInitializer{

    @Override
    public void initializeDefaultPreferences() {
        IPreferenceStore preferenceStore = CoreRuntimePlugin.getInstance().getProjectPreferenceManager().getPreferenceStore();
        preferenceStore.setDefault(JavaUtils.PROJECT_JAVA_VERSION_KEY, JavaCore.VERSION_1_8);
    }

}
