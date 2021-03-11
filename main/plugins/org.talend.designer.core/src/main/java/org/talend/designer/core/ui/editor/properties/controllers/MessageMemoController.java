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
package org.talend.designer.core.ui.editor.properties.controllers;

import java.beans.PropertyChangeEvent;

import org.eclipse.gef.commands.Command;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.fieldassist.DecoratedField;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.fieldassist.IControlCreator;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.commons.ui.swt.colorstyledtext.ColorStyledText;
import org.talend.core.CorePlugin;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.ui.CoreUIPlugin;
import org.talend.core.ui.properties.tab.IDynamicProperty;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.ui.editor.cmd.PropertyChangeCommand;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.preferences.TalendDesignerPrefConstants;

/**
 * DOC zli class global comment. Detailled comment <br/>
 */
public class MessageMemoController extends AbstractElementPropertySectionController {

    public MessageMemoController(IDynamicProperty dp) {
        super(dp);
    }

    private Button messageEditorButton;

    private ColorStyledText messageText;

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.designer.core.ui.editor.properties.controllers.AbstractElementPropertySectionController#createControl
     * (org.eclipse.swt.widgets.Composite, org.talend.core.model.process.IElementParameter, int, int, int,
     * org.eclipse.swt.widgets.Control)
     */
    @Override
    public Control createControl(Composite subComposite, IElementParameter param, int numInRow, int nbInRow, int top,
            Control lastControl) {
        this.curParameter = param;
        this.paramFieldType = param.getFieldType();
        final DecoratedField dField1 = new DecoratedField(subComposite, SWT.PUSH, new IControlCreator() {

            @Override
            public Control createControl(Composite parent, int style) {
                return new Button(parent, style);
            }
        });

        Control buttonControl = dField1.getLayoutControl();
        messageEditorButton = (Button) dField1.getControl();

        messageEditorButton.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        messageEditorButton.setImage(ImageProvider.getImage(CoreUIPlugin.getImageDescriptor(DOTS_BUTTON)));
        buttonControl.setBackground(subComposite.getBackground());
        messageEditorButton.setEnabled(true);
        messageEditorButton.setData(NAME, SQLEDITOR);
        messageEditorButton.setData(PARAMETER_NAME, param.getName());
        messageEditorButton.setEnabled(!param.isReadOnly());
        messageEditorButton.addSelectionListener(listenerSelection);

        FormData data1 = new FormData();
        data1.right = new FormAttachment(100, -ITabbedPropertyConstants.HSPACE);
        data1.left = new FormAttachment(100, -(ITabbedPropertyConstants.HSPACE + STANDARD_BUTTON_WIDTH));
        data1.top = new FormAttachment(0, top);

        buttonControl.setLayoutData(data1);

        int nbLines = param.getNbLines();

        IControlCreator txtCtrl = new IControlCreator() {

            @Override
            public Control createControl(final Composite parent, final int style) {
                return createColorStyledText(parent, style);
            }
        };
        DecoratedField dField = null;
        if (param.getNbLines() != 1) {
            dField = new DecoratedField(subComposite, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL, txtCtrl);
        } else {
            dField = new DecoratedField(subComposite, SWT.BORDER, txtCtrl);
        }
        if (param.isRequired()) {
            FieldDecoration decoration = FieldDecorationRegistry.getDefault().getFieldDecoration(
                    FieldDecorationRegistry.DEC_REQUIRED);
            dField.addFieldDecoration(decoration, SWT.RIGHT | SWT.TOP, false);
        }
        Control cLayout = dField.getLayoutControl();
        messageText = (ColorStyledText) dField.getControl();
        messageText.setText(getCurrentMessage());
        messageText.setData(PARAMETER_NAME, param.getName());
        editionControlHelper.register(param.getName(), messageText);

        FormData d = (FormData) messageText.getLayoutData();
        if (getAdditionalHeightSize() != 0) {
            nbLines += this.getAdditionalHeightSize() / messageText.getLineHeight();
        }
        d.height = messageText.getLineHeight() * nbLines;
        FormData data;
        messageText.getParent().setSize(subComposite.getSize().x, messageText.getLineHeight() * nbLines);
        cLayout.setBackground(subComposite.getBackground());
        messageText.setEnabled(!param.isReadOnly());
        if (elem instanceof Node) {
            messageText.setToolTipText(VARIABLE_TOOLTIP + param.getVariableName());
        }

        addDragAndDropTarget(messageText);

        CLabel labelLabel = getWidgetFactory().createCLabel(subComposite, param.getDisplayName());
        data = new FormData();
        if (lastControl != null) {
            data.left = new FormAttachment(lastControl, 0);
        } else {
            data.left = new FormAttachment((((numInRow - 1) * MAX_PERCENT) / nbInRow), 0);
        }
        data.top = new FormAttachment(0, top);
        labelLabel.setLayoutData(data);
        if (numInRow != 1) {
            labelLabel.setAlignment(SWT.RIGHT);
        }
        // *********************
        data = new FormData();
        int currentLabelWidth = STANDARD_LABEL_WIDTH;
        GC gc = new GC(labelLabel);
        Point labelSize = gc.stringExtent(param.getDisplayName());
        gc.dispose();

        if ((labelSize.x + ITabbedPropertyConstants.HSPACE) > currentLabelWidth) {
            currentLabelWidth = labelSize.x + ITabbedPropertyConstants.HSPACE;
        }

        if (numInRow == 1) {
            if (lastControl != null) {
                data.left = new FormAttachment(lastControl, currentLabelWidth);
            } else {
                data.left = new FormAttachment(0, currentLabelWidth);
            }

        } else {
            data.left = new FormAttachment(labelLabel, 0, SWT.RIGHT);
        }
        data.right = new FormAttachment(buttonControl, -5, SWT.LEFT);
        data.top = new FormAttachment(0, top);
        cLayout.setLayoutData(data);
        // **********************
        hashCurControls.put(param.getName(), messageText);

        Point initialSize = dField.getLayoutControl().computeSize(SWT.DEFAULT, SWT.DEFAULT);
        dynamicProperty.setCurRowSize(initialSize.y + ITabbedPropertyConstants.VSPACE);
        return null;
    }

