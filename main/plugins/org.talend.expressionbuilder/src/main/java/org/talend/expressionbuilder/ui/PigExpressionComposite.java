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

import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.IControlContentAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
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
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.talend.commons.ui.runtime.expressionbuilder.IExpressionDataBean;
import org.talend.commons.ui.swt.colorstyledtext.ColorStyledText;
import org.talend.commons.ui.swt.preferences.HotKeyUtil;
import org.talend.commons.ui.swt.proposal.ContentProposalAdapterExtended;
import org.talend.commons.ui.swt.proposal.ProposalUtils;
import org.talend.commons.ui.swt.proposal.StyledTextContentAdapter;
import org.talend.commons.utils.generation.JavaUtils;
import org.talend.core.CorePlugin;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.language.LanguageManager;
import org.talend.core.service.IPigMapService;
import org.talend.designer.rowgenerator.data.Function;
import org.talend.designer.rowgenerator.data.FunctionManager;
import org.talend.designer.rowgenerator.data.Parameter;
import org.talend.expressionbuilder.i18n.Messages;

/**
 * DOC hcyi class global comment. Detailled comment
 */
public class PigExpressionComposite extends ExpressionComposite {

    private IExpressionDataBean dataBean;

    public PigExpressionComposite(TrayDialog expressionBuilderDialog, Composite parent, int style, IExpressionDataBean dataBean) {
        super(parent, style);
        setLayout(new FillLayout());
        this.trayDialog = expressionBuilderDialog;
        this.dataBean = dataBean;
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
                textControl.replaceTextRange(0, textControl.getCharCount(), ""); //$NON-NLS-1$
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
        textControl = new ColorStyledText(composite, SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL, CorePlugin.getDefault()
                .getPreferenceStore(), LanguageManager.getCurrentLanguage().getName());
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
        createEditorProposal();
        // create DND in Expression Editor , This is ExpressionComposite drop.
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
                    ExpressionComposite expressionComposite = PigExpressionBuilderDialog.getExpressionComposite();
                    if (str != null && str.endsWith("()") && !str.startsWith(JavaUtils.JAVA_PIGUDF_DIRECTORY + ".")) {
                        expressionComposite.setExpression(str.substring(str.indexOf(".") + 1), true);
                    } else {
                        expressionComposite.setExpression(str, true);
                    }
                }
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

    /**
     * Creates proposal for editor.
     */
    private void createEditorProposal() {
        try {
            // create KeyStroke use Ctrl+Space as default
            KeyStroke keyStroke = HotKeyUtil.getHotKey(HotKeyUtil.contentAssist);
            IControlContentAdapter controlContentAdapter = new StyledTextContentAdapter();
            // TDI-25309 :expression builder UDF autocompletion need add the inputTable node
            if (GlobalServiceRegister.getDefault().isServiceRegistered(IPigMapService.class)) {
                final IPigMapService service = (IPigMapService) GlobalServiceRegister.getDefault().getService(
                        IPigMapService.class);
                ContentProposalAdapterExtended proposalAdaptor = ProposalUtils.getCommonProposal(textControl,
                        service.createExpressionProposalProvider(dataBean));
                new ContentProposalAdapter(textControl, controlContentAdapter,
                        service.createExpressionProposalProvider(dataBean), keyStroke, new char[] { ' ', '.' });
            }

        } catch (Exception e) {
            //
        }
    }
}
