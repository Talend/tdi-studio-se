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
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.IExternalNode;
import org.talend.core.ui.CoreUIPlugin;
import org.talend.core.ui.properties.tab.IDynamicProperty;
import org.talend.designer.core.model.components.ExternalUtilities;
import org.talend.designer.core.ui.editor.cmd.ExternalNodeChangeCommand;
import org.talend.designer.core.ui.editor.nodes.Node;

/**
 * DOC yzhang class global comment. Detailled comment <br/>
 *
 * $Id: ExternalController.java 1 2006-12-12 上午11:20:24 +0000 (上午11:20:24) yzhang $
 *
 */
public class ExternalController extends AbstractElementPropertySectionController {

    private static final String EXTERNAL = "EXTERNAL"; //$NON-NLS-1$

    /**
     * DOC yzhang ExternalController constructor comment.
     *
     * @param parameterBean
     */
    public ExternalController(IDynamicProperty dp) {
        super(dp);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.designer.core.ui.editor.properties2.editors.AbstractElementPropertySectionController#createCommand()
     */
    private Command createCommand() {
        Node node = (Node) elem;
        IExternalNode externalNode = ExternalUtilities.getExternalNodeReadyToOpen(node);

        if (externalNode == null) {
            MessageBox mBox = new MessageBox(composite.getShell(), SWT.ICON_ERROR);
            mBox.setText("Error"); //$NON-NLS-1$
            mBox.setMessage("Component plugin not found: " + node.getComponent().getPluginExtension()); //$NON-NLS-1$
            mBox.open();
        } else {
            if (externalNode.open(composite.getShell()) == SWT.OK) {
                return new ExternalNodeChangeCommand(node, externalNode);

            } else {
                externalNode.setExternalData(node.getExternalData());
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
        Button btnEdit;

        btnEdit = getWidgetFactory().createButton(subComposite, "", SWT.PUSH); //$NON-NLS-1$

        btnEdit.setImage(ImageProvider.getImage(CoreUIPlugin.getImageDescriptor(DOTS_BUTTON)));
        FormData data;
        btnEdit.setData(NAME, EXTERNAL);
        btnEdit.setData(PARAMETER_NAME, param.getName());
        // btnEdit.setEnabled(!param.isReadOnly());
        btnEdit.addSelectionListener(listenerSelection);
        if (elem instanceof Node) {
            btnEdit.setToolTipText(VARIABLE_TOOLTIP + param.getVariableName());
        }

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
                data.right = new FormAttachment(lastControl, currentLabelWidth + STANDARD_BUTTON_WIDTH);
            } else {
                data.left = new FormAttachment(0, currentLabelWidth);
                data.right = new FormAttachment(0, currentLabelWidth + STANDARD_BUTTON_WIDTH);
            }
        } else {
            data.left = new FormAttachment(labelLabel, 0, SWT.RIGHT);
            data.right = new FormAttachment(labelLabel, STANDARD_BUTTON_WIDTH, SWT.RIGHT);
        }
        data.top = new FormAttachment(0, top);
        labelLabel.pack();
        btnEdit.pack();
        int labelHeight = labelLabel.getSize().y;
        int btnHeight = btnEdit.getSize().y;
        if (labelHeight < btnHeight) {
            data.height = labelHeight;
        }
        btnEdit.setLayoutData(data);
        // **************************
        hashCurControls.put(param.getName(), btnEdit);

        Point initialSize = btnEdit.computeSize(SWT.DEFAULT, SWT.DEFAULT);
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
        Button btnEdit = getWidgetFactory().createButton(subComposite, "", SWT.PUSH); //$NON-NLS-1$
        btnEdit.setImage(ImageProvider.getImage(CoreUIPlugin.getImageDescriptor(DOTS_BUTTON)));
        Point initialSize = btnEdit.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        btnEdit.dispose();
        return initialSize.y + ITabbedPropertyConstants.VSPACE;
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

    SelectionListener listenerSelection = new SelectionListener() {

        @Override
        public void widgetDefaultSelected(SelectionEvent e) {
            // TODO Auto-generated method stub

        }

        @Override
        public void widgetSelected(SelectionEvent e) {
            Command cmd = createCommand();
            executeCommand(cmd);

        }

    };

    @Override
    public void refresh(IElementParameter param, boolean check) {
        // TODO Auto-generated method stub

    }

}
