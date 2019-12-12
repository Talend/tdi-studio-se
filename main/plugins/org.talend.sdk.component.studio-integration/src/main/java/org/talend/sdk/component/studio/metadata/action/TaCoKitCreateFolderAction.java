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
package org.talend.sdk.component.studio.metadata.action;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.repository.ui.actions.CreateFolderAction;
import org.talend.core.repository.ui.wizard.folder.FolderWizard;
import org.talend.sdk.component.server.front.model.ConfigTypeNode;
import org.talend.sdk.component.studio.util.TaCoKitUtil;


/**
 * DOC cmeng  class global comment. Detailled comment
 */
public class TaCoKitCreateFolderAction extends CreateFolderAction {

    private ConfigTypeNode configTypeNode;

    public TaCoKitCreateFolderAction(ConfigTypeNode configTypeNode) {
        this.configTypeNode = configTypeNode;
    }

    @Override
    protected void doRun() {
        IPath path = new Path(""); //$NON-NLS-1$

        if (configTypeNode != null) {
            ERepositoryObjectType objectType = null;
            try {
                objectType = TaCoKitUtil.getOrCreateERepositoryObjectType(configTypeNode);
            } catch (Exception e) {
                ExceptionHandler.process(e);
                return;
            }
            FolderWizard processWizard = new FolderWizard(path, objectType, null);
            Shell activeShell = Display.getCurrent().getActiveShell();
            WizardDialog dialog = new WizardDialog(activeShell, processWizard);
            dialog.setPageSize(400, 60);
            dialog.create();
            dialog.open();
        }
    }

    @Override
    public void init(TreeViewer viewer, IStructuredSelection selection) {
        super.init(viewer, selection);
        setEnabled(configTypeNode != null);
    }

    public ConfigTypeNode getConfigTypeNode() {
        return configTypeNode;
    }

    public void setConfigTypeNode(ConfigTypeNode configTypeNode) {
        this.configTypeNode = configTypeNode;
    }

}
