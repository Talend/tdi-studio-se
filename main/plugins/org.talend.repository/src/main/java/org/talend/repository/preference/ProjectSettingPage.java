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
package org.talend.repository.preference;

import org.eclipse.jface.preference.PreferencePage;
import org.talend.core.model.general.Project;
import org.talend.repository.ProjectManager;

/**
 * DOC aimingchen class global comment. Detailled comment
 */
public abstract class ProjectSettingPage extends PreferencePage {

    /**
     * The Project Object.
     */
    protected Project pro = ProjectManager.getInstance().getCurrentProject();

    public abstract void refresh();
}
