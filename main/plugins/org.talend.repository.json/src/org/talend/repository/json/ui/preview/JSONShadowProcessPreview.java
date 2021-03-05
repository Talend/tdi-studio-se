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
package org.talend.repository.json.ui.preview;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.talend.core.model.utils.RepositoryManagerHelper;
import org.talend.core.utils.CsvArray;
import org.talend.metadata.managment.ui.preview.ProcessDescription;
import org.talend.repository.model.json.SchemaTarget;

/**
 * Create SWT Table to show the content of a file. <br/>
 *
 * $Id:JSONShadowProcessPreview.java 1185 2006-12-22 10:43:29 +0000 (閺勭喐婀℃禍锟�22 閸椾椒绨╅張锟�006) amaumont $
 *
 */
public class JSONShadowProcessPreview {

    protected int maximumRowsToPreview = RepositoryManagerHelper.getMaximumRowsToPreview();

    /**
     * Constante and main var.
     */
    private int filePreviewWidth = 350;

    private int filePreviewHeight = 180;

    private String filePath;

    private String[] header;

    protected Table table;

    private boolean csvRowsEmpty = false;

    public boolean isCsvRowsEmpty() {
        return csvRowsEmpty;
    }

    public Table getTable() {
        return this.table;
    }

    public void setTable(Table table) {
        this.table = table;
    }

    protected Composite composite;

    /**
     * Create Object to manage Preview and MetaData.
     *
     * @param compositeFileViewer
     * @param filepath (null or path)
     */
    public JSONShadowProcessPreview(Composite composite, String filePath, int width, int height) {
        this.filePath = filePath;
        this.composite = composite;
        this.filePreviewWidth = width;
        this.filePreviewHeight = height;
    }

    /**
     * Create Table to show the content of a file.
     *
     * @return
     *
     */
    public void newTablePreview() {
        table = new Table(composite, SWT.BORDER);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        // table.setSize(filePreviewWidth, filePreviewHeight);

        // force the dimension width a gridData
        GridData gridData = new GridData(GridData.FILL_BOTH);
        // gridData.minimumWidth = filePreviewWidth;
        // gridData.minimumHeight = filePreviewHeight;
        // gridData.heightHint = filePreviewHeight;
        // gridData.widthHint = filePreviewWidth;
        table.setLayoutData(gridData);
    }

    /**
     * refresh Header of table Preview.
     *
     * @param columns
     */
    public void refreshPreviewHeader(final String[] columns) {
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
            TableColumn column = new TableColumn(table, SWT.LEFT);
            column.setText(columns[i]);
        }

