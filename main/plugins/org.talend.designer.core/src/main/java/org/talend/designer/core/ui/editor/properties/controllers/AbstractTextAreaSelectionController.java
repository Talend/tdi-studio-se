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

import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.GC;
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
import org.talend.core.ui.CoreUIPlugin;
import org.talend.core.ui.properties.tab.IDynamicProperty;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.ui.editor.nodes.Node;


/**
 * created by hcyi on Jul 26, 2019
 * Detailled comment
 *
 */
public abstract class AbstractTextAreaSelectionController extends AbstractElementPropertySectionController {

    private static int DECORATION_OFFSET = -7;

    private boolean editableTextArea = false;

    private Text text;

    public AbstractTextAreaSelectionController(IDynamicProperty dp) {
        super(dp);
    }

    public void setEditableTextArea(final boolean editable) {
        this.editableTextArea = editable;
    }

    @Override
    public final Control createControl(final Composite subComposite, final IElementParameter param, final int numInRow,
            final int nbInRow, final int top, final Control lastControl) {
        final CLabel label = createLabel(subComposite, param.getDisplayName());
        setupLabelLayout(label, numInRow, nbInRow, top, lastControl);

        final Button button = createEditButton(subComposite, param);
        setupButtonLayout(button, numInRow, nbInRow, label);

        text = createTextField(subComposite, param);
        final int labelWidth = computeLabelWidth(label, param);
        setupTextLayout(text, labelWidth, label, button, numInRow, lastControl);
        setupText(text, param);

        Point initialSize = text.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        dynamicProperty.setCurRowSize(initialSize.y + ITabbedPropertyConstants.VSPACE);

        return button;
    }

    @Override
    public void refresh(IElementParameter param, boolean checkErrorsWhenViewRefreshed) {
        final String name = param.getName();
        final Text labelText = (Text) hashCurControls.get(name);
        final Object value = param.getValue();
        if (labelText == null || labelText.isDisposed()) {
            return;
        }

        boolean valueChanged = false;
        if (value == null) {
            labelText.setText(""); //$NON-NLS-1$
        } else {
            if (!value.equals(labelText.getText())) {
                labelText.setText((String) value);
                valueChanged = true;
            }
        }
        if (checkErrorsWhenViewRefreshed || valueChanged) {
            checkErrorsForPropertiesOnly(labelText);
        }
        fixedCursorPosition(param, labelText, value, valueChanged);
    }

    protected abstract SelectionListener createOnButtonClickedListener(final IElementParameter parameter);

    private CLabel createLabel(final Composite parent, final String displayName) {
        final CLabel label = getWidgetFactory().createCLabel(parent, displayName);
        return label;
    }

    private void setupLabelLayout(final CLabel label, final int numInRow, final int nbInRow, final int top,
            final Control lastControl) {
        final FormData data = new FormData();
        if (lastControl != null) {
            data.left = new FormAttachment(lastControl, 0);
        } else {
            data.left = new FormAttachment((((numInRow - 1) * MAX_PERCENT) / nbInRow), 0);
        }
        data.top = new FormAttachment(0, top);
        label.setLayoutData(data);
        if (numInRow != 1) {
            label.setAlignment(SWT.RIGHT);
        }
    }

    private Button createEditButton(final Composite parent, final IElementParameter param) {
        final Button editButton = getWidgetFactory().createButton(parent, "", SWT.PUSH);
        editButton.setImage(ImageProvider.getImage(CoreUIPlugin.getImageDescriptor(DOTS_BUTTON)));
        editButton.setEnabled(!param.isRepositoryValueUsed());
        editButton.addSelectionListener(createOnButtonClickedListener(param));
        return editButton;
    }

    private void setupButtonLayout(final Button button, final int numInRow, final int nbInRow, final CLabel label) {
        final FormData data = new FormData();
        data.left = new FormAttachment(((numInRow * MAX_PERCENT) / nbInRow), -STANDARD_BUTTON_WIDTH);
        data.right = new FormAttachment(((numInRow * MAX_PERCENT) / nbInRow), 0);
        data.top = new FormAttachment(label, 0, SWT.CENTER);
        data.height = STANDARD_HEIGHT - 2;
        button.setLayoutData(data);
    }

    private int computeLabelWidth(final CLabel label, final IElementParameter param) {
        int currentLabelWidth = STANDARD_LABEL_WIDTH;
        GC gc = new GC(label);
        Point labelSize = gc.stringExtent(param.getDisplayName());
        gc.dispose();
        if ((labelSize.x + ITabbedPropertyConstants.HSPACE) > currentLabelWidth) {
            currentLabelWidth = labelSize.x + ITabbedPropertyConstants.HSPACE;
        }
        return currentLabelWidth;
    }

    private Text createTextField(final Composite parent, final IElementParameter param) {
        Text text = new Text(parent, SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
        if (param.isRequired()) {
            ControlDecoration controlDecoration = new ControlDecoration(text, SWT.RIGHT | SWT.TOP);
            controlDecoration.setDescriptionText("Parameter is required"); // TODO i18n it //$NON-NLS-1$
            FieldDecoration fieldDecoration = FieldDecorationRegistry.getDefault()
                    .getFieldDecoration(FieldDecorationRegistry.DEC_REQUIRED);
            controlDecoration.setImage(fieldDecoration.getImage());
        }
        if (param.isRepositoryValueUsed()) {
            ControlDecoration controlDecoration = new ControlDecoration(text, SWT.RIGHT | SWT.BOTTOM);
            controlDecoration.setDescriptionText(Messages.getString("FileController.decoration.description")); //$NON-NLS-1$
            FieldDecoration fieldDecoration = FieldDecorationRegistry.getDefault()
                    .getFieldDecoration(FieldDecorationRegistry.DEC_CONTENT_PROPOSAL);
            controlDecoration.setImage(fieldDecoration.getImage());
        }

        return text;
    }

    private void setupTextLayout(final Text text, final int labelWidth, final CLabel label, final Button button,
            final int numInRow, final Control lastControl) {
        final FormData data = new FormData();
        if (lastControl != null) {
            data.left = new FormAttachment(lastControl, labelWidth);
        } else {
            data.left = new FormAttachment(0, labelWidth);
        }
        data.right = new FormAttachment(button, DECORATION_OFFSET);
        data.top = new FormAttachment(label, 0, SWT.TOP);
        data.height = 60;
        text.setLayoutData(data);
    }

    private void setupText(final Text text, final IElementParameter param) {
        text.setData(PARAMETER_NAME, param.getName());
        text.setEditable(editableTextArea);
        editionControlHelper.register(param.getName(), text);
        addDragAndDropTarget(text);
        if (elem instanceof Node) {
            text.setToolTipText(VARIABLE_TOOLTIP + param.getVariableName());
        }
        hashCurControls.put(param.getName(), text);
    }

    @Override
    public int estimateRowSize(final Composite subComposite, final IElementParameter param) {
        return 0;
    }

    @Override
    public void propertyChange(final PropertyChangeEvent evt) {
        // no-op
    }
}