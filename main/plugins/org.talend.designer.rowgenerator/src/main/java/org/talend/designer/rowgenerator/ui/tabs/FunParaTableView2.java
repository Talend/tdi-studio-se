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

import java.util.List;

import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.talend.commons.ui.runtime.expressionbuilder.IExpressionBuilderDialogController;
import org.talend.commons.ui.runtime.swt.tableviewer.behavior.ColumnCellModifier;
import org.talend.commons.ui.runtime.swt.tableviewer.celleditor.CellEditorDialogBehavior;
import org.talend.commons.ui.swt.advanced.dataeditor.AbstractDataTableEditorView;
import org.talend.commons.ui.swt.advanced.dataeditor.ExtendedToolbarView;
import org.talend.commons.ui.swt.proposal.ExtendedTextCellEditorWithProposal;
import org.talend.commons.ui.swt.tableviewer.TableViewerCreator;
import org.talend.commons.ui.swt.tableviewer.TableViewerCreatorColumn;
import org.talend.commons.utils.data.bean.IBeanPropertyAccessors;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.IService;
import org.talend.core.model.process.INode;
import org.talend.core.runtime.services.IExpressionBuilderDialogService;
import org.talend.core.ui.proposal.TalendProposalProvider;
import org.talend.designer.rowgenerator.data.Function;
import org.talend.designer.rowgenerator.data.ListParameter;
import org.talend.designer.rowgenerator.data.Parameter;
import org.talend.designer.rowgenerator.i18n.Messages;
import org.talend.designer.rowgenerator.ui.editor.MetadataColumnExt;
import org.talend.designer.rowgenerator.ui.editor.MetadataTableEditorViewExt;

/**
 * qzhang class global comment. Detailled comment <br/>
 *
 * $Id: talend-code-templates.xml 1 2007-2-4 下午12:09:57 (星期五, 29 九月 2006) qzhang $
 *
 */
public class FunParaTableView2 extends AbstractDataTableEditorView<Parameter> {

    private static final String VALUE_PROPERTY = "Value"; //$NON-NLS-1$

    private MetadataTableEditorViewExt rowGenTableEditor2;

    private IExpressionBuilderDialogController dialog;

    private TableViewerCreator<Parameter> tableViewerCreator;

    private INode component;

    public FunParaTableView2(Composite parentComposite, int mainCompositeStyle, INode component) {
        super(parentComposite, mainCompositeStyle, false);
        this.component = component;
        initGraphicComponents();
    }

    public FunParaTableView2(Composite inEditorContainer, int border, MetadataTableEditorViewExt genTableEditor2, INode component) {
        this(inEditorContainer, border, component);
        this.rowGenTableEditor2 = genTableEditor2;
    }

    private ExtendedTextCellEditorWithProposal cellEditor;

    public void notifyOkPressed() {
        cellEditor.focusLost();
    }

    @Override
    protected void createColumns(TableViewerCreator<Parameter> tableViewerCreator, final Table table) {
        this.tableViewerCreator = tableViewerCreator;

        IService expressionBuilderDialogService = GlobalServiceRegister.getDefault().getService(
                IExpressionBuilderDialogService.class);

        TableViewerCreatorColumn column;
        column = new TableViewerCreatorColumn(tableViewerCreator);
        column.setTitle(Messages.getString("FunParaTableView2.Parameter")); //$NON-NLS-1$
        column.setBeanPropertyAccessors(new IBeanPropertyAccessors<Parameter, Object>() {

            public String get(Parameter bean) {
                return bean.getName();
            }

            public void set(Parameter bean, Object value) {
            }

        });
        column.setModifiable(false);
        column.setWidth(115);
        // column.setCellEditor(new
        // TextCellEditor(tableViewerCreator.getTable()));
        // ////////////////////////////////////////////////////////
        column = new TableViewerCreatorColumn(tableViewerCreator);
        column.setTitle(Messages.getString("FunParaTableView2.Value")); //$NON-NLS-1$
        column.setId(VALUE_PROPERTY);

        CellEditorDialogBehavior behavior = new CellEditorDialogBehavior();
        cellEditor = new ExtendedTextCellEditorWithProposal(tableViewerCreator.getTable(), SWT.MULTI | SWT.BORDER, column,
                behavior);

        column.setBeanPropertyAccessors(new IBeanPropertyAccessors<Parameter, Object>() {

            public String get(Parameter bean) {
                StringBuffer id = new StringBuffer();
                id.append(component.getLabel() + "=>"); //$NON-NLS-1$

                TableItem[] item = rowGenTableEditor2.getTable().getSelection();
                if (item.length == 1) {
                    id.append(((MetadataColumnExt) item[0].getData()).getLabel() + "=>"); //$NON-NLS-1$
                }

                cellEditor.setOwnerId(id.append(bean.getName()).toString());

                cellEditor.setExpressionType(bean.getType());
                String valueTemp = bean.getValue();
                if (valueTemp != null && !valueTemp.endsWith(" ")) {
                    bean.setValue(valueTemp + " ");
                }
                return bean.getValue();
            }

            public void set(Parameter bean, Object value) {

                if (value == null) {
                    return;
                }
                String valueTemp = value.toString();
                if (!valueTemp.endsWith(" ")) {
                    value = value + " ";
                }
                bean.setValue(value.toString());

                if (ext != null) {
                    ext.setChanged(true);
                }

                if (!rowGenTableEditor2.getTableViewerCreator().getTableViewer().isCellEditorActive()) {
                    rowGenTableEditor2.getTableViewerCreator().getTableViewer().refresh();
                }
            }

        });

        final ColumnCellModifier columnCellModifier = new ColumnCellModifier(column) {

            @Override
            public boolean canModify(Object bean) {
                return !rowGenTableEditor2.isReadOnly();
            }

        };
        column.setModifiable(true);
        column.setColumnCellModifier(columnCellModifier);
        column.setWidth(115);

        dialog = ((IExpressionBuilderDialogService) expressionBuilderDialogService).getExpressionBuilderInstance(mainComposite,
                cellEditor, component);
        behavior.setCellEditorDialog(dialog);

        cellEditor.setContentProposalProvider(getProcessProposals());
        cellEditor.setExpressionType("String"); //$NON-NLS-1$
        column.setCellEditor(cellEditor);

        // ////////////////////////////////////////////////////////

        column = new TableViewerCreatorColumn(tableViewerCreator);
        column.setTitle(Messages.getString("FunParaTableView2.Comment")); //$NON-NLS-1$
        column.setBeanPropertyAccessors(new IBeanPropertyAccessors<Parameter, Object>() {

            public String get(Parameter bean) {
                return bean.getComment();
            }

            public void set(Parameter bean, Object value) {
            }

        });
        column.setModifiable(false);
        column.setWidth(155);
        // column.setCellEditor(new
        // TextCellEditor(tableViewerCreator.getTable()));
        // ////////////////////////////////////////////////////////

    }

