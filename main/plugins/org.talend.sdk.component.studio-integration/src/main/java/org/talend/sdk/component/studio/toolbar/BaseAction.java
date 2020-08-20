/**
 * Copyright (C) 2006-2019 Talend Inc. - www.talend.com
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
package org.talend.sdk.component.studio.toolbar;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

abstract class BaseAction implements IWorkbenchWindowActionDelegate, Runnable {

    @Override
    public void run(final IAction action) {
        run();
    }

    @Override
    public void selectionChanged(final IAction action, final ISelection selection) {
        // no-op
    }

    @Override
    public void dispose() {
        // no-op
    }

    @Override
    public void init(final IWorkbenchWindow window) {
        // no-op
    }
}
