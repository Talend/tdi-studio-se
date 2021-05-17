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
package org.talend.designer.core.generic.controller;

import org.eclipse.gef.commands.Command;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.utils.ContextParameterUtils;
import org.talend.core.ui.CoreUIPlugin;
import org.talend.core.ui.properties.tab.IDynamicProperty;
import org.talend.core.utils.TalendQuoteUtils;
import org.talend.designer.core.generic.i18n.Messages;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.ui.editor.cmd.PropertyChangeCommand;
import org.talend.designer.core.ui.editor.properties.controllers.TextController;

/**
 * created by hcyi on Feb 19, 2016 Detailled comment
 *
 */
public class GenericHiddenTextController extends TextController {

    protected static final String HIDDEN_TEXT = "HIDDEN_TEXT"; //$NON-NLS-1$

    public GenericHiddenTextController(IDynamicProperty dp) {
        super(dp);
    }

    @Override
    protected boolean isPasswordParam(IElementParameter parameter) {
        String value = String.valueOf(parameter.getValue());
        if (ContextParameterUtils.containContextVariables(value)) {
            return false;
        }
        return TalendQuoteUtils.isCommonString(value) || isInWizard();
    }

    @Override
    protected boolean isReadOnly() {
        return !isInWizard();
    }

    @Override
    public Control createControl(Composite subComposite, IElementParameter param, int numInRow, int nbInRow, int top,
            Control lastControl) {
        Control lastControlUsed = super.createControl(subComposite, param, numInRow, nbInRow, top, lastControl);
        Text labelText = (Text) hashCurControls.get(param.getName());
        if (labelText != null && param != null && isPasswordParam(param)) {
            labelText.setEchoChar('*');
        }
        FormData data = (FormData) lastControlUsed.getLayoutData();
        if (!param.isRepositoryValueUsed() && !isInWizard()) {
            data.right = new FormAttachment((numInRow * MAX_PERCENT) / nbInRow, -STANDARD_BUTTON_WIDTH);
            lastControlUsed = addButton(subComposite, param, lastControlUsed, numInRow, nbInRow, top);
        }
        return lastControlUsed;
    }

    protected Control addButton(Composite subComposite, IElementParameter param, Control lastControl, int numInRow, int nbInRow,
            int top) {
        Button btn;
        Control lastControlUsed = lastControl;
        Point btnSize;
        FormData data;

        btn = getWidgetFactory().createButton(subComposite, "", SWT.PUSH); //$NON-NLS-1$
        btnSize = btn.computeSize(SWT.DEFAULT, SWT.DEFAULT);

        btn.setImage(ImageProvider.getImage(CoreUIPlugin.getImageDescriptor(DOTS_BUTTON)));

        btn.addSelectionListener(listenerSelection);
        btn.setData(NAME, HIDDEN_TEXT);
        btn.setData(PARAMETER_NAME, param.getName());

        lastControlUsed = btn;

        data = new FormData();
        // data.right = new FormAttachment(lastControl, -5, SWT.LEFT);
        // data.left = new FormAttachment(lastControl, -(15 + STANDARD_BUTTON_WIDTH), SWT.LEFT);
        // data.right = new FormAttachment(((numInRow * MAX_PERCENT) / nbInRow), 0);
        // data.left = new FormAttachment(((numInRow * MAX_PERCENT) / nbInRow), -STANDARD_BUTTON_WIDTH);
        data.left = new FormAttachment(lastControl, 0);
        data.right = new FormAttachment(lastControl, STANDARD_BUTTON_WIDTH, SWT.RIGHT);
        data.top = new FormAttachment(0, top);

        data.height = STANDARD_HEIGHT - 2;
        btn.setLayoutData(data);

        // dynamicProperty.setCurRowSize(btnSize.y + ITabbedPropertyConstants.VSPACE);
        int buttonHeight = btnSize.y + ITabbedPropertyConstants.VSPACE;
        if (dynamicProperty.getCurRowSize() < buttonHeight) {
            dynamicProperty.setCurRowSize(buttonHeight);
        }
        return lastControlUsed;
    }

    protected Command createButtonCommand(final Button button) {
        if (button.getData(NAME).equals(HIDDEN_TEXT)) {
            InputDialog dlg = new InputDialog(
                    button.getShell(),
                    Messages.getString("GenericHiddenTextController.NewPassword"), Messages.getString("GenericHiddenTextController.NoteConvention"), "\"\"", null) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

                /*
                 * (non-Javadoc)
                 *
                 * @see org.eclipse.jface.dialogs.InputDialog#createDialogArea(org.eclipse.swt.widgets.Composite)
                 */
                @Override
                protected Control createDialogArea(Composite parent) {
                    Control control = super.createDialogArea(parent);
                    String paramName = (String) button.getData(PARAMETER_NAME);
                    getText().setData(PARAMETER_NAME, paramName);
                    editionControlHelper.register(paramName, getText());
                    return control;
                }
            };
            if (dlg.open() == Window.OK) {
                String paramName = (String) button.getData(PARAMETER_NAME);
                elem.setPropertyValue(EParameterName.UPDATE_COMPONENTS.getName(), new Boolean(true));
                return new PropertyChangeCommand(elem, paramName, dlg.getValue());
            }
        }

        return null;
    }

    SelectionListener listenerSelection = new SelectionListener() {

        @Override
        public void widgetDefaultSelected(SelectionEvent e) {
            // do nothing.
        }

        @Override
        public void widgetSelected(SelectionEvent e) {
            Command cmd = createCommand(e);
            executeCommand(cmd);
            if (e.getSource() instanceof Button) {
                Button button = (Button) e.getSource();
                if (button.getData(NAME).equals(HIDDEN_TEXT)) {
                    String paramName = (String) button.getData(PARAMETER_NAME);
                    refresh(elem.getElementParameter(paramName), false);
                }
            }
        }
    };

    private Command createCommand(SelectionEvent selectionEvent) {
        if (selectionEvent.getSource() instanceof Button) {
            return createButtonCommand((Button) selectionEvent.getSource());
        }
        return null;
    }
}
