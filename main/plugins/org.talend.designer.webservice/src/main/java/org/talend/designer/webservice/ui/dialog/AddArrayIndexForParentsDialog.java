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
package org.talend.designer.webservice.ui.dialog;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableColumn;
import org.talend.designer.webservice.data.ArrayIndexList;
import org.talend.designer.webservice.ws.wsdlinfo.ParameterInfo;

/**
 * DOC gcui class global comment. Detailled comment
 */
public class AddArrayIndexForParentsDialog extends Dialog {

    private ParameterInfo para;

    private ParameterInfo selectedParaInfo;

    private int arraySize = 0;

    private Shell parentShell;

    private String indexText = "-1";

    private String title;

    private boolean maximized;

    private Rectangle size;

    private TableViewer table;

    public static final int NAME = 0;

    public static final int INDEX = 1;

    public static final String[] COLUMN_NAME = { "Parameter Name", "Index Number" };

    private List<ArrayIndexList> arrayIndexList = new ArrayList<ArrayIndexList>();

    /**
     * DOC gcui ErrorMessageDialog constructor comment.
     *
     * @param parentShell
     */
    protected AddArrayIndexForParentsDialog(Shell parentShell) {
        super(parentShell);
        setShellStyle(getShellStyle() | SWT.RESIZE);
        this.parentShell = parentShell;
    }

    public AddArrayIndexForParentsDialog(Shell parentShell, ParameterInfo para) {
        this(parentShell);
        this.parentShell = parentShell;
        this.para = para;
        this.arraySize = para.getArraySize();
        createParentsArrayIndexItem(para);

    }

    private List<ParameterInfo> getAllArrayFromParents(ParameterInfo parameter, List<ParameterInfo> allArrayParents) {
        if (allArrayParents == null) {
            allArrayParents = new ArrayList<ParameterInfo>();
        }
        if (parameter.getParent() != null) {
            ParameterInfo parent = parameter.getParent();
            if (parent.getArraySize() != 0) {
                allArrayParents.add(parent);
            }

            getAllArrayFromParents(parent, allArrayParents);
        }

        return allArrayParents;
    }

    protected Control createDialogArea(Composite parent) {
        Composite createDialogArea = (Composite) super.createDialogArea(parent);
        FillLayout layout = new FillLayout();
        // layout.horizontalSpacing = 1;
        // layout.numColumns = 1;
        GridData data = new GridData(GridData.FILL_BOTH);
        createDialogArea.setLayout(layout);
        createDialogArea.setLayoutData(data);

        table = new TableViewer(createDialogArea, SWT.FULL_SELECTION | SWT.BORDER);

        new TableColumn(table.getTable(), SWT.LEFT).setText(COLUMN_NAME[0]);
        table.getTable().getColumn(0).pack();
        new TableColumn(table.getTable(), SWT.LEFT).setText(COLUMN_NAME[1]);
        table.getTable().getColumn(1).pack();

        table.getTable().setHeaderVisible(true);
        table.getTable().setLinesVisible(true);

        table.setContentProvider(new IndexContentProvider());
        table.setLabelProvider(new IndexLabelProvider());

        table.setInput(arrayIndexList);

        table.setColumnProperties(COLUMN_NAME);

        CellEditor[] editors = new CellEditor[2];
        editors[0] = null;
        editors[1] = new TextCellEditor(table.getTable());

        table.setCellEditors(editors);
        table.setCellModifier(new ICellModifier() {

            public boolean canModify(Object element, String property) {
                if (property.equals(COLUMN_NAME[0]))
                    return false;
                return true;
            }

            public Object getValue(Object element, String property) {
                ArrayIndexList index = (ArrayIndexList) element;
                if (property.equals(COLUMN_NAME[1])) {
                    if (index.getIndexNum() != null) {
                        return index.getIndexNum();
                    } else {
                        return "";
                    }
                }
                return null;
            }

            public void modify(Object element, String property, Object value) {
                if (element instanceof Item)
                    element = ((Item) element).getData();
                ArrayIndexList index = (ArrayIndexList) element;
                if (property.equals(COLUMN_NAME[1]))
                    index.setIndexNum((String) value);

                table.refresh();
            }
        });

        return createDialogArea;
    }

    /**
     * DOC gcui Comment method "createParentsArrayIndexItem".
     */
    private List<ArrayIndexList> createParentsArrayIndexItem(ParameterInfo itemPara) {
        List<ParameterInfo> allArray = getAllArrayFromParents(itemPara, null);
        for (int i = 0; i < allArray.size(); i++) {
            String name = allArray.get(i).getName();
            ArrayIndexList indexItem = new ArrayIndexList(name);
            arrayIndexList.add(indexItem);
        }

        return arrayIndexList;
    }

