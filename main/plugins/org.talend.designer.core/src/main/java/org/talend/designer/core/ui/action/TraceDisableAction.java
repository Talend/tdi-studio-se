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

/**
 * ggu class global comment. Detailled comment
 */
public class TraceDisableAction extends AbstractTraceAction {

    public TraceDisableAction(IWorkbenchPart part) {
        super(part, Messages.getString("TraceDisableAction.TraceDesableTitle"), //$NON-NLS-1$
                Messages.getString("TraceDisableAction.TraceDisableDesc"),//$NON-NLS-1$
                Messages.getString("TraceDisableAction.TraceDisableDesc"));//$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.ui.action.AbstractTraceAction#isEnableAction()
     */
    @Override
    protected boolean isEnableAction() {
        return false;
    }

}
