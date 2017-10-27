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
package org.talend.componentdesigner.ui.dialog;

import java.io.IOException;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.swt.widgets.Shell;
import org.talend.componentdesigner.PluginConstant;
import org.talend.componentdesigner.manager.ComponentFolderManager;

/**
 * @author rli
 * 
 */
public class CopyComponentDialog extends InputDialog {

    IPath path;

    public CopyComponentDialog(Shell parentShell, String dialogTitle, String dialogMessage, IPath initialValue,
            IInputValidator validator) {
        super(parentShell, dialogTitle, dialogMessage, initialValue.lastSegment(), validator);
        path = initialValue;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.dialogs.Dialog#okPressed()
     */
    protected void okPressed() {
        super.okPressed();
        try {
            new ComponentFolderManager().copyComponent(ResourcesPlugin.getWorkspace().getRoot().getProject(
                    PluginConstant.COMPONENT_PROJECT), path, this.getValue());
        } catch (CoreException e) {
            // e.printStackTrace();
            org.talend.componentdesigner.exception.ExceptionHandler.process(e);
        } catch (IOException e) {
            // e.printStackTrace();
            org.talend.componentdesigner.exception.ExceptionHandler.process(e);
        }
    }

}
