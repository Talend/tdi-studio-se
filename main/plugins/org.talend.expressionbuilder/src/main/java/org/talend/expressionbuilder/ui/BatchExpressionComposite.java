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
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.talend.commons.ui.runtime.expressionbuilder.IExpressionDataBean;
import org.talend.commons.ui.swt.colorstyledtext.ColorStyledText;
import org.talend.commons.ui.swt.preferences.HotKeyUtil;
import org.talend.commons.ui.swt.proposal.ContentProposalAdapterExtended;
import org.talend.commons.ui.swt.proposal.ProposalUtils;
import org.talend.commons.ui.swt.proposal.StyledTextContentAdapter;
import org.talend.core.CorePlugin;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.language.LanguageManager;
import org.talend.core.service.IPigMapService;
import org.talend.designer.rowgenerator.data.Function;
import org.talend.designer.rowgenerator.data.FunctionManager;
import org.talend.designer.rowgenerator.data.Parameter;
import org.talend.expressionbuilder.i18n.Messages;

/**
 * created by hcyi on Feb 15, 2017 Detailled comment
 *
 */
public class BatchExpressionComposite extends ExpressionComposite {

    private IExpressionDataBean dataBean;

    public BatchExpressionComposite(TrayDialog expressionBuilderDialog, Composite parent, int style, IExpressionDataBean dataBean) {
        super(parent, style);
        setLayout(new FillLayout());
        this.trayDialog = expressionBuilderDialog;
        this.dataBean = dataBean;
        final Group expressionGroup = new Group(this, SWT.NONE);
        GridLayout groupLayout = new GridLayout();
        expressionGroup.setLayout(groupLayout);
        expressionGroup.setText(Messages.getString("ExpressionComposite.expression")); //$NON-NLS-1$

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
        textControl = new ColorStyledText(composite, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL, CorePlugin.getDefault()
                .getPreferenceStore(), LanguageManager.getCurrentLanguage().getName());
        textControl.setLayoutData(new GridData(GridData.FILL_BOTH));
        textControl.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
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
                    ExpressionComposite expressionComposite = ExpressionBuilderDialog.getExpressionComposite();
                    expressionComposite.setExpression(str, true);
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
