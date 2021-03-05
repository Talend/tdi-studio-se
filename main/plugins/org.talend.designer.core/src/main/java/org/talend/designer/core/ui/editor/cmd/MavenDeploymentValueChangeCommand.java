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

import static org.talend.repository.utils.MavenVersionUtils.*;
import org.eclipse.gef.commands.Command;

public class MavenDeploymentValueChangeCommand extends Command {

    private Object object;

    private String type;

    private String newValue;

    public MavenDeploymentValueChangeCommand(Object object, String type, String value) {
        this.object = object;
        this.type = type;
        newValue = value;
    }

    @Override
    public void execute() {
        if (object != null) {
            if (newValue == null || newValue.trim().equals("")) { //$NON-NLS-1$
                // if empty, remove key
                if (containsKey(object, type)) {
                    remove(object, type);
                }
            } else {
                put(object, type, newValue);
            }
        }
    }

}
