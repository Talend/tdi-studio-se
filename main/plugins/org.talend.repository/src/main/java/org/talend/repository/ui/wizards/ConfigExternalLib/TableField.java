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
package org.talend.repository.ui.wizards.ConfigExternalLib;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Widget;

/**
 * DOC tguiu class global comment. Detailled comment <br/>
 *
 * $Id: TableEditor.java 1819 2007-02-05 09:09:15 +0000 (星期一, 05 二月 2007) yzhang $
 *
 */
public abstract class TableField {

    private TableViewer viewer;

    private List<Object> list;

    /**
     * The button box containing the Add, Remove, Up, and Down buttons; <code>null</code> if none (before creation or
     * after disposal).
     */
    private Composite buttonBox;

    /**
     * The Add button.
     */
    private Button addButton;

    /**
     * The Remove button.
     */
    private Button removeButton;

    /**
     * The Up button.
     */
    private Button upButton;

    /**
     * The Down button.
     */
    private Button downButton;

    // use this to store the original input
    private List<Object> backupList = new ArrayList<Object>();

    /**
     * The selection listener.
     */
    private SelectionListener selectionListener;

    private boolean isReadOnly;

    public TableField(String name, Composite parent) {
        this(name, parent, false);
    }

    public TableField(String name, Composite parent, boolean isReadOnly) {
        this.isReadOnly = isReadOnly;
        createControl(name, parent);
    }

    @SuppressWarnings("unchecked")
    public void setInput(List list) {
        this.list = list;
        backupList.addAll(list);
        viewer.setInput(list);
    }

    /**
     * Creates this field editor's main control containing all of its basic controls.
     *
     * @param name
     *
     * @param parent the parent control
     */
    protected void createControl(String name, Composite parent) {
        GridLayout layout = new GridLayout();
        layout.numColumns = 3;
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        parent.setLayout(layout);
        doFillIntoGrid(name, parent);
    }

    /**
     * Notifies that the Add button has been pressed.
     */
    private void addPressed() {
        List input = getNewInputObject();

        if (input != null) {
            int index = viewer.getTable().getSelectionIndex();
            if (index >= 0) {
                list.addAll(index + 1, input);
            } else {
                list.addAll(input);
            }
            viewer.refresh();
            selectionChanged();
        }
    }

    /**
     * Creates the Add, Remove, Up, and Down button in the given button box.
     *
     * @param box the box for the buttons
     */
    private void createButtons(Composite box) {
        addButton = createPushButton(box, "ListEditor.add"); //$NON-NLS-1$
        removeButton = createPushButton(box, "ListEditor.remove"); //$NON-NLS-1$
        addButton.setEnabled(!isReadOnly);
        removeButton.setEnabled(!isReadOnly);
        // upButton = createPushButton(box, "ListEditor.up"); //$NON-NLS-1$
        // downButton = createPushButton(box, "ListEditor.down"); //$NON-NLS-1$
    }

    /**
     * Helper method to create a push button.
     *
     * @param parent the parent control
     * @param key the resource name used to supply the button's label text
     * @return Button
     */
    private Button createPushButton(Composite parent, String key) {
        Button button = new Button(parent, SWT.PUSH);
        button.setText(JFaceResources.getString(key));
        button.setFont(parent.getFont());
        GridData data = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING);
        int widthHint = IDialogConstants.BUTTON_WIDTH;
        data.widthHint = Math.max(widthHint, button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
        button.setLayoutData(data);
        button.addSelectionListener(getSelectionListener());
        return button;
    }

