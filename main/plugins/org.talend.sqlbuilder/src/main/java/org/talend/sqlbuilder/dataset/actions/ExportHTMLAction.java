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
 * Copy an entire datasettable to the clipboard.
 *
 * @author Davy Vanherbergen
 */
public class ExportHTMLAction extends AbstractDataSetTableContextAction {

    private static final ImageDescriptor IMAGE = ImageUtil.getDescriptor("Images.ExportIcon"); //$NON-NLS-1$


    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.action.IAction#getText()
     */
    public String getText() {
        return Messages.getString("DataSetTable.Actions.Export.HTML"); //$NON-NLS-1$
    }


    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.action.IAction#getImageDescriptor()
     */
    public ImageDescriptor getImageDescriptor() {
        return IMAGE;
    }


    /**
     * Copy all table data to clipboard.
     * @see org.eclipse.jface.action.IAction#run()
     */
    public void run() {

        FileDialog fileDialog = new FileDialog(ptable.getShell(), SWT.SAVE);
        String[] filterExtensions = new String[] {"*.htm", "*.html"}; //$NON-NLS-1$ //$NON-NLS-2$
        fileDialog.setFilterExtensions(filterExtensions);

        final String fileName = fileDialog.open();
        if (fileName == null || fileName.trim().length() == 0) {
            return;
        }

        BusyIndicator.showWhile(Display.getCurrent(), new Runnable() {

            public void run() {

                try {

                    File file = new File(fileName);

                    if (file.exists()) {
                        // overwrite existing files
                        file.delete();
                    }

                    file.createNewFile();
                    BufferedWriter writer = new BufferedWriter(new FileWriter(file));
                    StringBuffer buffer = new StringBuffer(""); //$NON-NLS-1$

                    // get preferences
                    boolean includeColumnNames = SqlBuilderPlugin.getDefault().getPreferenceStore().getBoolean(
                            IConstants.CLIP_EXPORT_COLUMNS);

                    TableItem[] items = ptable.getItems();
                    DataSet dataSet = (DataSet) ptable.getData();

                    if (items == null || dataSet == null) {
                        return;
                    }

                    writer.write("<html>"); //$NON-NLS-1$
                    writer.newLine();

                    writer.write("<style>"); //$NON-NLS-1$
                    writer.write("TABLE {border-collapse: collapse;}"); //$NON-NLS-1$
                    writer.write("TH {background-color: rgb(240, 244, 245);}"); //$NON-NLS-1$
                    writer.write("TH, TD {border: 1px solid #D1D6D4;font-size: 10px;font-family: Verdana, Arial, Helvetica, sans-serif;}"); //$NON-NLS-1$
                    writer.write(".right {text-align: right;}"); //$NON-NLS-1$
                    writer.write("</style>"); //$NON-NLS-1$
                    writer.write("</head>"); //$NON-NLS-1$
                    writer.write("<table>"); //$NON-NLS-1$
                    writer.newLine();

                    // export column names
                    if (includeColumnNames) {

                        buffer.append("<tr>"); //$NON-NLS-1$
                        String[] columnNames = dataSet.getColumnLabels();
                        for (int i = 0; i < columnNames.length; i++) {
                            buffer.append("<th>"); //$NON-NLS-1$
                            buffer.append(columnNames[i]);
                            buffer.append("</th>"); //$NON-NLS-1$
                        }
                        buffer.append("</tr>"); //$NON-NLS-1$
                        writer.write(buffer.toString());
                        writer.newLine();
                    }

                    DataSet set = (DataSet) ptable.getData();

                    // export column data
                    int columnCount = ptable.getColumnCount();
                    for (int i = 0; i < items.length; i++) {

                        buffer = new StringBuffer("<tr>"); //$NON-NLS-1$

                        for (int j = 0; j < columnCount; j++) {

                            if (set.getColumnTypes()[j] == DataSet.TYPE_DOUBLE
                                    || set.getColumnTypes()[j] == DataSet.TYPE_INTEGER) {
                                // right align numbers
                                buffer.append("<td class=\"right\">");     //$NON-NLS-1$
                            } else {
                                buffer.append("<td>"); //$NON-NLS-1$
                            }

                            buffer.append(items[i].getText(j));
                            buffer.append("</td>"); //$NON-NLS-1$
                        }

                        buffer.append("</tr>"); //$NON-NLS-1$

                        writer.write(buffer.toString());
                        writer.newLine();
                    }

                    writer.write("</table>"); //$NON-NLS-1$
                    writer.newLine();
                    writer.write("</html>"); //$NON-NLS-1$
                    writer.newLine();

                    writer.close();


                } catch (final Exception e) {
                    ptable.getShell().getDisplay().asyncExec(new Runnable() {

                        public void run() {
                            MessageDialog.openError(ptable.getShell(), Messages.getString("SQLResultsView.Error.Export.Title"), e //$NON-NLS-1$
                                    .getMessage());
                            SqlBuilderPlugin.log(Messages.getString("SQLResultsView.Error.Export.Title"), e); //$NON-NLS-1$
                        }
                    });
                }
            }
        });

    }

}
