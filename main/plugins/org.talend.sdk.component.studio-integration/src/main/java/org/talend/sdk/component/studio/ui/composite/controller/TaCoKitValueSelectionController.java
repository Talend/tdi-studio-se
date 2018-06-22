/**
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
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

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Text;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.ui.properties.tab.IDynamicProperty;
import org.talend.designer.core.ui.editor.cmd.PropertyChangeCommand;
import org.talend.sdk.component.studio.model.parameter.ValueSelectionParameter;

public class TaCoKitValueSelectionController extends AbstractValueSelectionController {

    public TaCoKitValueSelectionController(IDynamicProperty dp) {
        super(dp);
    }

    @Override
    protected SelectionListener createOnButtonClickedListener(final IElementParameter parameter) {
        final ValueSelectionParameter valueSelectionParameter = (ValueSelectionParameter) parameter; 
        return new SelectionAdapter() {
            
            @Override
            public void widgetSelected(final SelectionEvent e) {
                final ValueSelectionDialog dialog = new ValueSelectionDialog(composite.getShell(), valueSelectionParameter.getSuggestionValues());
                if (dialog.open() == IDialogConstants.OK_ID) {
                    final String result = dialog.getResult();
                    Text text = (Text) hashCurControls.get(valueSelectionParameter.getName());
                    text.setText(result);
                    PropertyChangeCommand command = new PropertyChangeCommand(elem, valueSelectionParameter.getName(), result);
                    executeCommand(command);
                }
            }
        };
    }

}
