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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.talend.commons.runtime.model.expressionbuilder.Variable;
import org.talend.expressionbuilder.i18n.Messages;
import org.talend.expressionbuilder.test.JavaTestShadow;
import org.talend.expressionbuilder.test.shadow.ExpressionTestMain;

/**
 * yzhang class global comment. Detailled comment <br/>
 *
 * $Id: TestComposite.java 上午10:14:03 2007-7-24 +0000 (2007-7-24) yzhang $
 *
 */
public class TestComposite extends Composite {

    protected Table table;

    protected Button buttonAddVar, buttonRemoveVar;

    protected TableViewer variableTableViewer;

    protected Button testButton;

    protected Button clearButton;

    protected StyledText testResultText;

    protected String NAME_PROPERTY = "Name"; //$NON-NLS-1$

    protected String VALUE_PROPERTY = "Value"; //$NON-NLS-1$

    protected JavaTestShadow shadow;

    protected TextTransfer textTransfer = TextTransfer.getInstance();

    /**
     *
     * DOC TestComposite constructor comment.
     *
     * @param parent
     * @param style
     * @param temp
     */
    public TestComposite(Composite parent, int style, String temp) {
        super(parent, style);
    }

    /**
     * Create the composite
     *
     * @param parent
     * @param style
     */
    public TestComposite(Composite parent, int style) {
        super(parent, style);
        setLayout(new FillLayout());

        final Group group = new Group(this, SWT.NONE);
        group.setLayout(new GridLayout());
        group.setText(Messages.getString("TestComposite.test")); //$NON-NLS-1$

        final Composite composite = new Composite(group, SWT.NONE);
        composite.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
        final FillLayout fillLayout = new FillLayout();
        fillLayout.spacing = 8;
        composite.setLayout(fillLayout);

        testButton = new Button(composite, SWT.NONE);
        testButton.setText(Messages.getString("TestComposite.test2")); //$NON-NLS-1$

        clearButton = new Button(composite, SWT.NONE);
        clearButton.setText(Messages.getString("TestComposite.clear")); //$NON-NLS-1$

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
        variableTableViewer.setCellModifier(new ICellModifier() {

            /*
             * (non-Javadoc)
             *
             * @see org.eclipse.jface.viewers.ICellModifier#canModify(java.lang.Object, java.lang.String)
             */
            public boolean canModify(Object element, String property) {
                return true;
            }

            /*
             * (non-Javadoc)
             *
             * @see org.eclipse.jface.viewers.ICellModifier#getValue(java.lang.Object, java.lang.String)
             */
            public Object getValue(Object element, String property) {
                if (NAME_PROPERTY.equals(property)) {
                    return ((Variable) element).getName();
                } else if (VALUE_PROPERTY.equals(property)) {
                    return ((Variable) element).getValue();
                }
                return ""; //$NON-NLS-1$
            }

            /*
             * (non-Javadoc)
             *
             * @see org.eclipse.jface.viewers.ICellModifier#modify(java.lang.Object, java.lang.String, java.lang.Object)
             */
            public void modify(Object element, String property, Object value) {
                Variable var = (Variable) ((TableItem) element).getData();
                if (NAME_PROPERTY.equals(property)) {
                    List<String> nameList = new ArrayList<String>();
                    TableItem[] items = table.getItems();
                    for (TableItem item : items) {
                        if (item == (TableItem) element) {
                            continue;
                        }
                        nameList.add(((Variable) item.getData()).getName());
                    }
                    if (nameList.contains(value)) {
                        MessageDialog.openError(getShell(), Messages.getString("TestComposite.error"), Messages //$NON-NLS-1$
                                .getString("TestComposite.message")); //$NON-NLS-1$
                        return;
                    } else {
                        var.setName((String) value);
                    }
                } else if (VALUE_PROPERTY.equals(property)) {
                    var.setValue((String) value);
                }
                variableTableViewer.refresh();
            }

        });
        table = variableTableViewer.getTable();
        variableTableViewer.setCellEditors(new CellEditor[] { new DoubleClickTextCellEditor(table), new TextCellEditor(table) });
        variableTableViewer.setColumnProperties(new String[] { NAME_PROPERTY, VALUE_PROPERTY });
        variableTableViewer.setInput(new LinkedList<Variable>());

        variableTableViewer.addDoubleClickListener(new IDoubleClickListener() {

            public void doubleClick(DoubleClickEvent event) {
                Variable var = (Variable) (((IStructuredSelection) event.getSelection()).getFirstElement());
                ExpressionComposite expressionComposite = ExpressionBuilderDialog.getExpressionComposite();
                expressionComposite.setExpression(var.getName(), true);
                expressionComposite.modificationRecord.pushRecored(expressionComposite.getExpression());
            }

        });

        // gcui create DND in Expression Editor.
        // This is TestComposite drag.

        DragSource source = new DragSource(table, DND.DROP_MOVE | DND.DROP_COPY);
        source.setTransfer(new Transfer[] { textTransfer });
        source.addDragListener(new DragSourceListener() {

            public void dragStart(DragSourceEvent event) {
                if (table.getSelectionCount() == 0)
                    event.doit = false;
            }

            public void dragSetData(DragSourceEvent event) {
                if (textTransfer.isSupportedType(event.dataType)) {
                    event.data = table.getSelection()[0].getText();
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
        varColumn.setText(Messages.getString("TestComposite.var")); //$NON-NLS-1$

        final TableColumn valueColumn = new TableColumn(table, SWT.NONE);
        valueColumn.setWidth(100);
        valueColumn.setText(Messages.getString("TestComposite.value")); //$NON-NLS-1$

        testResultText = new StyledText(sashForm, SWT.MULTI | SWT.BORDER | SWT.WRAP);

        Composite buttonPart = new Composite(tablePart, SWT.NONE);
        buttonPart.setLayout(new GridLayout(2, false));
        buttonPart.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));

        buttonAddVar = new Button(buttonPart, SWT.NONE);
        buttonAddVar.setText(Messages.getString("TestComposite.add")); //$NON-NLS-1$
        buttonAddVar.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));

        buttonRemoveVar = new Button(buttonPart, SWT.NONE);
        buttonRemoveVar.setText(Messages.getString("TestComposite.remove")); //$NON-NLS-1$
        buttonRemoveVar.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
        sashForm.setWeights(new int[] { 1, 1 });

        shadow = new JavaTestShadow();

        installListener();
        //
    }

