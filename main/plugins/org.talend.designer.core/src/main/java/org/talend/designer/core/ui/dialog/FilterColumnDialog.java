// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.core.ui.dialog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.ui.editor.connections.Connection;
import org.talend.designer.core.ui.editor.connections.TracesConnectionUtils;

/**
 * DOC Administrator class global comment. Detailled comment
 */
public class FilterColumnDialog extends Dialog {

    private static final String SETUP_TRACES_TITLE = Messages.getString("FilterTraceColumnAction.SetupTraces"); //$NON-NLS-1$

    private static final String CONDITION = "CONDITION"; //$NON-NLS-1$

    private static final String COLUMN = "COLUMN"; //$NON-NLS-1$

    private Connection conn;

    private CheckboxTableViewer tabView;

    private Table tab;

    private String[] coluName = { COLUMN, CONDITION };

    private CellEditor[] cellEditor;

    private Button checkAll;

    private Button checkNo;

    private CommandStack stack;

    /**
     * hwang FilterColumn constructor comment.
     *
     * @param parentShell
     */
    protected FilterColumnDialog(Shell parentShell) {
        super(parentShell);
    }

    public FilterColumnDialog(Shell parentShell, Connection conn, CommandStack stack) {
        super(parentShell);
        this.setShellStyle(this.getShellStyle() | SWT.RESIZE | SWT.MAX);
        this.conn = conn;
        this.stack = stack;
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(SETUP_TRACES_TITLE);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite createDialogArea = (Composite) super.createDialogArea(parent);
        createDialogArea.setLayout(new GridLayout(2, false));
        createDialogArea.setLayoutData(new GridData(GridData.FILL_BOTH));

        tabView = CheckboxTableViewer.newCheckList(createDialogArea, SWT.FULL_SELECTION);
        tab = tabView.getTable();

        Composite btnComposite = new Composite(createDialogArea, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.marginTop = 15;
        btnComposite.setLayout(layout);
        btnComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

        checkAll = new Button(btnComposite, SWT.PUSH);
        checkAll.setText(Messages.getString("FilterTraceColumnAction.SelectAll")); //$NON-NLS-1$
        checkAll.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER));

