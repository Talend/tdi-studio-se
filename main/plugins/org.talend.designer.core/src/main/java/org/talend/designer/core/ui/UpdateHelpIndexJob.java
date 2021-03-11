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
package org.talend.designer.core.ui;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.help.internal.base.BaseHelpSystem;
import org.eclipse.help.internal.search.LocalSearchManager;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.ui.editor.TalendPaletteSearchIndex;

/**
 * DOC cmeng class global comment. Detailled comment
 */
public class UpdateHelpIndexJob extends Job {

    public UpdateHelpIndexJob(String name) {
        super(name);
    }

    public UpdateHelpIndexJob() {
        this(Messages.getString("UpdateHelpIndexJob.title")); //$NON-NLS-1$
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {
        LocalSearchManager localSearchManager = BaseHelpSystem.getInstance().getLocalSearchManager();
        IStatus status = Status.OK_STATUS;
        try {
            TalendPaletteSearchIndex talendPaletteSearchIndex = TalendPaletteSearchIndex.getInstance();
            if (talendPaletteSearchIndex != null) {
                localSearchManager.ensureIndexUpdated(monitor, talendPaletteSearchIndex);
            } else {
                status = Status.CANCEL_STATUS;
            }
        } catch (Throwable e) {
            status = Status.CANCEL_STATUS;
        }
        return status;
    }

}
