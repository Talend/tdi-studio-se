/**
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
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
package org.talend.sdk.component.studio.ui.guessschema;

import static java.util.stream.Collectors.toList;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import org.eclipse.gef.commands.CommandStack;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.ui.runtime.exception.ExceptionMessageDialog;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.ITDQRuleService;
import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.MetadataColumn;
import org.talend.core.model.metadata.MetadataTable;
import org.talend.core.model.metadata.MetadataToolHelper;
import org.talend.core.model.process.IContext;
import org.talend.core.model.process.IElement;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.ui.metadata.dialog.MetadataDialog;
import org.talend.core.utils.KeywordsValidator;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.ui.editor.cmd.ChangeMetadataCommand;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.properties.controllers.uidialog.OpenContextChooseComboDialog;
import org.talend.sdk.component.studio.i18n.Messages;

public class GuessSchemaSelectionAdapter extends SelectionAdapter {

    private static final String CONTEXT_CHOOSE_DIALOG_TITLE = "Choose a context for query :";//$NON-NLS-1$

    private IElement elem;

    private IElementParameter elementParameter;

    private CommandStack commandStack;

    private Composite composite;

    public GuessSchemaSelectionAdapter(final IElement elem, final IElementParameter elementParameter,
            final Composite composite,
            final CommandStack commandStack) {
        this.elem = elem;
        this.elementParameter = elementParameter;
        this.composite = composite;
        this.commandStack = commandStack;

    }

    private IContext selectContext() {
        Shell parentShell = composite.getShell();
        List<IContext> allContexts = Node.class.cast(elementParameter.getElement())
                .getProcess().getContextManager().getListContext();
        OpenContextChooseComboDialog dialog = new OpenContextChooseComboDialog(parentShell, allContexts);
        dialog.create();
        dialog.getShell().setText(CONTEXT_CHOOSE_DIALOG_TITLE);
        IContext selectContext = null;
        // job only have defoult context,or the query isn't context mode
        if (allContexts.size() == 1) {
            selectContext = Node.class.cast(elementParameter.getElement())
                    .getProcess().getContextManager().getDefaultContext();
        } else if (Window.OK == dialog.open()) {
            selectContext = dialog.getSelectedContext();
        }
        return selectContext;
    }

    @Override
    public void widgetSelected(final SelectionEvent event) {
        super.widgetSelected(event);
        final IContext context = selectContext();
        if (context == null) {
            return;
        }

        final GuessSchemaRunnable guessSchema = new GuessSchemaRunnable(context, elementParameter);
        try {
            new ProgressMonitorDialog(this.composite.getShell())
                    .run(true, true, guessSchema); //this block until the guessSchema is done
        } catch (InvocationTargetException | InterruptedException e) {
            Display.getDefault().asyncExec(() -> {
                ExceptionMessageDialog.openError(composite.getShell(),
                        Messages.getString("guessSchema.dialog.error.title"), //$NON-NLS-1$
                        Messages.getString("guessSchema.dialog.error.msg.default"), e); //$NON-NLS-1$
            });

            if (InterruptedException.class.isInstance(e)) {
                Thread.currentThread().interrupt();
            }
        }

        if (guessSchema.isCanceled()) {
            return; // task was canceled
        }

        final String schema = guessSchema.getSchema();
        if (schema == null) {
            ExceptionMessageDialog.openError(composite.getShell(),
                    Messages.getString("guessSchema.dialog.error.title"), //$NON-NLS-1$
                    Messages.getString("guessSchema.dialog.error.msg.default"));
            return;
        }

        final Node node = Node.class.cast(elementParameter.getElement());
        IMetadataTable newMeta = buildMetadata(schema, Node.class.cast(elementParameter.getElement()));
        if (newMeta == null) {
            return;
        }
        MetadataDialog metaDialog = new MetadataDialog(composite.getShell(), newMeta, node, commandStack);
        metaDialog.setText(Messages.getString("guessSchema.dialog.title", node.getLabel())); //$NON-NLS-1$
        if (metaDialog.open() == MetadataDialog.OK) {
            final IMetadataTable outputMetaCopy = metaDialog.getOutputMetaData();
            boolean modified = false;
            final IMetadataTable old = node.getMetadataTable(elementParameter.getName());
            if (!outputMetaCopy.sameMetadataAs(old, IMetadataColumn.OPTIONS_NONE)) {
                modified = true;
            }

            if (modified) {
                IElementParameter switchParam =
                        elem.getElementParameter(EParameterName.REPOSITORY_ALLOW_AUTO_SWITCH.getName());
                if (switchParam != null) {
                    switchParam.setValue(Boolean.FALSE);
                }
                IElementParameter param = node.getElementParameter(elementParameter.getName());
                final ChangeMetadataCommand cmd = new ChangeMetadataCommand(node, param, old, outputMetaCopy);
                if (commandStack != null) {
                    commandStack.execute(cmd);
                } else {
                    cmd.execute();
                }
            }
        }
    }

    private IMetadataTable buildMetadata(final String schema, final Node inputNode) {
        final String[] lines = schema.split("\n");
        List<String[]> schemaContent = Stream.of(lines)
                .filter(Objects::nonNull)
                .filter(l -> !l.trim().isEmpty())
                .filter(l -> l.contains(";"))
                .map(l -> l.split(";"))
                .collect(toList());

        if (schemaContent == null || schemaContent.isEmpty()) {
            return null;
        }
        List<String> columnLabels = new ArrayList<>();
        List<IMetadataColumn> columns = new ArrayList<>();

        int i = -1;
        for (String[] row : schemaContent) {
            i++;
            IMetadataColumn oneColum = new MetadataColumn();
            String labelName = row[0];
            String name = labelName;
            String sub = ""; //$NON-NLS-1$
            String sub2 = ""; //$NON-NLS-1$
            if (labelName != null && labelName.length() > 0 && labelName.startsWith("_")) { //$NON-NLS-1$
                sub = labelName.substring(1);
                if (sub.length() > 0) {
                    sub2 = sub.substring(1);
                }
            }
            if (KeywordsValidator.isKeyword(labelName) || KeywordsValidator.isKeyword(sub)
                    || KeywordsValidator.isKeyword(sub2)) {
                labelName = "_" + labelName; //$NON-NLS-1$
            }
            oneColum.setLabel(MetadataToolHelper.validateColumnName(labelName, i, columnLabels));
            oneColum.setOriginalDbColumnName(name);
            oneColum.setPrecision(0);
            oneColum.setLength(0);
            try {
                oneColum.setTalendType(row[1]);

            } catch (Exception e) {
                /*
                 * the table have no data at all ,to do nothing
                 */
                ExceptionHandler.process(e);
            }
            // get if a column is nullable from the temp file genenrated by GuessSchemaProcess.java
            oneColum.setNullable(true);
            columns.add(oneColum);
            columnLabels.add(oneColum.getLabel());
        }

        if (columns.size() > 0) {
            IElementParameter dqRule = elem.getElementParameter("DQRULES_LIST");
            if (dqRule != null) {
                ITDQRuleService ruleService = null;
                try {
                    ruleService =
                            (ITDQRuleService) GlobalServiceRegister.getDefault().getService(ITDQRuleService.class);
                } catch (RuntimeException e) {
                    // nothing to do
                }
                IElementParameter queryParam = elem.getElementParameter("QUERY");
                if (ruleService != null && queryParam != null) {
                    ruleService.updateOriginalColumnNames(columns, queryParam);
                }
            }
        }

        IMetadataTable metadataTable = new MetadataTable();
        /* for bug 20973 */
        if (metadataTable.getTableName() == null) {
            metadataTable.setTableName(inputNode.getUniqueName());
        }
        metadataTable.setListColumns(columns);
        return metadataTable;
    }

}
