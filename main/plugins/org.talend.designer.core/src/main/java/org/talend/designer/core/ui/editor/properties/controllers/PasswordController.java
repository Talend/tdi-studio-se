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
package org.talend.designer.core.ui.editor.properties.controllers;

import org.eclipse.gef.commands.Command;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.fieldassist.DecoratedField;
import org.eclipse.jface.fieldassist.TextControlCreator;
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
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.utils.ContextParameterUtils;
import org.talend.core.ui.CoreUIPlugin;
import org.talend.core.ui.properties.tab.IDynamicProperty;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.ui.editor.cmd.PropertyChangeCommand;

/**
 * DOC nrousseau class global comment. Detailled comment
 */
public class PasswordController extends TextController {

    protected static final String PASSWORD = "PASSWORD"; //$NON-NLS-1$

    protected String getDialogTitle() {
        return Messages.getString("PasswordController.NewPassword"); //$NON-NLS-1$
    }

    protected String getDialogMessage() {
        return Messages.getString("PasswordController.NoteConvention"); //$NON-NLS-1$
    }

    /**
     * DOC nrousseau PasswordController constructor comment.
     *
     * @param dp
     */
    public PasswordController(IDynamicProperty dp) {
        super(dp);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.designer.core.ui.editor.properties.controllers.TextController#isPasswordParam(org.talend.core.model
     * .process.IElementParameter)
     */
    @Override
    protected boolean isPasswordParam(IElementParameter parameter) {
        return true;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.ui.editor.properties.controllers.TextController#isReadOnly()
     */
    @Override
    protected boolean isReadOnly() {
        return true;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.designer.core.ui.editor.properties.controllers.TextController#createControl(org.eclipse.swt.widgets
     * .Composite, org.talend.core.model.process.IElementParameter, int, int, int, org.eclipse.swt.widgets.Control)
     */
    @Override
    public Control createControl(Composite subComposite, IElementParameter param, int numInRow, int nbInRow, int top,
            Control lastControl) {
        Control lastControlUsed = super.createControl(subComposite, param, numInRow, nbInRow, top, lastControl);
        FormData data = (FormData) lastControlUsed.getLayoutData();
        if (!param.isRepositoryValueUsed()) {
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
        btn.setData(NAME, PASSWORD);
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

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.designer.core.ui.editor.properties.controllers.TextController#estimateRowSize(org.eclipse.swt.widgets
     * .Composite, org.talend.core.model.process.IElementParameter)
     */
    @Override
    public int estimateRowSize(Composite subComposite, IElementParameter param) {
        if (!estimateInitialized) {
            final DecoratedField dField = new DecoratedField(subComposite, SWT.BORDER, new TextControlCreator());
            Point initialSize = dField.getLayoutControl().computeSize(SWT.DEFAULT, SWT.DEFAULT);
            dField.getLayoutControl().dispose();

            Button btn = getWidgetFactory().createButton(subComposite, "", SWT.PUSH); //$NON-NLS-1$
            int buttonSize = btn.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
            btn.dispose();
            rowSize = Math.max(initialSize.y, buttonSize) + ITabbedPropertyConstants.VSPACE;
            estimateInitialized = true;

        }
        return rowSize;
    }

    protected Command createButtonCommand(final Button button) {
        if (button.getData(NAME).equals(PASSWORD)) {
            String paramName = (String) button.getData(PARAMETER_NAME);
            IElementParameter param = elem.getElementParameter(paramName);
            String initValue = "\"\"";//$NON-NLS-1$
            if (param.getValue() != null && ContextParameterUtils.containContextVariables(param.getValue().toString())) {
                initValue = param.getValue().toString();
            }

            InputDialog dlg = new InputDialog(
                    button.getShell(),
                    getDialogTitle(), getDialogMessage(),
                    initValue, null) {

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
        }
    };

    private Command createCommand(SelectionEvent selectionEvent) {
        if (selectionEvent.getSource() instanceof Button) {
            return createButtonCommand((Button) selectionEvent.getSource());
        }
        return null;
    }

}
