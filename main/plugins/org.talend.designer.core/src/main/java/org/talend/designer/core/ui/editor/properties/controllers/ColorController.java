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
import org.eclipse.jface.fieldassist.IControlCreator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.talend.commons.ui.utils.image.ColorUtils;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.ui.properties.tab.IDynamicProperty;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.ui.editor.cmd.PropertyChangeCommand;
import org.talend.designer.core.ui.editor.nodes.Node;

/**
 * DOC yzhang class global comment. Detailled comment <br/>
 *
 * $Id: CheckController.java 1 2006-12-12 下午01:45:55 +0000 (下午01:45:55) yzhang $
 *
 */
public class ColorController extends AbstractElementPropertySectionController {

    private static final int SIZE_X = 50;

    private static final int SIZE_Y = 18;

    /**
     * DOC yzhang CheckController constructor comment.
     *
     * @param parameterBean
     */
    public ColorController(IDynamicProperty dp) {
        super(dp);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.designer.core.ui.editor.properties2.editors.AbstractElementPropertySectionController#createCommand()
     */
    private Command createCommand(SelectionEvent event) {
        Control ctrl = (Control) event.getSource();
        if (ctrl instanceof Button) {
            String paramName = (String) ctrl.getData(PARAMETER_NAME);
            if (paramName != null) {
                ColorDialog colorDialog = new ColorDialog(ctrl.getShell());
                colorDialog.setRGB(ColorUtils.parseStringToRGB((String) elem.getPropertyValue(paramName)));
                RGB rgb = colorDialog.open();

                if (rgb != null) {
                    setButtonColor((Button) ctrl, rgb);
                    Command cmd;
                    cmd = new PropertyChangeCommand(elem, paramName, ColorUtils.getRGBValue(rgb));
                    return cmd;
                }
            }
        }
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.designer.core.ui.editor.properties2.editors.AbstractElementPropertySectionController#createControl()
     */
    @Override
    public Control createControl(final Composite subComposite, final IElementParameter param, final int numInRow,
            final int nbInRow, final int top, final Control lastControl) {
        final DecoratedField dField = new DecoratedField(subComposite, SWT.BORDER, new IControlCreator() {

            public Control createControl(Composite parent, int style) {
                return getWidgetFactory().createButton(parent, "", SWT.FLAT); //$NON-NLS-1$
            }

        });
        if (param.isRepositoryValueUsed()) {
            FieldDecoration decoration = FieldDecorationRegistry.getDefault().getFieldDecoration(
                    FieldDecorationRegistry.DEC_CONTENT_PROPOSAL);
            decoration.setDescription(Messages.getString("CheckController.decoration.description")); //$NON-NLS-1$
            dField.addFieldDecoration(decoration, SWT.RIGHT | SWT.BOTTOM, false);
        }

        Control cLayout = dField.getLayoutControl();
        cLayout.setBackground(subComposite.getBackground());
        Button colorBtn = (Button) dField.getControl();

        hashCurControls.put(param.getName(), colorBtn);
        colorBtn.setEnabled(!param.isReadOnly());
        colorBtn.addSelectionListener(listenerSelection);
        colorBtn.setData(PARAMETER_NAME, param.getName());
        if (elem instanceof Node) {
            colorBtn.setToolTipText(VARIABLE_TOOLTIP + param.getVariableName());
        }

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
        // data.right = new FormAttachment((numInRow * MAX_PERCENT) / nbInRow, 0);
        data.top = new FormAttachment(0, top);
        cLayout.setLayoutData(data);

        Point initialSize = colorBtn.computeSize(SIZE_X, SIZE_Y);
        dynamicProperty.setCurRowSize(initialSize.y + ITabbedPropertyConstants.VSPACE);
        refresh(param, false);
        return cLayout;
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
        final DecoratedField dField = new DecoratedField(subComposite, SWT.BORDER, new IControlCreator() {

            public Control createControl(Composite parent, int style) {
                return getWidgetFactory().createButton(parent, "", SWT.FLAT); //$NON-NLS-1$
            }

        });
        Button colorBtn = (Button) dField.getControl();
        Point initialSize = colorBtn.computeSize(SIZE_X, SIZE_Y);
        dField.getLayoutControl().dispose();
        return initialSize.y;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent evt) {
        // TODO Auto-generated method stub

    }

    SelectionListener listenerSelection = new SelectionAdapter() {

        public void widgetSelected(SelectionEvent event) {
            Command cmd = createCommand(event);
            executeCommand(cmd);
        }
    };

    @Override
    public void refresh(IElementParameter param, boolean checkErrorsWhenViewRefreshed) {
        Button colorBtn = (Button) hashCurControls.get(param.getName());
        Object value = param.getValue();
        if (colorBtn == null || colorBtn.isDisposed()) {
            return;
        }
        if (value != null) {
            setButtonColor(colorBtn, ColorUtils.parseStringToRGB((String) value));
        }
    }

    /**
     * yzhang Comment method "resetButtonColor".
     *
     * @param colorBtn
     * @param value
     */
    private void setButtonColor(Button colorBtn, RGB rgb) {
        if (colorBtn.getImage() != null) {
            colorBtn.getImage().dispose();
        }

        ImageData id = createColorImage(colorBtn, rgb);
        Image image = new Image(colorBtn.getDisplay(), id, id.getTransparencyMask());
        addResourceDisposeListener(colorBtn, image);
        colorBtn.setImage(image);
    }

    private ImageData createColorImage(Button button, RGB color) {
        int width = SIZE_X - 5;
        int height = SIZE_Y - 5;

        RGB black = new RGB(0, 0, 0);
        PaletteData dataPalette = new PaletteData(new RGB[] { black, black, color });
        ImageData data = new ImageData(width, height, 4, dataPalette);
        data.transparentPixel = 0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (x == 0 || y == 0 || x == (width - 1) || y == (height - 1)) {
                    data.setPixel(x, y, 1);
                } else {
                    data.setPixel(x, y, 2);
                }
            }
        }

        return data;
    }
}
