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
package org.talend.repository.ui.actions;

import org.eclipse.jface.action.Action;
import org.talend.commons.ui.runtime.image.EImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.RepositoryManager;
import org.talend.core.repository.model.ProjectRepositoryNode;
import org.talend.repository.i18n.Messages;
import org.talend.repository.ui.views.IRepositoryView;

/**
 * Action used to refresh a repository view.<br/>
 *
 * $Id$
 *
 */
public class RefreshAction extends Action {

    private IRepositoryView view;

    public RefreshAction(IRepositoryView view) {
        super();
        this.view = view;

        this.setText(Messages.getString("RefreshAction.text")); //$NON-NLS-1$
        this.setToolTipText(Messages.getString("RefreshAction.toolTipText")); //$NON-NLS-1$
        this.setImageDescriptor(ImageProvider.getImageDesc(EImage.REFRESH_ICON));
        this.setActionDefinitionId("refresh"); //$NON-NLS-1$
    }

    @Override
    public void run() {
        ProjectRepositoryNode.refProjectBool = true;
        view.refreshView();
        ProjectRepositoryNode.refProjectBool = false;
        // qli modified to fix the bug 6659.
        RepositoryManager.syncRoutineAndJoblet(ERepositoryObjectType.ROUTINES);
        RepositoryManager.syncRoutineAndJoblet(ERepositoryObjectType.JOBLET);
        RepositoryManager.syncUserComponents();

    }
}
