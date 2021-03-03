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
package org.talend.designer.mapper.ui.dialog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

/**
 * DOC ycbai class global comment. Detailled comment
 *
 * @param <E>
 */
public class ListStringValueDialog<E> extends Dialog {

    private TableViewer tableViewer;

    private Table table;

    private List<E> dataList = new ArrayList<E>();

    private Object selectObj;

    /**
     * Create the dialog
     *
     * @param parentShell
     */
    public ListStringValueDialog(Shell parentShell) {
        super(parentShell);
    }

    public ListStringValueDialog(Shell parentShell, List<E> dataList) {
        this(parentShell);
        this.dataList = dataList;
        setShellStyle(SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.RESIZE | SWT.MIN | SWT.MAX);
    }

    public ListStringValueDialog(Shell parentShell, E[] dataArray) {
        this(parentShell);
        this.dataList = Arrays.asList(dataArray);
        setShellStyle(SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.RESIZE | SWT.MIN | SWT.MAX);
    }

    /**
     * Create contents of the dialog
     *
     * @param parent
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);

        tableViewer = new TableViewer(container, SWT.FULL_SELECTION | SWT.BORDER);
        table = tableViewer.getTable();
        table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        TableLayout layout = new TableLayout();
        table.setLayout(layout);

        layout.addColumnData(new ColumnWeightData(100));
        final TableColumn tableColumn = new TableColumn(table, SWT.NONE);
        tableColumn.setAlignment(SWT.CENTER);
        tableColumn.setWidth(100);
        tableColumn.setText("column");

        tableViewer.setContentProvider(new ArrayContentProvider());
        tableViewer.setLabelProvider(new ViewerLabelProvider());
        tableViewer.setInput(dataList);

        tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {

            public void selectionChanged(SelectionChangedEvent event) {
                IStructuredSelection selection = (IStructuredSelection) event.getSelection();
                selectObj = selection.getFirstElement();
            }
        });
        tableViewer.addDoubleClickListener(new IDoubleClickListener() {

            public void doubleClick(final DoubleClickEvent event) {
                IStructuredSelection selection = (IStructuredSelection) event.getSelection();
                selectObj = selection.getFirstElement();
                setReturnCode(0);
                close();
            }
        });

        //
        return container;
    }

    /**
     * Create contents of the button bar
     *
     * @param parent
     */
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
        createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
    }

    /**
     * Sets the dataList.
     *
     * @param dataList the dataList to set
     */
    public void setDataList(List<E> dataList) {
        this.dataList = dataList;
        tableViewer.setInput(dataList);
    }

    public void setDataList(E[] dataArray) {
        this.dataList = Arrays.asList(dataArray);
        tableViewer.setInput(dataList);
    }

    /**
     * Getter for selectObj.
     *
     * @return the selectObj
     */
    public Object getSelectObj() {
        return this.selectObj;
    }

    public String getSelectStr() {
        if (selectObj != null) {
            return selectObj.toString();
        }
        return null;
    }

    /**
     * Return the initial size of the dialog
     *
     * For bug TUP-21564, remove the initialSize and return the default size from parent class instead.
     */
//    @Override
//    protected Point getInitialSize() {
//        return new Point(227, 278);
//    }

    /**
     * DOC ycbai CustomerCellDialog class global comment. Detailled comment
     */
    class ViewerLabelProvider extends LabelProvider implements ITableLabelProvider {

        public Image getColumnImage(Object element, int columnIndex) {
            return null;
        }

        public String getColumnText(Object element, int columnIndex) {
            E type = (E) element;
            if (columnIndex == 0) {
                return type.toString();
            }
            return "";
        }
    }

    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("Options");
    }

}
