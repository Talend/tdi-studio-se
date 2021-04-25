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
package org.talend.expressionbuilder.ui;

import java.util.LinkedList;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.TableColumn;
import org.talend.commons.runtime.model.expressionbuilder.Variable;
import org.talend.expressionbuilder.i18n.Messages;
import org.talend.expressionbuilder.test.JavaTestShadow;

/**
 *
 * DOC hcyi class global comment. Detailled comment
 */
public class PigInputTableComposite extends TestComposite {

    public PigInputTableComposite(Composite parent, int style) {
        super(parent, style, null);
        setLayout(new FillLayout());

        final Group group = new Group(this, SWT.NONE);
        group.setLayout(new GridLayout());
        group.setText(Messages.getString("PigTestComposite.InputTable")); //$NON-NLS-1$

        final SashForm sashForm = new SashForm(group, SWT.NONE);
        sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        Composite tablePart = new Composite(sashForm, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.verticalSpacing = 0;
        layout.marginTop = 0;
        layout.marginHeight = 0;
        tablePart.setLayout(layout);

        variableTableViewer = new TableViewer(tablePart, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
        VariableTableViewerProvider provider = new VariableTableViewerProvider();
        variableTableViewer.setContentProvider(provider);
        variableTableViewer.setLabelProvider(provider);
        table = variableTableViewer.getTable();
        variableTableViewer.setCellEditors(new CellEditor[] { new DoubleClickTextCellEditor(table), new TextCellEditor(table) });
        variableTableViewer.setColumnProperties(new String[] { NAME_PROPERTY, VALUE_PROPERTY });
        variableTableViewer.setInput(new LinkedList<Variable>());

        variableTableViewer.addDoubleClickListener(new IDoubleClickListener() {

            public void doubleClick(DoubleClickEvent event) {
                Variable var = (Variable) (((IStructuredSelection) event.getSelection()).getFirstElement());
                ExpressionComposite expressionComposite = PigExpressionBuilderDialog.getExpressionComposite();
                expressionComposite.setExpression(var.getName() + "." + var.getValue(), true);
            }

        });

        DragSource source = new DragSource(table, DND.DROP_MOVE | DND.DROP_COPY);
        source.setTransfer(new Transfer[] { textTransfer });
        source.addDragListener(new DragSourceListener() {

            public void dragStart(DragSourceEvent event) {
                if (table.getSelectionCount() == 0)
                    event.doit = false;
            }

            public void dragSetData(DragSourceEvent event) {
                if (textTransfer.isSupportedType(event.dataType)) {
                    Variable var = (Variable) table.getSelection()[0].getData();
                    event.data = var.getName() + "." + var.getValue();
                }
            }

            public void dragFinished(DragSourceEvent event) {

            }
        });

        table.setHeaderVisible(true);
        GridData gd = new GridData(GridData.FILL_BOTH);
        gd.horizontalSpan = 2;
        table.setLayoutData(gd);

        final TableColumn varColumn = new TableColumn(table, SWT.NONE);
        varColumn.setWidth(100);
        varColumn.setText(Messages.getString("PigTestComposite.tableName")); //$NON-NLS-1$

        final TableColumn valueColumn = new TableColumn(table, SWT.NONE);
        valueColumn.setWidth(130);
        valueColumn.setText(Messages.getString("PigTestComposite.column")); //$NON-NLS-1$

        sashForm.setWeights(new int[] { 1 });
        shadow = new JavaTestShadow();
    }
}
