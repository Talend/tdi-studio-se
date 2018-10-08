// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
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
import org.eclipse.jface.preference.IPreferenceStore;
import org.talend.core.runtime.projectsetting.ProjectPreferenceManager;
import org.talend.repository.preference.ArtifactRepositoryShareSettingPage;

public class ArtifactRepositoryShareSettingPageInitializer extends AbstractPreferenceInitializer{

    @Override
    public void initializeDefaultPreferences() {
        IPreferenceStore preferenceStore = new ProjectPreferenceManager(ArtifactRepositoryShareSettingPage.PREFERENCE_NAME)
                .getPreferenceStore();
        preferenceStore.setDefault(ArtifactRepositoryShareSettingPage.PREF_KEY_SHARE_ENABLE, true);
        preferenceStore.setDefault(ArtifactRepositoryShareSettingPage.PREF_KEY_SHARE_REPOSITORY_ID,
                ArtifactRepositoryShareSettingPage.DEFAULT_REPOSITORY_ID);
        preferenceStore.setDefault(ArtifactRepositoryShareSettingPage.PREF_KEY_CHECK_UPDATE_PER_DAYS, 2);
        preferenceStore.setDefault(ArtifactRepositoryShareSettingPage.PREF_KEY_AUTO_CHECK_UPDATE, true);
        preferenceStore.setDefault(ArtifactRepositoryShareSettingPage.PREF_KEY_SHOW_WARN_DIALOG_WHEN_INSTALLING_FEATURES, true);

    }

}
