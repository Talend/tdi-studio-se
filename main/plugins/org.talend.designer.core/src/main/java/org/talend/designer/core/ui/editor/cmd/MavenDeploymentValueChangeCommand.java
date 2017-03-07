// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
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

import org.apache.commons.lang3.StringUtils;
import org.eclipse.gef.commands.Command;
import org.talend.designer.core.ui.editor.process.Process;

public class MavenDeploymentValueChangeCommand extends Command {

    private Process process;

    private String type;

    private String newValue;

    public MavenDeploymentValueChangeCommand(Process process, String type, String value) {
        this.process = process;
        this.type = type;
        newValue = value;
    }

    @Override
    public void execute() {
        if (process != null) {
            if (StringUtils.isEmpty(newValue)) {// if empty, remove key
                process.getAdditionalProperties().remove(type);
            } else {
                process.getAdditionalProperties().put(type, newValue);
            }
        }
    }

}
