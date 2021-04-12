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
package org.talend.designer.codegen;

import java.io.File;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.talend.commons.exception.BusinessException;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.ui.services.IComponentsLocalProviderService;
import org.talend.designer.codegen.components.model.ComponentFileChecker;
import org.talend.designer.codegen.components.ui.ComponenttRunJobPreferencePage;
import org.talend.designer.codegen.components.ui.IComponentPreferenceConstant;
import org.talend.designer.codegen.i18n.Messages;

/**
 * DOC Administrator class global comment. Detailled comment <br/>
 *
 * @author ftang, 17/08, 2007
 *
 */
public class ComponentsLocalProviderService implements IComponentsLocalProviderService {

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.components.IComponentsLocalProviderService#isAvoidToShowJobAfterDoubleClick()
     */
    public boolean isAvoidToShowJobAfterDoubleClick() {
        return CodeGeneratorActivator.getDefault().getPreferenceStore().getBoolean(ComponenttRunJobPreferencePage.IS_AVOID);
    }

    public boolean isAvoidToShowJobletAfterDoubleClick() {
        return CodeGeneratorActivator.getDefault().getPreferenceStore().getBoolean(IComponentPreferenceConstant.IS_AVOID_JOBLET);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.components.IComponentsLocalProviderService#getPreferenceStore()
     */
    public IPreferenceStore getPreferenceStore() {
        return CodeGeneratorActivator.getDefault().getPreferenceStore();
    }

    public void setPreferenceStoreValue(String key, Object value) {
        if (key != null) {
            if (value != null) {
                if (value instanceof String) {
                    getPreferenceStore().setValue(key, (String) value);
                } else if (value instanceof Boolean) {
                    getPreferenceStore().setValue(key, (Boolean) value);
                } else if (value instanceof Integer) {
                    getPreferenceStore().setValue(key, (Integer) value);
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.components.IComponentsLocalProviderService#getPlugin()
     */
    public AbstractUIPlugin getPlugin() {
        return CodeGeneratorActivator.getDefault();
    }

    public boolean validateComponent(String componentFolder, ECodeLanguage language) {
        if (componentFolder != null && language != null) {
            File folder = new File(componentFolder);
            if (folder.exists() && folder.isDirectory()) {
                try {
                    ComponentFileChecker.checkComponentFolder(folder, language.getName().toLowerCase());
                    return true; // It's ok
                } catch (BusinessException e) {
                    final BusinessException tempE = e;
                    Display.getDefault().syncExec(new Runnable() {

                        public void run() {
                            Status status = new Status(IStatus.ERROR, CodeGeneratorActivator.PLUGIN_ID, 1, tempE.getMessage(),
                                    tempE.getCause());
                            ErrorDialog dlg = new ErrorDialog(
                                    PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
                                    Messages.getString("ComponentsLocalProviderService.talendExchangeError"), Messages.getString("ComponentsLocalProviderService.componentLoadError"), status, IStatus.ERROR); //$NON-NLS-1$ //$NON-NLS-2$
                            dlg.open();
                        }

                    });

                }
            }
        }
        return false;
    }
}
