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
package org.talend.repository.plugin.integration;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Display;
import org.talend.core.CorePlugin;
import org.talend.repository.model.IRepositoryService;

/**
 * DOC bqian class global comment. Detailled comment <br/>
 *
 */
public class SwitchProjectAction extends Action {

    public static final boolean PLUGIN_MODEL = true;

    private static final String COMMAND_ID = "org.talend.repository.integration.bootTalend"; //$NON-NLS-1$

    private static final String CATEGORY_ID = "org.talend.repository.integration"; //$NON-NLS-1$

    private IPreferenceStore store;

    public SwitchProjectAction() {
        store = CorePlugin.getDefault().getPreferenceStore();
    }

    @Override
    public void run() {
        IRepositoryService repositoryService = CorePlugin.getDefault().getRepositoryService();
        repositoryService.openLoginDialog(Display.getDefault().getActiveShell());

    }
}
