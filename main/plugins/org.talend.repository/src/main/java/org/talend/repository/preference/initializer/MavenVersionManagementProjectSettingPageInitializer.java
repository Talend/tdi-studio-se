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
import org.eclipse.jface.preference.IPreferenceStore;
import org.talend.core.runtime.maven.MavenConstants;
import org.talend.designer.maven.DesignerMavenPlugin;
import org.talend.designer.maven.utils.PomUtil;

public class MavenVersionManagementProjectSettingPageInitializer extends AbstractPreferenceInitializer {

    @Override
    public void initializeDefaultPreferences() {
        // IPreferenceStore preferenceStore =
        // DesignerMavenPlugin.getPlugin().getProjectPreferenceManager().getPreferenceStore();
        // preferenceStore.setDefault(MavenConstants.PROJECT_VERSION, PomUtil.getDefaultMavenVersion());
        // preferenceStore.setDefault(MavenConstants.NAME_PUBLISH_AS_SNAPSHOT, false);
    }

}
