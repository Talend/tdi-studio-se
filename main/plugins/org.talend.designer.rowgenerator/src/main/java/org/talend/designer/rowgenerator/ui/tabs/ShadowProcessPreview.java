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
package org.talend.designer.rowgenerator.ui.tabs;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.talend.core.CorePlugin;
import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.core.prefs.ITalendCorePrefConstants;

/**
 * qzhang class global comment. Detailled comment <br/>
 *
 * $Id: ShadowProcessPreview.java,v 1.10 2007/01/31 05:20:52 pub Exp $
 *
 */
public class ShadowProcessPreview {

    protected static final int MAXIMUM_ROWS_TO_PREVIEW = CorePlugin.getDefault().getPreferenceStore().getInt(
            ITalendCorePrefConstants.PREVIEW_LIMIT);

    /**
     * Constante and main var.
     */

    private Table table;

    private Composite composite;

    /**
     * Create Object to manage Preview and MetaData.
     *
     * @param compositeFileViewer
     * @param filepath (null or path)
     */
    public ShadowProcessPreview(Composite composite, int width, int height) {
        this(composite);
    }

    /**
     * qzhang ShadowProcessPreview constructor comment.
     */
    public ShadowProcessPreview(Composite composite) {
        this.composite = composite;
    }

    /**
     * Create Table to show the content of run returns.
     *
     * @return
     *
     */
    public void newTablePreview() {
        table = new Table(composite, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        GridData gridData = new GridData(GridData.FILL_BOTH);
        table.setLayoutData(gridData);
    }

    /**
     * refresh Header of table Preview.
     *
     * @param columns
     */
    private void refreshPreviewHeader(final String[] columns) {
        int existingColumnsLength = table.getColumnCount();
        // update the existing columns
        for (int i = 0; i < existingColumnsLength; i++) {
            if (i < columns.length) {
                // the column exist and must be updated
                table.getColumn(i).setText(columns[i]);
            } else {
                // the following column exist and must be dispose
                i = existingColumnsLength;
            }
        }

        // if necessary, add the another columns must be created
        for (int i = existingColumnsLength; i < columns.length; i++) {
            if (i == 0) {
                new TableColumn(table, SWT.LEFT);

            } else {
                TableColumn column = new TableColumn(table, SWT.LEFT);
                column.setText(columns[i]);
            }
        }

        // if necessary, dispose the unusable columns
        while (table.getColumnCount() > columns.length) {
            table.getColumn(table.getColumnCount() - 1).dispose();
        }
    }

    /**
     * ocarbone Comment method "clearTablePreview".
     */
    private void clearTablePreview() {
        table.clearAll();
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumn(i).setText(""); //$NON-NLS-1$
            table.getColumn(i).setWidth(0);
        }
    }

    private void refreshTablePreview(final String[] columns, List<String[]> items) {
        refreshPreviewHeader(columns);
        refreshPreviewItem(items);
        // resize all the columns but not the table
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumn(i).pack();
        }
        // scroll to show the first col and first row
        if (table.getItems().length > 0) {
            table.showItem(table.getItem(0));
            if (table.getColumns() != null && table.getColumns().length > 0) {
                table.showColumn(table.getColumn(0));
            }
        }

    }

    /**
     * qzhang Comment method "refreshPreviewItem".
     *
     * @param xmlRows
     */
    private void refreshPreviewItem(List<String[]> xmlRows) {

        int existingItemCount = table.getItemCount();
        int end = xmlRows.size();
        for (int f = 0; f < end; f++) {
            if (f >= existingItemCount) {
                // create a new Item
                TableItem row = new TableItem(table, SWT.NONE);
                row.setText(xmlRows.get(f));
            } else {
                // update an existing Item
                table.getItem(f).setText(xmlRows.get(f));
            }
        }
        // remove the unused TableItem
        if (end < existingItemCount) {
            table.remove(end, existingItemCount - 1);
        }

    }

    /**
     * qzhang Comment method "refreshTablePreview".
     *
     * @param columns
     * @param items
     * @param isRefreshItems
     */
    public void refreshTablePreview(List<IMetadataColumn> columns, List<List<String>> items, boolean isRefreshItems) {
        if (isRefreshItems) {
            clearTablePreview();
        }
        if (items == null) {
            return;
        }
        String[] cols = new String[columns.size() + 1];
        for (int i = 0; i < cols.length; i++) {

            if (i == 0) {
                cols[i] = ""; //$NON-NLS-1$
            } else {
                cols[i] = columns.get(i - 1).getLabel();
            }
        }
        List<String[]> newItems = new ArrayList<String[]>();
        if (items != null && isRefreshItems) {
            for (List<String> strs : items) {
                String[] its = new String[strs.size()];
                for (int i = 0; i < its.length; i++) {
                    its[i] = strs.get(i);
                }
                newItems.add(its);
            }
        }

        refreshTablePreview(cols, newItems);
    }

    public Table getTable() {
        return this.table;
    }
}