    /**
     * DOC zhangyi TestComposite class global comment. Detailled comment <br/>
     *
     * $Id: talend.epf 1 2006-09-29 17:06:40Z nrousseau $
     *
     */
    protected class DoubleClickTextCellEditor extends TextCellEditor {

        public DoubleClickTextCellEditor(Composite parent) {
            super(parent);
            text.addMouseListener(new MouseAdapter() {

                /*
                 * (non-Javadoc)
                 *
                 * @see org.eclipse.swt.events.MouseAdapter#mouseDoubleClick(org.eclipse.swt.events.MouseEvent)
                 */
                @Override
                public void mouseDoubleClick(MouseEvent e) {
                    doubleClickOperation();
                }
            });
        }

        /**
         * DOC zhangyi Comment method "doubleClickOperation".
         */
        protected void doubleClickOperation() {
            String content = (String) doGetValue();
            ExpressionBuilderDialog.getExpressionComposite().insertExpression(content);

        }

    }

    private void installListener() {
        buttonAddVar.addMouseListener(new MouseAdapter() {

            /*
             * (non-Javadoc)
             *
             * @see org.eclipse.swt.events.MouseAdapter#mouseUp(org.eclipse.swt.events.MouseEvent)
             */
            @Override
            public void mouseUp(MouseEvent e) {
                List<Variable> input = (List<Variable>) variableTableViewer.getInput();
                input.add(new Variable(input.size()));
                variableTableViewer.refresh();
            }
        });

        buttonRemoveVar.addMouseListener(new MouseAdapter() {

            /*
             * (non-Javadoc)
             *
             * @see org.eclipse.swt.events.MouseAdapter#mouseUp(org.eclipse.swt.events.MouseEvent)
             */
            @Override
            public void mouseUp(MouseEvent e) {
                ISelection selection = variableTableViewer.getSelection();
                List<Variable> list = ((List<Variable>) variableTableViewer.getInput());

                if (!selection.isEmpty() && selection instanceof StructuredSelection) {
                    Object[] vars = ((StructuredSelection) selection).toArray();
                    for (Object var : vars) {
                        list.remove(var);
                        variableTableViewer.refresh();
                    }
                } else if (!list.isEmpty()) {
                    list.remove(list.size() - 1);
                    variableTableViewer.refresh();
                }
            }
        });

        testButton.addMouseListener(new MouseAdapter() {

            /*
             * (non-Javadoc)
             *
             * @see org.eclipse.swt.events.MouseAdapter#mouseUp(org.eclipse.swt.events.MouseEvent)
             */
            @Override
            public void mouseUp(MouseEvent e) {
                new ExpressionTestMain(CategoryComposite.getSelectedFunction(), testResultText);

            }
        });

        clearButton.addMouseListener(new MouseAdapter() {

            /*
             * (non-Javadoc)
             *
             * @see org.eclipse.swt.events.MouseAdapter#mouseUp(org.eclipse.swt.events.MouseEvent)
             */
            @Override
            public void mouseUp(MouseEvent e) {
                testResultText.setText(""); //$NON-NLS-1$
            }
        });

    }

    @Override
    public void dispose() {
        super.dispose();
    }

    /**
     * Stop the server socket.
     *
     * yzhang Comment method "stopServerThread".
     */
    public void stopServerThread() {
        shadow.stop();
    }

    @Override
    protected void checkSubclass() {
        // Disable the check that prevents subclassing of SWT components
    }

    /**
     * yzhang Comment method "getVariableList".
     */
    public List<Variable> getVariableList() {
        return (List<Variable>) variableTableViewer.getInput();
    }

    /**
     * yzhang Comment method "setVariableList".
     *
     * @param list
     */
    public void addVariables(List<Variable> list) {
        if (list != null) {
            ((List<Variable>) variableTableViewer.getInput()).addAll(list);
            variableTableViewer.refresh();
        }
    }

    /**
     * yzhang Comment method "setVariableList".
     *
     * @param list
     */
    public void setVariables(List<Variable> list) {
        if (list != null) {
            ((List<Variable>) variableTableViewer.getInput()).clear();
            ((List<Variable>) variableTableViewer.getInput()).addAll(list);
            variableTableViewer.refresh();
        }
    }

}
