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
import org.eclipse.gef.commands.Command;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.talend.sdk.component.studio.i18n.Messages;
import org.talend.sdk.component.studio.model.action.Action;
import org.talend.sdk.component.studio.model.action.ActionParameter;

/**
 * Synchronous action
 */
public class SyncAction extends Command implements TacokitCommand {

    private final Action action;

    public SyncAction(final Action action) {
        this.action = action;
    }

    @Override
    public void execute() {
        final Map<String, String> result = action.callback();
        final String dialogTitle = Messages.getString("action.result.title");
        if (OK.equals(result.get(STATUS))) {
            MessageDialog.openInformation(new Shell(), dialogTitle, result.get(MESSAGE));
        } else {
            MessageDialog.openError(new Shell(), dialogTitle, result.get(MESSAGE));
        }
    }
    
    /**
     * Adds Action method {@code parameter}. It should have unique name/path
     * 
     * @param elementParameterName name of ElementParameter, which provides values for ActionParameter
     * @param parameter ActionParameter to be added
     */
    @Override
    public void addParameter(final String elementParameterName, final ActionParameter parameter) {
        action.addParameter(elementParameterName, parameter);
    }

    @Override
    public void exec() {
        execute();
    }

    public Action getAction() {
        return this.action;
    }
}
