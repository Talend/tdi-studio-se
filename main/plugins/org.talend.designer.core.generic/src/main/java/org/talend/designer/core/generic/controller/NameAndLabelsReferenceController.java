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

import java.beans.PropertyChangeEvent;

import org.apache.commons.lang.StringUtils;
import org.eclipse.gef.commands.Command;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.fieldassist.DecoratedField;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
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
import org.talend.core.utils.TalendQuoteUtils;
import org.talend.designer.core.generic.model.GenericElementParameter;
import org.talend.designer.core.generic.utils.ComponentsUtils;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.ui.editor.cmd.PropertyChangeCommand;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.properties.controllers.AbstractElementPropertySectionController;
import org.talend.designer.core.ui.editor.properties.controllers.creator.SelectAllTextControlCreator;

/**
 *
 * created by ycbai on 2015年10月19日 Detailled comment
 *
 */
public class NameAndLabelsReferenceController extends AbstractElementPropertySectionController {

    private static final String MODULE = "MODULE"; //$NON-NLS-1$

    private static final String PARAM_OBJ = "PARAM_OBJ"; //$NON-NLS-1$

    public NameAndLabelsReferenceController(IDynamicProperty dp) {
        super(dp);
    }

    public Command createCommand() {
        return null;
    }

    public Command createCommand(Button button) {
        Object paramObj = button.getData(PARAM_OBJ);
        if (paramObj instanceof GenericElementParameter) {
            GenericElementParameter gParam = (GenericElementParameter) paramObj;
            if (gParam != null) {
                callBeforeActive(gParam);
                NameAndLabelsDialog nameAndLabelsDialog = new NameAndLabelsDialog(composite.getShell(),
                        ComponentsUtils.getFormalPossibleValues(gParam), this.isInWizard());
                if (nameAndLabelsDialog.open() == IDialogConstants.OK_ID) {
                    String propertyName = (String) button.getData(PARAMETER_NAME);
                    String result = StringUtils.trimToEmpty(nameAndLabelsDialog.getResult());
                    Text moduleText = (Text) hashCurControls.get(propertyName);
                    moduleText.setText(result);
                    PropertyChangeCommand pcc = new PropertyChangeCommand(elem, propertyName, result);
                    return pcc;
                }
            }
        }
        return null;
    }

    @Override
    public Control createControl(final Composite subComposite, final IElementParameter param, final int numInRow,
            final int nbInRow, final int top, final Control lastControl) {
        this.curParameter = param;
        CLabel labelLabel = getWidgetFactory().createCLabel(subComposite, param.getDisplayName());
        FormData data = new FormData();
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

        Button btnEdit = getWidgetFactory().createButton(subComposite, "", SWT.PUSH); //$NON-NLS-1$

        btnEdit.setImage(ImageProvider.getImage(CoreUIPlugin.getImageDescriptor(DOTS_BUTTON)));

        data = new FormData();
        data.left = new FormAttachment(((numInRow * MAX_PERCENT) / nbInRow), -STANDARD_BUTTON_WIDTH);
        data.right = new FormAttachment(((numInRow * MAX_PERCENT) / nbInRow), 0);
        data.top = new FormAttachment(labelLabel, 0, SWT.CENTER);
        data.height = STANDARD_HEIGHT - 2;
        btnEdit.setLayoutData(data);
        btnEdit.setData(NAME, MODULE);
        btnEdit.setData(PARAM_OBJ, param);
        btnEdit.setData(PARAMETER_NAME, param.getName());
        btnEdit.setEnabled(!param.isRepositoryValueUsed());
        // btnEdit.setEnabled(!param.isReadOnly());
        btnEdit.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                Command cmd = createCommand((Button) e.getSource());
                executeCommand(cmd);
            }
        });

        DecoratedField dField = new DecoratedField(subComposite, SWT.BORDER, new SelectAllTextControlCreator());
        if (param.isRequired()) {
            FieldDecoration decoration = FieldDecorationRegistry.getDefault().getFieldDecoration(
                    FieldDecorationRegistry.DEC_REQUIRED);
            dField.addFieldDecoration(decoration, SWT.RIGHT | SWT.TOP, false);
        }
        if (param.isRepositoryValueUsed()) {
            FieldDecoration decoration = FieldDecorationRegistry.getDefault().getFieldDecoration(
                    FieldDecorationRegistry.DEC_CONTENT_PROPOSAL);
            decoration.setDescription(Messages.getString("FileController.decoration.description")); //$NON-NLS-1$
            dField.addFieldDecoration(decoration, SWT.RIGHT | SWT.BOTTOM, false);
        }

        Control cLayout = dField.getLayoutControl();
        Text moduleText = (Text) dField.getControl();
        moduleText.setData(PARAMETER_NAME, param.getName());
        moduleText.setEditable(false);
        cLayout.setBackground(subComposite.getBackground());

        addDragAndDropTarget(moduleText);
        if (elem instanceof Node) {
            moduleText.setToolTipText(VARIABLE_TOOLTIP + param.getVariableName());
        }

        hashCurControls.put(param.getName(), moduleText);

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
        data.right = new FormAttachment(btnEdit, 0);
        data.top = new FormAttachment(labelLabel, 0, SWT.CENTER);
        cLayout.setLayoutData(data);

        Point initialSize = dField.getLayoutControl().computeSize(SWT.DEFAULT, SWT.DEFAULT);

        dynamicProperty.setCurRowSize(initialSize.y + ITabbedPropertyConstants.VSPACE);

        return btnEdit;
    }

    @Override
    public int estimateRowSize(Composite subComposite, IElementParameter param) {
        return 0;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
    }

    @Override
    public void refresh(IElementParameter param, boolean checkErrorsWhenViewRefreshed) {
        final String name = param.getName();
        Text labelText = (Text) hashCurControls.get(name);
        Object value = param.getValue();
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

}
