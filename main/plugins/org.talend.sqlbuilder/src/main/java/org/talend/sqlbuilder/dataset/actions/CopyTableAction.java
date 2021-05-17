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

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TableItem;
import org.talend.sqlbuilder.IConstants;
import org.talend.sqlbuilder.Messages;
import org.talend.sqlbuilder.SqlBuilderPlugin;
import org.talend.sqlbuilder.dataset.dataset.DataSet;
import org.talend.sqlbuilder.util.ImageUtil;

/**
 * Copy an entire datasettable to the clipboard.
 *
 * @author Davy Vanherbergen
 */
public class CopyTableAction extends AbstractDataSetTableContextAction {

    private static final ImageDescriptor IMAGE = ImageUtil.getDescriptor("Images.CopyIcon"); //$NON-NLS-1$

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.action.IAction#getText()
     */
    public String getText() {
        return Messages.getString("DataSetTable.Actions.CopyToClipboard"); //$NON-NLS-1$
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
     *
     * @see org.eclipse.jface.action.IAction#run()
     */
    public void run() {

        BusyIndicator.showWhile(Display.getCurrent(), new Runnable() {

            public void run() {

                try {

                    // create clipboard
                    Clipboard clipBoard = new Clipboard(Display.getCurrent());
                    TextTransfer textTransfer = TextTransfer.getInstance();
                    StringBuffer buffer = new StringBuffer(""); //$NON-NLS-1$

                    // get preferences
                    String lineSeparator = System.getProperty("line.separator"); //$NON-NLS-1$
                    String columnSeparator = SqlBuilderPlugin.getDefault().getPreferenceStore().getString(
                            IConstants.CLIP_EXPORT_SEPARATOR);
                    boolean includeColumnNames = SqlBuilderPlugin.getDefault().getPreferenceStore().getBoolean(
                            IConstants.CLIP_EXPORT_COLUMNS);

                    TableItem[] items = ptable.getItems();
                    DataSet dataSet = (DataSet) ptable.getData();

                    if (items == null || dataSet == null) {
                        return;
                    }

                    // export column names
                    if (includeColumnNames) {

                        String[] columnNames = dataSet.getColumnLabels();
                        for (int i = 0; i < columnNames.length; i++) {
                            buffer.append(columnNames[i]);
                            buffer.append(columnSeparator);
                        }
                        buffer.append(lineSeparator);
                    }

                    // export column data
                    int columnCount = ptable.getColumnCount();
                    for (int i = 0; i < items.length; i++) {

                        for (int j = 0; j < columnCount; j++) {
                            buffer.append(items[i].getText(j));
                            buffer.append(columnSeparator);
                        }
                        buffer.append(lineSeparator);
                    }

                    // put all on clipboard
                    clipBoard.setContents(new Object[] { buffer.toString() }, new Transfer[] { textTransfer });

                } catch (Exception e) {
                    SqlBuilderPlugin.log(Messages.getString("CopyTableAction.logMessage"), e); //$NON-NLS-1$
                }
            }
        });

    }

}