    @Override
    protected ExtendedToolbarView initToolBar() {
        return null;
    }

    private MetadataColumnExt ext;

    @SuppressWarnings("unchecked")
    public void update(MetadataColumnExt ext) {
        this.ext = ext;
        Function function = ext.getFunction();
        setTitle(function.getDescription());
        updateData(function.getParameters());
    }

    private void updateData(List<Parameter> params) {

        final Table table = this.getTable();
        final TableViewer viewer = this.getTableViewerCreator().getTableViewer();

        final TableEditor editor = new TableEditor(table);
        // The editor must have the same size as the cell and must
        // editing the Third column
        final int eDITABLECOLUMN = 2;
        table.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                // Clean up any previous editor control
                Control oldEditor = editor.getEditor();
                if (oldEditor != null) {
                    oldEditor.dispose();
                }
                // Identify the selected row
                TableItem item = (TableItem) e.item;
                if (item == null) {
                    return;
                }
                Parameter param = (Parameter) item.getData();

                if (param instanceof ListParameter) {
                    createCombo((ListParameter) param, item);
                }
            }

            private void createCombo(final ListParameter list, TableItem item) {
                final CCombo combo = new CCombo(table, SWT.NONE);

                combo.setItems(list.getValues());

                combo.setFocus();
                editor.setEditor(combo, item, eDITABLECOLUMN);
                combo.setText(list.getValue());
                Point size = combo.computeSize(SWT.DEFAULT, table.getItemHeight());
                // Set attributes of the editor
                editor.horizontalAlignment = SWT.LEFT;
                editor.minimumHeight = size.y;
                editor.minimumWidth = size.x;
                combo.addSelectionListener(new SelectionAdapter() {

                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        list.setValue(combo.getText());
                        viewer.refresh(list);
                        ext.setChanged(true);
                        rowGenTableEditor2.getTableViewerCreator().getTableViewer().refresh();
                    }

                });
                combo.addFocusListener(new FocusListener() {

                    public void focusGained(FocusEvent e) {

                    }

                    /**
                     * Sent when a control loses focus.
                     *
                     * @param e an event containing information about the focus change
                     */
                    public void focusLost(FocusEvent e) {
                        combo.dispose();
                    }
                });

            }

        });
        getTableViewerCreator().setInputList(params);
    }

    public void dispose() {
        if (getTable() != null) {
            getTable().dispose();
        }
    }

    /**
     *
     * ggu Comment method "getProcessProposals".
     *
     * @return IContentProposalProvider
     */
    private IContentProposalProvider getProcessProposals() {
        IContentProposalProvider processProposalProvider = new TalendProposalProvider(component.getProcess());
        return processProposalProvider;

    }

    protected void createLabelComposite(Composite mainComposite){
        if (!labelVisible) {
            return;
        }
        SashForm sash = new SashForm(mainComposite, SWT.VERTICAL);
        sash.setLayout(new GridLayout());
        sash.setLayoutData(new GridData(GridData.FILL_BOTH));
        Composite title = new Composite(sash, SWT.NONE);
        title.setLayout(new GridLayout());
        titleLabel = new Label(title, SWT.NONE);
        titleLabel.setLayoutData(new GridData(GridData.FILL_BOTH));
        if (parentComposite.getBackground() != null && !parentComposite.getBackground().equals(titleLabel.getBackground())) {
            titleLabel.setBackground(parentComposite.getBackground());
        }

        tableComposite = new Composite(sash, SWT.NONE);
        if (parentComposite.getBackground() != null && !parentComposite.getBackground().equals(tableComposite.getBackground())) {
            tableComposite.setBackground(parentComposite.getBackground());
        }
        tableComposite.setLayout(new GridLayout());

        sash.setSashWidth(6);
        sash.setWeights(new int[] { 1, 6 });
    }

}