        // if necessary, dispose the unusable columns
        while (table.getColumnCount() > columns.length) {
            table.getColumn(table.getColumnCount() - 1).dispose();
        }
    }

    /**
     * refresh TablePreview width the first rows of the file.
     *
     * @param csvArray
     * @param firstRowIsLabel
     */
    public void refreshTablePreview(final CsvArray csvArray, final boolean firstRowIsLabel, int... selectedColumnIndex) {
        List<String[]> csvRows = csvArray.getRows();

        if (csvRows.isEmpty()) {
            return;
        }

        // init the title columns

        // XmlRow firstRow = xmlRows.get(0);

        String[] firstRowFields = csvRows.get(0);

        // List<XmlField> firstRowFields = firstRow.getFields();

        Integer numbersOfColumns = getRightFirstRow(csvRows);

        String[] title = new String[numbersOfColumns];
        for (int i = 0; i < numbersOfColumns; i++) {
            if (firstRowIsLabel) {
                if (numbersOfColumns <= firstRowFields.length) {
                    if (firstRowFields[i] != null && !("").equals(firstRowFields[i])) { //$NON-NLS-1$[
                        title[i] = firstRowFields[i];
                    } else {
                        title[i] = "Column" + " " + i;
                    }
                } else {
                    if (i < firstRowFields.length) {
                        title[i] = firstRowFields[i];
                    } else {
                        title[i] = "Column" + " " + i; //$NON-NLS-1$ //$NON-NLS-2$
                    }
                }
            } else {
                title[i] = "Column" + " " + i; //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
        // fix bug 5694: column names check in FileDelimited wizard fails to
        // rename duplicate column name
        fixDuplicateNames(title);
        this.header = title;

        // clear all the item
        table.clearAll();

        // refresh the Header and the Item of the table
        refreshPreviewHeader(title);
        refreshPreviewItem(csvRows, firstRowIsLabel, selectedColumnIndex);

        refreshPreviewHeader(title);

        // resize all the columns but not the table
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumn(i).pack();
        }
        // scroll to show the first col and first row

        // see the note"17172" of the feature "6296",qli comment.
        if (table.getItemCount() > 0) {
            table.showItem(table.getItem(0));
            if (table.getColumns() != null && table.getColumns().length > 0) {
                table.showColumn(table.getColumn(0));
            }
        }
    }

    public static void fixDuplicateNames(String[] titles) {
        // fix bug 5694: column names check in FileDelimited wizard fails to
        // rename duplicate column name
        Set<String> existNames = new HashSet<String>();
        for (int i = 0; i < titles.length; i++) {
            String name = titles[i];
            if (existNames.contains(name)) {
                // name conflict, guess a new name
                for (int j = 1;; j++) {
                    String newName = name + j;
                    if (!existNames.contains(newName)) {
                        titles[i] = newName;
                        break;
                    }
                }
            }
            existNames.add(titles[i]);

        }
    }

    /**
     * refresh TablePreview width the first rows of the file.
     *
     * @param csvArray
     * @param firstRowIsLabel
     */
    public void refreshTablePreview(final CsvArray csvArray, final boolean firstRowIsLabel, ProcessDescription processDescription) {
        List<String[]> xmlRows = csvArray.getRows();

        if (xmlRows.isEmpty()) {
            return;
        }
        // init the title columns

        String[] firstRowFields = xmlRows.get(0);

        // List<XmlField> firstRowFields = firstRow.getFields();

        Integer numbersOfColumns = getRightFirstRow(xmlRows);

        String[] title = new String[numbersOfColumns];
        for (int i = 0; i < numbersOfColumns; i++) {
            if (firstRowIsLabel) {
                if (numbersOfColumns <= firstRowFields.length) {
                    if (firstRowFields[i] != null && !("").equals(firstRowFields[i])) { //$NON-NLS-1$
                        title[i] = firstRowFields[i];
                    } else {
                        title[i] = "Column" + " " + i; //$NON-NLS-1$ //$NON-NLS-2$
                    }
                } else {
                    if (i < firstRowFields.length) {
                        title[i] = firstRowFields[i];
                    } else {
                        title[i] = "Column" + " " + i; //$NON-NLS-1$ //$NON-NLS-2$
                    }
                }
            } else {
                title[i] = "" + processDescription.getSchema().get(0).getListColumns().get(i); //$NON-NLS-1$
            }
        }
        this.header = title;

        // clear all the item
        table.clearAll();

        // refresh the Header and the Item of the table
        refreshPreviewHeader(title);
        refreshPreviewItem(xmlRows, firstRowIsLabel);

        refreshPreviewHeader(title);

        // resize all the columns but not the table
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumn(i).pack();
        }

        // scroll to show the first col and first row
        table.showItem(table.getItem(0));
        if (table.getColumns() != null && table.getColumns().length > 0) {
            table.showColumn(table.getColumn(0));
        }
    }

    /**
     * refresh TablePreview width the first rows of the file.
     *
     * @param csvArray
     * @param firstRowIsLabel
     */
    public void refreshTablePreview(final CsvArray csvArray, final boolean firstRowIsLabel,
            ProcessDescription processDescription, String[] titleColumns) {
        List<String[]> xmlRows = csvArray.getRows();

        if (xmlRows.isEmpty()) {
            return;
        }

        this.header = titleColumns;

        // clear all the item
        table.clearAll();

        // refresh the Header and the Item of the table
        refreshPreviewHeader(titleColumns);
        refreshPreviewItem(xmlRows, firstRowIsLabel);

        refreshPreviewHeader(titleColumns);

        // resize all the columns but not the table
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumn(i).pack();
        }

        // scroll to show the first col and first row
        table.showItem(table.getItem(0));
        if (table.getColumns() != null && table.getColumns().length > 0) {
            table.showColumn(table.getColumn(0));
        }
    }

    /**
     * refresh TablePreview width the first rows of the file.
     *
     * @param csvArray
     * @param firstRowIsLabel
     */
    public void refreshTablePreview(final CsvArray csvArray, final boolean firstRowIsLabel, List<SchemaTarget> schemaTarget) {
        List<String[]> csvRows = csvArray.getRows();
        this.csvRowsEmpty = false;
        if (csvRows.isEmpty()) {
            this.csvRowsEmpty = true;
            return;
        }
        // init the title columns

        String[] firstRowFields = csvRows.get(0);

        // List<XmlField> firstRowFields = firstRow.getFields();

        Integer numbersOfColumns = getRightFirstRow(csvRows);

        String[] title = new String[numbersOfColumns];
        for (int i = 0; i < numbersOfColumns; i++) {
            if (firstRowIsLabel) {
                if (numbersOfColumns <= firstRowFields.length) {
                    if (firstRowFields[i] != null && !("").equals(firstRowFields[i])) { //$NON-NLS-1$
                        title[i] = firstRowFields[i];
                    } else {
                        title[i] = "Column" + " " + i; //$NON-NLS-1$ //$NON-NLS-2$
                    }
                } else {
                    if (i < firstRowFields.length) {
                        title[i] = firstRowFields[i];
                    } else {
                        title[i] = "Column" + " " + i; //$NON-NLS-1$ //$NON-NLS-2$
                    }
                }
            } else {
                if (schemaTarget.get(i).getTagName() != null && !schemaTarget.get(i).getTagName().equals("")) { //$NON-NLS-1$
                    title[i] = "" + schemaTarget.get(i).getTagName(); //$NON-NLS-1$
                } else {
                    title[i] = "Column" + " " + i; //$NON-NLS-1$ //$NON-NLS-2$
                }
            }
        }
        this.header = title;

        // clear all the item
        table.removeAll();

        // refresh the Header and the Item of the table
        refreshPreviewHeader(title);
        refreshPreviewItem(csvRows, firstRowIsLabel);

        refreshPreviewHeader(title);

        // resize all the columns but not the table
        for (int i = table.getColumnCount() - 1; i >= 0; i--) {
            table.getColumn(i).pack();
        }

        // scroll to show the first col and first row
        table.showItem(table.getItem(0));
        if (table.getColumns() != null && table.getColumns().length > 0) {
            table.showColumn(table.getColumn(0));
        }
    }

    // CALCULATE THE NULBER OF COLUMNS IN THE PREVIEW
    public Integer getRightFirstRow(List<String[]> csvRows) {

        Integer numbersOfColumns = null;
        int parserLine = csvRows.size();

        if (parserLine > maximumRowsToPreview) {
            parserLine = maximumRowsToPreview;
        }
        for (int i = 0; i < parserLine; i++) {
            if (csvRows.get(i) != null) {
                String[] nbRow = csvRows.get(i);
                // List<XmlField> nbRowFields = nbRow.getFields();
                if (numbersOfColumns == null || nbRow.length >= numbersOfColumns) {
                    numbersOfColumns = nbRow.length;
                }
            }
        }
        return numbersOfColumns;
    }

    /**
     * refresh the Item of Preview.
     *
     * @param csvRows
     * @param firstRowIsLabel
     */
    protected void refreshPreviewItem(final List<String[]> csvRows, final boolean firstRowIsLabel, int... selectedColumnIndex) {

        int existingItemCount = table.getItemCount();

        int end = csvRows.size();
        if (firstRowIsLabel) {
            end--;
        }

        for (int f = 0; f < end; f++) {
            String[] csvFields;
            if (firstRowIsLabel) {
                csvFields = csvRows.get(f + 1);
            } else {
                csvFields = csvRows.get(f);
            }

            String[] values = csvFields;
            if (f >= existingItemCount) {
                // create a new Item
                TableItem row = new TableItem(table, SWT.NONE);
                row.setText(values);
            } else {
                // update an existing Item
                table.getItem(f).setText(values);
            }
        }

        table.setRedraw(true);
    }

    /**
     * Getter for filePath.
     *
     * @return the filePath
     */
    public String getFilePath() {
        return this.filePath;
    }

    /**
     * Sets the filePath.
     *
     * @param filePath the filePath to set
     */
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    /**
     * Getter for header.
     *
     * @return the header
     */
    public String[] getHeader() {
        return this.header;
    }

    /**
     * DOC ocarbone Comment method "clearTablePreview".
     */
    public void clearTablePreview() {
        table.clearAll();
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumn(i).setText(""); //$NON-NLS-1$
            table.getColumn(i).setWidth(0);
        }
    }

    public void removePreviewContent() {
        table.clearAll();
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumn(i).setText(""); //$NON-NLS-1$
            table.getColumn(i).setWidth(0);
        }
    }

}
