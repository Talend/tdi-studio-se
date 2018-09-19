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
package org.talend.sdk.component.studio.model.parameter.command;

import static org.talend.sdk.component.studio.model.action.Action.MESSAGE;
import static org.talend.sdk.component.studio.model.action.Action.OK;
import static org.talend.sdk.component.studio.model.action.Action.STATUS;

import java.util.Map;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.talend.commons.ui.gmf.util.DisplayUtils;
import org.talend.sdk.component.studio.i18n.Messages;
import org.talend.sdk.component.studio.model.action.Action;

/**
 * Asynchronous Action
 */
public class AsyncAction extends BaseAsyncAction<String> {

    public AsyncAction(final Action<String> action) {
        super(action);
    }


    @Override
    protected void onResult(final Map<String, String> result) {
        Display.getDefault().asyncExec(() -> {
            final String dialogTitle = Messages.getString("action.result.title");
            if (OK.equals(result.get(STATUS))) {
                MessageDialog.openInformation(DisplayUtils.getDefaultShell(), dialogTitle, result.get(MESSAGE));
            } else {
                MessageDialog.openError(DisplayUtils.getDefaultShell(), dialogTitle, result.get(MESSAGE));
            }
        });
    }
}
