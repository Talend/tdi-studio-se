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
import org.talend.designer.core.model.process.jobsettings.JobSettingsConstants;
import org.talend.designer.core.model.utils.emf.talendfile.ParametersType;

/**
 * DOC aimingchen class global comment. Detailled comment
 */
public class ImplicitContextLoadProjectSettingPage extends AbstractJobSettingsPage {

    public ImplicitContextLoadProjectSettingPage() {
        super();
        this.noDefaultAndApplyButton();
    }

    protected void checkSettingExisted() {
        if (pro.getEmfProject().getImplicitContextSettings() == null) {
            // display one message box to tell the user the settings is get from old preference page.
            MessageDialog.openInformation(getShell(), getTitle(), Messages.getString("ImplicitContextLoad.LoadOldPreferences")); //$NON-NLS-1$
        }
    }

    protected Element checkAndCreateElement() {
        return ProjectSettingManager.createImplicitContextLoadElement(pro);
    }

    protected EComponentCategory getCategory() {
        return EComponentCategory.EXTRA;
    }

    protected String getTaskMessages() {
        return Messages.getString("ImplicitContextLoadProjectSettingPage.saveProjectSettings"); //$NON-NLS-1$
    }

    protected ParametersType getParametersType() {
        if (pro != null && pro.getEmfProject() != null && pro.getEmfProject().getImplicitContextSettings() != null) {
            return pro.getEmfProject().getImplicitContextSettings().getParameters();
        }
        return null;
    }

    protected String getPropertyTypeName() {
        return JobSettingsConstants.getExtraParameterName(EParameterName.PROPERTY_TYPE.getName()) + ':'
                + EParameterName.PROPERTY_TYPE.getName();
    }

    protected String getRepositoryPropertyName() {
        return JobSettingsConstants.getExtraParameterName(EParameterName.PROPERTY_TYPE.getName()) + ':'
                + EParameterName.REPOSITORY_PROPERTY_TYPE.getName();
    }

    protected EParameterName getParameterName() {
        return EParameterName.IMPLICITCONTEXT_USE_PROJECT_SETTINGS;
    }

    protected String getDisplayName() {
        return "Implicit Context Load";
    }

}