    public ParameterInfo getSelectedParaInfo() {
        return selectedParaInfo;
    }

    protected void okPressed() {
        // String com = text.getText();
        // int index = Integer.valueOf(indexText);
        //
        // if (text.getEnabled() == true && ("".equals(text.getText()) || text.getText() == null)) {
        // MessageBox box = new MessageBox(parentShell, SWT.ICON_ERROR | SWT.OK);
        //            box.setText(Messages.getString("AddArrayIndexDialog.Error")); //$NON-NLS-1$
        //            box.setMessage(Messages.getString("AddArrayIndexDialog.Input_Index")); //$NON-NLS-1$
        // box.open();
        // return;
        // }
        //
        // if (arraySize != -1 && (index < 0 || index > arraySize)) {
        // MessageBox box = new MessageBox(parentShell, SWT.ICON_ERROR | SWT.OK | SWT.CANCEL);
        //            box.setText(Messages.getString("AddArrayIndexDialog.Error")); //$NON-NLS-1$
        //            box.setMessage(Messages.getString("AddArrayIndexDialog.check_size")); //$NON-NLS-1$
        // box.open();
        //            text.setText(""); //$NON-NLS-1$
        // return;
        // }
        super.okPressed();
    }

    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setBounds(500, 300, 350, 200);
        newShell.setText("Index Of Parents Array.");
    }

    public void setTitle(String title) {
        this.title = title;

    }

    public void setMaximized(boolean maximized) {
        this.maximized = maximized;
    }

    public void setSize(Rectangle size) {
        this.size = size;
    }

    public int returnIndex() {
        int index = -1;
        if (indexText != null && !"".equals(indexText)) { //$NON-NLS-1$
            index = Integer.valueOf(indexText);
            if (arraySize != -1 && (index < 0 || index > arraySize)) {
                index = -1;
            }
        }
        return index;
    }

    public List<ArrayIndexList> getArrayIndexList() {
        return this.arrayIndexList;
    }

    public class IndexContentProvider implements IStructuredContentProvider {

        /*
         * (non-Javadoc)
         *
         * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
         */
        public Object[] getElements(Object inputElement) {
            if (inputElement instanceof List)
                return ((List) inputElement).toArray();
            return null;
        }

        /*
         * (non-Javadoc)
         *
         * @see org.eclipse.jface.viewers.IContentProvider#dispose()
         */
        public void dispose() {
            // TODO Auto-generated method stub

        }

        /*
         * (non-Javadoc)
         *
         * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer,
         * java.lang.Object, java.lang.Object)
         */
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            // TODO Auto-generated method stub

        }

    }

    public class IndexLabelProvider implements ITableLabelProvider {

        /*
         * (non-Javadoc)
         *
         * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
         */
        public Image getColumnImage(Object element, int columnIndex) {
            // TODO Auto-generated method stub
            return null;
        }

        /*
         * (non-Javadoc)
         *
         * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
         */
        public String getColumnText(Object element, int columnIndex) {
            ArrayIndexList index = (ArrayIndexList) element;
            if (columnIndex == NAME) {
                return index.getParameterName() + "";
            } else if (columnIndex == INDEX) {
                if (index.getIndexNum() != null) {
                    return index.getIndexNum() + "";
                } else {
                    return "";
                }
            }
            return "";
        }

        /*
         * (non-Javadoc)
         *
         * @see
         * org.eclipse.jface.viewers.IBaseLabelProvider#addListener(org.eclipse.jface.viewers.ILabelProviderListener)
         */
        public void addListener(ILabelProviderListener listener) {
            // TODO Auto-generated method stub

        }

        /*
         * (non-Javadoc)
         *
         * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
         */
        public void dispose() {
            // TODO Auto-generated method stub

        }

        /*
         * (non-Javadoc)
         *
         * @see org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(java.lang.Object, java.lang.String)
         */
        public boolean isLabelProperty(Object element, String property) {
            // TODO Auto-generated method stub
            return false;
        }

        /*
         * (non-Javadoc)
         *
         * @see
         * org.eclipse.jface.viewers.IBaseLabelProvider#removeListener(org.eclipse.jface.viewers.ILabelProviderListener)
         */
        public void removeListener(ILabelProviderListener listener) {
            // TODO Auto-generated method stub

        }

    }
}
