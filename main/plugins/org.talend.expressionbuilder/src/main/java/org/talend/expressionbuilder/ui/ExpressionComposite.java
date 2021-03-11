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

import java.util.List;

import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IEditorPart;
import org.talend.commons.runtime.model.expressionbuilder.Variable;
import org.talend.commons.ui.runtime.exception.MessageBoxExceptionHandler;
import org.talend.commons.ui.runtime.expressionbuilder.IExpressionDataBean;
import org.talend.designer.core.ui.viewer.ReconcilerViewer;
import org.talend.designer.core.ui.viewer.java.TalendJavaSourceViewer;
import org.talend.designer.rowgenerator.data.Function;
import org.talend.designer.rowgenerator.data.FunctionManager;
import org.talend.designer.rowgenerator.data.Parameter;
import org.talend.expressionbuilder.i18n.Messages;

/**
 * DOC yzhang class global comment. Detailled comment <br/>
 *
 */
public class ExpressionComposite extends Composite {

    private static final String TEXT_OPEN_SNIPPETS = "Open Snippets"; //$NON-NLS-1$

    private static final String TEXT_CLOSE_SNIPPETS = "Close Snippets"; //$NON-NLS-1$

    protected IDocument document;

    protected StyledText textControl;

    private String replacedText;

    protected ExpressionRecorder modificationRecord;

    protected ReconcilerViewer viewer;

    TrayDialog trayDialog = null;

    private IEditorPart editorPart;

    protected TextTransfer textTransfer = TextTransfer.getInstance();

    /**
     * DOC yzhang ExpressionComposite class global comment. Detailled comment <br/>
     *
     */
    class ButtonListener extends MouseAdapter {

        /*
         * (non-Javadoc)
         *
         * @see org.eclipse.swt.events.MouseAdapter#mouseUp(org.eclipse.swt.events.MouseEvent)
         */
        @Override
        public void mouseUp(MouseEvent e) {
            if (e.getSource() instanceof Button) {
                Button button = (Button) e.getSource();
                String buttonType = button.getText();
                Point sel = viewer.getSelectedRange();

                String textToInsert = buttonType;

                if (buttonType.equals("not")) { //$NON-NLS-1$
                    textToInsert = "!"; //$NON-NLS-1$
                } else if (buttonType.equals("and")) { //$NON-NLS-1$
                    textToInsert = "&&"; //$NON-NLS-1$
                } else if (buttonType.equals("or")) { //$NON-NLS-1$
                    textToInsert = "||"; //$NON-NLS-1$
                }
                try {
                    document.replace(sel.x, sel.y, textToInsert);
                    viewer.setSelectedRange(sel.x + textToInsert.length(), sel.y);
                    modificationRecord.pushRecored(textControl.getText());
                } catch (BadLocationException e1) {
                    MessageBoxExceptionHandler.process(e1);
                }
            }
        }
    }

    public ExpressionComposite(Composite parent, int style) {
        super(parent, style);
    }

