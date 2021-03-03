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

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.ui.CoreUIPlugin;
import org.talend.core.ui.properties.tab.IDynamicProperty;
import org.talend.designer.core.ui.editor.nodes.Node;

/**
 * DOC qwei class global comment. Detailled comment <br/>
 *
 * $Id: talend.epf 1 2006-09-29 17:06:40 +0000 (ææäº, 29 ä¹æ 2006) nrousseau $
 *
 */
public class LabelController extends AbstractElementPropertySectionController {

    /**
     * DOC qwei LabelController constructor comment.
     *
     * @param dtp
     */
    public LabelController(IDynamicProperty dp) {
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
        // TODO Auto-generated method stub
        FormData data;
        CLabel labelLabel;
        if (param.getValue().equals("")) { //$NON-NLS-1$
            labelLabel = getWidgetFactory().createCLabel(subComposite, param.getDisplayName(), SWT.SHADOW_NONE);
        } else {
            labelLabel = getWidgetFactory().createCLabel(subComposite, (String) param.getValue(), SWT.SHADOW_NONE);
        }

        RGB rgb = param.getColor();
        RGB bgRgb = param.getBackgroundColor();
        if (rgb != null || bgRgb != null) {
            CoreUIPlugin.setCSSClass(labelLabel, "", false);
            if (rgb != null) {
                CoreUIPlugin.removeCSSAttribute(labelLabel, "color"); //$NON-NLS-1$
                Color color = new Color(null, rgb);
                addResourceDisposeListener(labelLabel, color);
                labelLabel.setForeground(color);
            }

            if (bgRgb != null) {
                CoreUIPlugin.removeCSSAttribute(labelLabel, "background-color"); //$NON-NLS-1$
                Color bgColor = new Color(null, bgRgb);
                addResourceDisposeListener(labelLabel, bgColor);
                labelLabel.setBackground(bgColor);
            }
        }

        labelLabel.setData(PARAMETER_NAME, param.getName());

        if (elem instanceof Node) {
            labelLabel.setToolTipText(VARIABLE_TOOLTIP + param.getVariableName());
        }
        hashCurControls.put(param.getName(), labelLabel);
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
        // **************************
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
        Point initialSize = labelLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        dynamicProperty.setCurRowSize(initialSize.y + ITabbedPropertyConstants.VSPACE);
        return null;
    }

    @Override
    public void refresh(IElementParameter param, boolean check) {
        // TODO Auto-generated method stub
        final String name2 = param.getName();
        CLabel labelText = (CLabel) hashCurControls.get(name2);
        if (labelText == null || labelText.isDisposed()) {
            return;
        }
        Object value = param.getValue();
        if (value.equals("")) { //$NON-NLS-1$
            labelText.setText(param.getDisplayName());
        } else {
            labelText.setText((String) value);
        }

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

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.designer.core.ui.editor.properties.controllers.AbstractElementPropertySectionController#estimateRowSize
     * (org.eclipse.swt.widgets.Composite, org.talend.core.model.process.IElementParameter)
     */
    @Override
    public int estimateRowSize(Composite subComposite, IElementParameter param) {
        CLabel labelLabel;

        if (param.getValue().equals("")) { //$NON-NLS-1$
            labelLabel = getWidgetFactory().createCLabel(subComposite, param.getDisplayName(), SWT.SHADOW_NONE);
        } else {
            labelLabel = getWidgetFactory().createCLabel(subComposite, (String) param.getValue(), SWT.SHADOW_NONE);
        }

        Point initialSize = labelLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        labelLabel.dispose();
        return initialSize.y + ITabbedPropertyConstants.VSPACE;
    }

}
