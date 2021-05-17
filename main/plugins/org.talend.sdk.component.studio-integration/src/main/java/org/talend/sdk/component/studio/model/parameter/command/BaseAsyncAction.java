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
package org.talend.sdk.component.studio.model.parameter.command;

import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.talend.sdk.component.studio.i18n.Messages;
import org.talend.sdk.component.studio.model.action.Action;
import org.talend.sdk.component.studio.model.action.IActionParameter;

/**
 * Asynchronous Action
 */
public abstract class BaseAsyncAction<T> extends Job implements TacokitCommand {

    private final Action<T> action;

    public BaseAsyncAction(final Action<T> action) {
        super(Messages.getString("action.execution.progress"));
        this.action = action;
        setUser(true);
    }

    @Override
    protected IStatus run(final IProgressMonitor iProgressMonitor) {
        onResult(action.callback());
        return Status.OK_STATUS;
    }

    protected abstract void onResult(Map<String, T> result);

    @Override
    public void exec() {
        this.schedule();
    }

    @Override
    public void addParameter(final IActionParameter parameter) {
        action.addParameter(parameter);
    }
}
