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
package org.talend.designer.core.utils;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.general.ModuleNeeded;
import org.talend.designer.core.i18n.Messages;
import org.talend.librariesmanager.ui.LibManagerUiPlugin;
import org.talend.librariesmanager.ui.dialogs.ComponentExternalModulesDialog;
import org.talend.librariesmanager.ui.dialogs.ExternalModulesInstallDialog;

/**
 * DOC ggu class global comment. Detailled comment
 */
public class ModulesInstallerUtil {

    public static void installModules(Shell shell, IComponent component) {
        if (!LibManagerUiPlugin.getDefault().getPreferenceStore()
                .getBoolean(ExternalModulesInstallDialog.DO_NOT_SHOW_EXTERNALMODULESINSTALLDIALOG)) {
            forceInstallModules(shell, component, component.getModulesNeeded());
        }
    }

    public static void forceInstallModules(Shell shell, IComponent component, List<ModuleNeeded> modules) {
        String text = Messages.getString("ModulesInstaller_text1", component.getName());//$NON-NLS-1$
        String title = Messages.getString("ModulesInstaller_title1") + component.getName(); //$NON-NLS-1$
        String name = component.getName();
        if (name == null) {
            return;
        }
        boolean opened = false;
        if (!modules.isEmpty()) {
            // added by dlin fix bug https://jira.talendforge.org/browse/TDI-27679
            Shell[] shells = PlatformUI.getWorkbench().getDisplay().getShells();
            for (Shell shell2 : shells) {
                Object currentDialog = shell2.getData();
                if (currentDialog instanceof ComponentExternalModulesDialog) {
                    String componentName = ((ComponentExternalModulesDialog) currentDialog).getCoponentName();
                    if (componentName == null) {
                        return;
                    }
                    if (name.equalsIgnoreCase(componentName)) {
                        // one downloading dialog for one kind of component is enough only need to resopen instead of
                        // create a new one
                        opened = true;
                        // setMaximized is mandatory .or after you minmizing the dialog .only setSize cann't bring the
                        // dialog foreground .
                        shell2.setMaximized(true);
                        shell2.setSize(1050, 400);
                        break;
                    }
                }
            }
            if (!opened) {
                ComponentExternalModulesDialog dialog = new ComponentExternalModulesDialog(shell, text, title);
                dialog.showDialog(true, modules);
            }

        }
    }

    public static void installModules(Shell shell, List<IComponent> components) {
        if (!LibManagerUiPlugin.getDefault().getPreferenceStore()
                .getBoolean(ExternalModulesInstallDialog.DO_NOT_SHOW_EXTERNALMODULESINSTALLDIALOG)) {
            String text = Messages.getString("ModulesInstaller_text2"); //$NON-NLS-1$
            String title = Messages.getString("ModulesInstaller_title2"); //$NON-NLS-1$
            List<ModuleNeeded> needed = new ArrayList<ModuleNeeded>();
            for (IComponent component : components) {
                needed.addAll(component.getModulesNeeded());
            }
            if (!needed.isEmpty()) {
                ComponentExternalModulesDialog dialog = new ComponentExternalModulesDialog(shell, text, title);
                dialog.showDialog(true, needed);
            }
        }
    }

}
