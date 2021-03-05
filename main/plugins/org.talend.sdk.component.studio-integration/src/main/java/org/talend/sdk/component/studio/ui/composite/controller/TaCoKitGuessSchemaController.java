/**
 * Copyright (C) 2006-2021 Talend Inc. - www.talend.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.talend.sdk.component.studio.ui.composite.controller;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.utils.TalendTextUtils;
import org.talend.core.ui.properties.tab.IDynamicProperty;
import org.talend.sdk.component.studio.ui.guessschema.GuessSchemaSelectionAdapter;

/**
 * DOC cmeng class global comment. Detailled comment
 */
public class TaCoKitGuessSchemaController extends AbstractTaCoKitController {

    private static final String GUESS_SCHEMA_NAME = "Guess schema"; //$NON-NLS-1$

    private static final String SCHEMA = "SCHEMA"; //$NON-NLS-1$

    public TaCoKitGuessSchemaController(final IDynamicProperty dp) {
        super(dp);

    }

    @Override
    public Control createControl(final Composite subComposite, final IElementParameter param, final int numInRow,
            final int nbInRow, final int top, final Control lastControl) {
        this.curParameter = param;
        this.paramFieldType = param.getFieldType();
        FormData data;

        final Button btnCmd = new Button(subComposite, SWT.NONE);
        btnCmd.setText(GUESS_SCHEMA_NAME);

        data = new FormData();

        GC gc = new GC(btnCmd);
        gc.dispose();

        data.left = new FormAttachment(lastControl, 0);
        data.top = new FormAttachment(0, top);
        data.height = STANDARD_HEIGHT + 2;
        btnCmd.setLayoutData(data);
        btnCmd.setData(PARAMETER_NAME, param.getName());
        btnCmd.setData(NAME, SCHEMA);
        btnCmd.setData(SCHEMA, checkQuotes((String) param.getValue()));
        btnCmd.setEnabled(!param.isReadOnly());
        btnCmd.addSelectionListener(new GuessSchemaSelectionAdapter(elem, curParameter, composite, getCommandStack()));

        return btnCmd;
    }

    private String checkQuotes(final String str) {
        if (str == null || "".equals(str)) { //$NON-NLS-1$
            return TalendTextUtils.addQuotes(str);
        }

        return str;
    }
}
