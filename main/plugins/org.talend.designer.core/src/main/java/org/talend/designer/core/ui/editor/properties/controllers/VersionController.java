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

import org.eclipse.jface.fieldassist.DecoratedField;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.fieldassist.TextControlCreator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
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
import org.talend.core.model.process.IElementParameter;
import org.talend.core.ui.properties.tab.IDynamicProperty;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.properties.controllers.creator.SelectAllTextControlCreator;

/**
 * DOC yzhang class global comment. Detailled comment <br/>
 *
 * $Id: VersionController.java 1 2006-12-12 下午01:17:56 +0000 (下午01:17:56) yzhang $
 *
 */
public class VersionController extends AbstractElementPropertySectionController {

    private static final String VER_MIN = "MinVerUp"; //$NON-NLS-1$

    private static final String VER_MAJ = "MajVerUp"; //$NON-NLS-1$

    /**
     * DOC yzhang VersionController constructor comment.
     *
     * @param parameterBean
     */
    public VersionController(IDynamicProperty dp) {
        super(dp);

    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.ui.editor.properties2.editors.AbstractElementPropertySectionController#createControl()
     */
    @Override
    public Control createControl(final Composite subComposite, final IElementParameter param, final int numInRow,
            final int nbInRow, final int top, final Control lastControl) {
        Button btnUp, btnDown;

        btnUp = getWidgetFactory().createButton(subComposite, "M", SWT.PUSH); //$NON-NLS-1$
        btnUp.setData(NAME, VER_MAJ);
        btnUp.setEnabled(!param.isReadOnly());
        btnUp.addSelectionListener(listenerSelection);
        Point btnUpSize = btnUp.computeSize(SWT.DEFAULT, SWT.DEFAULT);

        btnDown = getWidgetFactory().createButton(subComposite, "m", SWT.PUSH); //$NON-NLS-1$
        Point btnDownSize = btnDown.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        Point btnSize = new Point(Math.max(btnUpSize.x, btnDownSize.x), Math.max(btnUpSize.y, btnDownSize.y));

        FormData data = new FormData();
        data.left = new FormAttachment(((numInRow * MAX_PERCENT) / nbInRow), -btnSize.x);
        data.right = new FormAttachment(((numInRow * MAX_PERCENT) / nbInRow), 0);
        data.top = new FormAttachment(0, top);
        data.height = btnSize.y;
        btnDown.setLayoutData(data);
        btnDown.setData(NAME, VER_MIN);
        btnDown.setEnabled(!param.isReadOnly());
        btnDown.addSelectionListener(listenerSelection);

        data = new FormData();
        data.left = new FormAttachment(((numInRow * MAX_PERCENT) / nbInRow), -((btnSize.x * 2) + ITabbedPropertyConstants.HSPACE));
        data.right = new FormAttachment(((numInRow * MAX_PERCENT) / nbInRow), -(btnSize.x + ITabbedPropertyConstants.HSPACE));
        data.top = new FormAttachment(0, top);
        data.height = btnSize.y;
        btnUp.setLayoutData(data);

        DecoratedField dField = new DecoratedField(subComposite, SWT.BORDER, new SelectAllTextControlCreator());
        if (param.isRequired()) {
            FieldDecoration decoration = FieldDecorationRegistry.getDefault().getFieldDecoration(
                    FieldDecorationRegistry.DEC_REQUIRED);
            dField.addFieldDecoration(decoration, SWT.RIGHT | SWT.TOP, false);
        }

        Control cLayout = dField.getLayoutControl();

        Text labelText = (Text) dField.getControl();
        cLayout.setBackground(subComposite.getBackground());
        labelText.setEditable(false);
        if (elem instanceof Node) {
            labelText.setToolTipText(VARIABLE_TOOLTIP + param.getVariableName());
        }

        CLabel labelLabel = getWidgetFactory().createCLabel(subComposite, param.getDisplayName()); //$NON-NLS-1$
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
        if (numInRow == 1) {
            if (lastControl != null) {
                data.left = new FormAttachment(lastControl, STANDARD_LABEL_WIDTH);
            } else {
                data.left = new FormAttachment((((numInRow - 1) * MAX_PERCENT) / nbInRow), STANDARD_LABEL_WIDTH);
            }

        } else {
            data.left = new FormAttachment(labelLabel, 0, SWT.RIGHT);
        }
        data.right = new FormAttachment(btnUp, 0);
        data.top = new FormAttachment(0, top);
        cLayout.setLayoutData(data);
        // **********************
        hashCurControls.put(param.getName(), labelText);

        Point initialSize = dField.getLayoutControl().computeSize(SWT.DEFAULT, SWT.DEFAULT);
        if (btnSize.y > initialSize.y) {
            // curRowSize = btnSize.y + ITabbedPropertyConstants.VSPACE;
            dynamicProperty.setCurRowSize(btnSize.y + ITabbedPropertyConstants.VSPACE);
        } else {
            dynamicProperty.setCurRowSize(initialSize.y + ITabbedPropertyConstants.VSPACE);
            // curRowSize = initialSize.y + ITabbedPropertyConstants.VSPACE;
        }
        return btnDown;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.ui.editor.properties.controllers.AbstractElementPropertySectionController#estimateRowSize(org.eclipse.swt.widgets.Composite,
     * org.talend.core.model.process.IElementParameter)
     */
    @Override
    public int estimateRowSize(Composite subComposite, IElementParameter param) {
        DecoratedField dField = new DecoratedField(subComposite, SWT.BORDER, new TextControlCreator());
        Point initialSize = dField.getLayoutControl().computeSize(SWT.DEFAULT, SWT.DEFAULT);
        dField.getLayoutControl().dispose();

        Button btnUp, btnDown;

        btnUp = getWidgetFactory().createButton(subComposite, "M", SWT.PUSH); //$NON-NLS-1$
        Point btnUpSize = btnUp.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        btnUp.dispose();

        btnDown = getWidgetFactory().createButton(subComposite, "m", SWT.PUSH); //$NON-NLS-1$
        Point btnDownSize = btnDown.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        btnDown.dispose();

        Point btnSize = new Point(Math.max(btnUpSize.x, btnDownSize.x), Math.max(btnUpSize.y, btnDownSize.y));

        return Math.max(btnSize.y, initialSize.y) + ITabbedPropertyConstants.VSPACE;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent evt) {
        // TODO Auto-generated method stub

    }

    SelectionListener listenerSelection = new SelectionListener() {

        public void widgetDefaultSelected(SelectionEvent e) {
            // TODO Auto-generated method stub

        }

        public void widgetSelected(SelectionEvent e) {
            // TODO Auto-generated method stub

        }

    };

    @Override
    public void refresh(IElementParameter param, boolean checkErrorsWhenViewRefreshed) {
        Text labelText = (Text) hashCurControls.get(param.getName());
        Object value = param.getValue();
        if (labelText == null || labelText.isDisposed()) {
            return;
        }
        if (value == null) {
            labelText.setText(""); //$NON-NLS-1$
        } else {
            if (!value.equals(labelText.getText())) {
                labelText.setText((String) value);
            }
        }
        if (checkErrorsWhenViewRefreshed) {
            checkErrorsForPropertiesOnly(labelText);
        }
    }
}
