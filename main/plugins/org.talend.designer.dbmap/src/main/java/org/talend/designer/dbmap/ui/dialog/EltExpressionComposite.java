// ============================================================================
//
// Copyright (C) 2006-2020 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.dbmap.ui.dialog;

import java.util.List;

import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.IControlContentAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.ui.runtime.exception.MessageBoxExceptionHandler;
import org.talend.commons.ui.runtime.expressionbuilder.IExpressionDataBean;
import org.talend.commons.ui.swt.colorstyledtext.ColorStyledText;
import org.talend.commons.ui.swt.preferences.HotKeyUtil;
import org.talend.commons.ui.swt.proposal.ContentProposalAdapterExtended;
import org.talend.commons.ui.swt.proposal.StyledTextContentAdapterExtended;
import org.talend.core.CorePlugin;
import org.talend.core.language.LanguageManager;
import org.talend.designer.dbmap.i18n.Messages;
import org.talend.designer.dbmap.managers.MapperManager;
import org.talend.designer.dbmap.ui.proposal.expression.ExpressionProposalProvider;
import org.talend.designer.dbmap.ui.visualmap.zone.Zone;
import org.talend.designer.rowgenerator.data.Function;
import org.talend.designer.rowgenerator.data.FunctionManager;
import org.talend.designer.rowgenerator.data.Parameter;
import org.talend.expressionbuilder.ui.ExpressionRecorder;

public class EltExpressionComposite extends ExpressionComposite {

    private MapperManager mapperManager;

    public EltExpressionComposite(TrayDialog expressionBuilderDialog, Composite parent, int style, IExpressionDataBean dataBean,
            MapperManager mapperManager) {
        super(parent, style);
        setLayout(new FillLayout());
        this.trayDialog = expressionBuilderDialog;
        this.mapperManager = mapperManager;
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
        wrapButton.setText(Messages.getString("ExpressionComposite.Wrap")); //$NON-NLS-1$
        wrapButton.setSelection(true);
        wrapButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                textControl.setWordWrap(wrapButton.getSelection());
            }

        });
        final Button undoButton = new Button(upperOperationButtonBar, SWT.NONE);
        undoButton.setText(Messages.getString("ExpressionComposite.undo") + "(Ctrl + Z)"); //$NON-NLS-1$
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
                // IRegion region = viewer.getViewerRegion();
                try {
                    textControl.setText("");
                    // document.replace(region.getOffset(), region.getLength(), ""); //$NON-NLS-1$
                } catch (Exception ex) {
                    MessageBoxExceptionHandler.process(ex);
                }
            }
        });

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
        textControl = new ColorStyledText(composite, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL,
                CorePlugin.getDefault().getPreferenceStore(), LanguageManager.getCurrentLanguage().getName());
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
            IControlContentAdapter controlContentAdapter = new StyledTextContentAdapterExtended();
            ExpressionProposalProvider contentProposalProvider = new ExpressionProposalProvider(mapperManager, null);
            contentProposalProvider.init(null, new Zone[] { Zone.INPUTS, Zone.OUTPUTS }, null);

            // create proposal page
            ContentProposalAdapterExtended contentProposalAdapter = new ContentProposalAdapterExtended(textControl,
                    controlContentAdapter,
                    contentProposalProvider, keyStroke, null);
            contentProposalAdapter.setPropagateKeys(true);
            contentProposalAdapter.setFilterStyle(ContentProposalAdapterExtended.FILTER_CUMULATIVE_ALL_START_WORDS);
            contentProposalAdapter.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_INSERT);
            contentProposalAdapter.setContentProposalProvider(contentProposalProvider);
            contentProposalAdapter.setAutoActivationDelay(10);
            contentProposalAdapter.setPopupSize(new Point(300, 300));

            // add listener to avoid input \r when try press enter to choose proposal
            textControl.addVerifyKeyListener(new VerifyKeyListener() {

                public void verifyKey(VerifyEvent verifyEvent) {
                    if (verifyEvent.character == '\r' && contentProposalAdapter != null
                            && contentProposalAdapter.isProposalOpened()) {
                        verifyEvent.doit = false;
                    } else {
                        verifyEvent.doit = true;
                    }
                }

            });
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }
    }
}