    /**
     * Create the composite
     *
     * @param expressionBuilderDialog
     *
     * @param parent
     * @param style
     */
    public ExpressionComposite(TrayDialog expressionBuilderDialog, Composite parent, int style, IExpressionDataBean dataBean) {
        super(parent, style);
        setLayout(new FillLayout());
        this.trayDialog = expressionBuilderDialog;
        final Group expressionGroup = new Group(this, SWT.NONE);
        GridLayout groupLayout = new GridLayout();
        expressionGroup.setLayout(groupLayout);
        expressionGroup.setText(Messages.getString("ExpressionComposite.expression")); //$NON-NLS-1$

        final Composite upperOperationButtonBar = new Composite(expressionGroup, SWT.NONE);
        final GridLayout gridLayout = new GridLayout();
        gridLayout.horizontalSpacing = 8;
        gridLayout.numColumns = 3;
        gridLayout.marginTop = 0;
        gridLayout.marginBottom = 0;
        gridLayout.marginLeft = 0;
        gridLayout.marginRight = 0;
        gridLayout.marginWidth = 0;
        gridLayout.marginHeight = 0;
        upperOperationButtonBar.setLayout(gridLayout);
        upperOperationButtonBar.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.HORIZONTAL_ALIGN_END));
        upperOperationButtonBar.setData("nsd", null); //$NON-NLS-1$

        final Button wrapButton = new Button(upperOperationButtonBar, SWT.CHECK);
        wrapButton.setText("Wrap"); //$NON-NLS-1$
        wrapButton.setSelection(true);
        wrapButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                textControl.setWordWrap(wrapButton.getSelection());
            }

        });
        final Button undoButton = new Button(upperOperationButtonBar, SWT.NONE);
        undoButton.setText("Undo(Ctrl + Z)"); //$NON-NLS-1$
        undoButton.setEnabled(false);
        modificationRecord = new ExpressionRecorder(undoButton);
        undoButton.addMouseListener(new MouseAdapter() {

            /*
             * (non-Javadoc)
             *
             * @see org.eclipse.swt.events.MouseAdapter#mouseDown(org.eclipse.swt.events.MouseEvent)
             */
            @Override
            public void mouseDown(MouseEvent e) {
                undoOperation();
            }

        });

        // final Button wrapButton = new Button(upperOperationButtonBar, SWT.NONE);
        // wrapButton.setText("Wrap");

        final Button clearButton = new Button(upperOperationButtonBar, SWT.NONE);
        clearButton.setText(Messages.getString("ExpressionComposite.clear")); //$NON-NLS-1$

        clearButton.addMouseListener(new MouseAdapter() {

            /*
             * (non-Javadoc)
             *
             * @see org.eclipse.swt.events.MouseAdapter#mouseUp(org.eclipse.swt.events.MouseEvent)
             */
            @Override
            public void mouseUp(MouseEvent e) {
                IRegion region = viewer.getViewerRegion();
                try {
                    document.replace(region.getOffset(), region.getLength(), ""); //$NON-NLS-1$
                } catch (BadLocationException ex) {
                    MessageBoxExceptionHandler.process(ex);
                }
            }
        });

        // ColorManager colorManager = new ColorManager(CorePlugin.getDefault().getPreferenceStore());
        Composite composite = new Composite(expressionGroup, SWT.BORDER);
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));
        GridLayout layout = new GridLayout();
        layout.marginBottom = 0;
        layout.marginTop = 0;
        layout.marginLeft = 0;
        layout.marginRight = 0;
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        composite.setLayout(layout);
        // text = new ColorStyledText(composite, SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL, colorManager,
        // LanguageManager.getCurrentLanguage().getName());
        viewer = TalendJavaSourceViewer.createViewerWithVariables(composite, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL
                | SWT.WRAP, dataBean);

        textControl = viewer.getTextWidget();
        // int ops = DND.DROP_COPY | DND.DROP_MOVE;
        // DropTargetListener dropLisenter = new SnippetDropTargetListener(viewer, null, null, null);
        // viewer.addDropSupport(ops, new Transfer[] { LocalSelectionTransfer.getTransfer() }, dropLisenter);

        document = viewer.getDocument();
        textControl.setWordWrap(wrapButton.getSelection());
        textControl.setLayoutData(new GridData(GridData.FILL_BOTH));
        textControl.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {

                String content = getExpression();
                modificationRecord.pushRecored(content);
                Point cursorPos = textControl.getSelection();
                modificationRecord.setCursorPosition(cursorPos);
            }

        });

        // gcui create DND in Expression Editor.
        // This is ExpressionComposite drop.

        DropTarget target = new DropTarget(textControl, DND.DROP_DEFAULT | DND.DROP_COPY);
        target.setTransfer(new Transfer[] { textTransfer });
        target.addDropListener(new DropTargetListener() {

            @Override
            public void dragEnter(DropTargetEvent event) {

                if (event.detail == DND.DROP_DEFAULT) {
                    event.detail = DND.DROP_COPY;
                }

            }

            @Override
            public void dragOver(DropTargetEvent event) {
                // event.feedback = DND.FEEDBACK_NONE;
            }

            @Override
            public void dragOperationChanged(DropTargetEvent event) {

                if (event.detail == DND.DROP_DEFAULT) {
                    event.detail = DND.DROP_COPY;
                }
            }

            @Override
            public void dragLeave(DropTargetEvent event) {
            }

            @Override
            public void dropAccept(DropTargetEvent event) {
            }

            @Override
            public void drop(DropTargetEvent event) {

                if (textTransfer.isSupportedType(event.currentDataType)) {
                    String str = (String) event.data;
                    modificationRecord.pushRecored(textControl.getText() + str);
                    ExpressionComposite expressionComposite = ExpressionBuilderDialog.getExpressionComposite();
                    expressionComposite.setExpression(str, true);
                }
            }
        });

        final Composite lowerOperationButtonBar = new Composite(expressionGroup, SWT.NONE);
        final GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
        lowerOperationButtonBar.setLayoutData(gridData);
        lowerOperationButtonBar.setLayout(new RowLayout(SWT.HORIZONTAL));

        ButtonListener buttonListener = new ButtonListener();
        final Button plusButton = new Button(lowerOperationButtonBar, SWT.NONE);
        plusButton.setText("+"); //$NON-NLS-1$
        plusButton.addMouseListener(buttonListener);

        final Button minusButton = new Button(lowerOperationButtonBar, SWT.NONE);
        minusButton.setText("-"); //$NON-NLS-1$
        minusButton.addMouseListener(buttonListener);

        final Button multiplyButton = new Button(lowerOperationButtonBar, SWT.NONE);
        multiplyButton.setText("*"); //$NON-NLS-1$
        multiplyButton.addMouseListener(buttonListener);

        final Button divideButton = new Button(lowerOperationButtonBar, SWT.NONE);
        divideButton.setText("/"); //$NON-NLS-1$
        divideButton.addMouseListener(buttonListener);

        final Label label = new Label(lowerOperationButtonBar, SWT.NONE);
        final RowData rowData = new RowData();
        rowData.width = 20;
        label.setLayoutData(rowData);

        final Button eqButton = new Button(lowerOperationButtonBar, SWT.NONE);
        eqButton.setText("=="); //$NON-NLS-1$
        eqButton.addMouseListener(buttonListener);

        final Button gButton = new Button(lowerOperationButtonBar, SWT.NONE);
        gButton.setText("<"); //$NON-NLS-1$
        gButton.addMouseListener(buttonListener);

        final Button gebutton = new Button(lowerOperationButtonBar, SWT.NONE);
        gebutton.setText("<="); //$NON-NLS-1$
        gebutton.addMouseListener(buttonListener);

        final Button nebutton = new Button(lowerOperationButtonBar, SWT.NONE);
        nebutton.setText("!="); //$NON-NLS-1$
        nebutton.addMouseListener(buttonListener);

        final Button lebutton = new Button(lowerOperationButtonBar, SWT.NONE);
        lebutton.setText(">="); //$NON-NLS-1$
        lebutton.addMouseListener(buttonListener);

        final Button lButton = new Button(lowerOperationButtonBar, SWT.NONE);
        lButton.setText(">"); //$NON-NLS-1$
        lButton.addMouseListener(buttonListener);

        final Label label1 = new Label(lowerOperationButtonBar, SWT.NONE);
        final RowData rowData1 = new RowData();
        rowData1.width = 20;
        label1.setLayoutData(rowData1);

        final Button andButton = new Button(lowerOperationButtonBar, SWT.NONE);
        andButton.setText("and"); //$NON-NLS-1$
        andButton.addMouseListener(buttonListener);

        final Button orButton = new Button(lowerOperationButtonBar, SWT.NONE);
        orButton.setText("or"); //$NON-NLS-1$
        orButton.addMouseListener(buttonListener);

        final Button notButton = new Button(lowerOperationButtonBar, SWT.NONE);
        notButton.setText("not"); //$NON-NLS-1$
        notButton.addMouseListener(buttonListener);

        final Label label2 = new Label(lowerOperationButtonBar, SWT.NONE);
        final RowData rowData2 = new RowData();
        rowData2.width = 20;
        label2.setLayoutData(rowData2);

        final Button leftBracketButton = new Button(lowerOperationButtonBar, SWT.NONE);
        leftBracketButton.setText("("); //$NON-NLS-1$
        leftBracketButton.addMouseListener(buttonListener);

        final Button rightBracketbutton = new Button(lowerOperationButtonBar, SWT.NONE);
        rightBracketbutton.setText(")"); //$NON-NLS-1$
        rightBracketbutton.addMouseListener(buttonListener);

    }

    /**
     * yzhang Comment method "undoOperation".
     */
    public void undoOperation() {

        modificationRecord.undo();
        setExpression(modificationRecord.popRecored(), false);
        modificationRecord.undoFinished();
        if (modificationRecord.getCursorPosition() != null) {
            textControl.setSelection(modificationRecord.getCursorPosition());
        }
    }

    public static final String PERL_FUN_PREFIX = "sub{"; //$NON-NLS-1$

    public static final String PERL_FUN_SUFFIX = ")}"; //$NON-NLS-1$

    public static final String FUN_PARAM_SEPARATED = ","; //$NON-NLS-1$

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.expressionbuilder.ui.ExpressionController#setExpression(org.talend.designer.rowgenerator.data.Function
     * )
     */
    public void setExpression(Function f) {
        String newValue = PERL_FUN_PREFIX;
        if (f != null) {

            final List<Parameter> parameters = f.getParameters();
            if (FunctionManager.isJavaProject()) {
                String fullName = f.getName();
                newValue = fullName + "("; //$NON-NLS-1$
                for (Parameter pa : parameters) {
                    newValue += pa.getValue() + FUN_PARAM_SEPARATED;
                }
                if (!parameters.isEmpty()) {
                    newValue = newValue.substring(0, newValue.length() - 1);
                }
                newValue += ")"; //$NON-NLS-1$

            } else {
                newValue += f.getName() + "("; //$NON-NLS-1$
                for (Parameter pa : parameters) {
                    newValue += pa.getValue() + FUN_PARAM_SEPARATED;
                }
                newValue = newValue.substring(0, newValue.length() - 1);
                newValue += PERL_FUN_SUFFIX;
            }
        }
        IRegion region = viewer.getViewerRegion();
        try {
            document.replace(region.getOffset(), region.getLength(), newValue);
        } catch (BadLocationException e) {
            MessageBoxExceptionHandler.process(e);
        }
    }

    /**
     * yzhang Comment method "getExpression".
     *
     * @return
     */
    public String getReplaceExpression() {
        if (document != null) {
            IRegion region = viewer.getViewerRegion();
            try {
                String doc = document.get(region.getOffset(), region.getLength());
                List<Variable> variables = ExpressionBuilderDialog.getTestComposite().getVariableList();
                for (Variable variable : variables) {
                    doc = doc.replaceAll(variable.getName(), variable.getValue());
                }
                return doc;
                // return document.get(region.getOffset(), region.getLength());
            } catch (BadLocationException e) {
                MessageBoxExceptionHandler.process(e);
            }
        }
        return null;
    }

    public String getExpression() {
        if (document != null) {
            IRegion region = viewer.getViewerRegion();
            try {
                return document.get(region.getOffset(), region.getLength());
            } catch (BadLocationException e) {
                MessageBoxExceptionHandler.process(e);
            }
        }
        return null;
    }

    /**
     * yzhang Comment method "setExpression".
     *
     * @param expression
     */
    public void setExpression(String expression, boolean append) {
        if (document != null) {
            if (append) {
                Point sel = viewer.getSelectedRange();
                try {
                    document.replace(sel.x, sel.y, expression);
                } catch (BadLocationException e) {
                    MessageBoxExceptionHandler.process(e);
                }
            } else {
                IRegion region = viewer.getViewerRegion();
                try {
                    document.replace(region.getOffset(), region.getLength(), expression);
                } catch (BadLocationException e) {
                    MessageBoxExceptionHandler.process(e);
                }
            }
        }
    }

    /**
     * Sets the replacedText.
     *
     * @param replacedText the replacedText to set
     */
    public void setReplacedText(String replacedText) {
        this.replacedText = replacedText;
    }

    /**
     * yzhang Comment method "replacedContent".
     *
     * @param content
     * @param position
     */
    public void replacedContent(String content, Point position) {
        if (replacedText.startsWith("*")) { //$NON-NLS-1$
            textControl.setSelection(position.x, position.x);
        } else {
            textControl.setSelection(position.x - replacedText.length(), position.x);
        }

        Point sel = viewer.getSelectedRange();

        try {
            document.replace(sel.x, sel.y, content);
        } catch (BadLocationException e1) {
            MessageBoxExceptionHandler.process(e1);
        }
    }

    public void insertExpression(String str) {
        textControl.insert(str);
    }
}
