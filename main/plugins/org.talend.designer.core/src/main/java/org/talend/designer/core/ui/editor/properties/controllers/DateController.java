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
import org.eclipse.jface.fieldassist.DecoratedField;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.fieldassist.TextControlCreator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.SelectionEvent;
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
import org.talend.commons.ui.runtime.swt.tableviewer.celleditor.DateDialog;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.utils.TalendTextUtils;
import org.talend.core.ui.CoreUIPlugin;
import org.talend.core.ui.properties.tab.IDynamicProperty;
import org.talend.core.utils.TalendQuoteUtils;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.ui.editor.cmd.PropertyChangeCommand;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.properties.controllers.creator.SelectAllTextControlCreator;

/**
 * DOC nrousseau class global comment. Detailled comment <br/>
 *
 */
public class DateController extends AbstractElementPropertySectionController {

    private static final String DATE = "DATE"; //$NON-NLS-1$

    private static final int STANDARD_TEXT_WIDTH_ADDITION = 15;

    private Text dateText;

    public DateController(IDynamicProperty dp) {
        super(dp);
    }

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
        FormData data;

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

        Button btnEdit = getWidgetFactory().createButton(subComposite, "", SWT.PUSH); //$NON-NLS-1$

        btnEdit.setImage(ImageProvider.getImage(CoreUIPlugin.getImageDescriptor(DOTS_BUTTON)));

        btnEdit.setData(NAME, DATE);
        btnEdit.setData(PARAMETER_NAME, param.getName());
        btnEdit.setEnabled(!param.isReadOnly());
        btnEdit.addSelectionListener(listenerSelection);

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
        dateText = (Text) dField.getControl();
        dateText.setData(PARAMETER_NAME, param.getName());
        cLayout.setBackground(subComposite.getBackground());
        dateText.setEditable(!param.isReadOnly());

        editionControlHelper.register(param.getName(), dateText);

        addDragAndDropTarget(dateText);
        if (elem instanceof Node) {
            dateText.setToolTipText(VARIABLE_TOOLTIP + param.getVariableName());
        }

        hashCurControls.put(param.getName(), dateText);

        // **************************
        data = new FormData();
        int currentLabelWidth = STANDARD_LABEL_WIDTH;
        GC gc = new GC(labelLabel);
        Point labelSize = gc.stringExtent(param.getDisplayName());
        gc.dispose();
        gc = new GC(dateText);
        Point defaultDateDateSize = gc.stringExtent("\"2007-06-22 00:00:00\""); // just an example of date //$NON-NLS-1$
        Object paramValue = param.getValue() != null ? param.getValue() : "";//$NON-NLS-1$
        Point currentDateSize = gc.stringExtent((String) paramValue); // current value
        gc.dispose();
        int currentDateWidth = defaultDateDateSize.x + ITabbedPropertyConstants.HSPACE + STANDARD_TEXT_WIDTH_ADDITION;
        if ((currentDateSize.x + ITabbedPropertyConstants.HSPACE + STANDARD_TEXT_WIDTH_ADDITION) > currentDateWidth) {
            currentDateWidth = currentDateSize.x + ITabbedPropertyConstants.HSPACE + STANDARD_TEXT_WIDTH_ADDITION;
        }

        dateText.computeSize(SWT.DEFAULT, SWT.DEFAULT);

        if ((labelSize.x + ITabbedPropertyConstants.HSPACE) > currentLabelWidth) {
            currentLabelWidth = labelSize.x + ITabbedPropertyConstants.HSPACE;
        }

        data.left = new FormAttachment(labelLabel, 0, SWT.RIGHT);
        data.right = new FormAttachment(labelLabel, currentDateWidth, SWT.RIGHT);
        data.top = new FormAttachment(labelLabel, 0, SWT.CENTER);
        cLayout.setLayoutData(data);

        data = new FormData();
        data.left = new FormAttachment(cLayout, 0, SWT.RIGHT);
        data.right = new FormAttachment(cLayout, STANDARD_BUTTON_WIDTH, SWT.RIGHT);
        data.top = new FormAttachment(labelLabel, 0, SWT.CENTER);
        data.height = STANDARD_HEIGHT - 2;
        btnEdit.setLayoutData(data);

        Point initialSize = dField.getLayoutControl().computeSize(SWT.DEFAULT, SWT.DEFAULT);

        dynamicProperty.setCurRowSize(initialSize.y + ITabbedPropertyConstants.VSPACE);

        return btnEdit;
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
        DecoratedField dField = new DecoratedField(subComposite, SWT.BORDER, new TextControlCreator());
        Point initialSize = dField.getLayoutControl().computeSize(SWT.DEFAULT, SWT.DEFAULT);
        dField.getLayoutControl().dispose();

        return initialSize.y + ITabbedPropertyConstants.VSPACE;
    }

    SelectionListener listenerSelection = new SelectionListener() {

        @Override
        public void widgetDefaultSelected(SelectionEvent e) {

        }

        @Override
        public void widgetSelected(SelectionEvent e) {
            Command cmd = createCommand((Button) e.getSource());
            executeCommand(cmd);
        }

    };

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.designer.core.ui.editor.properties2.editors.AbstractElementPropertySectionController#createCommand()
     */
    public Command createCommand(Button button) {
        DateDialog dateDial = new DateDialog(composite.getShell());
        int returnValue = dateDial.open();
        if (returnValue == DateDialog.OK) {
            String propertyName = (String) button.getData(PARAMETER_NAME);
            String date = dateDial.getTalendDateString();
            if (date != null && !date.equals(elem.getPropertyValue(propertyName))) {
                elem.setPropertyValue(EParameterName.UPDATE_COMPONENTS.getName(), Boolean.TRUE);
                return new PropertyChangeCommand(elem, propertyName, TalendTextUtils.addQuotes(date));
            }
        }
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.designer.core.ui.editor.properties.controllers.AbstractElementPropertySectionController#refresh(org
     * .talend.core.model.process.IElementParameter, boolean)
     */
    @Override
    public void refresh(IElementParameter param, boolean checkErrorsWhenViewRefreshed) {
        final String name2 = param.getName();
        Text labelText = (Text) hashCurControls.get(name2);
        Object value = param.getValue();
        if (labelText == null || labelText.isDisposed()) {
            return;
        }

        boolean valueChanged = false;
        if (value == null) {
            labelText.setText(TalendQuoteUtils.addQuotes("")); //$NON-NLS-1$
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

    /*
     * (non-Javadoc)
     *
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    @Override
    public void propertyChange(PropertyChangeEvent arg0) {
        // TODO Auto-generated method stub
    }

}
