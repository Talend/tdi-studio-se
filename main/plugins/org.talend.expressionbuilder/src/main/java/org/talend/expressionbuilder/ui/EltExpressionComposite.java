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
package org.talend.expressionbuilder.ui;

import java.util.List;

import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.swt.SWT;
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
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.talend.commons.ui.runtime.exception.MessageBoxExceptionHandler;
import org.talend.commons.ui.runtime.expressionbuilder.IExpressionDataBean;
import org.talend.designer.core.ui.viewer.java.TalendJavaSourceViewer;
import org.talend.designer.rowgenerator.data.Function;
import org.talend.designer.rowgenerator.data.FunctionManager;
import org.talend.designer.rowgenerator.data.Parameter;
import org.talend.expressionbuilder.i18n.Messages;

/**
 * DOC hcyi class global comment. Detailled comment
 */
public class EltExpressionComposite extends ExpressionComposite {

    private IExpressionDataBean dataBean;

    public EltExpressionComposite(TrayDialog expressionBuilderDialog, Composite parent, int style, IExpressionDataBean dataBean) {
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
        viewer = TalendJavaSourceViewer.createViewerWithVariables4Elt(composite,
                SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL | SWT.WRAP, dataBean);

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


    }

    @Override
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
        setExpression(newValue, true);
    }

    @Override
    public void setExpression(String expression, boolean append) {
        if (append) {
            Point sel = textControl.getSelectionRange();
            textControl.replaceTextRange(sel.x, sel.y, expression != null ? expression : "");
        } else {
            textControl.replaceTextRange(0, textControl.getCharCount(), expression != null ? expression : "");
        }
    }

    @Override
    public String getExpression() {
        return textControl.getText();
    }

}
