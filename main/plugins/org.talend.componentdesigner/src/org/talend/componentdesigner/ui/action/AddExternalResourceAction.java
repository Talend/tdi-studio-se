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
package org.talend.componentdesigner.ui.action;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.talend.componentdesigner.i18n.internal.Messages;
import org.talend.componentdesigner.model.ILibEntry;
import org.talend.componentdesigner.model.libentry.JarLibEntry;
import org.talend.componentdesigner.model.libentry.PmLibEntry;
import org.talend.componentdesigner.ui.composite.ILibListViewer;

/**
 * Adds an external jar to the runtime class path.
 */
public class AddExternalResourceAction extends OpenDialogAction {

    public AddExternalResourceAction(ILibListViewer viewer, String dialogSettingsPrefix) {
        super(Messages.getString("AddExternalResourceAction.AddELibs"), viewer, dialogSettingsPrefix); //$NON-NLS-1$
    }

    /**
     * Prompts for a project to add.
     * 
     * @see IAction#run()
     */
    @Override
    public void run() {

        // String lastUsedPath = getDialogSetting(LAST_PATH_SETTING);
        String lastUsedPath = null;
        if (lastUsedPath == null) {
            lastUsedPath = ""; //$NON-NLS-1$
        }
        FileDialog dialog = new FileDialog(getShell(), SWT.MULTI);
        dialog.setText(Messages.getString("AddExternalResourceAction.JarSelection")); //$NON-NLS-1$
        dialog.setFilterExtensions(new String[] { "*.*", "*.jar", "*.zip", "*.pm" }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        dialog.setFilterPath(lastUsedPath);
        String res = dialog.open();
        if (res == null) {
            return;
        }
        String[] fileNames = dialog.getFileNames();
        int nChosen = fileNames.length;

        IPath filterPath = new Path(dialog.getFilterPath());
        List<ILibEntry> list = new ArrayList<ILibEntry>();
        IPath path = null;
        for (int i = 0; i < nChosen; i++) {
            path = filterPath.append(fileNames[i]).makeAbsolute();
            if (path.toFile().exists()) {
                if (path.lastSegment().matches("(?i).*\\.(jar)\\b")) { //$NON-NLS-1$
                    list.add(new JarLibEntry(path));
                }
                if (path.lastSegment().matches("(?i).*\\.(pm)\\b")) { //$NON-NLS-1$
                    list.add(new PmLibEntry(path));
                }
            } else {
                MessageDialog.openError(getShell(), Messages.getString("AddExternalResourceAction.ErrorTitle"), //$NON-NLS-1$
                        Messages.getString("AddExternalResourceAction.ErrorMsg")); //$NON-NLS-1$
            }
        }
        if (list.size() > 0) {
            setDialogSetting(LAST_PATH_SETTING, filterPath.toOSString());
            getViewer().addEntries(list.toArray(new ILibEntry[list.size()]));
        }
    }

    @Override
    protected int getActionType() {
        return ADD;
    }
}
