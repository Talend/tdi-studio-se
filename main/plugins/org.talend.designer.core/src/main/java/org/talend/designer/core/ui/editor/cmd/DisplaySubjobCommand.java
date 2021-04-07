// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
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
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.ui.editor.subjobcontainer.SubjobContainer;

/**
 * Command that will set or remove the start status on a node. <br/>
 *
 * $Id: ChangeActivateStatusNodeCommand.java 3351 2007-05-04 12:14:00 +0000 (ven., 04 mai 2007) plegall $
 *
 */
public class DisplaySubjobCommand extends Command {

    private SubjobContainer subjob;

    private boolean value;

    /**
     * Gives the node where the status will be set or removed.
     *
     * @param newStartNode
     */
    public DisplaySubjobCommand(SubjobContainer subjob) {
        this.subjob = subjob;
        if (subjob.isDisplayed()) {
            value = false;
            setLabel(Messages.getString("DisplaySubjobCommand.HideSubjob2")); //$NON-NLS-1$
        } else {
            value = true;
            setLabel(Messages.getString("DisplaySubjobCommand.DisplaySubjob2")); //$NON-NLS-1$
        }

    }

    public void execute() {
        subjob.setDisplayed(value);
    }

    public void undo() {
        subjob.setDisplayed(!value);
    }

    public void redo() {
        this.execute();
    }
}