    /**
     * Creates a selection listener.
     */
    public void createSelectionListener() {
        selectionListener = new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent event) {
                Widget widget = event.widget;
                if (widget == addButton) {
                    addPressed();
                } else if (widget == removeButton) {
                    removePressed();
                } else if (widget == upButton) {
                    upPressed();
                } else if (widget == downButton) {
                    downPressed();
                    // } else if (widget == viewer.getTable()) {
                    // selectionChanged();
                }
            }
        };
    }

    /*
     * (non-Javadoc) Method declared on FieldEditor.
     */
    protected void doFillIntoGrid(String name, Composite parent) {

        Label label = new Label(parent, SWT.NONE);
        label.setText(name);
        GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.BEGINNING).applyTo(label);

        viewer = getTableControl(parent);
        GridData gd = new GridData(GridData.FILL_BOTH);
        gd.heightHint = 150;
        viewer.getTable().setLayoutData(gd);

        buttonBox = getButtonBoxControl(parent);
        gd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
        buttonBox.setLayoutData(gd);
    }

    /**
     * Notifies that the Down button has been pressed.
     */
    private void downPressed() {
        swap(false);
    }

    /**
     * Returns this field editor's button box containing the Add, Remove, Up, and Down button.
     *
     * @param parent the parent control
     * @return the button box
     */
    public Composite getButtonBoxControl(Composite parent) {
        buttonBox = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.marginWidth = 5;
        layout.marginTop = 0;
        buttonBox.setLayout(layout);
        createButtons(buttonBox);
        buttonBox.addDisposeListener(new DisposeListener() {

            @Override
            public void widgetDisposed(DisposeEvent event) {
                addButton = null;
                removeButton = null;
                upButton = null;
                downButton = null;
                buttonBox = null;
            }
        });

        selectionChanged();
        return buttonBox;
    }

    /**
     * Returns this field editor's list control.
     *
     * @param parent the parent control
     * @return the list control
     */
    protected TableViewer getTableControl(Composite parent) {
        list = new ArrayList<Object>();
        Table table = createTable(parent);
        viewer = new TableViewer(table);
        viewer.setContentProvider(createContentProvider());
        viewer.setLabelProvider(createLabelProvider());
        table.setFont(parent.getFont());
        viewer.addSelectionChangedListener(new ISelectionChangedListener() {

            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                TableField.this.selectionChanged();
            }
        });
        // viewer.addDoubleClickListener(new IDoubleClickListener() {
        //
        // public void doubleClick(DoubleClickEvent event) {
        // IStructuredSelection selection = ((IStructuredSelection) event.getSelection());
        // String existing = (String) selection.getFirstElement();
        // String value = getExistingInputObject(existing);
        // if (value != null) {
        // int indexOf = list.indexOf(existing);
        // list.remove(existing);
        // list.add(indexOf, value);
        // viewer.refresh();
        // }
        //
        // }
        // });
        return viewer;
    }

    protected abstract IStructuredContentProvider createContentProvider();

    protected abstract ITableLabelProvider createLabelProvider();

    protected abstract Table createTable(Composite parent);

    /**
     * Creates and returns a new item for the list.
     * <p>
     * Subclasses must implement this method.
     * </p>
     *
     * @return a new item
     */
    protected abstract List getNewInputObject();

    /**
     * Returns this field editor's selection listener. The listener is created if nessessary.
     *
     * @return the selection listener
     */
    private SelectionListener getSelectionListener() {
        if (selectionListener == null) {
            createSelectionListener();
        }
        return selectionListener;
    }

    /**
     * Returns this field editor's shell.
     * <p>
     * This method is internal to the framework; subclassers should not call this method.
     * </p>
     *
     * @return the shell
     */
    protected Shell getShell() {
        if (addButton == null) {
            return null;
        }
        return addButton.getShell();
    }

    /**
     * Notifies that the Remove button has been pressed.
     */
    private void removePressed() {
        IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
        if (selection.isEmpty()) {
            return;
        }
        for (Object obj : selection.toArray()) {
            list.remove(obj);
        }
        viewer.refresh();
        selectionChanged();

        afterDeleteSelection(selection.toList());
    }

    /**
     * subclass can implements this to do it's own job.
     *
     * @param list2
     */
    protected void afterDeleteSelection(List list) {

    }

    /**
     * Notifies that the list selection has changed.
     */
    private void selectionChanged() {

        int index = viewer.getTable().getSelectionIndex();
        int size = viewer.getTable().getItemCount();

        removeButton.setEnabled(index >= 0 && !isReadOnly);
        // upButton.setEnabled(size > 1 && index > 0);
        // downButton.setEnabled(size > 1 && index >= 0 && index < size - 1);
    }

    /*
     * (non-Javadoc) Method declared on FieldEditor.
     */
    public void setFocus() {
        if (viewer != null) {
            viewer.getTable().setFocus();
        }
    }

    /**
     * Moves the currently selected item up or down.
     *
     * @param up <code>true</code> if the item should move up, and <code>false</code> if it should move down
     */
    private void swap(boolean up) {
        int index = viewer.getTable().getSelectionIndex();
        int target = up ? index - 1 : index + 1;

        if (index >= 0) {
            Collections.swap(list, index, target);
            // viewer.setSelection(target);
        }
        viewer.refresh();
        selectionChanged();
    }

    /**
     * Notifies that the Up button has been pressed.
     */
    private void upPressed() {
        swap(true);
    }

    /*
     * @see FieldEditor.setEnabled(boolean,Composite).
     */
    public void setEnabled(boolean enabled, Composite parent) {
        getTableControl(parent).getTable().setEnabled(enabled);
        addButton.setEnabled(enabled);
        removeButton.setEnabled(enabled);
        // upButton.setEnabled(enabled);
        // downButton.setEnabled(enabled);
    }

    /**
     * Getter for list.
     *
     * @return the list
     */
    public List getList() {
        return this.list;
    }

    public void revert() {
        list.clear();
        list.addAll(backupList);
    }

}