        checkNo = new Button(btnComposite, SWT.PUSH);
        checkNo.setText(Messages.getString("FilterTraceColumnAction.UnselectAll")); //$NON-NLS-1$
        checkNo.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER));

        TableColumn tableColumn = new TableColumn(tab, SWT.NONE);
        tableColumn.setText(Messages.getString("FilterTraceColumnAction.Column")); //$NON-NLS-1$
        tableColumn.setWidth(100);

        // PTODO if enable trace condition, only active this.
        // tableColumn = new TableColumn(tab, SWT.NONE);
        // tableColumn.setText(Messages.getString("FilterTraceColumnAction.Condition")); //$NON-NLS-1$
        // tableColumn.setWidth(200);

        tab.setHeaderVisible(true);
        tab.setLinesVisible(true);
        tabView.setContentProvider(ArrayContentProvider.getInstance());
        tabView.setLabelProvider(new TableLabelProvider());
        tabView.setInput(conn.getMetadataTable().clone());

        GridData gridData = new GridData(GridData.FILL_BOTH);
        gridData.heightHint = 150;
        gridData.minimumHeight = 150;
        gridData.widthHint = 180;
        gridData.minimumWidth = 180;
        tab.setLayoutData(gridData);

        checkAll.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
                tabView.setAllChecked(true);

            }

        });

        checkNo.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
                tabView.setAllChecked(false);

            }

        });

        cellEditor = new CellEditor[2];
        cellEditor[0] = null;
        cellEditor[1] = new TextCellEditor(tab);
        tabView.setColumnProperties(coluName);
        tabView.setCellEditors(cellEditor);
        tabView.setCellModifier(new TableModify(tabView));

        initChecked();

        return createDialogArea;
    }

    private void initChecked() {
        List<ConditionBean> beans = new ArrayList<ConditionBean>();
        for (IMetadataColumn column : conn.getMetadataTable().getListColumns()) {
            ConditionBean bean = new ConditionBean(column);
            bean.setCondition(TracesConnectionUtils.getTracesColumnCondition(conn, column.getLabel()));
            beans.add(bean);
        }
        tabView.setInput(beans);

        List<String> enabledTraceColumns = TracesConnectionUtils.getEnabledTraceColumns(conn);
        for (TableItem item : tabView.getTable().getItems()) {
            Object data = item.getData();
            if (data instanceof ConditionBean) {
                ConditionBean bean = (ConditionBean) data;
                if (enabledTraceColumns.contains(bean.getColumnName())) {
                    tabView.setChecked(bean, true);
                }
            }
        }
    }

    @Override
    protected void okPressed() {
        stack.execute(new Command() {

            @Override
            public void execute() {
                List<Object> checkedElements = Arrays.asList(tabView.getCheckedElements());
                if (checkedElements.size() == 0)
                    conn.setNullColumn = true;
                Integer validColumn1 = 0;
                Object value = conn.getPropertyValue(EParameterName.TRACES_CONNECTION_FILTER.getName());
                conn.traceColumn.clear();
                for (TableItem item : tabView.getTable().getItems()) {
                    Object data = item.getData();
                    if (data instanceof ConditionBean) {
                        ConditionBean bean = (ConditionBean) data;
                        if (checkedElements.contains(bean)) {
                            conn.setNullColumn = false;
                            conn.traceColumn.add(validColumn1);
                        } else {
                            tabView.setChecked(item, false);
                        }
                        TracesConnectionUtils.setTraceColumnValues(conn, bean.getColumnName(), bean.getCondition(),
                                checkedElements.contains(bean));
                        validColumn1++;
                    }
                }
                // fire property change and refresh

                conn.setPropertyValue(EParameterName.TRACES_CONNECTION_FILTER.getName(), value);
            }
        });

        super.okPressed();
    }

    private class TableLabelProvider implements ITableLabelProvider {

        /*
         * (non-Javadoc)
         *
         * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
         */
        public Image getColumnImage(Object element, int columnIndex) {
            return null;
        }

        /*
         * (non-Javadoc)
         *
         * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
         */
        public String getColumnText(Object element, int columnIndex) {
            if (element instanceof ConditionBean) {
                switch (columnIndex) {
                case 0:
                    return ((ConditionBean) element).getColumnName();
                case 1:
                    return ((ConditionBean) element).getCondition();
                default:
                    break;
                }
            }
            return null;
        }

        /*
         * (non-Javadoc)
         *
         * @see
         * org.eclipse.jface.viewers.IBaseLabelProvider#addListener(org.eclipse.jface.viewers.ILabelProviderListener )
         */
        public void addListener(ILabelProviderListener listener) {

        }

        /*
         * (non-Javadoc)
         *
         * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
         */
        public void dispose() {

        }

        /*
         * (non-Javadoc)
         *
         * @see org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(java.lang.Object, java.lang.String)
         */
        public boolean isLabelProperty(Object element, String property) {
            return false;
        }

        /*
         * (non-Javadoc)
         *
         * @see
         * org.eclipse.jface.viewers.IBaseLabelProvider#removeListener(org.eclipse.jface.viewers.ILabelProviderListener
         * )
         */
        public void removeListener(ILabelProviderListener listener) {

        }
    }

    private class TableModify implements ICellModifier {

        CheckboxTableViewer tabView = null;

        public TableModify(CheckboxTableViewer tabView) {
            this.tabView = tabView;
        }

        public boolean canModify(Object element, String property) {
            return true;
        }

        public Object getValue(Object element, String property) {
            if (element instanceof ConditionBean) {
                if (property.equals(CONDITION)) {
                    return ((ConditionBean) element).getCondition();
                }
            }
            return null;
        }

        public void modify(Object element, String property, Object value) {

            TableItem item = (TableItem) element;
            if (item.getData() instanceof ConditionBean) {
                ConditionBean pojo = (ConditionBean) item.getData();
                if (property.equals(CONDITION)) {
                    pojo.setCondition((String) value);
                }
            }
            tabView.refresh();
        }

    }

    private class ConditionBean {

        private IMetadataColumn column;

        private String condition;

        public ConditionBean(IMetadataColumn column) {
            super();
            this.column = column;
        }

        public String getCondition() {
            return this.condition;
        }

        public void setCondition(String condition) {
            this.condition = condition;
        }

        public String getColumnName() {
            return this.column.getLabel();
        }
    }
}
