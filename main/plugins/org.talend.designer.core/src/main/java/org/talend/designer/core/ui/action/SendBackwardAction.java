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
package org.talend.designer.core.ui.action;

import org.eclipse.ui.IWorkbenchPart;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.ui.editor.cmd.SendBackwardCommand;

/**
 */
public class SendBackwardAction extends ZorderAction {

    public static final String ID = "org.talend.designer.core.ui.editor.action.SendBackwardAction"; //$NON-NLS-1$

    private static final String TEXT = Messages.getString("SendBackwardAction.sendBackward"); //$NON-NLS-1$

    public SendBackwardAction(IWorkbenchPart part) {
        super(part);
        setId(ID);
        setText(TEXT);
    }

    @Override
    protected void createZOrderCommand() {
        zorderCommand = new SendBackwardCommand(editPart);
    }
}
