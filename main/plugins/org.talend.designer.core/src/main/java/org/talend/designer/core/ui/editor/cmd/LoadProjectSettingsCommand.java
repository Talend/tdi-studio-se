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
package org.talend.designer.core.ui.editor.cmd;

import org.eclipse.gef.commands.Command;
import org.talend.core.model.general.Project;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.ui.editor.process.Process;
import org.talend.designer.core.ui.projectsetting.ElementParameter2ParameterType;
import org.talend.designer.core.ui.projectsetting.ProjectSettingManager;
import org.talend.repository.ProjectManager;

/**
 * DOC talend class global comment. Detailled comment
 */
public class LoadProjectSettingsCommand extends Command {

    private Process process;

    private String paramName;

    private boolean isUseProjectSettings;

    private Project pro;

    public LoadProjectSettingsCommand(Process process, String paramName, boolean isUseProjectSettings) {
        this.process = process;
        this.paramName = paramName;
        this.isUseProjectSettings = isUseProjectSettings;
        pro = ProjectManager.getInstance().getCurrentProject();
    }

    @Override
    public void execute() {
        if (process == null || paramName == null) {
            return;
        }
        ElementParameter2ParameterType.setParameterValue(process, paramName, isUseProjectSettings);
        if (isUseProjectSettings) {
            if (EParameterName.IMPLICITCONTEXT_USE_PROJECT_SETTINGS.getName().equals(paramName)) {
                ProjectSettingManager.reloadImplicitValuesFromProjectSettings(process, pro, null);
            } else if (EParameterName.STATANDLOG_USE_PROJECT_SETTINGS.getName().equals(paramName)) {
                ProjectSettingManager.reloadStatsAndLogFromProjectSettings(process, pro, null);
            }
        }
    }

}