    private ColorStyledText createColorStyledText(final Composite parent, final int style) {

        IPreferenceStore preferenceStore = CorePlugin.getDefault().getPreferenceStore();
        ColorStyledText colorText = new ColorStyledText(parent, style, preferenceStore, "tsql"); //$NON-NLS-1$

        String fontType = preferenceStore.getString(TalendDesignerPrefConstants.MEMO_TEXT_FONT);
        FontData fontData = new FontData(fontType);
        Font font = new Font(parent.getDisplay(), fontData);

        // Font font = new Font(null, "courier", 8, SWT.NONE); //$NON-NLS-1$
        addResourceDisposeListener(colorText, font);
        colorText.setFont(font);
        return colorText;
    }

    private String getCurrentMessage() {
        String propertyName = (String) messageEditorButton.getData(PARAMETER_NAME);
        String query = (String) elem.getPropertyValue(propertyName);
        if (query != null) {
            return query;
        } else {
            return "";
        }
    }

    SelectionListener listenerSelection = new SelectionListener() {

        @Override
        public void widgetDefaultSelected(SelectionEvent e) {

        }

        @Override
        public void widgetSelected(SelectionEvent e) {

            ShowMessageDialog showMessageDialog = new ShowMessageDialog(Display.getCurrent().getActiveShell(),
                    messageText.getText());
            if (showMessageDialog.open() == Window.OK) {
                Command cmd = createCommand(showMessageDialog);
                executeCommand(cmd);
            }
        }
    };

