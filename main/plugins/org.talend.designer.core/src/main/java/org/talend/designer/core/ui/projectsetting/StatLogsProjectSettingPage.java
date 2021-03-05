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
package org.talend.designer.core.ui.projectsetting;

import org.eclipse.jface.dialogs.MessageDialog;
import org.talend.core.model.process.EComponentCategory;
import org.talend.core.model.process.Element;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.model.utils.emf.talendfile.ParametersType;

/**
 * DOC aimingchen class global comment. Detailled comment
 */
public class StatLogsProjectSettingPage extends AbstractJobSettingsPage {

    public StatLogsProjectSettingPage() {
        super();
        this.noDefaultAndApplyButton();
    }

    protected void checkSettingExisted() {
        if (pro.getEmfProject().getStatAndLogsSettings() == null) {
            // display one message box to tell the user the settings is get from old preference page.
            MessageDialog.openInformation(getShell(), getTitle(), Messages.getString("StatLogs.LoadOldPreferences")); //$NON-NLS-1$
        }
    }

    protected Element checkAndCreateElement() {
        return ProjectSettingManager.createStatsAndLogsElement(pro);
    }

    protected EComponentCategory getCategory() {
        return EComponentCategory.STATSANDLOGS;
    }

    protected String getTaskMessages() {
        return Messages.getString("StatLogsProjectSettingPage.saveProjectSetting"); //$NON-NLS-1$
    }

    protected ParametersType getParametersType() {
        if (pro != null && pro.getEmfProject() != null && pro.getEmfProject().getStatAndLogsSettings() != null) {
            return pro.getEmfProject().getStatAndLogsSettings().getParameters();
        }
        return null;
    }

    protected String getPropertyTypeName() {
        return EParameterName.PROPERTY_TYPE.getName() + ':' + EParameterName.PROPERTY_TYPE.getName();
    }

    protected String getRepositoryPropertyName() {
        return EParameterName.PROPERTY_TYPE.getName() + ':' + EParameterName.REPOSITORY_PROPERTY_TYPE.getName();
    }

    protected EParameterName getParameterName() {
        return EParameterName.STATANDLOG_USE_PROJECT_SETTINGS;
    }

    protected String getDisplayName() {
        return "Stats & Logs";
    }
}
