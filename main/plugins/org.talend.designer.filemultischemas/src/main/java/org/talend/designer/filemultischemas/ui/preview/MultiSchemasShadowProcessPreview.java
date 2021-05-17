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
package org.talend.designer.filemultischemas.ui.preview;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.talend.commons.ui.runtime.image.EImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.designer.filemultischemas.ui.MultiSchemasUI;
import org.talend.metadata.managment.ui.preview.ShadowProcessPreview;

/**
 * cLi class global comment. Detailled comment
 */
public class MultiSchemasShadowProcessPreview extends ShadowProcessPreview {

    MultiSchemasUI multiSchemaUI;

    // hywang add varriable "key" for feature 7373

    // hywang add varrible "dftSelectedColumn" for feature 7373
    private static int dftSelectedColumn = -1;

    private SelectionListener listener;

    public int getSelectColumnIndex() {
        return multiSchemaUI.getSelectedColumnIndex();
    }

    public void setSelectColumnIndex(int selectedColumn) {
        multiSchemaUI.setSelectedColumnIndex(selectedColumn);
    }

    public MultiSchemasShadowProcessPreview(MultiSchemasUI multiSchemaUI, Composite composite) {
        super(composite, null, -1, -1);
        this.multiSchemaUI = multiSchemaUI;
    }

    @Override
    public void newTablePreview() {
        table = new Table(composite, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        table.setLayoutData(new GridData(GridData.FILL_BOTH));
    }

    /**
     * DOC hywang Comment method "showCheckImageOnColumn".
     *
     * @param selectedColumnIndex
     */
    private void showCheckImageOnColumn(int... selectedColumnIndex) {
        if (table == null || table.isDisposed()) {
            return;
        }

        checkSelectedIndex(selectedColumnIndex);
        // hywang add for feature 7373, removed it by 6759
        addListenerOncolumn();
    }

    public void addListenerOncolumn() {
        if (table == null || table.isDisposed()) {
            return;
        }
        for (int i = 0; i < table.getColumnCount(); i++) {
            final TableColumn tc = table.getColumn(i);
            SelectionListener listener = new SelectionListener() {

                public void widgetDefaultSelected(SelectionEvent e) {
                }

                public void widgetSelected(SelectionEvent e) {
                    if (!multiSchemaUI.getUseMultiSaparators().getSelection()) {
                        if (tc.getImage().equals(ImageProvider.getImage(EImage.UNCHECKED_ICON))) {
                            tc.setImage(ImageProvider.getImage(EImage.CHECKED_ICON));
                            for (int j = 0; j < table.getColumnCount(); j++) {
                                if (table.getColumn(j).getImage().equals(ImageProvider.getImage(EImage.CHECKED_ICON))
                                        && table.getColumn(j) != tc) {
                                    table.getColumn(j).setImage(ImageProvider.getImage(EImage.UNCHECKED_ICON));
                                }
                                if (tc.equals(table.getColumn(j))) {
                                    setSelectColumnIndex(j);
                                }
                            }
                        }
                    }
                }
            };
            this.listener = listener;

            tc.addSelectionListener(listener);
        }
    }

    private void checkSelectedIndex(int... selectedColumnIndex) {
        if (selectedColumnIndex == null) {
            selectedColumnIndex = new int[] { 0 };
        }
        table.setRedraw(false);

        for (int i = 0; i < table.getColumnCount(); i++) {
            final TableColumn tc = table.getColumn(i);
            if (i == selectedColumnIndex[0]) {
                tc.setImage(ImageProvider.getImage(EImage.CHECKED_ICON)); // default selected column is from
                // component
            } else {
                tc.setImage(ImageProvider.getImage(EImage.UNCHECKED_ICON));
            }
        }
        table.setHeaderVisible(true);

    }

    @Override
    protected void refreshPreviewItem(List<String[]> csvRows, boolean firstRowIsLabel, int... selectedColumnIndex) {
        setSelectColumnIndex(selectedColumnIndex[0]);
        int existingItemCount = table.getItemCount();

        int end = csvRows.size();
        if (firstRowIsLabel) {
            end--;
        }

        showCheckImageOnColumn(selectedColumnIndex);

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

    public SelectionListener getListener() {
        return this.listener;
    }

    public void setListener(SelectionListener listener) {
        this.listener = listener;
    }

}
