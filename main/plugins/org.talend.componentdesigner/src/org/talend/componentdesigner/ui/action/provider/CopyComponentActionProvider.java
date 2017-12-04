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
package org.talend.componentdesigner.ui.action.provider;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.eclipse.ui.navigator.ICommonViewerWorkbenchSite;
import org.talend.componentdesigner.ImageLib;
import org.talend.componentdesigner.i18n.internal.Messages;
import org.talend.componentdesigner.ui.dialog.CopyComponentDialog;
import org.talend.componentdesigner.ui.dialog.CopyComponentValidator;

/**
 * @author rli
 * 
 */
public class CopyComponentActionProvider extends CommonActionProvider {

    private IAction copyProjectAction;

    private IPath selectedProjectRelativePath;

    public void init(ICommonActionExtensionSite anExtensionSite) {

        if (anExtensionSite.getViewSite() instanceof ICommonViewerWorkbenchSite) {
            copyProjectAction = new CopyComponentAction();
        }
    }

    /**
     * Adds a submenu to the given menu with the name "New Component".
     */
    public void fillContextMenu(IMenuManager menu) {
        menu.insertBefore("group.edit", copyProjectAction); //$NON-NLS-1$
        Object obj = ((TreeSelection) this.getContext().getSelection()).getFirstElement();
        if (obj instanceof IFolder) {
            selectedProjectRelativePath = ((IFolder) obj).getProjectRelativePath();
        }
    }

    /**
     * @author rli
     * 
     */
    class CopyComponentAction extends Action {

        public CopyComponentAction() {
            super(Messages.getString("CopyComponentActionProvider.CopyComponent")); //$NON-NLS-1$
            setImageDescriptor(ImageLib.getImageDescriptor(ImageLib.COPYCOMPONENT_ACTION));
        }

        /*
         * (non-Javadoc) Method declared on IAction.
         */
        public void run() {
            CopyComponentValidator validator = new CopyComponentValidator();
            CopyComponentDialog dialog = new CopyComponentDialog(
                    PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
                    Messages.getString("CopyComponentActionProvider.CopyComponent2"), (Messages.getString("CopyComponentActionProvider.InputName") + selectedProjectRelativePath.lastSegment()) //$NON-NLS-1$ //$NON-NLS-2$
                    , selectedProjectRelativePath, validator);
            dialog.open();
        }
    }

}
