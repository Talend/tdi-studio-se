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

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.eclipse.ui.navigator.ICommonMenuConstants;
import org.eclipse.ui.navigator.ICommonViewerWorkbenchSite;
import org.talend.componentdesigner.ComponentDesigenerPlugin;
import org.talend.componentdesigner.i18n.internal.Messages;
import org.talend.componentdesigner.ui.wizard.creatcomponent.CreateComponentWizard;

/**
 * Provides the new (artifact creation) menu options for a context menu.
 * 
 * 
 */
public class NewActionProvider extends CommonActionProvider {

    private static final String NEW_MENU_NAME = "common.new.menu"; //$NON-NLS-1$

    private IAction newProjectAction;

    public void init(ICommonActionExtensionSite anExtensionSite) {

        if (anExtensionSite.getViewSite() instanceof ICommonViewerWorkbenchSite) {
            newProjectAction = new NewComponentAction();
        }
    }

    /**
     * Adds a submenu to the given menu with the name "New Component".
     */
    public void fillContextMenu(IMenuManager menu) {
        for (IContributionItem item : menu.getItems()) {
            if (item == null || item.getId() == null) {
                continue;
            }
            if (item.getId().equals("export") || item.getId().equals("import")) { //$NON-NLS-1$ //$NON-NLS-2$
                menu.remove(item);
            }
        }

        // append the submenu after the GROUP_NEW group.
        if (ComponentDesigenerPlugin.getDefault().isUsed()) {
            IMenuManager submenu = new MenuManager(Messages.getString("NewActionProvider.New"), NEW_MENU_NAME); //$NON-NLS-1$
            submenu.add(newProjectAction);
            menu.insertAfter(ICommonMenuConstants.GROUP_NEW, submenu);
        }
    }

    /**
     * @author rli
     * 
     */
    class NewComponentAction extends Action {

        public NewComponentAction() {
            super(Messages.getString("NewActionProvider.NewComponent")); //$NON-NLS-1$
            ISharedImages images = PlatformUI.getWorkbench().getSharedImages();
            setImageDescriptor(images.getImageDescriptor(ISharedImages.IMG_TOOL_NEW_WIZARD));
        }

        /*
         * (non-Javadoc) Method declared on IAction.
         */
        public void run() {
            CreateComponentWizard wizard = new CreateComponentWizard();
            wizard.init(PlatformUI.getWorkbench(), null);
            final WizardDialog dialog = new WizardDialog(null, wizard);
            // dialog.setPageSize(520, 440); //Right size for windows XP
            dialog.setPageSize(520, 490);
            dialog.open();
        }
    }

}
