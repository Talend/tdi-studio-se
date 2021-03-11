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
package org.talend.sqlbuilder.dataset.actions;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import net.sourceforge.sqlexplorer.IConstants;
import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.dataset.DataSet;
import net.sourceforge.sqlexplorer.util.ImageUtil;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.TableItem;
import org.talend.sqlbuilder.SqlBuilderPlugin;

/**
 * Export table contents to a CSV file.
 * @author Davy Vanherbergen
 */
public class ExportCSVAction extends AbstractDataSetTableContextAction {

    private static final ImageDescriptor IMAGE = ImageUtil.getDescriptor("Images.ExportIcon"); //$NON-NLS-1$


    /**
     * Return the text that will be displayed in the context popup menu for this action.
     */
    public String getText() {
        return Messages.getString("DataSetTable.Actions.Export.CSV"); //$NON-NLS-1$
    }

    /**
     * Provide image for action.
     */
    public ImageDescriptor getImageDescriptor() {
        return IMAGE;
    }

    /**
     * Main method. Prompt for file name and save table contents to csv file.
     */
    public void run() {

        // get filename
        FileDialog fileDialog = new FileDialog(ptable.getShell(), SWT.SAVE);
        String[] filterExtensions = new String[] {"*.csv"}; //$NON-NLS-1$
        fileDialog.setFilterExtensions(filterExtensions);

        final String fileName = fileDialog.open();
        if (fileName == null || fileName.trim().length() == 0) {
            return;
        }

        // let's show the fancy wait cursor..
        BusyIndicator.showWhile(Display.getCurrent(), new Runnable() {

            public void run() {

                try {

                    // create new file
                    File file = new File(fileName);

                    if (file.exists()) {
                        // overwrite existing files
                        file.delete();
                    }

                    file.createNewFile();
                    BufferedWriter writer = new BufferedWriter(new FileWriter(file));
                    StringBuffer buffer = new StringBuffer(""); //$NON-NLS-1$

                    // get column header and separator preferences
                    String columnSeparator = SqlBuilderPlugin.getDefault().getPreferenceStore().getString(IConstants.CLIP_EXPORT_SEPARATOR);
                    boolean includeColumnNames = SqlBuilderPlugin.getDefault().getPreferenceStore().getBoolean(
                            IConstants.CLIP_EXPORT_COLUMNS);

                    // check if there is somethign in our table
                    TableItem[] items = ptable.getItems();
                    DataSet dataSet = (DataSet) ptable.getData();

                    if (items == null || dataSet == null) {
                        return;
                    }

                    // export column names if we need to
                    if (includeColumnNames) {

                        String[] columnNames = dataSet.getColumnLabels();
                        for (int i = 0; i < columnNames.length; i++) {
                            buffer.append(columnNames[i]);
                            buffer.append(columnSeparator);
                        }
                        writer.write(buffer.toString(), 0, buffer.length());
                        writer.newLine();
                    }

                    // export column data
                    int columnCount = ptable.getColumnCount();
                    for (int i = 0; i < items.length; i++) {

                        buffer = new StringBuffer(""); //$NON-NLS-1$

                        for (int j = 0; j < columnCount; j++) {
                            buffer.append(items[i].getText(j));
                            buffer.append(columnSeparator);
                        }
                        writer.write(buffer.toString(), 0, buffer.length());
                        writer.newLine();
                    }

                    writer.close();


                } catch (final Exception e) {
                    ptable.getShell().getDisplay().asyncExec(new Runnable() {

                        public void run() {
                            MessageDialog.openError(ptable.getShell(), Messages.getString("SQLResultsView.Error.Export.Title"), e //$NON-NLS-1$
                                    .getMessage());
                        }
                    });
                }
            }
        });

    }

}
