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
package org.talend.designer.xmlmap.ui.dialog;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.talend.commons.ui.runtime.swt.tableviewer.CellEditorValueAdapterFactory;
import org.talend.commons.ui.runtime.swt.tableviewer.behavior.CellEditorValueAdapter;
import org.talend.commons.ui.swt.advanced.dataeditor.AbstractDataTableEditorView;
import org.talend.commons.ui.swt.advanced.dataeditor.ExtendedToolbarView;
import org.talend.commons.ui.swt.advanced.dataeditor.button.AddPushButton;
import org.talend.commons.ui.swt.advanced.dataeditor.button.AddPushButtonForExtendedTable;
import org.talend.commons.ui.swt.advanced.dataeditor.button.CopyPushButton;
import org.talend.commons.ui.swt.extended.table.ExtendedTableModel;
import org.talend.commons.ui.swt.tableviewer.IModifiedBeanListener;
import org.talend.commons.ui.swt.tableviewer.ModifiedBeanEvent;
import org.talend.commons.ui.swt.tableviewer.TableViewerCreator;
import org.talend.commons.ui.swt.tableviewer.TableViewerCreatorColumn;
import org.talend.commons.utils.data.bean.IBeanPropertyAccessors;
import org.talend.commons.utils.data.list.IListenableListListener;
import org.talend.commons.utils.data.list.ListenableListEvent;
import org.talend.designer.xmlmap.model.emf.xmlmap.InputLoopNodesTable;
import org.talend.designer.xmlmap.model.emf.xmlmap.TreeNode;
import org.talend.designer.xmlmap.ui.tabs.table.TreeSchemaTableEntry;

/**
 * DOC hcyi class global comment. Detailled comment
 */
public class SetLoopFunctionDialog extends Dialog {

    private Label statusLabel;

    private InputLoopNodesTable inputLoopNodesTable;

    private List<TreeNode> inputLoops;

    ExtendedTableModel<TreeSchemaTableEntry> tableModel = null;

    AbstractDataTableEditorView<TreeSchemaTableEntry> tableViwer;

    public SetLoopFunctionDialog(Shell parentShell, InputLoopNodesTable inputLoopNodesTable, List<TreeNode> inputLoops) {
        super(parentShell);
        setShellStyle(getShellStyle() | SWT.RESIZE);
        this.inputLoops = inputLoops;
        this.inputLoopNodesTable = inputLoopNodesTable;
        initData();
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("Configure source loops");
    }

    private void initData() {
        List<TreeSchemaTableEntry> inputLoopEntrys = new ArrayList<TreeSchemaTableEntry>();
        tableModel = new ExtendedTableModel<TreeSchemaTableEntry>("Source Loops", inputLoopEntrys);
        for (TreeNode sourceNode : inputLoopNodesTable.getInputloopnodes()) {
            TreeSchemaTableEntry entry = new TreeSchemaTableEntry(sourceNode);
            inputLoopEntrys.add(entry);
        }
    }

