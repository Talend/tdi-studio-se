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

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.commands.Command;
import org.talend.designer.core.ui.editor.TalendEditor;
import org.talend.designer.core.ui.editor.process.Process;

/**
 */
public abstract class CreateCommand extends Command {

    protected Process process;

    protected Point location;

    public CreateCommand(String label, Process process, Point location) {
        super(label);
        this.process = process;
        this.location = location;

        if (process.isGridEnabled()) {
            // replace the component to set it on the grid if it's enabled
            int tempVar = location.x / TalendEditor.GRID_SIZE;
            this.location.x = tempVar * TalendEditor.GRID_SIZE;
            tempVar = location.y / TalendEditor.GRID_SIZE;
            this.location.y = tempVar * TalendEditor.GRID_SIZE;
        }
    }

}
