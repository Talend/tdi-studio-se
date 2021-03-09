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

import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.ui.properties.tab.IDynamicProperty;
import org.talend.designer.core.ui.editor.cmd.PropertyChangeCommand;
import org.talend.designer.core.ui.editor.properties.controllers.AbstractValueSelectionController;
import org.talend.sdk.component.studio.i18n.Messages;
import org.talend.sdk.component.studio.model.parameter.ValueSelectionParameter;

public class TaCoKitValueSelectionController extends AbstractValueSelectionController {

    public TaCoKitValueSelectionController(IDynamicProperty dp) {
        super(dp);
    }

    @Override
    protected SelectionListener createOnButtonClickedListener(final IElementParameter parameter) {
        final ValueSelectionParameter valueSelectionParameter = (ValueSelectionParameter) parameter;
        return new SelectionAdapter() {

            private final Job job;
            {
                job = new Job(Messages.getString("suggestion.job.title")) {

                    @Override
                    protected IStatus run(IProgressMonitor monitor) {
                        monitor.subTask(Messages.getString("suggestion.job.subtask.retrieveValues"));
                        final Map<String, String> possibleValues = valueSelectionParameter.getSuggestionValues();
                        if (monitor.isCanceled()) {
                            return Status.CANCEL_STATUS;
                        }
                        monitor.subTask(Messages.getString("suggestion.job.subtask.openDialog"));
                        Display.getDefault().syncExec(new Runnable() {
                            public void run() {
                                final ValueSelectionDialog dialog = new ValueSelectionDialog(composite.getShell(), possibleValues,
                                        false);
                                if (dialog.open() == IDialogConstants.OK_ID) {
                                    final String selected = dialog.getSelectedValue();
                                    Text text = (Text) hashCurControls.get(valueSelectionParameter.getName());
                                    text.setText(selected);
                                    PropertyChangeCommand command = new PropertyChangeCommand(elem, valueSelectionParameter.getName(), selected);
                                    executeCommand(command);
                                }
                            }
                        });
                        monitor.done();
                        return Status.OK_STATUS;
                    }

                };
                job.setUser(true);
            }

            @Override
            public void widgetSelected(final SelectionEvent e) {
                int state = job.getState();
                if (Job.NONE == state) {
                    job.schedule();
                }
            }
        };
    }

}
