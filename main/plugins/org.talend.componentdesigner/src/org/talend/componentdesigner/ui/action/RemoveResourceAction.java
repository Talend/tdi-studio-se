// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.componentdesigner.ui.action;

import org.talend.componentdesigner.i18n.internal.Messages;
import org.talend.componentdesigner.model.ILibEntry;
import org.talend.componentdesigner.ui.composite.ILibListViewer;

/**
 * DOC rli class global comment. Detailled comment
 */
public class RemoveResourceAction extends UseResourceAction {

    public RemoveResourceAction(ILibListViewer viewer) {
        super(Messages.getString("RemoveResourceAction.Remove"), viewer); //$NON-NLS-1$
    }

    protected int getActionType() {
        return REMOVE;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    public void run() {
        ILibEntry[] libEntries = this.getViewer().getSelectedEntries();
        this.getViewer().removeEntries(libEntries);
    }

}
