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
package org.talend.designer.runprocess.prefs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.talend.commons.ui.swt.preferences.TableEditor;
import org.talend.designer.runprocess.i18n.Messages;
import org.talend.designer.runprocess.utils.JobVMArgumentsUtil;

/**
 * qzhang class global comment. Detailled comment <br/>
 *
 */
public class VMArgumentsViewer extends TableEditor {

    private static final List<String> EMPTY_STRING_LIST = Collections.unmodifiableList(new ArrayList<String>());

    /**
     * qzhang VMArgumentsViewer constructor comment.
     *
     * @param name
     * @param labelText
     * @param parent
     */
    public VMArgumentsViewer(String name, String labelText, Composite parent) {
        super(name, labelText, parent);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.commons.ui.swt.preferences.TableEditor#createContentProvider()
     */
    @Override
    protected IStructuredContentProvider createContentProvider() {
        return new IStructuredContentProvider() {

            public Object[] getElements(Object inputElement) {
                return ((List) inputElement).toArray();
            }

            public void dispose() {
            }

            public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            }

        };
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.commons.ui.swt.preferences.TableEditor#createLabelProvider()
     */
    @Override
    protected ITableLabelProvider createLabelProvider() {
        return new ITableLabelProvider() {

            public Image getColumnImage(Object element, int columnIndex) {
                return null;
            }

            public String getColumnText(Object element, int columnIndex) {
                String value = ((String) element);
                if (columnIndex == 0) {
                    return value;
                }
                throw new IllegalStateException();
            }

            public void addListener(ILabelProviderListener listener) {
            }

            public void dispose() {
            }

            public boolean isLabelProperty(Object element, String property) {
                return false;
            }

            public void removeListener(ILabelProviderListener listener) {
            }

        };
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.commons.ui.swt.preferences.TableEditor#createTable(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Table createTable(Composite parent) {
        Table contextTable = new Table(parent, SWT.BORDER | SWT.FULL_SELECTION);
        contextTable.setLinesVisible(true);
        contextTable.setHeaderVisible(true);

        TableColumn engineName = new TableColumn(contextTable, SWT.NONE);
        engineName.setText(Messages.getString("VMArgumentsViewer.argument")); //$NON-NLS-1$
        engineName.setWidth(350);
        return contextTable;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.commons.ui.swt.preferences.TableEditor#getExistingInputObject(java.lang.String)
     */
    @Override
    protected String getExistingInputObject(String obj) {
        VMArgumentDialog dialog = new VMArgumentDialog(getShell(), obj);
        if (dialog.open() == Dialog.OK) {
            return dialog.getNewItem();
        }
        return obj;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.commons.ui.swt.preferences.TableEditor#getNewInputObject()
     */
    @Override
    protected String getNewInputObject() {
        VMArgumentDialog dialog = new VMArgumentDialog(getShell(), null);
        if (dialog.open() == Dialog.OK) {
            return dialog.getNewItem();
        }
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.commons.ui.swt.preferences.TableEditor#readString(java.lang.String)
     */
    @Override
    protected List<String> readString(String stringList) {
        return new JobVMArgumentsUtil().readString(stringList);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.commons.ui.swt.preferences.TableEditor#writeString(java.util.List)
     */
    @Override
    protected String writeString(List<String> items) {
        return new JobVMArgumentsUtil().writeString(items);
    }

    /**
     * qzhang VMArgumentsViewer class global comment. Detailled comment. <br/>
     *
     */
    class VMArgumentDialog extends Dialog {

        private Text argText;

        private final String arg;

        private String newItem;

        /**
         * DOC qzhang VMArgumentDialog constructor comment.
         *
         * @param parentShell
         */
        protected VMArgumentDialog(Shell parentShell, String item) {
            super(parentShell);
            if (item == null || "".equals(item)) { //$NON-NLS-1$
                arg = ""; //$NON-NLS-1$
            } else {
                arg = item;
            }
            setShellStyle(getShellStyle() | SWT.RESIZE | SWT.DIALOG_TRIM | SWT.MIN | SWT.MAX);
        }

        /*
         * (non-Javadoc)
         *
         * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
         */
        @Override
        protected void configureShell(Shell newShell) {
            super.configureShell(newShell);
            newShell.setText(Messages.getString("VMArgumentsViewer.vmArgument")); //$NON-NLS-1$
        }

        /*
         * (non-Javadoc)
         *
         * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
         */
        @Override
        protected Control createDialogArea(Composite parent) {
            Composite container = (Composite) super.createDialogArea(parent);
            container.setLayout(new GridLayout(2, false));
            Label addLabel = new Label(container, SWT.NONE);
            addLabel.setText(Messages.getString("VMArgumentsViewer.argumentcolon")); //$NON-NLS-1$
            argText = new Text(container, SWT.BORDER);
            GridData gridData = new GridData(GridData.FILL_BOTH);
            gridData.heightHint = 50;
            gridData.widthHint = 200;
            argText.setLayoutData(gridData);
            argText.setText(arg);

            return container;
        }

        /*
         * (non-Javadoc)
         *
         * @see org.eclipse.jface.dialogs.Dialog#okPressed()
         */
        @Override
        protected void okPressed() {
            newItem = argText.getText();
            super.okPressed();
        }

        public String getNewItem() {
            return newItem;
        }
    }
}
