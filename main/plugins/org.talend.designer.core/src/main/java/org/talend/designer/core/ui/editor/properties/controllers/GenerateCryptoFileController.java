// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
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

import org.apache.commons.lang3.StringUtils;
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
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.ITDQCryptoFileService;
import org.talend.core.model.process.ElementParameterParser;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.utils.RepositoryManagerHelper;
import org.talend.core.ui.properties.tab.IDynamicProperty;
import org.talend.designer.core.DesignerPlugin;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.repository.ui.views.IRepositoryView;

public class GenerateCryptoFileController extends AbstractElementPropertySectionController {

    public GenerateCryptoFileController(IDynamicProperty dp) {
        super(dp);
        // TODO Auto-generated constructor stub
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent evt) {
        // TODO Auto-generated method stub

    }

    ITDQCryptoFileService service = null;
    SelectionListener listenerSelection = new SelectionListener() {

        public void widgetDefaultSelected(SelectionEvent e) {

        }

        public void widgetSelected(SelectionEvent e) {
            service = GlobalServiceRegister.getDefault().getService(ITDQCryptoFileService.class);
            if (service == null) {
                // nothing need to do if the service is not exist
                return;
            }
            Node node = (Node) elem;
            String cryptoFilePath =
                    removeQuotes(service
                            .getOriginalValue(node, ElementParameterParser.getValue(node, "__CRYPTO_FILE_PATH__")));
            String passwordM1 =
                    removeQuotes(
                            service.getOriginalValue(node, ElementParameterParser.getValue(node, "__PASSWORD_M1__")));

            boolean isParameterValidation = service.checkParameterValidation(cryptoFilePath, passwordM1);
            if (!isParameterValidation) {
                return;
            }

            String cryptoMethod = service.getCryptoMethod();
            if (!StringUtils.isNotEmpty(cryptoMethod)) {
                return;
            }

            // extract check parameter before start a command so that the customer no chance to cancel it again
            executeCommand(new Command() {

                @Override
                public void execute() {
                    Display disp = Display.getCurrent();
                    if (disp == null) {
                        disp = Display.getDefault();
                    }
                    disp.syncExec(new Runnable() {

                        public void run() {
                            service.generateCryptoFile(passwordM1, cryptoMethod, cryptoFilePath);
                        }
                    });
                }
            });

            IRepositoryView repoView = RepositoryManagerHelper.findRepositoryView();
            if (repoView != null) {
                repoView.refreshView();
            }
        }
    };

    private String removeQuotes(String text) {
        if (text == null) {
            return null;
        }
        if (text.startsWith("\"")) {
            text = text.substring(1);
        }
        if (text.endsWith("\"")) {
            text = text.substring(0, text.length() - 1);
        }
        return text;
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
        Button btnEdit;
        btnEdit = getWidgetFactory().createButton(subComposite, null, SWT.PUSH); // $NON-NLS-1$
        btnEdit.setImage(ImageProvider.getImage(DesignerPlugin.getImageDescriptor("icons/routine_generate.gif")));//$NON-NLS-1$
        FormData data;
        btnEdit.addSelectionListener(listenerSelection);

        if (elem instanceof Node) {
            btnEdit.setToolTipText(VARIABLE_TOOLTIP + param.getVariableName());
        }
        CLabel labelLabel = getWidgetFactory().createCLabel(subComposite, param.getDisplayName()); // $NON-NLS-1$
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
                data.right = new FormAttachment(lastControl, currentLabelWidth + STANDARD_BUTTON_WIDTH + 8);
            } else {
                data.left = new FormAttachment(0, currentLabelWidth);
                data.right = new FormAttachment(0, currentLabelWidth + STANDARD_BUTTON_WIDTH + 8);
            }
        } else {
            data.left = new FormAttachment(labelLabel, 0, SWT.RIGHT);
            data.right = new FormAttachment(labelLabel, STANDARD_BUTTON_WIDTH + 8, SWT.RIGHT);
        }
        data.top = new FormAttachment(0, top);
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
        // TODO Auto-generated method stub
        return 0;
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
        // TODO Auto-generated method stub

    }
}
