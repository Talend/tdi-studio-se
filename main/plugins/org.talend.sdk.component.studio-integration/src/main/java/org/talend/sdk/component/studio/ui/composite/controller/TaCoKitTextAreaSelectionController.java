// ============================================================================
//
// Copyright (C) 2006-2021 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
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
import org.talend.designer.core.ui.editor.properties.controllers.AbstractTextAreaSelectionController;
import org.talend.sdk.component.studio.i18n.Messages;
import org.talend.sdk.component.studio.model.parameter.TextAreaSelectionParameter;

/**
 * created by hcyi on Jul 26, 2019
 * Detailled comment
 *
 */
public class TaCoKitTextAreaSelectionController extends AbstractTextAreaSelectionController {

    public TaCoKitTextAreaSelectionController(IDynamicProperty dp) {
            super(dp);
        }

        @Override
        protected SelectionListener createOnButtonClickedListener(final IElementParameter parameter) {
        final TextAreaSelectionParameter textAreaSelectionParameter = (TextAreaSelectionParameter) parameter;
            return new SelectionAdapter() {

                private final Job job;

                {
                    job = new Job(Messages.getString("suggestion.job.title")) {

                        @Override
                        protected IStatus run(IProgressMonitor monitor) {
                            monitor.subTask(Messages.getString("suggestion.job.subtask.retrieveValues"));
                        final Map<String, String> possibleValues = textAreaSelectionParameter.getSuggestionValues();
                            if (monitor.isCanceled()) {
                                return Status.CANCEL_STATUS;
                            }
                            monitor.subTask(Messages.getString("suggestion.job.subtask.openDialog"));
                            Display.getDefault().asyncExec(new Runnable() {
                                public void run() {
                                final ValueSelectionDialog dialog = new ValueSelectionDialog(composite.getShell(), possibleValues,
                                        true);
                                    if (dialog.open() == IDialogConstants.OK_ID) {
                                        final String selected = dialog.getSelectedValue();
                                    Text text = (Text) hashCurControls.get(textAreaSelectionParameter.getName());
                                        text.setText(selected);
                                    PropertyChangeCommand command = new PropertyChangeCommand(elem,
                                            textAreaSelectionParameter.getName(), selected);
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
                    job.schedule();
                }
            };
        }

    }
