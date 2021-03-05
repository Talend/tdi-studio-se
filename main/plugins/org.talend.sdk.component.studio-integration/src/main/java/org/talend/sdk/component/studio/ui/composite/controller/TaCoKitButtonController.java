/**
 * Copyright (C) 2006-2021 Talend Inc. - www.talend.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.talend.sdk.component.studio.ui.composite.controller;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.ui.CoreUIPlugin;
import org.talend.core.ui.properties.tab.IDynamicProperty;
import org.talend.sdk.component.studio.model.parameter.ButtonParameter;

/**
 * Creates SWT Button control
 */
public class TaCoKitButtonController extends AbstractTaCoKitController {

    public TaCoKitButtonController(final IDynamicProperty dp) {
        super(dp);
    }

    /**
     * Creates Button control
     * Sets current row size in the end of the method. It is required to have proper position for subsequent controls
     *
     * @param subComposite composite which will contain created control
     * @param param ElementParameter instance
     * @param numInRow number of created control in the current row
     * @param nbInRow total number of all controls in the current row
     * @param top offset from top of the composite. It is computed as row size of all previously added controls + spaces
     * between controls
     * @param lastControl previously added control in the current row. It is null, if it is first control in the row
     * @return created button
     */
    @Override
    public Control createControl(final Composite subComposite, final IElementParameter param, final int numInRow,
            final int nbInRow, final int top, final Control lastControl) {
        Button button = new Button(subComposite, SWT.NONE);
        button.setBackground(subComposite.getBackground());
        if (param.getDisplayName().equals("")) {
            button.setImage(ImageProvider.getImage(CoreUIPlugin.getImageDescriptor(DOTS_BUTTON)));
        } else {
            button.setText(param.getDisplayName());
        }

        FormData data = new FormData();
        if (isInWizard()) {
            if (lastControl != null) {
                data.right = new FormAttachment(lastControl, 0);
            } else {
                data.right = new FormAttachment(100, -ITabbedPropertyConstants.HSPACE);
            }
        } else {
            if (lastControl != null) {
                data.left = new FormAttachment(lastControl, 0);
            } else {
                data.left = new FormAttachment((((numInRow - 1) * MAX_PERCENT) / nbInRow), 0);
            }
        }
        data.top = new FormAttachment(0, top);
        data.height = STANDARD_HEIGHT + 2;
        button.setLayoutData(data);
        button.setEnabled(!param.isReadOnly());
        button.setData(param);
        hashCurControls.put(param.getName(), button);
        button.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent e) {
                ButtonParameter btn = (ButtonParameter) ((Button) e.getSource()).getData();
                btn.getCommand().exec();
            }
        });
        Point initialSize = button.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        dynamicProperty.setCurRowSize(initialSize.y + ITabbedPropertyConstants.VSPACE);
        return button;
    }

}
