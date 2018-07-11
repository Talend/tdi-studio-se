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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.talend.commons.ui.gmf.util.DisplayUtils;
import org.talend.sdk.component.studio.i18n.Messages;
import org.talend.sdk.component.studio.model.action.Action;
import org.talend.sdk.component.studio.model.action.ActionParameter;

/**
 * Asynchronous Action
 */
public class AsyncAction extends Job implements TacokitCommand {

    private final Action action;

    public AsyncAction(final Action action) {
        super(Messages.getString("action.execution.progress"));
        this.action = action;
        setUser(true);
    }

    @Override
    protected IStatus run(final IProgressMonitor iProgressMonitor) {
        final Map<String, String> result = action.callback();
        refreshUi(result);
        return Status.OK_STATUS;
    }

    private void refreshUi(final Map<String, String> result) {
        Display.getDefault().asyncExec(() -> {
            final String dialogTitle = Messages.getString("action.result.title");
            if (OK.equals(result.get(STATUS))) {
                MessageDialog.openInformation(DisplayUtils.getDefaultShell(), dialogTitle, result.get(MESSAGE));
            } else {
                MessageDialog.openError(DisplayUtils.getDefaultShell(), dialogTitle, result.get(MESSAGE));
            }
        });

    }

    @Override
    public void exec() {
        this.schedule();
    }

    @Override
    public void addParameter(final ActionParameter parameter) {
        action.addParameter(parameter);
    }
}