    public Control createDialogArea(Composite parent) {
        Composite composite = new Composite(parent, SWT.BORDER);
        GridLayout layout = new GridLayout();
        composite.setLayout(layout);
        GridData data = new GridData(GridData.FILL_BOTH);
        composite.setLayoutData(data);

        tableViwer = new AbstractDataTableEditorView<TreeSchemaTableEntry>(composite, SWT.NONE, tableModel, false, true, false) {

            @Override
            protected void createColumns(TableViewerCreator<TreeSchemaTableEntry> tableViewerCreator, Table table) {
                TableViewerCreatorColumn column = new TableViewerCreatorColumn(tableViewerCreator);
                column.setTitle("Sequence");
                column.setWeight(40);

                column.setBeanPropertyAccessors(new IBeanPropertyAccessors<TreeSchemaTableEntry, Object>() {

                    public Object get(TreeSchemaTableEntry bean) {
                        int index = getExtendedTableModel().getBeansList().indexOf(bean);
                        return getExtendedTableModel().getBeansList().indexOf(bean);
                    }

                    public void set(TreeSchemaTableEntry bean, Object value) {
                        // do nothing
                    }
                });

                column = new TableViewerCreatorColumn(tableViewerCreator);
                column.setTitle("Xpath");
                column.setWeight(60);
                String[] items = new String[inputLoops.size()];
                for (int i = 0; i < inputLoops.size(); i++) {
                    items[i] = inputLoops.get(i).getXpath();
                }
                CellEditorValueAdapter comboValueAdapter = CellEditorValueAdapterFactory
                        .getComboAdapterForComboCellEditor("String");
                ComboBoxCellEditor cellEditor = new ComboBoxCellEditor(tableViewerCreator.getTable(), items, SWT.READ_ONLY);
                column.setModifiable(true);
                column.setCellEditor(cellEditor, comboValueAdapter);
                column.setBeanPropertyAccessors(new IBeanPropertyAccessors<TreeSchemaTableEntry, Object>() {

                    public Object get(TreeSchemaTableEntry bean) {
                        return bean.getXPath();
                    }

                    public void set(TreeSchemaTableEntry bean, Object value) {
                        for (TreeNode loopNode : inputLoops) {
                            if (loopNode.getXpath().equals(value)) {
                                bean.setTreeNode(loopNode);
                            }
                        }
                    }
                });
            }

            @Override
            protected ExtendedToolbarView initToolBar() {
                ExtendedToolbarView toolbarView = new ExtendedToolbarView(getMainComposite(), SWT.NONE,
                        this.getExtendedTableViewer()) {

                    @Override
                    protected CopyPushButton createCopyPushButton() {
                        return null;
                    }

                    @Override
                    protected AddPushButton createAddPushButton() {
                        return new AddPushButtonForExtendedTable(this.toolbar, getExtendedTableViewer()) {

                            @Override
                            public boolean getEnabledState() {
                                return super.getEnabledState();
                            }

                            @Override
                            protected Object getObjectToAdd() {
                                TreeNode loopNodetoAdd = null;
                                for (TreeNode loopNode : inputLoops) {
                                    boolean found = false;
                                    for (TreeSchemaTableEntry extendedModel : getExtendedTableModel().getBeansList()) {
                                        if (loopNode.getXpath().equals(extendedModel.getXPath())) {
                                            found = true;
                                            break;
                                        }
                                    }
                                    if (!found) {
                                        loopNodetoAdd = loopNode;
                                        break;
                                    }
                                }
                                if (loopNodetoAdd == null && !inputLoops.isEmpty()) {
                                    loopNodetoAdd = inputLoops.get(0);
                                }
                                if (loopNodetoAdd != null) {
                                    TreeSchemaTableEntry entry = new TreeSchemaTableEntry(loopNodetoAdd);
                                    return entry;
                                }

                                return null;
                            }

                        };
                    }

                };
                return super.initToolBar();
            }
        };
        tableViwer.setGridDataSize(400, 120);
        statusLabel = new Label(composite, SWT.NONE);
        statusLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        tableModel.addAfterOperationListListener(new IListenableListListener<TreeSchemaTableEntry>() {

            public void handleEvent(ListenableListEvent<TreeSchemaTableEntry> event) {
                updateStatus(true);
            }
        });
        tableModel.setModifiedBeanListenable(tableViwer.getTableViewerCreator());
        tableModel.addModifiedBeanListener(new IModifiedBeanListener<TreeSchemaTableEntry>() {

            public void handleEvent(ModifiedBeanEvent<TreeSchemaTableEntry> event) {
                updateStatus(false);
            }

        });
        return composite;
    }

    private void updateStatus(boolean refreshTree) {
        if (refreshTree) {
            tableViwer.getTableViewerCreator().getTableViewer().refresh();
        }
        List<String> xpath = new ArrayList<String>();
        boolean findDuplicated = false;
        for (TreeSchemaTableEntry entry : tableModel.getBeansList()) {
            if (xpath.contains(entry.getXPath())) {
                findDuplicated = true;
            } else {
                xpath.add(entry.getXPath());
            }
        }

        Button button = getButton(IDialogConstants.OK_ID);
        if (findDuplicated) {
            statusLabel.setText("Duplicated source loop node exist in the table ,please remove it.");
            button.setEnabled(false);
        } else {
            statusLabel.setText("");
            button.setEnabled(true);
        }
    }

    @Override
    protected void okPressed() {
        List<TreeNode> usedLoops = new ArrayList<TreeNode>();
        for (TreeSchemaTableEntry entry : tableModel.getBeansList()) {
            usedLoops.add(entry.getTreeNode());
        }
        inputLoopNodesTable.getInputloopnodes().clear();
        inputLoopNodesTable.getInputloopnodes().addAll(usedLoops);

        super.okPressed();
    }

}
