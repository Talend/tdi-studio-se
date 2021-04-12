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
package org.talend.sqlbuilder.actions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.talend.sqlbuilder.IConstants;
import org.talend.sqlbuilder.Messages;
import org.talend.sqlbuilder.SqlBuilderPlugin;
import org.talend.sqlbuilder.util.ImageUtil;

/**
 * This class is responsible for opening an existing file which in current OS. <br/>
 *
 * @author ftang
 *
 */
public class OpenFileAction extends AbstractEditorAction {

    private ImageDescriptor img = ImageUtil.getDescriptor("Images.OpenFileIcon"); //$NON-NLS-1$

    /*
     * (non-Javadoc)
     *
     * @see org.talend.sqlbuilder.actions.AbstractEditorAction#getText()
     */
    public String getText() {
        return Messages.getString("Open_1"); //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.action.Action#isEnabled()
     */
    public boolean isEnabled() {
        return true;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.sqlbuilder.actions.AbstractEditorAction#run()
     */
    public void run() {

        FileDialog dlg = new FileDialog(editor.getShell(), SWT.OPEN | SWT.MULTI);

        dlg.setFilterExtensions(new String[] { "*.sql;*.txt;*.*" }); //$NON-NLS-1$

        String path = dlg.open();
        if (path != null) {
            String[] files = dlg.getFileNames();
            loadFiles(files, dlg.getFilterPath());
        }

    }

    /**
     * Load one or more files into the editor.
     *
     * @param files string[] of relative file paths
     * @param filePath path where all files are found
     */
    public void loadFiles(String[] files, String filePath) {

        BufferedReader reader = null;

        try {

            StringBuffer all = new StringBuffer();
            String str = null;
            Preferences prefs = SqlBuilderPlugin.getDefault().getPluginPreferences();

            char delimiter = prefs.getString(IConstants.LINE_DELIMITER).charAt(0);

            for (int i = 0; i < files.length; i++) {

                String path = ""; //$NON-NLS-1$
                if (filePath != null) {
                    path += filePath + File.separator;
                }
                path += files[i];

                reader = new BufferedReader(new FileReader(path));

                while ((str = reader.readLine()) != null) {
                    all.append(str);
                    all.append(delimiter);
                }

                if (files.length > 1) {
                    all.append(delimiter);
                }
            }

            editor.setEditorContent(all.toString());

        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("OpenFileAction.logTextErrorLoadingDoc"), e); //$NON-NLS-1$

        } finally {
            try {
                reader.close();
            } catch (java.io.IOException e) {
                // noop
            }
        }

    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.sqlbuilder.actions.AbstractEditorAction#getToolTipText()
     */
    public String getToolTipText() {
        return Messages.getString("Open_2"); //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.action.Action#getHoverImageDescriptor()
     */
    public ImageDescriptor getHoverImageDescriptor() {
        return img;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.action.Action#getImageDescriptor()
     */
    public ImageDescriptor getImageDescriptor() {
        return img;
    };
}
