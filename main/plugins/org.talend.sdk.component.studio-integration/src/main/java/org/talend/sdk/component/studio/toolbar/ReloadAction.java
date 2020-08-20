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

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.talend.commons.ui.gmf.util.DisplayUtils;
import org.talend.sdk.component.studio.Lookups;
import org.talend.sdk.component.studio.TaCoKitGenericProvider;

public class ReloadAction extends AbstractHandler {

    @Override
    public Object execute(final ExecutionEvent executionEvent) {
        Display.getDefault().asyncExec(() -> {
            final String status = reload(new NullProgressMonitor());
            MessageDialog.openInformation(DisplayUtils.getDefaultShell(false), "Reload ended", status);
        });
        return null;
    }

    public String reload(final IProgressMonitor monitor) {
        String status = Lookups.manager().reload();
        doRefresh();
        return status;
    }

    private void doRefresh() {
        Lookups.taCoKitCache().reset();
        new TaCoKitGenericProvider().loadComponentsFromExtensionPoint();
    }
}