    /**
     * DOC zli Comment method "createCommand".
     *
     * @param showMessageDialog
     * @return
     */
    private Command createCommand(ShowMessageDialog showMessageDialog) {
        String propertyName = (String) messageEditorButton.getData(PARAMETER_NAME);
        String message = showMessageDialog.getMessages();
        if (message != null) {
            messageText.setText(message);
            return new PropertyChangeCommand(elem, propertyName, message);
        }
        return null;
    }

    /**
     *
     * zli ShowMessageDialog class global comment. Detailled comment
     */
    class ShowMessageDialog extends Dialog {

        private String message;

        public ShowMessageDialog(Shell parentShell, String message) {
            super(parentShell);
            setShellStyle(SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MIN | SWT.MAX | SWT.APPLICATION_MODAL);
            this.message = message;
        }

        @Override
        protected void configureShell(Shell newShell) {
            super.configureShell(newShell);
            newShell.setText(Messages.getString("MessageMemoController.Message")); //$NON-NLS-1$
        }

        @Override
        protected Control createDialogArea(Composite parent) {
            Composite composite2 = (Composite) super.createDialogArea(parent);
            final ColorStyledText colorStyledText = createColorStyledText(composite2, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL
                    | SWT.WRAP);
            GridData gridData = new GridData(GridData.FILL_BOTH);
            gridData.minimumHeight = 130;
            gridData.heightHint = 150;
            gridData.minimumWidth = 350;
            gridData.widthHint = 400;
            colorStyledText.setLayoutData(gridData);
            if (message != null) {
                colorStyledText.setText(message);
            }
            colorStyledText.addModifyListener(new ModifyListener() {

                @Override
                public void modifyText(ModifyEvent e) {
                    message = colorStyledText.getText();
                }
            });
            return colorStyledText;

        }

        @Override
        protected void createButtonsForButtonBar(Composite parent) {
            super.createButtonsForButtonBar(parent);
        }

        public String getMessages() {
            return this.message;
        }

    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.designer.core.ui.editor.properties.controllers.AbstractElementPropertySectionController#estimateRowSize
     * (org.eclipse.swt.widgets.Composite, org.talend.core.model.process.IElementParameter)
     */
    @Override
    public int estimateRowSize(Composite subComposite, IElementParameter param) {
        IControlCreator txtCtrl = new IControlCreator() {

            @Override
            public Control createControl(final Composite parent, final int style) {
                return createColorStyledText(parent, style);
            }
        };

        DecoratedField dField = null;
        if (param.getNbLines() != 1) {
            dField = new DecoratedField(subComposite, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL, txtCtrl);
        } else {
            dField = new DecoratedField(subComposite, SWT.BORDER, txtCtrl);
        }
        ColorStyledText text = (ColorStyledText) dField.getControl();
        FormData d = (FormData) text.getLayoutData();
        d.height = text.getLineHeight() * param.getNbLines();
        text.getParent().setSize(subComposite.getSize().x, text.getLineHeight() * param.getNbLines());

        Point initialSize = dField.getLayoutControl().computeSize(SWT.DEFAULT, SWT.DEFAULT);
        dField.getLayoutControl().dispose();

        return initialSize.y + ITabbedPropertyConstants.VSPACE;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.designer.core.ui.editor.properties.controllers.AbstractElementPropertySectionController#refresh(org
     * .talend.core.model.process.IElementParameter, boolean)
     */
    @Override
    public void refresh(IElementParameter param, boolean check) {
        ColorStyledText labelText = (ColorStyledText) hashCurControls.get(param.getName());
        if (labelText == null || labelText.isDisposed()) {
            return;
        }
        Object value = param.getValue();

        boolean valueChanged = false;
        if (value == null) {
            labelText.setText(""); //$NON-NLS-1$
        } else {
            if (!value.equals(labelText.getText())) {
                labelText.setText((String) value);
                valueChanged = true;
            }
        }
        if (check || valueChanged) {
            checkErrorsForPropertiesOnly(labelText);
        }
        fixedCursorPosition(param, labelText, value, valueChanged);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        // TODO Auto-generated method stub

    }
}
